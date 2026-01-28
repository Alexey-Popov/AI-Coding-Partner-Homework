# How to Run the Banking Transactions API

## Prerequisites

Before running the application, ensure you have the following installed:

- **Node.js** (v16.0.0 or higher)
- **npm** (comes with Node.js)

### Verify Installation

```bash
node --version   # Should output v16.x.x or higher
npm --version    # Should output 8.x.x or higher
```

## Environment Setup

### 1. Navigate to the Project Directory

```bash
cd homework-1
```

### 2. Install Dependencies

```bash
npm install
```

This will install:
- `express` - Web framework for Node.js

### 3. Environment Variables (Optional)

The application uses the following environment variable:

| Variable | Default | Description |
|----------|---------|-------------|
| `PORT` | `3000` | The port the server runs on |

To customize the port:

```bash
# Linux/macOS
export PORT=8080

# Windows (Command Prompt)
set PORT=8080

# Windows (PowerShell)
$env:PORT=8080
```

## Running the Application

### Start the Server

```bash
npm start
```

You should see:
```
Server running on port 3000
```

### Development Mode (with auto-restart)

If you have `nodemon` installed globally:

```bash
npx nodemon src/index.js
```

## Testing the API

### Option 1: Using the Test Script

```bash
chmod +x demo/requests.sh
./demo/requests.sh
```

> **Note:** Requires `jq` for JSON formatting. Install with:
> - macOS: `brew install jq`
> - Ubuntu: `sudo apt install jq`

### Option 2: Using VS Code REST Client

1. Install the "REST Client" extension in VS Code
2. Open `demo/test-requests.http`
3. Click "Send Request" above any request

### Option 3: Using Postman

1. Import `postman_collection.json` into Postman
2. Run individual requests or the entire collection

### Option 4: Using curl

```bash
# Create a deposit
curl -X POST http://localhost:3000/transactions \
  -H "Content-Type: application/json" \
  -d '{"toAccount":"ACC123","amount":500,"currency":"USD","type":"deposit"}'

# Get all transactions
curl http://localhost:3000/transactions

# Get account balance
curl http://localhost:3000/accounts/ACC123/balance
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/transactions` | Create a new transaction |
| GET | `/transactions` | List all transactions (with filters) |
| GET | `/transactions/:id` | Get transaction by ID |
| GET | `/transactions/export?format=csv` | Export transactions as CSV |
| GET | `/accounts/:accountId/balance` | Get account balance |
| GET | `/accounts/:accountId/summary` | Get account summary |
| GET | `/accounts/:accountId/interest` | Calculate simple interest |

## Troubleshooting

### Port Already in Use

```bash
# Find process using port 3000
lsof -i :3000

# Kill the process
kill -9 <PID>
```

### Module Not Found Error

```bash
# Reinstall dependencies
rm -rf node_modules
npm install
```

### Permission Denied (requests.sh)

```bash
chmod +x demo/requests.sh
```

## Project Structure

```
homework-1/
├── src/
│   ├── index.js              # Application entry point
│   ├── routes/
│   │   └── transactions.js   # Transaction routes & account endpoints
│   ├── models/
│   │   └── transaction.js    # Transaction model
│   ├── validators/
│   │   └── transactionValidator.js  # Input validation
│   └── utils/
│       └── helpers.js        # Shared utilities
├── demo/
│   ├── test-requests.http    # VS Code REST Client requests
│   └── requests.sh           # Shell script for testing
├── postman_collection.json   # Postman collection
├── package.json
├── README.md
└── HOWTORUN.md              # This file
```
