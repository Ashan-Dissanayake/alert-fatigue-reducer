package io.github.ashan.alertfatiguereducer.incident.correlation;

import io.github.ashan.alertfatiguereducer.alert.entity.Alert;
import io.github.ashan.alertfatiguereducer.incident.entity.Incident;
import io.github.ashan.alertfatiguereducer.incident.entity.IncidentSeverity;
import io.github.ashan.alertfatiguereducer.incident.entity.IncidentStatus;
import io.github.ashan.alertfatiguereducer.incident.repository.IncidentRepository;
import io.github.ashan.alertfatiguereducer.incident.service.CreateIncidentCommand;
import io.github.ashan.alertfatiguereducer.incident.service.IncidentAlertService;
import io.github.ashan.alertfatiguereducer.incident.service.IncidentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DefaultAlertCorrelationEngine implements AlertCorrelationEngine {

    private static final double CORRELATION_THRESHOLD = 0.70;
    private static final long CORRELATION_WINDOW_MINUTES = 10;

    private final IncidentRepository incidentRepository;
    private final IncidentAlertService incidentAlertService;
    private final IncidentService incidentService;
    private final List<CorrelationRule> correlationRules;


    @Override
    @Transactional
    public CorrelationResult correlate(Alert alert) {
        LocalDateTime cutoff = alert.getTimestamp()
                .minusMinutes(CORRELATION_WINDOW_MINUTES);

        List<Incident> candidates =
                incidentRepository.findByStatusAndEnvironmentAndLastUpdatedAtAfter(
                        IncidentStatus.OPEN,
                        alert.getEnvironment(),
                        cutoff
                );

        return candidates.stream()
                .map(incident -> {
                    List<Alert> relatedAlerts =
                            incidentAlertService.findAlertsByIncidentId(
                                    incident.getId()
                            );

                    CorrelationContext context = new CorrelationContext(
                            alert,
                            incident,
                            relatedAlerts
                    );

                    double score = correlationRules.stream()
                            .mapToDouble(rule -> rule.evaluate(context))
                            .sum();

                    return new CorrelationResult(
                            CorrelationAction.ATTACHED_TO_INCIDENT,
                            incident.getId(),
                            score
                    );
                })
                .filter(result ->
                        result.score() >= CORRELATION_THRESHOLD
                )
                .max(Comparator.comparingDouble(CorrelationResult::score))
                .map(result -> {
                    incidentAlertService.attachAlert(
                            result.incidentId(),
                            alert.getId(),
                            result.score()
                    );

                    return result;
                })


                .orElseGet(() -> {
                    Incident incident = incidentService.createIncident(
                            new CreateIncidentCommand(
                                    alert.getType() + " - " + alert.getService(),
                                    alert.getService(),
                                    alert.getEnvironment(),
                                    IncidentSeverity.valueOf(
                                            alert.getSeverity().name()
                                    ),
                                    alert.getTimestamp(),
                                    null
                            )
                    );

                    incidentAlertService.attachAlert(
                            incident.getId(),
                            alert.getId(),
                            0.0
                    );

                    return new CorrelationResult(
                            CorrelationAction.NEW_INCIDENT,
                            null,
                            0.0
                    );
                });
    }
}