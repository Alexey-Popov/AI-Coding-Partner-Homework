# ▶️ How to Run the Banking Transactions API

This guide provides step-by-step instructions to set up and run the Banking Transactions API on your local machine.

---

## 📋 Prerequisites

Before you begin, ensure you have the following installed:

### Required Software

1. **Node.js** (version 14.x or higher)
   - Download from: https://nodejs.org/
   - Verify installation: `node --version`

2. **npm** (comes with Node.js)
   - Verify installation: `npm --version`

### Optional Tools (for testing)

- **curl** - For command-line API testing (pre-installed on macOS/Linux)
- **Postman** - For GUI-based API testing (https://www.postman.com/)
- **VS Code REST Client extension** - For testing .http files

---

## 🚀 Quick Start

### Method 1: Using the Run Script (Recommended)

```bash
# Navigate to the project directory
cd homework-1

# Make the script executable (if not already)
chmod +x demo/run.sh

# Run the application
./demo/run.sh
```

The script will:
1. Check if Node.js is installed
2. Install dependencies if needed
3. Start the server on http://localhost:3000

### Method 2: Manual Setup

```bash
# 1. Navigate to the project directory
cd homework-1

# 2. Install dependencies
npm install

# 3. Start the server
npm start
```

### Method 3: Development Mode (with auto-restart)

```bash
# Install dependencies
npm install

# Start in development mode (requires nodemon)
npm run dev
```

---

## ✅ Verify the Installation

Once the server is running, you should see:

```
🚀 Banking Transactions API running on http://localhost:3000
📝 API documentation available at http://localhost:3000
```

### Test the API

Open a new terminal window and run:

```bash
curl http://localhost:3000/
```

You should receive a JSON response with API information and available endpoints.

---

## 🧪 Testing the API

### Option 1: Automated Test Script

Run the comprehensive test script to execute all sample requests:

```bash
# Make the script executable (if not already)
chmod +x demo/sample-requests.sh

# Run the test script
./demo/sample-requests.sh
```

This will:
- Test all API endpoints
- Create sample transactions
- Test validation
- Display results in your terminal

**Note:** Requires `jq` for JSON formatting. Install with:
- macOS: `brew install jq`
- Linux: `sudo apt-get install jq`

### Option 2: Manual Testing with curl

#### Create a Deposit Transaction
```bash
curl -X POST http://localhost:3000/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "toAccount": "ACC-12345",
    "amount": 500.00,
    "currency": "USD",
    "type": "deposit"
  }'
```

#### Create a Transfer Transaction
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

#### Get Account Summary
```bash
curl http://localhost:3000/accounts/ACC-12345/summary
```

#### Filter Transactions by Account
```bash
curl "http://localhost:3000/transactions?accountId=ACC-12345"
```

#### Filter Transactions by Type
```bash
curl "http://localhost:3000/transactions?type=deposit"
```

### Option 3: Using VS Code REST Client

If you have the REST Client extension installed in VS Code:

1. Open `demo/sample-requests.http`
2. Click "Send Request" above any request
3. View the response in the adjacent panel

### Option 4: Using Postman

1. Import the requests from `demo/sample-requests.http` or manually create them
2. Set the base URL to `http://localhost:3000`
3. Add `Content-Type: application/json` header for POST requests
4. Send requests and view responses

---

## 🛑 Stopping the Server

To stop the server:

1. Go to the terminal where the server is running
2. Press `Ctrl + C`

Or, if running in the background:

```bash
# Find the process
ps aux | grep "node src/index.js"

# Kill the process (replace PID with actual process ID)
kill <PID>

# Or use pkill
pkill -f "node src/index.js"
```

---

## 🔧 Configuration

### Port Configuration

By default, the API runs on port 3000. To change the port:

```bash
# Set the PORT environment variable
PORT=8080 npm start
```

### Environment Variables

You can create a `.env` file in the project root (optional):

```env
PORT=3000
NODE_ENV=development
```

**Note:** The `.env` file is ignored by git (included in `.gitignore`).

---

## 📝 Project Structure

```
homework-1/
├── src/                    # Source code
│   ├── index.js           # Main application file
│   ├── models/            # Data models
│   ├── routes/            # API route handlers
│   ├── validators/        # Input validation
│   └── utils/             # Helper functions
├── demo/                  # Demo and test files
│   ├── run.sh            # Quick start script
│   ├── sample-requests.sh # Automated tests
│   ├── sample-requests.http # REST Client tests
│   └── sample-data.json  # Sample data
├── package.json          # Dependencies and scripts
└── README.md            # Project documentation
```

---

## 🐛 Troubleshooting

### Issue: Port Already in Use

**Error:** `Error: listen EADDRINUSE: address already in use :::3000`

**Solution:**
```bash
# Find the process using port 3000
lsof -i :3000

# Kill the process (replace PID)
kill -9 <PID>

# Or change the port
PORT=3001 npm start
```

### Issue: Dependencies Not Installed

**Error:** `Cannot find module 'express'`

**Solution:**
```bash
# Delete node_modules and reinstall
rm -rf node_modules package-lock.json
npm install
```

### Issue: Permission Denied on Run Script

**Error:** `Permission denied: ./demo/run.sh`

**Solution:**
```bash
chmod +x demo/run.sh
./demo/run.sh
```

### Issue: Node.js Not Found

**Error:** `command not found: node`

**Solution:**
- Install Node.js from https://nodejs.org/
- Verify installation: `node --version`

---

## 📊 API Endpoints Reference

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | API information |
| POST | `/transactions` | Create transaction |
| GET | `/transactions` | List all transactions |
| GET | `/transactions/:id` | Get transaction by ID |
| GET | `/accounts/:accountId/balance` | Get account balance |
| GET | `/accounts/:accountId/summary` | Get account summary |

### Query Parameters

For `GET /transactions`:
- `accountId` - Filter by account
- `type` - Filter by type (deposit/withdrawal/transfer)
- `from` - Start date (YYYY-MM-DD)
- `to` - End date (YYYY-MM-DD)

---

## 📚 Additional Resources

- **Test Results:** See [demo/TEST_REPORT.md](demo/TEST_REPORT.md)
- **Project Overview:** See [README.md](README.md)
- **Sample Requests:** See [demo/sample-requests.http](demo/sample-requests.http)

---

## 🆘 Need Help?

If you encounter any issues:

1. Check the troubleshooting section above
2. Verify all prerequisites are installed
3. Ensure you're in the correct directory (`homework-1`)
4. Check the terminal for error messages

---

<div align="center">

**Ready to start? Run `./demo/run.sh` and begin testing!** 🚀

</div>