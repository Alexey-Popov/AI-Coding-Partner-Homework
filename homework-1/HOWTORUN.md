# ▶️ How to Run the Banking Transactions API

This guide provides step-by-step instructions to set up and run the Banking Transactions API on your local machine.

---

## 📋 Prerequisites

Before you begin, ensure you have the following installed:

### Required Software
- **Python 3.8 or higher** (Python 3.9+ recommended)
  - Check version: `python --version` or `python3 --version`
  - Download: [https://www.python.org/downloads/](https://www.python.org/downloads/)

### Optional Software (Recommended)
- **Git** - For cloning the repository
- **VS Code** - For editing and using REST Client extension
- **Postman** - For testing API endpoints
- **curl** - For command-line API testing (usually pre-installed on Linux/Mac)

---

## 🚀 Quick Start (3 Steps)

### 1️⃣ Clone the Repository
```bash
git clone <repository-url>
cd homework-1
```

### 2️⃣ Set Up Python Environment
```bash
# Create virtual environment
python -m venv venv

# Activate virtual environment
# On Linux/Mac:
source venv/bin/activate
# On Windows:
venv\Scripts\activate

# Install dependencies
pip install -r requirements.txt
```

### 3️⃣ Run the Server
```bash
# Option 1: Use the run script (Recommended)
# On Linux/Mac:
./demo/run.sh
# On Windows:
demo\run.bat

# Option 2: Run directly with uvicorn
cd src
uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

The API will be available at: **http://localhost:8000**

---

## 📖 Detailed Setup Instructions

### Step 1: Clone the Repository

```bash
# If you have Git installed
git clone <repository-url>
cd homework-1

# If you downloaded as ZIP
# Extract the ZIP file and navigate to homework-1 folder
cd homework-1
```

### Step 2: Create Virtual Environment

A virtual environment isolates project dependencies from your system Python.

**On Linux/Mac:**
```bash
# Create virtual environment
python3 -m venv venv

# Activate virtual environment
source venv/bin/activate

# You should see (venv) at the beginning of your terminal prompt
```

**On Windows (Command Prompt):**
```cmd
# Create virtual environment
python -m venv venv

# Activate virtual environment
venv\Scripts\activate

# You should see (venv) at the beginning of your terminal prompt
```

**On Windows (PowerShell):**
```powershell
# Create virtual environment
python -m venv venv

# Activate virtual environment
venv\Scripts\Activate.ps1

# If you get an error about execution policy, run:
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

### Step 3: Install Dependencies

With the virtual environment activated:

```bash
# Install all required packages
pip install -r requirements.txt

# Verify installation
pip list
```

Expected packages:
- fastapi==0.104.1
- uvicorn==0.24.0
- pydantic==2.5.0
- python-dateutil==2.8.2
- pytest==7.4.3

### Step 4: Run the Application

**Method 1: Using Run Scripts (Recommended)**

The run scripts automatically activate the virtual environment and start the server.

**Linux/Mac:**
```bash
# Make script executable (first time only)
chmod +x demo/run.sh

# Run the script
./demo/run.sh
```

**Windows:**
```cmd
demo\run.bat
```

**Method 2: Using Uvicorn Directly**

```bash
# Ensure virtual environment is activated
# Navigate to src directory
cd src

# Start the server
uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

**Method 3: Using Python Directly**

```bash
# From project root (with venv activated)
cd src
python -m uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

### Step 5: Verify the Server is Running

You should see output similar to:
```
INFO:     Uvicorn running on http://0.0.0.0:8000 (Press CTRL+C to quit)
INFO:     Started reloader process [12345] using WatchFiles
INFO:     Started server process [12346]
INFO:     Waiting for application startup.
INFO:     Application startup complete.
```

---

## 🧪 Testing the API

### Method 1: Interactive API Documentation (Easiest)

1. Open your browser
2. Navigate to: **http://localhost:8000/docs**
3. You'll see the Swagger UI with all endpoints
4. Click on any endpoint to expand it
5. Click "Try it out" to test the endpoint
6. Fill in the parameters and click "Execute"

**Alternative Documentation:**
- ReDoc: **http://localhost:8000/redoc**

### Method 2: Using curl (Command Line)

**Check API Status:**
```bash
curl http://localhost:8000/
```

**Create a Deposit Transaction:**
```bash
curl -X POST http://localhost:8000/api/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "toAccount": "ACC-12345",
    "amount": 1000.00,
    "currency": "USD",
    "type": "deposit",
    "status": "completed"
  }'
```

**Get All Transactions:**
```bash
curl http://localhost:8000/api/transactions
```

**Get Account Balance:**
```bash
curl http://localhost:8000/api/accounts/ACC-12345/balance
```

**Filter Transactions:**
```bash
curl "http://localhost:8000/api/transactions?accountId=ACC-12345"
curl "http://localhost:8000/api/transactions?type=deposit"
curl "http://localhost:8000/api/transactions?from=2024-01-01&to=2024-12-31"
```

### Method 3: Using VS Code REST Client Extension

1. Install the "REST Client" extension in VS Code
2. Open the file: `demo/sample-requests.http`
3. Click "Send Request" above any request
4. View the response in a new tab

### Method 4: Using Postman

1. Open Postman
2. Import the API by:
   - Going to http://localhost:8000/openapi.json
   - Copying the OpenAPI specification
   - Importing into Postman as "OpenAPI 3.0"
3. Test all endpoints from Postman collections

---

## 📁 Sample Data and Requests

### Using Sample Requests

The `demo/sample-requests.http` file contains 29+ pre-configured requests:
- Valid transaction creation (deposits, withdrawals, transfers)
- Validation error testing (invalid amounts, accounts, currencies)
- Transaction filtering examples
- Balance calculation examples
- Account summary examples

### Using Sample Data

The `demo/sample-data.json` file contains:
- 10 sample transactions
- Multiple test accounts (ACC-12345, ACC-67890, ACC-ABCDE, etc.)
- Various currencies (USD, EUR, GBP, JPY, CHF)
- Balance calculation examples

---

## 🛠️ Troubleshooting

### Issue: "python: command not found"

**Solution:**
- On some systems, Python 3 is installed as `python3`
- Try using `python3` instead of `python`:
  ```bash
  python3 -m venv venv
  ```

### Issue: "No module named 'fastapi'"

**Solution:**
- Virtual environment might not be activated
- Activate it first:
  ```bash
  # Linux/Mac
  source venv/bin/activate
  # Windows
  venv\Scripts\activate
  ```
- Then install dependencies:
  ```bash
  pip install -r requirements.txt
  ```

### Issue: "Port 8000 is already in use"

**Solution:**
- Another application is using port 8000
- Option 1: Stop the other application
- Option 2: Use a different port:
  ```bash
  uvicorn main:app --reload --port 8001
  ```

### Issue: "ModuleNotFoundError: No module named 'src'"

**Solution:**
- Make sure you're running uvicorn from the `src/` directory:
  ```bash
  cd src
  uvicorn main:app --reload
  ```

### Issue: Virtual environment not activating on Windows PowerShell

**Solution:**
- You may need to change the execution policy:
  ```powershell
  Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
  ```
- Then activate again:
  ```powershell
  venv\Scripts\Activate.ps1
  ```

### Issue: "ERROR: Could not find a version that satisfies the requirement"

**Solution:**
- Update pip to the latest version:
  ```bash
  pip install --upgrade pip
  ```
- Then try installing dependencies again:
  ```bash
  pip install -r requirements.txt
  ```

### Issue: Application crashes or shows errors

**Solution:**
- Check that all files are present in the `src/` directory
- Verify Python version (must be 3.8+):
  ```bash
  python --version
  ```
- Reinstall dependencies:
  ```bash
  pip uninstall -r requirements.txt -y
  pip install -r requirements.txt
  ```

---

## 🔄 Stopping the Server

To stop the FastAPI server:

1. Press **CTRL+C** in the terminal where the server is running
2. You should see:
   ```
   INFO:     Shutting down
   INFO:     Finished server process [12346]
   ```

To deactivate the virtual environment:
```bash
deactivate
```

---

## 📊 Available Endpoints

Once the server is running, these endpoints are available:

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/` | GET | API information |
| `/health` | GET | Health check |
| `/docs` | GET | Swagger UI (interactive docs) |
| `/redoc` | GET | ReDoc (alternative docs) |
| `/api/transactions` | POST | Create transaction |
| `/api/transactions` | GET | Get all transactions (with filters) |
| `/api/transactions/{id}` | GET | Get specific transaction |
| `/api/accounts/{accountId}/balance` | GET | Get account balance |
| `/api/accounts/{accountId}/summary` | GET | Get account summary |

---

## 🎯 Quick Testing Workflow

1. **Start the server**: `./demo/run.sh` or `demo\run.bat`
2. **Open Swagger UI**: http://localhost:8000/docs
3. **Create a deposit**:
   - Click on "POST /api/transactions"
   - Click "Try it out"
   - Use this JSON:
     ```json
     {
       "toAccount": "ACC-12345",
       "amount": 1000.00,
       "currency": "USD",
       "type": "deposit",
       "status": "completed"
     }
     ```
   - Click "Execute"
4. **Create a withdrawal**:
   ```json
   {
     "fromAccount": "ACC-12345",
     "amount": 150.00,
     "currency": "USD",
     "type": "withdrawal",
     "status": "completed"
   }
   ```
5. **Check balance**: GET `/api/accounts/ACC-12345/balance`
   - Expected: `{"accountId": "ACC-12345", "balance": 850.00, "currency": "ALL"}`
6. **Get summary**: GET `/api/accounts/ACC-12345/summary`
7. **Filter transactions**: GET `/api/transactions?accountId=ACC-12345`

---

## 💡 Tips for Development

### Auto-reload Feature
The `--reload` flag enables automatic server restart when code changes:
- Modify any Python file in `src/`
- Save the file
- Server automatically restarts
- Refresh your browser to see changes

### Viewing Logs
All server logs appear in the terminal:
- Request logs show each API call
- Error logs show detailed stack traces
- Use them to debug issues

### Testing Validation
Try these to see validation in action:
- Negative amount: `"amount": -100`
- Invalid account: `"toAccount": "invalid"`
- Invalid currency: `"currency": "XYZ"`
- Too many decimals: `"amount": 100.123`

---

## 📞 Getting Help

If you encounter issues not covered in this guide:
1. Check the `README.md` for project overview
2. Review `demo/sample-requests.http` for example requests
3. Examine `AI-PLAN.md` for implementation details
4. Check FastAPI documentation: https://fastapi.tiangolo.com/

---

## ✅ Verification Checklist

Before testing, verify:
- [ ] Python 3.8+ is installed
- [ ] Virtual environment is activated (you see `(venv)` in terminal)
- [ ] All dependencies are installed (`pip list` shows fastapi, uvicorn, etc.)
- [ ] Server is running (terminal shows "Uvicorn running on...")
- [ ] Browser can access http://localhost:8000
- [ ] Swagger UI loads at http://localhost:8000/docs

---

<div align="center">

**🎉 You're ready to start using the Banking Transactions API!**

Visit **http://localhost:8000/docs** to explore all endpoints interactively.

</div>