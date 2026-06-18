package com.goalkeeperdash.game.api;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.UUID;

/** Read projection of a ScoreSubmission for the back-office. */
public record SubmissionView(
        UUID id,
        UUID userId,
        UUID nationId,
        UUID seasonId,
        UUID sessionId,
        String mode,
        int score,
        boolean validated,
        boolean flagged,
        boolean manuallyInvalidated,
        boolean countsTowardBoards,
        long clientReportedDurationMs,
        JsonNode eventSummary,
        Instant createdAt) {}
