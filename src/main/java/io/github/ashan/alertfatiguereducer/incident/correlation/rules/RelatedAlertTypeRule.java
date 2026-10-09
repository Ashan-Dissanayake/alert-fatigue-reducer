package io.github.ashan.alertfatiguereducer.incident.correlation.rules;

import io.github.ashan.alertfatiguereducer.alert.entity.Alert;
import io.github.ashan.alertfatiguereducer.alert.entity.AlertType;
import io.github.ashan.alertfatiguereducer.incident.correlation.CorrelationContext;
import io.github.ashan.alertfatiguereducer.incident.correlation.CorrelationRule;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class RelatedAlertTypeRule implements CorrelationRule {

    private static final double SCORE = 0.30;

    private static final Map<AlertType, Set<AlertType>> RELATED_TYPES = Map.of(
            AlertType.CPU_HIGH, Set.of(AlertType.LATENCY_HIGH),
            AlertType.LATENCY_HIGH, Set.of(
                    AlertType.CPU_HIGH,
                    AlertType.ERROR_RATE_HIGH
            ),
            AlertType.ERROR_RATE_HIGH, Set.of(
                    AlertType.LATENCY_HIGH,
                    AlertType.TIMEOUT
            ),
            AlertType.TIMEOUT, Set.of(AlertType.ERROR_RATE_HIGH)
    );

    @Override
    public double evaluate(CorrelationContext context) {
        Alert incomingAlert = context.alert();

        if (incomingAlert.getType() == null) {
            return 0.0;
        }

        AlertType incomingType;
        try {
            incomingType = AlertType.valueOf(incomingAlert.getType());
        } catch (IllegalArgumentException exception) {
            return 0.0;
        }

        Set<AlertType> relatedTypes = RELATED_TYPES.get(incomingType);

        if (relatedTypes == null) {
            return 0.0;
        }


        return context.relatedAlerts().stream()
                .map(Alert::getType)
                .filter(type -> type != null)
                .anyMatch(type -> {
                    try {
                        return relatedTypes.contains(AlertType.valueOf(type));
                    } catch (IllegalArgumentException exception) {
                        return false;
                    }
                })
                ? SCORE
                : 0.0;
    }
}