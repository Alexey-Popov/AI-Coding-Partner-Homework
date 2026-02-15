# AI Agent Configuration — Virtual Card Lifecycle & Spending Controls

> Read this file before starting any task. Follow all rules. For details on specific policies, read ONLY the referenced file — do not load all files upfront.

## File Manifest

| File | Purpose | When to Read |
|---|---|---|
| `agents.md` | Operating principles, architecture, Definition of Done | Every task (always loaded) |
| `specification.md` | Feature spec with 18 ordered tasks, data model, business rules | Every task (read the specific task section) |
| `security-policy.md` | OWASP mapping, validation constraints, logging redaction, error contract | Tasks touching auth, validation, logging, or error handling |
| `testing-policy.md` | Test types, minimums, rules, isolation, assertion style | Tasks that create or update tests |
| `documentation-policy.md` | Javadoc and comment conventions | Tasks that add Javadoc or inline comments |
| `.github/copilot-instructions.md` | Naming conventions, anti-patterns, commit discipline | Every task (quick-reference) |
| `task.md` | Progress tracking — done, in progress, to do, blockers | Updated at the end of every task |

---

## 1. Required Skills Per Activity

The agent MUST use the designated skill for each activity. Do NOT perform these activities without invoking the corresponding skill.

| Activity | Skill to Use | When |
|---|---|---|
| Writing Java code (entities, services, controllers, config) | `/spring-boot-engineer` | Every implementation task (Tasks 1–17) |
| Running tests, verifying builds, validating coverage | `/test-master` | After every task, before marking it done |
| CI/CD, Docker, deployment config, infrastructure | `/devops-engineer` | If any task touches build pipeline or containerization |
| Security review, OWASP compliance, vulnerability checks | `/security-reviewer` | After implementing auth (Task 9), error handling (Task 7), config (Task 17), and as a final pass after Task 18 |

**Workflow per task:**
1. **Plan first:** Before writing any code, create a brief implementation plan — list the files to create/modify, the classes/methods involved, key decisions, and any edge cases. Present the plan and get approval before proceeding.
2. **Implement** → use `/spring-boot-engineer`
3. **Verify** → use `/test-master` to compile, run all tests, and verify coverage. If anything fails, fix before proceeding.
4. **Security audit (when applicable)** → if the task touched authorization, validation, error handling, logging, or configuration, run `/security-reviewer` against OWASP and `security-policy.md`.
5. **Update tracking** → move the task in `task.md` (see section 5). If the task changed architecture, data model, or API, update the relevant doc files to stay in sync.
6. **Move to the next task.**

**NEVER start coding without a plan.** Even for small tasks, write down what you intend to do and confirm before implementing.

**NEVER consider a task done without running `/test-master`.** A task that compiles but has failing tests is NOT done.

---

## 2. Operating Principles

- **Determinism:** Produce the same output given the same input. Use exact names from the task prompt. Do not rename or reorganize.
- **Minimal Diffs:** Change ONLY what the current task requires. Do not touch unrelated code.
- **No Scope Creep:** Implement ONLY what the task specifies. Flag gaps with `// TODO: spec gap — [description]`.
- **Task Ordering:** Tasks are numbered and MUST be implemented in order (dependencies flow downward).
- **One Task at a Time:** Complete one fully before starting the next.

### Failure & Blocked State

- If a task's acceptance criteria cannot be met (compilation failure, missing dependency from a prior task, ambiguous requirement), **stop and report the specific failure**. Do not attempt workarounds, partial implementations, or skip to the next task.
- Include in the report: task number, what failed, which prior artifact is missing or broken, and the exact error message.

### Conflict Resolution

- If `specification.md` task prompts contradict `agents.md` conventions for a specific detail (e.g., where DTO mapping happens), **the task prompt in `specification.md` takes precedence** for that task only.
- If two policy files contradict each other, `security-policy.md` takes precedence over all others (security wins).

---

## 3. Architecture Conventions

### Package Structure
```
com.levik.virtualcard
├── config/          — @Configuration classes only
├── controller/      — @RestController classes only (no business logic)
├── domain/model/    — JPA entities, enums, value objects
├── dto/             — Java records (immutable request/response)
├── exception/       — Custom exceptions + @RestControllerAdvice handler
├── repository/      — Spring Data JPA interfaces only
└── service/         — @Service classes (all business logic, including @Scheduled methods)
```

### Layer Dependency Rules

| Layer | May Depend On | MUST NOT Depend On |
|---|---|---|
| Controller | Service, DTO | Repository, Entity, other Controllers |
| Service | Repository, Entity, DTO, other Services | Controller, HttpServletRequest |
| Repository | Entity | Everything else |
| Domain/Model | Nothing | Everything |
| DTO | Nothing | Everything |

### Key Conventions

- **DTO Mapping:** Entity-to-DTO in the **service layer** or via a static factory on the DTO record. Controllers receive and return DTOs only.
- **Transactions:** `@Transactional` on service methods only. Audit uses `REQUIRES_NEW`. Reads use `readOnly = true`.
- **Entities:** UUID PKs (app-generated). `@Version` on concurrently-modified entities. `@PrePersist`/`@PreUpdate` for timestamps. No public setters on immutable fields.
- **Money:** `BigDecimal` + `NUMERIC(19,4)`. `RoundingMode.HALF_EVEN`. ISO-4217 currency. Never `float`/`double`.
- **Scheduled methods:** Live in the service class that owns the domain logic (e.g., `IdempotencyService.cleanupExpired()`). Do not create separate scheduler packages.

---

## 4. Policies (see dedicated files)

- **Testing:** see [`testing-policy.md`](testing-policy.md)
- **Security & OWASP:** see [`security-policy.md`](security-policy.md)
- **Documentation:** see [`documentation-policy.md`](documentation-policy.md)
- **Naming & PR Discipline:** see [`.github/copilot-instructions.md`](.github/copilot-instructions.md)

---

## 5. Task Tracking (`task.md`)

The agent MUST maintain a `task.md` file in the project root. This file tracks progress across all 18 tasks from `specification.md`.

**Format:**

```markdown
# Task Progress

## Done
- [x] Task 1: Define Domain Enums and Value Objects — <date completed>
- [x] Task 2: Create JPA Entities — VirtualCard and SpendingAggregate — <date completed>

## In Progress
- [ ] Task 3: Create JPA Entities — CardTransaction, CardAuditEvent, IdempotencyRecord

## To Do
- [ ] Task 4: Create Spring Data JPA Repositories
- [ ] Task 5: Create Flyway Migration Script
...

## Notes
- <any blockers, decisions, or deviations from the spec>
```

**Rules:**
- Update `task.md` at the end of every task (move from "To Do" → "In Progress" → "Done").
- Include the completion date for done tasks.
- Record any blockers, spec gaps, or deviations in the "Notes" section.
- This file is the single source of truth for project progress.

---

## 6. Definition of Done

A task is complete ONLY when ALL of the following are true:

**Build & Test (non-negotiable — use `/test-master`):**
- [ ] `/test-master` confirms: compile succeeds, all tests green, no skipped tests, coverage > 85%
- [ ] `/security-reviewer` passed (if task touched auth, validation, error handling, or config)

**Code Quality:**
- [ ] Files created/updated match exactly what the task specifies (no extra files)
- [ ] Class and method names match the task prompt exactly
- [ ] Package placement follows architecture conventions (section 3)
- [ ] All acceptance criteria from the task are satisfied
- [ ] BigDecimal used for all money
- [ ] `@Version` present on concurrently-modified entities
- [ ] No untagged `TODO` items (only `// TODO: spec gap —` allowed)

**Cross-cutting checks (apply once the relevant service/layer exists):**
- [ ] No entity returned from a controller — only DTOs
- [ ] No business logic in a controller
- [ ] Authorization checks present in every resource-accessing service method
- [ ] Audit events emitted for every state-changing operation
- [ ] Idempotency key checked on every mutating endpoint
- [ ] No sensitive data in logs or error responses (see `security-policy.md`)

**Documentation & Tracking (mandatory):**
- [ ] `task.md` updated — current task moved to "Done" with date
- [ ] If architecture/data model/API changed: relevant docs updated to stay in sync
- [ ] If new conventions were introduced: `agents.md` or policy files updated
