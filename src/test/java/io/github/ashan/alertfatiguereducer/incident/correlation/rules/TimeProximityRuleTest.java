package io.github.ashan.alertfatiguereducer.incident.correlation.rules;

import io.github.ashan.alertfatiguereducer.alert.entity.Alert;
import io.github.ashan.alertfatiguereducer.incident.correlation.CorrelationContext;
import io.github.ashan.alertfatiguereducer.incident.entity.Incident;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimeProximityRuleTest {

    private final TimeProximityRule rule = new TimeProximityRule();

    private final LocalDateTime baseTime =
            LocalDateTime.of(2026, 10, 9, 10, 0);

    private double evaluateAt(long minutesAfter) {
        Alert alert = new Alert();
        alert.setTimestamp(baseTime.plusMinutes(minutesAfter));

        Incident incident = new Incident();
        incident.setStartedAt(baseTime);

        return rule.evaluate(new CorrelationContext(alert, incident, List.of()));
    }

    @Test
    void shouldReturnHighestScoreWithinTwoMinutes() {
        assertEquals(0.25, evaluateAt(2));
    }

    @Test
    void shouldReturnMediumScoreWithinFiveMinutes() {
        assertEquals(0.20, evaluateAt(5));
    }

    @Test
    void shouldReturnLowerScoreWithinTenMinutes() {
        assertEquals(0.10, evaluateAt(10));
    }

    @Test
    void shouldReturnZeroBeyondTenMinutes() {
        assertEquals(0.0, evaluateAt(11));
    }

    @Test
    void shouldReturnZeroWhenAlertTimestampIsNull() {
        Alert alert = new Alert();

        Incident incident = new Incident();
        incident.setStartedAt(baseTime);

        assertEquals(0.0, rule.evaluate(new CorrelationContext(alert, incident, List.of())));
    }

    @Test
    void shouldReturnHighestScoreWhenAlertIsWithinTwoMinutesAndFiftyNineSeconds() {
        Alert alert = new Alert();
        alert.setTimestamp(baseTime.plusMinutes(2).plusSeconds(59));

        Incident incident = new Incident();
        incident.setStartedAt(baseTime);

        assertEquals(
                0.25,
                rule.evaluate(new CorrelationContext(alert, incident, List.of()))
        );
    }

    @Test
    void shouldReturnSameScoreWhenAlertOccursBeforeIncidentStart() {
        Alert alert = new Alert();
        alert.setTimestamp(baseTime.minusMinutes(2));

        Incident incident = new Incident();
        incident.setStartedAt(baseTime);

        assertEquals(
                0.25,
                rule.evaluate(new CorrelationContext(alert, incident, List.of()))
        );
    }
}