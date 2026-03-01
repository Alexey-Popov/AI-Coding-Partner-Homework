# Homework 4: 4-Agent Pipeline for Bug Fixing

**Student**: Mykhailo Bestiuk
**Course**: AI Coding Partner
**Assignment**: Build a multi-agent pipeline to research, verify, fix, secure-review, and test a bug

---

## Overview

This homework implements a **4-agent pipeline** that autonomously handles the full bug-fixing workflow — from research verification through implementation, security review, and unit test generation. The pipeline is applied to a Node.js/Express API with a documented type-mismatch bug (API-404).

### Pipeline Flow

```
Bug Research
    │
    ▼
┌─────────────────────┐
│  1. Research        │  Verifies research accuracy & quality
│     Verifier        │  → verified-research.md
└─────────────────────┘
    │
    ▼
Bug Planner (manual)
→ implementation-plan.md
    │
    ▼
┌─────────────────────┐
│  2. Bug             │  Applies the fix, runs tests
│     Implementer     │  → fix-summary.md
└─────────────────────┘
    │
    ├──────────────────────────────┐
    ▼                              ▼
┌─────────────────────┐  ┌─────────────────────┐
│  3. Security        │  │  4. Unit Test        │
│     Verifier        │  │     Generator        │
│  (report only)      │  │  (tests + report)    │
│  → security-report  │  │  → test-report       │
└─────────────────────┘  └─────────────────────┘
```

---

## The Bug (API-404)

**Symptom**: `GET /api/users/:id` returns 404 for all valid user IDs.

**Root cause**: Express URL parameters (`req.params.id`) are always strings. The users array stores numeric IDs. Strict equality (`===`) never matches `"123"` to `123`.

**Fix**: `parseInt(req.params.id, 10)` on line 19 of `userController.js`.

---

## Repository Structure

```
homework-4/
├── README.md                          # This file
├── HOWTORUN.md                        # How to run the app and pipeline
├── STUDENT.md                         # Student information
│
├── agents/                            # Agent definition files
│   ├── research-verifier.agent.md     # Verifies bug research quality
│   ├── bug-implementer.agent.md       # Applies fixes from implementation plan
│   ├── security-verifier.agent.md     # Security review (report only)
│   └── unit-test-generator.agent.md   # Generates unit tests for changed code
│
├── skills/                            # Reusable skill definitions
│   ├── research-quality-measurement.md  # Quality levels + weighted scoring
│   └── unit-tests-FIRST.md              # FIRST testing criteria
│
├── context/bugs/API-404/              # Pipeline artifacts for the bug
│   ├── research/
│   │   ├── codebase-research.md       # Bug researcher output
│   │   └── verified-research.md       # Verifier output (EXCELLENT, 4.7/5.0)
│   ├── implementation-plan.md         # Single-line fix plan
│   ├── fix-summary.md                 # Implementer output (SUCCESS)
│   ├── security-report.md             # 0 CRITICAL, 0 HIGH, 1 MEDIUM, 1 LOW
│   └── test-report.md                 # 8/8 tests pass, FIRST compliant
│
├── demo-bug-fix/                      # The application with bug fix applied
│   ├── server.js
│   ├── src/
│   │   ├── controllers/userController.js  # Fixed: parseInt on line 19
│   │   └── routes/users.js
│   ├── tests/
│   │   └── users.test.js              # 8 unit tests (Jest)
│   └── package.json                   # Includes jest devDependency
│
└── docs/screenshots/                  # Pipeline run screenshots
```

---

## Agents

| Agent | File | Role | Input → Output |
|-------|------|------|----------------|
| Research Verifier | [agents/research-verifier.agent.md](agents/research-verifier.agent.md) | Fact-checks research, scores quality | `codebase-research.md` → `verified-research.md` |
| Bug Implementer | [agents/bug-implementer.agent.md](agents/bug-implementer.agent.md) | Applies fixes, runs tests | `implementation-plan.md` → `fix-summary.md` |
| Security Verifier | [agents/security-verifier.agent.md](agents/security-verifier.agent.md) | Security scan (report only) | `fix-summary.md` + changed files → `security-report.md` |
| Unit Test Generator | [agents/unit-test-generator.agent.md](agents/unit-test-generator.agent.md) | Generates FIRST-compliant tests | `fix-summary.md` + changed files → `users.test.js` + `test-report.md` |

## Skills

| Skill | File | Used by |
|-------|------|---------|
| Research Quality Measurement | [skills/research-quality-measurement.md](skills/research-quality-measurement.md) | Research Verifier |
| Unit Tests FIRST | [skills/unit-tests-FIRST.md](skills/unit-tests-FIRST.md) | Unit Test Generator |

---

## Pipeline Results

| Step | Agent | Status | Key Output |
|------|-------|--------|-----------|
| 1 | Research Verifier | PASS | EXCELLENT quality (4.7/5.0) |
| 2 | Bug Implementer | SUCCESS | 1 line changed, all tests pass |
| 3 | Security Verifier | COMPLETE | 0 CRITICAL, 0 HIGH, 1 MEDIUM (validation gap) |
| 4 | Unit Test Generator | PASS | 8/8 tests pass, FIRST compliant |

---

## Quick Start

```bash
# Install and run the application
cd demo-bug-fix
npm install
npm start

# Test the fix
curl http://localhost:3000/api/users/123
# → {"id":123,"name":"Alice Smith","email":"alice@example.com"}

# Run unit tests
npm test
# → 8/8 tests pass
```

See [HOWTORUN.md](HOWTORUN.md) for detailed instructions.
