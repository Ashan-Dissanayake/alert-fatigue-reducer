package io.github.ashan.alertfatiguereducer.incident.correlation.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import io.github.ashan.alertfatiguereducer.incident.correlation.RuleEvaluation;
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
    void shouldReturnZeroWhenIncomingTypeIsNull() {
        CorrelationContext context = createContext(
                null,
                "CPU_HIGH"
        );

        assertEquals(0.0, rule.evaluate(context));
        verify(relationshipStore, never()).findRelatedTypes(null);
    }

    @Test
    void shouldReturnZeroWhenIncomingTypeIsUnknown() {
        CorrelationContext context = createContext(
                "UNKNOWN_TYPE",
                "CPU_HIGH"
        );

        assertEquals(0.0, rule.evaluate(context));
        verify(relationshipStore, never()).findRelatedTypes("UNKNOWN_TYPE");
    }

    @Test
    void shouldReturnZeroWhenNoRelationshipsAreRegistered() {
        when(relationshipStore.findRelatedTypes("CPU_HIGH"))
                .thenReturn(Set.of());

        CorrelationContext context = createContext(
                "CPU_HIGH",
                "LATENCY_HIGH"
        );

        assertEquals(0.0, rule.evaluate(context));
    }

    @Test
    void shouldMatchWhenAnyRelatedAlertMatches() {
        when(relationshipStore.findRelatedTypes("LATENCY_HIGH"))
                .thenReturn(Set.of("CPU_HIGH"));

        CorrelationContext context = new CorrelationContext(
                createAlert("LATENCY_HIGH"),
                null,
                List.of(
                        createAlert("DISK_FULL"),
                        createAlert("CPU_HIGH")
                )
        );

        assertEquals(0.30, rule.evaluate(context));
    }

    @Test
    void shouldHandleNullAlertTypesInRelatedAlerts() {
        when(relationshipStore.findRelatedTypes("LATENCY_HIGH"))
                .thenReturn(Set.of("CPU_HIGH"));

        CorrelationContext context = new CorrelationContext(
                createAlert("LATENCY_HIGH"),
                null,
                List.of(
                        createAlert(null),
                        createAlert("CPU_HIGH")
                )
        );

        assertEquals(0.30, rule.evaluate(context));
    }

    @Test
    void shouldReturnZeroWhenRelatedAlertsListIsEmpty() {
        when(relationshipStore.findRelatedTypes("LATENCY_HIGH"))
                .thenReturn(Set.of("CPU_HIGH"));

        CorrelationContext context = new CorrelationContext(
                createAlert("LATENCY_HIGH"),
                null,
                List.of()
        );

        assertEquals(0.0, rule.evaluate(context));
    }

    @Test
    void shouldBeCaseSensitive() {
        CorrelationContext context = createContext(
                "latency_high",
                "CPU_HIGH"
        );

        assertEquals(0.0, rule.evaluate(context));
        verify(relationshipStore, never()).findRelatedTypes("latency_high");
    }

    @Test
    void shouldNotSelfMatchWithoutExplicitSelfRelationship() {
        when(relationshipStore.findRelatedTypes("CPU_HIGH"))
                .thenReturn(Set.of("LATENCY_HIGH"));

        CorrelationContext context = createContext(
                "CPU_HIGH",
                "CPU_HIGH"
        );

        assertEquals(0.0, rule.evaluate(context));
    }

    @Test
    void shouldProvideExplanationWhenRelatedTypeMatches() {
        when(relationshipStore.findRelatedTypes("LATENCY_HIGH"))
                .thenReturn(Set.of("CPU_HIGH"));

        CorrelationContext context = createContext(
                "LATENCY_HIGH",
                "CPU_HIGH"
        );

        RuleEvaluation explanation = rule.evaluateExplanation(context);

        assertEquals("RelatedAlertTypeRule", explanation.ruleName());
        assertEquals(0.30, explanation.score());
        assertEquals(0.30, explanation.weight());
        assertTrue(explanation.matched());
        assertTrue(explanation.reason().contains("relates to incident alert type"));
    }

    @Test
    void shouldProvideExplanationWhenTypeHasNoRelationship() {
        when(relationshipStore.findRelatedTypes("CPU_HIGH"))
                .thenReturn(Set.of("LATENCY_HIGH"));

        CorrelationContext context = createContext(
                "CPU_HIGH",
                "TIMEOUT"
        );

        RuleEvaluation explanation = rule.evaluateExplanation(context);

        assertEquals("RelatedAlertTypeRule", explanation.ruleName());
        assertEquals(0.0, explanation.score());
        assertFalse(explanation.matched());
        assertTrue(explanation.reason().contains("has no relationships matching"));
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
