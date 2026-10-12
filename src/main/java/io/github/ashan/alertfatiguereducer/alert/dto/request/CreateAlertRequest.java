
package io.github.ashan.alertfatiguereducer.alert.dto.request;

import io.github.ashan.alertfatiguereducer.alert.entity.AlertEnvironment;
import io.github.ashan.alertfatiguereducer.alert.entity.AlertSeverity;
import io.github.ashan.alertfatiguereducer.alert.entity.AlertSource;
import io.github.ashan.alertfatiguereducer.alert.validation.FiniteNumber;
import io.github.ashan.alertfatiguereducer.alert.validation.PastOrPresentTimestamp;
import io.github.ashan.alertfatiguereducer.alert.validation.SupportedAlertType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

@Schema(
        name = "CreateAlertRequest",
        description = "Monitoring alert submitted for correlation with an incident"
)
public record CreateAlertRequest(

        @Schema(
                description = "Origin of the alert",
                example = "CUSTOM",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull
        AlertSource source,

        @Schema(
                description = "Name of the affected service",
                example = "payment-service",
                maxLength = 100,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank
        @Size(max = 100)
        String service,

        @Schema(
                description = "Environment where the alert occurred",
                example = "PRODUCTION",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull
        AlertEnvironment environment,

        @Schema(
                description = "Supported alert type",
                example = "CPU_HIGH",
                allowableValues = {
                        "CPU_HIGH",
                        "LATENCY_HIGH",
                        "ERROR_RATE_HIGH",
                        "TIMEOUT"
                },
                maxLength = 100,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank
        @Size(max = 100)
        @SupportedAlertType
        String type,

        @Schema(
                description = "Severity reported by the alert source",
                example = "CRITICAL",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull
        AlertSeverity severity,

        @Schema(
                description = "Human-readable alert details",
                example = "CPU usage exceeded the configured threshold",
                maxLength = 2000
        )
        @Size(max = 2000)
        String message,

        @Schema(
                description = "Time when the alert occurred; future timestamps are not accepted",
                example = "2026-10-08T10:30:00",
                type = "string",
                format = "date-time",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull
        @PastOrPresentTimestamp
        LocalDateTime timestamp,

        @Schema(
                description = "Metric associated with the alert, if applicable",
                example = "cpu_usage_percent",
                maxLength = 100
        )
        @Size(max = 100)
        String metric,

        @Schema(
                description = "Observed metric value; must be a finite number when provided",
                example = "95.5"
        )
        @FiniteNumber
        Double value,

        @Schema(
                description = "Threshold that triggered the alert; must be a finite number when provided",
                example = "90.0"
        )
        @FiniteNumber
        Double threshold
) {
}
