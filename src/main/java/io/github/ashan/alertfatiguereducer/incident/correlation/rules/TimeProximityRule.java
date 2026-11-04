package io.github.ashan.alertfatiguereducer.incident.correlation.rules;

import io.github.ashan.alertfatiguereducer.alert.entity.Alert;
import io.github.ashan.alertfatiguereducer.incident.correlation.CorrelationContext;
import io.github.ashan.alertfatiguereducer.incident.correlation.CorrelationProperties;
import io.github.ashan.alertfatiguereducer.incident.correlation.CorrelationRule;
import io.github.ashan.alertfatiguereducer.incident.correlation.RuleEvaluation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class TimeProximityRule implements CorrelationRule {

    private final CorrelationProperties properties;

    @Override
    public RuleEvaluation evaluateExplanation(CorrelationContext context) {
        Alert alert = context.alert();

        if (alert.getTimestamp() == null) {
            return new RuleEvaluation(
                    "TimeProximityRule",
                    0.0,
                    properties.getTimeProximityMaxScore(),
                    false,
                    "Alert timestamp is null"
            );
        }

        LocalDateTime referenceTimestamp =
                context.relatedAlerts().stream()
                        .map(Alert::getTimestamp)
                        .filter(Objects::nonNull)
                        .max(LocalDateTime::compareTo)
                        .orElse(context.incident().getStartedAt());

        if (referenceTimestamp == null) {
            return new RuleEvaluation(
                    "TimeProximityRule",
                    0.0,
                    properties.getTimeProximityMaxScore(),
                    false,
                    "Reference timestamp for incident is null"
            );
        }

        // Out-of-order alerts receive no temporal correlation score.
        if (alert.getTimestamp().isBefore(referenceTimestamp)) {
            return new RuleEvaluation(
                    "TimeProximityRule",
                    0.0,
                    properties.getTimeProximityMaxScore(),
                    false,
                    "Alert timestamp is before reference timestamp (" + referenceTimestamp + "); out-of-order alert"
            );
        }

        double elapsedMinutes = Duration.between(
                referenceTimestamp,
                alert.getTimestamp()
        ).toMillis() / 60_000.0;

        double decayRate = Math.log(2.0) / properties.getTimeProximityHalfLifeMinutes();
        double score = properties.getTimeProximityMaxScore() * Math.exp(-decayRate * elapsedMinutes);

        return new RuleEvaluation(
                "TimeProximityRule",
                score,
                properties.getTimeProximityMaxScore(),
                true,
                String.format("Alert occurred %.2f minutes after reference timestamp (temporal score: %.4f)", elapsedMinutes, score)
        );
    }

    @Override
    public double evaluate(CorrelationContext context) {
        return evaluateExplanation(context).score();
    }
}
