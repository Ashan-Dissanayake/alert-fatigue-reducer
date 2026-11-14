package io.github.ashan.alertfatiguereducer.incident.correlation.rules;

import io.github.ashan.alertfatiguereducer.alert.entity.Alert;
import io.github.ashan.alertfatiguereducer.alert.entity.AlertType;
import io.github.ashan.alertfatiguereducer.incident.correlation.AlertRelationshipStore;
import io.github.ashan.alertfatiguereducer.incident.correlation.CorrelationContext;
import io.github.ashan.alertfatiguereducer.incident.correlation.CorrelationProperties;
import io.github.ashan.alertfatiguereducer.incident.correlation.CorrelationRule;
import io.github.ashan.alertfatiguereducer.incident.correlation.RuleEvaluation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class RelatedAlertTypeRule implements CorrelationRule {

    private final AlertRelationshipStore relationshipStore;
    private final CorrelationProperties properties;

    @Override
    public RuleEvaluation evaluateExplanation(CorrelationContext context) {
        Alert incomingAlert = context.alert();

        if (incomingAlert.getType() == null) {
            return new RuleEvaluation(
                    "RelatedAlertTypeRule",
                    0.0,
                    properties.getRelatedAlertTypeWeight(),
                    false,
                    "Incoming alert type is null"
            );
        }

        String incomingType = incomingAlert.getType();

        try {
            AlertType.valueOf(incomingType);
        } catch (IllegalArgumentException exception) {
            return new RuleEvaluation(
                    "RelatedAlertTypeRule",
                    0.0,
                    properties.getRelatedAlertTypeWeight(),
                    false,
                    "Alert type '" + incomingType + "' is not a supported AlertType"
            );
        }

        Set<String> relatedTypes = relationshipStore.findRelatedTypes(incomingType);

        if (relatedTypes.isEmpty()) {
            return new RuleEvaluation(
                    "RelatedAlertTypeRule",
                    0.0,
                    properties.getRelatedAlertTypeWeight(),
                    false,
                    "No related alert types registered in database for type: " + incomingType
            );
        }

        List<String> matchedTypes = context.relatedAlerts().stream()
                .map(Alert::getType)
                .filter(Objects::nonNull)
                .filter(relatedTypes::contains)
                .distinct()
                .toList();

        boolean matched = !matchedTypes.isEmpty();
        double score = matched ? properties.getRelatedAlertTypeWeight() : 0.0;
        String reason = matched
                ? "Alert type '" + incomingType + "' relates to incident alert type(s): " + matchedTypes
                : "Alert type '" + incomingType + "' has no relationships matching existing incident alerts";

        return new RuleEvaluation(
                "RelatedAlertTypeRule",
                score,
                properties.getRelatedAlertTypeWeight(),
                matched,
                reason
        );
    }

    @Override
    public double evaluate(CorrelationContext context) {
        return evaluateExplanation(context).score();
    }
}
