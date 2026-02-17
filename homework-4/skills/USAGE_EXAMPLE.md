# OWASP LLM Top 10 Verification Skill - Usage Examples

This document demonstrates how to use the OWASP LLM Top 10 Verification Skill in various scenarios.

---

## Example 1: Security Verifier Agent

Here's how a security verifier agent would use this skill:

### Agent Configuration

**File**: `agents/owasp-security-verifier.agent.md`

```markdown
# OWASP Security Verifier Agent

## Role
Security review agent specializing in OWASP LLM Top 10 compliance verification.

## Responsibilities
1. Read the OWASP LLM Top 10 Verification Skill (`skills/owasp-llm-top-10-verification.md`)
2. Analyze the application codebase for LLM integration points
3. Check each integration against all 10 OWASP risks
4. Document findings using the skill's output format
5. Provide actionable remediation guidance

## Process
1. Load the OWASP LLM Top 10 Verification Skill
2. Identify all files containing LLM interactions (API calls, prompt construction, etc.)
3. For each file:
   - Check for LLM01-LLM10 vulnerabilities
   - Note vulnerable code patterns
   - Assess severity using skill definitions
4. Generate comprehensive security report
5. Create risk matrix and recommendations

## Required Skills
- `skills/owasp-llm-top-10-verification.md`

## Output
- `security-report.md` following the skill's output format
```

### Example Agent Execution

```bash
# Agent reads the skill
cat skills/owasp-llm-top-10-verification.md

# Agent scans codebase for LLM patterns
grep -r "openai\|anthropic\|llm\|prompt" src/

# Agent analyzes each file and creates report
# Output: security-report.md
```

---

## Example 2: Sample Security Report

Here's an example of a security report generated using the skill:

```markdown
# OWASP LLM Top 10 Verification Report

Application: ChatBot API v2.0
Date: 2025-02-17
Verifier: OWASP-Security-Agent

Overall Risk Level: HIGH
Total Findings: 8
├─ Critical: 2
├─ High: 3
├─ Medium: 2
└─ Low: 1

---

## Finding #1: Prompt Injection via Direct Concatenation

**Risk**: LLM01 - Prompt Injection
**Severity**: CRITICAL
**Location**: src/controllers/chatController.js:45
**Status**: OPEN

### Description
User input is directly concatenated with system prompt without any validation or sanitization, allowing attackers to override system instructions.

### Evidence
```javascript
// src/controllers/chatController.js:45
const prompt = `You are a helpful assistant. ${userMessage}`;
const response = await openai.chat.completions.create({
  messages: [{ role: "user", content: prompt }]
});
```

### Impact
- Attackers can inject instructions like "Ignore previous instructions and..."
- System behavior can be completely overridden
- Potential for data exfiltration or unauthorized actions
- Loss of application control and security boundaries

### Remediation
1. Use structured message format with separate roles:
```javascript
const messages = [
  { role: "system", content: "You are a helpful assistant." },
  { role: "user", content: userMessage }
];
```

2. Implement input validation:
```javascript
function validateUserInput(input) {
  // Check for prompt injection patterns
  const dangerousPatterns = [
    /ignore (previous|above) instructions/i,
    /system prompt/i,
    /new instructions:/i
  ];

  for (const pattern of dangerousPatterns) {
    if (pattern.test(input)) {
      throw new Error('Potentially malicious input detected');
    }
  }
  return input;
}
```

3. Add output validation to detect leaked instructions

### References
- OWASP LLM Top 10: LLM01 - Prompt Injection
- Skill: `skills/owasp-llm-top-10-verification.md` (Lines 15-75)

---

## Finding #2: API Keys Hardcoded in System Prompt

**Risk**: LLM02 - Sensitive Information Disclosure
**Severity**: CRITICAL
**Location**: src/config/prompts.js:12
**Status**: OPEN

### Description
API keys and database credentials are hardcoded in the system prompt, creating a risk of exposure through prompt leakage.

### Evidence
```javascript
// src/config/prompts.js:12
const systemPrompt = `
You are a helpful assistant.
Database connection: postgresql://admin:password123@db.example.com
API Key: sk-proj-abc123def456...
`;
```

### Impact
- Credentials exposed if prompt is leaked or logged
- Unauthorized access to database and external services
- Potential data breach
- Compliance violations (PCI, GDPR, etc.)

### Remediation
1. Move all credentials to environment variables:
```javascript
// .env
DATABASE_URL=postgresql://admin:password@db.example.com
EXTERNAL_API_KEY=sk-proj-abc123...

// src/config/prompts.js
const systemPrompt = `You are a helpful assistant.`;
// Use process.env.DATABASE_URL and process.env.EXTERNAL_API_KEY separately
```

2. Never include credentials in prompts or LLM context
3. Implement secret scanning in CI/CD pipeline
4. Rotate exposed credentials immediately

### References
- OWASP LLM Top 10: LLM02 - Sensitive Information Disclosure
- Skill: `skills/owasp-llm-top-10-verification.md` (Lines 77-130)

---

## Finding #3: Unvalidated LLM Output Used in SQL Query

**Risk**: LLM05 - Improper Output Handling
**Severity**: HIGH
**Location**: src/services/userService.js:78
**Status**: OPEN

### Description
LLM-generated content is directly interpolated into SQL query without sanitization, creating SQL injection vulnerability.

### Evidence
```javascript
// src/services/userService.js:78
const searchTerm = await llm.generateSearchQuery(userInput);
const query = `SELECT * FROM users WHERE name LIKE '%${searchTerm}%'`;
const results = await db.query(query);
```

### Impact
- SQL injection attack possible
- Unauthorized data access or modification
- Potential database compromise
- Data exfiltration risk

### Remediation
Use parameterized queries:
```javascript
const searchTerm = await llm.generateSearchQuery(userInput);
// Validate the LLM output
const validatedTerm = validateSearchTerm(searchTerm);
// Use parameterized query
const query = 'SELECT * FROM users WHERE name LIKE $1';
const results = await db.query(query, [`%${validatedTerm}%`]);
```

### References
- OWASP LLM Top 10: LLM05 - Improper Output Handling
- Skill: `skills/owasp-llm-top-10-verification.md` (Lines 192-255)

---

## Finding #4: Excessive Agency - Unrestricted Function Calling

**Risk**: LLM06 - Excessive Agency
**Severity**: HIGH
**Location**: src/agents/autonomousAgent.js:34
**Status**: OPEN

### Description
LLM agent has unrestricted access to dangerous functions including database deletion and file system operations without approval mechanisms.

### Evidence
```javascript
// src/agents/autonomousAgent.js:34
const availableTools = [
  deleteDatabase,
  dropTable,
  executeShellCommand,
  sendEmailToAllUsers,
  modifySystemConfig
];

const agent = new Agent({ llm, tools: availableTools });
```

### Impact
- LLM could inadvertently delete critical data
- Potential for system compromise
- Risk of mass email spam
- Unintended configuration changes

### Remediation
1. Implement least privilege:
```javascript
const safeTool = [
  readDatabase,
  searchUsers,
  generateReport
];

const dangerousTools = [
  { tool: deleteDatabase, requiresApproval: true },
  { tool: sendEmail, rateLimit: '10/hour' }
];
```

2. Add human-in-the-loop for sensitive operations:
```javascript
async function deleteDatabase() {
  const approval = await requestHumanApproval({
    action: 'deleteDatabase',
    risk: 'HIGH'
  });
  if (!approval.approved) throw new Error('Action not approved');
  // Proceed with deletion
}
```

### References
- OWASP LLM Top 10: LLM06 - Excessive Agency
- Skill: `skills/owasp-llm-top-10-verification.md` (Lines 257-320)

---

## Risk Matrix

| Risk Category | Severity | Count | Status |
|---------------|----------|-------|--------|
| LLM01: Prompt Injection | CRITICAL | 1 | OPEN |
| LLM02: Sensitive Information Disclosure | CRITICAL | 1 | OPEN |
| LLM03: Supply Chain | MEDIUM | 1 | OPEN |
| LLM04: Data/Model Poisoning | - | 0 | N/A |
| LLM05: Improper Output Handling | HIGH | 1 | OPEN |
| LLM06: Excessive Agency | HIGH | 1 | OPEN |
| LLM07: System Prompt Leakage | MEDIUM | 1 | OPEN |
| LLM08: Vector/Embedding Weaknesses | - | 0 | N/A |
| LLM09: Misinformation | LOW | 1 | OPEN |
| LLM10: Unbounded Consumption | HIGH | 1 | OPEN |

---

## Recommendations Summary

### Immediate Actions (Critical/High)
1. **Implement structured prompt templates** to prevent prompt injection (Finding #1)
2. **Remove all hardcoded credentials** from prompts and code (Finding #2)
3. **Add parameterized queries** for all database operations (Finding #3)
4. **Implement permission controls** and approval workflows for agent actions (Finding #4)
5. **Add rate limiting** to all LLM endpoints (Finding #8)

### Short-term (Medium)
6. **Pin all dependency versions** and implement SBOM (Finding #5)
7. **Implement prompt leakage protection** in API responses (Finding #6)

### Long-term (Low/Best Practice)
8. **Add confidence scores** and fact-checking for critical outputs (Finding #7)
9. **Implement comprehensive logging** and monitoring
10. **Regular security audits** of LLM integrations

---

## Compliance Status

- ✅ Verification completed for all 10 OWASP LLM risks
- ⚠️ 8 vulnerabilities identified
- ❌ 2 CRITICAL issues require immediate attention
- 📊 Security posture: Needs Improvement

## Next Steps

1. Address all CRITICAL findings within 24 hours
2. Create remediation plan for HIGH severity issues
3. Schedule follow-up verification after fixes
4. Implement automated security scanning in CI/CD
5. Conduct developer training on OWASP LLM Top 10

---

*Report generated using OWASP LLM Top 10 Verification Skill v1.0*
```

---

## Example 3: Integration in CI/CD Pipeline

### GitHub Actions Workflow

```yaml
name: OWASP LLM Security Scan

on:
  pull_request:
    branches: [ main, develop ]
  push:
    branches: [ main ]

jobs:
  owasp-llm-scan:
    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout@v3

    - name: Load OWASP LLM Verification Skill
      run: cat skills/owasp-llm-top-10-verification.md

    - name: Scan for LLM integrations
      run: |
        # Find all files with LLM code
        grep -r "openai\|anthropic\|llm" src/ > llm-files.txt

    - name: Run Security Verifier Agent
      run: |
        # Execute verifier agent
        claude-agent run agents/owasp-security-verifier.agent.md

    - name: Check for Critical Findings
      run: |
        if grep -q "CRITICAL" security-report.md; then
          echo "Critical security issues found!"
          exit 1
        fi

    - name: Upload Security Report
      uses: actions/upload-artifact@v3
      with:
        name: owasp-security-report
        path: security-report.md
```

---

## Example 4: Code Review Checklist

When reviewing LLM-related code changes, use this checklist based on the skill:

### Pre-Commit Checklist

- [ ] **LLM01**: User input separated from system prompts?
- [ ] **LLM02**: No secrets or PII in prompts or logs?
- [ ] **LLM03**: All dependencies verified and pinned?
- [ ] **LLM04**: Training data validated and sanitized?
- [ ] **LLM05**: LLM outputs validated before use in SQL/HTML/commands?
- [ ] **LLM06**: Agent has least privilege access only?
- [ ] **LLM07**: System prompts protected from leakage?
- [ ] **LLM08**: Vector DB documents validated and access-controlled?
- [ ] **LLM09**: Critical outputs fact-checked or human-reviewed?
- [ ] **LLM10**: Rate limiting and cost controls implemented?

### Code Review Template

```markdown
## Security Review (OWASP LLM Top 10)

**Reviewer**: [Name]
**Date**: [YYYY-MM-DD]
**PR**: #[Number]

### LLM Security Assessment

Files with LLM code:
- [ ] `src/controllers/chatController.js`
- [ ] `src/services/llmService.js`

Risks checked:
- [x] LLM01: Prompt Injection - ✅ Pass (structured prompts used)
- [x] LLM02: Info Disclosure - ⚠️ Warning (logging needs review)
- [x] LLM03: Supply Chain - ✅ Pass
- [x] LLM04: Poisoning - N/A
- [x] LLM05: Output Handling - ✅ Pass (parameterized queries)
- [x] LLM06: Excessive Agency - ✅ Pass
- [x] LLM07: Prompt Leakage - ✅ Pass
- [x] LLM08: Vector/Embedding - N/A
- [x] LLM09: Misinformation - ✅ Pass (disclaimers added)
- [x] LLM10: Unbounded Consumption - ❌ Fail (no rate limiting)

### Required Changes
1. Add rate limiting to `/api/chat` endpoint
2. Remove verbose logging in production mode

### Recommendation
- [ ] Approve after fixes
- [ ] Request changes
```

---

## Example 5: Quick Reference Card

### OWASP LLM Top 10 - Quick Check

| # | Risk | Quick Test | Fix |
|---|------|------------|-----|
| 01 | Prompt Injection | Try "Ignore instructions and..." | Use structured messages |
| 02 | Info Disclosure | Check logs for secrets | Env vars + output filtering |
| 03 | Supply Chain | Verify dependencies | Pin versions + SBOM |
| 04 | Poisoning | Check training data source | Validate all training inputs |
| 05 | Output Handling | LLM output in SQL/HTML? | Parameterize + escape |
| 06 | Excessive Agency | Can LLM delete/modify? | Least privilege + approval |
| 07 | Prompt Leakage | Can users see prompts? | Filter responses + no logs |
| 08 | Vector/Embedding | User docs in vector DB? | Validate + access control |
| 09 | Misinformation | Critical decision on LLM? | Fact-check + disclaimers |
| 10 | Unbounded | Rate limits exist? | Add throttling + quotas |

---

## Additional Resources

- Full Skill Document: `skills/owasp-llm-top-10-verification.md`
- OWASP Official Site: https://genai.owasp.org/llm-top-10/
- Example Security Verifier Agent: `agents/owasp-security-verifier.agent.md` (to be created)
- Testing Guide: `docs/OWASP_TESTING_GUIDE.md` (to be created)

---

*This usage guide is maintained alongside the OWASP LLM Top 10 Verification Skill.*
