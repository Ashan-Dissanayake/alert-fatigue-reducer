package io.github.ashan.alertfatiguereducer.shared.exception;

public class AlertAlreadyAssociatedException extends RuntimeException {

    public AlertAlreadyAssociatedException(Long alertId) {
        super("Alert is already associated with an incident: " + alertId);
    }
}