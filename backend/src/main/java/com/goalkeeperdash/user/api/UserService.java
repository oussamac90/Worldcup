package com.goalkeeperdash.user.api;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * User reads/operations exposed to other modules through a service interface
 * (modules MUST NOT reach into each other's internals).
 */
public interface UserService {

    Optional<UserSummary> findById(UUID userId);

    /** Display names for neighbor rendering on leaderboards, keyed by user id. */
    Map<UUID, String> displayNames(Collection<UUID> userIds);

    /**
     * Locks the user's nation for the given season if not already locked (§4.4).
     * Called by the game module when a user records their first validated score.
     * Idempotent.
     */
    void lockNationForSeasonIfUnlocked(UUID userId, UUID seasonId);
}
