# How to Run the Banking Transactions API

## Prerequisites

- **Node.js** version 18.x or higher
- **npm** (comes with Node.js)

To check your versions:
```bash
node --version
npm --version
```

---

## Installation

### Option 1: Using the Demo Script

```bash
cd homework-1
./demo/run.sh
```

This script will:
1. Check for Node.js installation
2. Install dependencies if needed
3. Start the server

### Option 2: Manual Installation

```bash
# Navigate to the homework-1 directory
cd homework-1

# Install dependencies
npm install

# Start the server
npm start
```

---

## Verify It's Running

Once started, you should see:
```
Banking Transactions API running on http://localhost:3000
Available endpoints:
  POST   /transactions          - Create a transaction
  GET    /transactions          - List all transactions
  GET    /transactions/:id      - Get transaction by ID
  GET    /transactions/export   - Export as CSV
  GET    /accounts/:id/balance  - Get account balance
  GET    /health                - Health check
```

### Health Check

```bash
curl http://localhost:3000/health
```

Expected response:
```json
{"status":"ok","timestamp":"2024-01-15T10:30:00.000Z"}
```

---

## Testing the API

### Using curl

```bash
# Create a deposit transaction
curl -X POST http://localhost:3000/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "toAccount": "ACC-12345",
    "amount": 1000.00,
    "currency": "USD",
    "type": "deposit"
  }'

# Get all transactions
curl http://localhost:3000/transactions

# Get account balance
curl http://localhost:3000/accounts/ACC-12345/balance
```

### Using VS Code REST Client

Open `demo/sample-requests.http` in VS Code with the REST Client extension installed. Click "Send Request" on any request to execute it.

### Using Postman

Import the requests from `demo/sample-requests.http` or create requests manually using the endpoints documented in [README.md](./README.md).

---

## Configuration

### Port

By default, the API runs on port 3000. To use a different port:

```bash
PORT=8080 npm start
```

---

## Stopping the Server

Press `Ctrl+C` in the terminal where the server is running.

---

## Troubleshooting

### "Port 3000 is already in use"

Another process is using port 3000. Either:
1. Stop the other process
2. Use a different port: `PORT=3001 npm start`

### "Module not found" errors

Run `npm install` to ensure all dependencies are installed.

### Permission denied on run.sh

Make the script executable:
```bash
chmod +x demo/run.sh
```

---

## Development Mode

For auto-restart on file changes (requires Node.js 18+):

```bash
npm run dev
```
