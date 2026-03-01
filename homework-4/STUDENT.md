# Student Information

**Name**: Mykhailo Bestiuk
**Course**: AI Coding Partner
**Homework**: Homework 4 — 4-Agent Pipeline for Bug Fixing

---

## Assignment Summary

Built a 4-agent pipeline for autonomous bug fixing:

1. **Research Verifier** — verifies bug research accuracy using a custom quality measurement skill
2. **Bug Implementer** — applies fixes from an implementation plan and runs tests
3. **Security Verifier** — performs security review of changed code (report only)
4. **Unit Test Generator** — generates FIRST-compliant unit tests for changed code

Applied the pipeline to bug API-404: a type mismatch in a Node.js/Express user lookup endpoint. All fixes applied, all 8 unit tests pass, pipeline artifacts complete.

---

## Deliverables

| Item | Location | Status |
|------|---------|--------|
| Agent: Research Verifier | `agents/research-verifier.agent.md` | Complete |
| Agent: Bug Implementer | `agents/bug-implementer.agent.md` | Complete |
| Agent: Security Verifier | `agents/security-verifier.agent.md` | Complete |
| Agent: Unit Test Generator | `agents/unit-test-generator.agent.md` | Complete |
| Skill: Research Quality | `skills/research-quality-measurement.md` | Complete |
| Skill: Unit Tests FIRST | `skills/unit-tests-FIRST.md` | Complete |
| Bug fix applied | `demo-bug-fix/src/controllers/userController.js` | Complete |
| Unit tests (8/8 pass) | `demo-bug-fix/tests/users.test.js` | Complete |
| Pipeline artifacts | `context/bugs/API-404/` | Complete |
| Documentation | `README.md`, `HOWTORUN.md` | Complete |
