# ▶️ How to Run the Banking Transactions API

## 📋 Prerequisites

Before running the application, ensure you have the following installed:

- **Node.js** (v14 or higher) and npm - [Download here](https://nodejs.org/)
- **curl** or **Postman** (for testing API endpoints)
- **Git** (for cloning the repository)

---

## 🚀 Installation & Setup

### Step 1: Clone the Repository

```bash
git clone <repository-url>
cd AI-Coding-Partner-Homework/homework-1
```

### Step 2: Install Dependencies

```bash
npm install
```

---

## ▶️ Running the Application

### Start the Server

```bash
npm start
# or for development with auto-reload
npm run dev
```

### Using the Demo Script

Alternatively, you can use the provided demo script:

```bash
# On Linux/Mac
chmod +x demo/run.sh
./demo/run.sh

# On Windows
demo\run.bat
```

---

## 🌐 API Access

Once the server is running, the API will be available at:

```
http://localhost:3000
```

> **Note:** Port may vary depending on configuration. Check the console output for the actual port.

---

## 🧪 Testing the API

### Option 1: Using curl

**Create a new transaction:**
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

**Get all transactions:**
```bash
curl http://localhost:3000/transactions
```

**Get a specific transaction:**
```bash
curl http://localhost:3000/transactions/<transaction-id>
```

**Get account balance:**
```bash
curl http://localhost:3000/accounts/ACC-12345/balance
```

**Filter transactions by account:**
```bash
curl "http://localhost:3000/transactions?accountId=ACC-12345"
```

**Filter transactions by type:**
```bash
curl "http://localhost:3000/transactions?type=transfer"
```

**Filter transactions by date range:**
```bash
curl "http://localhost:3000/transactions?from=2024-01-01&to=2024-12-31"
```

### Option 2: Using the Sample Requests File

Run the provided sample requests:

```bash
# If using .http file (with REST Client extension in VS Code)
# Open demo/sample-requests.http and click "Send Request"

# If using shell script
chmod +x demo/sample-requests.sh
./demo/sample-requests.sh
```

### Option 3: Using Postman

1. Import the `demo/postman-collection.json` file (if provided)
2. Set the base URL to `http://localhost:3000`
3. Execute the requests in order

---

## 📊 Expected Response Examples

### Successful Transaction Creation (201 Created)
```json
{
  "id": "txn-123456",
  "fromAccount": "ACC-12345",
  "toAccount": "ACC-67890",
  "amount": 100.50,
  "currency": "USD",
  "type": "transfer",
  "timestamp": "2024-01-15T10:30:00Z",
  "status": "completed"
}
```

### Validation Error (400 Bad Request)
```json
{
  "error": "Validation failed",
  "details": [
    {
      "field": "amount",
      "message": "Amount must be a positive number"
    }
  ]
}
```

### Not Found Error (404 Not Found)
```json
{
  "error": "Transaction not found",
  "message": "No transaction found with id: txn-999999"
}
```

---

## 🛑 Stopping the Server

Press `Ctrl + C` in the terminal where the server is running.

---

## 🐛 Troubleshooting

### Port Already in Use
If you get a "port already in use" error:

```bash
# Find and kill the process using the port (Linux/Mac)
lsof -ti:3000 | xargs kill -9

# On Windows
netstat -ano | findstr :3000
taskkill /PID <PID> /F
```

Or change the port in the configuration file/environment variables.

### Dependencies Not Installing
- Ensure you're in the correct directory (`homework-1/`)
- Check your Node.js version: `node --version`
- Try deleting `node_modules/` and reinstalling:
  ```bash
  rm -rf node_modules package-lock.json
  npm install
  ```

### API Not Responding
- Verify the server is running (check terminal output)
- Confirm the correct port number
- Check firewall settings
- Ensure no proxy settings are interfering

---

## 📚 Additional Resources

- API Documentation: Check `docs/api-documentation.md` (if available)
- Project README: See `README.md` for architecture and implementation details
- Task Requirements: See `TASKS.md` for complete assignment specifications

---

## ✅ Verification Checklist

After starting the application, verify:

- [ ] Server starts without errors
- [ ] Can access `http://localhost:3000` (or configured port)
- [ ] Can create a new transaction via POST
- [ ] Can retrieve all transactions via GET
- [ ] Can filter transactions by account/type/date
- [ ] Can get account balance
- [ ] Validation errors are returned for invalid data
- [ ] API returns appropriate HTTP status codes

---

<div align="center">

**🎉 You're all set! The Banking Transactions API is now running.**

For questions or issues, please refer to the README.md or contact the developer.

</div>