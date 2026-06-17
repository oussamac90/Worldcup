package com.goalkeeperdash.game.api;

import java.time.Instant;

/** Submission statistics exposed to the back-office analytics pages (§7.2). */
public interface GameStatsService {

    long totalSubmissions();

    long submissionsSince(Instant since);

    long flaggedCount();

    /** Distinct users with at least one submission since {@code since} (DAU when since=start of day). */
    long activeUsersSince(Instant since);
}
