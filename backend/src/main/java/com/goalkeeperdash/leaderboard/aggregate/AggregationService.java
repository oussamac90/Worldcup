package com.goalkeeperdash.leaderboard.aggregate;

import com.goalkeeperdash.leaderboard.api.ScoreIngestionService;
import com.goalkeeperdash.leaderboard.domain.NationSeasonStat;
import com.goalkeeperdash.leaderboard.domain.UserSeasonStat;
import com.goalkeeperdash.leaderboard.redis.LeaderboardRedis;
import com.goalkeeperdash.leaderboard.repo.NationSeasonStatRepository;
import com.goalkeeperdash.leaderboard.repo.UserSeasonStatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.UUID;

/**
 * Implements the write path (§5.2): durable Postgres aggregate upserts inside the
 * transaction, then a post-commit Redis update. National total = sum of per-user
 * season bestScore (§3.2), so the nation delta on each submit is the bestScore
 * improvement. If the Redis write fails it is logged; the rebuild job (§5.5) is
 * the backstop, so the failure is non-fatal.
 */
@Service
public class AggregationService implements ScoreIngestionService {

    private static final Logger log = LoggerFactory.getLogger(AggregationService.class);

    private final UserSeasonStatRepository userStats;
    private final NationSeasonStatRepository nationStats;
    private final LeaderboardRedis redis;

    public AggregationService(UserSeasonStatRepository userStats,
                              NationSeasonStatRepository nationStats,
                              LeaderboardRedis redis) {
        this.userStats = userStats;
        this.nationStats = nationStats;
        this.redis = redis;
    }

    @Override
    @Transactional
    public void applyValidatedScore(UUID userId, UUID nationId, String nationCode, UUID seasonId, int score) {
        UserSeasonStat stat = userStats.findByUserIdAndSeasonId(userId, seasonId).orElse(null);
        boolean firstContribution = (stat == null);
        int oldBest = firstContribution ? 0 : stat.getBestScore();

        if (firstContribution) {
            stat = new UserSeasonStat();
            stat.setUserId(userId);
            stat.setSeasonId(seasonId);
            stat.setNationId(nationId);
            stat.setBestScore(score);
            stat.setTotalScore(score);
            stat.setPlayCount(1);
        } else {
            stat.setTotalScore(stat.getTotalScore() + score);
            stat.setPlayCount(stat.getPlayCount() + 1);
            if (score > stat.getBestScore()) {
                stat.setBestScore(score);
            }
        }
        userStats.save(stat);

        int newBest = stat.getBestScore();
        long bestDelta = (long) newBest - oldBest; // contribution to national total

        NationSeasonStat nation = nationStats.findByNationIdAndSeasonId(nationId, seasonId).orElse(null);
        if (nation == null) {
            nation = new NationSeasonStat();
            nation.setNationId(nationId);
            nation.setSeasonId(seasonId);
            nation.setTotalScore(0);
            nation.setContributorCount(0);
        }
        nation.setTotalScore(nation.getTotalScore() + bestDelta);
        if (firstContribution) {
            nation.setContributorCount(nation.getContributorCount() + 1);
        }
        nationStats.save(nation);

        scheduleRedisUpdate(seasonId, nationCode, userId, newBest, nation.getTotalScore());
    }

    @Override
    @Transactional
    public void recomputeUserFromScores(UUID userId, UUID nationId, String nationCode, UUID seasonId,
                                        List<Integer> validScores) {
        UserSeasonStat stat = userStats.findByUserIdAndSeasonId(userId, seasonId).orElse(null);
        int oldBest = stat == null ? 0 : stat.getBestScore();
        boolean existed = stat != null;

        int newBest = validScores.stream().mapToInt(Integer::intValue).max().orElse(0);
        long total = validScores.stream().mapToLong(Integer::longValue).sum();
        int playCount = validScores.size();

        if (stat == null) {
            stat = new UserSeasonStat();
            stat.setUserId(userId);
            stat.setSeasonId(seasonId);
            stat.setNationId(nationId);
        }
        stat.setBestScore(newBest);
        stat.setTotalScore(total);
        stat.setPlayCount(playCount);
        userStats.save(stat);

        NationSeasonStat nation = nationStats.findByNationIdAndSeasonId(nationId, seasonId)
                .orElseGet(() -> {
                    NationSeasonStat n = new NationSeasonStat();
                    n.setNationId(nationId);
                    n.setSeasonId(seasonId);
                    return n;
                });
        nation.setTotalScore(nation.getTotalScore() + (newBest - oldBest));
        if (!existed && playCount > 0) {
            nation.setContributorCount(nation.getContributorCount() + 1);
        } else if (existed && playCount == 0) {
            nation.setContributorCount(Math.max(0, nation.getContributorCount() - 1));
        }
        nationStats.save(nation);

        scheduleRedisUpdate(seasonId, nationCode, userId, newBest, nation.getTotalScore());
    }

    /** Defer the Redis write until after the DB commit succeeds (§5.2). */
    private void scheduleRedisUpdate(UUID seasonId, String nationCode, UUID userId, int bestScore, long nationTotal) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            doRedisUpdate(seasonId, nationCode, userId, bestScore, nationTotal);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                doRedisUpdate(seasonId, nationCode, userId, bestScore, nationTotal);
            }
        });
    }

    private void doRedisUpdate(UUID seasonId, String nationCode, UUID userId, int bestScore, long nationTotal) {
        try {
            redis.putUserScore(seasonId, nationCode, userId, bestScore);
            redis.putNationTotal(seasonId, nationCode, nationTotal);
        } catch (Exception e) {
            log.warn("Redis leaderboard update failed (rebuild job is the backstop) user={} season={}: {}",
                    userId, seasonId, e.getMessage());
        }
    }
}
