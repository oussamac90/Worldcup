package com.goalkeeperdash.game.validation;

import com.goalkeeperdash.common.domain.GameMode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Unit tests for the v1 sanity validator (§6.4). Pure — no Spring context. */
class SanityValidatorTest {

    private final SanityValidator validator = new SanityValidator();

    private ScoreContext.EventSummary summary(int shots, int saves) {
        return new ScoreContext.EventSummary(shots, saves, shots - saves, 5, 2, 1);
    }

    @Test
    void acceptsAPlausibleRun() {
        var ctx = new ScoreContext(GameMode.TOURNAMENT, 880, 142_300, summary(120, 88));
        ValidationResult result = validator.validate(ctx);
        assertThat(result.validated()).isTrue();
        assertThat(result.flagged()).isFalse();
    }

    @Test
    void flagsScoreAboveModeCeiling() {
        var ctx = new ScoreContext(GameMode.TOURNAMENT, 99_999, 142_300, summary(120, 88));
        ValidationResult result = validator.validate(ctx);
        assertThat(result.flagged()).isTrue();
        assertThat(result.validated()).isFalse();
        assertThat(result.reasons()).anyMatch(r -> r.contains("ceiling"));
    }

    @Test
    void flagsSavesExceedingShotsFaced() {
        var ctx = new ScoreContext(GameMode.SURVIVAL, 500, 60_000, summary(40, 90));
        ValidationResult result = validator.validate(ctx);
        assertThat(result.flagged()).isTrue();
        assertThat(result.reasons()).anyMatch(r -> r.contains("saves"));
    }

    @Test
    void flagsSuperhumanSaveCadence() {
        // 88 saves in 1 second is far above the human ceiling.
        var ctx = new ScoreContext(GameMode.TOURNAMENT, 880, 1_000, summary(120, 88));
        ValidationResult result = validator.validate(ctx);
        assertThat(result.flagged()).isTrue();
        assertThat(result.reasons()).anyMatch(r -> r.contains("saves/sec"));
    }
}
