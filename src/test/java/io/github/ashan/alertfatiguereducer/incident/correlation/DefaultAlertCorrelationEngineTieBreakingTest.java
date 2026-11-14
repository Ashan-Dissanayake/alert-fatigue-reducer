package io.github.ashan.alertfatiguereducer.incident.correlation;

import io.github.ashan.alertfatiguereducer.alert.entity.Alert;
import io.github.ashan.alertfatiguereducer.alert.entity.AlertEnvironment;
import io.github.ashan.alertfatiguereducer.alert.entity.AlertSeverity;
import io.github.ashan.alertfatiguereducer.alert.entity.AlertSource;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class DefaultAlertCorrelationEngineTieBreakingTest {

    private IncidentRepository incidentRepository;
    private IncidentAlertService incidentAlertService;
    private IncidentService incidentService;
    private CorrelationRule correlationRule;
    private DefaultAlertCorrelationEngine engine;

    @BeforeEach
    void setUp() {
        incidentRepository = mock(IncidentRepository.class);
        incidentAlertService = mock(IncidentAlertService.class);
        incidentService = mock(IncidentService.class);
        correlationRule = mock(CorrelationRule.class);

        CorrelationProperties properties = new CorrelationProperties();
        properties.setThreshold(0.50);

        engine = new DefaultAlertCorrelationEngine(
                incidentRepository,
                incidentAlertService,
                incidentService,
                List.of(correlationRule),
                properties
        );
    }

    private Alert createAlert(Long id, LocalDateTime timestamp) {
        Alert alert = new Alert();
        alert.setId(id);
        alert.setSource(AlertSource.CUSTOM);
        alert.setService("order-service");
        alert.setEnvironment(AlertEnvironment.PRODUCTION);
        alert.setType("CPU_HIGH");
        alert.setSeverity(AlertSeverity.WARNING);
        alert.setMessage("High CPU");
        alert.setTimestamp(timestamp);
        return alert;
    }

    private Incident createIncident(Long id, LocalDateTime lastUpdatedAt) {
        Incident incident = new Incident();
        incident.setId(id);
        incident.setTitle("CPU_HIGH - order-service");
        incident.setService("order-service");
        incident.setEnvironment(AlertEnvironment.PRODUCTION);
        incident.setSeverity(IncidentSeverity.WARNING);
        incident.setStatus(IncidentStatus.OPEN);
        incident.setStartedAt(lastUpdatedAt.minusMinutes(10));
        incident.setLastUpdatedAt(lastUpdatedAt);
        return incident;
    }

    @Test
    void shouldPickCandidateWithHigherScore() {
        LocalDateTime now = LocalDateTime.now();
        Alert alert = createAlert(100L, now);

        Incident incidentA = createIncident(1L, now.minusMinutes(5));
        Incident incidentB = createIncident(2L, now.minusMinutes(2));

        when(incidentRepository.findByStatusAndEnvironmentAndLastUpdatedAtAfter(
                eq(IncidentStatus.OPEN), eq(AlertEnvironment.PRODUCTION), any(LocalDateTime.class)))
                .thenReturn(List.of(incidentA, incidentB));

        when(incidentAlertService.findAlertsByIncidentId(anyLong()))
                .thenReturn(List.of());

        // incidentA has higher score (0.80) than incidentB (0.60)
        when(correlationRule.evaluate(argThat(ctx -> ctx != null && ctx.incident().getId().equals(1L))))
                .thenReturn(0.80);
        when(correlationRule.evaluate(argThat(ctx -> ctx != null && ctx.incident().getId().equals(2L))))
                .thenReturn(0.60);

        CorrelationResult result = engine.correlate(alert);

        assertEquals(CorrelationAction.ATTACHED_TO_INCIDENT, result.action());
        assertEquals(1L, result.incidentId());
        assertEquals(0.80, result.score(), 0.001);
    }

    @Test
    void shouldBreakTieByPickingMostRecentlyUpdatedCandidateWhenScoresAreEqual() {
        LocalDateTime now = LocalDateTime.now();
        Alert alert = createAlert(100L, now);

        // incidentA updated 5 mins ago, incidentB updated 1 min ago (more recent)
        Incident incidentA = createIncident(1L, now.minusMinutes(5));
        Incident incidentB = createIncident(2L, now.minusMinutes(1));

        when(incidentRepository.findByStatusAndEnvironmentAndLastUpdatedAtAfter(
                eq(IncidentStatus.OPEN), eq(AlertEnvironment.PRODUCTION), any(LocalDateTime.class)))
                .thenReturn(List.of(incidentA, incidentB));

        when(incidentAlertService.findAlertsByIncidentId(anyLong()))
                .thenReturn(List.of());

        // Both candidates produce equal score 0.75
        when(correlationRule.evaluate(any(CorrelationContext.class)))
                .thenReturn(0.75);

        CorrelationResult result = engine.correlate(alert);

        assertEquals(CorrelationAction.ATTACHED_TO_INCIDENT, result.action());
        assertEquals(2L, result.incidentId(), "Should pick incidentB because its lastUpdatedAt is more recent");
        assertEquals(0.75, result.score(), 0.001);
    }

    @Test
    void shouldBreakTieByHigherIncidentIdWhenScoresAndLastUpdatedAtAreEqual() {
        LocalDateTime now = LocalDateTime.now();
        Alert alert = createAlert(100L, now);

        LocalDateTime exactSameTimestamp = now.minusMinutes(3);
        Incident incidentA = createIncident(10L, exactSameTimestamp);
        Incident incidentB = createIncident(25L, exactSameTimestamp);

        when(incidentRepository.findByStatusAndEnvironmentAndLastUpdatedAtAfter(
                eq(IncidentStatus.OPEN), eq(AlertEnvironment.PRODUCTION), any(LocalDateTime.class)))
                .thenReturn(List.of(incidentA, incidentB));

        when(incidentAlertService.findAlertsByIncidentId(anyLong()))
                .thenReturn(List.of());

        when(correlationRule.evaluate(any(CorrelationContext.class)))
                .thenReturn(0.75);

        CorrelationResult result = engine.correlate(alert);

        assertEquals(CorrelationAction.ATTACHED_TO_INCIDENT, result.action());
        assertEquals(25L, result.incidentId(), "Should deterministically pick incident with higher ID on identical timestamps");
    }
}
