package io.github.ashan.alertfatiguereducer.incident.correlation.rules;

import io.github.ashan.alertfatiguereducer.alert.entity.Alert;
import io.github.ashan.alertfatiguereducer.alert.entity.AlertEnvironment;
import io.github.ashan.alertfatiguereducer.incident.correlation.CorrelationContext;
import io.github.ashan.alertfatiguereducer.incident.entity.Incident;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SameEnvironmentRuleTest {

    private final SameEnvironmentRule rule = new SameEnvironmentRule();

    @Test
    void shouldReturnScoreWhenEnvironmentsMatch() {
        Alert alert = new Alert();
        alert.setEnvironment(AlertEnvironment.PRODUCTION);

        Incident incident = new Incident();
        incident.setEnvironment(AlertEnvironment.PRODUCTION);

        assertEquals(0.15, rule.evaluate(new CorrelationContext(alert, incident, List.of())));
    }

    @Test
    void shouldReturnZeroWhenEnvironmentsDiffer() {
        Alert alert = new Alert();
        alert.setEnvironment(AlertEnvironment.PRODUCTION);

        Incident incident = new Incident();
        incident.setEnvironment(AlertEnvironment.STAGING);

        assertEquals(0.0, rule.evaluate(new CorrelationContext(alert, incident, List.of())));
    }

    @Test
    void shouldReturnZeroWhenAlertEnvironmentIsNull() {
        Alert alert = new Alert();

        Incident incident = new Incident();
        incident.setEnvironment(AlertEnvironment.PRODUCTION);

        assertEquals(0.0, rule.evaluate(new CorrelationContext(alert, incident, List.of())));
    }
}