package io.github.ashan.alertfatiguereducer.incident.dto.response;

import io.github.ashan.alertfatiguereducer.alert.entity.AlertEnvironment;
import io.github.ashan.alertfatiguereducer.incident.entity.IncidentSeverity;
import io.github.ashan.alertfatiguereducer.incident.entity.IncidentStatus;

import java.time.LocalDateTime;

public record IncidentResponse(
        Long id,
        String title,
        String service,
        AlertEnvironment environment,
        IncidentSeverity severity,
        IncidentStatus status,
        LocalDateTime startedAt,
        LocalDateTime lastUpdatedAt,
        Double correlationScore
) {
}