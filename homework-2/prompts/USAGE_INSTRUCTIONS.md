# Prompt Template Usage Instructions

## Overview

This folder contains structured prompt templates for AI-assisted Python/FastAPI development. Templates are organized by AI model and optimized for each model's strengths.

---

## 📁 Folder Structure

```
prompts/
├── GUARDRAILS.md                    # Shared rules for all templates
├── USAGE_INSTRUCTIONS.md            # This file
├── claude/                          # Anthropic Claude templates (XML format)
│   ├── TEMPLATE_CLAUDE_PLANNER.md
│   ├── TEMPLATE_CLAUDE_CODER_MODELS.md
│   ├── TEMPLATE_CLAUDE_CODER_SERVICES.md
│   ├── TEMPLATE_CLAUDE_CODER_CLASSIFICATION.md
│   ├── TEMPLATE_CLAUDE_TESTER_INTEGRATION.md
│   ├── TEMPLATE_CLAUDE_DOCUMENTER_README.md
│   └── TEMPLATE_CLAUDE_DOCUMENTER_ARCHITECTURE.md
├── gpt4/                            # OpenAI GPT-4 templates (JSON format)
│   ├── TEMPLATE_GPT4_PLANNER.md
│   ├── TEMPLATE_GPT4_CODER_VALIDATORS.md
│   ├── TEMPLATE_GPT4_TESTER_UNIT.md
│   ├── TEMPLATE_GPT4_TESTER_IMPORT.md
│   ├── TEMPLATE_GPT4_DOCUMENTER_API_REFERENCE.md
│   └── TEMPLATE_GPT4_REVIEWER.md
└── copilot/                         # GitHub Copilot templates (comment format)
    ├── TEMPLATE_COPILOT_CODER_ROUTES.md
    ├── TEMPLATE_COPILOT_CODER_SERVICES.md
    └── TEMPLATE_COPILOT_TESTER_QUICK.md
```

---

## 🎯 Model Selection Guide

### When to Use Claude (Anthropic)

**Best for:**
- Complex planning and architecture decisions
- Pydantic models with nested schemas
- Business logic services
- Integration tests with multi-step workflows
- Long-form documentation (README, Architecture)

**Format:** XML tags (`<system>`, `<context>`, `<task>`, `<output_format>`)

**Why:** Claude excels at understanding hierarchical structure and maintaining context across complex requirements.

---

### When to Use GPT-4 (OpenAI)

**Best for:**
- Validators with clear input/output patterns
- Unit tests with specific assertions
- Import parsing tests (CSV, JSON, XML)
- API reference documentation (tabular)
- Code reviews with checklists

**Format:** System/User message structure with JSON context blocks

**Why:** GPT-4 is excellent at pattern matching and generating structured outputs with specific examples.

---

### When to Use GitHub Copilot

**Best for:**
- API route scaffolding
- Following existing patterns from codebase
- Quick test generation from signatures
- Incremental code completion
- Real-time pair programming

**Format:** Comment-driven with inline TODOs and pattern references

**Why:** Copilot excels at contextual completion within the IDE and pattern replication.

---

## 📝 Context-Model-Prompt Framework

Every prompt should follow this structure:

### 1. Context (What you're building)
```markdown
- Project type: Customer Support Ticket System
- Tech stack: Python 3.8+, FastAPI, Pydantic
- Existing patterns: Reference homework-1/src/ structure
- Current phase: [Planning | Coding | Testing | Documentation]
```

### 2. Model (Which AI and why)
```markdown
- Selected model: [Claude | GPT-4 | Copilot]
- Reasoning: [Why this model for this task]
- Template: [Link to specific template]
```

### 3. Prompt (The filled template)
```markdown
- Replace all {{PLACEHOLDER}} values
- Include relevant code snippets as context
- Specify exact output format expected
```

---

## 🚀 Quick Start Workflow

### Step 1: Identify Your Task
| Task Type | Recommended Template |
|-----------|---------------------|
| Create implementation plan | `claude/TEMPLATE_CLAUDE_PLANNER.md` |
| Create Pydantic models | `claude/TEMPLATE_CLAUDE_CODER_MODELS.md` |
| Create service layer | `claude/TEMPLATE_CLAUDE_CODER_SERVICES.md` |
| Create classification logic | `claude/TEMPLATE_CLAUDE_CODER_CLASSIFICATION.md` |
| Create API routes | `copilot/TEMPLATE_COPILOT_CODER_ROUTES.md` |
| Create validators | `gpt4/TEMPLATE_GPT4_CODER_VALIDATORS.md` |
| Create unit tests | `gpt4/TEMPLATE_GPT4_TESTER_UNIT.md` |
| Create import tests | `gpt4/TEMPLATE_GPT4_TESTER_IMPORT.md` |
| Create integration tests | `claude/TEMPLATE_CLAUDE_TESTER_INTEGRATION.md` |
| Create README | `claude/TEMPLATE_CLAUDE_DOCUMENTER_README.md` |
| Create API reference | `gpt4/TEMPLATE_GPT4_DOCUMENTER_API_REFERENCE.md` |
| Create architecture docs | `claude/TEMPLATE_CLAUDE_DOCUMENTER_ARCHITECTURE.md` |
| Code review | `gpt4/TEMPLATE_GPT4_REVIEWER.md` |

### Step 2: Copy the Template
1. Open the appropriate template file
2. Copy the entire content
3. Create a new file or paste into AI chat

### Step 3: Fill in Placeholders
Replace all `{{PLACEHOLDER}}` values with your specific context:
- `{{PROJECT_NAME}}` → "Customer Support Ticket System"
- `{{COMPONENT_NAME}}` → "TicketService"
- `{{EXISTING_CODE}}` → Paste relevant code snippets

### Step 4: Reference Existing Patterns
Use homework-1/src/ as reference:
```
homework-1/src/
├── models/transaction.py       # Pydantic model patterns
├── routes/transactions.py      # API route patterns
├── services/transaction_service.py  # Service layer patterns
└── validators/transaction_validator.py  # Validation patterns
```

### Step 5: Include Guardrails
Every prompt must reference [GUARDRAILS.md](GUARDRAILS.md):
```markdown
## Guardrails
- Do not guess if uncertain
- Ask 3 clarifying questions for ambiguous input
- List all missing data explicitly
```

### Step 6: Submit and Review
1. Submit the filled prompt to the selected AI
2. Review output against guardrails
3. Verify assumptions match your requirements
4. Iterate if clarifications are needed

---

## 📋 Template Placeholder Reference

| Placeholder | Description | Example |
|-------------|-------------|---------|
| `{{PROJECT_NAME}}` | Full project name | Customer Support Ticket System |
| `{{PROJECT_DESCRIPTION}}` | Brief description | Multi-format ticket import with auto-classification |
| `{{TECH_STACK}}` | Technologies used | Python 3.8+, FastAPI, Pydantic |
| `{{COMPONENT_NAME}}` | Specific component | TicketService, TicketValidator |
| `{{COMPONENT_TYPE}}` | Component category | model, service, route, validator, test |
| `{{EXISTING_CODE}}` | Reference code | Paste from homework-1/src/ |
| `{{REQUIREMENTS}}` | Specific requirements | List of features/constraints |
| `{{INPUT_FORMAT}}` | Expected input | JSON, CSV, XML schema |
| `{{OUTPUT_FORMAT}}` | Expected output | Response structure |
| `{{TEST_SCENARIOS}}` | Test cases | List of scenarios to cover |
| `{{COVERAGE_TARGET}}` | Test coverage goal | 85% |

---

## ✅ Best Practices

1. **Always include context** - More context leads to better outputs
2. **Reference existing code** - Paste patterns from homework-1/src/
3. **Be specific about output format** - Specify file structure, imports, style
4. **Include guardrails** - Prevent hallucination and speculation
5. **Iterate** - Use follow-up prompts to refine output
6. **Validate** - Always review AI output before using

---

## 🔗 Related Files

- [GUARDRAILS.md](GUARDRAILS.md) - Universal rules for all prompts
- [../TASKS.md](../TASKS.md) - Homework-2 requirements
- [../../homework-1/src/](../../homework-1/src/) - Reference implementation patterns
