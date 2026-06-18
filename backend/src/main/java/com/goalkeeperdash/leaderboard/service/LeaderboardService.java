package com.goalkeeperdash.leaderboard.service;

import com.goalkeeperdash.common.domain.SeasonService;
import com.goalkeeperdash.common.domain.SeasonView;
import com.goalkeeperdash.common.error.ApiException;
import com.goalkeeperdash.common.error.ErrorCode;
import com.goalkeeperdash.leaderboard.redis.LeaderboardRedis;
import com.goalkeeperdash.leaderboard.web.dto.LeaderboardDtos.DualLeaderboard;
import com.goalkeeperdash.leaderboard.web.dto.LeaderboardDtos.NationBoard;
import com.goalkeeperdash.leaderboard.web.dto.LeaderboardDtos.NationDto;
import com.goalkeeperdash.leaderboard.web.dto.LeaderboardDtos.NationInWorld;
import com.goalkeeperdash.leaderboard.web.dto.LeaderboardDtos.NationNeighbor;
import com.goalkeeperdash.leaderboard.web.dto.LeaderboardDtos.PersonalWithinNation;
import com.goalkeeperdash.leaderboard.web.dto.LeaderboardDtos.SeasonDto;
import com.goalkeeperdash.leaderboard.web.dto.LeaderboardDtos.UserBoard;
import com.goalkeeperdash.leaderboard.web.dto.LeaderboardDtos.UserNeighbor;
import com.goalkeeperdash.user.api.NationService;
import com.goalkeeperdash.user.api.NationView;
import com.goalkeeperdash.user.api.UserService;
import com.goalkeeperdash.user.api.UserSummary;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Read path for leaderboards (§5.3/§5.4). Ranks come from Redis ZSETs; if a
 * requested season isn't warm in Redis it is lazily rebuilt from Postgres, so
 * Redis can be ephemeral and reads are self-healing.
 */
@Service
public class LeaderboardService {

    private static final int NEIGHBOR_RADIUS = 2;

    private final LeaderboardRedis redis;
    private final RebuildService rebuildService;
    private final SeasonService seasonService;
    private final NationService nationService;
    private final UserService userService;

    public LeaderboardService(LeaderboardRedis redis, RebuildService rebuildService, SeasonService seasonService,
                              NationService nationService, UserService userService) {
        this.redis = redis;
        this.rebuildService = rebuildService;
        this.seasonService = seasonService;
        this.nationService = nationService;
        this.userService = userService;
    }

    public DualLeaderboard dualForUser(UUID userId) {
        SeasonView season = activeSeason();
        ensureWarm(season.id());

        UserSummary user = userService.findById(userId)
                .orElseThrow(() -> ApiException.notFound("User not found"));
        if (user.nationId() == null || user.nationCode() == null) {
            throw new ApiException(ErrorCode.NATION_NOT_CHOSEN, "Pick a nation before viewing leaderboards");
        }
        String code = user.nationCode();
        NationView nation = nationService.findByCode(code).orElseThrow(() -> ApiException.notFound("Nation"));

        PersonalWithinNation personal = personalWithinNation(season.id(), code, userId);
        NationInWorld world = nationInWorld(season.id(), code);

        return new DualLeaderboard(
                seasonDto(season),
                new NationDto(nation.code(), nation.name(), nation.flagColors()),
                personal,
                world);
    }

    public NationBoard nationsBoard(UUID seasonId, int limit, int offset) {
        SeasonView season = resolveSeason(seasonId);
        ensureWarm(season.id());
        String key = redis.nationsKey(season.id());
        long total = redis.size(key);
        List<NationNeighbor> entries = new ArrayList<>();
        for (LeaderboardRedis.Entry e : redis.range(key, offset, offset + limit - 1L)) {
            entries.add(new NationNeighbor(e.rank() + 1, e.member(), (long) e.score()));
        }
        return new NationBoard(seasonDto(season), total, entries);
    }

    public UserBoard usersBoard(UUID seasonId, int limit, int offset) {
        SeasonView season = resolveSeason(seasonId);
        ensureWarm(season.id());
        String key = redis.usersKey(season.id());
        return userBoardFromKey(season, key, limit, offset);
    }

    public UserBoard nationUsersBoard(String nationCode, UUID seasonId, int limit, int offset) {
        SeasonView season = resolveSeason(seasonId);
        ensureWarm(season.id());
        String key = redis.nationUsersKey(season.id(), nationCode);
        return userBoardFromKey(season, key, limit, offset);
    }

    // --- helpers ---

    private UserBoard userBoardFromKey(SeasonView season, String key, int limit, int offset) {
        long total = redis.size(key);
        List<LeaderboardRedis.Entry> raw = redis.range(key, offset, offset + limit - 1L);
        Map<UUID, String> names = userService.displayNames(raw.stream().map(e -> UUID.fromString(e.member())).toList());
        List<UserNeighbor> entries = new ArrayList<>();
        for (LeaderboardRedis.Entry e : raw) {
            UUID uid = UUID.fromString(e.member());
            entries.add(new UserNeighbor(e.rank() + 1, names.getOrDefault(uid, "Keeper"), (long) e.score()));
        }
        return new UserBoard(seasonDto(season), total, entries);
    }

    private PersonalWithinNation personalWithinNation(UUID seasonId, String code, UUID userId) {
        String key = redis.nationUsersKey(seasonId, code);
        long total = redis.size(key);
        Long rank0 = redis.revRank(key, userId.toString());
        Double bestScore = redis.score(key, userId.toString());
        int best = bestScore == null ? 0 : bestScore.intValue();
        long rank = rank0 == null ? 0 : rank0 + 1;

        List<UserNeighbor> neighbors = List.of();
        if (rank0 != null) {
            long from = Math.max(0, rank0 - NEIGHBOR_RADIUS);
            long to = rank0 + NEIGHBOR_RADIUS;
            List<LeaderboardRedis.Entry> window = redis.range(key, from, to);
            Map<UUID, String> names = userService.displayNames(
                    window.stream().map(e -> UUID.fromString(e.member())).toList());
            List<UserNeighbor> list = new ArrayList<>();
            for (LeaderboardRedis.Entry e : window) {
                UUID uid = UUID.fromString(e.member());
                list.add(new UserNeighbor(e.rank() + 1, names.getOrDefault(uid, "Keeper"), (long) e.score()));
            }
            neighbors = list;
        }
        return new PersonalWithinNation(rank, total, best, neighbors);
    }

    private NationInWorld nationInWorld(UUID seasonId, String code) {
        String key = redis.nationsKey(seasonId);
        long totalNations = redis.size(key);
        Long rank0 = redis.revRank(key, code);
        Double scoreD = redis.score(key, code);
        long nationTotal = scoreD == null ? 0 : (long) (double) scoreD;
        long rank = rank0 == null ? 0 : rank0 + 1;

        List<NationNeighbor> neighbors = List.of();
        if (rank0 != null) {
            long from = Math.max(0, rank0 - NEIGHBOR_RADIUS);
            long to = rank0 + NEIGHBOR_RADIUS;
            List<NationNeighbor> list = new ArrayList<>();
            for (LeaderboardRedis.Entry e : redis.range(key, from, to)) {
                list.add(new NationNeighbor(e.rank() + 1, e.member(), (long) e.score()));
            }
            neighbors = list;
        }
        return new NationInWorld(rank, totalNations, nationTotal, neighbors);
    }

    private void ensureWarm(UUID seasonId) {
        if (!redis.seasonHasData(seasonId)) {
            rebuildService.rebuildSeason(seasonId);
        }
    }

    private SeasonView activeSeason() {
        return seasonService.findActiveSeason()
                .orElseThrow(() -> new ApiException(ErrorCode.NO_ACTIVE_SEASON, "No active season"));
    }

    private SeasonView resolveSeason(UUID seasonId) {
        if (seasonId == null) {
            return activeSeason();
        }
        return seasonService.findById(seasonId).orElseThrow(() -> ApiException.notFound("Season not found"));
    }

    private SeasonDto seasonDto(SeasonView s) {
        return new SeasonDto(s.id(), s.name(), s.endsAt());
    }
}
