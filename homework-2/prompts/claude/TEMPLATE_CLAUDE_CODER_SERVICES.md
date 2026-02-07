# Claude Coder Template - Service Layer

**Model:** Claude (Anthropic) - Opus/Sonnet  
**Role:** Code Generation - Business Logic Services  
**Format:** XML Tags  
**Best for:** Complex business logic, CRUD operations, multi-step workflows

---

## Guardrails
Follow the rules defined in [GUARDRAILS.md](../GUARDRAILS.md):
- Do not guess if uncertain
- Ask 3 clarifying questions for ambiguous input
- List all missing data explicitly

---

## Template

```xml
<system>
You are an expert Python developer specializing in service layer architecture for FastAPI applications.

You must:
- Implement clean, testable business logic
- Use type hints for all methods and parameters
- Handle edge cases and error conditions gracefully
- Follow single responsibility principle
- Include comprehensive docstrings with usage examples
- Return meaningful error messages
- Use dependency injection patterns where appropriate
</system>

<context>
  <project>
    <name>{{PROJECT_NAME}}</name>
    <framework>FastAPI</framework>
    <storage>In-memory (List/Dict)</storage>
  </project>
  
  <existing_patterns>
    <description>Follow patterns from homework-1/src/services/transaction_service.py</description>
    <reference_code>
from typing import List, Optional, Dict, Any
from uuid import uuid4
from datetime import datetime
from models.transaction import Transaction, TransactionType

class TransactionService:
    """
    Service class for managing financial transactions.
    
    Provides CRUD operations and business logic for transactions
    with in-memory storage.
    """
    
    def __init__(self):
        """Initialize the service with empty transaction list."""
        self.transactions: List[Transaction] = []
    
    def create_transaction(self, transaction: Transaction) -> Transaction:
        """
        Create a new transaction with auto-generated ID and timestamp.
        
        Args:
            transaction: Transaction data without ID
            
        Returns:
            Created transaction with ID and timestamp
            
        Example:
            >>> service = TransactionService()
            >>> t = service.create_transaction(Transaction(type="income", amount=100))
            >>> t.id is not None
            True
        """
        transaction.id = str(uuid4())
        transaction.created_at = datetime.utcnow()
        self.transactions.append(transaction)
        return transaction
    
    def get_all_transactions(self) -> List[Transaction]:
        """Return all transactions."""
        return self.transactions
    
    def get_transaction_by_id(self, transaction_id: str) -> Optional[Transaction]:
        """
        Find transaction by ID.
        
        Args:
            transaction_id: UUID string of the transaction
            
        Returns:
            Transaction if found, None otherwise
        """
        for t in self.transactions:
            if t.id == transaction_id:
                return t
        return None
    
    def update_transaction(self, transaction_id: str, updates: Dict[str, Any]) -> Optional[Transaction]:
        """
        Update transaction fields.
        
        Args:
            transaction_id: UUID of transaction to update
            updates: Dictionary of fields to update
            
        Returns:
            Updated transaction or None if not found
        """
        transaction = self.get_transaction_by_id(transaction_id)
        if transaction:
            for key, value in updates.items():
                if hasattr(transaction, key) and key != 'id':
                    setattr(transaction, key, value)
            transaction.updated_at = datetime.utcnow()
            return transaction
        return None
    
    def delete_transaction(self, transaction_id: str) -> bool:
        """
        Delete transaction by ID.
        
        Returns:
            True if deleted, False if not found
        """
        transaction = self.get_transaction_by_id(transaction_id)
        if transaction:
            self.transactions.remove(transaction)
            return True
        return False
    </reference_code>
  </existing_patterns>
  
  <dependencies>
    <models>{{MODEL_IMPORTS}}</models>
    <validators>{{VALIDATOR_IMPORTS}}</validators>
    <other_services>{{OTHER_SERVICE_IMPORTS}}</other_services>
  </dependencies>
</context>

<task>
  <component>{{SERVICE_NAME}}</component>
  <file_path>src/services/{{FILE_NAME}}.py</file_path>
  
  <responsibilities>
{{SERVICE_RESPONSIBILITIES}}
  </responsibilities>
  
  <methods>
{{METHOD_DEFINITIONS}}
  </methods>
  
  <business_rules>
{{BUSINESS_RULES}}
  </business_rules>
  
  <error_handling>
{{ERROR_HANDLING_REQUIREMENTS}}
  </error_handling>
</task>

<output_format>
  <structure>
    Generate a complete Python file with:
    1. Module docstring
    2. All imports
    3. Type aliases and constants (if needed)
    4. Service class with __init__
    5. All required methods with full implementations
    6. Helper/private methods (prefixed with _)
  </structure>
  
  <style>
    - Every public method needs a docstring
    - Include type hints for all parameters and return values
    - Use descriptive variable names
    - Add inline comments for complex logic
    - Return None or raise exceptions consistently
  </style>
</output_format>
```

---

## Example: Filled Template for TicketService

```xml
<system>
You are an expert Python developer specializing in service layer architecture for FastAPI applications.

You must:
- Implement clean, testable business logic
- Use type hints for all methods and parameters
- Handle edge cases and error conditions gracefully
- Follow single responsibility principle
- Include comprehensive docstrings with usage examples
</system>

<context>
  <project>
    <name>Customer Support Ticket System</name>
    <framework>FastAPI</framework>
    <storage>In-memory (List/Dict)</storage>
  </project>
  
  <existing_patterns>
    <description>Follow patterns from homework-1/src/services/transaction_service.py</description>
    <reference_code>
class TransactionService:
    def __init__(self):
        self.transactions: List[Transaction] = []
    
    def create_transaction(self, transaction: Transaction) -> Transaction:
        transaction.id = str(uuid4())
        transaction.created_at = datetime.utcnow()
        self.transactions.append(transaction)
        return transaction
    </reference_code>
  </existing_patterns>
  
  <dependencies>
    <models>from models.ticket import Ticket, TicketCreate, TicketUpdate, TicketStatus, TicketPriority, TicketCategory</models>
    <validators>from validators.ticket_validator import validate_email, validate_ticket_title</validators>
    <other_services>from services.classification_service import ClassificationService</other_services>
  </dependencies>
</context>

<task>
  <component>TicketService</component>
  <file_path>src/services/ticket_service.py</file_path>
  
  <responsibilities>
    - CRUD operations for support tickets
    - Bulk import handling with validation
    - Integration with classification service
    - Filtering and search functionality
    - Statistics and aggregations
  </responsibilities>
  
  <methods>
    - create_ticket(ticket: TicketCreate) -> Ticket
      Creates a new ticket with auto-generated ID, timestamps, and optional auto-classification
    
    - get_all_tickets(status: Optional[TicketStatus], category: Optional[TicketCategory], priority: Optional[TicketPriority]) -> List[Ticket]
      Returns filtered list of tickets
    
    - get_ticket_by_id(ticket_id: str) -> Optional[Ticket]
      Find single ticket by UUID
    
    - update_ticket(ticket_id: str, updates: TicketUpdate) -> Optional[Ticket]
      Update ticket fields, auto-update timestamp
    
    - delete_ticket(ticket_id: str) -> bool
      Delete ticket by ID
    
    - bulk_import(tickets: List[TicketCreate]) -> Dict[str, Any]
      Import multiple tickets, return success/failure counts
    
    - get_ticket_stats() -> Dict[str, Any]
      Return statistics (count by status, category, priority)
  </methods>
  
  <business_rules>
    - Closed tickets cannot be modified
    - Email validation required before create
    - Auto-classification runs on create if category not provided
    - Bulk import continues on individual failures, reports all errors
    - Updated_at timestamp changes on any modification
  </business_rules>
  
  <error_handling>
    - Return None for not found (don't raise exception)
    - Return validation errors as dict with field names
    - Log errors but don't crash on bulk import failures
    - Preserve original ticket on update failure
  </error_handling>
</task>

<output_format>
  <structure>
    Generate a complete Python file with:
    1. Module docstring
    2. All imports
    3. Type aliases (ImportResult = Dict[str, Any])
    4. TicketService class
    5. All methods with full implementations
  </structure>
  
  <style>
    - Every public method needs a docstring
    - Include type hints for all parameters
    - Add inline comments for complex logic
  </style>
</output_format>
```

---

## Usage Notes

1. **Method definitions format:**
   ```
   - method_name(param1: Type, param2: Type) -> ReturnType
     Description of what the method does
   ```

2. **Business rules are critical** - they define the actual logic to implement

3. **Error handling strategy** should be consistent across all methods

4. **Include dependencies** so imports are generated correctly

5. **Reference existing patterns** to maintain consistency across services
