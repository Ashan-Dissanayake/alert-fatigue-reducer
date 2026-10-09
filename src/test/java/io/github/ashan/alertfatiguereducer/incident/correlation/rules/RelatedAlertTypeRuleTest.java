package io.github.ashan.alertfatiguereducer.incident.correlation.rules;

import io.github.ashan.alertfatiguereducer.alert.entity.Alert;
import io.github.ashan.alertfatiguereducer.incident.correlation.CorrelationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RelatedAlertTypeRuleTest {

    private RelatedAlertTypeRule rule;

    @BeforeEach
    void setUp() {
        rule = new RelatedAlertTypeRule();
    }

    @Test
    void shouldReturnScoreWhenRelatedAlertTypeExists() {
        Alert incomingAlert = createAlert("LATENCY_HIGH");
        Alert relatedAlert = createAlert("CPU_HIGH");

        CorrelationContext context = new CorrelationContext(
                incomingAlert,
                null,
                List.of(relatedAlert)
        );

        assertEquals(0.30, rule.evaluate(context));
    }

    @Test
    void shouldReturnZeroWhenNoRelatedAlertTypeExists() {
        Alert incomingAlert = createAlert("CPU_HIGH");
        Alert existingAlert = createAlert("TIMEOUT");

        CorrelationContext context = new CorrelationContext(
                incomingAlert,
                null,
                List.of(existingAlert)
        );

        assertEquals(0.0, rule.evaluate(context));
    }

    @Test
    void shouldReturnZeroWhenRelatedAlertsAreEmpty() {
        Alert incomingAlert = createAlert("CPU_HIGH");

        CorrelationContext context = new CorrelationContext(
                incomingAlert,
                null,
                List.of()
        );

        assertEquals(0.0, rule.evaluate(context));
    }

    @Test
    void shouldReturnZeroWhenIncomingAlertTypeIsInvalid() {
        Alert incomingAlert = createAlert("INVALID_TYPE");
        Alert existingAlert = createAlert("LATENCY_HIGH");

        CorrelationContext context = new CorrelationContext(
                incomingAlert,
                null,
                List.of(existingAlert)
        );

        assertEquals(0.0, rule.evaluate(context));
    }

    @Test
    void shouldReturnZeroWhenExistingAlertTypeIsInvalid() {
        Alert incomingAlert = createAlert("LATENCY_HIGH");
        Alert existingAlert = createAlert("INVALID_TYPE");

        CorrelationContext context = new CorrelationContext(
                incomingAlert,
                null,
                List.of(existingAlert)
        );

        assertEquals(0.0, rule.evaluate(context));
    }

    private Alert createAlert(String type) {
        Alert alert = new Alert();
        alert.setType(type);
        return alert;
    }
}
