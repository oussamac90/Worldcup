package com.goalkeeperdash.common.ratelimit;

import com.goalkeeperdash.common.config.AppProperties;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory token-bucket rate limiter (Bucket4j). Adequate for v1 single
 * instance; swap for a Redis-backed bucket when scaling horizontally (§8.4).
 */
@Service
public class RateLimiterService {

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final int submitPerMinute;
    private final int authPerMinute;

    public RateLimiterService(AppProperties props) {
        this.submitPerMinute = props.ratelimit().submitPerMinute();
        this.authPerMinute = props.ratelimit().authPerMinute();
    }

    /** @return true if the action is allowed, false if the caller is over budget. */
    public boolean tryConsumeSubmit(String userId) {
        return bucket("submit:" + userId, submitPerMinute).tryConsume(1);
    }

    public boolean tryConsumeAuth(String ip) {
        return bucket("auth:" + ip, authPerMinute).tryConsume(1);
    }

    private Bucket bucket(String key, int perMinute) {
        return buckets.computeIfAbsent(key, k -> Bucket.builder()
                .addLimit(Bandwidth.classic(perMinute, Refill.greedy(perMinute, Duration.ofMinutes(1))))
                .build());
    }
}
