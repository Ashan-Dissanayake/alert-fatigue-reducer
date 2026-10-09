package io.github.ashan.alertfatiguereducer.incident.repository;

import io.github.ashan.alertfatiguereducer.alert.entity.AlertEnvironment;
import io.github.ashan.alertfatiguereducer.incident.entity.Incident;
import io.github.ashan.alertfatiguereducer.incident.entity.IncidentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {

    List<Incident> findByStatusAndEnvironmentAndLastUpdatedAtAfter(
            IncidentStatus status,
            AlertEnvironment environment,
            LocalDateTime cutoff
    );
}