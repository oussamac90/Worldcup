package com.goalkeeperdash.leaderboard.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Aggregate per user per season — fast personal totals and the durable source for
 * the personal Redis ZSETs. Personal rank is by {@code bestScore} (§3.2).
 */
@Entity
@Table(name = "user_season_stat",
        uniqueConstraints = @UniqueConstraint(name = "uq_user_season", columnNames = {"user_id", "season_id"}),
        indexes = {
                @Index(name = "idx_uss_season_nation", columnList = "season_id,nation_id"),
                @Index(name = "idx_uss_season_best", columnList = "season_id,best_score")
        })
@Getter
@Setter
@NoArgsConstructor
public class UserSeasonStat {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "user_id", columnDefinition = "uuid", nullable = false)
    private UUID userId;

    @Column(name = "season_id", columnDefinition = "uuid", nullable = false)
    private UUID seasonId;

    @Column(name = "nation_id", columnDefinition = "uuid", nullable = false)
    private UUID nationId;

    @Column(name = "best_score", nullable = false)
    private int bestScore;

    @Column(name = "total_score", nullable = false)
    private long totalScore;

    @Column(name = "play_count", nullable = false)
    private int playCount;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        if (id == null) id = UUID.randomUUID();
        updatedAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
