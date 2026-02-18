# Homework 3: Specification-Driven Design

> **Student Name**: Oleksandr Stadnyk
>
> **Date Submitted**: 14 Feb 2026
>
> **AI Tools Used**: Claude Code
>
> **Task**: Design a specification package for a finance-oriented application — Virtual Card Lifecycle Management. This homework produces only documents (specification, agent rules, editor rules, and this README). No implementation is required.

---

## Rationale

### Why Virtual Card Lifecycle?

Virtual cards are a core primitive in modern FinTech. They combine several concerns that make for a rich specification exercise:

- **Sensitive data handling** — card numbers (PAN), CVVs, and PII must be protected at every layer, making PCI-DSS compliance a first-class concern in the spec.
- **Clear state machine** — cards move through well-defined states (ACTIVE → FROZEN → ACTIVE, or → CANCELLED), which forces the spec to be explicit about allowed transitions and rejection cases.
- **Auditability** — regulators require a record of who did what and when, making audit trail design a mandatory part of the specification rather than an afterthought.
- **Multiple stakeholder roles** — end-users manage their own cards while ops/compliance teams need read access across all users, which naturally drives RBAC design.
- **Financial precision** — spending limits and transactions involve money, requiring Decimal-only constraints and explicit currency handling.

### Why This Specification Structure?

The specification follows a three-tier hierarchy:

1. **High-Level Objective** — a single sentence that anchors the entire project. Every decision below must trace back to this objective.
2. **Mid-Level Objectives** — six measurable outcomes that decompose the high-level goal into testable capabilities. Each maps to a distinct functional area (card creation, state management, limits, transactions, audit, RBAC).
3. **Low-Level Tasks** — ten implementation-ready task descriptions, each with four fields (prompt, file, function, details) that an AI coding partner can execute directly. This eliminates ambiguity about what to build and where to put it.

This structure ensures **traceability**: any line of code can be traced back to a low-level task, which maps to a mid-level objective, which supports the high-level goal. If a piece of code doesn't trace back to this chain, it shouldn't exist.

### Why This Tech Stack?

- **FastAPI** — native async support, automatic OpenAPI docs, and first-class Pydantic integration make it the standard choice for modern Python APIs.
- **SQLAlchemy 2.x** — mature ORM with async support and strong typing; the repository pattern maps naturally to its session-based API.
- **PostgreSQL** — JSONB support (for audit snapshots), robust NUMERIC type for money, and ACID compliance for transactional consistency.
- **Pydantic v2** — strict validation mode prevents type coercion bugs; `ConfigDict(extra="forbid")` rejects unexpected input fields.

---

## Industry Best Practices

The table below maps each practice to where it appears in the specification package.

| Practice | Where It Appears |
|----------|-----------------|
| **PCI-DSS** (card data protection) | `specification.md` → Implementation Notes (encryption, masking); `agents.md` → Domain Rules #3, Security Constraints #1; `.claude/settings.md` → Anti-Patterns (never log PAN/CVV) |
| **GDPR / CCPA** (data privacy) | `specification.md` → Implementation Notes (data retention, right to erasure, masking); `agents.md` → Compliance Requirements #2–#4 |
| **Audit Trail** (regulatory accountability) | `specification.md` → Mid-Level Objective #5, Low-Level Task #6; `agents.md` → Domain Rules #4, Compliance #1; `.claude/settings.md` → Architectural Patterns #6 |
| **Event Sourcing** (immutable state history) | `specification.md` → Implementation Notes (event log, state projection); `agents.md` → Compliance #1 (append-only table) |
| **Decimal for Money** (financial precision) | `specification.md` → Implementation Notes, Low-Level Task #4; `agents.md` → Domain Rules #1; `.claude/settings.md` → Anti-Patterns |
| **Idempotency** (duplicate prevention) | `specification.md` → Implementation Notes, Low-Level Task #2; `agents.md` → Domain Rules #6; Low-Level Task #8 (API endpoints) |
| **RBAC** (access control) | `specification.md` → Mid-Level Objective #6, Low-Level Task #7; `agents.md` → Security Constraints #3, Compliance #6 |
| **Input Validation** (injection prevention) | `specification.md` → Implementation Notes, Low-Level Task #9; `agents.md` → Security Constraints #2; `.claude/settings.md` → Anti-Patterns |
| **Rate Limiting** (abuse prevention) | `specification.md` → Implementation Notes, Low-Level Tasks #2–#3; `agents.md` → Security Constraints #4 |
| **Formal API Contracts** (cross-team alignment) | `specification.md` → Formal Contracts section (API Contract Table, JSON Schema Contracts with example payloads and field-level constraints) |
| **State Machine Definition** (transition safety) | `specification.md` → Formal Contracts → Card State Machine (ASCII diagram + allowed transitions table) |
| **Error Catalog** (predictable failure handling) | `specification.md` → Formal Contracts → Error Catalog (14 error codes mapped to HTTP statuses with triggers) |
| **Executable Invariants** (verifiable correctness) | `specification.md` → Domain Invariants & Executable Constraints (entity invariants, pre/postconditions, cross-entity constraints, security invariants — all as GIVEN/WHEN/THEN assertions) |
| **Design by Contract** (pre/postconditions) | `specification.md` → Domain Invariants → Preconditions (8 rules) and Postconditions (8 rules) for every mutating operation |
| **Property-Based Testing** (universal quantification) | `specification.md` → Property-Based Requirements — state machine properties (4), monetary properties (5), audit properties (4), idempotency properties (2), security properties (3) — all as `FOR ALL` statements for randomized test generation |
| **Formal Verification** (mathematical contracts) | `specification.md` → Formal Verification Annotations — Hoare-triple `{P} op {Q}` contracts for every operation, typed domain model, system-wide invariants in set-theoretic notation, and a pure `authorize` decision function with monotonicity proof obligation |
| **Structured Error Handling** (security + UX) | `specification.md` → Implementation Notes, Formal Contracts → Error Catalog; `.claude/settings.md` → Error Handling section, Architectural Patterns #5 |
| **Separation of Concerns** (clean architecture) | `.claude/settings.md` → Architectural Patterns (repository, service, DTO layers); File Organization section |
| **Conventional Commits** (change tracking) | `.claude/settings.md` → Commit Messages section |
| **Dependency Pinning** (supply chain security) | `agents.md` → Security Constraints #6 |
| **KYC Gating** (regulatory compliance) | `specification.md` → Mid-Level Objective #1, Low-Level Task #2; `agents.md` → Domain Rules #7 |

---

## File Index

| File | Purpose |
|------|---------|
| `specification.md` | Full product specification with objectives, implementation notes, formal contracts, domain invariants, property-based requirements, formal verification annotations, and 10 low-level tasks |
| `agents.md` | AI agent configuration: tech stack, domain rules, code style, testing, security, compliance |
| `.claude/settings.md` | Editor/AI rules: naming, patterns, anti-patterns, error handling, file organization |
| `README.md` | This file — rationale, industry practices, and file index |
