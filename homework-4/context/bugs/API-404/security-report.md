# Security Report

**Bug**: API-404 — GET /api/users/:id type mismatch fix
**Reviewed by**: Security Verifier Agent
**Date**: 2026-03-01
**Files reviewed**: `demo-bug-fix/src/controllers/userController.js`

---

## Executive Summary

The bug fix (`parseInt(req.params.id, 10)` on line 19 of `userController.js`) is safe and does not introduce new security vulnerabilities. The change is a minimal, targeted type conversion with no side effects on authentication, data integrity, or access control.

One MEDIUM finding exists: the fix does not explicitly reject non-numeric IDs (e.g., `/api/users/abc`), which are currently silently handled as a 404. While not exploitable in the current implementation, this represents a missing input validation pattern that should be addressed before production use. Three additional lower-severity findings are pre-existing issues in the application unrelated to the fix.

**Finding counts:**

| Severity | Count |
|----------|-------|
| CRITICAL | 0 |
| HIGH | 0 |
| MEDIUM | 1 |
| LOW | 1 |
| INFO | 2 |

---

## Findings

### [SEC-001] MEDIUM — Missing Explicit NaN Validation After parseInt

**Severity**: MEDIUM
**File**: `demo-bug-fix/src/controllers/userController.js`
**Line**: 19
**Category**: Missing Input Validation

**Description**:
`parseInt(req.params.id, 10)` returns `NaN` for non-numeric inputs such as `/api/users/abc` or `/api/users/null`. The current code passes `NaN` to `users.find()` where `u.id === NaN` always evaluates to `false` (NaN is not equal to itself in JavaScript). This results in a 404 response, which is functional but does not explicitly validate the input at the boundary.

In the current demo application, this is low-risk. However, in a production system with a real database, passing `NaN` or a malformed ID to a database query could cause unexpected behavior (error exposure, unintended queries, or query injection if the ID is later interpolated into a string).

**Current code (after fix):**
```javascript
const userId = parseInt(req.params.id, 10);
const user = users.find(u => u.id === userId);
```

**Recommended remediation:**
```javascript
const userId = parseInt(req.params.id, 10);
if (isNaN(userId) || userId <= 0) {
  return res.status(400).json({ error: 'Invalid user ID: must be a positive integer' });
}
const user = users.find(u => u.id === userId);
```

**Impact**: Without remediation, malformed IDs return 404 (not exploitable), but the application silently accepts invalid input rather than rejecting it at the boundary.

---

### [SEC-002] LOW — No Authentication or Authorization on User Endpoints

**Severity**: LOW (pre-existing, not introduced by fix)
**File**: `demo-bug-fix/src/routes/users.js`
**Line**: 5–8
**Category**: Missing Authentication

**Description**:
Both `GET /api/users` and `GET /api/users/:id` are publicly accessible with no authentication mechanism. Any caller can enumerate all users and retrieve individual user profiles including email addresses. In a demo application this is expected, but it would be a HIGH finding in a production system.

**Recommended remediation**:
Add authentication middleware (e.g., JWT validation) before route handlers:
```javascript
router.get('/:id', authMiddleware, getUserById);
```

**Note**: This is a pre-existing design decision in the demo application and is not related to the API-404 bug fix.

---

### [SEC-003] INFO — User Email Addresses Exposed in API Response

**Severity**: INFO (pre-existing)
**File**: `demo-bug-fix/src/controllers/userController.js`
**Line**: 7–11
**Category**: Information Disclosure

**Description**:
The API returns user email addresses in both list and single-user responses. In a production API, PII (Personally Identifiable Information) like email addresses should be returned only when necessary and should be scoped to the authenticated user's own data.

**Note**: Expected behavior for this demo application. No action required for the current scope.

---

### [SEC-004] INFO — No Rate Limiting on User Endpoints

**Severity**: INFO (pre-existing)
**File**: `demo-bug-fix/server.js`
**Category**: Defense in Depth

**Description**:
No rate limiting is applied to the user endpoints. An attacker could enumerate user IDs by making many rapid requests. In a demo application with in-memory storage, this has no practical impact, but production deployments should apply rate limiting.

**Note**: Pre-existing architectural decision. No action required for current scope.

---

## Security Scan Coverage

| Category | Checked | Findings |
|----------|---------|---------|
| Command/SQL Injection | ✓ | None — no external calls, no query building |
| Path Traversal | ✓ | None — no file system access |
| Hardcoded Secrets | ✓ | None — no credentials in source |
| Insecure Comparisons | ✓ | Fix uses `===` with `parseInt` result — safe |
| Missing Input Validation | ✓ | SEC-001 (MEDIUM) |
| Unsafe Dependencies | ✓ | Express 4.18.2 — as of 2026-03-01, no critical CVEs were identified in our review; verify against current CVE databases and prefer the latest Express 4.x for production use |
| XSS | ✓ | None — JSON API, no HTML rendering |
| CSRF | ✓ | None — read-only endpoints (GET only) |
| Authentication | ✓ | SEC-002 (LOW) — pre-existing |
| Authorization | ✓ | SEC-002 (LOW) — pre-existing |

---

## Conclusion

**The fix is safe to deploy in the current demo context.**

The `parseInt` change on line 19 introduces no new security vulnerabilities. The MEDIUM finding (SEC-001) represents a missing validation pattern that should be added before production use but does not block the bug fix from being merged in the current demo scope. The LOW and INFO findings are pre-existing architectural gaps unrelated to the fix.

**Recommendation**: Proceed with the fix. Address SEC-001 in a follow-up task before any production deployment.

---

## References

- Fix summary: `context/bugs/API-404/fix-summary.md`
- Files reviewed: `demo-bug-fix/src/controllers/userController.js`, `demo-bug-fix/src/routes/users.js`, `demo-bug-fix/server.js`
- Bug context: `demo-bug-fix/bugs/API-404/bug-context.md`
