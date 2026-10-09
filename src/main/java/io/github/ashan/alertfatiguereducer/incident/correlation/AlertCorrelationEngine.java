package io.github.ashan.alertfatiguereducer.incident.correlation;

import io.github.ashan.alertfatiguereducer.alert.entity.Alert;

public interface AlertCorrelationEngine {

    CorrelationResult correlate(Alert alert);
}