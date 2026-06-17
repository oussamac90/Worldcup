package com.goalkeeperdash.game.domain;

import com.goalkeeperdash.common.domain.GameMode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * Server-issued, single-use game session for anti-replay (§6.1). {@code submit}
 * consumes it; a second submit is rejected. Sessions expire after a TTL.
 */
@Entity
@Table(name = "game_session", indexes = @Index(name = "idx_session_user", columnList = "user_id"))
@Getter
@Setter
@NoArgsConstructor
public class GameSession {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "user_id", columnDefinition = "uuid", nullable = false)
    private UUID userId;

    @Column(name = "nonce", nullable = false)
    private String nonce;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false, length = 20)
    private GameMode mode;

    /** Seedable RNG seed handed to the client at open (forward hook for replay). */
    @Column(name = "seed", nullable = false)
    private long seed;

    @Column(name = "opened_at", nullable = false)
    private Instant openedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "consumed_at")
    private Instant consumedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private GameSessionStatus status = GameSessionStatus.OPEN;

    @PrePersist
    void onCreate() {
        if (id == null) id = UUID.randomUUID();
    }
}
