package io.github.ashan.alertfatiguereducer.shared.exception;

public class IncidentNotFoundException extends RuntimeException {

    public IncidentNotFoundException(Long incidentId) {
        super("Incident not found: " + incidentId);
    }
}