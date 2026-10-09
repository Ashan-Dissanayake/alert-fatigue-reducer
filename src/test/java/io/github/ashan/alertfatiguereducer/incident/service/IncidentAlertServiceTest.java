package io.github.ashan.alertfatiguereducer.incident.service;

import static org.junit.jupiter.api.Assertions.*;

import io.github.ashan.alertfatiguereducer.alert.entity.Alert;
import io.github.ashan.alertfatiguereducer.alert.entity.AlertEnvironment;
import io.github.ashan.alertfatiguereducer.alert.entity.AlertSeverity;
import io.github.ashan.alertfatiguereducer.alert.entity.AlertSource;
import io.github.ashan.alertfatiguereducer.alert.repository.AlertRepository;
import io.github.ashan.alertfatiguereducer.incident.entity.Incident;
import io.github.ashan.alertfatiguereducer.incident.entity.IncidentAlert;
import io.github.ashan.alertfatiguereducer.incident.entity.IncidentSeverity;
import io.github.ashan.alertfatiguereducer.incident.entity.IncidentStatus;
import io.github.ashan.alertfatiguereducer.incident.repository.IncidentAlertRepository;
import io.github.ashan.alertfatiguereducer.incident.repository.IncidentRepository;
import io.github.ashan.alertfatiguereducer.shared.exception.AlertAlreadyAssociatedException;
import io.github.ashan.alertfatiguereducer.shared.exception.AlertNotFoundException;
import io.github.ashan.alertfatiguereducer.shared.exception.IncidentNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncidentAlertServiceTest {

    @Mock
    private IncidentAlertRepository incidentAlertRepository;

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private AlertRepository alertRepository;

    private IncidentAlertService incidentAlertService;

    @BeforeEach
    void setUp() {
        incidentAlertService = new IncidentAlertService(
                incidentAlertRepository,
                incidentRepository,
                alertRepository
        );
    }


    @Test
    void shouldAttachAlertWithCorrelationScore() {
        Incident incident = createIncident(10L, IncidentSeverity.WARNING);
        Alert alert = createAlert(101L, AlertSeverity.CRITICAL);

        when(incidentRepository.findById(10L))
                .thenReturn(Optional.of(incident));
        when(alertRepository.findById(101L))
                .thenReturn(Optional.of(alert));
        when(incidentAlertRepository.existsByAlertId(101L))
                .thenReturn(false);
        when(incidentAlertRepository.save(any(IncidentAlert.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        IncidentAlert result =
                incidentAlertService.attachAlert(10L, 101L, 0.85);

        assertEquals(10L, result.getIncidentId());
        assertEquals(101L, result.getAlertId());
        assertEquals(0.85, result.getCorrelationScore());
        assertEquals(IncidentSeverity.CRITICAL, incident.getSeverity());

        verify(incidentAlertRepository).save(any(IncidentAlert.class));
        verify(incidentRepository).save(incident);
    }

    @Test
    void shouldRejectCorrelationScoreBelowZero() {
        assertThrows(
                IllegalArgumentException.class,
                () -> incidentAlertService.attachAlert(10L, 101L, -0.01)
        );

        verify(incidentAlertRepository, never()).save(any());
    }

    @Test
    void shouldRejectCorrelationScoreAboveOne() {
        assertThrows(
                IllegalArgumentException.class,
                () -> incidentAlertService.attachAlert(10L, 101L, 1.01)
        );

        verify(incidentAlertRepository, never()).save(any());
    }


    @Test
    void shouldRejectAlertAlreadyAssociatedWithIncident() {
        Incident incident = createIncident(10L, IncidentSeverity.WARNING);
        Alert alert = createAlert(101L, AlertSeverity.WARNING);

        when(incidentRepository.findById(10L))
                .thenReturn(Optional.of(incident));
        when(alertRepository.findById(101L))
                .thenReturn(Optional.of(alert));
        when(incidentAlertRepository.existsByAlertId(101L))
                .thenReturn(true);

        assertThrows(
                AlertAlreadyAssociatedException.class,
                () -> incidentAlertService.attachAlert(10L, 101L, 0.85)
        );

        verify(incidentAlertRepository, never()).save(any());
        verify(incidentRepository, never()).save(any());
    }


    @Test
    void shouldThrowWhenIncidentDoesNotExist() {
        when(incidentRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThrows(
                IncidentNotFoundException.class,
                () -> incidentAlertService.attachAlert(10L, 101L, 0.85)
        );

        verify(alertRepository, never()).findById(any());
        verify(incidentAlertRepository, never()).save(any());
    }


    @Test
    void shouldThrowWhenAlertDoesNotExist() {
        Incident incident = createIncident(10L, IncidentSeverity.WARNING);

        when(incidentRepository.findById(10L))
                .thenReturn(Optional.of(incident));
        when(alertRepository.findById(101L))
                .thenReturn(Optional.empty());

        assertThrows(
                AlertNotFoundException.class,
                () -> incidentAlertService.attachAlert(10L, 101L, 0.85)
        );

        verify(incidentAlertRepository, never()).save(any());
        verify(incidentRepository, never()).save(any());
    }


    private Incident createIncident(Long id, IncidentSeverity severity) {
        Incident incident = new Incident();
        incident.setId(id);
        incident.setTitle("Test incident");
        incident.setService("order-service");
        incident.setEnvironment(AlertEnvironment.PRODUCTION);
        incident.setSeverity(severity);
        incident.setStatus(IncidentStatus.OPEN);
        incident.setStartedAt(LocalDateTime.now().minusMinutes(1));
        incident.setLastUpdatedAt(LocalDateTime.now().minusMinutes(1));
        return incident;
    }

    private Alert createAlert(Long id, AlertSeverity severity) {
        Alert alert = new Alert();
        alert.setId(id);
        alert.setSource(AlertSource.CUSTOM);
        alert.setService("order-service");
        alert.setEnvironment(AlertEnvironment.PRODUCTION);
        alert.setType("CPU_HIGH");
        alert.setSeverity(severity);
        alert.setTimestamp(LocalDateTime.now());
        return alert;
    }


    @Test
    void shouldEscalateIncidentSeverityWhenMoreSevereAlertIsAttached() {
        Incident incident = createIncident(10L, IncidentSeverity.WARNING);
        Alert alert = createAlert(101L, AlertSeverity.CRITICAL);

        when(incidentRepository.findById(10L))
                .thenReturn(Optional.of(incident));
        when(alertRepository.findById(101L))
                .thenReturn(Optional.of(alert));
        when(incidentAlertRepository.existsByAlertId(101L))
                .thenReturn(false);
        when(incidentAlertRepository.save(any(IncidentAlert.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        incidentAlertService.attachAlert(10L, 101L, 0.85);

        assertEquals(IncidentSeverity.CRITICAL, incident.getSeverity());
        verify(incidentRepository).save(incident);
    }

    @Test
    void shouldNotDowngradeIncidentSeverityWhenLessSevereAlertIsAttached() {
        Incident incident = createIncident(10L, IncidentSeverity.CRITICAL);
        Alert alert = createAlert(101L, AlertSeverity.WARNING);

        when(incidentRepository.findById(10L))
                .thenReturn(Optional.of(incident));
        when(alertRepository.findById(101L))
                .thenReturn(Optional.of(alert));
        when(incidentAlertRepository.existsByAlertId(101L))
                .thenReturn(false);
        when(incidentAlertRepository.save(any(IncidentAlert.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        incidentAlertService.attachAlert(10L, 101L, 0.85);

        assertEquals(IncidentSeverity.CRITICAL, incident.getSeverity());
        verify(incidentRepository).save(incident);
    }

    @Test
    void shouldUpdateIncidentLastUpdatedAtWhenAlertIsAttached() {
        LocalDateTime previousTimestamp = LocalDateTime.now().minusMinutes(10);

        Incident incident = createIncident(10L, IncidentSeverity.WARNING);
        incident.setLastUpdatedAt(previousTimestamp);

        Alert alert = createAlert(101L, AlertSeverity.WARNING);

        when(incidentRepository.findById(10L))
                .thenReturn(Optional.of(incident));
        when(alertRepository.findById(101L))
                .thenReturn(Optional.of(alert));
        when(incidentAlertRepository.existsByAlertId(101L))
                .thenReturn(false);
        when(incidentAlertRepository.save(any(IncidentAlert.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        incidentAlertService.attachAlert(10L, 101L, 0.85);

        assertTrue(incident.getLastUpdatedAt().isAfter(previousTimestamp));
        verify(incidentRepository).save(incident);
    }
}