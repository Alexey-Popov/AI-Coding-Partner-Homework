---
name: unit-tests-first
description: FIRST rubric (Fast, Independent, Repeatable, Self-validating, Timely) for generating and reviewing unit tests for changed code only. Use for unit test generation, test quality review, FIRST assessment, test-report.
---

# Unit Tests FIRST Skill

Use this skill to design and review unit tests for changed code only.

## FIRST Rubric

### F - Fast
- No network access
- No slow sleeps/retries
- Prefer direct function invocation over full app startup

### I - Independent
- Fresh stubs/fixtures per test
- No order dependence
- No leaked mutable global state

### R - Repeatable
- Deterministic inputs
- No wall-clock dependency
- No uncontrolled randomness

### S - Self-validating
- Explicit assertions
- Automatic pass/fail with no manual inspection

### T - Timely
- Covers the reported bug regression now
- Covers the changed branches only
- Avoids unrelated legacy coverage expansion

## Checklist
- Add a regression test for the original bug
- Add success-path coverage for the fixed behavior
- Add negative-path tests only for validation introduced by the fix
- Keep tests deterministic and isolated

## Reporting Requirement
`test-report.md` must include a FIRST assessment with evidence and pass/fail for each letter.
