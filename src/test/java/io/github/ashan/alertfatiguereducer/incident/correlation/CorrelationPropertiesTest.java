package io.github.ashan.alertfatiguereducer.incident.correlation;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CorrelationPropertiesTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation
                .buildDefaultValidatorFactory()
                .getValidator();
    }

    // ── Existing per-field tests ─────────────────────────────────────────────

    @Test
    void shouldAcceptValidConfiguration() {
        CorrelationProperties properties = new CorrelationProperties();

        assertTrue(validator.validate(properties).isEmpty());
    }

    @Test
    void shouldRejectZeroTimeProximityHalfLife() {
        CorrelationProperties properties = new CorrelationProperties();
        properties.setTimeProximityHalfLifeMinutes(0.0);

        assertFalse(validator.validate(properties).isEmpty());
    }

    @Test
    void shouldRejectThresholdAboveOne() {
        CorrelationProperties properties = new CorrelationProperties();
        properties.setThreshold(1.1);

        assertFalse(validator.validate(properties).isEmpty());
    }

    @Test
    void shouldRejectZeroWindowMinutes() {
        CorrelationProperties properties = new CorrelationProperties();
        properties.setWindowMinutes(0);

        assertFalse(validator.validate(properties).isEmpty());
    }

    // ── New: threshold exclusive lower bound ─────────────────────────────────

    @Test
    void shouldRejectZeroThreshold() {
        CorrelationProperties properties = new CorrelationProperties();
        properties.setThreshold(0.0);

        // @DecimalMin(value = "0.0", inclusive = false) must reject threshold == 0.0
        assertFalse(validator.validate(properties).isEmpty());
    }

    // ── New: cross-field weight sum validation ───────────────────────────────

    @Test
    void shouldPassValidationWhenWeightSumIsExactlyOne() {
        CorrelationProperties properties = new CorrelationProperties();
        // Default weights sum to exactly 1.0 — must pass
        assertDoesNotThrow(properties::validateWeightSum);
    }

    @Test
    void shouldFailValidationWhenWeightSumExceedsOne() {
        CorrelationProperties properties = new CorrelationProperties();
        // Push sameServiceWeight up so the total exceeds 1.0
        properties.setSameServiceWeight(0.50); // 0.50 + 0.30 + 0.15 + 0.25 = 1.20
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                properties::validateWeightSum
        );
        assertTrue(ex.getMessage().contains("must not exceed 1.0"),
                "Exception message should mention the weight sum limit");
    }

    @Test
    void shouldPassValidationWhenWeightSumIsBelowOne() {
        CorrelationProperties properties = new CorrelationProperties();
        // Reduce all weights so sum < 1.0
        properties.setSameServiceWeight(0.20);
        properties.setRelatedAlertTypeWeight(0.20);
        properties.setSameEnvironmentWeight(0.10);
        properties.setTimeProximityMaxScore(0.10); // sum = 0.60
        assertDoesNotThrow(properties::validateWeightSum);
    }
}
