package io.github.ashan.alertfatiguereducer.incident.correlation;

import io.github.ashan.alertfatiguereducer.alert.entity.AlertEnvironment;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.ByteBuffer;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostgresCorrelationLockService implements CorrelationLockService {

    private final EntityManager entityManager;

    /**
     * Acquires a PostgreSQL transaction-level advisory lock using a 64-bit bigint key
     * derived from the sha-256 hash of (environment + ":" + service).
     *
     * This provides fine-grained locking: alerts for different services in the same
     * environment do not contend or block each other.
     */
    @Override
    @Transactional
    public void acquireLock(AlertEnvironment environment, String service) {
        long lockKey = computeLockKey(environment, service);
        log.debug("Acquiring correlation advisory lock for environment: {}, service: {}, lockKey: {}",
                environment, service, lockKey);

        try {
            entityManager.createNativeQuery("SELECT pg_advisory_xact_lock(:lockKey)")
                    .setParameter("lockKey", lockKey)
                    .getSingleResult();
            log.debug("Successfully acquired advisory lock for key: {}", lockKey);
        } catch (Exception ex) {
            log.warn("Advisory lock acquisition returned or not supported by underlying database: {}",
                    ex.getMessage());
        }
    }

    /**
     * Generates a deterministic 64-bit bigint lock key from environment and service.
     */
    public long computeLockKey(AlertEnvironment environment, String service) {
        String input = (environment != null ? environment.name() : "") + ":" + (service != null ? service : "");
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            ByteBuffer buffer = ByteBuffer.wrap(hash);
            return buffer.getLong();
        } catch (NoSuchAlgorithmException e) {
            // Fallback to basic bitwise combination
            return (((long) (environment != null ? environment.hashCode() : 0)) << 32)
                    ^ (service != null ? service.hashCode() : 0);
        }
    }
}
