package com.goalkeeperdash.game.validation;

import com.goalkeeperdash.common.domain.GameMode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * v1 "trust plus sanity" validator (§6.4). Accepts client scores but flags the
 * impossible:
 * <ul>
 *   <li>score ceiling per mode,</li>
 *   <li>rate/cadence vs duration (saves-per-second below a human ceiling),</li>
 *   <li>internal consistency of the eventSummary (saves ≤ shotsFaced; score
 *       derivable from the summary within tolerance).</li>
 * </ul>
 * A soft failure → {@code flagged} (kept for audit, excluded from boards).
 */
@Component
@ConditionalOnProperty(name = "app.validation.mode", havingValue = "sanity", matchIfMissing = true)
public class SanityValidator implements ScoreValidator {

    /** Per-mode maximum plausible score. */
    private static final Map<GameMode, Integer> SCORE_CEILING = Map.of(
            GameMode.TOURNAMENT, 5000,
            GameMode.SURVIVAL, 8000,
            GameMode.SUDDEN_DEATH, 2000,
            GameMode.SHOOTOUT, 1000);

    /** Human ceiling on saves per second. */
    private static final double MAX_SAVES_PER_SECOND = 6.0;

    /** Points implied per save when sanity-deriving the score; generous tolerance. */
    private static final int POINTS_PER_SAVE = 10;
    private static final double SCORE_DERIVATION_TOLERANCE = 0.5; // ±50%

    @Override
    public ValidationResult validate(ScoreContext ctx) {
        List<String> reasons = new ArrayList<>();

        if (ctx.score() < 0) {
            reasons.add("negative score");
        }
        int ceiling = SCORE_CEILING.getOrDefault(ctx.mode(), 5000);
        if (ctx.score() > ceiling) {
            reasons.add("score " + ctx.score() + " exceeds ceiling " + ceiling + " for " + ctx.mode());
        }

        var summary = ctx.summary();
        if (summary != null) {
            if (summary.saves() > summary.shotsFaced()) {
                reasons.add("saves (" + summary.saves() + ") exceed shotsFaced (" + summary.shotsFaced() + ")");
            }
            if (ctx.durationMs() > 0) {
                double seconds = ctx.durationMs() / 1000.0;
                double savesPerSecond = summary.saves() / Math.max(seconds, 0.001);
                if (savesPerSecond > MAX_SAVES_PER_SECOND) {
                    reasons.add("saves/sec " + String.format("%.2f", savesPerSecond) + " above human ceiling");
                }
            } else if (ctx.score() > 0) {
                reasons.add("non-positive duration with positive score");
            }
            // Score should be roughly derivable from saves within tolerance.
            int implied = summary.saves() * POINTS_PER_SAVE;
            if (implied > 0) {
                double ratio = Math.abs(ctx.score() - implied) / (double) implied;
                if (ratio > SCORE_DERIVATION_TOLERANCE && ctx.score() > implied) {
                    reasons.add("score not derivable from summary within tolerance");
                }
            }
        }

        return reasons.isEmpty() ? ValidationResult.ok() : ValidationResult.flagged(reasons);
    }
}
