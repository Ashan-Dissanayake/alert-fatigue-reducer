package io.github.ashan.alertfatiguereducer.incident.correlation;

public interface CorrelationRule {

    double evaluate(CorrelationContext context);

    default RuleEvaluation evaluateExplanation(CorrelationContext context) {
        double score = evaluate(context);
        return new RuleEvaluation(
                getClass().getSimpleName(),
                score,
                score,
                score > 0.0,
                score > 0.0 ? "Rule condition met" : "Rule condition not met"
        );
    }
}
