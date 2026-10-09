package io.github.ashan.alertfatiguereducer.incident.correlation.rules;

import io.github.ashan.alertfatiguereducer.alert.entity.Alert;
import io.github.ashan.alertfatiguereducer.incident.correlation.CorrelationContext;
import io.github.ashan.alertfatiguereducer.incident.entity.Incident;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SameServiceRuleTest {

    private final SameServiceRule rule = new SameServiceRule();

    @Test
    void shouldReturnScoreWhenServicesMatch() {
        Alert alert = new Alert();
        alert.setService("order-service");

        Incident incident = new Incident();
        incident.setService("order-service");

        assertEquals(0.30, rule.evaluate(new CorrelationContext(alert, incident, List.of())));
    }

    @Test
    void shouldReturnZeroWhenServicesDiffer() {
        Alert alert = new Alert();
        alert.setService("order-service");

        Incident incident = new Incident();
        incident.setService("payment-service");

        assertEquals(0.0, rule.evaluate(new CorrelationContext(alert, incident, List.of())));
    }

    @Test
    void shouldReturnZeroWhenAlertServiceIsNull() {
        Alert alert = new Alert();

        Incident incident = new Incident();
        incident.setService("order-service");

        assertEquals(0.0, rule.evaluate(new CorrelationContext(alert, incident, List.of())));
    }
}