# Skill: Unit Tests — FIRST Criteria

## Overview

When an AI agent generates unit tests, human reviewers cannot always inspect every test manually. The FIRST criteria provide an objective checklist ensuring that generated tests are reliable, maintainable, and genuinely useful. Tests that violate FIRST criteria create false confidence — they may pass in one environment and fail in another, or mask bugs rather than catch them. The Unit Test Generator must evaluate every generated test against this skill before submitting.

---

## The FIRST Criteria

### F — Fast

Tests must execute quickly so the full suite can run on every save, every commit, and every CI trigger without adding friction.

**Rules:**
- Each test must complete in < 100 ms
- Full test suite must complete in < 5 seconds (for small modules)
- No file system reads or writes in test body
- No outbound HTTP calls, no database connections
- Use mock objects for all external dependencies (e.g., `jest.fn()` for Express `req`/`res`)
- No `setTimeout`, `setInterval`, or sleep-based waits

**Anti-patterns:**
```javascript
// ❌ SLOW — real HTTP call
test('get user', async () => {
  const res = await fetch('http://localhost:3000/api/users/123');
  expect(res.status).toBe(200);
});

// ✅ FAST — mock req/res, direct function call
test('get user', async () => {
  const req = { params: { id: '123' } };
  const res = { json: jest.fn(), status: jest.fn().mockReturnThis() };
  await getUserById(req, res);
  expect(res.json).toHaveBeenCalledWith(expect.objectContaining({ id: 123 }));
});
```

---

### I — Independent

Each test must stand alone. Running tests in isolation must produce the same result as running the full suite.

**Rules:**
- No test may depend on state set by another test
- No shared mutable variables between tests (use `beforeEach` to reset)
- Each test creates its own mock objects
- Tests must pass when run in any order
- Tests must pass when run individually: `jest --testNamePattern="test name"`

**Anti-patterns:**
```javascript
// ❌ DEPENDENT — second test relies on first test's side effect
let sharedRes;
test('first', async () => { sharedRes = { json: jest.fn() }; });
test('second', async () => { expect(sharedRes.json).toHaveBeenCalled(); });

// ✅ INDEPENDENT — each test creates its own mocks
test('each test', async () => {
  const res = { json: jest.fn(), status: jest.fn().mockReturnThis() };
  // ...
});
```

---

### R — Repeatable

Tests must produce the same pass/fail result on every machine and in every environment.

**Rules:**
- No dependency on system clock (mock `Date.now()` or `new Date()` if needed)
- No dependency on environment variables unless explicitly set in test setup
- No dependency on file system paths that differ between machines
- No flaky async behavior — all promises must resolve deterministically
- No random data in assertions — if randomness is needed, seed it

**Anti-patterns:**
```javascript
// ❌ NON-REPEATABLE — depends on system time
test('timestamp is today', () => {
  expect(result.date).toBe(new Date().toISOString().slice(0, 10));
});

// ✅ REPEATABLE — mock the date
// Note: jest.useFakeTimers().setSystemTime is available in Jest 29+.
// For earlier Jest versions, call jest.useFakeTimers(); then jest.setSystemTime(new Date('2024-01-15')).
jest.useFakeTimers().setSystemTime(new Date('2024-01-15'));
test('timestamp is fixed', () => {
  expect(result.date).toBe('2024-01-15');
});
```

---

### S — Self-Validating

A test must produce a clear, unambiguous pass or fail — no human needs to read output to determine the result.

**Rules:**
- All assertions use `expect()` matchers with descriptive failure messages
- Never use `console.log()` as a substitute for assertions
- Failure messages must clearly state what was expected vs. what was received
- Use specific matchers: `toEqual`, `toHaveBeenCalledWith`, `toContain` — not just `toBeTruthy`
- One logical assertion per test (multiple `expect()` calls are fine if they verify one concept)

**Anti-patterns:**
```javascript
// ❌ NOT SELF-VALIDATING — human must inspect console
test('user found', async () => {
  await getUserById(req, res);
  console.log(res.json.mock.calls[0][0]); // "looks right"
});

// ✅ SELF-VALIDATING — assertion proves it
test('returns user when found', async () => {
  await getUserById(req, res);
  expect(res.json).toHaveBeenCalledWith({
    id: 123,
    name: 'Alice Smith',
    email: 'alice@example.com'
  });
});
```

---

### T — Timely

Tests must be written for the code that was actually changed — not for existing, unrelated code.

**Rules:**
- In bug-fix contexts: tests must cover the specific function(s) changed by the fix
- Include at least one regression test that would have caught the bug before the fix
- Do not generate tests for unchanged modules (out of scope)
- Tests for the bug's root cause scenario are mandatory
- Tests should be written at the same time as (or immediately after) the fix

**For bug fix contexts:**
```javascript
// ✅ TIMELY — regression test for the specific bug (string ID that previously failed)
test('getUserById: string "123" resolves to user (regression for API-404)', async () => {
  const req = { params: { id: '123' } };  // This is what caused the 404 before the fix
  const res = { json: jest.fn(), status: jest.fn().mockReturnThis() };
  await getUserById(req, res);
  expect(res.json).toHaveBeenCalledWith(expect.objectContaining({ id: 123 }));
});
```

---

## FIRST Compliance Checklist

Before submitting generated tests, verify each item:

```markdown
### FIRST Compliance Checklist

**Fast**
- [ ] All tests use mock req/res objects (no real HTTP calls)
- [ ] No file system or database access in tests
- [ ] No setTimeout/sleep in test body
- [ ] Suite runs in < 5 seconds

**Independent**
- [ ] Each test creates its own mock objects
- [ ] No shared mutable state between tests
- [ ] Tests pass when run individually

**Repeatable**
- [ ] No system clock dependency
- [ ] No environment variable dependency
- [ ] No file path dependency
- [ ] All async resolved deterministically

**Self-Validating**
- [ ] All assertions use expect() matchers
- [ ] No console.log() substituting for assertions
- [ ] Failure messages are meaningful

**Timely**
- [ ] Tests cover only changed functions
- [ ] At least one regression test for the specific bug
- [ ] Tests written for the actual fix, not hypothetical changes
```

---

## Test Report Format for Agents

When using this skill in a test report, include:

```markdown
## FIRST Compliance Assessment

| Criterion | Status | Evidence |
|-----------|--------|---------|
| Fast | PASS | All 8 tests use mock req/res; suite runs in 287ms |
| Independent | PASS | Each test declares its own req/res mocks; no shared state |
| Repeatable | PASS | No time/env/path dependencies; deterministic mock data |
| Self-Validating | PASS | All assertions use expect() with specific matchers |
| Timely | PASS | Tests cover getUserById (changed) + getAllUsers; 1 regression test for API-404 |

**Overall**: FIRST compliant ✓
```
