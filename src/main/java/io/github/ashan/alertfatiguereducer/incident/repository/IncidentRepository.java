package io.github.ashan.alertfatiguereducer.incident.repository;

import io.github.ashan.alertfatiguereducer.incident.entity.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {
}