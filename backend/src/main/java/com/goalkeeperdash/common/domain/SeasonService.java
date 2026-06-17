package com.goalkeeperdash.common.domain;

import java.util.Optional;
import java.util.UUID;

/**
 * Cross-module read/contract for season context. The interface lives in
 * {@code common} (depended on by all modules); the implementation lives in the
 * {@code leaderboard} module, which owns the Season entity and aggregates.
 *
 * <p>This is what lets {@code user} (which depends only on {@code common}) embed
 * the active season id in the access JWT and enforce the nation-lock rule.
 */
public interface SeasonService {

    /** @return the single ACTIVE season, or empty if none is active. */
    Optional<SeasonView> findActiveSeason();

    Optional<SeasonView> findById(UUID seasonId);

    /** @return the active season id, or null if none active (convenience for JWT claims). */
    default UUID activeSeasonIdOrNull() {
        return findActiveSeason().map(SeasonView::id).orElse(null);
    }
}
