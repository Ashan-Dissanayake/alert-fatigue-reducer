
package io.github.ashan.alertfatiguereducer.incident.correlation.rules;

import io.github.ashan.alertfatiguereducer.alert.entity.Alert;
import io.github.ashan.alertfatiguereducer.incident.correlation.CorrelationContext;
import io.github.ashan.alertfatiguereducer.incident.correlation.CorrelationRule;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

import org.springframework.stereotype.Component;

@Component
public class TimeProximityRule implements CorrelationRule {

    private static final double MAX_SCORE = 0.25;
    private static final double HALF_LIFE_MINUTES = 5.0;

    @Override
    public double evaluate(CorrelationContext context) {
        Alert alert = context.alert();

        if (alert.getTimestamp() == null) {
            return 0.0;
        }

        LocalDateTime referenceTimestamp =
                context.relatedAlerts().stream()
                        .map(Alert::getTimestamp)
                        .filter(Objects::nonNull)
                        .max(LocalDateTime::compareTo)
                        .orElse(context.incident().getStartedAt());

        if (referenceTimestamp == null) {
            return 0.0;
        }

        // Out-of-order alerts receive no temporal correlation score.
        if (alert.getTimestamp().isBefore(referenceTimestamp)) {
            return 0.0;
        }

        double elapsedMinutes = Duration.between(
                referenceTimestamp,
                alert.getTimestamp()
        ).toMillis() / 60_000.0;

        double decayRate = Math.log(2.0) / HALF_LIFE_MINUTES;

        return MAX_SCORE * Math.exp(-decayRate * elapsedMinutes);
    }
}
