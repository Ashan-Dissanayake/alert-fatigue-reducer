package io.github.ashan.alertfatiguereducer.integration;

import io.github.ashan.alertfatiguereducer.alert.entity.AlertEnvironment;
import io.github.ashan.alertfatiguereducer.incident.correlation.CorrelationLockService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class CorrelationLockServiceIntegrationTest
        extends AbstractPostgresIntegrationTest {

    @Autowired
    private CorrelationLockService correlationLockService;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldBlockSecondTransactionUntilFirstTransactionCompletes()
            throws Exception {

        TransactionTemplate transactionTemplate =
                new TransactionTemplate(transactionManager);

        CountDownLatch firstLockAcquired = new CountDownLatch(1);
        CountDownLatch releaseFirstTransaction = new CountDownLatch(1);
        CountDownLatch secondAttemptStarted = new CountDownLatch(1);
        CountDownLatch secondLockAcquired = new CountDownLatch(1);

        AtomicBoolean secondAcquiredBeforeRelease = new AtomicBoolean(false);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<?> firstTransaction = executor.submit(() ->
                    transactionTemplate.executeWithoutResult(status -> {
                        correlationLockService.lockEnvironment(
                                AlertEnvironment.DEVELOPMENT);

                        firstLockAcquired.countDown();
                        await(releaseFirstTransaction);
                    })
            );

            assertTrue(
                    firstLockAcquired.await(10, TimeUnit.SECONDS),
                    "First transaction should acquire the lock"
            );

            Future<?> secondTransaction = executor.submit(() ->
                    transactionTemplate.executeWithoutResult(status -> {
                        secondAttemptStarted.countDown();

                        correlationLockService.lockEnvironment(
                                AlertEnvironment.DEVELOPMENT);

                        secondAcquiredBeforeRelease.set(
                                releaseFirstTransaction.getCount() > 0);

                        secondLockAcquired.countDown();
                    })
            );

            assertTrue(
                    secondAttemptStarted.await(10, TimeUnit.SECONDS),
                    "Second transaction should attempt to acquire the lock"
            );

            try {
                assertFalse(
                        secondLockAcquired.await(500, TimeUnit.MILLISECONDS),
                        "Second transaction must wait while the first holds the lock"
                );
            } finally {
                releaseFirstTransaction.countDown();
            }

            firstTransaction.get(10, TimeUnit.SECONDS);
            secondTransaction.get(10, TimeUnit.SECONDS);

            assertTrue(
                    secondLockAcquired.getCount() == 0,
                    "Second transaction should acquire the lock after release"
            );

            assertFalse(
                    secondAcquiredBeforeRelease.get(),
                    "Second transaction must not acquire the lock before release"
            );
        } finally {
            releaseFirstTransaction.countDown();
            executor.shutdownNow();
        }
    }


    @Test
    void shouldReleaseAdvisoryLockAfterTransactionRollback() throws Exception {
        TransactionTemplate transactionTemplate =
                new TransactionTemplate(transactionManager);

        assertThrows(
                IllegalStateException.class,
                () -> transactionTemplate.executeWithoutResult(status -> {
                    correlationLockService.lockEnvironment(
                            AlertEnvironment.DEVELOPMENT);

                    throw new IllegalStateException("Simulated transaction failure");
                })
        );

        Boolean acquired = transactionTemplate.execute(status -> {
            correlationLockService.lockEnvironment(
                    AlertEnvironment.DEVELOPMENT);
            return true;
        });

        assertEquals(Boolean.TRUE, acquired,
                "The advisory lock should be available after rollback");
    }


    private void await(CountDownLatch latch) {
        try {
            if (!latch.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException(
                        "Timed out waiting for transaction release");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Interrupted while waiting for transaction release",
                    exception);
        }
    }
}
