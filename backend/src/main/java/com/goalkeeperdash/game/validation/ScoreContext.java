package com.goalkeeperdash.game.validation;

import com.goalkeeperdash.common.domain.GameMode;

/**
 * Server-computed inputs to score validation. The {@code summary} is the parsed
 * eventSummary; in v1 it feeds sanity checks and is the forward hook for a future
 * {@code ReplayValidator} (§6.5).
 */
public record ScoreContext(GameMode mode, int score, long durationMs, EventSummary summary) {

    /** Parsed view of the eventSummary JSON (defaults to zeros if absent). */
    public record EventSummary(
            int shotsFaced,
            int saves,
            int goalsConceded,
            int maxCombo,
            int perfectSaves,
            int schemaVersion) {}
}
