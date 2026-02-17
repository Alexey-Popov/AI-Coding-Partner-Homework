# Skills Directory

Universal skills for AI coding assistants across the entire repository.

---

## What are Skills?

**Skills** are structured knowledge documents that define methodologies, frameworks, and quality standards for AI assistants to follow when performing specific tasks.

Skills enable consistent application of best practices across:
- **GitHub Copilot** (via `.github/copilot-instructions.md`)
- **Claude Code** (via agent configurations)
- **Cursor** (via `.cursorrules`)
- **Other AI coding assistants**

---

## Available Skills

### OWASP LLM Top 10 Verification Skill

**File**: [`owasp-llm-top-10-verification.md`](owasp-llm-top-10-verification.md)

**Purpose**: Comprehensive security verification for LLM-powered applications based on OWASP Top 10 for Large Language Model Applications (2025).

**Covers**:
1. **LLM01**: Prompt Injection
2. **LLM02**: Sensitive Information Disclosure
3. **LLM03**: Supply Chain Vulnerabilities
4. **LLM04**: Data and Model Poisoning
5. **LLM05**: Improper Output Handling
6. **LLM06**: Excessive Agency
7. **LLM07**: System Prompt Leakage
8. **LLM08**: Vector and Embedding Weaknesses
9. **LLM09**: Misinformation
10. **LLM10**: Unbounded Consumption

**Features**:
- Detection patterns for each vulnerability
- Vulnerable and secure code examples (JavaScript & Python)
- Severity ratings (CRITICAL, HIGH, MEDIUM, LOW, INFO)
- Mitigation strategies and remediation guidance
- Standardized security report format
- Code review checklist

**When to Use**:
- Building or reviewing LLM-powered applications
- Security audits of AI features
- Code reviews involving LLM integrations
- CI/CD security scanning
- Developer training on LLM security

---

## How to Use Skills

### For GitHub Copilot Users

Skills are automatically active! The OWASP LLM security guidelines are integrated into `.github/copilot-instructions.md`.

Copilot will:
- ✅ Generate secure LLM code following OWASP guidelines
- ✅ Avoid common vulnerabilities (prompt injection, output handling, etc.)
- ✅ Suggest secure patterns automatically

**To reference explicitly**:
```javascript
// GitHub Copilot will follow OWASP LLM guidelines
// See: skills/owasp-llm-top-10-verification.md
```

### For Claude Code Users

Reference skills in your agent configurations:

```markdown
# Security Verifier Agent

## Required Skills
- `skills/owasp-llm-top-10-verification.md`

## Process
1. Read the OWASP LLM Top 10 skill
2. Scan codebase for LLM integration points
3. Check against all 10 risks
4. Generate security report
```

### For Cursor Users

Add to `.cursorrules`:
```
# OWASP LLM Security
- Follow skills/owasp-llm-top-10-verification.md
- Check all LLM code against the 10 security risks
- Use structured prompts, validate outputs, implement rate limiting
```

### For Manual Code Reviews

Use the skill as a checklist:

```bash
# Open the skill
cat skills/owasp-llm-top-10-verification.md

# Review code changes
git diff main...feature/llm-integration

# Check each risk:
# ✓ LLM01: Structured prompts? ✓
# ✓ LLM02: No secrets? ✓
# ✗ LLM10: Missing rate limiting → Add TODO
```

---

## Quick Security Checklist

Before committing LLM-related code:

- [ ] **LLM01**: User input separated from system prompts?
- [ ] **LLM02**: No API keys or secrets in prompts/logs?
- [ ] **LLM03**: All dependencies pinned and verified?
- [ ] **LLM04**: Training data validated?
- [ ] **LLM05**: LLM outputs validated before use in SQL/HTML/commands?
- [ ] **LLM06**: Agent has least privilege access only?
- [ ] **LLM07**: System prompts protected from leakage?
- [ ] **LLM08**: Vector DB documents validated?
- [ ] **LLM09**: AI content includes disclaimers?
- [ ] **LLM10**: Rate limiting and timeouts implemented?

---

## Integration Examples

### CI/CD Pipeline

```yaml
# .github/workflows/security-scan.yml
name: OWASP LLM Security Scan

on: [pull_request]

jobs:
  security:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Scan for LLM vulnerabilities
        run: |
          # Reference the skill
          cat skills/owasp-llm-top-10-verification.md
          # Run security checks
          ./scripts/owasp-llm-scan.sh
```

### Pre-commit Hook

```bash
#!/bin/bash
# .git/hooks/pre-commit

# Check if LLM code is being committed
if git diff --cached | grep -E "llm|openai|anthropic|claude"; then
  echo "LLM code detected. Running OWASP LLM security checks..."
  # Reference checklist from skill
  echo "See: skills/owasp-llm-top-10-verification.md"
fi
```

### Code Review Template

```markdown
## Security Review (OWASP LLM Top 10)

Skill: `skills/owasp-llm-top-10-verification.md`

Risks Checked:
- [x] LLM01: Prompt Injection - ✅ Pass
- [x] LLM02: Info Disclosure - ✅ Pass
- [x] LLM05: Output Handling - ⚠️ Needs SQL parameterization
- [x] LLM10: Resource Consumption - ❌ No rate limiting

Required Changes:
1. Add parameterized queries (LLM05)
2. Implement rate limiting (LLM10)
```

---

## Common Vulnerable Patterns

### ❌ Avoid These

```javascript
// VULNERABLE: Prompt injection (LLM01)
const prompt = systemPrompt + userInput;

// VULNERABLE: Secret exposure (LLM02)
const prompt = "API Key: sk-abc123...";

// VULNERABLE: SQL injection (LLM05)
const query = `SELECT * FROM users WHERE name = '${llmResponse}'`;

// VULNERABLE: No rate limiting (LLM10)
app.post('/api/chat', async (req, res) => {
  const response = await llm.complete(req.body.message);
});
```

### ✅ Use These Instead

```javascript
// SECURE: Structured messages (LLM01)
const messages = [
  { role: "system", content: "You are a helpful assistant." },
  { role: "user", content: userInput }
];

// SECURE: Environment variables (LLM02)
const apiKey = process.env.OPENAI_API_KEY;

// SECURE: Parameterized query (LLM05)
const query = 'SELECT * FROM users WHERE name = $1';
await db.query(query, [llmResponse]);

// SECURE: Rate limiting (LLM10)
app.post('/api/chat', rateLimit({ max: 10, windowMs: 60000 }),
  async (req, res) => {
    const response = await llm.complete(req.body.message, {
      max_tokens: 500,
      timeout: 30000
    });
  }
);
```

---

## Resources

### OWASP LLM Top 10
- **Official Site**: https://genai.owasp.org/llm-top-10/
- **2025 Documentation**: https://genai.owasp.org/resource/owasp-top-10-for-llm-applications-2025/
- **GitHub Repository**: https://github.com/OWASP/www-project-top-10-for-large-language-model-applications/

### This Repository
- **Copilot Instructions**: `.github/copilot-instructions.md`
- **Main README**: `../README.md`
- **Homework Examples**: `../homework-*/`

---

## Contributing New Skills

To add a new skill:

1. Create `skills/your-skill-name.md` following this structure:
   ```markdown
   # Your Skill Name

   ## Overview
   [What this skill covers]

   ## Methodology
   [Step-by-step guidance]

   ## Code Patterns
   [Examples and anti-patterns]

   ## Checklist
   [Quick reference]

   ## References
   [External resources]
   ```

2. Update `.github/copilot-instructions.md` to include your skill

3. Add to this README with:
   - Skill name and file
   - Purpose and coverage
   - When to use it

4. Provide examples for different AI tools

---

## Skill Versioning

Skills use semantic versioning:

- **Major (v2.0)**: Significant methodology changes
- **Minor (v1.1)**: New sections or expanded coverage
- **Patch (v1.0.1)**: Clarifications, fixes, typos

Current versions:
- `owasp-llm-top-10-verification.md`: **v1.0** (2025-02-17)

---

## Support

For questions or issues:
1. Check the skill documentation
2. Review examples in `.github/copilot-instructions.md`
3. Consult OWASP official resources
4. Contact course instructor

---

**Remember**: These skills are active across the entire repository for all AI coding assistants!

*Last Updated: 2025-02-17*
