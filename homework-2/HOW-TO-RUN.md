# How to Run

## Prerequisites

- Python 3.11+

## Setup

```bash
cd homework-2
pip install -r requirements.txt
```

## Run the Server

```bash
python run.py
```

The API starts at `http://localhost:5000`.

## Example Requests

**Create a ticket:**

```bash
curl -X POST http://localhost:5000/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "customer_id": "CUST-001",
    "customer_email": "john@example.com",
    "customer_name": "John Doe",
    "subject": "Cannot login",
    "description": "I cannot login to my account since yesterday",
    "category": "account_access",
    "priority": "high"
  }'
```

**List tickets (with optional filters):**

```bash
curl http://localhost:5000/tickets
curl http://localhost:5000/tickets?status=new&priority=high
```

**Bulk import (JSON):**

```bash
curl -X POST "http://localhost:5000/tickets/import?format=json" \
  -H "Content-Type: application/json" \
  -d '[{"customer_id":"C1","customer_email":"a@b.com","customer_name":"Alice","subject":"Test","description":"A test ticket description here","category":"bug_report","priority":"low"}]'
```

**Bulk import (CSV file upload):**

```bash
curl -X POST http://localhost:5000/tickets/import \
  -F "file=@tickets.csv"
```

**Bulk import (XML):**

```bash
curl -X POST "http://localhost:5000/tickets/import?format=xml" \
  -H "Content-Type: text/xml" \
  -d '<tickets><ticket><customer_id>X1</customer_id><customer_email>x@y.com</customer_email><customer_name>XML User</customer_name><subject>XML Ticket</subject><description>Imported from XML format</description><category>other</category><priority>low</priority></ticket></tickets>'
```
