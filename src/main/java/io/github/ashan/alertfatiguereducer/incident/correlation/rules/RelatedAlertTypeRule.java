package io.github.ashan.alertfatiguereducer.incident.correlation.rules;

import io.github.ashan.alertfatiguereducer.alert.entity.Alert;
import io.github.ashan.alertfatiguereducer.alert.entity.AlertType;
import io.github.ashan.alertfatiguereducer.incident.correlation.AlertRelationshipStore;
import io.github.ashan.alertfatiguereducer.incident.correlation.CorrelationContext;
import io.github.ashan.alertfatiguereducer.incident.correlation.CorrelationProperties;
import io.github.ashan.alertfatiguereducer.incident.correlation.CorrelationRule;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RelatedAlertTypeRule implements CorrelationRule {

    private final AlertRelationshipStore relationshipStore;
    private final CorrelationProperties properties;


    @Override
    public double evaluate(CorrelationContext context) {
        Alert incomingAlert = context.alert();

        if (incomingAlert.getType() == null) {
            return 0.0;
        }

        String incomingType = incomingAlert.getType();

        try {
            AlertType.valueOf(incomingType);
        } catch (IllegalArgumentException exception) {
            return 0.0;
        }

        Set<String> relatedTypes =
                relationshipStore.findRelatedTypes(incomingType);

        if (relatedTypes.isEmpty()) {
            return 0.0;
        }

        return context.relatedAlerts().stream()
                .map(Alert::getType)
                .filter(type -> type != null)
                .anyMatch(relatedTypes::contains)
                ? properties.getRelatedAlertTypeWeight()
                : 0.0;
    }
}