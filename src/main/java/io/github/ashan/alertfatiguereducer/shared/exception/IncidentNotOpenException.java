package io.github.ashan.alertfatiguereducer.shared.exception;

public class IncidentNotOpenException extends RuntimeException {

    public IncidentNotOpenException(Long incidentId) {
        super("Incident with ID " + incidentId
                + " is not open and cannot accept new alerts");
    }
}