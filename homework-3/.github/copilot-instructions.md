# Copilot Instructions — Virtual Card Service

> Quick-reference rules for the AI editor. For full details see the referenced files.

## Tech Stack

Java 21 | Spring Boot 3.x | Hibernate/JPA | PostgreSQL | Maven | Flyway | Monolith (controller -> service -> repository -> domain/model)

## Naming Conventions

- **Packages:** `com.levik.virtualcard.{config,controller,service,repository,domain.model,dto,exception}`
- **Entities:** singular nouns, `@Entity` (`VirtualCard`)
- **Repositories:** `{Entity}Repository` extending `JpaRepository<Entity, UUID>`
- **Services:** `{Domain}Service` (`CardLifecycleService`)
- **Controllers:** `{Domain}Controller` (`CardController`)
- **DTOs:** `{Action}{Entity}Request` / `{Entity}Response` — Java records
- **Enums:** PascalCase name, UPPER_SNAKE_CASE values
- **Tests:** `{Class}Test` (unit), `{Controller}IntegrationTest` (integration)
- **Methods:** camelCase, verb-first
- **Constants:** `UPPER_SNAKE_CASE`, `private static final`

## Layer Rules & Anti-Patterns

> Full layer dependency table in [`agents.md`](../agents.md) section 3.

- **DO NOT** put business logic in controllers or return entities from them.
- **DO NOT** use `float`/`double` for money — `BigDecimal` only.
- **DO NOT** concatenate strings into SQL/JPQL — parameterized queries only.
- **DO NOT** use `@Autowired` on fields — constructor injection.
- **DO NOT** catch generic `Exception` in services — let `GlobalExceptionHandler` handle it.
- **DO NOT** use H2 for tests — Testcontainers + PostgreSQL.
- **DO NOT** add features/endpoints not in the current task.
- **DO NOT** create separate scheduler/job packages — `@Scheduled` methods live in the owning service class.

## Security & Error Handling

> Full rules in [`security-policy.md`](../security-policy.md).

- No secrets in code or logs. No wildcard CORS. CSRF disabled. Rate limiting enforced.
- Authorization via `AuthorizationService` on every service method. Deny by default.
- Errors via `GlobalExceptionHandler` returning `ErrorResponse`. Never leak internals.

## PR Discipline

- **One task per commit:** `task-{N}: {brief description}`
- **No unrelated changes** in a commit.
- **All tests pass** before committing.
