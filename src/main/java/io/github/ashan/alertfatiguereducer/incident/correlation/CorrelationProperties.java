package io.github.ashan.alertfatiguereducer.incident.correlation;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.correlation")
public class CorrelationProperties {

    private double threshold = 0.70;
    private long windowMinutes = 10;

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public long getWindowMinutes() {
        return windowMinutes;
    }

    public void setWindowMinutes(long windowMinutes) {
        this.windowMinutes = windowMinutes;
    }
}
