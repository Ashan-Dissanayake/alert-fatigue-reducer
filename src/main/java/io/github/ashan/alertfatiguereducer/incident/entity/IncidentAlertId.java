package io.github.ashan.alertfatiguereducer.incident.entity;

import java.io.Serializable;
import java.util.Objects;

public class IncidentAlertId implements Serializable {

    private Long incidentId;
    private Long alertId;

    public IncidentAlertId() {
    }

    public IncidentAlertId(Long incidentId, Long alertId) {
        this.incidentId = incidentId;
        this.alertId = alertId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof IncidentAlertId that)) {
            return false;
        }

        return Objects.equals(incidentId, that.incidentId)
                && Objects.equals(alertId, that.alertId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(incidentId, alertId);
    }
}