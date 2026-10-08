package io.github.ashan.alertfatiguereducer.alert.dto.request;


import io.github.ashan.alertfatiguereducer.alert.entity.AlertEnvironment;
import io.github.ashan.alertfatiguereducer.alert.entity.AlertSeverity;
import io.github.ashan.alertfatiguereducer.alert.entity.AlertSource;
import io.github.ashan.alertfatiguereducer.alert.validation.PastOrPresentTimestamp;
import io.github.ashan.alertfatiguereducer.alert.validation.SupportedAlertType;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record CreateAlertRequest(

        @NotNull
        AlertSource source,

        @NotBlank
        @Size(max = 100)
        String service,

        @NotNull
        AlertEnvironment environment,

        @NotBlank
        @Size(max = 100)
        @SupportedAlertType
        String type,

        @NotNull
        AlertSeverity severity,

        @Size(max = 2000)
        String message,

        @NotNull
        @PastOrPresentTimestamp
        LocalDateTime timestamp,

        @Size(max = 100)
        String metric,

        Double value,

        Double threshold
) {
}