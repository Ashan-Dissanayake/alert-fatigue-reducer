package io.github.ashan.alertfatiguereducer.incident.correlation;

import io.github.ashan.alertfatiguereducer.alert.entity.Alert;
import io.github.ashan.alertfatiguereducer.alert.entity.AlertEnvironment;
import io.github.ashan.alertfatiguereducer.alert.entity.AlertSeverity;
import io.github.ashan.alertfatiguereducer.incident.entity.Incident;
import io.github.ashan.alertfatiguereducer.incident.entity.IncidentSeverity;
import io.github.ashan.alertfatiguereducer.incident.entity.IncidentStatus;
import io.github.ashan.alertfatiguereducer.incident.repository.IncidentRepository;
import io.github.ashan.alertfatiguereducer.incident.service.CreateIncidentCommand;
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

class DefaultAlertCorrelationEngineTest {

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

        engine = new DefaultAlertCorrelationEngine(
                incidentRepository,
                incidentAlertService,
                incidentService,
                List.of(correlationRule)
        );
    }

    @Test
    void shouldCreateIncidentAndAttachAlertWhenNoCandidatesExist() {
        Alert alert = createAlert(1L, "CPU_HIGH", "order-service");

        when(incidentRepository
                .findByStatusAndEnvironmentAndLastUpdatedAtAfter(
                        eq(IncidentStatus.OPEN),
                        eq(AlertEnvironment.PRODUCTION),
                        any(LocalDateTime.class)
                ))
                .thenReturn(List.of());

        Incident createdIncident = createIncident(10L);

        when(incidentService.createIncident(any(CreateIncidentCommand.class)))
                .thenReturn(createdIncident);

        CorrelationResult result = engine.correlate(alert);

        assertEquals(CorrelationAction.NEW_INCIDENT, result.action());
        assertEquals(10L, result.incidentId());
        assertEquals(0.0, result.score());

        verify(incidentService).createIncident(any(CreateIncidentCommand.class));
        verify(incidentAlertService).attachAlert(10L, 1L, 0.0);
        verifyNoInteractions(correlationRule);
    }

    @Test
    void shouldAttachAlertToCandidateWhenScoreMeetsThreshold() {
        Alert alert = createAlert(2L, "LATENCY_HIGH", "order-service");
        Incident candidate = createIncident(20L);

        when(incidentRepository
                .findByStatusAndEnvironmentAndLastUpdatedAtAfter(
                        eq(IncidentStatus.OPEN),
                        eq(AlertEnvironment.PRODUCTION),
                        any(LocalDateTime.class)
                ))
                .thenReturn(List.of(candidate));

        when(incidentAlertService.findAlertsByIncidentId(20L))
                .thenReturn(List.of(createAlert(1L, "CPU_HIGH", "order-service")));

        when(correlationRule.evaluate(any(CorrelationContext.class)))
                .thenReturn(0.70);

        CorrelationResult result = engine.correlate(alert);

        assertEquals(CorrelationAction.ATTACHED_TO_INCIDENT, result.action());
        assertEquals(20L, result.incidentId());
        assertEquals(0.70, result.score());

        verify(incidentAlertService).attachAlert(20L, 2L, 0.70);
        verify(incidentService, never()).createIncident(any());
    }

    @Test
    void shouldCreateIncidentWhenCandidateScoresAreBelowThreshold() {
        Alert alert = createAlert(3L, "CPU_HIGH", "order-service");
        Incident candidate = createIncident(30L);

        when(incidentRepository
                .findByStatusAndEnvironmentAndLastUpdatedAtAfter(
                        eq(IncidentStatus.OPEN),
                        eq(AlertEnvironment.PRODUCTION),
                        any(LocalDateTime.class)
                ))
                .thenReturn(List.of(candidate));

        when(incidentAlertService.findAlertsByIncidentId(30L))
                .thenReturn(List.of(createAlert(1L, "TIMEOUT", "order-service")));

        when(correlationRule.evaluate(any(CorrelationContext.class)))
                .thenReturn(0.40);

        Incident createdIncident = createIncident(40L);

        when(incidentService.createIncident(any(CreateIncidentCommand.class)))
                .thenReturn(createdIncident);

        CorrelationResult result = engine.correlate(alert);

        assertEquals(CorrelationAction.NEW_INCIDENT, result.action());
        assertEquals(40L, result.incidentId());
        assertEquals(0.0, result.score());

        verify(incidentService)
                .createIncident(any(CreateIncidentCommand.class));

        verify(incidentAlertService).attachAlert(40L, 3L, 0.0);

        verify(incidentAlertService, never())
                .attachAlert(30L, 3L, 0.40);
    }

    @Test
    void shouldChooseCandidateWithHighestQualifyingScore() {
        Alert alert = createAlert(4L, "LATENCY_HIGH", "order-service");
        Incident firstCandidate = createIncident(50L);
        Incident secondCandidate = createIncident(60L);

        when(incidentRepository
                .findByStatusAndEnvironmentAndLastUpdatedAtAfter(
                        eq(IncidentStatus.OPEN),
                        eq(AlertEnvironment.PRODUCTION),
                        any(LocalDateTime.class)
                ))
                .thenReturn(List.of(firstCandidate, secondCandidate));

        when(incidentAlertService.findAlertsByIncidentId(50L))
                .thenReturn(List.of(createAlert(1L, "CPU_HIGH", "order-service")));

        when(incidentAlertService.findAlertsByIncidentId(60L))
                .thenReturn(List.of(createAlert(2L, "ERROR_RATE_HIGH", "order-service")));

        when(correlationRule.evaluate(any(CorrelationContext.class)))
                .thenReturn(0.75, 0.85);

        CorrelationResult result = engine.correlate(alert);

        assertEquals(CorrelationAction.ATTACHED_TO_INCIDENT, result.action());
        assertEquals(60L, result.incidentId());
        assertEquals(0.85, result.score());

        verify(incidentAlertService).attachAlert(60L, 4L, 0.85);
        verify(incidentAlertService, never()).attachAlert(50L, 4L, 0.75);
        verify(incidentService, never()).createIncident(any());
    }

    private Alert createAlert(Long id, String type, String service) {
        Alert alert = new Alert();
        alert.setId(id);
        alert.setType(type);
        alert.setService(service);
        alert.setEnvironment(AlertEnvironment.PRODUCTION);
        alert.setSeverity(AlertSeverity.CRITICAL);
        alert.setTimestamp(LocalDateTime.now());
        return alert;
    }

    private Incident createIncident(Long id) {
        Incident incident = new Incident();
        incident.setId(id);
        incident.setService("order-service");
        incident.setEnvironment(AlertEnvironment.PRODUCTION);
        incident.setSeverity(IncidentSeverity.CRITICAL);
        incident.setStatus(IncidentStatus.OPEN);
        incident.setStartedAt(LocalDateTime.now());
        incident.setLastUpdatedAt(LocalDateTime.now());
        return incident;
    }
}