package io.github.ashan.alertfatiguereducer.incident.correlation.rules;

import io.github.ashan.alertfatiguereducer.alert.entity.Alert;
import io.github.ashan.alertfatiguereducer.incident.correlation.CorrelationContext;
import io.github.ashan.alertfatiguereducer.incident.entity.Incident;
import io.github.ashan.alertfatiguereducer.incident.correlation.CorrelationRule;
import org.springframework.stereotype.Component;

@Component
public class SameEnvironmentRule implements CorrelationRule {

    private static final double SCORE = 0.15;

    @Override
    public double evaluate(CorrelationContext context) {

        var alert = context.alert();
        var incident = context.incident();

        if (alert.getEnvironment() == null
                || incident.getEnvironment() == null) {
            return 0.0;
        }

        return alert.getEnvironment() == incident.getEnvironment()
                ? SCORE
                : 0.0;
    }
}