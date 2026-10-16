package io.github.ashan.alertfatiguereducer.incident.correlation.repository;

import io.github.ashan.alertfatiguereducer.incident.correlation.entity.AlertRelationship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRelationshipRepository
        extends JpaRepository<AlertRelationship, Long> {

    List<AlertRelationship> findBySourceType(String sourceType);
}