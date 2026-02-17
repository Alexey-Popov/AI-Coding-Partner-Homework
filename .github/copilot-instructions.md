# GitHub Copilot Instructions — AI Coding Partner Homework Repository

> These rules apply to ALL code generation, completion, and suggestion in this project.

---

## Project Context

This repository contains homework assignments for the **AI as a Personalized Coding Partner** course. Projects may involve various technologies, but all LLM-powered code must follow strict security guidelines.

---

## Security Requirements — OWASP LLM Top 10 🔒

**CRITICAL**: All LLM-powered code in this repository must comply with the OWASP Top 10 for Large Language Model Applications (2025).

Full documentation: [`skills/owasp-llm-top-10-verification.md`](../skills/owasp-llm-top-10-verification.md)

### ALWAYS Do When Working with LLMs

#### LLM01: Prevent Prompt Injection
- ✅ Use structured message formats with separate roles (`system`, `user`, `assistant`)
- ✅ Implement input validation and sanitization
- ✅ Use delimiter tokens to separate system and user content
- ❌ **NEVER** concatenate user input directly with system prompts

```javascript
// ✅ CORRECT: Structured messages
const messages = [
  { role: "system", content: "You are a helpful assistant." },
  { role: "user", content: userInput }
];

// ❌ WRONG: Direct concatenation
const prompt = `You are a helpful assistant. ${userInput}`;
```

```python
# ✅ CORRECT: Structured messages
messages = [
    {"role": "system", "content": "You are a helpful assistant."},
    {"role": "user", "content": user_input}
]

# ❌ WRONG: Direct concatenation
prompt = f"You are a helpful assistant. {user_input}"
```

#### LLM02: Protect Sensitive Information
- ✅ Store all secrets in environment variables
- ✅ Filter PII from outputs before display
- ✅ Never log full conversation histories in production
- ❌ **NEVER** include API keys, passwords, or credentials in prompts
- ❌ **NEVER** log sensitive data (PII, API keys, full prompts)

```javascript
// ✅ CORRECT: Secrets in environment
const apiKey = process.env.OPENAI_API_KEY;

// ❌ WRONG: Hardcoded secrets
const systemPrompt = "You are a bot with API key: sk-abc123...";
```

#### LLM03: Secure Supply Chain
- ✅ Pin all LLM dependency versions
- ✅ Use verified model sources only
- ✅ Verify model integrity (checksums, signatures)
- ❌ **NEVER** use wildcard versions (`*` or `latest`)
- ❌ **NEVER** load unverified third-party models

```json
// ✅ CORRECT: Pinned versions
"dependencies": {
  "openai": "4.28.0",
  "langchain": "0.1.25"
}

// ❌ WRONG: Wildcard versions
"dependencies": {
  "openai": "*",
  "langchain": "latest"
}
```

#### LLM05: Validate All LLM Outputs
- ✅ Treat LLM output as untrusted user input
- ✅ Use parameterized queries for SQL
- ✅ HTML-escape outputs before rendering
- ✅ Sandbox execution of LLM-generated code
- ❌ **NEVER** use LLM output directly in SQL queries
- ❌ **NEVER** render LLM output as HTML without escaping
- ❌ **NEVER** use `eval()` or `exec()` on LLM-generated code
- ❌ **NEVER** pass LLM output to system commands

```javascript
// ✅ CORRECT: Parameterized query
const query = 'SELECT * FROM users WHERE name = $1';
await db.query(query, [llmResponse]);

// ❌ WRONG: Direct interpolation
const query = `SELECT * FROM users WHERE name = '${llmResponse}'`;

// ✅ CORRECT: HTML escape
res.send(`<div>${escapeHtml(llmResponse)}</div>`);

// ❌ WRONG: No escaping
res.send(`<div>${llmResponse}</div>`);
```

```python
# ✅ CORRECT: Parameterized query
cursor.execute("SELECT * FROM users WHERE name = %s", (llm_response,))

# ❌ WRONG: String formatting
cursor.execute(f"SELECT * FROM users WHERE name = '{llm_response}'")
```

#### LLM06: Implement Least Privilege
- ✅ Limit LLM agent permissions to minimum necessary
- ✅ Require human approval for sensitive operations
- ✅ Implement rate limiting on LLM-initiated actions
- ❌ **NEVER** give LLM unrestricted access to destructive operations
- ❌ **NEVER** allow LLM to delete data without approval

```javascript
// ✅ CORRECT: Limited permissions with approval
const safeTool = [readData, searchUsers];
const sensitiveTool = [
  { tool: deleteData, requiresApproval: true }
];

// ❌ WRONG: Unrestricted access
const tools = [deleteDatabase, modifyUsers, sendEmailToAll];
const agent = new Agent({ llm, tools });
```

#### LLM07: Prevent System Prompt Leakage
- ✅ Filter system prompts from API responses
- ✅ Never log full system prompts
- ✅ Implement prompt extraction detection
- ❌ **NEVER** return full conversation history including system prompts
- ❌ **NEVER** expose system prompts in error messages

```javascript
// ✅ CORRECT: Filter system messages
const userVisibleHistory = conversation.filter(m => m.role !== 'system');

// ❌ WRONG: Return everything
res.json({ conversation }); // Includes system prompt
```

#### LLM08: Secure Vector Databases
- ✅ Validate documents before embedding
- ✅ Implement access controls on vector stores
- ✅ Verify retrieved document integrity
- ❌ **NEVER** add user content to vector DB without validation

```javascript
// ✅ CORRECT: Validation before embedding
async function addDocument(doc) {
  const validated = await validateDocument(doc);
  const embedding = await embedder.embed(validated.text);
  await vectorDB.insert(embedding, { userId: doc.userId });
}

// ❌ WRONG: No validation
await vectorDB.insert(await embedder.embed(userDoc.text));
```

#### LLM09: Address Misinformation
- ✅ Add disclaimers for AI-generated content
- ✅ Display confidence scores where applicable
- ✅ Require human review for critical decisions
- ❌ **NEVER** present LLM output as guaranteed factual
- ❌ **NEVER** use LLM alone for medical, legal, or financial advice

```javascript
// ✅ CORRECT: With disclaimer
return {
  answer: llmResponse,
  disclaimer: "AI-generated content may contain inaccuracies. Verify important information."
};

// ❌ WRONG: No disclaimer
return { answer: llmResponse, verified: true };
```

#### LLM10: Control Resource Consumption
- ✅ Implement rate limiting on all LLM endpoints
- ✅ Set token limits on requests
- ✅ Add timeouts to all LLM calls
- ✅ Monitor costs and set budgets
- ❌ **NEVER** allow unlimited LLM requests
- ❌ **NEVER** omit `max_tokens` parameter

```javascript
// ✅ CORRECT: With limits
app.post('/api/chat', rateLimit({ max: 10, windowMs: 60000 }), async (req, res) => {
  const response = await llm.complete(req.body.message, {
    max_tokens: 500,
    timeout: 30000
  });
});

// ❌ WRONG: No limits
app.post('/api/chat', async (req, res) => {
  const response = await llm.complete(req.body.message);
});
```

```python
# ✅ CORRECT: With timeout and token limit
response = await asyncio.wait_for(
    llm.complete(user_input, max_tokens=500),
    timeout=30.0
)

# ❌ WRONG: No limits
response = await llm.complete(user_input)
```

---

## General Coding Standards

### Code Quality
- Write clean, readable, well-documented code
- Use meaningful variable and function names
- Follow language-specific conventions (PEP 8 for Python, Airbnb for JavaScript)
- Add type hints/annotations where applicable

### Security
- Never hardcode secrets, credentials, or API keys
- Validate all user input
- Use secure defaults
- Follow the principle of least privilege
- Implement proper error handling without exposing internals

### Testing
- Write tests for all significant functionality
- Include edge cases and error conditions
- Mock external services (including LLM APIs)
- Aim for high test coverage

---

## Documentation

### Code Comments
- Explain "why" not "what" in comments
- Document complex algorithms or business logic
- Add docstrings to all public functions/classes
- Keep comments up-to-date with code changes

### README Files
- Include clear setup instructions
- Document dependencies and how to install them
- Provide usage examples
- Explain project structure
- Add troubleshooting section

---

## OWASP LLM Security Checklist

When writing or reviewing LLM-related code, verify:

- [ ] **LLM01**: User input separated from system prompts?
- [ ] **LLM02**: No secrets in prompts or logs?
- [ ] **LLM03**: Dependencies pinned and verified?
- [ ] **LLM04**: Training data validated?
- [ ] **LLM05**: LLM outputs validated before use?
- [ ] **LLM06**: Least privilege implemented?
- [ ] **LLM07**: System prompts protected?
- [ ] **LLM08**: Vector DB access controlled?
- [ ] **LLM09**: Disclaimers added for AI content?
- [ ] **LLM10**: Rate limiting and timeouts in place?

---

## Language-Specific Guidelines

### JavaScript/TypeScript
- Use `const` by default, `let` when needed, never `var`
- Use async/await over callbacks
- Implement proper error handling with try/catch
- Use ES6+ features appropriately

### Python
- Follow PEP 8 style guide
- Use type hints (Python 3.7+)
- Use f-strings for string formatting
- Implement proper exception handling
- Use context managers (`with` statements) for resources

### SQL
- Always use parameterized queries
- Never construct SQL with string concatenation
- Use transactions for data consistency
- Implement proper indexing

---

## Error Handling

### DO
- Handle errors gracefully
- Log errors with context (but not sensitive data)
- Provide user-friendly error messages
- Implement retry logic with exponential backoff where appropriate

### DON'T
- Don't expose stack traces to users
- Don't log sensitive information
- Don't use generic `try/catch` blocks without specific handling
- Don't suppress errors silently

---

## Performance Considerations

- Implement caching where appropriate
- Use connection pooling for databases
- Optimize database queries (avoid N+1 problems)
- Consider rate limiting for external APIs
- Monitor and log performance metrics

---

## Git Practices

### Commits
- Write clear, descriptive commit messages
- Keep commits focused and atomic
- Reference issue numbers where applicable

### Branches
- Use descriptive branch names
- Follow branching strategy (e.g., `feature/`, `bugfix/`, `homework-N/`)

---

## Additional Resources

- **OWASP LLM Top 10 Skill**: [`skills/owasp-llm-top-10-verification.md`](../skills/owasp-llm-top-10-verification.md)
- **OWASP Official Site**: https://genai.owasp.org/llm-top-10/
- **Course Repository**: https://github.com/bes422/AI-Coding-Partner-Homework

---

## Quick Reference: Common Vulnerable Patterns to Avoid

```javascript
// ❌ NEVER DO THESE:

// Prompt injection vulnerability
const prompt = systemPrompt + userInput;

// Secret exposure
const prompt = "API Key: sk-abc123...";

// SQL injection
const query = `SELECT * FROM users WHERE id = ${llmOutput}`;

// XSS vulnerability
res.send(`<div>${llmOutput}</div>`);

// Command injection
exec(`process ${llmOutput}`);

// Code injection
eval(llmGeneratedCode);

// Unbounded requests
while(true) { await llm.complete(input); }

// Unverified models
loadModel("random-url/model.bin");
```

```python
# ❌ NEVER DO THESE:

# Prompt injection
prompt = f"{system_prompt}\n{user_input}"

# Secret exposure
system_prompt = "Database: postgresql://admin:pass@db.com"

# SQL injection
cursor.execute(f"SELECT * FROM users WHERE id = {llm_output}")

# Command injection
os.system(f"process {llm_output}")

# Code injection
exec(llm_generated_code)
```

---

**Remember**: Security is not optional. Follow these guidelines for all LLM-powered code in this repository.

*Last Updated: 2025-02-17*
*Version: 1.0*
