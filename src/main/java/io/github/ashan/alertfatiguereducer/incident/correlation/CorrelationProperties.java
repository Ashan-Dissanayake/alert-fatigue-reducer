package io.github.ashan.alertfatiguereducer.incident.correlation;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "app.correlation")
@Validated
@Getter
@Setter
public class CorrelationProperties {

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private double threshold = 0.70;

    @Min(1)
    private long windowMinutes = 10;

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private double sameServiceWeight = 0.30;

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private double relatedAlertTypeWeight = 0.30;

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private double sameEnvironmentWeight = 0.15;

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private double timeProximityMaxScore = 0.25;

    @DecimalMin(value = "0.0", inclusive = false)
    private double timeProximityHalfLifeMinutes = 5.0;
}