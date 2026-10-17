package io.github.ashan.alertfatiguereducer.incident.correlation.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.ashan.alertfatiguereducer.alert.entity.Alert;
import io.github.ashan.alertfatiguereducer.incident.correlation.AlertRelationshipStore;
import io.github.ashan.alertfatiguereducer.incident.correlation.CorrelationContext;
import java.util.List;
import java.util.Set;

import io.github.ashan.alertfatiguereducer.incident.correlation.CorrelationProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RelatedAlertTypeRuleTest {

    private AlertRelationshipStore relationshipStore;
    private RelatedAlertTypeRule rule;

    @BeforeEach
    void setUp() {
        relationshipStore = mock(AlertRelationshipStore.class);

        CorrelationProperties properties = new CorrelationProperties();
        rule = new RelatedAlertTypeRule(relationshipStore, properties);
    }

    @Test
    void shouldReturnScoreWhenRelatedAlertTypeExists() {
        when(relationshipStore.findRelatedTypes("LATENCY_HIGH"))
                .thenReturn(Set.of("CPU_HIGH"));

        CorrelationContext context = createContext(
                "LATENCY_HIGH",
                "CPU_HIGH"
        );

        assertEquals(0.30, rule.evaluate(context));
    }

    @Test
    void shouldReturnZeroWhenNoRelatedAlertTypeExists() {
        when(relationshipStore.findRelatedTypes("CPU_HIGH"))
                .thenReturn(Set.of("LATENCY_HIGH"));

        CorrelationContext context = createContext(
                "CPU_HIGH",
                "TIMEOUT"
        );

        assertEquals(0.0, rule.evaluate(context));
    }

    @Test
    void shouldReturnZeroWhenRelatedAlertsAreEmpty() {
        when(relationshipStore.findRelatedTypes("CPU_HIGH"))
                .thenReturn(Set.of("LATENCY_HIGH"));

        CorrelationContext context = new CorrelationContext(
                createAlert("CPU_HIGH"),
                null,
                List.of()
        );

        assertEquals(0.0, rule.evaluate(context));
    }

    @Test
    void shouldReturnZeroWhenIncomingAlertTypeIsInvalid() {
        CorrelationContext context = createContext(
                "INVALID_TYPE",
                "LATENCY_HIGH"
        );

        assertEquals(0.0, rule.evaluate(context));

        verify(relationshipStore, never())
                .findRelatedTypes("INVALID_TYPE");
    }

    @Test
    void shouldReturnZeroWhenExistingAlertTypeIsInvalid() {
        when(relationshipStore.findRelatedTypes("LATENCY_HIGH"))
                .thenReturn(Set.of("CPU_HIGH"));

        CorrelationContext context = createContext(
                "LATENCY_HIGH",
                "INVALID_TYPE"
        );

        assertEquals(0.0, rule.evaluate(context));
    }

    @Test
    void shouldReturnZeroWhenIncomingAlertTypeIsNull() {
        CorrelationContext context = createContext(null, "LATENCY_HIGH");

        assertEquals(0.0, rule.evaluate(context));

        verify(relationshipStore, never()).findRelatedTypes(null);
    }

    @Test
    void shouldReturnScoreWhenCpuHighIsRelatedToLatencyHigh() {
        when(relationshipStore.findRelatedTypes("CPU_HIGH"))
                .thenReturn(Set.of("LATENCY_HIGH"));

        CorrelationContext context = createContext(
                "CPU_HIGH",
                "LATENCY_HIGH"
        );

        assertEquals(0.30, rule.evaluate(context));
    }

    @Test
    void shouldReturnScoreWhenErrorRateHighIsRelatedToTimeout() {
        when(relationshipStore.findRelatedTypes("ERROR_RATE_HIGH"))
                .thenReturn(Set.of("TIMEOUT"));

        CorrelationContext context = createContext(
                "ERROR_RATE_HIGH",
                "TIMEOUT"
        );

        assertEquals(0.30, rule.evaluate(context));
    }

    @Test
    void shouldReturnScoreWhenTimeoutIsRelatedToErrorRateHigh() {
        when(relationshipStore.findRelatedTypes("TIMEOUT"))
                .thenReturn(Set.of("ERROR_RATE_HIGH"));

        CorrelationContext context = createContext(
                "TIMEOUT",
                "ERROR_RATE_HIGH"
        );

        assertEquals(0.30, rule.evaluate(context));
    }

    @Test
    void shouldReturnZeroWhenStoreHasNoRelationships() {
        when(relationshipStore.findRelatedTypes("CPU_HIGH"))
                .thenReturn(Set.of());

        CorrelationContext context = createContext(
                "CPU_HIGH",
                "LATENCY_HIGH"
        );

        assertEquals(0.0, rule.evaluate(context));
    }

    private CorrelationContext createContext(
            String incomingType,
            String existingType
    ) {
        return new CorrelationContext(
                createAlert(incomingType),
                null,
                List.of(createAlert(existingType))
        );
    }

    private Alert createAlert(String type) {
        Alert alert = new Alert();
        alert.setType(type);
        return alert;
    }
}
