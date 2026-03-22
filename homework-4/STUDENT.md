# Student Information

## Course

**Program**: AI Coding Partner
**Homework**: Homework 4 — 4-Agent Pipeline

## Submission

**Date**: 2026-03-09
**Branch**: main

## Project Summary

Implemented a 4-agent pipeline for automated bug fixing:

1. **Bug Research Verifier** (`agents/research-verifier.agent.md`) — verifies research accuracy using the Research Quality Measurement skill
2. **Bug Implementer** (`agents/bug-implementer.agent.md`) — applies code fixes from an implementation plan and runs tests
3. **Security Verifier** (`agents/security-verifier.agent.md`) — scans changed code for vulnerabilities and rates findings by severity
4. **Unit Test Generator** (`agents/unit-test-generator.agent.md`) — generates FIRST-compliant unit tests for changed code

**Skills created**:
- `skills/research-quality-measurement.md` — defines EXCELLENT/GOOD/ACCEPTABLE/POOR/INSUFFICIENT quality levels
- `skills/unit-tests-FIRST.md` — defines Fast/Independent/Repeatable/Self-validating/Timely test principles

**Bug fixed**: API-404 — type mismatch in `userController.js` causing `GET /api/users/:id` to always return 404.
