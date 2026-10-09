package io.github.ashan.alertfatiguereducer.incident.correlation;

import io.github.ashan.alertfatiguereducer.alert.entity.Alert;
import io.github.ashan.alertfatiguereducer.incident.entity.Incident;

import java.util.List;

public record CorrelationContext(
        Alert alert,
        Incident incident,
        List<Alert> relatedAlerts
) {
    public CorrelationContext {
        relatedAlerts = relatedAlerts == null
                ? List.of()
                : List.copyOf(relatedAlerts);
    }
}