# How to Run the Banking Transactions API

## Prerequisites

- **Node.js** (v18 or higher recommended)
- **npm** (comes with Node.js)

## Quick Start

### Option 1: Using the demo script

**Windows:**
```bash
cd homework-1/demo
run.bat
```

**Linux/Mac:**
```bash
cd homework-1/demo
chmod +x run.sh
./run.sh
```

### Option 2: Manual setup

1. Navigate to the homework-1 directory:
   ```bash
   cd homework-1
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Start the server:
   ```bash
   npm start
   ```

4. The API will be running at: `http://localhost:3000`

## Testing the API

### Using the Web Frontend (Recommended)

The easiest way to test the API is through the built-in web interface:

1. Start the server (see Quick Start above)
2. Open your browser and go to: `http://localhost:3000`
3. You'll see a dashboard with all available operations:

**Create Transaction**
- Fill in the form with account numbers (format: `ACC-XXXXX`)
- Enter amount, select currency and transaction type
- Click "Create Transaction"
- The response will show below the form

**Get Transaction by ID**
- Enter a transaction ID (e.g., `TXN-000001`)
- Click "Get" to retrieve the transaction details

**Get Account Balance**
- Enter an account ID (e.g., `ACC-12345`)
- Click "Get Balance" to see the current balance

**View All Transactions**
- Use the filter fields to narrow results:
  - Filter by Account ID
  - Filter by Type (transfer, deposit, withdrawal)
  - Filter by Date Range
- Click "Load Transactions" to refresh the list
- Click "Export CSV" to download all transactions as a CSV file

---

### Using VS Code REST Client

1. Install the "REST Client" extension in VS Code
2. Open `demo/sample-requests.http`
3. Click "Send Request" above each request to test

### Using curl

```bash
# Health check
curl http://localhost:3000/

# Create a transaction
curl -X POST http://localhost:3000/transactions \
  -H "Content-Type: application/json" \
  -d '{"fromAccount":"ACC-12345","toAccount":"ACC-67890","amount":100.50,"currency":"USD","type":"transfer"}'

# Get all transactions
curl http://localhost:3000/transactions

# Get transaction by ID
curl http://localhost:3000/transactions/TXN-000001

# Get filtered transactions
curl "http://localhost:3000/transactions?accountId=ACC-12345"
curl "http://localhost:3000/transactions?type=transfer"

# Get account balance
curl http://localhost:3000/accounts/ACC-12345/balance

# Export as CSV
curl http://localhost:3000/transactions/export?format=csv
```

### Using Postman

1. Import the requests from `demo/sample-requests.http` or create them manually
2. Base URL: `http://localhost:3000`
3. Use the endpoints documented in README.md

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `PORT` | 3000 | Port number for the server |

To use a different port:
```bash
PORT=4000 npm start
```

## Troubleshooting

### Port already in use
If port 3000 is busy, either:
- Kill the process using the port
- Use a different port: `PORT=3001 npm start`

### Module not found errors
Run `npm install` to ensure all dependencies are installed.
