# Banking Transactions API - Demo Scripts

This folder contains scripts to easily run and manage the Banking Transactions API application.

## Available Scripts

### 🚀 `run.sh` - Start the Application

Builds and starts the Spring Boot application.

```bash
./run.sh
```

**What it does:**
1. Checks if port 8080 is already in use
2. Prompts to stop any existing processes on port 8080 (if found)
3. Builds the application (skipping tests for faster startup)
4. Starts the application on http://localhost:8080

**Note:** The script will automatically check for conflicts and give you the option to stop existing applications.

### 🛑 `stop.sh` - Stop the Application

Stops any running instance of the Banking Transactions API.

```bash
./stop.sh
```

**What it does:**
1. Finds all processes using port 8080
2. Gracefully stops them (SIGTERM first, then SIGKILL if needed)
3. Cleans up any related gradle daemon processes
4. Verifies all processes are stopped

## Quick Start

### Start the Application

```bash
cd /path/to/banking-transactions-api-claude/demo
./run.sh
```

Wait for the message:
```
Started BankingApplication in X.XXX seconds
```

The API will be available at: **http://localhost:8080**

### Test the API

```bash
# Get all transactions
curl http://localhost:8080/transactions

# Create a deposit
curl -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "toAccount": "ACC-123456",
    "amount": 1000.00,
    "currency": "USD",
    "type": "deposit"
  }'
```

### Stop the Application

Press `Ctrl+C` in the terminal where the application is running, or use:

```bash
./stop.sh
```

## Troubleshooting

### Port 8080 Already in Use

If you see an error about port 8080 being in use:

1. **Option 1:** Run `./stop.sh` to stop any existing applications
2. **Option 2:** Run `./run.sh` and choose "y" when prompted to stop existing processes
3. **Option 3:** Manually find and kill the process:
   ```bash
   lsof -i :8080
   kill -9 <PID>
   ```

### Application Won't Start

1. Make sure you have **Java 17+** installed:
   ```bash
   java -version
   ```

2. Make sure gradle wrapper is present in the parent directory:
   ```bash
   ls -la ../../gradlew
   ```

3. Try cleaning the build:
   ```bash
   cd ../..
   ./gradlew :banking-transactions-api-claude:clean
   ```

### Scripts Not Executable

If you get "Permission denied", make the scripts executable:

```bash
chmod +x run.sh stop.sh
```

## Additional Resources

- **API Documentation:** See the main [README.md](../README.md)
- **Sample Requests:** Check [sample-requests.http](sample-requests.http)
- **How to Run Guide:** See [HOWTORUN.md](../HOWTORUN.md)

## API Endpoints

Once running, you can access:

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/transactions` | Create a new transaction |
| GET | `/transactions` | Get all transactions |
| GET | `/transactions/{id}` | Get transaction by ID |
| GET | `/accounts/{accountId}/balance` | Get account balance |
| GET | `/accounts/{accountId}/summary` | Get account summary |

## Examples

See [sample-requests.http](sample-requests.http) for more examples and test cases.

