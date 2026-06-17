package com.goalkeeperdash.user.service;

import com.goalkeeperdash.common.config.AppProperties;
import com.goalkeeperdash.common.error.ApiException;
import com.goalkeeperdash.common.error.ErrorCode;
import com.goalkeeperdash.common.util.HashUtil;
import com.goalkeeperdash.user.domain.RefreshToken;
import com.goalkeeperdash.user.repo.RefreshTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Manages opaque refresh tokens: issued once, stored hashed, and rotated on use
 * (the presented token is revoked and a fresh one returned).
 */
@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repo;
    private final int ttlDays;

    public RefreshTokenService(RefreshTokenRepository repo, AppProperties props) {
        this.repo = repo;
        this.ttlDays = props.jwt().refreshTtlDays();
    }

    /** @return the raw token to hand to the client (only stored hashed server-side). */
    @Transactional
    public String issue(UUID userId) {
        String raw = HashUtil.randomToken();
        RefreshToken token = new RefreshToken();
        token.setUserId(userId);
        token.setTokenHash(HashUtil.sha256Hex(raw));
        token.setExpiresAt(Instant.now().plus(ttlDays, ChronoUnit.DAYS));
        repo.save(token);
        return raw;
    }

    /** Validates and rotates: revokes the presented token, issues a new one. */
    @Transactional
    public Rotation rotate(String rawToken) {
        RefreshToken existing = repo.findByTokenHashAndRevokedFalse(HashUtil.sha256Hex(rawToken))
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_TOKEN, "Invalid refresh token"));
        if (existing.getExpiresAt().isBefore(Instant.now())) {
            existing.setRevoked(true);
            throw new ApiException(ErrorCode.INVALID_TOKEN, "Refresh token expired");
        }
        existing.setRevoked(true);
        String newRaw = issue(existing.getUserId());
        return new Rotation(existing.getUserId(), newRaw);
    }

    @Transactional
    public void revoke(String rawToken) {
        repo.findByTokenHashAndRevokedFalse(HashUtil.sha256Hex(rawToken))
                .ifPresent(t -> t.setRevoked(true));
    }

    public record Rotation(UUID userId, String newRefreshToken) {}
}
