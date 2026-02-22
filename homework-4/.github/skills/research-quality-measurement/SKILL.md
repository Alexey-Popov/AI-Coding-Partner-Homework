---
name: research-quality-measurement
description: Rubric for verifying codebase bug research quality before implementation. Use for research verification, claim accuracy, file:line reference checking, RQ scoring, codebase-research review.
---

# Research Quality Measurement Skill

Use this skill when verifying a `codebase-research.md` artifact before planning or implementation.

## Evaluation Dimensions
Score each dimension as `Pass`, `Partial`, or `Fail`.

1. `Reference Accuracy`
- File paths exist
- Line references point to the cited code
- Snippets match source (formatting-only drift is acceptable)

2. `Claim Correctness`
- Technical interpretation is true
- Root cause is correctly stated
- Unsupported assumptions are avoided

3. `Coverage Sufficiency`
- Failing path and adjacent relevant files are covered
- Exact change location(s) are identified
- Enough detail exists to plan without re-research

4. `Actionability`
- Planner can draft `implementation-plan.md` directly
- Risks/constraints/test points are identified

## Quality Levels
- `RQ-4 (Excellent)`: all dimensions pass, no material discrepancies
- `RQ-3 (Good)`: minor non-blocking discrepancies only
- `RQ-2 (Fair)`: at least one partial dimension; planner may proceed with verifier corrections
- `RQ-1 (Poor)`: blocking inaccuracies or insufficient coverage; re-research required
- `RQ-0 (Invalid)`: missing/unusable/unverifiable artifact; planning must not proceed

## Decision Rules
- Fabricated snippet or nonexistent reference => at most `RQ-1`
- Wrong root cause => at most `RQ-1`
- Missing exact fix location but correct diagnosis => at most `RQ-2`

## Required Verifier Output Usage
`verified-research.md` must include:
- Final `RQ-*` level
- Dimension-by-dimension reasoning
- Whether planning may proceed (`Yes`/`No`)
