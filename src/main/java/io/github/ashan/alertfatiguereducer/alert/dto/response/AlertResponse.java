package io.github.ashan.alertfatiguereducer.alert.dto.response;

import io.github.ashan.alertfatiguereducer.alert.entity.AlertEnvironment;
import io.github.ashan.alertfatiguereducer.alert.entity.AlertSeverity;
import io.github.ashan.alertfatiguereducer.alert.entity.AlertSource;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(
        name = "AlertResponse",
        description = "Details of a stored monitoring alert"
)
public record AlertResponse(

        @Schema(description = "Unique alert identifier", example = "101")
        Long id,

        @Schema(description = "Source that submitted the alert", example = "CUSTOM")
        AlertSource source,

        @Schema(description = "Name of the affected service", example = "payment-service")
        String service,

        @Schema(description = "Environment where the alert occurred", example = "PRODUCTION")
        AlertEnvironment environment,

        @Schema(description = "Type of monitoring alert", example = "CPU_HIGH")
        String type,

        @Schema(description = "Severity reported by the alert source", example = "CRITICAL")
        AlertSeverity severity,

        @Schema(
                description = "Human-readable alert details",
                example = "CPU usage exceeded 90 percent"
        )
        String message,

        @Schema(
                description = "Time when the alert occurred",
                example = "2026-10-08T10:30:00",
                type = "string",
                format = "date-time"
        )
        LocalDateTime timestamp,

        @Schema(description = "Metric associated with the alert", example = "cpu_usage_percent")
        String metric,

        @Schema(description = "Observed metric value", example = "95.5")
        Double value,

        @Schema(description = "Threshold associated with the alert", example = "90.0")
        Double threshold,

        @Schema(
                description = "Time when the alert was stored",
                example = "2026-10-08T10:30:02",
                type = "string",
                format = "date-time"
        )
        LocalDateTime createdAt
) {
}