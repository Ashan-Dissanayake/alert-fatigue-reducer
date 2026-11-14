package io.github.ashan.alertfatiguereducer.shared.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenBucketTest {

    @Test
    void shouldConsumeTokensUpToCapacity() {
        TokenBucket bucket = new TokenBucket(3, 1.0);

        assertTrue(bucket.tryConsume(), "1st token consume should succeed");
        assertTrue(bucket.tryConsume(), "2nd token consume should succeed");
        assertTrue(bucket.tryConsume(), "3rd token consume should succeed");
        assertFalse(bucket.tryConsume(), "4th token consume should fail when capacity is exhausted");
    }

    @Test
    void shouldRefillTokensOverTime() throws InterruptedException {
        // Capacity: 1, refill: 50 tokens per second (1 token every 20ms)
        TokenBucket bucket = new TokenBucket(1, 50.0);

        assertTrue(bucket.tryConsume());
        assertFalse(bucket.tryConsume());

        // Wait for refill
        Thread.sleep(30);

        assertTrue(bucket.tryConsume(), "Token should be refilled after elapsed time");
    }
}
