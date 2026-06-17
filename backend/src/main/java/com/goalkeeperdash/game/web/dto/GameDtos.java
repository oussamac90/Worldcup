package com.goalkeeperdash.game.web.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.goalkeeperdash.common.domain.GameMode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Request/response payloads for the game session + submit contract (§6.1/§6.2). */
public final class GameDtos {

    private GameDtos() {}

    public record OpenSessionRequest(@NotNull GameMode mode) {}

    public record OpenSessionResponse(UUID sessionId, String nonce, Instant serverTime, long seed) {}

    public record SubmitRequest(
            @NotNull String nonce,
            @PositiveOrZero int score,
            GameMode mode,
            @PositiveOrZero long durationMs,
            JsonNode eventSummary) {}

    public record SubmitResponse(
            UUID submissionId,
            boolean accepted,
            boolean flagged,
            int score,
            List<String> reasons) {}
}
