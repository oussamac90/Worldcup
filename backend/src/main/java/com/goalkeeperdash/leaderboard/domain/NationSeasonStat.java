package com.goalkeeperdash.leaderboard.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
 * Aggregate per nation per season — powers the global national board.
 *
 * <p>National total = sum of each contributing user's season {@code bestScore}
 * (§3.2): rewards breadth across the playerbase and resists grind-spam. The
 * alternative (sum of all submissions) is a one-line change in the aggregation
 * service if product wants it.
 */
@Entity
@Table(name = "nation_season_stat",
        uniqueConstraints = @UniqueConstraint(name = "uq_nation_season", columnNames = {"nation_id", "season_id"}))
@Getter
@Setter
@NoArgsConstructor
public class NationSeasonStat {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "nation_id", columnDefinition = "uuid", nullable = false)
    private UUID nationId;

    @Column(name = "season_id", columnDefinition = "uuid", nullable = false)
    private UUID seasonId;

    @Column(name = "total_score", nullable = false)
    private long totalScore;

    @Column(name = "contributor_count", nullable = false)
    private int contributorCount;

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
