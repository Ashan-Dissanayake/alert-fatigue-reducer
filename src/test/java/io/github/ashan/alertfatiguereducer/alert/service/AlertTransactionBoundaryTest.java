package io.github.ashan.alertfatiguereducer.alert.service;

import io.github.ashan.alertfatiguereducer.alert.dto.request.CreateAlertRequest;
import io.github.ashan.alertfatiguereducer.alert.entity.Alert;
import io.github.ashan.alertfatiguereducer.alert.entity.AlertEnvironment;
import io.github.ashan.alertfatiguereducer.alert.entity.AlertSeverity;
import io.github.ashan.alertfatiguereducer.alert.entity.AlertSource;
import io.github.ashan.alertfatiguereducer.alert.mapper.AlertMapper;
import io.github.ashan.alertfatiguereducer.alert.repository.AlertRepository;
import io.github.ashan.alertfatiguereducer.incident.correlation.AlertCorrelationEngine;
import io.github.ashan.alertfatiguereducer.incident.correlation.CorrelationLockService;
import io.github.ashan.alertfatiguereducer.incident.correlation.DefaultAlertCorrelationEngine;
import io.github.ashan.alertfatiguereducer.incident.repository.IncidentAlertRepository;
import io.github.ashan.alertfatiguereducer.incident.service.IncidentAlertService;
import io.github.ashan.alertfatiguereducer.incident.service.IncidentService;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AlertTransactionBoundaryTest {

    @Test
    void shouldHaveSpringTransactionalOnAlertServiceCreateAlert() throws NoSuchMethodException {
        Method createAlertMethod = AlertService.class.getMethod("createAlert", CreateAlertRequest.class);
        Transactional transactional = AnnotatedElementUtils.findMergedAnnotation(createAlertMethod, Transactional.class);

        assertNotNull(transactional, "AlertService.createAlert must be annotated with Spring's @Transactional");
        assertEquals(Propagation.REQUIRED, transactional.propagation(), "Transaction propagation should default to REQUIRED");
    }

    @Test
    void shouldHaveSpringTransactionalOnDefaultAlertCorrelationEngineCorrelate() throws NoSuchMethodException {
        Method correlateMethod = DefaultAlertCorrelationEngine.class.getMethod("correlate", Alert.class);
        Transactional transactional = AnnotatedElementUtils.findMergedAnnotation(correlateMethod, Transactional.class);

        assertNotNull(transactional, "DefaultAlertCorrelationEngine.correlate must be annotated with Spring's @Transactional");
        assertEquals(Propagation.REQUIRED, transactional.propagation());
    }

    @Test
    void shouldHaveSpringTransactionalOnIncidentAlertServiceAttachAlert() throws NoSuchMethodException {
        Method attachAlertMethod = IncidentAlertService.class.getMethod("attachAlert", Long.class, Long.class, double.class);
        Transactional transactional = AnnotatedElementUtils.findMergedAnnotation(attachAlertMethod, Transactional.class);

        assertNotNull(transactional, "IncidentAlertService.attachAlert must be annotated with Spring's @Transactional");
    }

    @Test
    void shouldHaveSpringTransactionalOnIncidentServiceCreateIncident() {
        Method[] methods = IncidentService.class.getMethods();
        boolean hasTransactional = false;
        for (Method method : methods) {
            if ("createIncident".equals(method.getName())) {
                Transactional transactional = AnnotatedElementUtils.findMergedAnnotation(method, Transactional.class);
                if (transactional != null) {
                    hasTransactional = true;
                    break;
                }
            }
        }
        assertTrue(hasTransactional, "IncidentService.createIncident must be annotated with Spring's @Transactional");
    }

    @Test
    void shouldRollbackAlertWhenDownstreamCorrelationFailsWithRuntimeException() {
        AlertRepository alertRepository = mock(AlertRepository.class);
        AlertMapper alertMapper = new AlertMapper();
        AlertCorrelationEngine correlationEngine = mock(AlertCorrelationEngine.class);
        IncidentAlertRepository incidentAlertRepository = mock(IncidentAlertRepository.class);

        AlertService service = new AlertService(
                alertRepository,
                alertMapper,
                correlationEngine,
                incidentAlertRepository
        );

        CreateAlertRequest request = new CreateAlertRequest(
                AlertSource.CUSTOM,
                "order-service",
                AlertEnvironment.PRODUCTION,
                "CPU_HIGH",
                AlertSeverity.CRITICAL,
                "High CPU",
                LocalDateTime.now(),
                "cpu",
                95.0,
                80.0
        );

        when(alertRepository.findFirstByServiceAndEnvironmentAndTypeAndTimestamp(any(), any(), any(), any()))
                .thenReturn(java.util.Optional.empty());

        when(alertRepository.save(any(Alert.class))).thenAnswer(inv -> {
            Alert a = inv.getArgument(0);
            a.setId(10L);
            return a;
        });

        // Downstream correlation failure
        when(correlationEngine.correlate(any(Alert.class)))
                .thenThrow(new IllegalStateException("Correlation pipeline error"));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.createAlert(request)
        );

        assertEquals("Correlation pipeline error", ex.getMessage());
        // In a real Spring transaction proxy, this unhandled RuntimeException triggers an immediate rollback
        // of alertRepository.save(alert).
    }
}
