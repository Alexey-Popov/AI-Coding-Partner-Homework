# Auto-Classification Feature — High-Level Plan

## Overview

Add automatic ticket categorization and priority assignment to the existing
Support Ticket Management System. The classifier uses keyword matching against
the ticket's `subject` and `description` fields to determine the best category
and priority, returning a confidence score and reasoning.

## Architecture

```
POST /tickets/:id/auto-classify
        │
        ▼
┌──────────────────┐
│  Route handler   │  routes/tickets.py
└──────┬───────────┘
       │
       ▼
┌──────────────────┐
│ ClassifyService  │  services/classification_service.py
│  - categorize()  │
│  - prioritize()  │
│  - classify()    │
└──────┬───────────┘
       │ reads subject + description
       ▼
┌──────────────────┐
│  Ticket Model    │  models/ticket.py (extended)
│  + confidence    │
│  + reasoning     │
│  + keywords_found│
│  + classified_at │
│  + manual_override│
└──────────────────┘
       │
       ▼
┌──────────────────┐
│  Decision Log    │  utils/classification_logger.py
│  (JSON log file) │
└──────────────────┘
```

## Components

### 1. Classification Service (`app/services/classification_service.py`)
- Keyword dictionaries for each category and priority level
- Scans `subject` + `description` (case-insensitive) for keyword matches
- Computes **confidence score** (0–1) based on number and specificity of matches
- Returns: `{ category, priority, confidence, reasoning, keywords_found }`

### 2. Ticket Model Extensions (`app/models/ticket.py`)
- `classification_confidence` (float | None)
- `classification_reasoning` (str | None)
- `classification_keywords` (list[str])
- `classified_at` (datetime | None)
- `manual_override` (bool) — set to True when user manually changes category/priority

### 3. API Endpoints
- `POST /tickets/:id/auto-classify` — classify an existing ticket
- `POST /tickets` — accepts optional `auto_classify: true` flag
- `PUT /tickets/:id` — when category or priority is changed manually, set `manual_override = True`

### 4. Classification Logger (`app/utils/classification_logger.py`)
- Logs every classification decision (ticket ID, result, timestamp)
- Writes to a structured log (Python logging, JSON format)

## Keyword Mapping

### Categories
| Category          | Keywords                                                              |
|-------------------|-----------------------------------------------------------------------|
| account_access    | login, password, 2fa, two-factor, sign in, locked out, access denied  |
| technical_issue   | bug, error, crash, broken, not working, exception, timeout, glitch    |
| billing_question  | payment, invoice, refund, charge, billing, subscription, pricing      |
| feature_request   | feature, enhancement, suggestion, improve, add, wish, request         |
| bug_report        | defect, reproduce, steps to reproduce, regression, unexpected behavior|
| other             | (fallback when no strong matches)                                     |

### Priority
| Priority | Keywords                                           |
|----------|----------------------------------------------------|
| urgent   | can't access, critical, production down, security  |
| high     | important, blocking, asap                          |
| medium   | (default)                                          |
| low      | minor, cosmetic, suggestion                        |

## Confidence Scoring
- Each matched keyword adds weight; category-specific keywords contribute more
- Final score = min(1.0, matched_weight / threshold)
- `>= 0.7` → high confidence, `0.4–0.7` → medium, `< 0.4` → low

## Implementation Order
1. Create classification service with keyword engine
2. Extend Ticket model with classification fields
3. Add `POST /tickets/:id/auto-classify` route
4. Add `auto_classify` flag support to ticket creation
5. Add manual override tracking to update endpoint
6. Add classification decision logger
7. Update HOW-TO-RUN.md
