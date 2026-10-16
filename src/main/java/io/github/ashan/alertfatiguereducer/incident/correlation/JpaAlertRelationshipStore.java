package io.github.ashan.alertfatiguereducer.incident.correlation;

import io.github.ashan.alertfatiguereducer.incident.correlation.entity.AlertRelationship;
import io.github.ashan.alertfatiguereducer.incident.correlation.repository.AlertRelationshipRepository;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
public class JpaAlertRelationshipStore implements AlertRelationshipStore {

    private final AlertRelationshipRepository repository;

    public JpaAlertRelationshipStore(AlertRelationshipRepository repository) {
        this.repository = repository;
    }

    @Override
    public Set<String> findRelatedTypes(String sourceType) {
        if (sourceType == null || sourceType.isBlank()) {
            return Set.of();
        }

        return repository.findBySourceType(sourceType).stream()
                .map(AlertRelationship::getRelatedType)
                .collect(Collectors.toUnmodifiableSet());
    }
}