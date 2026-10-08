package io.github.ashan.alertfatiguereducer.alert.dto.response;


import io.github.ashan.alertfatiguereducer.alert.entity.AlertEnvironment;
import io.github.ashan.alertfatiguereducer.alert.entity.AlertSeverity;
import io.github.ashan.alertfatiguereducer.alert.entity.AlertSource;

import java.time.LocalDateTime;

public record AlertResponse(
        Long id,
        AlertSource source,
        String service,
        AlertEnvironment environment,
        String type,
        AlertSeverity severity,
        String message,
        LocalDateTime timestamp,
        String metric,
        Double value,
        Double threshold,
        LocalDateTime createdAt
) {
}