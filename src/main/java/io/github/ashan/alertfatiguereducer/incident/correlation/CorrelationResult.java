package io.github.ashan.alertfatiguereducer.incident.correlation;

public record CorrelationResult(
        CorrelationAction action,
        Long incidentId,
        double score
) {
}