package com.goalkeeperdash.leaderboard.service;

import com.goalkeeperdash.leaderboard.domain.NationSeasonStat;
import com.goalkeeperdash.leaderboard.domain.Season;
import com.goalkeeperdash.leaderboard.domain.UserSeasonStat;
import com.goalkeeperdash.leaderboard.redis.LeaderboardRedis;
import com.goalkeeperdash.leaderboard.repo.NationSeasonStatRepository;
import com.goalkeeperdash.leaderboard.repo.SeasonRepository;
import com.goalkeeperdash.leaderboard.repo.UserSeasonStatRepository;
import com.goalkeeperdash.user.api.NationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Rebuilds all Redis ZSETs from the durable Postgres aggregates (§5.5). This is
 * the safety net that lets Redis be ephemeral and makes §5.2 Redis-write failures
 * non-fatal. Idempotent: it clears and re-writes a season's keys.
 */
@Service
public class RebuildService {

    private static final Logger log = LoggerFactory.getLogger(RebuildService.class);

    private final SeasonRepository seasons;
    private final UserSeasonStatRepository userStats;
    private final NationSeasonStatRepository nationStats;
    private final NationService nationService;
    private final LeaderboardRedis redis;

    public RebuildService(SeasonRepository seasons, UserSeasonStatRepository userStats,
                          NationSeasonStatRepository nationStats, NationService nationService,
                          LeaderboardRedis redis) {
        this.seasons = seasons;
        this.userStats = userStats;
        this.nationStats = nationStats;
        this.nationService = nationService;
        this.redis = redis;
    }

    @Transactional(readOnly = true)
    public int rebuildSeason(UUID seasonId) {
        redis.deleteSeason(seasonId);
        Map<UUID, String> codeByNation = new HashMap<>();

        int users = 0;
        for (UserSeasonStat s : userStats.findBySeasonId(seasonId)) {
            String code = codeByNation.computeIfAbsent(s.getNationId(), this::codeOf);
            if (code == null) continue;
            redis.putUserScore(seasonId, code, s.getUserId(), s.getBestScore());
            users++;
        }
        for (NationSeasonStat n : nationStats.findBySeasonId(seasonId)) {
            String code = codeByNation.computeIfAbsent(n.getNationId(), this::codeOf);
            if (code == null) continue;
            redis.putNationTotal(seasonId, code, n.getTotalScore());
        }
        log.info("Rebuilt Redis leaderboards for season {} ({} users)", seasonId, users);
        return users;
    }

    @Transactional(readOnly = true)
    public int rebuildAll() {
        int total = 0;
        for (Season s : seasons.findAll()) {
            total += rebuildSeason(s.getId());
        }
        return total;
    }

    private String codeOf(UUID nationId) {
        return nationService.findById(nationId).map(n -> n.code()).orElse(null);
    }
}
