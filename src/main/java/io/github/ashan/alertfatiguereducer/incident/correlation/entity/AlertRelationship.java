
package io.github.ashan.alertfatiguereducer.incident.correlation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "alert_relationships",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_alert_relationships_types",
                        columnNames = {"source_type", "related_type"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class AlertRelationship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_type", nullable = false, length = 100)
    private String sourceType;

    @Column(name = "related_type", nullable = false, length = 100)
    private String relatedType;
}
