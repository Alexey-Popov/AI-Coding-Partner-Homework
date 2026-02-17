# OWASP LLM Top 10 Verification Skill

A comprehensive security verification skill for checking applications against the OWASP Top 10 for Large Language Model Applications (2025).

---

## Overview

This project provides a **reusable skill** that defines a complete methodology for identifying and mitigating security vulnerabilities in LLM-powered applications. The skill is designed to be used by AI security agents, developers, and security teams to ensure applications comply with the latest OWASP LLM security standards.

### What is a Skill?

A **skill** is a structured knowledge document that defines:
- Specific methodologies and frameworks
- Detection patterns for vulnerabilities
- Quality standards and criteria
- Standardized output formats
- Remediation guidance

Skills enable AI agents and developers to consistently apply industry best practices and security standards.

---

## Features

### Comprehensive Coverage

The skill covers all **10 OWASP LLM security risks** for 2025:

1. **LLM01: Prompt Injection** - Manipulation of LLM behavior through crafted inputs
2. **LLM02: Sensitive Information Disclosure** - Inadvertent exposure of confidential data
3. **LLM03: Supply Chain Vulnerabilities** - Compromised dependencies or models
4. **LLM04: Data and Model Poisoning** - Malicious manipulation of training data
5. **LLM05: Improper Output Handling** - Insufficient validation leading to injection attacks
6. **LLM06: Excessive Agency** - Over-permissioned LLM agents
7. **LLM07: System Prompt Leakage** - Exposure of internal prompts and instructions
8. **LLM08: Vector and Embedding Weaknesses** - RAG and vector database vulnerabilities
9. **LLM09: Misinformation** - LLM hallucinations and overreliance
10. **LLM10: Unbounded Consumption** - Resource exhaustion and cost overruns

### Practical Security Guidance

For each risk, the skill provides:
- **Detailed descriptions** of the vulnerability
- **Detection patterns** to identify the issue
- **Code examples** showing vulnerable and secure patterns
- **Severity ratings** (CRITICAL, HIGH, MEDIUM, LOW, INFO)
- **Mitigation strategies** with concrete implementation guidance
- **References** to authoritative sources

### Standardized Reporting

The skill defines a consistent output format for security reports including:
- Verification summary with overall risk level
- Detailed findings with severity and location
- Evidence and impact assessment
- Remediation steps
- Risk matrix and recommendations

---

## Repository Structure

```
homework-4/
├── README.md                          # This file
├── TASKS.md                           # Assignment requirements
├── skills/
│   ├── README.md                      # Skills directory overview
│   ├── owasp-llm-top-10-verification.md   # Main skill document
│   └── USAGE_EXAMPLE.md               # Detailed usage examples
└── demo-bug-fix/                      # Demo application (separate)
```

---

## Quick Start

### 1. Review the Skill

Read the main skill document to understand the methodology:

```bash
cat skills/owasp-llm-top-10-verification.md
```

### 2. Study the Examples

Check the usage examples for practical applications:

```bash
cat skills/USAGE_EXAMPLE.md
```

### 3. Apply to Your Application

Use the skill to verify your LLM application:

**For AI Agents:**
```markdown
# Reference the skill in your agent configuration
## Required Skills
- `skills/owasp-llm-top-10-verification.md`
```

**For Developers:**
```bash
# Use as a security checklist during code review
# Check each LLM integration point against the 10 risks
```

---

## Use Cases

### 1. Security Verification Agent

Create an agent that automatically scans codebases for OWASP LLM vulnerabilities:

```markdown
# OWASP Security Verifier Agent

## Responsibilities
1. Read the OWASP LLM Top 10 Verification Skill
2. Scan codebase for LLM integration points
3. Check each integration against all 10 risks
4. Generate security report using skill's output format

## Required Skills
- `skills/owasp-llm-top-10-verification.md`
```

### 2. Code Review Checklist

Use during code reviews to ensure security compliance:

- [ ] LLM01: User input separated from system prompts?
- [ ] LLM02: No secrets in prompts or logs?
- [ ] LLM05: LLM outputs validated before use in SQL/HTML?
- [ ] LLM06: Agent has least privilege access?
- [ ] LLM10: Rate limiting implemented?

### 3. CI/CD Integration

Integrate into your continuous integration pipeline:

```yaml
# .github/workflows/owasp-llm-scan.yml
- name: OWASP LLM Security Scan
  run: |
    # Load skill and run verification
    claude-agent run agents/owasp-security-verifier.agent.md
    # Fail if critical issues found
    grep -q "CRITICAL" security-report.md && exit 1
```

### 4. Security Training

Use as educational material for developers learning about LLM security:
- Study each of the 10 risks
- Review vulnerable code patterns
- Understand mitigation strategies
- Practice identifying vulnerabilities

---

## Example Security Report

Here's what a report generated using this skill looks like:

```markdown
# OWASP LLM Top 10 Verification Report

Application: ChatBot API
Overall Risk Level: HIGH
Total Findings: 5
├─ Critical: 2
├─ High: 2
└─ Medium: 1

## Finding #1: Prompt Injection via Direct Concatenation
Risk: LLM01 - Prompt Injection
Severity: CRITICAL
Location: src/chat.js:45

[Detailed finding with evidence, impact, and remediation...]

## Risk Matrix
| Risk | Severity | Count | Status |
|------|----------|-------|--------|
| LLM01: Prompt Injection | CRITICAL | 1 | OPEN |
| LLM02: Info Disclosure | CRITICAL | 1 | OPEN |
...
```

See `skills/USAGE_EXAMPLE.md` for complete report examples.

---

## Key Concepts

### Severity Levels

The skill defines 5 severity levels:

- **CRITICAL**: Immediate exploitation possible with severe impact
- **HIGH**: Exploitation likely with significant impact
- **MEDIUM**: Exploitation possible with moderate impact
- **LOW**: Minor security concern with limited impact
- **INFO**: No immediate security impact, best practice recommendation

### Verification Methodology

The skill follows a 4-step process:

1. **Scope Definition**: Identify all LLM integration points
2. **Risk Assessment**: Check each integration against the 10 risks
3. **Testing**: Perform security testing (prompt injection, output validation, etc.)
4. **Documentation**: Generate comprehensive report

### Detection Patterns

The skill provides specific code patterns to look for, such as:

```javascript
// VULNERABLE: Direct prompt concatenation (LLM01)
const prompt = systemPrompt + userInput;

// VULNERABLE: Secrets in prompts (LLM02)
const prompt = "API Key: sk-abc123...";

// VULNERABLE: SQL injection via LLM output (LLM05)
const query = `SELECT * FROM users WHERE name = '${llmResponse}'`;

// VULNERABLE: Unrestricted agent (LLM06)
const agent = new Agent({ tools: [deleteDatabase, sendEmail] });
```

---

## Documentation

### Main Documents

1. **Main Skill**: `skills/owasp-llm-top-10-verification.md`
   - Complete methodology and reference guide
   - All 10 risks with detection and mitigation
   - Output format specifications

2. **Usage Examples**: `skills/USAGE_EXAMPLE.md`
   - Sample security reports
   - Agent configurations
   - Code review templates
   - CI/CD integration examples

3. **Skills README**: `skills/README.md`
   - Overview of the skills system
   - How to create and use skills
   - Best practices

### External References

- [OWASP LLM Top 10 Official Site](https://genai.owasp.org/llm-top-10/)
- [OWASP LLM Top 10 2025 Documentation](https://genai.owasp.org/resource/owasp-top-10-for-llm-applications-2025/)
- [OWASP GenAI Security Project](https://genai.owasp.org/)
- [GitHub Repository](https://github.com/OWASP/www-project-top-10-for-large-language-model-applications/)

---

## Benefits

### For Security Teams

- **Standardized assessments** across all LLM applications
- **Comprehensive coverage** of latest OWASP risks
- **Consistent reporting** for tracking and compliance
- **Actionable remediation** guidance

### For Developers

- **Security checklist** integrated into development workflow
- **Concrete examples** of vulnerable and secure code
- **Best practices** for LLM security
- **Early detection** of vulnerabilities before production

### For AI Agents

- **Clear methodology** to follow during verification
- **Structured output** format for reports
- **Reference material** for security knowledge
- **Consistent quality** across multiple runs

### For Organizations

- **Compliance** with OWASP standards
- **Risk reduction** in LLM deployments
- **Knowledge base** for security training
- **Automation** of security verification

---

## Integration with Development Workflow

### Pre-Commit

```bash
# Check staged LLM code before commit
git diff --cached | grep -E "llm|openai|anthropic"
# Review against OWASP checklist
```

### Code Review

```markdown
## Security Checklist (OWASP LLM Top 10)
- [ ] All 10 risks reviewed
- [ ] No critical issues found
- [ ] Remediation plan for medium/high issues
```

### CI/CD Pipeline

```yaml
security-scan:
  - Load OWASP LLM skill
  - Run security verifier agent
  - Generate report
  - Fail on critical findings
```

### Regular Audits

```bash
# Monthly security audit
./scripts/owasp-llm-audit.sh
# Review security-report.md
# Track trends over time
```

---

## Future Enhancements

Potential additions to the skill:

- [ ] Automated testing scripts for each risk
- [ ] Integration with static analysis tools (Semgrep, CodeQL)
- [ ] Language-specific detection patterns (Python, JavaScript, etc.)
- [ ] Remediation code templates
- [ ] Compliance mapping (SOC2, ISO 27001, etc.)
- [ ] Risk scoring calculator
- [ ] Trend analysis and metrics

---

## Contributing

To contribute improvements to this skill:

1. Review the current skill document
2. Identify gaps or areas for improvement
3. Add detection patterns, examples, or guidance
4. Update version history
5. Submit changes for review

### Areas for Contribution

- Additional vulnerable code examples
- More mitigation strategies
- Tool integrations
- Testing methodologies
- Case studies
- Language-specific guidance

---

## Version History

### v1.0 (2025-02-17)
- Initial release
- Complete coverage of OWASP LLM Top 10 (2025)
- All 10 risks documented with detection patterns
- Standardized output format
- Usage examples and documentation

---

## License

This skill is provided as educational material for the AI-Assisted Development course.

---

## Authors

Created as part of the AI-Assisted Development Course - Homework 4

---

## Support

For questions or issues:
- Review the documentation in `skills/`
- Check usage examples in `USAGE_EXAMPLE.md`
- Consult OWASP official resources
- Contact course instructor

---

## Acknowledgments

Based on:
- OWASP Top 10 for Large Language Model Applications 2025
- OWASP GenAI Security Project
- Industry best practices for LLM security
- Community feedback and contributions

---

**Last Updated**: 2025-02-17
**Skill Version**: 1.0
**OWASP Version**: 2025
