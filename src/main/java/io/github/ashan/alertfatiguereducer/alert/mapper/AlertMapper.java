package io.github.ashan.alertfatiguereducer.alert.mapper;

import io.github.ashan.alertfatiguereducer.alert.dto.request.CreateAlertRequest;
import io.github.ashan.alertfatiguereducer.alert.dto.response.AlertResponse;
import io.github.ashan.alertfatiguereducer.alert.entity.Alert;
import org.springframework.stereotype.Component;

@Component
public class AlertMapper {

    public Alert toEntity(CreateAlertRequest request) {
        Alert alert = new Alert();

        alert.setSource(request.source());
        alert.setService(request.service());
        alert.setEnvironment(request.environment());
        alert.setType(request.type());
        alert.setSeverity(request.severity());
        alert.setMessage(request.message());
        alert.setTimestamp(request.timestamp());
        alert.setMetric(request.metric());
        alert.setValue(request.value());
        alert.setThreshold(request.threshold());

        return alert;
    }

    public AlertResponse toResponse(Alert alert) {
        return new AlertResponse(
                alert.getId(),
                alert.getSource(),
                alert.getService(),
                alert.getEnvironment(),
                alert.getType(),
                alert.getSeverity(),
                alert.getMessage(),
                alert.getTimestamp(),
                alert.getMetric(),
                alert.getValue(),
                alert.getThreshold(),
                alert.getCreatedAt()
        );
    }
}