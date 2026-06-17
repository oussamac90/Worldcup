package com.goalkeeperdash.leaderboard.redis;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Redis sorted-set index for leaderboards (§5.1). This is a cache/index, NOT the
 * source of truth — it MUST be rebuildable from Postgres aggregates (§5.5).
 *
 * <ul>
 *   <li>Personal global:   {@code lb:s{seasonId}:users}            member=userId score=bestScore</li>
 *   <li>Personal by nation:{@code lb:s{seasonId}:nation:{code}:users} member=userId score=bestScore</li>
 *   <li>National board:    {@code lb:s{seasonId}:nations}          member=code   score=totalScore</li>
 * </ul>
 */
@Component
public class LeaderboardRedis {

    private final StringRedisTemplate redis;

    public LeaderboardRedis(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public String usersKey(UUID seasonId) {
        return "lb:s" + seasonId + ":users";
    }

    public String nationUsersKey(UUID seasonId, String nationCode) {
        return "lb:s" + seasonId + ":nation:" + nationCode + ":users";
    }

    public String nationsKey(UUID seasonId) {
        return "lb:s" + seasonId + ":nations";
    }

    /** Upserts a user's bestScore into the personal global + personal-by-nation ZSETs. */
    public void putUserScore(UUID seasonId, String nationCode, UUID userId, double bestScore) {
        ZSetOperations<String, String> z = redis.opsForZSet();
        z.add(usersKey(seasonId), userId.toString(), bestScore);
        z.add(nationUsersKey(seasonId, nationCode), userId.toString(), bestScore);
    }

    /** Upserts a nation's total into the national ZSET. */
    public void putNationTotal(UUID seasonId, String nationCode, double totalScore) {
        redis.opsForZSet().add(nationsKey(seasonId), nationCode, totalScore);
    }

    /** 0-based rank from the top (highest score = rank 0), or null if absent. */
    public Long revRank(String key, String member) {
        return redis.opsForZSet().reverseRank(key, member);
    }

    public Double score(String key, String member) {
        return redis.opsForZSet().score(key, member);
    }

    public long size(String key) {
        Long n = redis.opsForZSet().zCard(key);
        return n == null ? 0 : n;
    }

    /** Top-N (or a window) as ordered (member, score) entries, highest first. */
    public List<Entry> range(String key, long start, long end) {
        Set<ZSetOperations.TypedTuple<String>> tuples =
                redis.opsForZSet().reverseRangeWithScores(key, start, end);
        List<Entry> result = new ArrayList<>();
        if (tuples != null) {
            long rank = start;
            for (ZSetOperations.TypedTuple<String> t : tuples) {
                result.add(new Entry(t.getValue(), t.getScore() == null ? 0 : t.getScore(), rank++));
            }
        }
        return result;
    }

    /** Deletes all leaderboard keys (used by rebuild). */
    public void deleteSeason(UUID seasonId) {
        Set<String> keys = redis.keys("lb:s" + seasonId + ":*");
        if (keys != null && !keys.isEmpty()) {
            redis.delete(keys);
        }
    }

    public boolean seasonHasData(UUID seasonId) {
        return size(nationsKey(seasonId)) > 0 || size(usersKey(seasonId)) > 0;
    }

    /** A ranked entry: member, score and 0-based rank within the queried window. */
    public record Entry(String member, double score, long rank) {}
}
