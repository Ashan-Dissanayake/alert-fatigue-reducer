package io.github.ashan.alertfatiguereducer.shared.exception;

public class AlertNotFoundException extends RuntimeException {

    public AlertNotFoundException(Long alertId) {
        super("Alert not found: " + alertId);
    }
}
