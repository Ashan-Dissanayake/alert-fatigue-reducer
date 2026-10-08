package io.github.ashan.alertfatiguereducer.alert.repository;

import io.github.ashan.alertfatiguereducer.alert.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
}