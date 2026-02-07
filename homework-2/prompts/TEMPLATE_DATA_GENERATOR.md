# Sample Data Generator Template

**Purpose:** Generate realistic test fixtures for multi-format import testing  
**Output:** Sample data files in CSV, JSON, and XML formats  
**Best for:** Creating consistent test data across all import formats

---

## Guardrails
Follow the rules defined in [GUARDRAILS.md](GUARDRAILS.md):
- Do not guess if uncertain
- Ask 3 clarifying questions for ambiguous input
- List all missing data explicitly

---

## Method 1: AI Prompt for Data Generation

### Prompt Template

```
Generate realistic customer support ticket sample data for testing.

## Data Schema

Each ticket must have:
- **id**: Integer, unique, sequential starting from 1
- **title**: String, 10-100 chars, descriptive issue title
- **description**: String, 50-500 chars, detailed problem description
- **customer_email**: Valid email format (use @example.com domain)
- **category**: One of: {{CATEGORIES}}
- **priority**: One of: {{PRIORITIES}}
- **status**: One of: {{STATUSES}}
- **source**: One of: {{SOURCES}}
- **created_at**: ISO 8601 datetime (past 30 days)
- **updated_at**: ISO 8601 datetime (>= created_at)

## Category Distribution (ensure coverage)
- account_access: 15% of tickets
- technical_issue: 20% of tickets
- billing_question: 15% of tickets
- feature_request: 20% of tickets
- bug_report: 20% of tickets
- other: 10% of tickets

## Priority Distribution
- urgent: 10% (include keywords: "can't access", "critical", "production down", "security")
- high: 20% (include keywords: "important", "blocking", "asap")
- medium: 40%
- low: 30% (include keywords: "minor", "cosmetic", "suggestion")

## Requirements

Generate:
1. **sample_tickets.csv** - {{CSV_COUNT}} tickets in CSV format
2. **sample_tickets.json** - {{JSON_COUNT}} tickets in JSON array format
3. **sample_tickets.xml** - {{XML_COUNT}} tickets in XML format

Each format should have DIFFERENT tickets (not duplicates).

## Output Format Examples

### CSV Header
```csv
id,title,description,customer_email,category,priority,status,source,created_at,updated_at
```

### JSON Structure
```json
[
  {
    "id": 1,
    "title": "...",
    "description": "...",
    "customer_email": "...",
    "category": "...",
    "priority": "...",
    "status": "...",
    "source": "...",
    "created_at": "...",
    "updated_at": "..."
  }
]
```

### XML Structure
```xml
<?xml version="1.0" encoding="UTF-8"?>
<tickets>
  <ticket>
    <id>1</id>
    <title>...</title>
    <description>...</description>
    <customer_email>...</customer_email>
    <category>...</category>
    <priority>...</priority>
    <status>...</status>
    <source>...</source>
    <created_at>...</created_at>
    <updated_at>...</updated_at>
  </ticket>
</tickets>
```

Generate realistic, diverse data that covers edge cases:
- Tickets with all priority/category combinations
- Various status progressions
- Different customer email patterns
- Timestamps spanning the past 30 days
```

---

## Method 2: Python Generator Script

### Script Template

```python
#!/usr/bin/env python3
"""
Sample Data Generator for Customer Support Ticket System
Generates test fixtures in CSV, JSON, and XML formats
"""

import csv
import json
import random
import xml.etree.ElementTree as ET
from datetime import datetime, timedelta
from pathlib import Path
from xml.dom import minidom
from typing import List, Dict, Any

# Configuration
CATEGORIES = ["account_access", "technical_issue", "billing_question", 
              "feature_request", "bug_report", "other"]
PRIORITIES = ["urgent", "high", "medium", "low"]
STATUSES = ["new", "in_progress", "waiting_customer", "resolved", "closed"]
SOURCES = ["web_form", "email", "api", "chat", "phone"]

# Keyword patterns for auto-classification
PRIORITY_KEYWORDS = {
    "urgent": ["can't access", "critical", "production down", "security", 
               "emergency", "immediately"],
    "high": ["important", "blocking", "asap", "urgent need", "high priority"],
    "low": ["minor", "cosmetic", "suggestion", "nice to have", "low priority"]
}

CATEGORY_KEYWORDS = {
    "account_access": ["login", "password", "access", "locked out", "can't sign in"],
    "technical_issue": ["error", "crash", "not working", "bug", "broken"],
    "billing_question": ["invoice", "payment", "charge", "refund", "subscription"],
    "feature_request": ["would be nice", "feature", "add", "suggestion", "improve"],
    "bug_report": ["bug", "issue", "defect", "problem", "unexpected"]
}

# Sample title/description templates per category
TEMPLATES = {
    "account_access": [
        ("Cannot access my account", "I've been locked out of my account after multiple failed login attempts. {keyword}"),
        ("Password reset not working", "Tried to reset my password but the email never arrives. {keyword}"),
        ("Two-factor authentication issue", "My 2FA codes are not being accepted. {keyword}"),
    ],
    "technical_issue": [
        ("Application crashes on startup", "The app crashes immediately when I try to open it. {keyword}"),
        ("Slow performance", "The system is running extremely slow lately. {keyword}"),
        ("Error message appearing", "Getting an error message when trying to save. {keyword}"),
    ],
    "billing_question": [
        ("Unexpected charge on my account", "I noticed an unexpected charge of $XX on my statement. {keyword}"),
        ("Need invoice for tax purposes", "Please send me an invoice for my recent purchases. {keyword}"),
        ("Subscription cancellation", "I'd like to cancel my subscription and get a refund. {keyword}"),
    ],
    "feature_request": [
        ("Dark mode support", "Would be great if you could add dark mode to the app. {keyword}"),
        ("Export to PDF feature", "Please add ability to export reports to PDF. {keyword}"),
        ("Mobile app improvement", "The mobile app needs better navigation. {keyword}"),
    ],
    "bug_report": [
        ("Button not responding", "The submit button doesn't work on the contact form. {keyword}"),
        ("Data not saving correctly", "When I save my profile, some fields are lost. {keyword}"),
        ("Display issue on mobile", "The layout is broken on mobile devices. {keyword}"),
    ],
    "other": [
        ("General inquiry", "I have a question about your services. {keyword}"),
        ("Partnership opportunity", "Would like to discuss potential partnership. {keyword}"),
        ("Feedback on recent update", "Just wanted to share my thoughts on the recent update. {keyword}"),
    ]
}


def random_datetime(days_back: int = 30) -> datetime:
    """Generate random datetime within the past N days"""
    now = datetime.now()
    random_days = random.uniform(0, days_back)
    return now - timedelta(days=random_days)


def generate_ticket(ticket_id: int, force_category: str = None, 
                    force_priority: str = None) -> Dict[str, Any]:
    """Generate a single ticket with realistic data"""
    
    category = force_category or random.choice(CATEGORIES)
    priority = force_priority or random.choices(
        PRIORITIES, weights=[10, 20, 40, 30]
    )[0]
    
    # Select template based on category
    title_template, desc_template = random.choice(TEMPLATES.get(category, TEMPLATES["other"]))
    
    # Add keyword based on priority
    keyword = ""
    if priority in PRIORITY_KEYWORDS:
        keyword = f"This is {random.choice(PRIORITY_KEYWORDS[priority])}."
    
    description = desc_template.format(keyword=keyword).strip()
    
    created_at = random_datetime()
    updated_at = created_at + timedelta(hours=random.uniform(0, 48))
    
    return {
        "id": ticket_id,
        "title": title_template,
        "description": description,
        "customer_email": f"customer{ticket_id}@example.com",
        "category": category,
        "priority": priority,
        "status": random.choice(STATUSES),
        "source": random.choice(SOURCES),
        "created_at": created_at.isoformat(),
        "updated_at": updated_at.isoformat()
    }


def generate_tickets(count: int, start_id: int = 1) -> List[Dict[str, Any]]:
    """Generate multiple tickets with good category/priority distribution"""
    tickets = []
    
    # Ensure coverage of all categories and priorities
    for i, category in enumerate(CATEGORIES):
        tickets.append(generate_ticket(start_id + i, force_category=category))
    
    for i, priority in enumerate(PRIORITIES):
        tickets.append(generate_ticket(start_id + len(CATEGORIES) + i, force_priority=priority))
    
    # Fill remaining with random tickets
    remaining = count - len(tickets)
    for i in range(remaining):
        tickets.append(generate_ticket(start_id + len(CATEGORIES) + len(PRIORITIES) + i))
    
    return tickets[:count]


def write_csv(tickets: List[Dict], filepath: Path):
    """Write tickets to CSV file"""
    fieldnames = ["id", "title", "description", "customer_email", "category", 
                  "priority", "status", "source", "created_at", "updated_at"]
    
    with open(filepath, 'w', newline='', encoding='utf-8') as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(tickets)
    
    print(f"✅ Generated {filepath} with {len(tickets)} tickets")


def write_json(tickets: List[Dict], filepath: Path):
    """Write tickets to JSON file"""
    with open(filepath, 'w', encoding='utf-8') as f:
        json.dump(tickets, f, indent=2)
    
    print(f"✅ Generated {filepath} with {len(tickets)} tickets")


def write_xml(tickets: List[Dict], filepath: Path):
    """Write tickets to XML file"""
    root = ET.Element("tickets")
    
    for ticket in tickets:
        ticket_elem = ET.SubElement(root, "ticket")
        for key, value in ticket.items():
            child = ET.SubElement(ticket_elem, key)
            child.text = str(value)
    
    # Pretty print
    xml_str = minidom.parseString(ET.tostring(root)).toprettyxml(indent="  ")
    
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(xml_str)
    
    print(f"✅ Generated {filepath} with {len(tickets)} tickets")


def main():
    """Generate all sample data files"""
    output_dir = Path("fixtures")
    output_dir.mkdir(exist_ok=True)
    
    # Generate different tickets for each format
    csv_tickets = generate_tickets(50, start_id=1)
    json_tickets = generate_tickets(20, start_id=101)
    xml_tickets = generate_tickets(30, start_id=201)
    
    # Write files
    write_csv(csv_tickets, output_dir / "sample_tickets.csv")
    write_json(json_tickets, output_dir / "sample_tickets.json")
    write_xml(xml_tickets, output_dir / "sample_tickets.xml")
    
    # Generate invalid data for negative testing
    invalid_tickets = [
        {"id": "not-a-number", "title": "", "description": "x" * 1000,
         "customer_email": "invalid-email", "category": "unknown",
         "priority": "super-high", "status": "pending", "source": "carrier_pigeon",
         "created_at": "not-a-date", "updated_at": "also-not-a-date"},
    ]
    write_csv(invalid_tickets, output_dir / "invalid_tickets.csv")
    
    print(f"\n📁 All files generated in {output_dir.absolute()}")


if __name__ == "__main__":
    main()
```

---

## Filled Example for Ticket System

### AI Prompt (Filled)

```
Generate realistic customer support ticket sample data for testing.

## Data Schema

Each ticket must have:
- **id**: Integer, unique, sequential starting from 1
- **title**: String, 10-100 chars, descriptive issue title
- **description**: String, 50-500 chars, detailed problem description
- **customer_email**: Valid email format (use @example.com domain)
- **category**: One of: account_access, technical_issue, billing_question, feature_request, bug_report, other
- **priority**: One of: urgent, high, medium, low
- **status**: One of: new, in_progress, waiting_customer, resolved, closed
- **source**: One of: web_form, email, api, chat, phone
- **created_at**: ISO 8601 datetime (past 30 days)
- **updated_at**: ISO 8601 datetime (>= created_at)

## Category Distribution (ensure coverage)
- account_access: 15% of tickets
- technical_issue: 20% of tickets
- billing_question: 15% of tickets
- feature_request: 20% of tickets
- bug_report: 20% of tickets
- other: 10% of tickets

## Priority Distribution
- urgent: 10% (include keywords: "can't access", "critical", "production down", "security")
- high: 20% (include keywords: "important", "blocking", "asap")
- medium: 40%
- low: 30% (include keywords: "minor", "cosmetic", "suggestion")

## Requirements

Generate:
1. **sample_tickets.csv** - 50 tickets in CSV format
2. **sample_tickets.json** - 20 tickets in JSON array format
3. **sample_tickets.xml** - 30 tickets in XML format

Each format should have DIFFERENT tickets (not duplicates).
```

---

## Usage Notes

1. **Method 1 (AI Prompt)** is faster for one-time generation
2. **Method 2 (Script)** is reproducible and can be re-run
3. **Combine both**: Use AI to generate templates, script for consistency
4. **Invalid data**: Always generate some invalid fixtures for negative testing
5. **Distribution**: Ensure all enum values appear in sample data
6. **Keywords**: Include priority keywords so auto-classification can be tested
