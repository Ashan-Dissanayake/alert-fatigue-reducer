package io.github.ashan.alertfatiguereducer.incident.correlation;

import java.util.List;

public record CorrelationResult(
        CorrelationAction action,
        Long incidentId,
        double score,
        List<RuleEvaluation> explanations,
        String policyVersion
) {
    public CorrelationResult(CorrelationAction action, Long incidentId, double score) {
        this(action, incidentId, score, List.of(), "v1.0");
    }

    public CorrelationResult(CorrelationAction action, Long incidentId, double score, List<RuleEvaluation> explanations) {
        this(action, incidentId, score, explanations, "v1.0");
    }
}
