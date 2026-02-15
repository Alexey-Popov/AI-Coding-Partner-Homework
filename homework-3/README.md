# Homework 3: Specification-Driven Design

**Student:** Yevgen Polukov
**Date:** February 2026
**Domain:** Virtual Card Lifecycle & Spending Controls

---

## Task Summary

This homework produces a specification package for a FinTech virtual card management system — no implementation code. The deliverables are seven documents that, together, provide everything an AI coding agent (or a human developer) needs to build the system from scratch:

1. **specification.md** — Full feature specification with 18 low-level tasks, each containing an exact AI prompt, target files, and testable acceptance criteria.
2. **agents.md** — Slim router defining operating principles and architecture conventions; references policy files so the LLM loads only what is needed per task.
3. **security-policy.md** — OWASP Top 10 mapping, logging redaction, input validation, error response rules.
4. **testing-policy.md** — Test types, minimums, rules, and what not to test.
5. **documentation-policy.md** — Javadoc and comment conventions.
6. **.github/copilot-instructions.md** — Concise editor-level rules for naming, layering, anti-patterns, and commit discipline; references agents.md and security-policy.md for details.
7. **README.md** — This file: rationale and industry best-practice mapping.

---

## Rationale

### Why This Structure?

The specification follows a **three-tier objective hierarchy** (high-level -> mid-level -> low-level tasks) because this mirrors how real FinTech teams decompose work:

- The **high-level objective** is a single sentence that any stakeholder (product, engineering, compliance) can read and agree on. It anchors every decision below it.
- The **mid-level objectives** (7 items) are measurable outcomes. Each one maps to a testable capability — "spending controls enforcement" can be verified with integration tests; "idempotency and concurrency safety" can be verified with concurrent request tests. This level exists so QA and compliance can independently validate completeness.
- The **low-level tasks** (18 items) are ordered implementation steps with exact prompts, file targets, and acceptance criteria. The ordering encodes dependency: domain model before repositories, repositories before services, services before controllers, controllers before tests. This prevents an AI agent from creating code that references undefined types.

### Why Option A (Virtual Card Lifecycle)?

Virtual card management was chosen because it exercises the widest range of FinTech concerns within a single bounded context: state machine enforcement (card lifecycle), financial limit enforcement (spending controls), append-only audit logging (compliance), idempotency (payment safety), and role-based access control (end-user vs. ops/compliance). It is complex enough to be realistic but contained enough for a monolith.

### Why a Monolith?

A monolith with layered architecture was chosen over microservices because: (a) the scope is a single bounded context, (b) transactional consistency across card state + spending aggregates + audit is simpler within one process, and (c) premature decomposition into services introduces distributed-system complexity (eventual consistency, network failures, saga coordination) that is unnecessary at this scale and distracts from the core FinTech patterns.

### Why No Foreign Keys?

The specification explicitly forbids foreign keys between tables. This is a deliberate FinTech pattern: in high-throughput payment systems, foreign keys create cascading lock contention during concurrent writes. Application-level referential integrity (enforced in the service layer and validated by integration tests) provides the same correctness guarantee with better concurrency characteristics. This decision is documented in the Flyway migration task (Task 5).

### Why Separate Audit Propagation (REQUIRES_NEW)?

Audit events use `@Transactional(propagation = REQUIRES_NEW)` so that if a business operation fails and rolls back, the audit record of the attempt still persists. In a regulated environment, knowing that an action was *attempted* (even if it failed) is as important as knowing it succeeded. This pattern is specified in Task 10 (AuditService).

### Why Modular Files Instead of One Large agents.md?

The specification package is split into 7 files instead of 4 for one reason: **LLM context efficiency**. When an AI agent implements Task 6 (DTOs with validation), it needs the validation constraints from `security-policy.md` but not the testing rules from `testing-policy.md`. By splitting policies into dedicated files, the agent's "Required Reading" table in `agents.md` routes it to load only the relevant context. This reduces token waste, prevents the agent from being distracted by irrelevant rules, and follows the single-responsibility principle for documentation — each file has one job and one reason to change.

---

## Industry Best Practices

The table below lists each practice incorporated into the specification and points to the exact section(s) where it appears.

| Practice | Where It Appears |
|---|---|
| **Strict state machine with explicit transitions** | Mid-Level Objective 1; Task 1 (CardState.allowedTransitions); Task 2 (VirtualCard.transitionTo); specification.md > Card State Machine diagram |
| **Immutable append-only audit trail** | Mid-Level Objective 7; Implementation Notes > Audit & Compliance; Task 3 (CardAuditEvent — no setters); Task 10 (AuditService) |
| **OWASP Top 10 systematic mapping** | Mid-Level Objective 5; `security-policy.md` (full OWASP table) |
| **Optimistic locking for concurrency control** | Mid-Level Objective 6; Task 2 (@Version on VirtualCard); Task 12 (SpendingAggregate locking) |
| **Idempotency keys for safe retries** | Mid-Level Objective 6; Implementation Notes > Idempotency; Task 8 (IdempotencyService) |
| **BigDecimal for money (never float/double)** | `agents.md` > Key Conventions; Task 2 (NUMERIC(19,4)); Task 6 (DTO @Digits) |
| **ISO-4217 currency codes** | Implementation Notes > Data Modeling Guidance; Task 6 (CreateCardRequest validation) |
| **Banker's rounding (HALF_EVEN)** | `agents.md` > Key Conventions |
| **Role-based access control (RBAC) — deny by default** | Mid-Level Objective 5; Task 9 (AuthorizationService); Task 16 (OpsController) |
| **Input validation at system boundary** | `security-policy.md` > Domain Validation Constraints; Task 6 (Jakarta Bean Validation on DTOs) |
| **Safe error handling (no leakage)** | `security-policy.md` > Error Response Contract; Task 7 (GlobalExceptionHandler) |
| **Correlation IDs across request lifecycle** | Mid-Level Objective 7; Task 10 (AuditService); Task 14 (CardController correlation ID generation) |
| **Structured logging with sensitive-field redaction** | `security-policy.md` > Logging Redaction Rules; Task 10 (AuditService logging rules) |
| **Rate limiting / throttling** | Implementation Notes > Rate Limiting; Task 17 (RateLimitConfig) |
| **No foreign keys (FinTech concurrency pattern)** | Task 5 (V1__initial_schema.sql — documented decision) |
| **Testcontainers for realistic integration tests** | `testing-policy.md`; Task 18 (all integration tests use PostgreSQL via Testcontainers) |
| **Separation of DTOs from entities** | `agents.md` > Layer Dependency Rules; Task 6 (DTOs); Task 14 (controller passes through DTOs) |
| **Flyway for versioned schema migrations** | Task 5 (V1__initial_schema.sql) |
| **Data minimization (no PAN in responses/logs)** | Mid-Level Objective 5; Task 6 (CardResponse excludes cardToken); `security-policy.md` > Logging Redaction Rules |
| **Audit retention awareness** | Implementation Notes > Audit & Compliance (7-year retention noted as production concern) |
| **Out-of-scope boundaries** | Implementation Notes > Out of Scope (prevents unbounded monolith growth) |
| **Modular documentation for LLM efficiency** | `agents.md` > File Manifest (context-aware routing table) |
