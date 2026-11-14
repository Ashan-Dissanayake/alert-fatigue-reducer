package io.github.ashan.alertfatiguereducer.incident.correlation;

import io.github.ashan.alertfatiguereducer.alert.entity.AlertEnvironment;

/**
 * Provides transactional locking granularity to prevent concurrent correlation races
 * and duplicate incident creation within the same environment and service scope.
 */
public interface CorrelationLockService {

    /**
     * Acquires a transactional lock for the given environment and service.
     * The lock is automatically released by the database when the ambient transaction completes.
     *
     * @param environment the alert environment (e.g. PRODUCTION, STAGING)
     * @param service     the service name (e.g. payment-service)
     */
    void acquireLock(AlertEnvironment environment, String service);
}
