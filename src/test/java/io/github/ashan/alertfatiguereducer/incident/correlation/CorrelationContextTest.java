package io.github.ashan.alertfatiguereducer.incident.correlation;

import io.github.ashan.alertfatiguereducer.alert.entity.Alert;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertThrows;

class CorrelationContextTest {

    @Test
    void shouldRejectNullRelatedAlertElements() {
        Alert alert = new Alert();

        assertThrows(
                NullPointerException.class,
                () -> new CorrelationContext(
                        alert,
                        null,
                        Arrays.asList((Alert) null)
                )
        );
    }
}
