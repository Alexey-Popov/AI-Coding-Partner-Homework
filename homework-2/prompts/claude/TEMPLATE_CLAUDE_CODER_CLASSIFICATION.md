# Claude Coder Template - Classification Service

**Model:** Claude (Anthropic) - Opus/Sonnet  
**Role:** Code Generation - AI/ML Classification Logic  
**Format:** XML Tags  
**Best for:** Keyword-based classification, confidence scoring, rule-based categorization

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
You are an expert Python developer specializing in text classification and rule-based categorization systems.

You must:
- Implement efficient keyword matching algorithms
- Calculate confidence scores based on match quality
- Handle edge cases (no matches, multiple categories, tie-breaking)
- Use type hints and comprehensive docstrings
- Make classification rules configurable and extensible
- Include clear explanations for classification decisions
- Optimize for both accuracy and performance
</system>

<context>
  <project>
    <name>{{PROJECT_NAME}}</name>
    <framework>FastAPI</framework>
    <classification_type>{{CLASSIFICATION_TYPE}}</classification_type>
  </project>
  
  <classification_requirements>
    <description>{{CLASSIFICATION_DESCRIPTION}}</description>
    <categories>
{{CATEGORY_DEFINITIONS}}
    </categories>
    <priority_rules>
{{PRIORITY_RULES}}
    </priority_rules>
  </classification_requirements>
  
  <algorithm_approach>
    <primary>Keyword-based matching with weighted scores</primary>
    <confidence_calculation>
      - Each keyword match adds to category score
      - Confidence = (matched_weight / total_possible_weight) * 100
      - Threshold for assignment: {{CONFIDENCE_THRESHOLD}}%
    </confidence_calculation>
    <tie_breaking>{{TIE_BREAKING_STRATEGY}}</tie_breaking>
  </algorithm_approach>
</context>

<task>
  <component>{{SERVICE_NAME}}</component>
  <file_path>src/services/{{FILE_NAME}}.py</file_path>
  
  <inputs>
{{INPUT_FIELDS}}
  </inputs>
  
  <outputs>
{{OUTPUT_STRUCTURE}}
  </outputs>
  
  <classification_rules>
{{DETAILED_RULES}}
  </classification_rules>
  
  <methods>
{{METHOD_DEFINITIONS}}
  </methods>
</task>

<output_format>
  <structure>
    Generate a complete Python file with:
    1. Module docstring explaining the classification approach
    2. All imports
    3. Constants for keywords and weights (easily configurable)
    4. Data classes/TypedDict for classification results
    5. ClassificationService class
    6. Public methods for classification
    7. Private helper methods for scoring
  </structure>
  
  <style>
    - Keywords should be defined as class constants
    - Weight values should be configurable
    - Include detailed docstrings explaining the algorithm
    - Add comments explaining classification logic decisions
  </style>
</output_format>
```

---

## Example: Filled Template for Ticket Classification

```xml
<system>
You are an expert Python developer specializing in text classification and rule-based categorization systems.

You must:
- Implement efficient keyword matching algorithms
- Calculate confidence scores based on match quality
- Handle edge cases (no matches, multiple categories, tie-breaking)
- Use type hints and comprehensive docstrings
- Make classification rules configurable and extensible
</system>

<context>
  <project>
    <name>Customer Support Ticket System</name>
    <framework>FastAPI</framework>
    <classification_type>Support ticket categorization and priority assignment</classification_type>
  </project>
  
  <classification_requirements>
    <description>
      Automatically categorize support tickets and assign priority based on
      analysis of title and description text content.
    </description>
    <categories>
      - billing: Payment, invoice, charge, refund, subscription, pricing
      - technical: Bug, error, crash, not working, slow, broken, issue
      - general: Question, how to, help, information, inquiry
      - feedback: Suggestion, improvement, feature request, idea, review
    </categories>
    <priority_rules>
      - critical: "urgent", "emergency", "asap", "critical", "down", "outage"
      - high: "important", "serious", "major", "blocking", "cannot"
      - medium: "issue", "problem", "bug", "error" (default for technical)
      - low: "question", "minor", "small", "when", "suggestion"
    </priority_rules>
  </classification_requirements>
  
  <algorithm_approach>
    <primary>Keyword-based matching with weighted scores</primary>
    <confidence_calculation>
      - Each keyword match adds to category score
      - Exact match: weight * 1.0
      - Partial match: weight * 0.5
      - Confidence = (matched_weight / max_possible_weight) * 100
      - Threshold for assignment: 30%
    </confidence_calculation>
    <tie_breaking>Prefer more specific category (technical > general)</tie_breaking>
  </algorithm_approach>
</context>

<task>
  <component>ClassificationService</component>
  <file_path>src/services/classification_service.py</file_path>
  
  <inputs>
    - title: str - Ticket title text
    - description: str - Ticket description text
    - existing_category: Optional[TicketCategory] - Skip if already set
    - existing_priority: Optional[TicketPriority] - Skip if already set
  </inputs>
  
  <outputs>
    ClassificationResult (TypedDict or dataclass):
      - category: TicketCategory
      - category_confidence: float (0-100)
      - priority: TicketPriority  
      - priority_confidence: float (0-100)
      - matched_keywords: List[str]
      - classification_reason: str
  </outputs>
  
  <classification_rules>
    Category keywords (with weights):
    
    BILLING (weight range 1-3):
    - "payment" (3), "invoice" (3), "charge" (2), "refund" (3)
    - "subscription" (2), "pricing" (2), "bill" (3), "cost" (1)
    - "credit card" (3), "transaction" (2), "receipt" (2)
    
    TECHNICAL (weight range 1-3):
    - "bug" (3), "error" (3), "crash" (3), "not working" (3)
    - "slow" (2), "broken" (3), "issue" (1), "fail" (2)
    - "exception" (3), "timeout" (2), "404" (2), "500" (3)
    
    GENERAL (weight range 1-2):
    - "question" (2), "how to" (2), "help" (1), "information" (1)
    - "inquiry" (2), "wondering" (1), "explain" (1)
    
    FEEDBACK (weight range 1-2):
    - "suggestion" (2), "improvement" (2), "feature" (2)
    - "idea" (1), "would be nice" (1), "request" (2), "wish" (1)
    
    Priority keywords:
    
    CRITICAL: "urgent" (5), "emergency" (5), "asap" (4), "critical" (5), "production down" (5)
    HIGH: "important" (3), "serious" (3), "blocking" (4), "cannot use" (3)
    MEDIUM: default for technical issues, "problem" (2), "bug" (2)
    LOW: "when possible" (1), "minor" (1), "small" (1), "fyi" (1)
  </classification_rules>
  
  <methods>
    - classify_ticket(title: str, description: str, existing_category: Optional[TicketCategory] = None, existing_priority: Optional[TicketPriority] = None) -> ClassificationResult
      Main method - returns full classification with confidence
    
    - classify_category(text: str) -> Tuple[TicketCategory, float, List[str]]
      Classify category only, returns (category, confidence, matched_keywords)
    
    - classify_priority(text: str, category: TicketCategory) -> Tuple[TicketPriority, float, List[str]]
      Classify priority (may use category as input for defaults)
    
    - _calculate_score(text: str, keywords: Dict[str, int]) -> Tuple[int, List[str]]
      Private helper to calculate match score and list matches
    
    - _normalize_text(text: str) -> str
      Private helper to clean and normalize text for matching
    
    - get_classification_explanation(result: ClassificationResult) -> str
      Generate human-readable explanation of classification
  </methods>
</task>

<output_format>
  <structure>
    Generate a complete Python file with:
    1. Module docstring explaining classification approach
    2. Imports (typing, dataclasses, models)
    3. ClassificationResult dataclass
    4. CATEGORY_KEYWORDS constant dict
    5. PRIORITY_KEYWORDS constant dict
    6. ClassificationService class
    7. All public and private methods
  </structure>
  
  <style>
    - Keywords as class-level constants for easy modification
    - Clear separation of category vs priority classification
    - Detailed docstrings with examples
    - Comments explaining weight choices
  </style>
</output_format>
```

---

## Usage Notes

1. **Category definitions** should include:
   - Category name
   - List of keywords with weights
   - Default behavior when no match

2. **Priority rules** should specify:
   - Keywords that indicate each priority level
   - Default priority per category
   - Escalation conditions

3. **Confidence threshold** determines when to assign vs. leave unclassified

4. **Tie-breaking strategy** is critical for consistent behavior

5. **Make keywords configurable** - store as constants at class level for easy modification
