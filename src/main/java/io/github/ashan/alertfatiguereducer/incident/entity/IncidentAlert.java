package io.github.ashan.alertfatiguereducer.incident.entity;

import io.github.ashan.alertfatiguereducer.alert.entity.Alert;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "incident_alerts")
@IdClass(IncidentAlertId.class)
@Setter
@Getter
@NoArgsConstructor
public class IncidentAlert {

    @Id
    @Column(name = "incident_id", nullable = false)
    private Long incidentId;

    @Id
    @Column(name = "alert_id", nullable = false)
    private Long alertId;

    @Column(name = "correlation_score")
    private Double correlationScore;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "incident_id",
            insertable = false,
            updatable = false
    )
    private Incident incident;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "alert_id",
            insertable = false,
            updatable = false
    )
    private Alert alert;

    public IncidentAlert(
            Long incidentId,
            Long alertId,
            Double correlationScore
    ) {
        this.incidentId = incidentId;
        this.alertId = alertId;
        this.correlationScore = correlationScore;
    }

}