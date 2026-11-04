package io.github.ashan.alertfatiguereducer.alert.dto.response;

import io.github.ashan.alertfatiguereducer.incident.correlation.CorrelationAction;
import io.github.ashan.alertfatiguereducer.incident.correlation.RuleEvaluation;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

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
        double correlationScore,

        @Schema(description = "Rule-by-rule evaluation breakdown explaining why the incident was selected")
        List<RuleEvaluation> explanations
) {
    public AlertCorrelationResponse(
            AlertResponse alert,
            CorrelationAction correlationAction,
            Long incidentId,
            double correlationScore
    ) {
        this(alert, correlationAction, incidentId, correlationScore, List.of());
    }
}
