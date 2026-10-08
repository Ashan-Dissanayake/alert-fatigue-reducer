package io.github.ashan.alertfatiguereducer.alert.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "alerts",
        indexes = {
                @Index(
                        name = "idx_alerts_service_environment",
                        columnList = "service, environment"
                ),
                @Index(
                        name = "idx_alerts_timestamp",
                        columnList = "timestamp"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AlertSource source;

    @Column(nullable = false, length = 100)
    private String service;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AlertEnvironment environment;

    @Column(nullable = false, length = 100)
    private String type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AlertSeverity severity;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(length = 100)
    private String metric;

    private Double value;

    private Double threshold;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

}