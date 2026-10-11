package io.github.ashan.alertfatiguereducer.integration;


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
import io.github.ashan.alertfatiguereducer.incident.service.IncidentAlertService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class IncidentAlertPersistenceIntegrationTest
        extends AbstractPostgresIntegrationTest {

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private IncidentAlertRepository incidentAlertRepository;

    @Autowired
    private IncidentAlertService incidentAlertService;

    @Test
    void shouldPersistAssociationAndUpdateIncidentSeverity() {
        LocalDateTime now = LocalDateTime.now().minusMinutes(1);

        Incident incident = new Incident();
        incident.setTitle("CPU_HIGH - payment-service");
        incident.setService("payment-service");
        incident.setEnvironment(AlertEnvironment.DEVELOPMENT);
        incident.setSeverity(IncidentSeverity.WARNING);
        incident.setStatus(IncidentStatus.OPEN);
        incident.setStartedAt(now);
        incident.setLastUpdatedAt(now);

        Incident savedIncident = incidentRepository.saveAndFlush(incident);

        Alert alert = new Alert();
        alert.setSource(AlertSource.CUSTOM);
        alert.setService("payment-service");
        alert.setEnvironment(AlertEnvironment.DEVELOPMENT);
        alert.setType("CPU_HIGH");
        alert.setSeverity(AlertSeverity.CRITICAL);
        alert.setMessage("CPU usage exceeded threshold");
        alert.setTimestamp(now);
        alert.setMetric("cpu.usage");
        alert.setValue(95.0);
        alert.setThreshold(80.0);
        alert.setCreatedAt(LocalDateTime.now());

        Alert savedAlert = alertRepository.saveAndFlush(alert);

        IncidentAlert association = incidentAlertService.attachAlert(
                savedIncident.getId(),
                savedAlert.getId(),
                0.85
        );

        assertNotNull(association);

        IncidentAlert persistedAssociation =
                incidentAlertRepository.findByIncidentId(savedIncident.getId())
                        .stream()
                        .filter(item -> item.getAlertId().equals(savedAlert.getId()))
                        .findFirst()
                        .orElseThrow();

        assertEquals(0.85, persistedAssociation.getCorrelationScore(), 0.000001);

        Incident updatedIncident = incidentRepository
                .findById(savedIncident.getId())
                .orElseThrow();

        assertEquals(IncidentSeverity.CRITICAL, updatedIncident.getSeverity());
        assertNotNull(updatedIncident.getLastUpdatedAt());
        assertTrue(
                updatedIncident.getLastUpdatedAt().compareTo(now) > 0,
                "Incident lastUpdatedAt should advance when an alert is attached"
        );
    }
}
