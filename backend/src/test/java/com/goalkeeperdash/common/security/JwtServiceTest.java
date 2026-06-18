package com.goalkeeperdash.common.security;

import com.goalkeeperdash.common.config.AppProperties;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Unit tests for app access-token issuing/parsing. Pure — no Spring context. */
class JwtServiceTest {

    private AppProperties props(String secret) {
        return new AppProperties(
                new AppProperties.Jwt(secret, 30, 30, "goalkeeper-dash"),
                new AppProperties.Oidc("g", "f", "a", "t", true),
                new AppProperties.Seed(true, true),
                new AppProperties.Admin("admin", "pw"),
                new AppProperties.Cors("http://localhost"),
                new AppProperties.RateLimit(30, 20),
                new AppProperties.Game(30));
    }

    @Test
    void issuesAndParsesRoundTrip() {
        JwtService service = new JwtService(props("this_is_a_sufficiently_long_secret_value_123456"));
        UUID userId = UUID.randomUUID();
        UUID seasonId = UUID.randomUUID();

        String token = service.issueAccessToken(userId, "MAR", seasonId, List.of("USER"));
        Claims claims = service.parse(token);

        assertThat(claims.getSubject()).isEqualTo(userId.toString());
        assertThat(claims.get("nation")).isEqualTo("MAR");
        assertThat(claims.get("season")).isEqualTo(seasonId.toString());
        assertThat(JwtService.rolesOf(claims)).containsExactly("USER");
    }

    @Test
    void rejectsShortSecret() {
        assertThatThrownBy(() -> new JwtService(props("tooshort")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsTamperedToken() {
        JwtService service = new JwtService(props("this_is_a_sufficiently_long_secret_value_123456"));
        String token = service.issueAccessToken(UUID.randomUUID(), null, null, List.of("USER"));
        assertThatThrownBy(() -> service.parse(token + "tampered"))
                .isInstanceOf(io.jsonwebtoken.JwtException.class);
    }
}
