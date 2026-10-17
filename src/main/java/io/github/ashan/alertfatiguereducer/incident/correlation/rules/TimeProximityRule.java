
package io.github.ashan.alertfatiguereducer.incident.correlation.rules;

import io.github.ashan.alertfatiguereducer.alert.entity.Alert;
import io.github.ashan.alertfatiguereducer.incident.correlation.CorrelationContext;
import io.github.ashan.alertfatiguereducer.incident.correlation.CorrelationProperties;
import io.github.ashan.alertfatiguereducer.incident.correlation.CorrelationRule;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TimeProximityRule implements CorrelationRule {

    private final CorrelationProperties properties;

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

        double decayRate = Math.log(2.0) / properties.getTimeProximityHalfLifeMinutes();

        return properties.getTimeProximityMaxScore() * Math.exp(-decayRate * elapsedMinutes);
    }
}
