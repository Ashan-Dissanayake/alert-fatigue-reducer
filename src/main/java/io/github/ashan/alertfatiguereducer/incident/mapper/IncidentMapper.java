package io.github.ashan.alertfatiguereducer.incident.mapper;

import io.github.ashan.alertfatiguereducer.incident.dto.response.IncidentResponse;
import io.github.ashan.alertfatiguereducer.incident.entity.Incident;
import org.springframework.stereotype.Component;

@Component
public class IncidentMapper {

    public IncidentResponse toResponse(Incident incident) {
        return new IncidentResponse(
                incident.getId(),
                incident.getTitle(),
                incident.getService(),
                incident.getEnvironment(),
                incident.getSeverity(),
                incident.getStatus(),
                incident.getStartedAt(),
                incident.getLastUpdatedAt(),
                incident.getCorrelationScore()
        );
    }
}