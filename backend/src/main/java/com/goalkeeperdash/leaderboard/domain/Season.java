package com.goalkeeperdash.leaderboard.domain;

import com.goalkeeperdash.common.domain.SeasonStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * A competitive season. Invariant: at most one ACTIVE season at a time, enforced
 * in the service layer (§3.1). Owned by the leaderboard module.
 */
@Entity
@Table(name = "season")
@Getter
@Setter
@NoArgsConstructor
public class Season {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    @Column(name = "ends_at", nullable = false)
    private Instant endsAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SeasonStatus status = SeasonStatus.SCHEDULED;

    /** Set on close. */
    @Column(name = "winning_nation_id", columnDefinition = "uuid")
    private UUID winningNationId;

    @PrePersist
    void onCreate() {
        if (id == null) id = UUID.randomUUID();
    }
}
