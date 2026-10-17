package io.github.ashan.alertfatiguereducer.integration;

import io.github.ashan.alertfatiguereducer.alert.dto.request.CreateAlertRequest;
import io.github.ashan.alertfatiguereducer.alert.entity.Alert;
import io.github.ashan.alertfatiguereducer.alert.entity.AlertEnvironment;
import io.github.ashan.alertfatiguereducer.alert.entity.AlertSeverity;
import io.github.ashan.alertfatiguereducer.alert.entity.AlertSource;
import io.github.ashan.alertfatiguereducer.alert.repository.AlertRepository;
import io.github.ashan.alertfatiguereducer.alert.service.AlertService;
import io.github.ashan.alertfatiguereducer.incident.correlation.AlertCorrelationEngine;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class AlertTransactionIntegrationTest
        extends AbstractPostgresIntegrationTest {

    @Autowired
    private AlertService alertService;

    @Autowired
    private AlertRepository alertRepository;

    @MockitoBean
    private AlertCorrelationEngine alertCorrelationEngine;

    @Test
    void shouldRollbackAlertWhenCorrelationFails() {
        long initialCount = alertRepository.count();

        CreateAlertRequest request = new CreateAlertRequest(
                AlertSource.CUSTOM,
                "payment-service",
                AlertEnvironment.DEVELOPMENT,
                "CPU_HIGH",
                AlertSeverity.CRITICAL,
                "CPU usage exceeded threshold",
                LocalDateTime.now().minusMinutes(1),
                "cpu.usage",
                95.0,
                80.0
        );

        when(alertCorrelationEngine.correlate(any(Alert.class)))
                .thenThrow(new IllegalStateException(
                        "Simulated correlation failure"));

        assertThrows(
                IllegalStateException.class,
                () -> alertService.createAlert(request)
        );

        assertEquals(
                initialCount,
                alertRepository.count(),
                "The alert insert should be rolled back"
        );
    }
}
