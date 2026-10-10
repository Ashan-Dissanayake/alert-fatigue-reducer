package io.github.ashan.alertfatiguereducer.incident.correlation.rules;

import io.github.ashan.alertfatiguereducer.incident.correlation.CorrelationContext;
import io.github.ashan.alertfatiguereducer.incident.correlation.CorrelationRule;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class TimeProximityRule implements CorrelationRule {

    private static final double WITHIN_TWO_MINUTES = 0.25;
    private static final double WITHIN_FIVE_MINUTES = 0.20;
    private static final double WITHIN_TEN_MINUTES = 0.10;

    @Override
    public double evaluate(CorrelationContext context) {
        var alert = context.alert();
        var incident = context.incident();

        LocalDateTime alertTimestamp = alert.getTimestamp();
        LocalDateTime incidentStartedAt = incident.getStartedAt();

        if (alertTimestamp == null || incidentStartedAt == null) {
            return 0.0;
        }

        long minutes = Math.abs(
                Duration.between(incidentStartedAt, alertTimestamp).toMinutes()
        );

        if (minutes <= 2) {
            return WITHIN_TWO_MINUTES;
        }

        if (minutes <= 5) {
            return WITHIN_FIVE_MINUTES;
        }

        if (minutes <= 10) {
            return WITHIN_TEN_MINUTES;
        }

        return 0.0;
    }
}