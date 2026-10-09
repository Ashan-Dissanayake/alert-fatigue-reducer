package io.github.ashan.alertfatiguereducer.incident.entity;

import io.github.ashan.alertfatiguereducer.alert.entity.AlertEnvironment;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "incidents",
        indexes = {
                @Index(
                        name = "idx_incidents_status_environment",
                        columnList = "status, environment"
                ),
                @Index(
                        name = "idx_incidents_service_environment",
                        columnList = "service, environment"
                ),
                @Index(
                        name = "idx_incidents_last_updated_at",
                        columnList = "last_updated_at"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 100)
    private String service;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AlertEnvironment environment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private IncidentSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private IncidentStatus status;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "last_updated_at", nullable = false)
    private LocalDateTime lastUpdatedAt;

    @Column(name = "correlation_score")
    private Double correlationScore;

}