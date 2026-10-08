package io.github.ashan.alertfatiguereducer.alert.service;

import io.github.ashan.alertfatiguereducer.alert.dto.request.CreateAlertRequest;
import io.github.ashan.alertfatiguereducer.alert.dto.response.AlertResponse;
import io.github.ashan.alertfatiguereducer.alert.entity.Alert;
import io.github.ashan.alertfatiguereducer.alert.entity.AlertEnvironment;
import io.github.ashan.alertfatiguereducer.alert.entity.AlertSeverity;
import io.github.ashan.alertfatiguereducer.alert.entity.AlertSource;
import io.github.ashan.alertfatiguereducer.alert.mapper.AlertMapper;
import io.github.ashan.alertfatiguereducer.alert.repository.AlertRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    private AlertRepository alertRepository;

    private AlertMapper alertMapper;

    private AlertService alertService;

    @BeforeEach
    void setUp() {
        alertMapper = new AlertMapper();
        alertService = new AlertService(alertRepository, alertMapper);
    }

    @Test
    void shouldCreateAlertSuccessfully() {

        CreateAlertRequest request = new CreateAlertRequest(
                AlertSource.CUSTOM,
                "order-service",
                AlertEnvironment.PRODUCTION,
                "CPU_HIGH",
                AlertSeverity.WARNING,
                "CPU usage exceeded 90%",
                LocalDateTime.of(2026, 10, 8, 9, 31),
                "cpu_usage",
                94.5,
                90.0
        );

        when(alertRepository.save(any(Alert.class)))
                .thenAnswer(invocation -> {
                    Alert alert = invocation.getArgument(0);

                    alert.setId(1L);

                    return alert;
                });

        AlertResponse response = alertService.createAlert(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.service()).isEqualTo("order-service");
        assertThat(response.type()).isEqualTo("CPU_HIGH");
        assertThat(response.severity()).isEqualTo(AlertSeverity.WARNING);
        assertThat(response.environment())
                .isEqualTo(AlertEnvironment.PRODUCTION);

        verify(alertRepository).save(any(Alert.class));
    }

    @Test
    void shouldSetCreatedAtWhenCreatingAlert() {

        CreateAlertRequest request = new CreateAlertRequest(
                AlertSource.CUSTOM,
                "order-service",
                AlertEnvironment.PRODUCTION,
                "CPU_HIGH",
                AlertSeverity.WARNING,
                "CPU usage exceeded 90%",
                LocalDateTime.of(2026, 10, 8, 9, 31),
                "cpu_usage",
                94.5,
                90.0
        );

        ArgumentCaptor<Alert> alertCaptor =
                ArgumentCaptor.forClass(Alert.class);

        when(alertRepository.save(any(Alert.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        alertService.createAlert(request);

        verify(alertRepository).save(alertCaptor.capture());

        Alert savedAlert = alertCaptor.getValue();

        assertThat(savedAlert.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldMapSavedAlertToResponse() {

        CreateAlertRequest request = new CreateAlertRequest(
                AlertSource.CUSTOM,
                "payment-service",
                AlertEnvironment.PRODUCTION,
                "ERROR_RATE_HIGH",
                AlertSeverity.CRITICAL,
                "Error rate exceeded threshold",
                LocalDateTime.of(2026, 10, 8, 10, 0),
                "error_rate",
                12.5,
                5.0
        );

        when(alertRepository.save(any(Alert.class)))
                .thenAnswer(invocation -> {
                    Alert alert = invocation.getArgument(0);
                    alert.setId(25L);
                    return alert;
                });

        AlertResponse response = alertService.createAlert(request);

        assertThat(response.id()).isEqualTo(25L);
        assertThat(response.service()).isEqualTo("payment-service");
        assertThat(response.metric()).isEqualTo("error_rate");
        assertThat(response.value()).isEqualTo(12.5);
        assertThat(response.threshold()).isEqualTo(5.0);
    }

    @Test
    void shouldPropagateRepositoryException() {

        CreateAlertRequest request = new CreateAlertRequest(
                AlertSource.CUSTOM,
                "order-service",
                AlertEnvironment.PRODUCTION,
                "CPU_HIGH",
                AlertSeverity.WARNING,
                "CPU usage exceeded 90%",
                LocalDateTime.of(2026, 10, 8, 9, 31),
                "cpu_usage",
                94.5,
                90.0
        );

        when(alertRepository.save(any(Alert.class)))
                .thenThrow(new RuntimeException("Database error"));

        org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> alertService.createAlert(request)
        );
    }
}