package io.github.ashan.alertfatiguereducer.incident.correlation;

import java.util.List;

public record CorrelationResult(
        CorrelationAction action,
        Long incidentId,
        double score,
        List<RuleEvaluation> explanations
) {
    public CorrelationResult(CorrelationAction action, Long incidentId, double score) {
        this(action, incidentId, score, List.of());
    }
}
