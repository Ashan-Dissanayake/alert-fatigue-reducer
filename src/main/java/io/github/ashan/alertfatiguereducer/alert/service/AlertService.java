package io.github.ashan.alertfatiguereducer.alert.service;

import io.github.ashan.alertfatiguereducer.alert.dto.request.CreateAlertRequest;
import io.github.ashan.alertfatiguereducer.alert.dto.response.AlertCorrelationResponse;
import io.github.ashan.alertfatiguereducer.alert.dto.response.AlertResponse;
import io.github.ashan.alertfatiguereducer.alert.entity.Alert;
import io.github.ashan.alertfatiguereducer.alert.mapper.AlertMapper;
import io.github.ashan.alertfatiguereducer.alert.repository.AlertRepository;
import io.github.ashan.alertfatiguereducer.incident.correlation.AlertCorrelationEngine;
import io.github.ashan.alertfatiguereducer.incident.correlation.CorrelationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;
    private final AlertMapper alertMapper;
    private final AlertCorrelationEngine alertCorrelationEngine;

    @Transactional
    public AlertCorrelationResponse createAlert(CreateAlertRequest request) {

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
                correlationResult.score()
        );
    }
}