# Homework 3 — Specification-Driven Design

> **Student Name**: Ivan Petrych
> **Date Submitted**: 23.03.2026
> **AI Tools Used**: [Claude Sonnet 4.6, GPT-5.1]

## Student & Task Summary

**Task**: Design a complete specification package for a finance-oriented application. No implementation is required — the deliverable is a set of documents that could be handed directly to an AI coding agent or a development team to build the product.

**Chosen domain**: Virtual Card Lifecycle Management — enabling account holders to create, control, and monitor virtual payment cards within a regulated banking environment.

### Deliverables in this folder

| File | Purpose |
|---|---|
| `specification.md` | Full product spec: high-level objective, mid-level objectives, implementation notes, context, and 8 low-level implementation tasks |
| `agents.md` | AI agent configuration: tech stack, domain rules, code style, testing, security, observability, CI/CD |
| `.github/copilot-instructions.md` | GitHub Copilot-specific rules: inline code patterns, what to prefer and what to avoid |
| `README.md` | This file — rationale and industry best practices mapping |

---

## Rationale

### Why Virtual Card Lifecycle Management?

Virtual cards represent a self-contained, bounded feature that touches every critical concern of a regulated financial system in a manageable scope:

- **Security** — card data (PAN, CVV) is maximally sensitive; handling it forces clear rules about encryption, masking, and logging boundaries.
- **Compliance** — card issuance, state changes, and spending limit enforcement all require audit trails that are mandated by PCI-DSS and AML regulations.
- **Business logic complexity** — spending limits with multiple periods (per-transaction, daily, weekly, lifetime) that must be enforced atomically and reset on schedule introduce realistic concurrency and correctness challenges.
- **Data immutability** — the transaction ledger and audit log are append-only by regulatory requirement, which demonstrates a meaningful architectural constraint.
- **Event-driven design** — notifications and audit outbox naturally lead to a Kafka-based event architecture, typical of modern banking platforms.

This scope is narrow enough to fully specify (8 tasks) but realistic enough that the specification reads as something a real engineering team would produce.

### Why this specification structure?

The specification follows a **goal → constraint → task decomposition** pattern:

1. **High-Level Objective** answers *what* the system does for stakeholders in one sentence.
2. **Mid-Level Objectives** answer *what capabilities* must exist and how they can be independently tested.
3. **Implementation Notes** capture the *how* constraints (tech choices, compliance rules) that an AI agent cannot infer from the objectives alone.
4. **Context** (beginning / ending) tells an AI agent the *starting state of the filesystem* and the *expected ending state* — this is borrowed from the [Aider](https://aider.chat) and similar tool conventions and dramatically reduces hallucinated file paths.
5. **Low-Level Tasks** each carry a **prompt**, a **file target**, a **function target**, and **key details** — structured so that an AI agent can execute each task independently without re-reading the entire spec.

This layered structure means the same document can serve multiple audiences: product managers read objectives, architects read implementation notes, and AI coding agents read low-level tasks.

---

## Industry Best Practices — Where They Appear

| # | Practice | Where in docs | Why it matters |
|---|----------|---------------|----------------|
| 1 | PCI-DSS compliance | specification.md (Security notes, Tasks 2, 3, 6); agents.md (Sensitive data handling); .github/copilot-instructions.md (Rules 2, 7) | Protects cardholder data with encryption, masking, and mTLS as required by PCI-DSS. |
| 2 | Immutable audit trail | specification.md (Compliance notes, Tasks 1, 7); agents.md (No mutation of ledger); .github/copilot-instructions.md (Rule 4) | Ensures every state change is traceable and tamper-evident for regulators and forensics. |
| 3 | Outbox pattern | specification.md (Compliance notes, Tasks 7, 8); agents.md (Audit rules) | Avoids dual-write issues by committing DB and Kafka events reliably in regulated workflows. |
| 4 | Decimal/fixed-point money | specification.md (Monetary notes, Task 2); agents.md (Monetary rules); .github/copilot-instructions.md (Rule 1) | Prevents rounding errors in financial calculations by avoiding floating-point arithmetic. |
| 5 | GDPR pseudonymization | specification.md (Compliance notes) | Balances right-to-erasure with mandatory financial record retention via pseudonymized PII. |
| 6 | AML rules | specification.md (Task 7); agents.md (AML alerts) | Detects suspicious velocity and amount anomalies, supporting AML/CTF compliance. |
| 7 | RFC 7807 errors | specification.md (Error handling notes); agents.md (Error handling); .github/copilot-instructions.md (Rule 5) | Standardises API error responses, avoids leaking internal details, and simplifies client handling. |
| 8 | Pessimistic locking | specification.md (Task 5) | Prevents race conditions when enforcing spending limits under concurrent authorizations. |
| 9 | Idempotency keys | specification.md (Error handling notes, Task 3); agents.md (Idempotency); .github/copilot-instructions.md (Fastify conventions) | Makes retries safe so network issues do not cause duplicate card issuance or limit changes. |
| 10 | Structured logging & OpenTelemetry | agents.md (Observability); .github/copilot-instructions.md (no console.log) | Enables traceable, SLO-driven operations with correlation IDs and distributed traces. |

## File Structure

```
homework-3/
├── README.md                           ← This file
├── specification.md                    ← Product/feature specification (8 tasks)
├── agents.md                           ← AI agent configuration and domain rules
├── .github/
│   └── copilot-instructions.md         ← GitHub Copilot editor rules
└── specification-TEMPLATE-example.md   ← Reference template (provided)
```
