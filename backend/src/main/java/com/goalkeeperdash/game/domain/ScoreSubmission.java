package com.goalkeeperdash.game.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.goalkeeperdash.common.domain.GameMode;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
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
import org.hibernate.annotations.Type;

import java.time.Instant;
import java.util.UUID;

/**
 * The immutable score event log — the audit trail (§3.1). {@code nationId} is a
 * denormalized snapshot at submit time so historical attribution survives nation
 * switches. {@code eventSummary} (JSONB) is the forward hook for replay
 * validation (§6.5); {@code validated}/{@code flagged} separate "accepted" from
 * "counted toward boards".
 */
@Entity
@Table(name = "score_submission", indexes = {
        @Index(name = "idx_sub_user_season", columnList = "user_id,season_id"),
        @Index(name = "idx_sub_season_nation", columnList = "season_id,nation_id"),
        @Index(name = "idx_sub_flagged", columnList = "flagged")
})
@Getter
@Setter
@NoArgsConstructor
public class ScoreSubmission {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "user_id", columnDefinition = "uuid", nullable = false)
    private UUID userId;

    @Column(name = "nation_id", columnDefinition = "uuid", nullable = false)
    private UUID nationId;

    @Column(name = "season_id", columnDefinition = "uuid", nullable = false)
    private UUID seasonId;

    @Column(name = "session_id", columnDefinition = "uuid", nullable = false)
    private UUID sessionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false, length = 20)
    private GameMode mode;

    @Column(name = "score", nullable = false)
    private int score;

    /** Passed sanity checks. */
    @Column(name = "validated", nullable = false)
    private boolean validated;

    /** Failed a soft check — kept for audit but excluded from boards/aggregates. */
    @Column(name = "flagged", nullable = false)
    private boolean flagged;

    /** Set when a moderator manually invalidates the submission (excludes it). */
    @Column(name = "manually_invalidated", nullable = false)
    private boolean manuallyInvalidated;

    @Column(name = "client_reported_duration_ms", nullable = false)
    private long clientReportedDurationMs;

    @Type(JsonBinaryType.class)
    @Column(name = "event_summary", columnDefinition = "jsonb")
    private JsonNode eventSummary;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = Instant.now();
    }

    /** Whether this submission counts toward leaderboards. */
    public boolean countsTowardBoards() {
        return validated && !flagged && !manuallyInvalidated;
    }
}
