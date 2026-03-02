# API Reference

Base URL:
```
http://localhost:3000
```

## Response Format
**Success**: JSON objects or lists.
**Error**:
```json
{
  "error": "Error message",
  "details": "Optional details"
}
```

## Ticket Model (Summary)
```json
{
  "id": "uuid",
  "customer_id": "string",
  "customer_email": "email",
  "customer_name": "string",
  "subject": "1-200 chars",
  "description": "10-2000 chars",
  "category": "account_access | technical_issue | billing_question | feature_request | bug_report | other",
  "priority": "urgent | high | medium | low",
  "status": "new | in_progress | waiting_customer | resolved | closed",
  "assigned_to": "string | null",
  "tags": ["string"],
  "metadata": { "source": "web_form | email | api | chat | phone", "browser": "string", "device_type": "desktop | mobile | tablet" },
  "created_at": "ISO 8601",
  "updated_at": "ISO 8601",
  "resolved_at": "ISO 8601 | null"
}
```

## Endpoints

### Create Ticket
`POST /tickets`

Request:
```json
{
  "customer_id": "CUST001",
  "customer_email": "john@example.com",
  "customer_name": "John Doe",
  "subject": "Cannot login",
  "description": "I cannot access my account"
}
```

Response: `201` with ticket object.

cURL:
```bash
curl -X POST http://localhost:3000/tickets \
  -H "Content-Type: application/json" \
  -d '{"customer_id":"CUST001","customer_email":"john@example.com","customer_name":"John Doe","subject":"Cannot login","description":"I cannot access my account"}'
```

### List Tickets
`GET /tickets`

Query params: `status`, `category`, `priority`, `customer_id`, `assigned_to`

Response: `200`
```json
{ "total": 2, "tickets": [ { "id": "uuid", "subject": "..." } ] }
```

cURL:
```bash
curl "http://localhost:3000/tickets?status=new&priority=high"
```

### Get Ticket
`GET /tickets/:id`

Response: `200` with ticket object, or `404`.

cURL:
```bash
curl http://localhost:3000/tickets/<id>
```

### Update Ticket
`PUT /tickets/:id`

Request (partial):
```json
{ "status": "resolved", "assigned_to": "agent-1" }
```

Response: `200` with updated ticket, or `404`.

cURL:
```bash
curl -X PUT http://localhost:3000/tickets/<id> \
  -H "Content-Type: application/json" \
  -d '{"status":"resolved","assigned_to":"agent-1"}'
```

### Delete Ticket
`DELETE /tickets/:id`

Response: `204`, or `404`.

cURL:
```bash
curl -X DELETE http://localhost:3000/tickets/<id>
```

### Bulk Import
`POST /tickets/import?type=csv|json|xml`

Upload multipart form file field `file`. If `type` is omitted, the file extension is used.

Response: `202`
```json
{
  "summary": { "total": 3, "successful": 2, "failed": 1 },
  "results": [ { "index": 0, "success": true, "id": "uuid" }, { "index": 1, "success": false, "errors": [] } ]
}
```

cURL:
```bash
curl -X POST "http://localhost:3000/tickets/import?type=csv" \
  -F "file=@sample_data/sample_tickets.csv"
```

### Auto-Classify Ticket
`POST /tickets/:id/auto-classify`

Request (optional overrides):
```json
{ "allow_override": true, "category": "technical_issue", "priority": "high" }
```

Response: `200`
```json
{
  "ticket_id": "uuid",
  "category": "technical_issue",
  "priority": "high",
  "confidence": 0.72,
  "reasoning": "Matched keywords for technical issues",
  "keywords": ["error", "crash"],
  "allow_override": true
}
```

cURL:
```bash
curl -X POST http://localhost:3000/tickets/<id>/auto-classify \
  -H "Content-Type: application/json" \
  -d '{"allow_override":true}'
```
