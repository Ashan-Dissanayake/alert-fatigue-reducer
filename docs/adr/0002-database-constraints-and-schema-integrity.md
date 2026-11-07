# ADR 0002: Database Constraints & Schema Integrity

## Status
Accepted

## Context
Data integrity and concurrency guarantees require alignment between the PostgreSQL relational schema (managed by Flyway) and JPA entity definitions. As the correlation engine evaluates, groups, and attaches incoming alerts to incidents concurrently, schema-level constraints act as the ultimate safeguard against data anomalies and inconsistent states.

## Decision
1. **Flyway-First Migrations:** Flyway retains single ownership of the database schema (`ddl-auto=validate` in production). JPA annotations must strictly mirror Flyway DDL constraints and indexes.
2. **Alert Index Parity:** Aligned `Alert` JPA entity indexes with Flyway migration `V2__harden_alert_schema.sql` by maintaining `idx_alerts_service_environment_timestamp` (`service, environment, timestamp`) and dropping single-column indexes.
3. **Single Incident per Alert Invariant:** Flyway migration `V5__enforce_single_incident_per_alert.sql` establishes `uk_incident_alerts_alert_id` unique constraint on `incident_alerts(alert_id)`. The `IncidentAlert` JPA entity is updated to declare this unique constraint.
4. **Relational Constraints for Graph Integrity:** Flyway migration `V6__create_alert_relationships.sql` enforces type pair uniqueness (`uk_alert_relationships_types`) and bidirectional non-identity (`chk_alert_relationships_different_types`).

## Consequences
- Schema validation tools (`hibernate.ddl-auto=validate`) pass without schema divergence warnings.
- Multiple threads attempting to attach the same alert to different incidents will fail at the database level with a unique constraint violation rather than corrupting relational integrity.
