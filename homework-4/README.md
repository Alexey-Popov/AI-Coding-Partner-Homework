# Homework 4 - 4-Agent Pipeline (GitHub Copilot Ready)

This folder implements the `homework-4/TASKS.md` assignment with a spec-driven multi-agent pipeline for the `API-404` demo bug.

## What is included
- 7 agent specifications in `.github/agents/` (4 required + researcher, planner, orchestrator)
- 2 required skills in `.github/skills/`
- Reusable artifact templates in `context/bugs/XXX/`
- Pipeline output artifacts in `context/bugs/API-404/`
- Bug fix applied to `demo-bug-fix/`
- Unit tests in `demo-bug-fix/tests/`
- Screenshots in `docs/screenshots/`
- Run instructions in `HOWTORUN.md`

## Pipeline Flow
1. Bug Researcher writes `research/codebase-research.md`
2. Research Verifier validates it and writes `research/verified-research.md`
3. Bug Planner writes `implementation-plan.md`
4. Bug Implementer applies code changes and writes `fix-summary.md`
5. Security Verifier writes `security-report.md`
6. Unit Test Generator writes tests and `test-report.md`

Each agent reads the corresponding template from `context/bugs/XXX/` as a read-only reference and creates its output in the bug-specific directory (e.g. `context/bugs/API-404/`).

## Demo App Bug Fixed
- Bug: `GET /api/users/:id` returned `404` for valid numeric IDs because `req.params.id` is a string and the controller compared it to numeric IDs using strict equality (`===`).
- Fix: parse the route parameter with `Number()`, validate it with `Number.isInteger()`, and compare the numeric value (`demo-bug-fix/src/controllers/userController.js`).

## Repository Layout
```
homework-4/
├── .github/
│   ├── agents/                  # Agent specs (Copilot agent format)
│   │   ├── research-verifier.agent.md
│   │   ├── bug-implementer.agent.md
│   │   ├── security-verifier.agent.md
│   │   ├── unit-test-generator.agent.md
│   │   ├── bug-researcher.agent.md
│   │   ├── bug-planner.agent.md
│   │   └── bug-pipeline-orchestrator.agent.md
│   └── skills/                  # Reusable rubrics used by agents
│       ├── research-quality-measurement/SKILL.md
│       └── unit-tests-first/SKILL.md
├── context/
│   └── bugs/
│       ├── XXX/                 # Read-only templates (do not edit)
│       └── API-404/             # Pipeline output for the demo bug
├── demo-bug-fix/                # Demo Express app (with fix applied)
│   ├── src/
│   ├── tests/                   # Unit tests for changed code
│   └── package.json
├── docs/screenshots/            # Pipeline run evidence
├── README.md
├── HOWTORUN.md
├── STUDENT.md
└── TASKS.md
```

## Bringing Your Own Bug

`context/bugs/XXX/` is a **read-only template folder**. Agents reference it for structure but never modify it.

To run the pipeline against a new bug:

1. Create a `bug-context.md` for your bug (see `demo-bug-fix/bugs/API-404/bug-context.md` as an example).
2. Point the pipeline orchestrator at your bug ID and source root:
   ```
   Run pipeline for context/bugs/<YOUR-BUG-ID> using <your-app-source-root>
   ```
3. Each agent will read the `XXX/` templates for structure and create output files in `context/bugs/<YOUR-BUG-ID>/`.

## Notes
- Screenshots must be captured manually during Copilot agent runs and placed in `docs/screenshots/`.
- Each agent writes exactly one canonical artifact with a strict output contract defined by the `XXX/` templates.
