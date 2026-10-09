package io.github.ashan.alertfatiguereducer.incident.service;

import io.github.ashan.alertfatiguereducer.alert.entity.Alert;
import io.github.ashan.alertfatiguereducer.alert.entity.AlertSeverity;
import io.github.ashan.alertfatiguereducer.alert.repository.AlertRepository;
import io.github.ashan.alertfatiguereducer.incident.entity.Incident;
import io.github.ashan.alertfatiguereducer.incident.entity.IncidentAlert;
import io.github.ashan.alertfatiguereducer.incident.entity.IncidentSeverity;
import io.github.ashan.alertfatiguereducer.incident.repository.IncidentAlertRepository;
import io.github.ashan.alertfatiguereducer.incident.repository.IncidentRepository;
import io.github.ashan.alertfatiguereducer.shared.exception.AlertAlreadyAssociatedException;
import io.github.ashan.alertfatiguereducer.shared.exception.AlertNotFoundException;
import io.github.ashan.alertfatiguereducer.shared.exception.IncidentNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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
        if (!Double.isFinite(correlationScore)
                || correlationScore < 0.0
                || correlationScore > 1.0) {
            throw new IllegalArgumentException(
                    "Correlation score must be between 0.0 and 1.0"
            );
        }

        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IncidentNotFoundException(incidentId));

        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new AlertNotFoundException(alertId));

        if (incidentAlertRepository.existsByAlertId(alertId)) {
            throw new AlertAlreadyAssociatedException(alertId);
        }

        IncidentAlert association = new IncidentAlert(
                incidentId,
                alertId,
                correlationScore
        );

        IncidentAlert savedAssociation =
                incidentAlertRepository.save(association);

        incident.setLastUpdatedAt(LocalDateTime.now());

        if (severityRank(alert.getSeverity())
                > severityRank(incident.getSeverity())) {
            incident.setSeverity(
                    IncidentSeverity.valueOf(alert.getSeverity().name())
            );
        }

        incidentRepository.save(incident);

        return savedAssociation;
    }


    private int severityRank(AlertSeverity severity) {
        return switch (severity) {
            case INFO -> 1;
            case WARNING -> 2;
            case CRITICAL -> 3;
        };
    }

    private int severityRank(IncidentSeverity severity) {
        return switch (severity) {
            case INFO -> 1;
            case WARNING -> 2;
            case CRITICAL -> 3;
        };
    }

    @Transactional(readOnly = true)
    public List<Alert> findAlertsByIncidentId(Long incidentId) {
        return incidentAlertRepository.findByIncidentId(incidentId)
                .stream()
                .map(IncidentAlert::getAlert)
                .toList();
    }
}