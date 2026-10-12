package io.github.ashan.alertfatiguereducer.incident.dto.response;

import io.github.ashan.alertfatiguereducer.alert.entity.AlertEnvironment;
import io.github.ashan.alertfatiguereducer.incident.entity.IncidentSeverity;
import io.github.ashan.alertfatiguereducer.incident.entity.IncidentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(
        name = "IncidentResponse",
        description = "Incident details and its current correlation state"
)
public record IncidentResponse(

        @Schema(description = "Unique incident identifier", example = "42")
        Long id,

        @Schema(description = "Generated incident title", example = "CPU_HIGH on payment-service")
        String title,

        @Schema(description = "Affected service", example = "payment-service")
        String service,

        @Schema(description = "Environment associated with the incident", example = "PRODUCTION")
        AlertEnvironment environment,

        @Schema(description = "Current incident severity", example = "CRITICAL")
        IncidentSeverity severity,

        @Schema(description = "Incident lifecycle status", example = "OPEN")
        IncidentStatus status,

        @Schema(
                description = "Time when the incident started",
                example = "2026-10-08T10:30:00",
                type = "string",
                format = "date-time"
        )
        LocalDateTime startedAt,

        @Schema(
                description = "Time when the incident was last updated",
                example = "2026-10-08T10:35:00",
                type = "string",
                format = "date-time"
        )
        LocalDateTime lastUpdatedAt,

        @Schema(
                description = "Correlation score associated with the incident",
                example = "0.85",
                minimum = "0.0",
                maximum = "1.0"
        )
        Double correlationScore
) {
}