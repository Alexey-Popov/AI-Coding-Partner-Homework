# Student Submission — Homework 4

## Student Information

- **Student Name**: Yevgen Polukov
- **Course**: AI Coding Partner / Multi-Agent Systems
- **Homework**: Homework 4 — 4-Agent Bug Fixing Pipeline
- **Submission Date**: 2026-03-15
- **Repository Branch**: `homework-4`

---

## Submission Checklist

### Required Agents
- [x] `agents/research-verifier.agent.md`
- [x] `agents/bug-implementer.agent.md`
- [x] `agents/security-verifier.agent.md`
- [x] `agents/unit-test-generator.agent.md`

### Required Skills
- [x] `skills/research-quality-measurement.md`
- [x] `skills/unit-tests-FIRST.md`

### Pipeline Artifacts
- [x] `context/bugs/API-404/research/codebase-research.md`
- [x] `context/bugs/API-404/research/verified-research.md`
- [x] `context/bugs/API-404/implementation-plan.md`
- [x] `context/bugs/API-404/fix-summary.md`
- [x] `context/bugs/API-404/security-report.md`
- [x] `context/bugs/API-404/test-report.md`

### Application
- [x] Bug fix applied: `demo-bug-fix/src/controllers/userController.js`
- [x] Unit tests generated: `demo-bug-fix/tests/userController.test.js`
- [x] Tests pass: 5/5

### Documentation
- [x] `README.md` — overview, pipeline diagram, how to run
- [x] `HOWTORUN.md` — step-by-step commands
- [x] `STUDENT.md` — this file
- [x] `docs/screenshots/README.md` — screenshot instructions

---

## Bug Fixed

**Bug ID**: API-404
**Title**: GET /api/users/:id returns 404 for valid user IDs
**Root Cause**: Type coercion mismatch — `req.params.id` is a string, user IDs are numbers, strict `===` always fails
**Fix**: `parseInt(req.params.id, 10)` + `isNaN` guard for invalid input

---
