# Homework 2 Delivery Plan

Goal: Build the Intelligent Customer Support System per `homework-2/TASKS.md` and meet root submission requirements in `README.md`.

## Checklist
- [x] Choose stack: Node/Express (selected).
- [x] Implement API: CRUD + bulk import + auto-classify endpoint.
- [x] Build validation and error handling for all fields and file formats.
- [x] Implement auto-classification with confidence, reasoning, keywords, and logging; allow manual override.
- [x] Create comprehensive tests (>85% coverage) across required test files.
- [x] Produce sample data files and invalid fixtures.
- [x] Write multi-level documentation with Mermaid diagrams.
- [ ] Capture required screenshots (AI interactions, app running, tests).
- [x] Prepare demo scripts and request examples.
- [ ] Follow submission workflow (branch, PR template, reviewer).

## Requirements Extract
### Root project requirements (`README.md`)
- Required docs per homework: `README.md`, `HOWTORUN.md`.
- Screenshots expected in `docs/screenshots/` (AI interactions, app running, test results, AI suggestions).
- Demo artifacts recommended: `demo/run.sh` or `demo/run.bat`, `demo/test-requests.http` or `demo/requests.sh`, optional `demo/sample-data/`.
- Grading focuses on functionality, AI usage documentation, code quality, documentation, demo/screenshots.
- Submission: branch per homework, PR with summary/tools/challenges/screenshots, assign instructor reviewer.

### Homework 2 requirements (`homework-2/TASKS.md`)
- REST API endpoints:
  - `POST /tickets` create
  - `POST /tickets/import` bulk import CSV/JSON/XML
  - `GET /tickets` list with filtering
  - `GET /tickets/:id` get
  - `PUT /tickets/:id` update
  - `DELETE /tickets/:id` delete
  - `POST /tickets/:id/auto-classify`
- Ticket model with validation rules (email format, string lengths, enums, required fields, timestamps).
- Import parsing with summary (total/success/failed + error details) and graceful errors for malformed files.
- Auto-classification: categories, priority rules, confidence score, reasoning, keywords, store decision, manual override, logging, optional auto-run on create.
- Tests: >85% coverage and required test file set with specified test counts.
- Docs: `README.md`, `API_REFERENCE.md`, `ARCHITECTURE.md`, `TESTING_GUIDE.md` (plus `HOWTORUN.md` from root); at least 3 Mermaid diagrams; use different AI models for different doc types.
- Integration/performance tests: lifecycle workflow, bulk import + classification, 20+ concurrent requests, combined filtering.
- Deliverables: coverage report + screenshot `docs/screenshots/test_coverage.png`, sample data files (CSV/JSON/XML with given counts), invalid data files.

## Implementation Plan
1. **Select stack** and set project structure under `homework-2/` (src/tests/docs/demo/sample data).
2. **Model & validation**: implement ticket schema, enums, and field validation with clear error messages.
3. **Storage layer**: in-memory store with CRUD operations and filtering; add audit/log for classification decisions.
4. **Importers**: CSV/JSON/XML parsers with robust error handling and bulk summary response.
5. **Auto-classification**: keyword-based classifier for category/priority, compute confidence, store reasoning/keywords, allow override.
6. **API layer**: implement endpoints + status codes and filtering params.
7. **Tests**: implement unit, API, import, categorization, integration, performance suites; add fixtures; hit >85% coverage.
8. **Docs**: write required docs, include Mermaid diagrams, document AI tool usage and model choices.
9. **Demo & screenshots**: add demo scripts and sample request files; capture required screenshots including coverage.
10. **Submission**: branch, PR with template items, assign reviewer, add labels.

## Open Decisions
- Pick camelCaseName for the plan filename (default used here: `homework2`).
