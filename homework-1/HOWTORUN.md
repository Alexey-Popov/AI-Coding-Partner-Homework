# ▶️ How to Run the Banking Transactions API

This guide provides step-by-step instructions to set up and run the Banking Transactions API on your local machine.

---

## ⚠️ Important: Server Must Run from src/ Directory

**Critical:** The server MUST be started from the `src/` directory for routes to work correctly.

```bash
# ✅ CORRECT - From src directory:
cd homework-1/src
node index.js

# ✅ CORRECT - Using run script (handles directory automatically):
cd homework-1
./demo/run.sh

# ❌ WRONG - From homework-1 directory:
cd homework-1
node src/index.js  # This will cause 404 errors!
```

If you see "Cannot GET /" or "Cannot POST /transactions", check your current directory!

### ⚠️ Multiple Server Instances Problem

**Issue:** Transactions don't clear after "restart"
**Cause:** Multiple Node.js processes running simultaneously

**Solution:** Always stop all servers before starting:
```bash
# Kill all Node.js servers on port 3000
lsof -ti:3000 | xargs kill -9

# Then start fresh
./demo/run.sh
```

**Check for multiple instances:**
```bash
ps aux | grep "node.*index.js" | grep -v grep
# Should show only ONE process
```

---

## 📋 Prerequisites

Before you begin, ensure you have the following installed:

### Required Software

| Software | Minimum Version | Check Command | Download Link |
|----------|----------------|---------------|---------------|
| **Node.js** | 14.x or higher | `node --version` | [nodejs.org](https://nodejs.org/) |
| **npm** | 6.x or higher | `npm --version` | Included with Node.js |

### Optional Tools

- **jq** (for formatted JSON in command-line tests): `brew install jq` (macOS) or `apt-get install jq` (Linux)
- **VS Code** with REST Client extension (for `.http` file testing)
- **Postman** (alternative API testing tool)
- **curl** (usually pre-installed on macOS/Linux)

---

## 🚀 Installation Steps

### Step 1: Navigate to Project Directory

```bash
cd /path/to/AI-Coding-Partner-Homework/homework-1
```

### Step 2: Install Dependencies

Navigate to the `src` directory and install Node.js dependencies:

```bash
cd src
npm install
```

**Expected output:**
```
added 92 packages, and audited 93 packages in 2s

26 packages are looking for funding
  run `npm fund` for details

found 0 vulnerabilities
```

### Step 3: Verify Installation

Check that dependencies were installed correctly:

```bash
ls -la node_modules | head -10
```

You should see folders for `express`, `nodemon`, and other dependencies.

---

## ▶️ Starting the Server

### Option 1: Using the Run Script (Recommended)

From the `homework-1` directory:

```bash
./demo/run.sh
```

**Expected output:**
```
🚀 Starting Banking Transactions API...

✅ Dependencies ready
🌐 Starting server on port 3000...
📡 API will be available at http://localhost:3000

Press Ctrl+C to stop the server
---

[nodemon] 3.1.11
[nodemon] to restart at any time, enter `rs`
[nodemon] watching path(s): *.*
[nodemon] watching extensions: js,mjs,cjs,json
[nodemon] starting `node index.js`
🚀 Banking Transactions API running on port 3000
📡 Health check: http://localhost:3000/
```

### Option 2: Direct npm Commands

From the `src` directory:

**For development (with auto-restart on file changes):**
```bash
npm run dev
```

**For production (no auto-restart):**
```bash
npm start
```

### Option 3: Direct Node Command

From the `src` directory:

```bash
node index.js
```

---

## ✅ Verify Server is Running

### Test 1: Health Check Endpoint

Open a new terminal window and run:

```bash
curl http://localhost:3000/
```

**Expected response:**
```json
{
  "message": "Banking Transactions API",
  "version": "1.0.0",
  "status": "running"
}
```

### Test 2: Check Server Logs

In the terminal where the server is running, you should see:
```
🚀 Banking Transactions API running on port 3000
📡 Health check: http://localhost:3000/
```

---

## 🧪 Testing the API

### Option 1: Automated Test Script (Recommended)

Run the comprehensive test script (server must be running):

```bash
# From homework-1 directory
./demo/sample-requests.sh
```

This will:
- ✅ Test all endpoints
- ✅ Verify validation
- ✅ Check filtering
- ✅ Test balance calculation
- ✅ Demonstrate rate limiting
- ✅ Display colored output with test results

**Sample output:**
```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
1. HEALTH CHECK
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

▶ GET /
{
  "message": "Banking Transactions API",
  "version": "1.0.0",
  "status": "running"
}
...
```

### Option 2: VS Code REST Client

If you have VS Code with the REST Client extension:

1. Open `demo/sample-requests.http` in VS Code
2. Click "Send Request" above any request
3. View the response in the split pane

### Option 3: Manual curl Commands

#### Create a Transaction

```bash
curl -X POST http://localhost:3000/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "toAccount": "ACC-67890",
    "amount": 100.50,
    "currency": "USD",
    "type": "transfer"
  }'
```

#### Get All Transactions

```bash
curl http://localhost:3000/transactions
```

#### Get Account Balance

```bash
curl http://localhost:3000/accounts/ACC-12345/balance
```

#### Filter Transactions

```bash
curl "http://localhost:3000/transactions?accountId=ACC-12345"
curl "http://localhost:3000/transactions?type=deposit"
curl "http://localhost:3000/transactions?from=2024-01-01&to=2024-12-31"
```

### Option 4: Postman

1. Import the endpoints from `demo/sample-requests.http`
2. Or manually create requests to `http://localhost:3000/transactions`

---

## 🧩 Sample Test Workflow

Follow these steps to test the complete functionality:

### 1. Create Some Transactions

```bash
# Create a deposit
curl -X POST http://localhost:3000/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-00000",
    "toAccount": "ACC-12345",
    "amount": 1000.00,
    "currency": "USD",
    "type": "deposit"
  }'

# Create a transfer
curl -X POST http://localhost:3000/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "toAccount": "ACC-67890",
    "amount": 250.50,
    "currency": "USD",
    "type": "transfer"
  }'

# Create a withdrawal
curl -X POST http://localhost:3000/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "toAccount": "ACC-00000",
    "amount": 100.00,
    "currency": "USD",
    "type": "withdrawal"
  }'
```

### 2. Verify Transactions Were Created

```bash
curl http://localhost:3000/transactions | jq '.'
```

You should see 3 transactions.

### 3. Check Account Balance

```bash
curl http://localhost:3000/accounts/ACC-12345/balance | jq '.'
```

**Expected output:**
```json
{
  "accountId": "ACC-12345",
  "balance": 649.5,
  "currency": "USD",
  "transactionCount": 3
}
```

**Calculation**: 1000.00 (deposit) - 250.50 (transfer out) - 100.00 (withdrawal) = 649.50 ✅

### 4. Test Filtering

```bash
# Filter by account
curl "http://localhost:3000/transactions?accountId=ACC-12345" | jq '. | length'
# Should return: 3

# Filter by type
curl "http://localhost:3000/transactions?type=deposit" | jq '. | length'
# Should return: 1

# Combined filters
curl "http://localhost:3000/transactions?accountId=ACC-12345&type=transfer" | jq '. | length'
# Should return: 1
```

### 5. Test Validation (Should Return Errors)

```bash
# Invalid amount (negative)
curl -X POST http://localhost:3000/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "toAccount": "ACC-67890",
    "amount": -50,
    "currency": "USD",
    "type": "transfer"
  }' | jq '.'

# Expected:
# {
#   "error": "Validation failed",
#   "details": [
#     {
#       "field": "amount",
#       "message": "Amount must be a positive number"
#     }
#   ]
# }
```

### 6. Test Rate Limiting

Check the rate limit headers on any request:

```bash
curl -i http://localhost:3000/ | grep "X-RateLimit"
```

**Expected output:**
```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 99
X-RateLimit-Reset: 1769003628690
```

---

## ⚠️ Troubleshooting

### Problem: Port Already in Use

**Error:**
```
Error: listen EADDRINUSE: address already in use :::3000
```

**Solutions:**

1. **Find and kill the process using port 3000:**
   ```bash
   # macOS/Linux
   lsof -ti:3000 | xargs kill -9

   # Or manually find the process
   lsof -i:3000
   # Then: kill -9 <PID>
   ```

2. **Use a different port:**
   ```bash
   PORT=3001 node index.js
   ```

### Problem: Dependencies Not Installing

**Error:**
```
npm ERR! code ENOENT
npm ERR! syscall open
npm ERR! path /path/to/package.json
```

**Solutions:**

1. **Verify you're in the correct directory:**
   ```bash
   pwd
   # Should be: .../homework-1/src
   ls package.json
   ```

2. **Try clearing npm cache:**
   ```bash
   npm cache clean --force
   npm install
   ```

### Problem: Module Not Found

**Error:**
```
Error: Cannot find module 'express'
```

**Solution:**
```bash
cd src
npm install
```

### Problem: Permission Denied on Scripts

**Error:**
```
bash: ./demo/run.sh: Permission denied
```

**Solution:**
```bash
chmod +x demo/run.sh
chmod +x demo/sample-requests.sh
```

### Problem: curl Command Not Found

**Solution:**

- **macOS**: curl is pre-installed
- **Windows**: Use Git Bash or install curl
- **Linux**: `sudo apt-get install curl` or `sudo yum install curl`

### Problem: jq Not Installed (Pretty JSON)

The test scripts work better with `jq` for formatting JSON output.

**Install jq:**

- **macOS**: `brew install jq`
- **Linux**: `sudo apt-get install jq` or `sudo yum install jq`
- **Windows**: Download from [stedolan.github.io/jq](https://stedolan.github.io/jq/)

**Alternative**: Remove `| jq '.'` from curl commands to see unformatted JSON

### Problem: Server Stops Unexpectedly

**Check for errors in the console output:**

- Look for any stack traces or error messages
- Verify all dependencies are installed
- Check that Node.js version is 14.x or higher

**Restart the server:**
```bash
# Stop: Ctrl+C
# Start again:
./demo/run.sh
```

---

## 🛑 Stopping the Server

### If Running in Foreground

Press `Ctrl+C` in the terminal where the server is running.

### If Running in Background

```bash
# Find the process
ps aux | grep node

# Kill by PID
kill <PID>

# Or kill all node processes (use with caution)
killall node
```

---

## 📊 Expected Behavior

### Successful Startup

```
🚀 Banking Transactions API running on port 3000
📡 Health check: http://localhost:3000/
```

### Successful Request

- **Status Code**: 200 (GET), 201 (POST)
- **Response**: Valid JSON with transaction data
- **Headers**: Include `X-RateLimit-*` headers

### Validation Error

- **Status Code**: 400
- **Response**: JSON with error and details array

### Not Found Error

- **Status Code**: 404
- **Response**: JSON with error and resource ID

### Rate Limit Exceeded

- **Status Code**: 429
- **Response**: JSON with error, retry information
- **Headers**: Include `Retry-After`

---

## 🎯 Quick Reference

### Common Commands

```bash
# Start server
./demo/run.sh

# Run all tests
./demo/sample-requests.sh

# Create transaction
curl -X POST http://localhost:3000/transactions -H "Content-Type: application/json" -d '{...}'

# Get all transactions
curl http://localhost:3000/transactions

# Get balance
curl http://localhost:3000/accounts/ACC-12345/balance

# Filter transactions
curl "http://localhost:3000/transactions?accountId=ACC-12345"

# Check rate limits
curl -i http://localhost:3000/ | grep X-RateLimit
```

### API Endpoints

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/` | GET | Health check |
| `/transactions` | POST | Create transaction |
| `/transactions` | GET | List transactions (with filters) |
| `/transactions/:id` | GET | Get specific transaction |
| `/accounts/:accountId/balance` | GET | Get account balance |
| `/accounts/:accountId/summary` | GET | Get account summary |

---

## 📝 Additional Notes

### Data Persistence

- This API uses **in-memory storage**
- All data is lost when the server restarts
- This is intentional for the homework assignment

### Development Mode vs Production

- **Development** (`npm run dev`): Uses nodemon, auto-restarts on file changes
- **Production** (`npm start`): Regular node, no auto-restart

### Environment Variables

You can customize the port:

```bash
PORT=3001 npm start
```

Or create a `.env` file (already in .gitignore):

```
PORT=3001
```

---

## ✅ Verification Checklist

Before considering setup complete, verify:

- [ ] Server starts without errors
- [ ] Health check endpoint returns 200
- [ ] Can create a transaction (POST)
- [ ] Can retrieve transactions (GET)
- [ ] Validation errors return 400
- [ ] Balance calculation is correct
- [ ] Filtering works
- [ ] Rate limit headers are present
- [ ] Test script runs successfully

---

<div align="center">

**Need more help?** Refer to the comprehensive API documentation in [README.md](README.md)

**Having issues?** Check the Troubleshooting section above

</div>