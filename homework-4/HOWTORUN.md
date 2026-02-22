# HOWTORUN - Homework 4 Pipeline

## Prerequisites
- Node.js 18+ (for built-in `node:test` support)
- npm
- GitHub Copilot (Chat / Agent mode) in your IDE

## Install demo app dependencies
```bash
cd homework-4/demo-bug-fix
npm install
```

## Run the demo app
```bash
npm start
```

### Manual verification (before/after fix behavior)
```bash
curl http://localhost:3000/health
curl http://localhost:3000/api/users
curl http://localhost:3000/api/users/123
curl http://localhost:3000/api/users/not-a-number
```

## Run unit tests
From the repository root:
```bash
node --test homework-4/demo-bug-fix/tests/*.test.js
```

Or from inside `homework-4/demo-bug-fix`:
```bash
node --test tests/*.test.js
```

## Running the 6-stage pipeline in GitHub Copilot

Agent specs live in `.github/agents/`. Run them in sequence, attaching the listed inputs. Each agent reads the corresponding `context/bugs/XXX/` template as a read-only reference and writes output to `context/bugs/API-404/`.

### 1. Bug Researcher
- Spec: `.github/agents/bug-researcher.agent.md`
- Inputs:
  - `demo-bug-fix/bugs/API-404/bug-context.md`
  - demo app source in `demo-bug-fix/src/`
- Output:
  - `context/bugs/API-404/research/codebase-research.md`

### 2. Research Verifier
- Spec: `.github/agents/research-verifier.agent.md`
- Inputs:
  - `context/bugs/API-404/research/codebase-research.md`
  - `.github/skills/research-quality-measurement/SKILL.md`
  - referenced code files
- Output:
  - `context/bugs/API-404/research/verified-research.md`

### 3. Bug Planner
- Spec: `.github/agents/bug-planner.agent.md`
- Inputs:
  - `context/bugs/API-404/research/verified-research.md`
- Output:
  - `context/bugs/API-404/implementation-plan.md`

### 4. Bug Implementer
- Spec: `.github/agents/bug-implementer.agent.md`
- Inputs:
  - `context/bugs/API-404/implementation-plan.md`
  - `context/bugs/API-404/research/verified-research.md`
- Outputs:
  - code changes in `demo-bug-fix/src/`
  - `context/bugs/API-404/fix-summary.md`

### 5. Security Vulnerabilities Verifier
- Spec: `.github/agents/security-verifier.agent.md`
- Inputs:
  - `context/bugs/API-404/fix-summary.md`
  - changed files
- Output:
  - `context/bugs/API-404/security-report.md`

### 6. Unit Test Generator
- Spec: `.github/agents/unit-test-generator.agent.md`
- Inputs:
  - `context/bugs/API-404/fix-summary.md`
  - `.github/skills/unit-tests-first/SKILL.md`
  - changed files
- Outputs:
  - test files in `demo-bug-fix/tests/`
  - `context/bugs/API-404/test-report.md`

## Screenshots for submission
Capture and place screenshots in `docs/screenshots/` for:
- pipeline run
- bug fix evidence
- security verification run
- unit test run
