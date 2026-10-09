package io.github.ashan.alertfatiguereducer.incident.service;

import io.github.ashan.alertfatiguereducer.alert.repository.AlertRepository;
import io.github.ashan.alertfatiguereducer.incident.entity.IncidentAlert;
import io.github.ashan.alertfatiguereducer.incident.repository.IncidentAlertRepository;
import io.github.ashan.alertfatiguereducer.incident.repository.IncidentRepository;
import io.github.ashan.alertfatiguereducer.shared.exception.AlertAlreadyAssociatedException;
import io.github.ashan.alertfatiguereducer.shared.exception.AlertNotFoundException;
import io.github.ashan.alertfatiguereducer.shared.exception.IncidentNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IncidentAlertService {

    private final IncidentAlertRepository incidentAlertRepository;
    private final IncidentRepository incidentRepository;
    private final AlertRepository alertRepository;


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

        if (!incidentRepository.existsById(incidentId)) {
            throw new IncidentNotFoundException(incidentId);
        }

        if (!alertRepository.existsById(alertId)) {
            throw new AlertNotFoundException(alertId);
        }

        if (incidentAlertRepository.existsByAlertId(alertId)) {
            throw new AlertAlreadyAssociatedException(alertId);
        }

        IncidentAlert association = new IncidentAlert(
                incidentId,
                alertId,
                correlationScore
        );

        return incidentAlertRepository.save(association);
    }
}