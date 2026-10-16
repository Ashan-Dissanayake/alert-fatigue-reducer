package io.github.ashan.alertfatiguereducer.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.ashan.alertfatiguereducer.incident.correlation.AlertRelationshipStore;
import io.github.ashan.alertfatiguereducer.incident.correlation.repository.AlertRelationshipRepository;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

class AlertRelationshipStoreIntegrationTest
        extends AbstractPostgresIntegrationTest {

    @Autowired
    private AlertRelationshipStore relationshipStore;

    @Autowired
    private AlertRelationshipRepository relationshipRepository;

    @Test
    void shouldLoadSeededRelationshipsFromPostgres() {
        Set<String> relatedTypes =
                relationshipStore.findRelatedTypes("LATENCY_HIGH");

        assertEquals(
                Set.of("CPU_HIGH", "ERROR_RATE_HIGH"),
                relatedTypes
        );
    }

    @Test
    void shouldReturnEmptySetWhenNoRelationshipsExist() {
        assertTrue(
                relationshipStore.findRelatedTypes("UNKNOWN_TYPE").isEmpty()
        );
    }

    @Test
    @Transactional
    void shouldReadPersistedRelationshipThroughRepository() {
        assertTrue(
                relationshipRepository.findBySourceType("CPU_HIGH")
                        .stream()
                        .anyMatch(relationship ->
                                "LATENCY_HIGH".equals(
                                        relationship.getRelatedType()
                                ))
        );
    }
}