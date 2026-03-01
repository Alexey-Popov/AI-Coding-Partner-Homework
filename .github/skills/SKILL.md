---
name: Homework Assignment Helper
description: Use when the user asks about homework structure, submission requirements, commit messages, pull requests, or documentation for course assignments. Also use when summarizing staged changes or writing commit messages for homework submissions.
---

# Homework Assignment Helper

This skill helps with AI-Assisted Development course homework submissions, ensuring all requirements are met and best practices are followed.

## When to Use This Skill

- Creating or organizing homework folders (homework-1/ through homework-6/)
- Writing commit messages for homework submissions
- Summarizing staged changes before commits
- Creating pull requests for homework submissions
- Setting up required documentation (README.md, HOWTORUN.md)
- Understanding grading criteria and submission requirements
- Organizing code structure (models, routes, services, validators, utils)
- Creating demo scripts and test files
- Documenting AI tool usage with screenshots

## Repository Structure

Each homework folder follows this standardized structure:

```
homework-X/
├── README.md              # Solution explanation, approach, AI tools used
├── HOWTORUN.md           # Step-by-step execution guide
├── requirements.txt      # Dependencies (Python) or package.json (Node.js)
├── src/                  # Source code
│   ├── models/           # Data models
│   ├── routes/           # API endpoints
│   ├── services/         # Business logic
│   ├── validators/       # Input validation
│   └── utils/            # Helper functions
├── tests/                # Test files (when applicable)
├── docs/                 # Documentation
│   ├── screenshots/      # AI interactions and app demos
│   └── API_REFERENCE.md  # API documentation (when applicable)
└── demo/                 # Demo scripts and sample data
    ├── run.bat           # Windows startup script
    ├── run.sh            # Unix startup script
    └── sample-requests.http  # API examples
```

## Commit Message Format

When writing commit messages for homework submissions, follow this format:

### For Feature Implementation
```
feat(hw-X): implement [feature name]

- Added [specific component/functionality]
- Implemented [another component]
- Configured [technology/tool]
```

### For Documentation Updates
```
docs(hw-X): add [documentation type]

- Created README.md with solution approach
- Added HOWTORUN.md with setup instructions
- Included screenshots of AI interactions
```

### For Testing
```
test(hw-X): add [test type] tests

- Implemented unit tests for [component]
- Added integration tests for [feature]
- Achieved X% test coverage
```

### For Initial Setup
```
chore(hw-X): initial project setup

- Created folder structure
- Added requirements.txt with dependencies
- Set up demo scripts and sample data
```

### For Homework Completion
```
feat(hw-X): complete homework [number] submission

- Implemented all required features
- Added comprehensive documentation
- Included AI usage screenshots
- Created demo scripts and test suite

AI Tools Used: [Claude/Copilot/GPT-4/etc]
```

## Submission Requirements Checklist

Every homework submission MUST include:

### Required Documentation
- [ ] **README.md** with:
  - Student name and date
  - Solution explanation
  - AI tools used and how
  - Project structure overview
  - Quick start instructions
  - Key technologies

- [ ] **HOWTORUN.md** with:
  - Prerequisites
  - Installation steps
  - Environment setup (virtual env, .env files)
  - How to run the application
  - How to run tests
  - Common troubleshooting

### Screenshots (Highly Expected)
- [ ] AI tool interactions (prompts and responses)
- [ ] Application running successfully
- [ ] Test results (if applicable)
- [ ] Interesting AI suggestions or corrections

Place in `docs/screenshots/` folder.

### Demo Files
- [ ] `demo/run.bat` (Windows) or `demo/run.sh` (Unix)
- [ ] `demo/sample-requests.http` or similar API examples
- [ ] Sample data files if needed

### Code Quality
- [ ] Meaningful file and folder names
- [ ] Separation of concerns (routes, services, models)
- [ ] `.gitignore` for dependencies and secrets
- [ ] Type hints (Python) or TypeScript definitions
- [ ] Inline comments for complex logic
- [ ] Consistent code style

## Grading Criteria (Use for Self-Assessment)

| Criteria | Weight | Focus |
|----------|--------|-------|
| ⚙️ Functionality | 30% | Does it work as specified? |
| 📝 AI Usage Documentation | 25% | Clear AI tool documentation |
| 💻 Code Quality | 20% | Clean, readable code |
| 📚 Documentation | 15% | README, comments, explanations |
| 🎬 Demo & Screenshots | 10% | Visual proof and AI interactions |

## Pull Request Guidelines

When creating a PR for homework submission:

1. **Branch naming**: `homework-X-submission`
2. **PR Title**: `feat: complete homework [X] - [brief description]`
3. **PR Description** should include:
   - ✅ Summary of implementation
   - 🛠️ AI tools used (with specific examples)
   - ⚠️ Challenges encountered and solutions
   - 📸 Links to screenshots/demos
   - 📊 Self-assessment against grading criteria

## AI Tool Documentation Best Practices

When documenting AI usage:

1. **Capture the Context**: What were you trying to achieve?
2. **Document the Prompt**: Exact prompt given to the AI
3. **Record the Response**: AI's suggestion or generated code
4. **Explain the Decision**: Did you accept, modify, or reject? Why?
5. **Screenshot the Interaction**: Visual proof in `docs/screenshots/`
6. **Reflect on Learning**: What did you learn from the AI interaction?

### Example Documentation Format

```markdown
### AI Interaction: Implementing State Machine

**Tool Used**: Claude Code
**Timestamp**: 2026-02-10

**Prompt**:
"Help me implement a state machine for card lifecycle with states: 
CREATED, ACTIVE, FROZEN, CLOSED. Include validation for valid transitions."

**AI Response Summary**:
Claude suggested using an Enum for states and a dictionary mapping 
valid transitions. Recommended raising ValueError for invalid transitions.

**Decision**: 
- ✅ Accepted: Enum approach for type safety
- ✅ Accepted: Transition validation dictionary
- 🔄 Modified: Used custom exception instead of ValueError
- ❌ Rejected: AI's logging approach (used structured logging instead)

**Screenshot**: See `docs/screenshots/ai-state-machine-design.png`

**Learning**: State machines benefit from explicit transition validation 
at the model layer rather than relying on business logic checks.
```

## Technology Stack Patterns

### Python Projects
- Virtual environment: `python -m venv venv`
- Dependencies: `requirements.txt` with pinned versions
- Framework: FastAPI for APIs, Flask for simple apps
- Testing: pytest with coverage
- Type checking: mypy (optional but recommended)

### Node.js Projects
- Package manager: npm or yarn
- Dependencies: `package.json` with `package-lock.json`
- Framework: Express.js for APIs
- Testing: Jest or Mocha
- Type checking: TypeScript (recommended)

## Common Tasks

### Creating a New Homework Folder
```bash
mkdir homework-X
cd homework-X
mkdir -p src/{models,routes,services,validators,utils}
mkdir -p tests docs/screenshots demo
touch README.md HOWTORUN.md requirements.txt
```

### Setting Up Python Environment
```bash
python -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate
pip install -r requirements.txt
```

### Running Tests with Coverage
```bash
pytest tests/ -v --cov=src --cov-report=term-missing
```

### Creating Demo Script
```bash
# demo/run.sh
#!/bin/bash
source venv/bin/activate
uvicorn src.main:app --reload --port 8000
```

## Quick Reference: File Purposes

| File | Purpose | Required |
|------|---------|----------|
| `README.md` | Project overview, AI usage, quick start | ✅ Yes |
| `HOWTORUN.md` | Detailed setup and run instructions | ✅ Yes |
| `requirements.txt` | Python dependencies | ✅ Yes (Python) |
| `package.json` | Node.js dependencies | ✅ Yes (Node) |
| `.gitignore` | Exclude dependencies, secrets, cache | ✅ Yes |
| `.env.example` | Environment variable template | Recommended |
| `docs/screenshots/` | AI interactions, demo screenshots | ✅ Yes |
| `demo/run.sh` | Startup script (Unix) | Recommended |
| `demo/run.bat` | Startup script (Windows) | Recommended |
| `tests/` | Test files | If applicable |

## Tips for Success

1. **Start with Structure**: Set up the folder structure first
2. **Document as You Go**: Don't wait until the end to document AI usage
3. **Screenshot Everything**: Capture AI interactions immediately
4. **Test Early**: Write tests alongside implementation
5. **Review Requirements**: Check the submission checklist before submitting
6. **Read the Rubric**: Understand grading criteria upfront
7. **Ask Questions**: Use AI tools to clarify requirements
8. **Commit Often**: Make small, focused commits with clear messages
9. **Demo It**: Ensure your demo scripts work on a fresh environment
10. **Reflect**: Include what you learned in your documentation

## Example Student Workflow

1. **Setup Phase**
   - Create homework folder structure
   - Initialize Git branch: `git checkout -b homework-X-submission`
   - Set up virtual environment and dependencies

2. **Implementation Phase**
   - Use AI to help design architecture
   - Implement features incrementally
   - Screenshot AI interactions
   - Write tests alongside code
   - Commit regularly with clear messages

3. **Documentation Phase**
   - Write README.md with AI usage details
   - Create HOWTORUN.md with setup steps
   - Organize screenshots in `docs/screenshots/`
   - Create demo scripts and verify they work

4. **Submission Phase**
   - Review submission checklist
   - Test on fresh environment
   - Create pull request with complete description
   - Assign instructor for review

## Troubleshooting Common Issues

| Issue | Solution |
|-------|----------|
| Import errors | Check `requirements.txt` installed, virtual env activated |
| Port already in use | Change port in config or kill existing process |
| Tests failing | Check test isolation, fixtures, mock data |
| Demo script fails | Verify paths, permissions, environment variables |
| Missing screenshots | Go back and recreate AI interactions with screenshots |
| Incomplete documentation | Use the checklists above to verify completeness |

---

**Remember**: The goal is to demonstrate both functional implementation AND effective use of AI as a coding partner. Document your AI interactions thoroughly!
