package com.goalkeeperdash.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * A player account. Cross-module references (nation, locked season) are stored as
 * plain UUID columns rather than JPA associations, keeping module entities
 * decoupled (interaction happens through service interfaces).
 */
@Entity
@Table(name = "app_user", indexes = {
        @Index(name = "idx_user_nation", columnList = "nation_id"),
        @Index(name = "idx_user_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "display_name", nullable = false, length = 40)
    private String displayName;

    /** FK → Nation (nullable until nation chosen). */
    @Column(name = "nation_id", columnDefinition = "uuid")
    private UUID nationId;

    @Column(name = "nation_chosen_at")
    private Instant nationChosenAt;

    /** FK → Season — once set, the user cannot switch nations for that season (§4.4). */
    @Column(name = "nation_locked_for_season_id", columnDefinition = "uuid")
    private UUID nationLockedForSeasonId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    /** True for synthetic seed users (§10) so they can be excluded/purged. */
    @Column(name = "synthetic", nullable = false)
    private boolean synthetic = false;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        if (id == null) id = UUID.randomUUID();
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
