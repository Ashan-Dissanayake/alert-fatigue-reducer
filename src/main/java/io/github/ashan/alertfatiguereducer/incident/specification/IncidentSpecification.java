package io.github.ashan.alertfatiguereducer.incident.specification;

import io.github.ashan.alertfatiguereducer.alert.entity.AlertEnvironment;
import io.github.ashan.alertfatiguereducer.incident.entity.Incident;
import io.github.ashan.alertfatiguereducer.incident.entity.IncidentStatus;
import org.springframework.data.jpa.domain.Specification;

public final class IncidentSpecification {

    private IncidentSpecification() {
    }

    public static Specification<Incident> hasStatus(IncidentStatus status) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<Incident> hasEnvironment(
            AlertEnvironment environment) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("environment"), environment);
    }
}