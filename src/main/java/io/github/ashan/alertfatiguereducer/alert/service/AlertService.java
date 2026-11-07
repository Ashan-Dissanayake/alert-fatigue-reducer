package io.github.ashan.alertfatiguereducer.alert.service;

import io.github.ashan.alertfatiguereducer.alert.dto.request.CreateAlertRequest;
import io.github.ashan.alertfatiguereducer.alert.dto.response.AlertCorrelationResponse;
import io.github.ashan.alertfatiguereducer.alert.dto.response.AlertResponse;
import io.github.ashan.alertfatiguereducer.alert.entity.Alert;
import io.github.ashan.alertfatiguereducer.alert.mapper.AlertMapper;
import io.github.ashan.alertfatiguereducer.alert.repository.AlertRepository;
import io.github.ashan.alertfatiguereducer.incident.correlation.AlertCorrelationEngine;
import io.github.ashan.alertfatiguereducer.incident.correlation.CorrelationAction;
import io.github.ashan.alertfatiguereducer.incident.correlation.CorrelationResult;
import io.github.ashan.alertfatiguereducer.incident.correlation.RuleEvaluation;
import io.github.ashan.alertfatiguereducer.incident.entity.IncidentAlert;
import io.github.ashan.alertfatiguereducer.incident.repository.IncidentAlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;
    private final AlertMapper alertMapper;
    private final AlertCorrelationEngine alertCorrelationEngine;
    private final IncidentAlertRepository incidentAlertRepository;

    @Transactional
    public AlertCorrelationResponse createAlert(CreateAlertRequest request) {

        // Idempotency check: deduplicate duplicate alerts on identical service, environment, type, timestamp
        Optional<Alert> existingAlertOpt = alertRepository.findFirstByServiceAndEnvironmentAndTypeAndTimestamp(
                request.service(),
                request.environment(),
                request.type(),
                request.timestamp()
        );

        if (existingAlertOpt.isPresent()) {
            Alert existingAlert = existingAlertOpt.get();
            AlertResponse alertResponse = alertMapper.toResponse(existingAlert);

            // Lookup existing incident association if already attached
            Optional<IncidentAlert> incidentAlertOpt = incidentAlertRepository.findByAlertId(existingAlert.getId());

            Long incidentId = incidentAlertOpt.map(IncidentAlert::getIncidentId).orElse(null);
            double score = incidentAlertOpt.map(IncidentAlert::getCorrelationScore).orElse(0.0);

            return new AlertCorrelationResponse(
                    alertResponse,
                    CorrelationAction.ATTACHED_TO_INCIDENT,
                    incidentId,
                    score,
                    List.of(new RuleEvaluation(
                            "IdempotentDeduplication",
                            score,
                            1.0,
                            true,
                            "Alert already processed with identical service, environment, type, and timestamp"
                    )),
                    "v1.0"
            );
        }

        Alert alert = alertMapper.toEntity(request);
        alert.setCreatedAt(LocalDateTime.now());

        Alert savedAlert = alertRepository.save(alert);

        CorrelationResult correlationResult =
                alertCorrelationEngine.correlate(savedAlert);

        AlertResponse alertResponse =
                alertMapper.toResponse(savedAlert);

        return new AlertCorrelationResponse(
                alertResponse,
                correlationResult.action(),
                correlationResult.incidentId(),
                correlationResult.score(),
                correlationResult.explanations(),
                correlationResult.policyVersion()
        );
    }
}
