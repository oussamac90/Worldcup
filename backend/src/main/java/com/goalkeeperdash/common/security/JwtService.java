package com.goalkeeperdash.common.security;

import com.goalkeeperdash.common.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Issues and verifies the application's own short-lived access JWT (HS256).
 * Distinct from the IdP tokens (which are verified in the user module) and from
 * the opaque refresh tokens (stored hashed in Postgres).
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final String issuer;
    private final int accessTtlMin;

    public JwtService(AppProperties props) {
        byte[] secret = props.jwt().secret().getBytes(StandardCharsets.UTF_8);
        if (secret.length < 32) {
            throw new IllegalStateException("APP_JWT_SECRET must be at least 32 bytes for HS256");
        }
        this.key = Keys.hmacShaKeyFor(secret);
        this.issuer = props.jwt().issuer();
        this.accessTtlMin = props.jwt().accessTtlMin();
    }

    /**
     * @param userId  the application user id (becomes {@code sub})
     * @param nation  ISO alpha-3 nation code, or null if not yet chosen
     * @param seasonId active season id, or null
     * @param roles   granted roles (e.g. ["USER"])
     */
    public String issueAccessToken(UUID userId, String nation, UUID seasonId, List<String> roles) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(issuer)
                .subject(userId.toString())
                .issuedAt(java.util.Date.from(now))
                .expiration(java.util.Date.from(now.plus(accessTtlMin, ChronoUnit.MINUTES)))
                .claim("nation", nation)
                .claim("season", seasonId == null ? null : seasonId.toString())
                .claim("roles", roles)
                .signWith(key)
                .compact();
    }

    /** Parses & verifies an access token, returning its claims, or throws on any failure. */
    public Claims parse(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(issuer)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            throw new JwtException("Invalid access token", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static List<String> rolesOf(Claims claims) {
        Object roles = claims.get("roles");
        if (roles instanceof List<?> list) {
            return (List<String>) list;
        }
        return List.of();
    }

    public Map<String, Object> debugClaims(Claims claims) {
        return Map.of(
                "sub", claims.getSubject(),
                "nation", String.valueOf(claims.get("nation")),
                "season", String.valueOf(claims.get("season")),
                "exp", String.valueOf(claims.getExpiration()));
    }
}
