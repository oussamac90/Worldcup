package com.goalkeeperdash.backoffice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Back-office admin account — separate from player auth (§7.1). Password hashed
 * with BCrypt; authentication is via a secure cookie session, never the player JWT.
 */
@Entity
@Table(name = "admin_account")
@Getter
@Setter
@NoArgsConstructor
public class AdminAccount {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "role", nullable = false, length = 20)
    private String role = "ADMIN";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (id == null) id = UUID.randomUUID();
        createdAt = Instant.now();
    }
}
