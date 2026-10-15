package io.github.ashan.alertfatiguereducer.incident.correlation.rules;

import io.github.ashan.alertfatiguereducer.alert.entity.Alert;
import io.github.ashan.alertfatiguereducer.incident.correlation.CorrelationContext;
import io.github.ashan.alertfatiguereducer.incident.entity.Incident;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;


class TimeProximityRuleTest {

    private static final double DELTA = 0.000001;

    private final TimeProximityRule rule = new TimeProximityRule();

    private final LocalDateTime baseTime =
            LocalDateTime.of(2026, 10, 9, 10, 0);

    private double evaluateAt(long minutesAfter) {
        Alert alert = new Alert();
        alert.setTimestamp(baseTime.plusMinutes(minutesAfter));

        Incident incident = new Incident();
        incident.setStartedAt(baseTime);

        return rule.evaluate(
                new CorrelationContext(alert, incident, List.of())
        );
    }

    @Test
    void shouldReturnMaximumScoreAtIncidentStart() {
        assertEquals(0.25, evaluateAt(0), DELTA);
    }

    @Test
    void shouldReturnApproximatelyHalfScoreAfterFiveMinutes() {
        assertEquals(0.125, evaluateAt(5), DELTA);
    }

    @Test
    void shouldReturnQuarterScoreAfterTenMinutes() {
        assertEquals(0.0625, evaluateAt(10), DELTA);
    }

    @Test
    void shouldDecaySmoothlyBetweenTwoAndThreeMinutes() {
        double afterTwoMinutes = evaluateAt(2);
        double afterThreeMinutes = evaluateAt(3);

        assertTrue(afterTwoMinutes > afterThreeMinutes);
        assertTrue(afterThreeMinutes > 0.0);
    }

    @Test
    void shouldReturnPositiveScoreBeyondTenMinutes() {
        assertTrue(evaluateAt(11) > 0.0);
    }

    @Test
    void shouldReturnZeroWhenAlertTimestampIsNull() {
        Alert alert = new Alert();

        Incident incident = new Incident();
        incident.setStartedAt(baseTime);

        assertEquals(
                0.0,
                rule.evaluate(
                        new CorrelationContext(alert, incident, List.of())
                ),
                DELTA
        );
    }

    @Test
    void shouldReturnMaximumScoreWhenTimestampsAreEqual() {
        assertEquals(0.25, evaluateAt(0), DELTA);
    }


    @Test
    void shouldReturnZeroWhenAlertTimestampIsBeforeIncidentStart() {
        Alert alert = new Alert();
        alert.setTimestamp(baseTime.minusMinutes(2));

        Incident incident = new Incident();
        incident.setStartedAt(baseTime);

        double score = rule.evaluate(
                new CorrelationContext(alert, incident, List.of())
        );

        assertEquals(0.0, score, DELTA);
    }


    @Test
    void shouldReturnZeroWhenIncidentStartIsNull() {
        Alert alert = new Alert();
        alert.setTimestamp(baseTime);

        Incident incident = new Incident();

        assertEquals(
                0.0,
                rule.evaluate(
                        new CorrelationContext(alert, incident, List.of())
                ),
                DELTA
        );
    }


    @Test
    void shouldCalculateScoreFromLatestRelatedAlertTimestamp() {
        Alert olderRelatedAlert = new Alert();
        olderRelatedAlert.setTimestamp(baseTime);

        Alert latestRelatedAlert = new Alert();
        latestRelatedAlert.setTimestamp(baseTime.plusMinutes(4));

        Alert incomingAlert = new Alert();
        incomingAlert.setTimestamp(baseTime.plusMinutes(9));

        Incident incident = new Incident();
        incident.setStartedAt(baseTime);

        double score = rule.evaluate(
                new CorrelationContext(
                        incomingAlert,
                        incident,
                        List.of(olderRelatedAlert, latestRelatedAlert)
                )
        );

        // 5 minutes after the latest related alert: half of MAX_SCORE.
        assertEquals(0.125, score, DELTA);
    }

}
