# How to Run — Homework 4

## Prerequisites

- Node.js 18+ installed
- npm 9+ installed
- Claude Code CLI installed (`npm install -g @anthropic-ai/claude-code`)

---

## Setup

```bash
cd homework-4/demo-bug-fix
npm install
```

---

## Run the App

```bash
cd homework-4/demo-bug-fix
npm start
# → Server running at http://localhost:3000
```

---

## Run Tests

```bash
cd homework-4/demo-bug-fix
npm test
# → Jest runs tests/userController.test.js
# → Expected: 5 tests pass
```

---

## Run the Pipeline (Agent by Agent)

All agents are in `homework-4/agents/`. Each is a Claude Code agent markdown file.

From the `homework-4/` directory, run each step in order:

### Step 1 — Bug Research Verifier
```bash
# Open Claude Code in homework-4/
claude
# Then reference the agent:
# @agents/research-verifier.agent.md
# It reads:  context/bugs/API-404/research/codebase-research.md
# It writes: context/bugs/API-404/research/verified-research.md
```

### Step 2 — Bug Implementer
```bash
# @agents/bug-implementer.agent.md
# It reads:  context/bugs/API-404/implementation-plan.md
# It writes: context/bugs/API-404/fix-summary.md
# It modifies: demo-bug-fix/src/controllers/userController.js
# It runs: npm test
```

### Step 3 — Security Verifier
```bash
# @agents/security-verifier.agent.md
# It reads:  context/bugs/API-404/fix-summary.md
# It reads:  demo-bug-fix/src/controllers/userController.js
# It writes: context/bugs/API-404/security-report.md
```

### Step 4 — Unit Test Generator
```bash
# @agents/unit-test-generator.agent.md
# It reads:  context/bugs/API-404/fix-summary.md
# It reads:  demo-bug-fix/src/controllers/userController.js
# It writes: demo-bug-fix/tests/userController.test.js
# It writes: context/bugs/API-404/test-report.md
# It runs:   npm test
```

---

## Where Outputs Appear

| Output | Path |
|--------|------|
| Verified research | `context/bugs/API-404/research/verified-research.md` |
| Fix summary | `context/bugs/API-404/fix-summary.md` |
| Security report | `context/bugs/API-404/security-report.md` |
| Test report | `context/bugs/API-404/test-report.md` |
| Unit tests | `demo-bug-fix/tests/userController.test.js` |

---

## Manual API Verification

After `npm start`:

```bash
# Valid user — should return 200 + user object
curl http://localhost:3000/api/users/123

# Non-existent user — should return 404
curl http://localhost:3000/api/users/999

# Non-numeric ID — should return 400 (new NaN guard)
curl http://localhost:3000/api/users/abc

# All users — should return 200 + array
curl http://localhost:3000/api/users

# Health check
curl http://localhost:3000/health
```
