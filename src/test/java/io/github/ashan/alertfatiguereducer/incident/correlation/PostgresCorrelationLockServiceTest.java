package io.github.ashan.alertfatiguereducer.incident.correlation;

import io.github.ashan.alertfatiguereducer.alert.entity.AlertEnvironment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class PostgresCorrelationLockServiceTest {

    private EntityManager entityManager;
    private PostgresCorrelationLockService lockService;

    @BeforeEach
    void setUp() {
        entityManager = mock(EntityManager.class);
        lockService = new PostgresCorrelationLockService(entityManager);
    }

    @Test
    void shouldComputeDeterministicLockKey() {
        long key1 = lockService.computeLockKey(AlertEnvironment.PRODUCTION, "payment-service");
        long key2 = lockService.computeLockKey(AlertEnvironment.PRODUCTION, "payment-service");

        assertEquals(key1, key2, "Same environment and service must produce identical lock key");
    }

    @Test
    void shouldProduceDistinctLockKeysForDifferentServices() {
        long paymentKey = lockService.computeLockKey(AlertEnvironment.PRODUCTION, "payment-service");
        long orderKey = lockService.computeLockKey(AlertEnvironment.PRODUCTION, "order-service");

        assertNotEquals(paymentKey, orderKey, "Different services must yield different lock keys for granular locking");
    }

    @Test
    void shouldProduceDistinctLockKeysForDifferentEnvironments() {
        long prodKey = lockService.computeLockKey(AlertEnvironment.PRODUCTION, "payment-service");
        long stagingKey = lockService.computeLockKey(AlertEnvironment.STAGING, "payment-service");

        assertNotEquals(prodKey, stagingKey, "Different environments must yield different lock keys");
    }

    @Test
    void shouldExecuteAdvisoryLockQuerySuccessfully() {
        Query mockQuery = mock(Query.class);
        when(entityManager.createNativeQuery(anyString())).thenReturn(mockQuery);
        when(mockQuery.setParameter(eq("lockKey"), anyLong())).thenReturn(mockQuery);
        when(mockQuery.getSingleResult()).thenReturn(null);

        assertDoesNotThrow(() ->
                lockService.acquireLock(AlertEnvironment.PRODUCTION, "order-service")
        );

        verify(entityManager).createNativeQuery("SELECT pg_advisory_xact_lock(:lockKey)");
        verify(mockQuery).setParameter(eq("lockKey"), anyLong());
        verify(mockQuery).getSingleResult();
    }

    @Test
    void shouldHandleNonPostgresOrUnsupportedGracefullyWithoutThrowing() {
        Query mockQuery = mock(Query.class);
        when(entityManager.createNativeQuery(anyString())).thenReturn(mockQuery);
        when(mockQuery.setParameter(eq("lockKey"), anyLong())).thenReturn(mockQuery);
        when(mockQuery.getSingleResult()).thenThrow(new RuntimeException("Function pg_advisory_xact_lock does not exist"));

        assertDoesNotThrow(() ->
                lockService.acquireLock(AlertEnvironment.PRODUCTION, "order-service")
        );
    }
}
