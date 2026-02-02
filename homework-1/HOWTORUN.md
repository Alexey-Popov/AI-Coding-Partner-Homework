# How to Run the Application

## Prerequisites

- Java 21 or later
- Gradle 8.x (or use the Gradle wrapper if included)

## Build

```bash
cd homework-1
gradle build
```

## Run

```bash
gradle run
```

The server starts on http://localhost:8080.

## Quick Test

```bash
# Create a transaction
curl -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{"fromAccount":"ACC-12345","toAccount":"ACC-67890","amount":100.50,"currency":"USD","type":"transfer"}'

# List all transactions
curl http://localhost:8080/transactions

# Get account balance
curl http://localhost:8080/accounts/ACC-12345/balance
```

## Windows

Run `demo/run.bat` to start the application on Windows.
