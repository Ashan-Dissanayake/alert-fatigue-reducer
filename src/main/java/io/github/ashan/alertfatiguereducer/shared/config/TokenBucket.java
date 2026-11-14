package io.github.ashan.alertfatiguereducer.shared.config;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe token bucket implementation for request rate limiting and burst protection.
 */
public class TokenBucket {

    private final long capacity;
    private final double refillTokensPerNano;

    private final AtomicLong availableTokens;
    private final AtomicLong lastRefillNanos;

    public TokenBucket(long capacity, double refillTokensPerSecond) {
        this.capacity = capacity;
        this.refillTokensPerNano = refillTokensPerSecond / 1_000_000_000.0;
        this.availableTokens = new AtomicLong(capacity);
        this.lastRefillNanos = new AtomicLong(System.nanoTime());
    }

    public synchronized boolean tryConsume() {
        refill();
        if (availableTokens.get() >= 1) {
            availableTokens.decrementAndGet();
            return true;
        }
        return false;
    }

    private void refill() {
        long now = System.nanoTime();
        long last = lastRefillNanos.get();
        long elapsedNanos = now - last;

        if (elapsedNanos > 0) {
            long tokensToAdd = (long) (elapsedNanos * refillTokensPerNano);
            if (tokensToAdd > 0) {
                long current = availableTokens.get();
                long updated = Math.min(capacity, current + tokensToAdd);
                availableTokens.set(updated);
                lastRefillNanos.set(now);
            }
        }
    }
}
