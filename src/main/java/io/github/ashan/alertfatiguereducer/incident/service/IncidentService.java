package io.github.ashan.alertfatiguereducer.incident.service;

import io.github.ashan.alertfatiguereducer.alert.entity.AlertEnvironment;
import io.github.ashan.alertfatiguereducer.incident.entity.Incident;
import io.github.ashan.alertfatiguereducer.incident.entity.IncidentSeverity;
import io.github.ashan.alertfatiguereducer.incident.entity.IncidentStatus;
import io.github.ashan.alertfatiguereducer.incident.repository.IncidentRepository;
import io.github.ashan.alertfatiguereducer.shared.exception.IncidentNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;

    public IncidentService(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    @Transactional
    public Incident createIncident(CreateIncidentCommand command) {

        Incident incident = new Incident();

        incident.setTitle(command.title());
        incident.setService(command.service());
        incident.setEnvironment(command.environment());
        incident.setSeverity(command.severity());
        incident.setStatus(IncidentStatus.OPEN);
        incident.setStartedAt(command.occurredAt());
        incident.setLastUpdatedAt(command.occurredAt());
        incident.setCorrelationScore(command.correlationScore());

        return incidentRepository.save(incident);
    }

    @Transactional
    public void resolveIncident(Long incidentId) {

        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IncidentNotFoundException(incidentId));

        if (incident.getStatus() == IncidentStatus.RESOLVED) {
            return;
        }

        incident.setStatus(IncidentStatus.RESOLVED);
        incident.setLastUpdatedAt(LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public Incident getIncidentById(Long incidentId) {
        return incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IncidentNotFoundException(incidentId));
    }
}