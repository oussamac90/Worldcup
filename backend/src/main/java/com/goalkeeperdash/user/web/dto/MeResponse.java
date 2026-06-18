package com.goalkeeperdash.user.web.dto;

import java.time.Instant;
import java.util.UUID;

/** Response for GET /api/v1/me — profile, nation, lock status, current season. */
public record MeResponse(
        UUID id,
        String displayName,
        NationDto nation,
        String status,
        boolean nationLocked,
        Instant nationChosenAt,
        SeasonDto season) {

    public record NationDto(String code, String name, String flagColors) {}

    public record SeasonDto(UUID id, String name, Instant endsAt) {}
}
