package com.goalkeeperdash.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Strongly-typed application configuration, bound from the {@code app.*} keys in
 * {@code application.yml} (which in turn read environment variables).
 */
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Jwt jwt,
        Oidc oidc,
        Seed seed,
        Admin admin,
        Cors cors,
        RateLimit ratelimit,
        Game game) {

    public record Jwt(String secret, int accessTtlMin, int refreshTtlDays, String issuer) {}

    public record Oidc(
            String googleClientId,
            String facebookAppId,
            String appleClientId,
            String appleTeamId,
            boolean devLoginEnabled) {}

    public record Seed(boolean enabled, boolean simulatedNations) {}

    public record Admin(String bootstrapUser, String bootstrapPassword) {}

    public record Cors(String allowedOrigins) {}

    public record RateLimit(int submitPerMinute, int authPerMinute) {}

    public record Game(int sessionTtlMin) {}
}
