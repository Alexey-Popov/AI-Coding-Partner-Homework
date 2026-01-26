# API Testing with curl

Complete curl examples for testing the Banking API.

## Setup

Start the backend:
```bash
cd bank_api
source venv/bin/activate
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

API will be available at http://localhost:8000

## Authentication Flow

### 1. Register New User

```bash
curl -X POST http://localhost:8000/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice@example.com",
    "password": "password123",
    "full_name": "Alice Smith"
  }'
```

Expected response (201 Created):
```json
{
  "id": "uuid",
  "email": "alice@example.com",
  "full_name": "Alice Smith",
  "created_at": "2024-01-01T12:00:00.000Z"
}
```

### 2. Login

```bash
curl -X POST http://localhost:8000/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice@example.com",
    "password": "password123"
  }'
```

Expected response (200 OK):
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "bearer"
}
```

**Save the token for subsequent requests:**
```bash
export TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### 3. Get Current User

```bash
curl -X GET http://localhost:8000/v1/auth/me \
  -H "Authorization: Bearer $TOKEN"
```

Expected response (200 OK):
```json
{
  "id": "uuid",
  "email": "alice@example.com",
  "full_name": "Alice Smith",
  "created_at": "2024-01-01T12:00:00.000Z"
}
```

## Account Management

### 4. Create EUR Account

```bash
curl -X POST http://localhost:8000/v1/accounts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "currency": "EUR",
    "initial_balance": "1000.00",
    "card_number": "1111 2222 3333 4444"
  }'
```

Expected response (201 Created):
```json
{
  "id": "account-uuid-1",
  "user_id": "user-uuid",
  "card_number": "1111 2222 3333 4444",
  "currency": "EUR",
  "balance": "1000.0000",
  "created_at": "2024-01-01T12:00:00.000Z",
  "updated_at": "2024-01-01T12:00:00.000Z"
}
```

**Save account ID:**
```bash
export EUR_ACCOUNT_ID="account-uuid-1"
```

### 5. Create USD Account

```bash
curl -X POST http://localhost:8000/v1/accounts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "currency": "USD",
    "initial_balance": "500.00",
    "card_number": "5555 6666 7777 8888"
  }'
```

**Save account ID:**
```bash
export USD_ACCOUNT_ID="account-uuid-2"
```

### 6. Create Account with Auto-Generated Card Number

```bash
curl -X POST http://localhost:8000/v1/accounts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "currency": "GBP",
    "initial_balance": "0"
  }'
```

### 7. List All Accounts

```bash
curl -X GET http://localhost:8000/v1/accounts \
  -H "Authorization: Bearer $TOKEN"
```

Expected response (200 OK):
```json
{
  "accounts": [
    {
      "id": "account-uuid-1",
      "card_number": "1111 2222 3333 4444",
      "currency": "EUR",
      "balance": "1000.0000",
      ...
    },
    {
      "id": "account-uuid-2",
      "card_number": "5555 6666 7777 8888",
      "currency": "USD",
      "balance": "500.0000",
      ...
    }
  ]
}
```

### 8. Get Account Details

```bash
curl -X GET http://localhost:8000/v1/accounts/$EUR_ACCOUNT_ID \
  -H "Authorization: Bearer $TOKEN"
```

## Transfers

### 9. Transfer EUR to USD with Currency Conversion

```bash
curl -X POST http://localhost:8000/v1/transfers \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "source_card_number": "1111 2222 3333 4444",
    "target_card_number": "5555 6666 7777 8888",
    "source_currency": "EUR",
    "source_amount": "100.00",
    "target_currency": "USD",
    "fx_rate": "1.1",
    "description": "Currency exchange"
  }'
```

Expected response (201 Created):
```json
{
  "transaction_id": "txn-uuid",
  "status": "COMPLETED",
  "source_account": {
    "id": "account-uuid-1",
    "balance": "900.0000",
    "currency": "EUR",
    ...
  },
  "target_account": {
    "id": "account-uuid-2",
    "balance": "610.0000",
    "currency": "USD",
    ...
  }
}
```

**Calculation:** 100 EUR × 1.1 = 110 USD
- Source balance: 1000 - 100 = 900 EUR
- Target balance: 500 + 110 = 610 USD

### 10. Transfer with Idempotency Key

```bash
curl -X POST http://localhost:8000/v1/transfers \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: unique-key-12345" \
  -d '{
    "source_card_number": "1111 2222 3333 4444",
    "target_card_number": "5555 6666 7777 8888",
    "source_currency": "EUR",
    "source_amount": "50.00",
    "target_currency": "USD",
    "fx_rate": "1.1"
  }'
```

If you retry with the same idempotency key, you'll get a 409 Conflict.

### 11. Transfer with Auto-Calculated Target Amount

```bash
curl -X POST http://localhost:8000/v1/transfers \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "source_card_number": "1111 2222 3333 4444",
    "target_card_number": "5555 6666 7777 8888",
    "source_currency": "EUR",
    "source_amount": "25.00",
    "target_currency": "USD",
    "fx_rate": "1.08"
  }'
```

Target amount will be auto-calculated: 25 × 1.08 = 27 USD

## Transaction History

### 12. Get Account Transactions (First Page)

```bash
curl -X GET "http://localhost:8000/v1/accounts/$EUR_ACCOUNT_ID/transactions?limit=20&offset=0" \
  -H "Authorization: Bearer $TOKEN"
```

Expected response (200 OK):
```json
{
  "transactions": [
    {
      "id": "txn-uuid",
      "created_at": "2024-01-01T12:00:00.000Z",
      "type": "TRANSFER",
      "source_account_id": "account-uuid-1",
      "target_account_id": "account-uuid-2",
      "source_amount": "100.0000",
      "source_currency": "EUR",
      "target_amount": "110.0000",
      "target_currency": "USD",
      "fx_rate": "1.10000000",
      "description": "Currency exchange",
      "status": "COMPLETED"
    }
  ],
  "total": 1,
  "limit": 20,
  "offset": 0
}
```

### 13. Get Transactions with Pagination

```bash
# Get next 20 transactions
curl -X GET "http://localhost:8000/v1/accounts/$EUR_ACCOUNT_ID/transactions?limit=20&offset=20" \
  -H "Authorization: Bearer $TOKEN"

# Get 10 transactions per page
curl -X GET "http://localhost:8000/v1/accounts/$EUR_ACCOUNT_ID/transactions?limit=10&offset=0" \
  -H "Authorization: Bearer $TOKEN"
```

## Health Check

### 14. Check API Health

```bash
curl -X GET http://localhost:8000/health
```

Expected response (200 OK):
```json
{
  "status": "healthy",
  "database": "healthy",
  "timestamp": "2024-01-01T12:00:00.000Z"
}
```

## Error Scenarios

### 15. Register with Duplicate Email (409 Conflict)

```bash
curl -X POST http://localhost:8000/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice@example.com",
    "password": "password123"
  }'
```

Expected response (409 Conflict):
```json
{
  "detail": "Email already registered"
}
```

### 16. Login with Invalid Credentials (401 Unauthorized)

```bash
curl -X POST http://localhost:8000/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice@example.com",
    "password": "wrongpassword"
  }'
```

Expected response (401 Unauthorized):
```json
{
  "detail": "Invalid credentials"
}
```

### 17. Access Protected Endpoint Without Token (403 Forbidden)

```bash
curl -X GET http://localhost:8000/v1/accounts
```

Expected response (403 Forbidden):
```json
{
  "detail": "Not authenticated"
}
```

### 18. Transfer with Insufficient Funds (422 Unprocessable Entity)

```bash
curl -X POST http://localhost:8000/v1/transfers \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "source_card_number": "1111 2222 3333 4444",
    "target_card_number": "5555 6666 7777 8888",
    "source_currency": "EUR",
    "source_amount": "99999.00",
    "target_currency": "USD",
    "fx_rate": "1.1"
  }'
```

Expected response (422 Unprocessable Entity):
```json
{
  "detail": "Insufficient funds: available 900.0000, required 99999.00"
}
```

### 19. Transfer to Non-Existent Account (400 Bad Request)

```bash
curl -X POST http://localhost:8000/v1/transfers \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "source_card_number": "1111 2222 3333 4444",
    "target_card_number": "9999 9999 9999 9999",
    "source_currency": "EUR",
    "source_amount": "10.00",
    "target_currency": "USD",
    "fx_rate": "1.1"
  }'
```

Expected response (400 Bad Request):
```json
{
  "detail": "Target account not found"
}
```

### 20. Create Account with Duplicate Card Number (400 Bad Request)

```bash
curl -X POST http://localhost:8000/v1/accounts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "currency": "EUR",
    "initial_balance": "0",
    "card_number": "1111 2222 3333 4444"
  }'
```

Expected response (400 Bad Request):
```json
{
  "detail": "Card number already exists"
}
```

## Complete Test Flow

Run this complete flow to test all functionality:

```bash
# 1. Register
curl -X POST http://localhost:8000/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com", "password": "test123", "full_name": "Test User"}'

# 2. Login and save token
TOKEN=$(curl -X POST http://localhost:8000/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com", "password": "test123"}' \
  | jq -r '.access_token')

echo "Token: $TOKEN"

# 3. Create EUR account
curl -X POST http://localhost:8000/v1/accounts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"currency": "EUR", "initial_balance": "1000", "card_number": "1111 1111 1111 1111"}'

# 4. Create USD account
curl -X POST http://localhost:8000/v1/accounts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"currency": "USD", "initial_balance": "0", "card_number": "2222 2222 2222 2222"}'

# 5. List accounts
curl -X GET http://localhost:8000/v1/accounts \
  -H "Authorization: Bearer $TOKEN"

# 6. Execute transfer
curl -X POST http://localhost:8000/v1/transfers \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "source_card_number": "1111 1111 1111 1111",
    "target_card_number": "2222 2222 2222 2222",
    "source_currency": "EUR",
    "source_amount": "100",
    "target_currency": "USD",
    "fx_rate": "1.1",
    "description": "Test transfer"
  }'

# 7. View updated accounts
curl -X GET http://localhost:8000/v1/accounts \
  -H "Authorization: Bearer $TOKEN"
```

## Tips

### Pretty Print JSON with jq

```bash
curl http://localhost:8000/v1/accounts \
  -H "Authorization: Bearer $TOKEN" | jq
```

### Save Response to File

```bash
curl http://localhost:8000/v1/accounts \
  -H "Authorization: Bearer $TOKEN" > accounts.json
```

### Verbose Output (See Headers)

```bash
curl -v http://localhost:8000/health
```

### Extract Specific Field with jq

```bash
# Extract just the access token
curl -X POST http://localhost:8000/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com", "password": "test123"}' \
  | jq -r '.access_token'

# Extract account balances
curl http://localhost:8000/v1/accounts \
  -H "Authorization: Bearer $TOKEN" \
  | jq '.accounts[] | {currency, balance}'
```

## Additional Resources

- **Swagger UI**: http://localhost:8000/docs
- **OpenAPI JSON**: http://localhost:8000/openapi.json
- **ReDoc**: http://localhost:8000/redoc
