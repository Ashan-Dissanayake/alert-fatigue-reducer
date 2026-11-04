# ADR 0001: Architecture Structure, Component Responsibilities, and Transaction Boundaries

## Status
Accepted (Milestone 1 — Architecture Review)

## Context
The Alert Fatigue Reducer application is designed as a backend service that ingests monitoring alerts and correlates them into actionable incidents using explainable, rule-based scoring. As part of Milestone 1, an architectural review was performed on the existing package structure, dependencies, component responsibilities, and database transaction boundaries.

---

## 1. Package Structure & Modular Monolith Layout

The application follows a **feature-oriented package structure** within `io.github.ashan.alertfatiguereducer`:

```
io.github.ashan.alertfatiguereducer
├── alert
│   ├── controller       # REST endpoints for alert submission
│   ├── dto              # Request and response contract objects (records)
│   ├── entity           # Alert JPA entity and domain enumerations
│   ├── mapper           # DTO-to-entity and entity-to-DTO converters
│   ├── repository       # Spring Data JPA alert persistence
│   ├── service          # Ingestion orchestration (AlertService)
│   └── validation       # Custom Jakarta Bean Validation constraints
├── incident
│   ├── controller       # REST endpoints for incident queries
│   ├── correlation      # Correlation engine, rules, properties, and relationships
│   │   ├── entity       # AlertRelationship JPA entity
│   │   ├── repository   # Relationship repository
│   │   └── rules        # CorrelationRule implementations
│   ├── dto              # Incident response contracts
│   ├── entity           # Incident, IncidentAlert, IncidentAlertId, Enums
│   ├── mapper           # Incident entity-to-DTO converters
│   ├── repository       # Incident and IncidentAlert JPA repositories
│   ├── service          # Incident lifecycle and alert association services
│   └── specification    # Spring Data JPA Specification for dynamic filtering
└── shared
    ├── config           # Security, OpenAPI, and pagination configurations
    ├── exception        # Domain exceptions and GlobalExceptionHandler
    └── response         # Standard ErrorResponse contract
```

### Architectural Strengths
- **Clear Separation of Concerns:** Controllers, services, repositories, and DTOs follow standard layered boundaries.
- **Dedicated Correlation Sub-package:** The correlation logic is encapsulated behind interfaces (`AlertCorrelationEngine`, `CorrelationRule`, `AlertRelationshipStore`).
- **Shared Infrastructure Isolation:** Common configurations, cross-cutting exception handlers, and security definitions reside in `shared`.

---

## 2. Dependency Flow and Package Coupling Analysis

### Observed Dependency Graph
1. **`alert` package -> `incident` package:**
   - `AlertService` depends on `AlertCorrelationEngine` (interface) and `CorrelationResult`.
2. **`incident` package -> `alert` package:**
   - `IncidentAlert` entity maintains a `@ManyToOne` foreign key reference to `Alert`.
   - `DefaultAlertCorrelationEngine` and `CorrelationContext` accept `Alert` entities for evaluation.
   - Rules evaluate properties of `Alert` (service, environment, timestamp, type).

### Coupling Evaluation
- **Current State:** The coupling between `alert` and `incident` is bidirectional at the entity and domain level. In a modular monolith, this direct reference is cohesive, performant, and avoids premature microservice overhead.
- **Future Decoupling Path:** If alert ingestion needs to scale independently as a dedicated ingestion microservice, `AlertService` can publish an `AlertIngestedEvent` asynchronously (or via transactional outbox/message queue) to decouple ingestion from incident correlation.

---

## 3. Component Responsibilities (Single Responsibility Principle)

| Layer / Component | Responsibility | Boundary Compliance |
|---|---|---|
| `AlertController` | Ingestion HTTP contract, parameter validation, HTTP status codes | Compliant (delegates to `AlertService`) |
| `AlertService` | Orchestrates alert persistence and triggers correlation | Compliant (application service) |
| `DefaultAlertCorrelationEngine` | Queries open candidates, executes rules, determines match/create action | Compliant (domain engine) |
| `CorrelationRule` implementations | Evaluates a single specific match criterion (Service, Env, Type, Time) | Compliant (pure rule logic) |
| `IncidentService` | Manages incident creation, updates, and lifecycle queries | Compliant (incident domain service) |
| `IncidentAlertService` | Manages association records and incident severity escalation | Compliant (join-table domain service) |
| `GlobalExceptionHandler` | Centralized translation of domain exceptions into HTTP error responses | Compliant (cross-cutting) |

---

## 4. Transaction Boundaries & Atomicity Analysis

### Transaction Scope
Alert ingestion and incident correlation are executed under a single, unified transaction:
1. `AlertService.createAlert()` starts the outer `@Transactional` boundary.
2. `Alert` entity is persisted to PostgreSQL via `alertRepository.save(alert)`.
3. `DefaultAlertCorrelationEngine.correlate(alert)` executes within the same active transaction.
4. If a matching incident qualifies (`score >= threshold`), the alert is attached via `incidentAlertService.attachAlert()`. Otherwise, a new incident is created via `incidentService.createIncident()`.
5. Upon method completion, the Spring transaction manager commits all modifications (alert insert, incident insert/update, and incident_alert join record) atomically.

### Transaction Inconsistency Identified & Resolved
- **Issue:** `DefaultAlertCorrelationEngine` was importing `jakarta.transaction.Transactional`, whereas all other services in the codebase use Spring's standard `org.springframework.transaction.annotation.Transactional`.
- **Resolution:** Standardized `DefaultAlertCorrelationEngine` to use `org.springframework.transaction.annotation.Transactional` to ensure consistent Spring transaction management and propagation.

### Failure and Rollback Guarantees
- If correlation encounters a runtime failure (or database constraint violation), the entire transaction rolls back.
- This prevents orphan alerts from remaining un-correlated in the system without an incident assignment.
- Verified by integration test: `AlertTransactionIntegrationTest.shouldRollbackAlertWhenCorrelationFails()`.

---

## 5. Summary & Review Sign-off

Milestone 1 architecture review confirms that the codebase structure is well-organized, respects standard layered architectural patterns, enforces clear transactional atomicity on alert ingestion, and maintains consistent transaction semantics across services.
