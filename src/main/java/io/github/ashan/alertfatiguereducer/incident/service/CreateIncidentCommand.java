package io.github.ashan.alertfatiguereducer.incident.service;

import io.github.ashan.alertfatiguereducer.alert.entity.AlertEnvironment;
import io.github.ashan.alertfatiguereducer.incident.entity.IncidentSeverity;

import java.time.LocalDateTime;

public record CreateIncidentCommand(
        String title,
        String service,
        AlertEnvironment environment,
        IncidentSeverity severity,
        LocalDateTime occurredAt,
        Double correlationScore
) {
}