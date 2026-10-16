package io.github.ashan.alertfatiguereducer.incident.correlation;

import io.github.ashan.alertfatiguereducer.alert.entity.Alert;
import io.github.ashan.alertfatiguereducer.alert.entity.AlertEnvironment;
import io.github.ashan.alertfatiguereducer.incident.correlation.rules.RelatedAlertTypeRule;
import io.github.ashan.alertfatiguereducer.incident.correlation.rules.SameEnvironmentRule;
import io.github.ashan.alertfatiguereducer.incident.correlation.rules.SameServiceRule;
import io.github.ashan.alertfatiguereducer.incident.correlation.rules.TimeProximityRule;
import io.github.ashan.alertfatiguereducer.incident.entity.Incident;
import io.github.ashan.alertfatiguereducer.incident.entity.IncidentSeverity;
import io.github.ashan.alertfatiguereducer.incident.entity.IncidentStatus;
import io.github.ashan.alertfatiguereducer.incident.repository.IncidentRepository;
import io.github.ashan.alertfatiguereducer.incident.service.IncidentAlertService;
import io.github.ashan.alertfatiguereducer.incident.service.IncidentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultAlertCorrelationEngineRulesTest {

    private IncidentRepository incidentRepository;
    private IncidentAlertService incidentAlertService;
    private IncidentService incidentService;

    private DefaultAlertCorrelationEngine engine;

    @BeforeEach
    void setUp() {
        incidentRepository = mock(IncidentRepository.class);
        incidentAlertService = mock(IncidentAlertService.class);
        incidentService = mock(IncidentService.class);

        AlertRelationshipStore relationshipStore =
                mock(AlertRelationshipStore.class);

        when(relationshipStore.findRelatedTypes("LATENCY_HIGH"))
                .thenReturn(Set.of("CPU_HIGH"));

        List<CorrelationRule> rules = List.of(
                new SameServiceRule(),
                new SameEnvironmentRule(),
                new RelatedAlertTypeRule(relationshipStore),
                new TimeProximityRule()
        );

        engine = new DefaultAlertCorrelationEngine(
                incidentRepository,
                incidentAlertService,
                incidentService,
                rules
        );
    }

    @Test
    void shouldCombineAllFourRulesAndAttachAlertWhenScoreMeetsThreshold() {
        LocalDateTime timestamp = LocalDateTime.of(2026, 10, 10, 10, 0);

        Alert incomingAlert = new Alert();
        incomingAlert.setId(2L);
        incomingAlert.setType("LATENCY_HIGH");
        incomingAlert.setService("payment-service");
        incomingAlert.setEnvironment(AlertEnvironment.PRODUCTION);
        incomingAlert.setTimestamp(timestamp);

        Incident candidate = new Incident();
        candidate.setId(50L);
        candidate.setService("payment-service");
        candidate.setEnvironment(AlertEnvironment.PRODUCTION);
        candidate.setSeverity(IncidentSeverity.CRITICAL);
        candidate.setStatus(IncidentStatus.OPEN);
        candidate.setStartedAt(timestamp.minusMinutes(1));
        candidate.setLastUpdatedAt(timestamp.minusMinutes(1));

        Alert relatedAlert = new Alert();
        relatedAlert.setId(1L);
        relatedAlert.setType("CPU_HIGH");
        relatedAlert.setService("payment-service");
        relatedAlert.setEnvironment(AlertEnvironment.PRODUCTION);
        relatedAlert.setTimestamp(timestamp.minusMinutes(1));

        when(incidentRepository.findByStatusAndEnvironmentAndLastUpdatedAtAfter(
                eq(IncidentStatus.OPEN),
                eq(AlertEnvironment.PRODUCTION),
                eq(timestamp.minusMinutes(10))
        )).thenReturn(List.of(candidate));

        when(incidentAlertService.findAlertsByIncidentId(50L))
                .thenReturn(List.of(relatedAlert));

        CorrelationResult result = engine.correlate(incomingAlert);


        // Same service: 0.30
        // Same environment: 0.15
        // Related alert types: 0.30
        // Exponential time decay after one minute: approximately 0.2176
        // Total: approximately 0.9676

        assertEquals(CorrelationAction.ATTACHED_TO_INCIDENT, result.action());
        assertEquals(50L, result.incidentId());
        assertEquals(0.967637640824031, result.score(), 0.000001);

        verify(incidentAlertService)
                .attachAlert(50L, 2L, result.score());

    }


}
