package io.github.ashan.alertfatiguereducer.incident.repository;

import io.github.ashan.alertfatiguereducer.incident.entity.IncidentAlert;
import io.github.ashan.alertfatiguereducer.incident.entity.IncidentAlertId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidentAlertRepository extends JpaRepository<IncidentAlert, IncidentAlertId> {

    List<IncidentAlert> findByIncidentId(Long incidentId);

    boolean existsByAlertId(Long alertId);
}