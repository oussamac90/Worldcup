package com.goalkeeperdash.user.service;

import com.goalkeeperdash.common.config.AppProperties;
import com.goalkeeperdash.common.domain.SeasonService;
import com.goalkeeperdash.common.error.ApiException;
import com.goalkeeperdash.common.error.ErrorCode;
import com.goalkeeperdash.common.security.JwtService;
import com.goalkeeperdash.user.auth.OidcTokenVerifier;
import com.goalkeeperdash.user.auth.VerifiedIdentity;
import com.goalkeeperdash.user.domain.AuthProvider;
import com.goalkeeperdash.user.domain.Nation;
import com.goalkeeperdash.user.domain.User;
import com.goalkeeperdash.user.domain.UserIdentity;
import com.goalkeeperdash.user.repo.NationRepository;
import com.goalkeeperdash.user.repo.UserIdentityRepository;
import com.goalkeeperdash.user.repo.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Orchestrates login: verifies the IdP token server-side, finds-or-creates the
 * {@code User}+{@code UserIdentity}, then issues the app's own access JWT + refresh
 * token (§4.1/§4.2). Depends only on {@code common} contracts for season context.
 */
@Service
public class AuthService {

    private final OidcTokenVerifier verifier;
    private final UserRepository users;
    private final UserIdentityRepository identities;
    private final NationRepository nations;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokens;
    private final SeasonService seasonService;
    private final DisplayNamePolicy displayNamePolicy;
    private final boolean devLoginEnabled;

    public AuthService(OidcTokenVerifier verifier, UserRepository users, UserIdentityRepository identities,
                       NationRepository nations, JwtService jwtService, RefreshTokenService refreshTokens,
                       SeasonService seasonService, DisplayNamePolicy displayNamePolicy, AppProperties props) {
        this.verifier = verifier;
        this.users = users;
        this.identities = identities;
        this.nations = nations;
        this.jwtService = jwtService;
        this.refreshTokens = refreshTokens;
        this.seasonService = seasonService;
        this.displayNamePolicy = displayNamePolicy;
        this.devLoginEnabled = props.oidc().devLoginEnabled();
    }

    @Transactional
    public AuthResult login(AuthProvider provider, String idToken) {
        VerifiedIdentity identity;
        if (provider == AuthProvider.DEV) {
            if (!devLoginEnabled) {
                throw new ApiException(ErrorCode.FORBIDDEN, "Dev login is disabled");
            }
            // Dev path: idToken is treated as an opaque subject (NOT verified). Never enable in prod.
            identity = new VerifiedIdentity(AuthProvider.DEV, "dev:" + idToken, idToken + "@dev.local");
        } else {
            identity = verifier.verify(provider, idToken);
        }

        User user = identities.findByProviderAndSubject(identity.provider(), identity.subject())
                .map(existing -> users.findById(existing.getUserId())
                        .orElseThrow(() -> new ApiException(ErrorCode.INTERNAL_ERROR, "Identity without user")))
                .orElseGet(() -> createUser(identity));

        if (user.getStatus() == com.goalkeeperdash.user.domain.UserStatus.BANNED) {
            throw new ApiException(ErrorCode.FORBIDDEN, "Account is banned");
        }

        return issueTokens(user);
    }

    @Transactional
    public AuthResult refresh(String refreshToken) {
        RefreshTokenService.Rotation rotation = refreshTokens.rotate(refreshToken);
        User user = users.findById(rotation.userId())
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_TOKEN, "Unknown user"));
        UUID seasonId = seasonService.activeSeasonIdOrNull();
        String accessToken = jwtService.issueAccessToken(user.getId(), nationCode(user), seasonId, List.of("USER"));
        return new AuthResult(accessToken, rotation.newRefreshToken(), user, nationCode(user));
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokens.revoke(refreshToken);
    }

    private User createUser(VerifiedIdentity identity) {
        User user = new User();
        user.setDisplayName(displayNamePolicy.generateDefault());
        users.save(user);

        UserIdentity ui = new UserIdentity(user.getId(), identity.provider(), identity.subject(), identity.email());
        identities.save(ui);
        return user;
    }

    private AuthResult issueTokens(User user) {
        UUID seasonId = seasonService.activeSeasonIdOrNull();
        String accessToken = jwtService.issueAccessToken(user.getId(), nationCode(user), seasonId, List.of("USER"));
        String refreshToken = refreshTokens.issue(user.getId());
        return new AuthResult(accessToken, refreshToken, user, nationCode(user));
    }

    private String nationCode(User user) {
        if (user.getNationId() == null) return null;
        return nations.findById(user.getNationId()).map(Nation::getCode).orElse(null);
    }

    public record AuthResult(String accessToken, String refreshToken, User user, String nationCode) {}
}
