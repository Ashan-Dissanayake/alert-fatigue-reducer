package io.github.ashan.alertfatiguereducer.alert.repository;

import io.github.ashan.alertfatiguereducer.alert.entity.Alert;
import io.github.ashan.alertfatiguereducer.alert.entity.AlertEnvironment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    /**
     * Looks up an existing alert with the exact same fingerprint (service, environment, type, timestamp)
     * to support idempotent ingestion and prevent duplicate records on client retries.
     */
    Optional<Alert> findFirstByServiceAndEnvironmentAndTypeAndTimestamp(
            String service,
            AlertEnvironment environment,
            String type,
            LocalDateTime timestamp
    );
}
