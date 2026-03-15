# Security Report: API-404

## Scope

- **Files Reviewed**: `demo-bug-fix/src/controllers/userController.js`, `demo-bug-fix/src/routes/users.js`, `demo-bug-fix/server.js`
- **Functions Reviewed**: `getUserById` (changed), `getAllUsers` (unchanged, reviewed for context)
- **Review Type**: Focused review of modified code; surrounding context read for completeness
- **Date**: 2026-03-15
- **Reviewer**: security-verifier agent (executed 2026-03-15)

---

## Checks Performed

- [x] Injection risks (SQL, command, template) — N/A: no SQL queries, no `exec`/`spawn`/`eval`, no template engine
- [x] Input validation and type coercion safety
- [x] Hardcoded secrets
- [x] Insecure comparisons
- [x] Authentication / Authorization (IDOR)
- [x] Error handling and information disclosure
- [x] Path traversal — N/A: no file system operations
- [x] XSS — N/A: JSON-only API, no HTML responses
- [x] CSRF — N/A: read-only GET endpoint, no state change
- [x] Unsafe deserialization — N/A: no deserialization in changed code
- [x] New dependency review — Jest 29.7.0 added as devDependency only

---

## Findings

### Finding 1
- **Severity**: LOW
- **Title**: No upper bound on parsed integer ID
- **File:Line**: `demo-bug-fix/src/controllers/userController.js:19`
- **Explanation**: `parseInt(req.params.id, 10)` will parse arbitrarily large inputs without error (e.g., `"99999999999"` → `99999999999`). While not exploitable against this in-memory store, this pattern becomes risky if IDs are later passed to a database with integer overflow constraints or compared with JavaScript's `Number.MAX_SAFE_INTEGER` limit (9007199254740991). Large-number inputs are silently accepted and return 404.
- **Remediation**: Add a range check: `if (userId < 1 || userId > 2147483647) return res.status(400).json({ error: 'Invalid user ID' });`

### Finding 2
- **Severity**: LOW
- **Title**: Async function without try/catch — unhandled rejection surface
- **File:Line**: `demo-bug-fix/src/controllers/userController.js:18`
- **Explanation**: `getUserById` is declared `async` but contains no `await` or `try/catch`. While the current synchronous array operations cannot throw, if any future change adds async I/O (e.g., real DB call), an unhandled rejection would crash the Node.js process in older versions or produce an unhandled error in Express without proper error middleware. The `async` keyword is also redundant on a fully synchronous function.
- **Remediation**: Either remove `async` (function is synchronous), or add `try/catch` with `res.status(500).json({ error: 'Internal server error' })` as a defensive measure.

### Finding 3
- **Severity**: INFO
- **Title**: No authentication on individual user profile endpoint
- **File:Line**: `demo-bug-fix/src/routes/users.js:14`
- **Explanation**: `GET /api/users/:id` is unauthenticated. Any caller knowing a valid ID can retrieve any user's profile. This is a pre-existing condition not introduced by this fix. For a demo/workshop application it is acceptable.
- **Remediation**: Add authentication middleware if deployed to production. Not required for demo use.

### Finding 4
- **Severity**: INFO
- **Title**: Full user object including email returned without field filtering
- **File:Line**: `demo-bug-fix/src/controllers/userController.js:31`
- **Explanation**: `res.json(user)` returns the full user object including email. If the schema grows to include sensitive fields (passwords, tokens, PII), they would be exposed. Pre-existing condition, not introduced by this fix.
- **Remediation**: Use a response serializer or explicitly select response fields in production code.

### Finding 5
- **Severity**: INFO
- **Title**: Negative integer IDs are not rejected
- **File:Line**: `demo-bug-fix/src/controllers/userController.js:19`
- **Explanation**: `parseInt("-1", 10)` returns `-1`. The `isNaN` guard does not catch negative numbers. A request to `/api/users/-1` will proceed to `users.find()` and return 404 (since no user has id -1). No security risk in current implementation, but negative IDs are semantically invalid.
- **Remediation**: Include in the range check from Finding 1: `if (userId < 1 || ...)`.

---

## Conclusion

- **Total Findings**: 5
- **Critical**: 0 | **High**: 0 | **Medium**: 0 | **Low**: 2 | **Info**: 3
- **Overall Security Status**: PASS WITH NOTES

The fix itself (`parseInt` + `isNaN` guard) is a security improvement over the pre-fix code: it adds input validation that was previously absent. No new vulnerabilities were introduced. The LOW findings are defense-in-depth improvements; the INFO findings are pre-existing conditions unrelated to this change.

---

## Residual Risk Notes

- This is a demo/workshop application with an in-memory data store. The current security posture is appropriate for its intended purpose.
- If migrated to a real database: address Finding 1 (integer bounds) and Finding 2 (error handling) before deployment.
- Finding 3 (no auth) is the most significant residual risk if this API is ever exposed publicly.

---

## References

- Fix summary: `context/bugs/API-404/fix-summary.md`
- Files reviewed: `demo-bug-fix/src/controllers/userController.js`, `demo-bug-fix/src/routes/users.js`, `demo-bug-fix/server.js`
