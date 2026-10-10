package io.github.ashan.alertfatiguereducer.incident.service;

import io.github.ashan.alertfatiguereducer.incident.dto.response.IncidentResponse;
import io.github.ashan.alertfatiguereducer.incident.entity.Incident;
import io.github.ashan.alertfatiguereducer.incident.entity.IncidentStatus;
import io.github.ashan.alertfatiguereducer.incident.mapper.IncidentMapper;
import io.github.ashan.alertfatiguereducer.incident.repository.IncidentRepository;
import io.github.ashan.alertfatiguereducer.shared.exception.IncidentNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final IncidentMapper incidentMapper;



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
    public IncidentResponse getIncidentById(Long incidentId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IncidentNotFoundException(incidentId));

        return incidentMapper.toResponse(incident);
    }

    @Transactional(readOnly = true)
    public List<IncidentResponse> getAllIncidents() {
        return incidentRepository.findAll()
                .stream()
                .map(incidentMapper::toResponse)
                .toList();
    }
}