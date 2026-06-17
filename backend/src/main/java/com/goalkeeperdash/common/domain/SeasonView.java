package com.goalkeeperdash.common.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Immutable season projection shared across modules via {@link SeasonService}.
 * Lets the {@code user} and {@code game} modules read season context without
 * depending on the {@code leaderboard} module's internal Season entity.
 */
public record SeasonView(UUID id, String name, Instant startsAt, Instant endsAt, SeasonStatus status) {}
