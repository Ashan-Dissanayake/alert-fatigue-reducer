package io.github.ashan.alertfatiguereducer.incident.correlation.rules;

import io.github.ashan.alertfatiguereducer.incident.correlation.CorrelationContext;
import io.github.ashan.alertfatiguereducer.incident.correlation.CorrelationProperties;
import io.github.ashan.alertfatiguereducer.incident.correlation.CorrelationRule;
import io.github.ashan.alertfatiguereducer.incident.correlation.RuleEvaluation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SameServiceRule implements CorrelationRule {

    private final CorrelationProperties properties;

    @Override
    public RuleEvaluation evaluateExplanation(CorrelationContext context) {
        var alert = context.alert();
        var incident = context.incident();

        if (alert.getService() == null || incident.getService() == null) {
            return new RuleEvaluation(
                    "SameServiceRule",
                    0.0,
                    properties.getSameServiceWeight(),
                    false,
                    "Service name is missing from alert or incident"
            );
        }

        boolean matched = alert.getService().equals(incident.getService());
        double score = matched ? properties.getSameServiceWeight() : 0.0;
        String reason = matched
                ? "Services match: " + alert.getService()
                : "Services do not match: alert service is '" + alert.getService() + "' while incident service is '" + incident.getService() + "'";

        return new RuleEvaluation(
                "SameServiceRule",
                score,
                properties.getSameServiceWeight(),
                matched,
                reason
        );
    }

    @Override
    public double evaluate(CorrelationContext context) {
        return evaluateExplanation(context).score();
    }
}
