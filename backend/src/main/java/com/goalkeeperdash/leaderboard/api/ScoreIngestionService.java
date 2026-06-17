package com.goalkeeperdash.leaderboard.api;

import java.util.List;
import java.util.UUID;

/**
 * Score ingestion contract used by the {@code game} module (which owns the
 * immutable ScoreSubmission log) to fold a validated score into the season
 * aggregates and Redis index. Runs inside the caller's transaction; the Redis
 * write is deferred until after commit (§5.2/§6.3).
 */
public interface ScoreIngestionService {

    /**
     * Applies one validated score: upserts {@code UserSeasonStat}, adjusts the
     * {@code NationSeasonStat} by the bestScore delta, and (after commit) updates
     * the personal + national Redis ZSETs.
     */
    void applyValidatedScore(UUID userId, UUID nationId, String nationCode, UUID seasonId, int score);

    /**
     * Recomputes a single user's season aggregate from the supplied set of valid
     * scores (used by moderation when a submission is invalidated/restored).
     */
    void recomputeUserFromScores(UUID userId, UUID nationId, String nationCode, UUID seasonId, List<Integer> validScores);
}
