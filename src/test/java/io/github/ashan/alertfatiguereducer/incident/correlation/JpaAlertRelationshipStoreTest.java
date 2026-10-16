package io.github.ashan.alertfatiguereducer.incident.correlation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.ashan.alertfatiguereducer.incident.correlation.entity.AlertRelationship;
import io.github.ashan.alertfatiguereducer.incident.correlation.repository.AlertRelationshipRepository;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JpaAlertRelationshipStoreTest {

    private AlertRelationshipRepository repository;
    private JpaAlertRelationshipStore store;

    @BeforeEach
    void setUp() {
        repository = mock(AlertRelationshipRepository.class);
        store = new JpaAlertRelationshipStore(repository);
    }

    @Test
    void shouldReturnRelatedTypesFromRepository() {
        when(repository.findBySourceType("LATENCY_HIGH"))
                .thenReturn(List.of(
                        createRelationship("LATENCY_HIGH", "CPU_HIGH"),
                        createRelationship("LATENCY_HIGH", "ERROR_RATE_HIGH")
                ));

        Set<String> result = store.findRelatedTypes("LATENCY_HIGH");

        assertEquals(Set.of("CPU_HIGH", "ERROR_RATE_HIGH"), result);
        verify(repository).findBySourceType("LATENCY_HIGH");
    }

    @Test
    void shouldReturnEmptySetWhenNoRelationshipsExist() {
        when(repository.findBySourceType("CPU_HIGH"))
                .thenReturn(List.of());

        assertEquals(Set.of(), store.findRelatedTypes("CPU_HIGH"));
    }

    @Test
    void shouldReturnEmptySetWhenSourceTypeIsNull() {
        assertEquals(Set.of(), store.findRelatedTypes(null));

        verify(repository, never()).findBySourceType(null);
    }

    @Test
    void shouldReturnEmptySetWhenSourceTypeIsBlank() {
        assertEquals(Set.of(), store.findRelatedTypes(" "));

        verify(repository, never()).findBySourceType(" ");
    }

    private AlertRelationship createRelationship(
            String sourceType,
            String relatedType
    ) {
        AlertRelationship relationship = new AlertRelationship();
        relationship.setSourceType(sourceType);
        relationship.setRelatedType(relatedType);
        return relationship;
    }
}