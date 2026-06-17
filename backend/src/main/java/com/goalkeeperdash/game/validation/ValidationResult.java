package com.goalkeeperdash.game.validation;

import java.util.List;

/**
 * Outcome of soft validation. {@code validated} = passed all sanity checks;
 * {@code flagged} = a soft check failed (kept for audit, excluded from boards).
 * Hard-invalid inputs (bad nonce/session) are rejected before reaching a
 * validator, so they never produce a result.
 */
public record ValidationResult(boolean validated, boolean flagged, List<String> reasons) {

    public static ValidationResult ok() {
        return new ValidationResult(true, false, List.of());
    }

    public static ValidationResult flagged(List<String> reasons) {
        return new ValidationResult(false, true, reasons);
    }
}
