package io.github.ashan.alertfatiguereducer.shared.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.security.rate-limit")
@Getter
@Setter
public class RateLimitingProperties {

    /**
     * Whether rate limiting is enabled.
     */
    private boolean enabled = true;

    /**
     * Maximum burst request capacity allowed in the token bucket per client.
     */
    private int capacity = 50;

    /**
     * Tokens refilled per second into the bucket.
     */
    private int refillTokensPerSecond = 20;
}
