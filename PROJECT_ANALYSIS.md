# Project Analysis: AI-Assisted Development Course Homework Repository

**Date:** January 21, 2026
**Analyzed by:** Claude Code

---

## 1. Project Overview

This is a homework repository for the **AI as a Personalized Coding Partner** course. It serves as a template for submitting 6 homework assignments focused on AI-assisted software development practices.

### Repository Purpose
- Educational template for AI-assisted development course
- Structured submission system for homework assignments
- Documentation of AI tool usage and learning outcomes

---

## 2. Project Structure Analysis

```
AI-Coding-Partner-Homework/
├── README.md                    # Main repository documentation
├── .gitignore                   # Git ignore patterns
├── homework-1/                  # Simple Banking API with AI Assistance
│   ├── README.md               # Project overview (template)
│   ├── HOWTORUN.md             # Run instructions (empty)
│   ├── TASKS.md                # Detailed task requirements
│   ├── src/                    # Source code directory (empty - has .gitkeep)
│   └── docs/                   # Documentation
│       └── screenshots/        # AI interaction screenshots (empty - has .gitkeep)
├── homework-2/                  # (Not yet created)
├── homework-3/                  # (Not yet created)
├── homework-4/                  # (Not yet created)
├── homework-5/                  # (Not yet created)
└── homework-6/                  # (Not yet created)
```

### Current Status
- **Repository initialized:** Yes (git repository with 3 commits)
- **Main branch:** main
- **Current state:** Clean working directory
- **Homework 1 status:** Template structure created, no implementation yet

---

## 3. Homework 1 Requirements Analysis

### 3.1 Core Requirements

#### Task 1: Core API Implementation (Required) - 25 points
Create a REST API with 4 endpoints:

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/transactions` | POST | Create new transaction |
| `/transactions` | GET | List all transactions |
| `/transactions/:id` | GET | Get specific transaction |
| `/accounts/:accountId/balance` | GET | Get account balance |

**Data Model:**
```json
{
  "id": "auto-generated UUID",
  "fromAccount": "ACC-XXXXX format",
  "toAccount": "ACC-XXXXX format",
  "amount": "positive number, max 2 decimals",
  "currency": "ISO 4217 (USD, EUR, GBP, etc.)",
  "type": "deposit | withdrawal | transfer",
  "timestamp": "ISO 8601 datetime",
  "status": "pending | completed | failed"
}
```

#### Task 2: Transaction Validation (Required) - 15 points
- Amount: positive, max 2 decimal places
- Account format: `ACC-XXXXX` (alphanumeric)
- Currency: valid ISO 4217 codes
- Meaningful error messages with field-level details

#### Task 3: Transaction Filtering (Required) - 15 points
Support query parameters on GET /transactions:
- `?accountId=ACC-12345` - Filter by account
- `?type=transfer` - Filter by transaction type
- `?from=2024-01-01&to=2024-01-31` - Date range filter
- Combined filters support

#### Task 4: Additional Features (Choose 1+) - Variable points
Options:
- A: Transaction Summary Endpoint (totals, counts, recent date)
- B: Simple Interest Calculation
- C: Transaction Export (CSV format)
- D: Rate Limiting (100 req/min per IP)

### 3.2 Technical Requirements
- **Technology Stack:** Choose from Node.js/Express, Python/Flask/FastAPI, or Java/Spring Boot
- **Storage:** In-memory (no database required)
- **HTTP Status Codes:** 200, 201, 400, 404, 429
- **Error Handling:** Comprehensive validation and error responses
- **AI Tools:** Must use at least 2 AI tools (Claude Code, GitHub Copilot, etc.)

### 3.3 Deliverables

1. **Source Code:**
   - Complete working API
   - Organized folder structure (routes, models, validators, utils)
   - .gitignore file

2. **Documentation:**
   - README.md: Overview, features, architecture decisions
   - HOWTORUN.md: Step-by-step setup and run instructions

3. **Screenshots (in docs/screenshots/):**
   - AI tool interactions (prompts and responses)
   - API running successfully
   - Sample API requests/responses

4. **Demo Files (in demo/):**
   - run.sh or run.bat
   - sample-requests.http or .sh
   - sample-data.json

---

## 4. Grading Criteria

| Category | Weight | Key Points |
|----------|--------|------------|
| Functionality | 30% | Working endpoints, correct logic |
| AI Usage Documentation | 25% | Screenshots, prompt documentation |
| Code Quality | 20% | Clean, readable, well-structured |
| Documentation | 15% | README, HOWTORUN, comments |
| Demo & Screenshots | 10% | Visual evidence, working solution |

### Specific Point Distribution:
- Task 1 (Core API): 25 points
- Task 2 (Validation): 15 points
- Task 3 (Filtering): 15 points
- Task 4 (Additional): Variable
- Code Quality: 5 points
- Documentation: 5 points

---

## 5. Technology Stack Recommendations

### Option 1: Node.js + Express (Recommended for Speed)
**Pros:**
- Quick setup
- Extensive middleware ecosystem
- Good AI tool support
- JSON handling built-in

**Cons:**
- JavaScript type safety (unless using TypeScript)

### Option 2: Python + FastAPI (Recommended for Learning)
**Pros:**
- Automatic API documentation (Swagger)
- Type hints with Pydantic validation
- Async support
- Excellent for data manipulation

**Cons:**
- Slightly slower initial setup

### Option 3: Java + Spring Boot
**Pros:**
- Strong typing
- Enterprise-grade
- Comprehensive validation framework

**Cons:**
- More boilerplate code
- Longer development time

---

## 6. Key Design Decisions Needed

1. **Language/Framework Choice:** Node.js vs Python vs Java
2. **Project Structure:** How to organize routes, models, validators
3. **Validation Strategy:** Manual validation vs validation library
4. **Error Handling Pattern:** Centralized vs inline
5. **Additional Feature:** Which Task 4 option to implement
6. **Testing Approach:** Manual testing vs automated tests

---

## 7. Implementation Roadmap

### Phase 1: Setup (10-15 min)
1. Choose technology stack
2. Initialize project (npm init / pip setup / maven)
3. Install dependencies
4. Create folder structure

### Phase 2: Core API (30-45 min)
1. Implement POST /transactions
2. Implement GET /transactions
3. Implement GET /transactions/:id
4. Implement GET /accounts/:accountId/balance
5. Test basic functionality

### Phase 3: Validation (20-30 min)
1. Implement amount validation
2. Implement account format validation
3. Implement currency validation
4. Create error response structure

### Phase 4: Filtering (15-25 min)
1. Implement account filter
2. Implement type filter
3. Implement date range filter
4. Test combined filters

### Phase 5: Additional Feature (20-40 min)
1. Choose one Task 4 option
2. Implement selected feature
3. Test thoroughly

### Phase 6: Documentation (30-45 min)
1. Write comprehensive README.md
2. Write HOWTORUN.md
3. Take screenshots of AI interactions
4. Take screenshots of API working
5. Create demo files

### Phase 7: Testing & Refinement (20-30 min)
1. Test all endpoints
2. Test all validations
3. Test edge cases
4. Fix any bugs

**Total Estimated Time:** 2.5-4 hours

---

## 8. Success Criteria

### Must Have:
- All 4 core endpoints working
- All validation rules implemented
- All 3 filtering options working
- At least 1 Task 4 feature
- Complete README.md
- Complete HOWTORUN.md
- Screenshots of AI usage
- Screenshots of working API

### Nice to Have:
- Multiple Task 4 features
- Automated tests
- Error logging
- API documentation (Swagger/OpenAPI)
- Docker setup
- More extensive filtering

---

## 9. Common Pitfalls to Avoid

1. **Incomplete Validation:** Ensure all validation rules are implemented
2. **Missing Error Handling:** Every endpoint should handle errors gracefully
3. **Poor Documentation:** Screenshots and documentation are 35% of grade
4. **Hardcoded Values:** Use configuration for ports, limits, etc.
5. **No AI Evidence:** Must show AI tool usage with screenshots
6. **Inconsistent Data:** Ensure balance calculations are accurate
7. **Missing Edge Cases:** Test with negative amounts, invalid formats, etc.

---

## 10. AI Tool Usage Strategy

### Best Practices for AI-Assisted Development:

1. **Start with Clear Prompts:**
   - "Create a Node.js Express API for banking transactions"
   - "Add validation for account format ACC-XXXXX"
   - "Implement filtering by date range"

2. **Iterate Incrementally:**
   - Build one endpoint at a time
   - Test each feature before moving on
   - Refine prompts based on results

3. **Document Everything:**
   - Screenshot initial prompts
   - Screenshot generated code
   - Screenshot any corrections/refinements
   - Note which AI tool worked better for what

4. **Compare AI Tools:**
   - Try same prompt with different tools
   - Document differences in output
   - Choose best approach

5. **Review AI Output:**
   - Don't blindly accept generated code
   - Check for security issues
   - Ensure it meets requirements
   - Test thoroughly

---

## 11. Submission Checklist

Before submitting, ensure you have:

- [ ] All source code committed
- [ ] README.md completed with AI tools used
- [ ] HOWTORUN.md with clear instructions
- [ ] At least 3 screenshots of AI interactions
- [ ] Screenshots of API working
- [ ] Demo files (run.sh, sample-requests)
- [ ] .gitignore properly configured
- [ ] All endpoints tested and working
- [ ] All validations working
- [ ] All filters working
- [ ] At least 1 Task 4 feature working
- [ ] Code is clean and well-organized
- [ ] No sensitive data in repository
- [ ] Branch created: homework-1-submission
- [ ] Pull request created to instructor's repo

---

## 12. Next Steps After Analysis

1. Review this analysis document
2. Choose technology stack (Node.js recommended for this homework)
3. Review the comprehensive prompt (IMPLEMENTATION_PROMPT.md)
4. Start implementation with AI assistance
5. Document AI interactions throughout
6. Test thoroughly
7. Complete documentation
8. Submit via pull request

---

**End of Analysis**
