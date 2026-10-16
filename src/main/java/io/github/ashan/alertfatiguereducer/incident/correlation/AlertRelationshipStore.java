package io.github.ashan.alertfatiguereducer.incident.correlation;

import java.util.Set;

public interface AlertRelationshipStore {

    Set<String> findRelatedTypes(String sourceType);
}