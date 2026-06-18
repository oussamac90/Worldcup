package com.goalkeeperdash.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Shared infrastructure beans: configuration properties binding, the Redis
 * string template used for leaderboard sorted sets, and a shared ObjectMapper.
 */
@Configuration
@EnableConfigurationProperties(AppProperties.class)
public class CommonConfig {

    /** Leaderboards use plain string members/scores in ZSETs. */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
        return new StringRedisTemplate(factory);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }
}
