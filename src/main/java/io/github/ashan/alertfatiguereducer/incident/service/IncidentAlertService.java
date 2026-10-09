package io.github.ashan.alertfatiguereducer.incident.service;

import io.github.ashan.alertfatiguereducer.incident.entity.IncidentAlert;
import io.github.ashan.alertfatiguereducer.incident.repository.IncidentAlertRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IncidentAlertService {

    private final IncidentAlertRepository incidentAlertRepository;

    public IncidentAlertService(
            IncidentAlertRepository incidentAlertRepository
    ) {
        this.incidentAlertRepository = incidentAlertRepository;
    }

    @Transactional
    public IncidentAlert attachAlert(
            Long incidentId,
            Long alertId,
            double correlationScore
    ) {
        if (correlationScore < 0.0 || correlationScore > 1.0) {
            throw new IllegalArgumentException(
                    "Correlation score must be between 0.0 and 1.0"
            );
        }

        if (incidentAlertRepository.existsByAlertId(alertId)) {
            throw new IllegalStateException(
                    "Alert is already associated with an incident: " + alertId
            );
        }

        IncidentAlert association = new IncidentAlert(
                incidentId,
                alertId,
                correlationScore
        );

        return incidentAlertRepository.save(association);
    }
}