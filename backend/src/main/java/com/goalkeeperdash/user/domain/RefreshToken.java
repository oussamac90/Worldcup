package com.goalkeeperdash.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Opaque, long-lived refresh token stored <em>hashed</em> (SHA-256). Rotated on
 * use: the old row is revoked and a new one issued (§4.2).
 */
@Entity
@Table(name = "refresh_token", indexes = {
        @Index(name = "idx_refresh_token_hash", columnList = "token_hash"),
        @Index(name = "idx_refresh_user", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
public class RefreshToken {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "user_id", columnDefinition = "uuid", nullable = false)
    private UUID userId;

    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked", nullable = false)
    private boolean revoked = false;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (id == null) id = UUID.randomUUID();
        createdAt = Instant.now();
    }
}
