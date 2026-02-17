# Skills Directory

This directory contains reusable **skills** that define methodologies, frameworks, and quality standards for AI agents to follow when performing specific tasks.

---

## What are Skills?

**Skills** are structured knowledge documents that:
- Define specific methodologies or frameworks (e.g., OWASP LLM Top 10, FIRST testing principles)
- Provide consistent quality standards and measurement criteria
- Offer detection patterns and remediation guidance
- Ensure agents follow industry best practices
- Enable reproducible and standardized outputs

Skills are referenced by agents to ensure they perform tasks according to established standards and best practices.

---

## Available Skills

### 1. OWASP LLM Top 10 Verification Skill

**File**: `owasp-llm-top-10-verification.md`

**Purpose**: Comprehensive methodology for verifying applications against the OWASP Top 10 for Large Language Model Applications (2025).

**Use Cases**:
- Security verification of LLM-powered applications
- Code review for LLM integrations
- Compliance checking against OWASP standards
- Security testing and vulnerability assessment

**Key Features**:
- Coverage of all 10 OWASP LLM security risks (2025 version)
- Detection patterns and vulnerable code examples
- Severity definitions (CRITICAL, HIGH, MEDIUM, LOW, INFO)
- Structured output format for security reports
- Remediation guidance for each vulnerability type
- Risk assessment methodology

**The 10 Risks Covered**:
1. LLM01: Prompt Injection
2. LLM02: Sensitive Information Disclosure
3. LLM03: Supply Chain Vulnerabilities
4. LLM04: Data and Model Poisoning
5. LLM05: Improper Output Handling
6. LLM06: Excessive Agency
7. LLM07: System Prompt Leakage
8. LLM08: Vector and Embedding Weaknesses
9. LLM09: Misinformation
10. LLM10: Unbounded Consumption

**Documentation**:
- Main skill: `owasp-llm-top-10-verification.md`
- Usage examples: `USAGE_EXAMPLE.md`
- Quick reference: See section in usage examples

**When to Use**:
- Before deploying LLM-powered applications to production
- During code reviews of LLM integrations
- As part of CI/CD security pipelines
- When auditing existing LLM applications
- For security training and education

---

## How to Use Skills

### For AI Agents

1. **Reference the skill** in your agent configuration:
   ```markdown
   ## Required Skills
   - `skills/owasp-llm-top-10-verification.md`
   ```

2. **Load and study the skill** before performing tasks:
   ```
   Agent: Reading skill: skills/owasp-llm-top-10-verification.md
   Agent: Understanding the 10 security risks and detection patterns...
   Agent: Ready to perform verification according to the skill.
   ```

3. **Follow the methodology** defined in the skill:
   - Use the detection patterns to identify vulnerabilities
   - Apply severity definitions consistently
   - Follow the output format for reports
   - Reference the skill in your findings

4. **Output results** in the format specified by the skill

### For Developers

1. **Review the skill** to understand security requirements:
   ```bash
   cat skills/owasp-llm-top-10-verification.md
   ```

2. **Use as a checklist** during development and code review

3. **Reference in documentation** when explaining security measures

4. **Integrate into CI/CD** pipelines for automated checks

---

## Skill Structure

Each skill document typically includes:

### 1. Overview
- Purpose and scope of the skill
- When and how to use it

### 2. Core Content
- Definitions and concepts
- Methodologies and frameworks
- Detection patterns or quality criteria
- Examples and anti-patterns

### 3. Methodology
- Step-by-step process to follow
- Assessment criteria
- Testing procedures

### 4. Output Format
- Standardized report structure
- Required sections and fields
- Severity/quality definitions

### 5. References
- External standards and documentation
- Related resources

---

## Creating New Skills

When creating a new skill, follow this template:

```markdown
# [Skill Name]

## Overview
[Brief description and purpose]

## [Main Content Sections]
### [Section 1]
[Content with examples, patterns, criteria]

### [Section 2]
[Content...]

## Methodology
[Step-by-step process]

## Output Format
[Standardized format for results]

## References
[External resources]
```

### Guidelines for Skill Creation

1. **Be Specific**: Provide concrete examples and patterns
2. **Be Actionable**: Include clear steps and procedures
3. **Be Consistent**: Use standardized formats and terminology
4. **Be Complete**: Cover all aspects of the methodology
5. **Be Referenced**: Link to authoritative sources
6. **Be Maintainable**: Include version history and update dates

---

## Skill Usage Examples

### Example 1: Security Verifier Agent

```markdown
# Security Verifier Agent

## Role
Perform OWASP LLM Top 10 security verification.

## Process
1. Read `skills/owasp-llm-top-10-verification.md`
2. Scan codebase for LLM integrations
3. Check each integration against the 10 risks
4. Generate report using skill's output format

## Required Skills
- `skills/owasp-llm-top-10-verification.md`
```

### Example 2: Code Review

```bash
# Developer uses skill as checklist
$ cat skills/owasp-llm-top-10-verification.md

# Review code changes
$ git diff main...feature/llm-chat

# Check against each risk:
# ✓ LLM01: Using structured prompts
# ✗ LLM10: Missing rate limiting → Add TODO
```

---

## Integration with Agents

### Agent Types That Use Skills

1. **Verifier Agents**: Use skills to check quality/compliance
   - Bug Research Verifier → Research Quality Skill
   - Security Verifier → OWASP LLM Top 10 Skill
   - Code Quality Verifier → Code Standards Skill

2. **Generator Agents**: Use skills to ensure output quality
   - Unit Test Generator → FIRST Principles Skill
   - Documentation Generator → Doc Standards Skill

3. **Reviewer Agents**: Use skills as review criteria
   - Code Reviewer → Multiple quality skills
   - Security Reviewer → Security standards skills

### Skill Chaining

Agents can use multiple skills together:

```markdown
## Required Skills
- `skills/owasp-llm-top-10-verification.md` (Security)
- `skills/code-quality-standards.md` (Code Quality)
- `skills/performance-benchmarks.md` (Performance)
```

---

## Best Practices

### For Skill Authors

1. **Keep skills focused**: One skill = one methodology/framework
2. **Update regularly**: Maintain version history and update with new standards
3. **Provide examples**: Include both good and bad examples
4. **Test thoroughly**: Validate with real-world scenarios
5. **Document clearly**: Use clear language and structure

### For Skill Users (Agents/Developers)

1. **Read completely**: Understand the full skill before applying it
2. **Follow exactly**: Don't skip steps or modify the methodology
3. **Reference properly**: Cite the skill in findings and reports
4. **Provide feedback**: Report issues or suggestions for improvement
5. **Combine appropriately**: Use multiple skills when needed

---

## Skill Versioning

Skills follow semantic versioning:

- **Major version** (v2.0): Significant methodology changes
- **Minor version** (v1.1): New sections or expanded content
- **Patch version** (v1.0.1): Bug fixes, clarifications, typos

Example:
```
v1.0.0 (2025-02-17): Initial release
v1.1.0 (2025-03-01): Added new risk LLM11
v1.1.1 (2025-03-05): Fixed typo in severity definition
```

---

## Repository Structure

```
skills/
├── README.md                              # This file
├── owasp-llm-top-10-verification.md      # OWASP LLM skill
├── USAGE_EXAMPLE.md                       # Usage examples
└── [future-skills].md                     # Additional skills
```

---

## Related Documentation

- **Agents Directory**: `../agents/` - Agents that use these skills
- **Usage Examples**: `USAGE_EXAMPLE.md` - Detailed examples
- **Homework Tasks**: `../TASKS.md` - Assignment requirements

---

## Contributing

To contribute a new skill:

1. Create a new markdown file in this directory
2. Follow the skill structure template
3. Provide usage examples
4. Update this README with the new skill
5. Submit for review

---

## Resources

### OWASP LLM Top 10
- Official Website: https://genai.owasp.org/llm-top-10/
- 2025 Documentation: https://genai.owasp.org/resource/owasp-top-10-for-llm-applications-2025/
- GitHub Repository: https://github.com/OWASP/www-project-top-10-for-large-language-model-applications/

### Additional Standards
- OWASP Top 10 (Web Applications): https://owasp.org/www-project-top-ten/
- NIST AI Risk Management Framework: https://www.nist.gov/itl/ai-risk-management-framework
- ISO/IEC 42001 (AI Management): https://www.iso.org/standard/81230.html

---

## License

These skills are provided as educational materials for the AI-Assisted Development course.

---

## Contact

For questions, issues, or suggestions regarding these skills:
- Create an issue in the course repository
- Contact the course instructor
- Contribute improvements via pull request

---

*Last Updated: 2025-02-17*
*Version: 1.0*
