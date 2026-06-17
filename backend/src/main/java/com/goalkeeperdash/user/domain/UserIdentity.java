package com.goalkeeperdash.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * One row per linked IdP. Each (provider, subject) is unique; v1 treats each as
 * its own identity (no cross-provider linking — documented limitation, §4.3).
 */
@Entity
@Table(name = "user_identity", uniqueConstraints =
        @UniqueConstraint(name = "uq_identity_provider_subject", columnNames = {"provider", "subject"}))
@Getter
@Setter
@NoArgsConstructor
public class UserIdentity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "user_id", columnDefinition = "uuid", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 20)
    private AuthProvider provider;

    /** The IdP {@code sub} claim. */
    @Column(name = "subject", nullable = false)
    private String subject;

    /** Nullable — Apple may withhold email. */
    @Column(name = "email")
    private String email;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public UserIdentity(UUID userId, AuthProvider provider, String subject, String email) {
        this.userId = userId;
        this.provider = provider;
        this.subject = subject;
        this.email = email;
    }

    @PrePersist
    void onCreate() {
        if (id == null) id = UUID.randomUUID();
        createdAt = Instant.now();
    }
}
