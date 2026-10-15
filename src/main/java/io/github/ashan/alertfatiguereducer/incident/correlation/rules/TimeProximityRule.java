package io.github.ashan.alertfatiguereducer.incident.correlation.rules;

import io.github.ashan.alertfatiguereducer.incident.correlation.CorrelationContext;
import io.github.ashan.alertfatiguereducer.incident.correlation.CorrelationRule;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;


@Component
public class TimeProximityRule implements CorrelationRule {

    private static final double MAX_SCORE = 0.25;
    private static final double HALF_LIFE_MINUTES = 5.0;

    @Override
    public double evaluate(CorrelationContext context) {
        var alert = context.alert();
        var incident = context.incident();

        LocalDateTime alertTimestamp = alert.getTimestamp();
        LocalDateTime incidentStartedAt = incident.getStartedAt();

        if (alertTimestamp == null || incidentStartedAt == null) {
            return 0.0;
        }

        double elapsedMinutes = Math.abs(
                Duration.between(
                        incidentStartedAt,
                        alertTimestamp
                ).toMillis() / 60_000.0
        );

        double decayRate = Math.log(2.0) / HALF_LIFE_MINUTES;

        return MAX_SCORE * Math.exp(-decayRate * elapsedMinutes);
    }
}
