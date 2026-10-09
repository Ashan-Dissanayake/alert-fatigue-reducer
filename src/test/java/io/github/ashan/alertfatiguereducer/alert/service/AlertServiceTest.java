package io.github.ashan.alertfatiguereducer.alert.service;

import io.github.ashan.alertfatiguereducer.alert.dto.request.CreateAlertRequest;
import io.github.ashan.alertfatiguereducer.alert.dto.response.AlertCorrelationResponse;
import io.github.ashan.alertfatiguereducer.alert.dto.response.AlertResponse;
import io.github.ashan.alertfatiguereducer.alert.entity.Alert;
import io.github.ashan.alertfatiguereducer.alert.entity.AlertEnvironment;
import io.github.ashan.alertfatiguereducer.alert.entity.AlertSeverity;
import io.github.ashan.alertfatiguereducer.alert.entity.AlertSource;
import io.github.ashan.alertfatiguereducer.alert.mapper.AlertMapper;
import io.github.ashan.alertfatiguereducer.alert.repository.AlertRepository;
import io.github.ashan.alertfatiguereducer.incident.correlation.AlertCorrelationEngine;
import io.github.ashan.alertfatiguereducer.incident.correlation.CorrelationAction;
import io.github.ashan.alertfatiguereducer.incident.correlation.CorrelationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;



@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private AlertCorrelationEngine alertCorrelationEngine;

    private AlertMapper alertMapper;
    private AlertService alertService;

    @BeforeEach
    void setUp() {
        alertMapper = new AlertMapper();
        alertService = new AlertService(
                alertRepository,
                alertMapper,
                alertCorrelationEngine
        );
    }

    @Test
    void shouldCreateAlertSuccessfully() {
        CreateAlertRequest request = createRequest(
                "order-service",
                "CPU_HIGH",
                AlertSeverity.WARNING
        );

        when(alertRepository.save(any(Alert.class)))
                .thenAnswer(invocation -> {
                    Alert alert = invocation.getArgument(0);
                    alert.setId(1L);
                    return alert;
                });

        mockCorrelationResult(
                CorrelationAction.NEW_INCIDENT,
                10L,
                0.0
        );

        AlertCorrelationResponse response =
                alertService.createAlert(request);

        assertThat(response.alert().id()).isEqualTo(1L);
        assertThat(response.alert().service()).isEqualTo("order-service");
        assertThat(response.alert().type()).isEqualTo("CPU_HIGH");
        assertThat(response.alert().severity())
                .isEqualTo(AlertSeverity.WARNING);
        assertThat(response.alert().environment())
                .isEqualTo(AlertEnvironment.PRODUCTION);

        assertThat(response.correlationAction())
                .isEqualTo(CorrelationAction.NEW_INCIDENT);
        assertThat(response.incidentId()).isEqualTo(10L);
        assertThat(response.correlationScore()).isEqualTo(0.0);

        verify(alertRepository).save(any(Alert.class));
        verify(alertCorrelationEngine).correlate(any(Alert.class));
    }

    @Test
    void shouldSetCreatedAtWhenCreatingAlert() {
        CreateAlertRequest request = createRequest(
                "order-service",
                "CPU_HIGH",
                AlertSeverity.WARNING
        );

        ArgumentCaptor<Alert> alertCaptor =
                ArgumentCaptor.forClass(Alert.class);

        when(alertRepository.save(any(Alert.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        mockCorrelationResult(
                CorrelationAction.NEW_INCIDENT,
                10L,
                0.0
        );

        alertService.createAlert(request);

        verify(alertRepository).save(alertCaptor.capture());

        Alert savedAlert = alertCaptor.getValue();

        assertThat(savedAlert.getCreatedAt()).isNotNull();
        verify(alertCorrelationEngine).correlate(savedAlert);
    }

    @Test
    void shouldMapSavedAlertToResponse() {
        CreateAlertRequest request = createRequest(
                "payment-service",
                "ERROR_RATE_HIGH",
                AlertSeverity.CRITICAL
        );

        when(alertRepository.save(any(Alert.class)))
                .thenAnswer(invocation -> {
                    Alert alert = invocation.getArgument(0);
                    alert.setId(25L);
                    return alert;
                });

        mockCorrelationResult(
                CorrelationAction.ATTACHED_TO_INCIDENT,
                30L,
                0.85
        );

        AlertCorrelationResponse response =
                alertService.createAlert(request);

        assertThat(response.alert().id()).isEqualTo(25L);
        assertThat(response.alert().service()).isEqualTo("payment-service");
        assertThat(response.alert().metric()).isEqualTo("cpu_usage");
        assertThat(response.alert().value()).isEqualTo(94.5);
        assertThat(response.alert().threshold()).isEqualTo(90.0);

        assertThat(response.correlationAction())
                .isEqualTo(CorrelationAction.ATTACHED_TO_INCIDENT);
        assertThat(response.incidentId()).isEqualTo(30L);
        assertThat(response.correlationScore()).isEqualTo(0.85);

        verify(alertCorrelationEngine).correlate(any(Alert.class));
    }

    @Test
    void shouldPropagateRepositoryException() {
        CreateAlertRequest request = createRequest(
                "order-service",
                "CPU_HIGH",
                AlertSeverity.WARNING
        );

        when(alertRepository.save(any(Alert.class)))
                .thenThrow(new RuntimeException("Database error"));

        org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> alertService.createAlert(request)
        );

        verifyNoInteractions(alertCorrelationEngine);
    }

    @Test
    void shouldPropagateCorrelationException() {
        CreateAlertRequest request = createRequest(
                "order-service",
                "CPU_HIGH",
                AlertSeverity.WARNING
        );

        when(alertRepository.save(any(Alert.class)))
                .thenAnswer(invocation -> {
                    Alert alert = invocation.getArgument(0);
                    alert.setId(1L);
                    return alert;
                });

        when(alertCorrelationEngine.correlate(any(Alert.class)))
                .thenThrow(new RuntimeException("Correlation failed"));

        org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> alertService.createAlert(request)
        );

        verify(alertRepository).save(any(Alert.class));
        verify(alertCorrelationEngine).correlate(any(Alert.class));
    }

    private void mockCorrelationResult(
            CorrelationAction action,
            Long incidentId,
            double score
    ) {
        when(alertCorrelationEngine.correlate(any(Alert.class)))
                .thenReturn(new CorrelationResult(action, incidentId, score));
    }

    private CreateAlertRequest createRequest(
            String service,
            String type,
            AlertSeverity severity
    ) {
        return new CreateAlertRequest(
                AlertSource.CUSTOM,
                service,
                AlertEnvironment.PRODUCTION,
                type,
                severity,
                "Test alert message",
                LocalDateTime.of(2026, 10, 8, 9, 31),
                "cpu_usage",
                94.5,
                90.0
        );
    }
}