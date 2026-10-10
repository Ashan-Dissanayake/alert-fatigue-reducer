package io.github.ashan.alertfatiguereducer.shared.config;

import java.util.Set;

public final class AllowedIncidentSortFields {

    public static final Set<String> FIELDS = Set.of(
            "id",
            "title",
            "service",
            "environment",
            "severity",
            "status",
            "startedAt",
            "lastUpdatedAt",
            "correlationScore"
    );
}
