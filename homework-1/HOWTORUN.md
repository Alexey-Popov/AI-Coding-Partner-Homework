# ▶️ How to Run the Application

## 🚀 Quick Start

### Prerequisites
- **Node.js 18+** (LTS recommended)
- **npm** (Node Package Manager)
- A web browser (for testing the UI)
- *Optional*: [REST Client extension](https://marketplace.visualstudio.com/items?itemName=humao.rest-client) for VS Code

---

## 📦 Installation

### 1. Navigate to the homework folder
```bash
cd homework-1
```

### 2. Install dependencies
```bash
npm install
```

This will install:
- `express` (^5.2.1) — Web framework
- `nodemon` (^3.1.11) — Development auto-reload tool

---

## ▶️ Running the Application

### Development Mode (with auto-reload)
```bash
npm run dev
```
This mode automatically restarts the server when you make code changes.

### Production Mode
```bash
npm start
```

### Expected Output
You should see:
```
🚀 Banking Transaction API running on http://localhost:3000
📝 API Documentation:
   POST   /transactions           - Create new transaction
   GET    /transactions           - List all transactions (with filters)
   GET    /transactions/:id       - Get specific transaction
   GET    /accounts/:accountId/balance - Get account balance
   GET    /summary                - Get transaction summary
   GET    /health                 - Health check
```

The API will be available at: **`http://localhost:3000`**

---

## 🌐 Web Interface

Open your browser to **`http://localhost:3000`**

The interactive web UI includes:
- **Transaction creation forms** with smart fields based on type
- **Account balance checker**
- **Transaction summary** with currency grouping
- **Transaction list** with filtering
- **Real-time validation** feedback

---

## 🎬 Demo & Quick Testing

### Populate Test Data
After starting the server, populate it with 10 test transactions:

```bash
cd demo
.\populate-test-data.bat
```

This creates:
- 5 Deposits (USD, EUR, GBP, JPY, CAD)
- 2 Withdrawals (USD, GBP)
- 3 Transfers (USD, EUR)
- 4 Different accounts (ACC-12345, ACC-67890, ACC-ABC12, ACC-XYZ99)

---

## 🧪 Testing Methods

### 1. Web Interface Testing (Recommended for Beginners)

1. Open `http://localhost:3000` in your browser
2. Use the interactive forms to:
   - Create transactions (deposit/withdrawal/transfer)
   - Check account balances
   - View transaction summaries
   - Filter transactions

**Features**:
- ✅ Visual feedback
- ✅ Real-time validation
- ✅ No additional tools needed
- ✅ User-friendly interface

---

### 2. VS Code REST Client

**Setup**:
1. Install the [REST Client extension](https://marketplace.visualstudio.com/items?itemName=humao.rest-client)
2. Open `demo/sample-requests.http`
3. Click "Send Request" above each request

**Features**:
- ✅ All endpoints pre-configured
- ✅ Valid and invalid examples
- ✅ Easy to modify and test
- ✅ Response displayed in VS Code

**Note**: Sample requests updated for new validation rules.

---

### 3. cURL Commands

**For Linux/Mac**:
```bash
cd demo
chmod +x sample-requests.sh
./sample-requests.sh
```

**For Windows (PowerShell)**:
See `demo/sample-requests.sh` for individual cURL commands you can copy.

---

### 4. Windows Batch Script

```bash
cd demo
.\test-api.bat
```

Runs automated tests for:
- Health check
- Creating deposit
- Creating withdrawal
- Listing transactions
- Getting account balance
- Getting summary

---

## 📡 API Endpoints Reference

### Health Check
```http
GET http://localhost:3000/health
```

### Create Transaction Examples

**Transfer (requires both accounts)**:
```http
POST http://localhost:3000/transactions
Content-Type: application/json

{
  "fromAccount": "ACC-12345",
  "toAccount": "ACC-67890",
  "amount": 500.00,
  "currency": "USD",
  "type": "transfer"
}
```

**Deposit (only toAccount)**:
```http
POST http://localhost:3000/transactions
Content-Type: application/json

{
  "toAccount": "ACC-12345",
  "amount": 1000.00,
  "currency": "USD",
  "type": "deposit"
}
```

**Withdrawal (only fromAccount)**:
```http
POST http://localhost:3000/transactions
Content-Type: application/json

{
  "fromAccount": "ACC-12345",
  "amount": 250.00,
  "currency": "USD",
  "type": "withdrawal"
}
```

### List Transactions
```http
GET http://localhost:3000/transactions
```

**With filters**:
```http
GET http://localhost:3000/transactions?accountId=ACC-12345
GET http://localhost:3000/transactions?type=deposit
GET http://localhost:3000/transactions?from=2024-01-01&to=2024-12-31
GET http://localhost:3000/transactions?accountId=ACC-12345&type=transfer
```

### Get Specific Transaction
```http
GET http://localhost:3000/transactions/1000
```

### Get Account Balance
```http
GET http://localhost:3000/accounts/ACC-12345/balance
```

### Get Transaction Summary
```http
GET http://localhost:3000/summary
GET http://localhost:3000/summary?accountId=ACC-12345
```

---

## 🔍 Transaction Type Rules

**Important**: Different transaction types have different account requirements:

| Type | fromAccount | toAccount | Example Use Case |
|------|-------------|-----------|------------------|
| **deposit** | ❌ Not allowed | ✅ Required | Money coming into an account |
| **withdrawal** | ✅ Required | ❌ Not allowed | Money leaving an account |
| **transfer** | ✅ Required | ✅ Required | Money moving between accounts |

**Validation**:
- Account format: `ACC-XXXXX` (where X is alphanumeric)
- Amount: Positive number with max 2 decimal places
- Currency: Valid ISO 4217 code (USD, EUR, GBP, JPY, etc.)

---

## 🛠️ Troubleshooting

### Port 3000 Already in Use

**Windows**:
```powershell
# Find process using port 3000
netstat -ano | findstr :3000

# Kill the process (replace PID with actual process ID)
taskkill /PID <PID> /F
```

**Linux/Mac**:
```bash
# Find and kill process on port 3000
lsof -ti:3000 | xargs kill -9
```

### Dependencies Not Installing

```bash
# Clear npm cache
npm cache clean --force

# Delete node_modules and package-lock.json
rm -rf node_modules package-lock.json

# Reinstall
npm install
```

### Server Not Starting

1. Check Node.js version:
   ```bash
   node --version
   ```
   Should be 18.x or higher

2. Check if port 3000 is available
3. Look for error messages in the console
4. Ensure all dependencies are installed

---

## 📸 Demo Screenshots

Comprehensive screenshots are available in `docs/screenshots/` showing:
- All transaction types
- Balance calculations
- Currency-grouped summaries
- Filtering capabilities
- Validation error handling

See [docs/screenshots/DEMO_SCREENSHOTS.md](docs/screenshots/DEMO_SCREENSHOTS.md) for detailed descriptions.

---

## 📚 Additional Documentation

- **[README.md](README.md)** - Project overview and architecture
- **[docs/ASSIGNMENT_TASKS.md](docs/ASSIGNMENT_TASKS.md)** - Original requirements
- **[demo/README.md](demo/README.md)** - Demo files documentation
- **[docs/SUBMISSION_READY.md](docs/SUBMISSION_READY.md)** - Submission checklist

---

## 💡 Tips

1. **Start with the Web UI** - Easiest way to understand the API
2. **Use populate-test-data.bat** - Quick way to get sample data
3. **Check validation errors** - They provide detailed field-level feedback
4. **Try different currencies** - See how summary groups by currency
5. **Test filtering** - Combine multiple filter parameters

---

## 🎯 Quick Test Workflow

```bash
# Terminal 1: Start the server
npm start

# Terminal 2: Populate test data
cd demo
.\populate-test-data.bat

# Then:
# - Open http://localhost:3000 in browser
# - Try creating different transaction types
# - Check balances
# - View summary with currency grouping
```

---

**Ready to start!** 🚀 Run `npm start` and open http://localhost:3000