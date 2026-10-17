package io.github.ashan.alertfatiguereducer.integration;

import io.github.ashan.alertfatiguereducer.alert.dto.request.CreateAlertRequest;
import io.github.ashan.alertfatiguereducer.alert.dto.response.AlertCorrelationResponse;
import io.github.ashan.alertfatiguereducer.alert.entity.AlertEnvironment;
import io.github.ashan.alertfatiguereducer.alert.entity.AlertSeverity;
import io.github.ashan.alertfatiguereducer.alert.entity.AlertSource;
import io.github.ashan.alertfatiguereducer.alert.service.AlertService;
import io.github.ashan.alertfatiguereducer.incident.repository.IncidentAlertRepository;
import io.github.ashan.alertfatiguereducer.incident.repository.IncidentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlertCorrelationConcurrencyIntegrationTest
        extends AbstractPostgresIntegrationTest {

    @Autowired
    private AlertService alertService;

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private IncidentAlertRepository incidentAlertRepository;

    @Test
    void shouldCorrelateConcurrentRelatedAlertsIntoOneIncident()
            throws Exception {

        long initialIncidentCount = incidentRepository.count();

        String service = "concurrency-test-service";
        LocalDateTime timestamp = LocalDateTime.now();

        CreateAlertRequest firstRequest = createRequest(
                service, "CPU_HIGH", timestamp
        );

        CreateAlertRequest secondRequest = createRequest(
                service, "LATENCY_HIGH", timestamp
        );

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<AlertCorrelationResponse> firstFuture = executor.submit(() -> {
                ready.countDown();
                awaitStart(start);
                return alertService.createAlert(firstRequest);
            });

            Future<AlertCorrelationResponse> secondFuture = executor.submit(() -> {
                ready.countDown();
                awaitStart(start);
                return alertService.createAlert(secondRequest);
            });

            assertTrue(
                    ready.await(10, TimeUnit.SECONDS),
                    "Both requests should be ready to start"
            );

            start.countDown();

            var firstResponse = firstFuture.get(20, TimeUnit.SECONDS);
            var secondResponse = secondFuture.get(20, TimeUnit.SECONDS);

            assertEquals(
                    firstResponse.incidentId(),
                    secondResponse.incidentId(),
                    "Both related alerts should resolve to the same incident"
            );

            assertEquals(
                    initialIncidentCount + 1,
                    incidentRepository.count(),
                    "Concurrent related alerts should create only one incident"
            );

            assertEquals(
                    2,
                    incidentAlertRepository.findByIncidentId(
                            firstResponse.incidentId()
                    ).size(),
                    "The incident should contain both alerts"
            );
        } finally {
            executor.shutdownNow();
        }
    }

    private CreateAlertRequest createRequest(
            String service,
            String type,
            LocalDateTime timestamp
    ) {
        return new CreateAlertRequest(
                AlertSource.CUSTOM,
                service,
                AlertEnvironment.DEVELOPMENT,
                type,
                AlertSeverity.CRITICAL,
                "Concurrency integration test alert",
                timestamp,
                "concurrency.test",
                95.0,
                80.0
        );
    }

    private void awaitStart(CountDownLatch start) {
        try {
            if (!start.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException(
                        "Timed out waiting for concurrent start"
                );
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Interrupted while waiting to start",
                    exception
            );
        }
    }
}
