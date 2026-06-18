package com.goalkeeperdash.common.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Validates the {@code Authorization: Bearer <jwt>} header on API requests and
 * populates the security context with an {@link AppUserPrincipal}. Invalid/absent
 * tokens leave the context unauthenticated (the security chain rejects as needed).
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Claims claims = jwtService.parse(token);
                UUID userId = UUID.fromString(claims.getSubject());
                String nation = (String) claims.get("nation");
                String seasonRaw = (String) claims.get("season");
                UUID seasonId = (seasonRaw == null || "null".equals(seasonRaw)) ? null : UUID.fromString(seasonRaw);

                List<SimpleGrantedAuthority> authorities = JwtService.rolesOf(claims).stream()
                        .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                        .toList();

                var principal = new AppUserPrincipal(userId, nation, seasonId);
                var auth = new UsernamePasswordAuthenticationToken(principal, null, authorities);
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception ignored) {
                // Leave unauthenticated; protected endpoints will return 401.
            }
        }
        chain.doFilter(request, response);
    }
}
