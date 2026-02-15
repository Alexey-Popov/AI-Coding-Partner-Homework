# Documentation Policy

> Referenced by: `agents.md`.

## What Must Be Documented

| Artifact | Where | Content |
|---|---|---|
| Class purpose | Javadoc on class declaration | One sentence: what this class does and why |
| Public method contracts | Javadoc on public methods | Parameters, return value, exceptions thrown, side effects |
| Business rules | Inline comments | Only where the logic is non-obvious (state machine transitions, spending limit checks) |
| Design decisions | `// DECISION:` comments | When the spec offers a choice (e.g., pessimistic vs. optimistic locking) |
| Spec gaps | `// TODO: spec gap —` comments | When the agent identifies something missing from the spec |

## What Must NOT Be Documented

- Javadoc on private methods (unless complex algorithms).
- Comments that restate the code (e.g., `// set the name` above `setName(name)`).
- `@author` tags.
- Separate documentation files (README, CHANGELOG) unless a task explicitly requires it.
