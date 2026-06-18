package com.goalkeeperdash.leaderboard.api;

import java.util.List;
import java.util.UUID;

/** Per-nation season aggregates exposed to the back-office analytics pages (§7.2). */
public interface SeasonStatsService {

    List<NationContribution> contributorCounts(UUID seasonId);

    record NationContribution(String nationCode, String nationName, int contributorCount, long totalScore) {}
}
