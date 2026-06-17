package com.goalkeeperdash.user.web;

import com.goalkeeperdash.common.error.ApiException;
import com.goalkeeperdash.common.error.ErrorCode;
import com.goalkeeperdash.common.ratelimit.RateLimiterService;
import com.goalkeeperdash.user.domain.AuthProvider;
import com.goalkeeperdash.user.service.AuthService;
import com.goalkeeperdash.user.web.dto.AuthDtos;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

/**
 * Auth endpoints (§4.6). Each verifies an IdP ID token server-side (except the
 * gated dev path), then issues the app's access JWT + refresh token.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final RateLimiterService rateLimiter;

    public AuthController(AuthService authService, RateLimiterService rateLimiter) {
        this.authService = authService;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping("/{provider}")
    public AuthDtos.AuthResponse login(@PathVariable String provider,
                                       @Valid @RequestBody AuthDtos.LoginRequest body,
                                       HttpServletRequest request) {
        enforceAuthRateLimit(request);
        AuthProvider parsed = parseProvider(provider);
        AuthService.AuthResult result = authService.login(parsed, body.idToken());
        return toResponse(result);
    }

    @PostMapping("/refresh")
    public AuthDtos.AuthResponse refresh(@Valid @RequestBody AuthDtos.RefreshRequest body,
                                         HttpServletRequest request) {
        enforceAuthRateLimit(request);
        return toResponse(authService.refresh(body.refreshToken()));
    }

    @PostMapping("/logout")
    public void logout(@Valid @RequestBody AuthDtos.LogoutRequest body) {
        authService.logout(body.refreshToken());
    }

    private AuthProvider parseProvider(String provider) {
        try {
            return AuthProvider.valueOf(provider.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Unsupported provider: " + provider);
        }
    }

    private void enforceAuthRateLimit(HttpServletRequest request) {
        if (!rateLimiter.tryConsumeAuth(clientIp(request))) {
            throw new ApiException(ErrorCode.RATE_LIMITED, "Too many auth attempts, slow down");
        }
    }

    private String clientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private AuthDtos.AuthResponse toResponse(AuthService.AuthResult result) {
        var user = result.user();
        return new AuthDtos.AuthResponse(
                result.accessToken(),
                result.refreshToken(),
                new AuthDtos.UserDto(user.getId(), user.getDisplayName(), result.nationCode()));
    }
}
