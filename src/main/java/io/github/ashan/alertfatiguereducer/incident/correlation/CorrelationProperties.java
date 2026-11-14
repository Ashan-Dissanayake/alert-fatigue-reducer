package io.github.ashan.alertfatiguereducer.incident.correlation;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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

    @NotBlank
    private String policyVersion = "v1.0";

    @DecimalMin(value = "0.0", inclusive = false)
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

    /**
     * Cross-field validation: ensures the sum of all rule weights does not exceed 1.0,
     * preventing a configuration where correlated alerts always score above the threshold
     * regardless of actual rule matches.
     */
    @PostConstruct
    public void validateWeightSum() {
        double weightSum = sameServiceWeight
                + relatedAlertTypeWeight
                + sameEnvironmentWeight
                + timeProximityMaxScore;
        if (weightSum > 1.0 + 1e-9) {
            throw new IllegalStateException(
                    "Correlation rule weights sum (" + weightSum + ") must not exceed 1.0. " +
                    "Check app.correlation.* weight properties.");
        }
    }
}
