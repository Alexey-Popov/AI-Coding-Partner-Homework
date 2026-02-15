# Testing Policy

> Referenced by: `agents.md`, `specification.md` (Task 18).

## Test Types & Minimums

| Test Type | Scope | Tools | Minimum Per Class |
|---|---|---|---|
| Unit | Service classes, domain model logic | JUnit 5 + Mockito + AssertJ | 6 tests |
| Integration | Controller endpoints (full stack) | SpringBootTest + MockMvc + Testcontainers (PostgreSQL) | 4 tests |
| Security | Authorization enforcement | SpringBootTest + MockMvc with role-based test users | 3 tests |

## Rules

- Every test method: **Given / When / Then** (Arrange / Act / Assert).
- Use `@DisplayName("should [behavior] when [condition]")` on every test method.
- No test may depend on execution order.
- Unit tests mock all dependencies via `@Mock` / `@InjectMocks`.
- Integration tests use **Testcontainers with PostgreSQL** — never H2 or in-memory databases.
- **Test data isolation:** Integration tests MUST use `@Transactional` (auto-rollback) or explicit cleanup in `@AfterEach` to prevent test pollution. Never rely on a clean database — always set up your own state.
- **Assertions:** Use AssertJ (`assertThat`) for all assertions. Do not mix JUnit `assertEquals`/`assertTrue` with AssertJ in the same test class.
- Test monetary calculations with `BigDecimal.compareTo`, not `equals` (scale may differ).
- Cover edge cases: null limits, zero amounts, boundary values, concurrent modification (`OptimisticLockException` simulation).

## Coverage Requirement

- **Line coverage MUST exceed 85%**, enforced via JaCoCo.
- Configure the `jacoco-maven-plugin` with a `check` goal that fails the build if coverage drops below 0.85.
- Exclusions from coverage measurement: configuration classes (`config/`), DTOs (`dto/`), and the Spring Boot application entry point (`VirtualCardApplication.java`).
- Coverage is measured across unit + integration tests combined.

## What NOT to Test

- Spring Data JPA generated query methods (Spring's responsibility).
- Framework annotations (`@Valid`, `@Transactional`) in isolation — test them through integration tests.
- Getters/setters.
