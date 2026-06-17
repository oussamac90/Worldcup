package com.goalkeeperdash.user.api;

import com.goalkeeperdash.user.domain.UserStatus;

import java.util.UUID;

/**
 * Lightweight user projection shared with other modules (game identifies the
 * player; leaderboard needs nation + display name for boards).
 */
public record UserSummary(
        UUID id,
        String displayName,
        UUID nationId,
        String nationCode,
        UserStatus status,
        boolean synthetic) {

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }
}
