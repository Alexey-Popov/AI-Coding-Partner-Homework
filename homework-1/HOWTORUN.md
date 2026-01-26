# How to Run the Banking Transactions API

## Prerequisites

- Node.js (v14 or higher)
- npm (comes with Node.js)

## Installation

1. Navigate to the project directory:
   ```bash
   cd homework-1
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

## Running the Server

### Option 1: Using npm start
```bash
npm start
```

### Option 2: Using the demo script
```bash
./demo/run.sh
```

### Option 3: Direct node command
```bash
node src/index.js
```

The server will start on port 3000 by default. You'll see:
```
Banking Transactions API running on port 3000
```

## Testing the API

### Health Check
```bash
curl http://localhost:3000/health
```

### Create a Deposit
```bash
curl -X POST http://localhost:3000/transactions \
  -H "Content-Type: application/json" \
  -d '{"toAccount": "ACC-12345", "amount": 1000, "currency": "USD", "type": "deposit"}'
```

### Create a Transfer
```bash
curl -X POST http://localhost:3000/transactions \
  -H "Content-Type: application/json" \
  -d '{"fromAccount": "ACC-12345", "toAccount": "ACC-67890", "amount": 100.50, "currency": "USD", "type": "transfer"}'
```

### List Transactions
```bash
curl http://localhost:3000/transactions
```

### Filter Transactions by Account
```bash
curl "http://localhost:3000/transactions?accountId=ACC-12345"
```

### Filter by Type and Date Range
```bash
curl "http://localhost:3000/transactions?type=transfer&from=2024-01-01&to=2024-12-31"
```

### Get Account Balance
```bash
curl http://localhost:3000/accounts/ACC-12345/balance
```

### Calculate Interest
```bash
curl "http://localhost:3000/accounts/ACC-12345/interest?rate=0.05&days=30"
```

## Using the Sample Requests File

If you have the REST Client extension in VS Code, you can open `demo/sample-requests.http` and click "Send Request" on any request to test it.

## Environment Variables

- `PORT`: Server port (default: 3000)

Example:
```bash
PORT=8080 npm start
```

## Stopping the Server

Press `Ctrl+C` in the terminal where the server is running.
