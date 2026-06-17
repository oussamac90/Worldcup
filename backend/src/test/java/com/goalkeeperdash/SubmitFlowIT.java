package com.goalkeeperdash;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end integration test against real Postgres + Redis (Testcontainers):
 * dev login → pick nation → open session → submit → dual leaderboard, plus the
 * nation-lock and flagged-score rules. Requires Docker to run.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SubmitFlowIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7")).withExposedPorts(6379);

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
        r.add("spring.data.redis.host", redis::getHost);
        r.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        r.add("app.jwt.secret", () -> "integration_test_secret_value_at_least_32_bytes");
        r.add("app.oidc.dev-login-enabled", () -> true);
        r.add("app.seed.enabled", () -> true);
        r.add("app.seed.simulated-nations", () -> false); // deterministic boards
    }

    @Autowired
    TestRestTemplate rest;

    @Test
    void fullFlow() {
        // Dev login.
        JsonNode login = post("/api/v1/auth/dev", "{\"idToken\":\"it-user\"}", null).getBody();
        String token = login.get("accessToken").asText();

        // Pick nation.
        ResponseEntity<JsonNode> me = exchange("/api/v1/me/nation", HttpMethod.PUT,
                "{\"nationCode\":\"MAR\"}", token);
        assertThat(me.getStatusCode().is2xxSuccessful()).isTrue();

        // Open session.
        JsonNode session = post("/api/v1/sessions", "{\"mode\":\"TOURNAMENT\"}", token).getBody();
        String sessionId = session.get("sessionId").asText();
        String nonce = session.get("nonce").asText();

        // Submit a valid score.
        String submitBody = "{\"nonce\":\"" + nonce + "\",\"score\":880,\"mode\":\"TOURNAMENT\","
                + "\"durationMs\":142300,\"eventSummary\":{\"shotsFaced\":120,\"saves\":88,\"schemaVersion\":1}}";
        JsonNode submit = post("/api/v1/sessions/" + sessionId + "/submit", submitBody, token).getBody();
        assertThat(submit.get("accepted").asBoolean()).isTrue();
        assertThat(submit.get("flagged").asBoolean()).isFalse();

        // Dual leaderboard reflects the score.
        JsonNode board = exchange("/api/v1/leaderboards/me", HttpMethod.GET, null, token).getBody();
        assertThat(board.get("personalWithinNation").get("bestScore").asInt()).isEqualTo(880);
        assertThat(board.get("nation").get("code").asText()).isEqualTo("MAR");

        // Nation is now locked → switching returns 409.
        ResponseEntity<JsonNode> locked = exchange("/api/v1/me/nation", HttpMethod.PUT,
                "{\"nationCode\":\"FRA\"}", token);
        assertThat(locked.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    private ResponseEntity<JsonNode> post(String path, String body, String token) {
        return exchange(path, HttpMethod.POST, body, token);
    }

    private ResponseEntity<JsonNode> exchange(String path, HttpMethod method, String body, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) headers.setBearerAuth(token);
        return rest.exchange(path, method, new HttpEntity<>(body, headers), JsonNode.class);
    }
}
