# Screenshots — Capture Instructions

Place all screenshots in this directory (`docs/screenshots/`).

## Required Screenshots

### 1. Pipeline Run — Research Verifier
**File**: `docs/screenshots/01-research-verifier.png`

Capture the Claude Code session showing:
- The Research Verifier agent reading `codebase-research.md`
- Verification of each file:line reference
- The quality score and level being written to `verified-research.md`

### 2. Pipeline Run — Bug Implementer
**File**: `docs/screenshots/02-bug-implementer.png`

Capture the Claude Code session showing:
- The Bug Implementer reading `implementation-plan.md`
- The code change being applied to `userController.js`
- The test run output (`npm test` — 5 tests passing)

### 3. Applied Fix — Code Diff
**File**: `docs/screenshots/03-fix-applied.png`

Capture a diff view or editor showing:
- Before: `const userId = req.params.id;` (string, causes 404)
- After: `const userId = parseInt(req.params.id, 10);` + NaN guard

### 4. Security Scan — Report
**File**: `docs/screenshots/04-security-report.png`

Capture the Claude Code session showing:
- The Security Verifier reading `fix-summary.md`
- Checks being performed on the changed code
- The findings summary (0 critical/high, 1 low, 2 info)

### 5. Unit Test Execution
**File**: `docs/screenshots/05-unit-tests.png`

Capture the terminal showing:
```
PASS tests/userController.test.js
  getUserById
    ✓ returns user object with 200 when numeric ID matches
    ✓ returns 404 when user ID does not exist
    ✓ REGRESSION: string "123" now correctly finds user
    ✓ returns 400 for non-numeric string ID
    ✓ returns 400 for empty string ID

Tests: 5 passed, 5 total
```

### 6. Manual API Verification (Optional)
**File**: `docs/screenshots/06-api-verification.png`

Capture terminal output of:
```bash
curl http://localhost:3000/api/users/123
# {"id":123,"name":"Alice Smith","email":"alice@example.com"}
```

---

## How to Capture

1. Run each pipeline step in Claude Code
2. Use your OS screenshot tool:
   - macOS: `Cmd+Shift+4` for area selection
   - Linux: `PrtSc` or `gnome-screenshot`
   - Windows: `Win+Shift+S`
3. Save screenshots with the filenames listed above into this directory
