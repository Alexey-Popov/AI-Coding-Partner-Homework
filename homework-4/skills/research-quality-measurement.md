---
name: research-quality-measurement
description: Skill for measuring and rating the quality of bug research output. Defines quality levels, criteria, and how to apply them when verifying codebase research.
type: skill
---

# Research Quality Measurement Skill

## Purpose

This skill provides a structured rubric for evaluating the quality of bug research output produced by a Bug Researcher agent. It defines quality levels, scoring criteria, and instructions for how to apply the rubric when producing a `verified-research.md` document.

---

## Quality Levels

| Level | Label | Score Range | Meaning |
|-------|-------|-------------|---------|
| 5 | **EXCELLENT** | 90–100% | All references verified, all snippets match source exactly, claims are fully grounded, planner can act immediately |
| 4 | **GOOD** | 75–89% | Most references verified, minor inaccuracies, claims mostly grounded, planner can act with small caveats |
| 3 | **PARTIAL** | 50–74% | Some references missing or wrong, some claims unverified, planner needs additional verification |
| 2 | **WEAK** | 25–49% | Many references wrong or missing, significant unverified claims, planner cannot act safely |
| 1 | **FAIL** | 0–24% | Research is unreliable, references fabricated or absent, planner must not use this research without full redo |

---

## Scoring Criteria (5 Dimensions, 20 points each)

### 1. Reference Accuracy (20 pts)
- **20**: Every file:line reference exists and is correct
- **15**: ≤10% of references have minor line offset errors (±3 lines)
- **10**: 11–30% of references are wrong or missing
- **5**: 31–60% of references are wrong or missing
- **0**: >60% of references are wrong, missing, or fabricated

### 2. Snippet Accuracy (20 pts)
- **20**: Every quoted code snippet matches the source file exactly
- **15**: Minor whitespace/formatting differences only
- **10**: Some snippets partially match (wrong variable names or slight rewording)
- **5**: Most snippets are paraphrased or significantly different
- **0**: Snippets are invented or completely wrong

### 3. Completeness (20 pts)
- **20**: All relevant files, functions, and call paths related to the bug are covered
- **15**: One or two relevant files or paths missing but core bug is described
- **10**: Root cause identified but supporting context incomplete
- **5**: Only surface-level description, missing root cause analysis
- **0**: Bug description is absent or entirely off-target

### 4. Traceability (20 pts)
- **20**: Every claim links to a specific file, function, and line
- **15**: Most claims traced; a few general statements without exact location
- **10**: About half the claims have traceable references
- **5**: Few claims are traceable; mostly narrative
- **0**: No traceability; claims are assertions without evidence

### 5. Planner Usability (20 pts)
- **20**: Output contains enough detail for Bug Planner to write an implementation plan without additional research
- **15**: Output is mostly usable; one area needs clarification
- **10**: Output partially usable; planner needs to re-research some aspects
- **5**: Output provides hints but planner must do most research again
- **0**: Output is not actionable for planning

---

## How to Apply This Skill

When the Research Verifier agent evaluates `codebase-research.md`, it must:

1. **For each claim** in the research document:
   - Locate the referenced file and line in the actual codebase
   - Compare the quoted snippet against the actual source
   - Mark as: `VERIFIED`, `PARTIALLY_VERIFIED`, or `UNVERIFIED`

2. **Score each of the 5 dimensions** using the rubric above.

3. **Compute overall score**: sum all dimension scores (max 100).

4. **Assign quality level** based on score range from the table above.

5. **Record all discrepancies** with:
   - What was claimed
   - What was found
   - Impact on planner usability

6. **Write the Research Quality Assessment section** in `verified-research.md` including:
   - Numeric score per dimension
   - Total score
   - Quality level label (e.g., `GOOD`)
   - Pass/Fail decision (PASS if level ≥ PARTIAL; FAIL if WEAK or FAIL)
   - Brief reasoning

---

## Pass/Fail Threshold

| Quality Level | Pipeline Decision |
|---------------|------------------|
| EXCELLENT | PASS — proceed to Bug Planner immediately |
| GOOD | PASS — proceed with documented caveats |
| PARTIAL | CONDITIONAL PASS — planner must acknowledge gaps |
| WEAK | FAIL — request re-research before planning |
| FAIL | FAIL — halt pipeline; research must be redone |
