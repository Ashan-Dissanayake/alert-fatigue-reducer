package io.github.ashan.alertfatiguereducer.incident.correlation.rules;


import io.github.ashan.alertfatiguereducer.incident.correlation.CorrelationContext;
import io.github.ashan.alertfatiguereducer.incident.correlation.CorrelationProperties;
import io.github.ashan.alertfatiguereducer.incident.correlation.CorrelationRule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SameServiceRule implements CorrelationRule {

    private final CorrelationProperties properties;

    @Override
    public double evaluate(CorrelationContext context) {
        var alert = context.alert();
        var incident = context.incident();

        if (alert.getService() == null || incident.getService() == null) {
            return 0.0;
        }

        return alert.getService().equals(incident.getService())
                ? properties.getSameServiceWeight()
                : 0.0;
    }
}