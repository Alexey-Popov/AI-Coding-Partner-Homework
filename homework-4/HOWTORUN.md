# How to Run — Homework 4: 4-Agent Pipeline

---

## Prerequisites

| Tool | Minimum Version | Check command |
|------|----------------|---------------|
| Node.js | 18.x | `node --version` |
| npm | 9.x | `npm --version` |

---

## Part 1: Run the Demo Application

### Step 1: Install dependencies

```bash
cd homework-4/demo-bug-fix
npm install
```

### Step 2: Start the server

```bash
npm start
```

Expected output:
```
Server running on http://localhost:3000
Try: GET http://localhost:3000/api/users
     GET http://localhost:3000/api/users/:id
```

### Step 3: Test the API endpoints

Open a new terminal and run:

**List all users (was working before fix):**
```bash
curl http://localhost:3000/api/users
```
Expected:
```json
[
  {"id":123,"name":"Alice Smith","email":"alice@example.com"},
  {"id":456,"name":"Bob Johnson","email":"bob@example.com"},
  {"id":789,"name":"Charlie Brown","email":"charlie@example.com"}
]
```

**Get single user by ID (fixed by bug fix):**
```bash
curl http://localhost:3000/api/users/123
```
Expected:
```json
{"id":123,"name":"Alice Smith","email":"alice@example.com"}
```

**Verify 404 for unknown IDs:**
```bash
curl http://localhost:3000/api/users/999
```
Expected (HTTP 404):
```json
{"error":"User not found"}
```

**Health check:**
```bash
curl http://localhost:3000/health
```

---

## Part 2: Run the Unit Tests

### Step 1: Navigate to the app directory

```bash
cd homework-4/demo-bug-fix
```

### Step 2: Run Jest

```bash
npm test
```

Expected output:
```
PASS tests/users.test.js
  getUserById
    ✓ returns user for valid ID 123
    ✓ returns user for valid ID 456
    ✓ returns user for valid ID 789
    ✓ returns 404 for non-existent ID 999
    ✓ returns 404 for non-numeric ID "abc"
    ✓ [REGRESSION] string "123" resolves to user (API-404 fix)
  getAllUsers
    ✓ returns all 3 users
    ✓ each user has id, name, and email

Tests: 8 passed, 8 total
```

---

## Part 3: Understanding the 4-Agent Pipeline

The agents in this pipeline are **definition documents** (prompt templates) designed to be executed by an AI assistant (e.g., Claude Code). To "run" the pipeline:

### Agent 1: Research Verifier

1. Open `agents/research-verifier.agent.md`
2. Provide it with `context/bugs/API-404/research/codebase-research.md` as input
3. The agent verifies all file:line references and scores quality using `skills/research-quality-measurement.md`
4. Output: `context/bugs/API-404/research/verified-research.md`

### Agent 2: Bug Implementer

1. Open `agents/bug-implementer.agent.md`
2. Provide it with `context/bugs/API-404/implementation-plan.md` as input
3. The agent applies the changes and runs tests
4. Output: `context/bugs/API-404/fix-summary.md` + modified source files

### Agent 3: Security Verifier

1. Open `agents/security-verifier.agent.md`
2. Provide it with `context/bugs/API-404/fix-summary.md` and changed files as input
3. The agent scans for security issues (report only — no code changes)
4. Output: `context/bugs/API-404/security-report.md`

### Agent 4: Unit Test Generator

1. Open `agents/unit-test-generator.agent.md`
2. Provide it with `context/bugs/API-404/fix-summary.md` and changed files as input
3. The agent generates tests following `skills/unit-tests-FIRST.md` and runs them
4. Output: `demo-bug-fix/tests/users.test.js` + `context/bugs/API-404/test-report.md`

### Pre-generated artifacts

All agent outputs are pre-generated and available in `context/bugs/API-404/`:

| Artifact | Location |
|---------|---------|
| Codebase research | `context/bugs/API-404/research/codebase-research.md` |
| Verified research | `context/bugs/API-404/research/verified-research.md` |
| Implementation plan | `context/bugs/API-404/implementation-plan.md` |
| Fix summary | `context/bugs/API-404/fix-summary.md` |
| Security report | `context/bugs/API-404/security-report.md` |
| Test report | `context/bugs/API-404/test-report.md` |

---

## Troubleshooting

### `npm install` fails

- Ensure Node.js 18+ is installed: `node --version`
- Delete `node_modules/` and `package-lock.json`, then re-run `npm install`

### Port 3000 already in use

```bash
# Windows
netstat -ano | findstr :3000
taskkill /PID <PID> /F

# Linux/Mac
lsof -ti:3000 | xargs kill
```

Or change the port:
```bash
PORT=3001 npm start
```

### Tests fail

- Ensure `npm install` was run first (Jest must be installed)
- Check Node.js version: `node --version` (must be 18+)
- Run a single test in verbose mode: `npx jest --verbose`
