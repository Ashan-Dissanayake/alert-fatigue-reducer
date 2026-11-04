package io.github.ashan.alertfatiguereducer.incident.correlation;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "RuleEvaluation",
        description = "Evaluation result and explanation of a correlation rule against an incident"
)
public record RuleEvaluation(

        @Schema(description = "Name of the correlation rule", example = "SameServiceRule")
        String ruleName,

        @Schema(description = "Score contributed by this rule", example = "0.30")
        double score,

        @Schema(description = "Configured weight or maximum score for this rule", example = "0.30")
        double weight,

        @Schema(description = "Whether the rule condition matched", example = "true")
        boolean matched,

        @Schema(
                description = "Human-readable explanation of why this score was awarded",
                example = "Services match: order-service"
        )
        String reason
) {
}
