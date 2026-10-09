package io.github.ashan.alertfatiguereducer.incident.correlation;

public interface CorrelationRule {

    double evaluate(CorrelationContext context);
}