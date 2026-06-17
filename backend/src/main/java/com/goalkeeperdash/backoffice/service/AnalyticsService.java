package com.goalkeeperdash.backoffice.service;

import com.goalkeeperdash.common.domain.SeasonService;
import com.goalkeeperdash.common.domain.SeasonView;
import com.goalkeeperdash.game.api.GameStatsService;
import com.goalkeeperdash.leaderboard.api.SeasonStatsService;
import com.goalkeeperdash.user.api.UserAdminService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Basic analytics (§7.2), composed from each module's service interface — DAU,
 * submissions/day, new users/day, per-nation contributor counts, flagged-rate.
 */
@Service
public class AnalyticsService {

    private final UserAdminService userAdmin;
    private final GameStatsService gameStats;
    private final SeasonStatsService seasonStats;
    private final SeasonService seasonService;

    public AnalyticsService(UserAdminService userAdmin, GameStatsService gameStats,
                            SeasonStatsService seasonStats, SeasonService seasonService) {
        this.userAdmin = userAdmin;
        this.gameStats = gameStats;
        this.seasonStats = seasonStats;
        this.seasonService = seasonService;
    }

    public Snapshot snapshot() {
        Instant dayAgo = Instant.now().minus(1, ChronoUnit.DAYS);
        long total = gameStats.totalSubmissions();
        long flagged = gameStats.flaggedCount();
        double flaggedRate = total == 0 ? 0 : (double) flagged / total;

        List<SeasonStatsService.NationContribution> contributions = seasonService.findActiveSeason()
                .map(s -> seasonStats.contributorCounts(s.id()))
                .orElse(List.of());

        return new Snapshot(
                userAdmin.countRealUsers(),
                userAdmin.countUsersCreatedSince(dayAgo),
                gameStats.activeUsersSince(dayAgo),
                gameStats.submissionsSince(dayAgo),
                total,
                flagged,
                flaggedRate,
                seasonService.findActiveSeason().orElse(null),
                contributions);
    }

    public record Snapshot(
            long totalUsers,
            long newUsersLast24h,
            long dau,
            long submissionsLast24h,
            long totalSubmissions,
            long flaggedSubmissions,
            double flaggedRate,
            SeasonView activeSeason,
            List<SeasonStatsService.NationContribution> nationContributions) {}
}
