# How to Run the 4-Agent Pipeline

## Prerequisites

- Claude Code CLI installed and configured
- Node.js 18+ and npm
- Working directory: `homework-4/`

---

## Step 0: Install Application Dependencies

```bash
cd demo-bug-fix
npm install
cd ..
```

---

## Step 1: Run the Bug Research Verifier Agent

This agent verifies the accuracy of `context/bugs/API-404/research/codebase-research.md`.

```bash
claude --agent agents/research-verifier.agent.md \
  --context context/bugs/API-404/research/codebase-research.md \
  --working-dir .
```

Or open Claude Code and provide the agent file as instructions. The agent will:
1. Read `context/bugs/API-404/research/codebase-research.md`
2. Verify every file:line reference against actual source files
3. Apply `skills/research-quality-measurement.md` to rate quality
4. Write `context/bugs/API-404/research/verified-research.md`

**Expected output**: `context/bugs/API-404/research/verified-research.md` with quality rating GOOD or higher.

---

## Step 2: Review the Verified Research

Open `context/bugs/API-404/research/verified-research.md` and confirm:
- Overall Status: PASS
- Research Quality: ACCEPTABLE or above
- No critical discrepancies

If quality is POOR or INSUFFICIENT, re-run Bug Researcher before proceeding.

---

## Step 3: Run the Bug Implementer Agent

This agent applies the fix from `context/bugs/API-404/implementation-plan.md`.

```bash
claude --agent agents/bug-implementer.agent.md \
  --context context/bugs/API-404/implementation-plan.md \
  --working-dir .
```

The agent will:
1. Read `context/bugs/API-404/implementation-plan.md`
2. Apply the code change to `demo-bug-fix/src/controllers/userController.js:23`
3. Run `npm test` (if tests exist)
4. Write `context/bugs/API-404/fix-summary.md`

**Expected output**: `context/bugs/API-404/fix-summary.md` with status SUCCESS.

---

## Step 4: Run the Security Verifier Agent

This agent reviews the changed code for security vulnerabilities.

```bash
claude --agent agents/security-verifier.agent.md \
  --context context/bugs/API-404/fix-summary.md \
  --working-dir .
```

The agent will:
1. Read `context/bugs/API-404/fix-summary.md`
2. Open and review all changed files
3. Scan for injection, secrets, insecure comparisons, missing validation, XSS/CSRF
4. Write `context/bugs/API-404/security-report.md`

**Expected output**: `context/bugs/API-404/security-report.md` with no CRITICAL/HIGH findings.

---

## Step 5: Run the Unit Test Generator Agent

This agent creates and runs unit tests for the changed code.

```bash
claude --agent agents/unit-test-generator.agent.md \
  --context context/bugs/API-404/fix-summary.md \
  --working-dir .
```

The agent will:
1. Read `context/bugs/API-404/fix-summary.md`
2. Review changed files
3. Generate tests following FIRST principles (`skills/unit-tests-FIRST.md`)
4. Write test files to `tests/`
5. Run `npm test`
6. Write `context/bugs/API-404/test-report.md`

**Expected output**: All tests pass; `context/bugs/API-404/test-report.md` shows 100% pass rate.

---

## Step 6: Verify the Application

After the pipeline completes, manually verify the fix:

```bash
cd demo-bug-fix
npm start
```

In another terminal:

```bash
# Should return user object (was broken before fix)
curl http://localhost:3000/api/users/123

# Should still return all users
curl http://localhost:3000/api/users

# Should return 404 (user does not exist)
curl http://localhost:3000/api/users/999
```

---

## Run Tests Directly

```bash
cd demo-bug-fix
npm test
```

---

## Agent Artifacts Location

After running the full pipeline:

```
context/bugs/API-404/
├── research/verified-research.md   ← Step 1 output
├── fix-summary.md                  ← Step 3 output
├── security-report.md              ← Step 4 output
└── test-report.md                  ← Step 5 output

tests/
└── userController.test.js          ← Step 5 generated test file
```

---

## Troubleshooting

| Issue | Resolution |
|-------|-----------|
| Research quality is POOR | Re-run Bug Researcher; do not proceed to Bug Implementer |
| Bug Implementer stops with FAILED status | Check fix-summary.md for error details; verify implementation-plan.md is correct |
| Security report has CRITICAL findings | Address findings before merging; do not ship with CRITICAL issues |
| Tests fail after fix | Check test-report.md for details; ensure bug fix was applied correctly |
