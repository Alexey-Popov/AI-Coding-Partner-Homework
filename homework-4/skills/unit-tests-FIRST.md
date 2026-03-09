# Skill: Unit Tests — FIRST Principles

## Purpose

This skill defines the **FIRST** principles for writing high-quality unit tests. It must be used by the **Unit Test Generator** agent when creating tests for changed code.

---

## The FIRST Principles

### F — Fast
- Tests must execute in milliseconds, not seconds
- No real network calls, no real database I/O, no real file system access
- Use mocks, stubs, or in-memory data for all external dependencies
- The entire test suite should complete in under 10 seconds

**How to verify**: Run `npm test` and confirm each test takes < 100ms

### I — Independent
- Each test must be completely self-contained
- No test should rely on the output or side effects of another test
- Tests can run in any order and still pass
- Use `beforeEach` / `afterEach` to reset shared state
- Never share mutable variables between tests without resetting them

**How to verify**: Run a single test in isolation; it should still pass

### R — Repeatable
- Tests produce the same result every time they run
- No dependency on external systems, time, random values, or environment
- If randomness is needed, seed it with a fixed value
- All test data is defined inline — no reliance on pre-existing state

**How to verify**: Run the test suite 3 times in a row; results must be identical

### S — Self-validating
- Tests produce a clear binary result: PASS or FAIL
- No manual inspection of output required
- Use explicit assertions (`expect(x).toBe(y)`, `expect(fn).toThrow()`, etc.)
- Each assertion has a clear pass/fail condition
- Test names describe the expected behavior: `"returns 404 when user ID does not exist"`

**How to verify**: A test must never require a human to decide if it passed

### T — Timely
- Tests are written at the same time as (or just after) the production code change
- Tests cover the specific code that was changed — not unrelated code
- Tests validate both the bug fix (regression test) and correct behavior
- At minimum, add one test per bug fix to prevent regression

**How to verify**: Every changed function has at least one corresponding test

---

## Test Structure Checklist

Before submitting tests, verify each test satisfies:

- [ ] **Fast**: No I/O, no network, no sleep
- [ ] **Independent**: No shared mutable state; uses `beforeEach` reset
- [ ] **Repeatable**: No randomness, no time dependency, no env dependency
- [ ] **Self-validating**: Has at least one `expect()` assertion; test name is descriptive
- [ ] **Timely**: Covers the exact code change documented in `fix-summary.md`

---

## How to Apply This Skill

1. Read `fix-summary.md` to identify every changed function and file.
2. For each changed function, write tests that cover:
   - The happy path (correct input → expected output)
   - The bug scenario (the input that used to fail, now passes)
   - Edge cases (null, undefined, empty, wrong type, boundary values)
3. Apply the FIRST checklist to each test before finalizing.
4. Run the full test suite with `npm test`.
5. Record results in `test-report.md`.

---

## Output Format

Each test file must follow this structure:

```js
describe('FunctionName', () => {
  // Setup / teardown for Independent principle
  beforeEach(() => { /* reset state */ });

  it('should [expected behavior] when [condition]', () => {
    // Arrange
    // Act
    // Assert
    expect(result).toBe(expected);
  });
});
```

Test report must include:
- Total tests run
- Pass/fail count
- FIRST compliance note per test group
- Coverage percentage (if available)
