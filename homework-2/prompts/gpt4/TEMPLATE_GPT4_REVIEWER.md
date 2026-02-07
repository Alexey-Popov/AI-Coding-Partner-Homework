# GPT-4 Reviewer Template - Code Review

**Model:** OpenAI GPT-4  
**Role:** Code Review and Quality Assessment  
**Format:** System/User Message with Checklist  
**Best for:** Structured code reviews, security audits, best practices validation

---

## Guardrails
Follow the rules defined in [GUARDRAILS.md](../GUARDRAILS.md):
- Do not guess if uncertain
- Ask 3 clarifying questions for ambiguous input
- List all missing data explicitly

---

## Template

### System Message

```
You are an expert code reviewer specializing in Python/FastAPI applications.

RULES:
1. If you are unsure about a potential issue, state your uncertainty. Do not guess.
2. If the code context is ambiguous, ask exactly 3 clarifying questions before proceeding.
3. If information is missing (e.g., requirements, dependencies), explicitly list what is missing.

REVIEW STANDARDS:
- Focus on correctness, security, performance, and maintainability
- Provide specific line references for issues
- Suggest concrete fixes, not just problem descriptions
- Prioritize issues by severity (Critical, High, Medium, Low)
- Acknowledge good practices, not just problems

OUTPUT FORMAT:
- Use structured sections for different review aspects
- Include code snippets for suggested fixes
- Provide a summary with pass/fail recommendation
```

### User Message Template

```
## Review Context

```json
{
  "project": "{{PROJECT_NAME}}",
  "component": "{{COMPONENT_NAME}}",
  "file_path": "{{FILE_PATH}}",
  "review_type": "{{REVIEW_TYPE}}",
  "requirements": {{REQUIREMENTS}}
}
```

## Code to Review

```python
{{CODE_TO_REVIEW}}
```

## Review Checklist

Please evaluate the code against these criteria:

### Correctness
- [ ] Logic is correct and handles all cases
- [ ] Edge cases are handled appropriately
- [ ] Error handling is comprehensive
- [ ] Return types match function signatures

### Security
- [ ] No SQL injection vulnerabilities
- [ ] No command injection vulnerabilities
- [ ] Input validation is sufficient
- [ ] Sensitive data is not exposed
- [ ] No hardcoded credentials or secrets

### Performance
- [ ] No unnecessary loops or iterations
- [ ] Appropriate data structures used
- [ ] No blocking operations in async code
- [ ] Database queries are optimized (if applicable)

### Maintainability
- [ ] Code is readable and well-organized
- [ ] Functions follow single responsibility principle
- [ ] Variable and function names are descriptive
- [ ] Comments explain "why", not "what"
- [ ] No magic numbers or hardcoded values

### Best Practices
- [ ] Type hints are used consistently
- [ ] Docstrings follow conventions
- [ ] Logging is appropriate
- [ ] Tests are sufficient (if reviewing tests)

### {{ADDITIONAL_CRITERIA}}

## Required Output

Provide a structured review with:

1. **Summary**
   - Overall assessment (Approve / Request Changes / Reject)
   - Critical issues count
   - Key strengths

2. **Issues Found**
   For each issue:
   ```
   [SEVERITY] Issue Title
   Location: file.py:line_number
   Description: What the problem is
   Impact: What could go wrong
   Suggestion: How to fix it
   ```

3. **Suggested Improvements**
   Non-blocking suggestions for better code

4. **Positive Observations**
   Good practices worth noting

5. **Missing Information**
   Anything needed for complete review
```

---

## Example: Filled Template for Ticket Service Review

### System Message
(Same as above)

### User Message

```
## Review Context

```json
{
  "project": "Customer Support Ticket System",
  "component": "TicketService",
  "file_path": "src/services/ticket_service.py",
  "review_type": "Full Review",
  "requirements": [
    "CRUD operations for tickets",
    "Bulk import with partial failure handling",
    "Auto-classification integration",
    "In-memory storage",
    "Thread-safe operations not required"
  ]
}
```

## Code to Review

```python
from typing import List, Optional, Dict, Any
from uuid import uuid4
from datetime import datetime
from models.ticket import Ticket, TicketCreate, TicketUpdate, TicketStatus
from services.classification_service import ClassificationService

class TicketService:
    def __init__(self):
        self.tickets: List[Ticket] = []
        self.classification_service = ClassificationService()
    
    def create_ticket(self, ticket_data: TicketCreate) -> Ticket:
        ticket = Ticket(
            id=str(uuid4()),
            title=ticket_data.title,
            description=ticket_data.description,
            customer_email=ticket_data.customer_email,
            customer_name=ticket_data.customer_name,
            status=TicketStatus.OPEN,
            created_at=datetime.utcnow()
        )
        
        # Auto-classify if category not provided
        if not ticket_data.category:
            result = self.classification_service.classify_ticket(
                ticket.title, ticket.description
            )
            ticket.category = result.category
            ticket.priority = result.priority
        else:
            ticket.category = ticket_data.category
            ticket.priority = ticket_data.priority
        
        self.tickets.append(ticket)
        return ticket
    
    def get_all_tickets(self, status=None, category=None, priority=None):
        result = self.tickets
        if status:
            result = [t for t in result if t.status == status]
        if category:
            result = [t for t in result if t.category == category]
        if priority:
            result = [t for t in result if t.priority == priority]
        return result
    
    def get_ticket_by_id(self, ticket_id: str):
        for ticket in self.tickets:
            if ticket.id == ticket_id:
                return ticket
        return None
    
    def update_ticket(self, ticket_id: str, updates: TicketUpdate) -> Optional[Ticket]:
        ticket = self.get_ticket_by_id(ticket_id)
        if not ticket:
            return None
        
        if ticket.status == TicketStatus.CLOSED:
            raise ValueError("Cannot modify closed ticket")
        
        update_data = updates.dict(exclude_unset=True)
        for key, value in update_data.items():
            setattr(ticket, key, value)
        
        ticket.updated_at = datetime.utcnow()
        return ticket
    
    def delete_ticket(self, ticket_id: str) -> bool:
        ticket = self.get_ticket_by_id(ticket_id)
        if ticket:
            self.tickets.remove(ticket)
            return True
        return False
    
    def bulk_import(self, tickets: List[TicketCreate]) -> Dict[str, Any]:
        results = {"success_count": 0, "error_count": 0, "errors": []}
        
        for i, ticket_data in enumerate(tickets):
            try:
                self.create_ticket(ticket_data)
                results["success_count"] += 1
            except Exception as e:
                results["error_count"] += 1
                results["errors"].append({"index": i, "error": str(e)})
        
        return results
```

## Review Checklist

(Same as template above, with additional criteria:)

### Additional Criteria: Business Logic
- [ ] Closed ticket modification is prevented
- [ ] Auto-classification triggers correctly
- [ ] Bulk import handles partial failures
- [ ] Filtering works for all supported fields

## Required Output

(Same as template above)
```

---

## Usage Notes

1. **Review type** affects depth (Quick Review vs Full Review vs Security Audit)
2. **Requirements context** helps assess if code meets specifications
3. **Checklist items** ensure consistent review coverage
4. **Severity levels** help prioritize fixes:
   - **Critical**: Security vulnerabilities, data loss risk
   - **High**: Bugs, incorrect behavior
   - **Medium**: Performance issues, maintainability concerns
   - **Low**: Style, minor improvements
5. **Positive observations** build team culture and reinforce good practices
6. **Missing information** prevents incomplete reviews
