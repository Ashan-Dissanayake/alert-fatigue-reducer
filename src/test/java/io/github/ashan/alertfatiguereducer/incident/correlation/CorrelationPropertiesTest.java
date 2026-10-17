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
}