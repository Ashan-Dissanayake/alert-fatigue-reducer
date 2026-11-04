package io.github.ashan.alertfatiguereducer.incident.correlation.rules;

import io.github.ashan.alertfatiguereducer.incident.correlation.CorrelationContext;
import io.github.ashan.alertfatiguereducer.incident.correlation.CorrelationProperties;
import io.github.ashan.alertfatiguereducer.incident.correlation.CorrelationRule;
import io.github.ashan.alertfatiguereducer.incident.correlation.RuleEvaluation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SameEnvironmentRule implements CorrelationRule {

    private final CorrelationProperties properties;

    @Override
    public RuleEvaluation evaluateExplanation(CorrelationContext context) {
        var alert = context.alert();
        var incident = context.incident();

        if (alert.getEnvironment() == null || incident.getEnvironment() == null) {
            return new RuleEvaluation(
                    "SameEnvironmentRule",
                    0.0,
                    properties.getSameEnvironmentWeight(),
                    false,
                    "Environment is missing from alert or incident"
            );
        }

        boolean matched = alert.getEnvironment() == incident.getEnvironment();
        double score = matched ? properties.getSameEnvironmentWeight() : 0.0;
        String reason = matched
                ? "Environments match: " + alert.getEnvironment()
                : "Environments do not match: alert environment is '" + alert.getEnvironment() + "' while incident environment is '" + incident.getEnvironment() + "'";

        return new RuleEvaluation(
                "SameEnvironmentRule",
                score,
                properties.getSameEnvironmentWeight(),
                matched,
                reason
        );
    }

    @Override
    public double evaluate(CorrelationContext context) {
        return evaluateExplanation(context).score();
    }
}
