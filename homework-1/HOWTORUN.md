# ▶️ How to Run the Banking Transactions API

This guide provides step-by-step instructions to set up and run the Banking Transactions API on your local machine.

---

## 📋 Prerequisites

Before running the application, ensure you have the following installed:

- **Node.js** (v16 or higher) - [Download here](https://nodejs.org/)
- **npm** (comes with Node.js)
- **Git** (optional, for cloning the repository)

To verify your installations:

```bash
node --version
npm --version
```

---

## 🚀 Quick Start

### Option 1: Using the Run Script (Recommended)

The easiest way to start the application:

```bash
# Navigate to the homework-1 directory
cd homework-1

# Make the run script executable (Unix/Mac)
chmod +x demo/run.sh

# Run the script
./demo/run.sh
```

The script will automatically:
1. Check for Node.js installation
2. Install dependencies if needed
3. Build the TypeScript code
4. Start the server

### Option 2: Manual Setup

If you prefer to run commands manually:

```bash
# Navigate to the homework-1 directory
cd homework-1

# Install dependencies
npm install

# Build the TypeScript code
npm run build

# Start the server
npm start
```

---

## 🔧 Development Mode

For development with auto-reload on file changes:

```bash
# Install dependencies (if not already done)
npm install

# Run in development mode with nodemon
npm run dev
```

This will start the server with `ts-node` and automatically restart when you make changes to the source code.

---

## ✅ Verify the Server is Running

Once the server starts, you should see output like:

```
==================================================
🏦 Banking Transactions API
==================================================
✅ Server running on http://localhost:3000
📝 Environment: development
==================================================

Available endpoints:
  GET    http://localhost:3000/
  POST   http://localhost:3000/transactions
  GET    http://localhost:3000/transactions
  GET    http://localhost:3000/transactions/:id
  GET    http://localhost:3000/transactions/export?format=csv
  GET    http://localhost:3000/accounts/:accountId/balance
==================================================
```

Test the health check endpoint:

```bash
curl http://localhost:3000/
```

You should receive a JSON response with API information.

---

## 🧪 Testing the API

### Method 1: Using VS Code REST Client

1. Install the **REST Client** extension in VS Code
2. Open `demo/sample-requests.http`
3. Click "Send Request" above any request to test it

### Method 2: Using curl

Create a transaction:

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

Get all transactions:

```bash
curl http://localhost:3000/transactions
```

Get account balance:

```bash
curl http://localhost:3000/accounts/ACC-12345/balance
```

Export transactions as CSV:

```bash
curl http://localhost:3000/transactions/export?format=csv
```

### Method 3: Using Postman

1. Import the endpoints into Postman
2. Set the base URL to `http://localhost:3000`
3. Use the sample requests from `demo/sample-requests.http` as a guide

---

## 🔍 Sample Test Workflow

Here's a complete test workflow to verify all functionality:

```bash
# 1. Create a deposit
curl -X POST http://localhost:3000/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-00000",
    "toAccount": "ACC-12345",
    "amount": 1000.00,
    "currency": "USD",
    "type": "deposit"
  }'

# 2. Create a transfer
curl -X POST http://localhost:3000/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "toAccount": "ACC-67890",
    "amount": 250.50,
    "currency": "USD",
    "type": "transfer"
  }'

# 3. Check balance
curl http://localhost:3000/accounts/ACC-12345/balance

# 4. Get all transactions
curl http://localhost:3000/transactions

# 5. Filter transactions by account
curl "http://localhost:3000/transactions?accountId=ACC-12345"

# 6. Export to CSV
curl http://localhost:3000/transactions/export?format=csv -o transactions.csv
```

---

## 🛠️ Configuration

### Environment Variables

Create a `.env` file in the `homework-1` directory (optional):

```env
PORT=3000
NODE_ENV=development
```

Default values will be used if `.env` is not present.

### Changing the Port

To run on a different port:

```bash
PORT=8080 npm start
```

Or update the `.env` file:

```env
PORT=8080
```

---

## 🐛 Troubleshooting

### Port Already in Use

If you see an error like "Port 3000 is already in use":

1. Stop any other process using port 3000
2. Or change the port using the `PORT` environment variable:

```bash
PORT=3001 npm start
```

### Dependencies Not Installing

If `npm install` fails:

1. Clear npm cache: `npm cache clean --force`
2. Delete `node_modules` and `package-lock.json`
3. Run `npm install` again

### TypeScript Build Errors

If you encounter TypeScript compilation errors:

1. Ensure you're using Node.js v16 or higher
2. Delete the `dist` folder: `rm -rf dist`
3. Rebuild: `npm run build`

### Module Not Found Errors

If you see "Cannot find module" errors:

1. Ensure all dependencies are installed: `npm install`
2. Rebuild the project: `npm run build`
3. Check that you're running from the `homework-1` directory

---

## 🛑 Stopping the Server

To stop the server:

- Press `Ctrl + C` in the terminal where the server is running

---

## 📁 Project Structure

```
homework-1/
├── src/                # TypeScript source code
├── dist/               # Compiled JavaScript (generated)
├── demo/               # Demo scripts and sample requests
├── node_modules/       # Dependencies (generated)
├── package.json        # Project configuration
├── tsconfig.json       # TypeScript configuration
└── README.md          # Project documentation
```

---

## 📚 Additional Resources

- **API Documentation**: See `README.md` for detailed API documentation
- **Sample Requests**: Check `demo/sample-requests.http` for all test cases
- **Sample Data**: Review `demo/sample-data.json` for example transactions

---

## 💡 Tips

- Use the VS Code REST Client extension for easy API testing
- Check the terminal output for request logs
- All data is stored in-memory and will be lost when the server restarts
- The API returns detailed validation errors to help with debugging

---

## ✅ Success Checklist

- [ ] Node.js is installed (v16+)
- [ ] Dependencies are installed (`npm install`)
- [ ] TypeScript code is compiled (`npm run build`)
- [ ] Server starts without errors
- [ ] Health check endpoint responds (`curl http://localhost:3000/`)
- [ ] Can create transactions
- [ ] Can retrieve transactions
- [ ] Can check account balances
- [ ] Can export to CSV

---

<div align="center">

**Need help?** Check the troubleshooting section or review the error messages in the terminal.

</div>