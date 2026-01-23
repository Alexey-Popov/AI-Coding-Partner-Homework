# How to Run

## Prerequisites

- Java 21
- PostgreSQL

## Database Setup

Create a PostgreSQL database:

```bash
createdb banking
```

Or via psql:

```sql
CREATE DATABASE banking;
```

## Environment Variables

Set the following (or use defaults):

```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=banking
export DB_USER=postgres
export DB_PASSWORD=yourpassword
```

## Run Application

```bash
./gradlew bootRun
```

The API will be available at `http://localhost:3000`.

## Sample Requests

```bash
# Create a transfer
curl -X POST http://localhost:3000/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "toAccount": "ACC-67890",
    "amount": 100.50,
    "currency": "USD",
    "type": "transfer"
  }'

# List all transactions
curl http://localhost:3000/transactions

# Filter by account
curl "http://localhost:3000/transactions?accountId=ACC-12345"

# Get balance
curl http://localhost:3000/accounts/ACC-12345/balance

# Export to CSV
curl http://localhost:3000/transactions/export?format=csv
```
