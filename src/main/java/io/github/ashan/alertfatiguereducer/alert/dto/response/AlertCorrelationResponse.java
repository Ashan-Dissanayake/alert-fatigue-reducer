package io.github.ashan.alertfatiguereducer.alert.dto.response;

import io.github.ashan.alertfatiguereducer.incident.correlation.CorrelationAction;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "AlertCorrelationResponse",
        description = "Alert creation result and its incident correlation outcome"
)
public record AlertCorrelationResponse(

        @Schema(description = "Details of the accepted alert")
        AlertResponse alert,

        @Schema(
                description = "Action taken by the correlation engine",
                example = "ATTACHED_TO_INCIDENT"
        )
        CorrelationAction correlationAction,

        @Schema(
                description = "ID of the incident associated with this alert",
                example = "42"
        )
        Long incidentId,

        @Schema(
                description = "Correlation score for the selected incident",
                example = "0.85",
                minimum = "0.0",
                maximum = "1.0"
        )
        double correlationScore
) {
}