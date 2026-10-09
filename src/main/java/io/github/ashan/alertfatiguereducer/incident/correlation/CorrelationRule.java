package io.github.ashan.alertfatiguereducer.incident.correlation;

import io.github.ashan.alertfatiguereducer.alert.entity.Alert;
import io.github.ashan.alertfatiguereducer.incident.entity.Incident;

public interface CorrelationRule {

    double evaluate(Alert alert, Incident incident);
}