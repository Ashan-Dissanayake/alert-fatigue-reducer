package io.github.ashan.alertfatiguereducer.incident.correlation.rules;

import io.github.ashan.alertfatiguereducer.incident.correlation.CorrelationContext;
import io.github.ashan.alertfatiguereducer.incident.correlation.CorrelationProperties;
import io.github.ashan.alertfatiguereducer.incident.correlation.CorrelationRule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SameEnvironmentRule implements CorrelationRule {

    private final CorrelationProperties properties;

    @Override
    public double evaluate(CorrelationContext context) {

        var alert = context.alert();
        var incident = context.incident();

        if (alert.getEnvironment() == null
                || incident.getEnvironment() == null) {
            return 0.0;
        }

        return alert.getEnvironment() == incident.getEnvironment()
                ? properties.getSameEnvironmentWeight()
                : 0.0;
    }
}