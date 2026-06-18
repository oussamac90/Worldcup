package com.goalkeeperdash.game.validation;

/**
 * Validation seam (§6.5). v1 ships a {@link SanityValidator}; a future
 * {@code ReplayValidator} can be dropped in via configuration without touching
 * the controller or schema. Validation logic MUST live behind this interface,
 * never inlined in the controller.
 */
public interface ScoreValidator {

    ValidationResult validate(ScoreContext context);
}
