package io.github.ashan.alertfatiguereducer.incident.service;

import static org.junit.jupiter.api.Assertions.*;

import io.github.ashan.alertfatiguereducer.alert.repository.AlertRepository;
import io.github.ashan.alertfatiguereducer.incident.entity.IncidentAlert;
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
        when(incidentRepository.existsById(10L)).thenReturn(true);
        when(alertRepository.existsById(101L)).thenReturn(true);
        when(incidentAlertRepository.existsByAlertId(101L))
                .thenReturn(false);

        when(incidentAlertRepository.save(any(IncidentAlert.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        IncidentAlert result =
                incidentAlertService.attachAlert(10L, 101L, 0.85);

        assertEquals(10L, result.getIncidentId());
        assertEquals(101L, result.getAlertId());
        assertEquals(0.85, result.getCorrelationScore());

        verify(incidentAlertRepository).save(any(IncidentAlert.class));
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
        when(incidentRepository.existsById(10L)).thenReturn(true);
        when(alertRepository.existsById(101L)).thenReturn(true);
        when(incidentAlertRepository.existsByAlertId(101L))
                .thenReturn(true);

        assertThrows(
                AlertAlreadyAssociatedException.class,
                () -> incidentAlertService.attachAlert(10L, 101L, 0.85)
        );

        verify(incidentAlertRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenIncidentDoesNotExist() {
        when(incidentRepository.existsById(10L)).thenReturn(false);

        assertThrows(
                IncidentNotFoundException.class,
                () -> incidentAlertService.attachAlert(10L, 101L, 0.85)
        );

        verify(incidentAlertRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenAlertDoesNotExist() {
        when(incidentRepository.existsById(10L)).thenReturn(true);
        when(alertRepository.existsById(101L)).thenReturn(false);

        assertThrows(
                AlertNotFoundException.class,
                () -> incidentAlertService.attachAlert(10L, 101L, 0.85)
        );

        verify(incidentAlertRepository, never()).save(any());
    }
}