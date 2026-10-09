
package io.github.ashan.alertfatiguereducer.alert.dto.response;

import io.github.ashan.alertfatiguereducer.incident.correlation.CorrelationAction;

public record AlertCorrelationResponse(
        AlertResponse alert,
        CorrelationAction correlationAction,
        Long incidentId,
        double correlationScore
) {
}