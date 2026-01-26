# How to Run the Banking Transactions API

## Prerequisites

- **Java 17** or higher
- **Gradle** (or use the included Gradle wrapper)

### Verify Java Installation

```bash
java -version
```

Expected output should show Java 17+:
```
openjdk version "17.0.x" ...
```

## Quick Start

### Option 1: Using the Demo Script

```bash
./demo/run.sh
```

### Option 2: Using Gradle Directly

```bash
# Build the application
./gradlew build

# Run the application
./gradlew bootRun
```

### Option 3: Using the JAR File

```bash
# Build the JAR
./gradlew build

# Run the JAR
java -jar build/libs/banking-0.0.1-SNAPSHOT.jar
```

## Verify the Application is Running

Once started, the API will be available at: **http://localhost:8080**

Test with a simple request:

```bash
curl http://localhost:8080/transactions
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/transactions` | Create a new transaction |
| GET | `/transactions` | Get all transactions (with optional filters) |
| GET | `/transactions/{id}` | Get transaction by ID |
| GET | `/accounts/{accountId}/balance` | Get account balance |
| GET | `/accounts/{accountId}/summary` | Get account summary |

### Query Parameters for GET /transactions

- `accountId` - Filter by account ID
- `type` - Filter by transaction type (deposit, withdrawal, transfer)
- `from` - Filter by start date (YYYY-MM-DD)
- `to` - Filter by end date (YYYY-MM-DD)

## Sample Requests

### Create a Deposit

```bash
curl -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "toAccount": "ACC-123456",
    "amount": 1000.00,
    "currency": "USD",
    "type": "deposit"
  }'
```

### Create a Transfer

```bash
curl -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-123456",
    "toAccount": "ACC-789012",
    "amount": 500.00,
    "currency": "EUR",
    "type": "transfer"
  }'
```

### Get Account Balance

```bash
curl http://localhost:8080/accounts/ACC-123456/balance
```

For more sample requests, see [demo/sample-requests.http](demo/sample-requests.http).

For sample transaction data, see [demo/sample-data.json](demo/sample-data.json).

## Running Tests

```bash
./gradlew test
```

View test reports at: `build/reports/tests/test/index.html`

## Troubleshooting

### Port Already in Use

If port 8080 is in use, either stop the other process or change the port:

```bash
SERVER_PORT=8081 ./gradlew bootRun
```

### Permission Denied on run.sh

```bash
chmod +x demo/run.sh
```

### Gradle Wrapper Not Executable

```bash
chmod +x gradlew
```
