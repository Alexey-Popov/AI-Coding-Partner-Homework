# Homework 3: Specification-Driven Design

> **Student**: Mykhailo Bestiuk
> **Date**: 2026-02-07
> **Domain**: Virtual Card Lifecycle Management

---

## Summary

This homework delivers a **specification package** (no code) for a Virtual Card Lifecycle Management microservice in a regulated FinTech environment. The package includes:

| Deliverable | File | Purpose |
|-------------|------|---------|
| Full specification | `specification.md` | High/mid/low-level objectives + 18 implementation tasks |
| Agent guidelines | `agents.md` | AI coding partner configuration for this domain |
| Editor rules | `.github/copilot-instructions.md` | Copilot-specific ALWAYS/NEVER rules |
| This README | `README.md` | Rationale and industry practice mapping |

---

## Rationale

### Why Virtual Card Lifecycle?

I chose this domain because it **maximizes the surface area** for demonstrating FinTech best practices:

1. **State machine complexity** — 4 states, 5 transitions, strict invariants. This is richer than simpler features (spending caps = on/off) and forces the spec to define clear rules for every edge case.

2. **Compliance surface** — Virtual cards touch PCI-DSS (PAN handling), PSD2 (authentication), and GDPR (cardholder data). This naturally produces a deep `agents.md` and rich compliance test suite.

3. **Audit trail richness** — Every state transition, limit change, and admin action generates an audit event. The immutable-log requirement drives a distinctive design pattern (append-only table, no-update guards).

4. **Multi-stakeholder** — End-users (cardholders) and ops/compliance have different views and permissions, which exercises RBAC design in the spec.

### Why Compliance-First Structure?

The specification is organized **compliance-first**: regulatory requirements (PCI-DSS, GDPR, audit) appear in the mid-level objectives before feature logic. This mirrors how regulated financial institutions actually design systems — compliance is not an afterthought bolted on at the end.

Benefits of this approach:
- Every low-level task inherits compliance context from its parent objective.
- AI agents reading this spec will embed security/audit behavior from the start rather than retrofitting it.
- The spec is auditable itself — a compliance officer can trace each regulation to specific tasks.

### Why 18 Low-Level Tasks?

Each task is scoped to produce 1–3 files and follows the template format (`prompt → file → function → details`). This level of granularity means an AI agent can execute each task independently without needing full-project context, while the ordering ensures dependencies are satisfied.

---

## Industry Best Practices

The following practices are embedded in the specification. Each row links the practice to its specific location across the deliverable files.

| # | Practice | Regulation / Standard | Where It Appears |
|---|----------|----------------------|------------------|
| 1 | **PAN tokenization** (never store raw card numbers) | PCI-DSS v4.0, Req 3.4 | `specification.md` MO-1, Task 3; `agents.md` §2 Domain Rules; `.github/copilot-instructions.md` NEVER rules |
| 2 | **PAN masking** in all outputs (last 4 only) | PCI-DSS v4.0, Req 3.4 | `specification.md` MO-1, Task 3, Task 18; `agents.md` §2; `.github/copilot-instructions.md` ALWAYS rules |
| 3 | **Immutable audit trail** (append-only, no UPDATE/DELETE) | PCI-DSS v4.0, Req 10.2; SOX | `specification.md` MO-2, Task 4, Task 6; `agents.md` §6 Compliance; `.github/copilot-instructions.md` NEVER rules |
| 4 | **Decimal for money** (never float) | IEEE 754 rounding avoidance; FinTech industry standard | `specification.md` MO-4, Task 5; `agents.md` §2; `.github/copilot-instructions.md` ALWAYS/NEVER rules |
| 5 | **GDPR right to erasure** with pseudonymization | GDPR Art. 17 | `specification.md` MO-7, Task 13, Task 18; `agents.md` §6 GDPR |
| 6 | **GDPR data portability** (export endpoint) | GDPR Art. 20 | `specification.md` MO-7, Task 13 |
| 7 | **Role-Based Access Control** (cardholder vs ops) | Principle of least privilege; SOC 2 | `specification.md` MO-6, Task 7, Task 18; `agents.md` §5 Security |
| 8 | **JWT authentication** on every request | OWASP API Security Top 10 | `specification.md` Task 7; `agents.md` §5; `.github/copilot-instructions.md` ALWAYS rules |
| 9 | **Security headers** (HSTS, CSP, X-Frame-Options) | OWASP Secure Headers | `specification.md` Task 15; `agents.md` §5 |
| 10 | **Rate limiting** on sensitive endpoints | OWASP API4:2023 (unrestricted resource consumption) | `specification.md` Task 7, Task 18; `agents.md` §5 |
| 11 | **Structured JSON logging** (no PII) | PCI-DSS Req 10; SIEM ingestion standard | `specification.md` Implementation Notes; `agents.md` §6 Regulatory Logging |
| 12 | **Parameterized queries** (no SQL injection) | OWASP A03:2021 (injection) | `agents.md` §5; `.github/copilot-instructions.md` ALWAYS/NEVER rules |
| 13 | **Secrets management** (env vars, not source) | CIS Benchmark; 12-Factor App | `specification.md` Task 1; `agents.md` §5; `.github/copilot-instructions.md` NEVER rules |
| 14 | **Standard error envelope** (no stack trace leaks) | OWASP; defense in depth | `specification.md` Task 14; `agents.md` §3; `.github/copilot-instructions.md` NEVER rules |
| 15 | **Data retention policy** (configurable archival) | GDPR Art. 5(1)(e); financial record-keeping regulations | `specification.md` MO-7; `agents.md` §6 Data Retention |
| 16 | **Idempotent operations** | REST best practices; payment industry standard | `specification.md` MO-4 (limit upsert); API design notes |

---

## Files in This Submission

```
homework-3/
├── README.md                        ← this file
├── specification.md                 ← 18-task specification (compliance-first)
├── agents.md                        ← AI agent guidelines (6 sections)
├── .github/
│   └── copilot-instructions.md      ← editor/AI rules (ALWAYS/NEVER format)
├── specification-TEMPLATE-example.md← provided template (reference only)
└── TASKS.md                         ← assignment instructions (reference only)
```
