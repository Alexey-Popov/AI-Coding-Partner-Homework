---
name: unit-tests-FIRST
description: Skill defining the FIRST principles for unit test quality. Used by the Unit Test Generator agent to ensure tests are Fast, Independent, Repeatable, Self-validating, and Timely.
type: skill
---

# Unit Tests — FIRST Principles Skill

## Purpose

This skill defines the FIRST principles for writing high-quality unit tests. The Unit Test Generator agent must apply these principles when generating tests for changed code, and must evaluate each test file against this checklist before submitting the test report.

---

## The FIRST Principles

### F — Fast

**Definition**: Tests must execute quickly. A single unit test should complete in milliseconds. The full test suite should complete in seconds, not minutes.

**Why**: Slow tests discourage frequent runs, which leads to bugs being found late.

**Pass criteria**:
- No test makes real network calls
- No test reads/writes to disk (unless mocked)
- No test has `sleep`, `setTimeout` with >10ms delay, or real I/O
- Test suite completes in <5 seconds for typical unit scope

**Fail criteria** (anti-patterns):
- Calling real HTTP endpoints
- Reading from real databases
- Using `setTimeout` or `setInterval` without mocks
- Waiting for real async I/O

---

### I — Independent

**Definition**: Tests must not depend on each other. Each test must be able to run in isolation, in any order, and produce the same result.

**Why**: Dependent tests cause cascading failures that are hard to diagnose.

**Pass criteria**:
- Each test sets up its own state in `beforeEach` or within the test itself
- No shared mutable state between tests
- Tests can run in any order or in parallel

**Fail criteria** (anti-patterns):
- One test modifies a global variable used by another test
- Tests rely on execution order (e.g., "test B must run after test A")
- Shared mock objects that accumulate state across tests

---

### R — Repeatable

**Definition**: A test must produce the same result every time it runs, regardless of environment, time, or order.

**Why**: Flaky tests erode trust in the test suite and mask real failures.

**Pass criteria**:
- No dependency on system time unless mocked
- No dependency on random values unless seeded
- No dependency on external services, APIs, or network state
- No dependency on environment-specific configuration that isn't set in the test

**Fail criteria** (anti-patterns):
- Using `new Date()` or `Date.now()` without mocking
- Using `Math.random()` without a fixed seed
- Calling real external APIs
- Tests that pass locally but fail in CI due to environment differences

---

### S — Self-validating

**Definition**: Each test must produce a clear pass or fail result automatically. No human interpretation of output should be required.

**Why**: Tests that require manual inspection are not tests — they are scripts.

**Pass criteria**:
- Every test has at least one `expect` / `assert` statement
- Assertions are specific (not just `toBeTruthy()` for complex objects)
- Test output is machine-readable (pass/fail reported by the test runner)
- No test logs output and asks the developer to "check" it manually

**Fail criteria** (anti-patterns):
- Tests with only `console.log` and no assertions
- Tests that always pass regardless of implementation
- `expect(result).toBeDefined()` as the only assertion when structure matters
- Assertions that are too loose to catch regressions

---

### T — Timely

**Definition**: Tests must be written at the right time — specifically, for new or changed code, not as an afterthought after code has been in production for months.

**Why**: Tests written before or alongside code changes catch bugs at the cheapest point.

**Pass criteria**:
- Tests are generated as part of the same pipeline run that applies the fix
- Tests cover the specific lines/functions changed by the bug fix
- Tests include a regression case that would have caught the original bug

**Fail criteria** (anti-patterns):
- Tests that only test pre-existing, unchanged behavior
- Tests submitted days or weeks after the fix
- No test that would fail against the buggy code (i.e., no regression test)

---

## FIRST Compliance Checklist

Before submitting tests, the Unit Test Generator must verify every test file against this checklist:

```
[ ] F: No real network calls, no real I/O, no sleep/delay
[ ] F: Test suite runs in <5 seconds
[ ] I: Each test uses beforeEach or local setup — no shared mutable state
[ ] I: Tests can run in any order
[ ] R: No dependency on system time, random values, or external services
[ ] R: Tests pass consistently in any environment (local and CI)
[ ] S: Every test has at least one meaningful assertion
[ ] S: Assertions are specific enough to catch regressions
[ ] T: Tests cover the changed/new code identified in fix-summary.md
[ ] T: At least one test would fail against the pre-fix (buggy) code
```

---

## How to Apply This Skill

When the Unit Test Generator agent creates tests:

1. Read `fix-summary.md` to identify exactly which files and functions were changed.
2. Generate tests targeting those functions only.
3. For each test, apply the FIRST checklist before including it.
4. In `test-report.md`, include a **FIRST Compliance Check** section with the checklist filled in for each test file.
5. Flag any principle that cannot be fully satisfied and explain why.
