#!/usr/bin/env python3
"""
Generate sample ticket data for testing

Creates:
- sample_tickets.csv (50 tickets)
- sample_tickets.json (20 tickets)
- sample_tickets.xml (30 tickets)
- invalid_tickets.csv (error cases)
- invalid_tickets.json (error cases)
- invalid_tickets.xml (error cases)
"""

import csv
import json
import random
from datetime import datetime, timedelta
from pathlib import Path
import xml.etree.ElementTree as ET
from xml.dom import minidom

# Sample data templates
CATEGORIES = ["account_access", "technical_issue", "billing_question", "feature_request", "bug_report", "other"]
PRIORITIES = ["urgent", "high", "medium", "low"]
SOURCES = ["web_form", "email", "api", "chat", "phone"]
BROWSERS = ["Chrome", "Firefox", "Safari", "Edge", None]
DEVICE_TYPES = ["desktop", "mobile", "tablet", None]

# Ticket templates by category
TICKET_TEMPLATES = {
    "account_access": [
        ("Cannot login to my account", "I've been trying to log in but keep getting 'invalid credentials' error. I've reset my password twice but still can't access my account. This is critical as I need to access my data."),
        ("Password reset email not received", "I requested a password reset 3 hours ago but haven't received the email yet. I've checked spam folder. My email is correct in the system."),
        ("Account locked after failed attempts", "My account got locked out after I entered wrong password. I know the correct password now but system won't let me try again. Please unlock my account immediately."),
        ("Two-factor authentication not working", "The 2FA codes from my authenticator app are being rejected. I've tried multiple codes and even resynchronized the time on my phone but nothing works."),
    ],
    "technical_issue": [
        ("Application crashes on startup", "The desktop application crashes immediately when I try to open it. Error message says 'Unexpected error occurred'. I've tried reinstalling but same issue persists."),
        ("Slow loading times on dashboard", "The main dashboard takes over 30 seconds to load. This started happening after the last update. Other pages load fine but dashboard is extremely slow."),
        ("File upload fails with timeout error", "Every time I try to upload a file larger than 5MB, I get a timeout error. Smaller files upload fine. This is blocking my work as I need to upload project documents."),
        ("Search function not returning results", "The search bar doesn't return any results even when searching for items I know exist. I've tried different keywords but getting zero results every time."),
    ],
    "billing_question": [
        ("Unexpected charge on my credit card", "I noticed a charge of $99.99 on my statement that I don't recognize. My subscription should only be $49.99 per month. Please explain this extra charge."),
        ("Need invoice for tax purposes", "I require detailed invoices for the past 6 months for my company's tax filing. Can you send them to my email? I need this by end of this week."),
        ("Subscription cancellation refund", "I cancelled my subscription 3 days ago but was still charged for this month. According to your policy I should get a prorated refund. Please process my refund."),
        ("Payment method update failed", "I'm trying to update my credit card information but the system keeps saying 'payment method update failed'. My card is valid and has sufficient balance."),
    ],
    "feature_request": [
        ("Add dark mode to the application", "It would be really great if you could add a dark mode option. Many of us work late hours and the bright interface is straining our eyes. This is a much requested feature."),
        ("Export reports to PDF format", "Please add ability to export reports directly to PDF. Currently we can only export to Excel which requires additional conversion step for our workflow."),
        ("Mobile app for iOS", "Your Android app is great but there's no iOS version. Many of our team members use iPhones and would benefit from a native iOS app. When can we expect this?"),
        ("Bulk edit functionality", "It would save a lot of time if we could edit multiple items at once instead of editing them one by one. A bulk edit feature would be a huge productivity boost."),
    ],
    "bug_report": [
        ("Submit button not working on contact form", "The submit button on the contact form doesn't respond to clicks. I've tried on Chrome and Firefox with same result. Console shows JavaScript error."),
        ("Data not saving properly", "When I edit my profile and click save, some fields revert to old values. Specifically the phone number and address fields don't persist after saving."),
        ("Calendar date picker shows wrong month", "The date picker in the scheduling section is showing February even though it's currently January. This causes confusion when trying to schedule appointments."),
        ("Email notifications arriving hours late", "I'm receiving email notifications 4-6 hours after the actual events. The system timestamp is correct but email delivery is severely delayed."),
    ],
    "other": [
        ("General inquiry about enterprise plans", "I represent a company with 500+ employees. We're interested in your enterprise plan. Can someone from sales contact me to discuss custom pricing and features?"),
        ("Partnership opportunity", "I run a complementary SaaS business and think there could be partnership opportunities between our companies. Would you be open to a discussion about integration?"),
        ("Feedback on recent UI update", "I wanted to share feedback on the recent interface redesign. Overall it looks modern but the navigation has become less intuitive. Happy to provide detailed feedback."),
        ("Documentation request", "I'm looking for technical documentation on your API endpoints. The current documentation seems outdated. Can you point me to the latest version?"),
    ],
}

# Priority keywords for realistic data
URGENT_KEYWORDS = ["critical", "production down", "can't access", "security breach", "data loss", "emergency"]
HIGH_KEYWORDS = ["blocking", "important", "asap", "urgent need", "immediately"]
LOW_KEYWORDS = ["minor", "cosmetic", "suggestion", "nice to have", "when you have time"]


def random_datetime(days_back=30):
    """Generate random datetime within past N days"""
    now = datetime.now()
    random_days = random.uniform(0, days_back)
    return now - timedelta(days=random_days)


def generate_ticket(customer_id, category=None, priority=None):
    """Generate a single ticket with realistic data"""
    if category is None:
        category = random.choice(CATEGORIES)
    
    # Select template for category
    subject, description = random.choice(TICKET_TEMPLATES[category])
    
    # Add priority keywords to description if needed
    if priority == "urgent" and not any(kw in description.lower() for kw in URGENT_KEYWORDS):
        description += f" This is {random.choice(URGENT_KEYWORDS)}!"
    elif priority == "high" and not any(kw in description.lower() for kw in HIGH_KEYWORDS):
        description += f" This is {random.choice(HIGH_KEYWORDS)}."
    elif priority == "low" and not any(kw in description.lower() for kw in LOW_KEYWORDS):
        description += f" This is {random.choice(LOW_KEYWORDS)}."
    
    if priority is None:
        priority = random.choice(PRIORITIES)
    
    tags = random.sample(["support", "customer-request", "ui", "backend", "urgent", "followup"], k=random.randint(0, 3))
    
    return {
        "customer_id": f"CUST-{customer_id:04d}",
        "customer_email": f"customer{customer_id}@example.com",
        "customer_name": f"Test Customer {customer_id}",
        "subject": subject,
        "description": description,
        "category": category,
        "priority": priority,
        "tags": ",".join(tags),
        "source": random.choice(SOURCES),
        "browser": random.choice(BROWSERS),
        "device_type": random.choice(DEVICE_TYPES),
    }


def write_csv(tickets, filepath):
    """Write tickets to CSV file"""
    fieldnames = ["customer_id", "customer_email", "customer_name", "subject", "description", 
                  "category", "priority", "tags", "source", "browser", "device_type"]
    
    with open(filepath, 'w', newline='', encoding='utf-8') as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        for ticket in tickets:
            writer.writerow(ticket)
    
    print(f"✅ Generated {filepath} with {len(tickets)} tickets")


def write_json(tickets, filepath):
    """Write tickets to JSON file"""
    # Convert for JSON format (nested metadata)
    json_tickets = []
    for ticket in tickets:
        # Handle both flat and nested format
        if "metadata" in ticket:
            # Already in JSON format
            json_tickets.append(ticket)
        else:
            # Convert from CSV format
            json_ticket = {
                "customer_id": ticket["customer_id"],
                "customer_email": ticket["customer_email"],
                "customer_name": ticket["customer_name"],
                "subject": ticket["subject"],
                "description": ticket["description"],
                "category": ticket["category"],
                "priority": ticket["priority"],
                "tags": ticket["tags"].split(",") if ticket["tags"] else [],
                "metadata": {
                    "source": ticket["source"],
                    "browser": ticket.get("browser"),
                    "device_type": ticket.get("device_type"),
                }
            }
            json_tickets.append(json_ticket)
    
    with open(filepath, 'w', encoding='utf-8') as f:
        json.dump(json_tickets, f, indent=2)
    
    print(f"✅ Generated {filepath} with {len(tickets)} tickets")


def write_xml(tickets, filepath):
    """Write tickets to XML file"""
    root = ET.Element("tickets")
    
    for ticket in tickets:
        ticket_elem = ET.SubElement(root, "ticket")
        
        # Add fields
        for key in ["customer_id", "customer_email", "customer_name", "subject", "description", "category", "priority"]:
            child = ET.SubElement(ticket_elem, key)
            child.text = str(ticket[key])
        
        # Add tags
        if ticket["tags"]:
            tags_elem = ET.SubElement(ticket_elem, "tags")
            for tag in ticket["tags"].split(","):
                if tag:
                    tag_elem = ET.SubElement(tags_elem, "tag")
                    tag_elem.text = tag
        
        # Add metadata
        metadata_elem = ET.SubElement(ticket_elem, "metadata")
        source_elem = ET.SubElement(metadata_elem, "source")
        source_elem.text = ticket["source"]
        if ticket["browser"]:
            browser_elem = ET.SubElement(metadata_elem, "browser")
            browser_elem.text = ticket["browser"]
        if ticket["device_type"]:
            device_elem = ET.SubElement(metadata_elem, "device_type")
            device_elem.text = ticket["device_type"]
    
    # Pretty print
    xml_str = minidom.parseString(ET.tostring(root)).toprettyxml(indent="  ")
    
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(xml_str)
    
    print(f"✅ Generated {filepath} with {len(tickets)} tickets")


def main():
    """Generate all sample data files"""
    output_dir = Path(__file__).parent
    
    print("Generating sample ticket data...\n")
    
    # Generate valid tickets with good distribution
    csv_tickets = []
    for i in range(50):
        category = CATEGORIES[i % len(CATEGORIES)]
        ticket = generate_ticket(i + 1, category=category)
        csv_tickets.append(ticket)
    
    json_tickets = []
    for i in range(20):
        category = CATEGORIES[i % len(CATEGORIES)]
        ticket = generate_ticket(i + 100, category=category)
        json_tickets.append(ticket)
    
    xml_tickets = []
    for i in range(30):
        category = CATEGORIES[i % len(CATEGORIES)]
        ticket = generate_ticket(i + 200, category=category)
        xml_tickets.append(ticket)
    
    # Write valid files
    write_csv(csv_tickets, output_dir / "sample_tickets.csv")
    write_json(json_tickets, output_dir / "sample_tickets.json")
    write_xml(xml_tickets, output_dir / "sample_tickets.xml")
    
    # Generate invalid files for error testing
    print("\nGenerating invalid test files...\n")
    
    # Invalid CSV
    invalid_csv = [
        {"customer_id": "", "customer_email": "invalid-email", "customer_name": "Test", 
         "subject": "", "description": "Too short", "category": "unknown_category",
         "priority": "super_urgent", "tags": "", "source": "pigeon", "browser": "", "device_type": ""},
        {"customer_id": "CUST-9999", "customer_email": "test@example.com", "customer_name": "A" * 300,
         "subject": "X" * 300, "description": "X", "category": "technical_issue",
         "priority": "medium", "tags": "", "source": "api", "browser": "", "device_type": ""},
    ]
    write_csv(invalid_csv, output_dir / "invalid_tickets.csv")
    
    # Invalid JSON (will be written as valid JSON but with invalid data)
    invalid_json = [
        {
            "customer_id": "",
            "customer_email": "not-an-email",
            "customer_name": "",
            "subject": "x",
            "description": "short",
            "category": "invalid_cat",
            "priority": "critical",
            "tags": [],
            "metadata": {"source": "invalid_source"}
        }
    ]
    write_json(invalid_json, output_dir / "invalid_tickets.json")
    
    # Invalid XML (malformed)
    with open(output_dir / "invalid_tickets.xml", 'w', encoding='utf-8') as f:
        f.write("""<?xml version="1.0" encoding="UTF-8"?>
<tickets>
  <ticket>
    <customer_id></customer_id>
    <customer_email>bad-email</customer_email>
    <customer_name></customer_name>
    <subject></subject>
    <description>too short</description>
    <category>bad_category</category>
    <priority>extreme</priority>
    <metadata>
      <source>carrier_pigeon</source>
    </metadata>
  </ticket>
  <ticket>
    <!-- Missing required fields -->
    <customer_id>CUST-8888</customer_id>
  </ticket>
</tickets>""")
    
    print(f"✅ Generated {output_dir / 'invalid_tickets.xml'} with error cases")
    
    print(f"\n📁 All sample data files generated in {output_dir.absolute()}")
    print("\nSummary:")
    print(f"  Valid:   50 CSV + 20 JSON + 30 XML = 100 total tickets")
    print(f"  Invalid: 2 CSV + 1 JSON + 2 XML = 5 error cases")


if __name__ == "__main__":
    main()
