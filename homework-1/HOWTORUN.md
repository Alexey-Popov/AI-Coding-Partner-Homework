# ▶️ How to Run the Application

This guide explains how to set up, run, and use the Banking Transactions API application.

## Table of Contents
- [Prerequisites](#prerequisites)
- [Backend Setup](#backend-setup)
- [Frontend Setup](#frontend-setup)
- [Running the Application](#running-the-application)
- [Using the Application](#using-the-application)
- [API Documentation](#api-documentation)
- [Troubleshooting](#troubleshooting)

---

## Prerequisites

### Required Software
- **Python 3.11+** (for backend)
- **Java JDK 11+** (for H2 database)
- **Node.js 18+** and **npm** (for frontend)
- **Git** (for version control)

### Verify Installation
```bash
# Check Python version
python --version  # or python3 --version

# Check Java version
java -version

# Check Node.js and npm
node --version
npm --version
```

---

## Backend Setup

### 1. Navigate to Backend Directory
```bash
cd bank_api
```

### 2. Create Python Virtual Environment
```bash
# Create virtual environment
python -m venv venv

# Activate virtual environment
# On macOS/Linux:
source venv/bin/activate

# On Windows:
venv\Scripts\activate
```

### 3. Install Dependencies
```bash
pip install -r requirements.txt
```

### 4. Configure Environment Variables
Create a `.env` file in the `bank_api` directory:

```bash
# Required settings
SECRET_KEY=your-super-secret-key-change-this-in-production
DATABASE_URL=jdbc:h2:file:./bank.db
JWT_ALGORITHM=HS256
ACCESS_TOKEN_EXPIRE_MINUTES=30

# Admin user (created automatically on first startup)
ADMIN_EMAIL=admin@example.com
ADMIN_PASSWORD=admin123

# Optional settings
CORS_ORIGINS=http://localhost:5173,http://localhost:3000
LOG_LEVEL=INFO
```

**Important**: Change `SECRET_KEY` and `ADMIN_PASSWORD` to secure values in production.

### 5. Run Backend Server
```bash
# Make sure virtual environment is activated
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

The backend will:
- Start on http://localhost:8000
- Initialize the H2 database (creates `bank.db.mv.db` file)
- Run database migrations automatically
- Create the admin user (if it doesn't exist)

You should see output like:
```
INFO:     Application startup complete.
INFO:     Uvicorn running on http://0.0.0.0:8000 (Press CTRL+C to quit)
```

---

## Frontend Setup

### 1. Navigate to Frontend Directory
Open a **new terminal window** and navigate to the frontend:

```bash
cd ui
```

### 2. Install Dependencies
```bash
npm install
```

### 3. Configure Environment (Optional)
The frontend is pre-configured to connect to `http://localhost:8000`. If your backend runs on a different URL, create a `.env` file in the `ui` directory:

```bash
VITE_API_BASE_URL=http://localhost:8000
```

### 4. Run Frontend Development Server
```bash
npm run dev
```

The frontend will start on http://localhost:5173

You should see:
```
VITE v5.4.21  ready in 199 ms

➜  Local:   http://localhost:5173/
```

---

## Running the Application

### Quick Start (Both Servers)

**Terminal 1 - Backend:**
```bash
cd bank_api
source venv/bin/activate  # On macOS/Linux
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

**Terminal 2 - Frontend:**
```bash
cd ui
npm run dev
```

### Stopping the Application
- Press `CTRL+C` in each terminal to stop the servers
- Or kill processes on ports:
  ```bash
  # Kill backend
  lsof -ti:8000 | xargs kill -9

  # Kill frontend
  lsof -ti:5173 | xargs kill -9
  ```

---

## Using the Application

### First Time Setup

1. **Open the application** in your browser: http://localhost:5173

2. **Register a new user account:**
   - Click "Register" on the login page
   - Enter email, password, and full name
   - Click "Register" button
   - You'll be automatically logged in

3. **Or login as admin:**
   - Email: `admin@example.com`
   - Password: `admin123` (or your custom password from `.env`)

### User Features (Regular Users)

#### Dashboard
After login, you'll see the main dashboard with:
- List of your bank accounts
- Account balances
- Quick actions (view details, make transfer)

#### Creating an Account
Currently accounts are created automatically on user registration. Future versions will support multiple accounts per user.

#### Viewing Account Details
1. Click "View Details" on any account card
2. See account information:
   - Account number
   - Card number (masked for security)
   - Current balance
   - Transaction history

#### Making a Transfer
1. From the dashboard, click "Transfer" on the source account
2. Enter transfer details:
   - **Target card number**: The recipient's 16-digit card number
   - **Amount**: Transfer amount (must be > 0 and ≤ available balance)
   - **Description**: Optional description for the transfer
3. Click "Transfer" to submit
4. The transfer is processed immediately with:
   - Idempotency protection (duplicate prevention)
   - Balance validation
   - Atomic transaction (both accounts updated or neither)

#### Transaction History
- View all your transactions on the account details page
- See transaction type (DEPOSIT, WITHDRAWAL, TRANSFER)
- See transaction status (PENDING, COMPLETED, FAILED)
- Filter and sort by date

### Admin Features (Admin Users Only)

When logged in as an admin, you'll see additional gold-colored menu items:

#### User Management (`/admin/users`)
1. Click "Users" in the navigation menu
2. View all registered users with:
   - Email, full name, role, status
   - Creation date
3. **Filter users:**
   - By status (ACTIVE/BLOCKED)
   - By email (search)
4. **Block a user:**
   - Click "Block" next to any user
   - Confirm the action
   - Blocked users cannot login or access the system
5. **Unblock a user:**
   - Click "Unblock" next to a blocked user
   - User can login again

**Note**: Admins cannot block themselves.

#### Transaction Monitoring (`/admin/transactions`)
1. Click "Transactions" in the navigation menu
2. View all transactions across all users with:
   - Transaction ID, type, status
   - Source and target user information
   - Card numbers (masked)
   - Amount, currency, description
   - Timestamps
3. **Filter transactions:**
   - By type (DEPOSIT, WITHDRAWAL, TRANSFER)
   - By status (PENDING, COMPLETED, FAILED)
   - By user ID
   - By date range

---

## API Documentation

### Swagger UI (Interactive Docs)
- **URL**: http://localhost:8000/docs
- **Features**:
  - View all available endpoints
  - See request/response schemas
  - Test API endpoints directly in the browser
  - Authentication support (click "Authorize" and enter JWT token)

### OpenAPI JSON Schema
- **URL**: http://localhost:8000/openapi.json
- Download the full OpenAPI specification

### Health Check
```bash
curl http://localhost:8000/health
```

Response:
```json
{
  "status": "healthy",
  "database": "healthy",
  "timestamp": "2026-01-25T21:26:46.226553Z"
}
```

### Key API Endpoints

#### Authentication
- `POST /v1/auth/register` - Register new user
- `POST /v1/auth/login` - Login and get JWT token
- `GET /v1/auth/me` - Get current user info

#### Accounts
- `GET /v1/accounts` - List user's accounts
- `GET /v1/accounts/{account_id}` - Get account details
- `GET /v1/accounts/{account_id}/transactions` - Get account transactions

#### Transfers
- `POST /v1/transfers` - Create a new transfer

#### Admin (requires ADMIN role)
- `GET /v1/admin/users` - List all users
- `PATCH /v1/admin/users/{user_id}/block` - Block a user
- `PATCH /v1/admin/users/{user_id}/unblock` - Unblock a user
- `GET /v1/admin/transactions` - View all transactions

### Testing with cURL

#### Login
```bash
curl -X POST http://localhost:8000/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "password": "admin123"
  }'
```

#### Make a Transfer (with authentication)
```bash
TOKEN="your-jwt-token-here"

curl -X POST http://localhost:8000/v1/transfers \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "source_account_id": "source-account-uuid",
    "target_card_number": "1234567812345678",
    "amount": 100.00,
    "currency": "USD",
    "description": "Test transfer"
  }'
```

#### Admin: List Users
```bash
TOKEN="your-admin-jwt-token-here"

curl -X GET "http://localhost:8000/v1/admin/users?status=ACTIVE&limit=10" \
  -H "Authorization: Bearer $TOKEN"
```

---

## Troubleshooting

### Backend Issues

#### Port 8000 already in use
```bash
# Find and kill the process
lsof -ti:8000 | xargs kill -9

# Or use a different port
uvicorn app.main:app --host 0.0.0.0 --port 8001 --reload
```

#### Database connection errors
- Make sure Java is installed: `java -version`
- Check the `DATABASE_URL` in `.env`
- Delete `bank.db.mv.db` to reset the database (will lose all data)

#### Admin user not created
- Check `.env` file has `ADMIN_EMAIL` and `ADMIN_PASSWORD`
- Restart the backend server
- Check logs for "Admin user created successfully"

#### Migration errors
If migrations fail:
1. Stop the backend
2. Delete the database file: `rm bank.db.mv.db`
3. Restart the backend (will recreate clean database)

#### Swagger UI not loading
- Clear browser cache
- Check browser console for CSP errors
- Verify you're accessing http://localhost:8000/docs (not https)

### Frontend Issues

#### Port 5173 already in use
```bash
# Kill the process
lsof -ti:5173 | xargs kill -9
```

#### API connection errors
- Verify backend is running on http://localhost:8000
- Check browser console for CORS errors
- Verify `CORS_ORIGINS` in backend `.env` includes `http://localhost:5173`

#### Login issues
- Check browser console for error messages
- Verify user exists in database
- For blocked users, you'll get a specific error message
- Check JWT token is being stored (browser DevTools → Application → Local Storage)

#### Admin pages not accessible
- Verify you're logged in as an admin user
- Check JWT token includes `"role": "ADMIN"`
- Non-admin users are automatically redirected to dashboard

### General Issues

#### Dependencies not installing
```bash
# Backend: upgrade pip
pip install --upgrade pip setuptools wheel

# Frontend: clear npm cache
npm cache clean --force
rm -rf node_modules package-lock.json
npm install
```

#### CORS errors in browser
Update `CORS_ORIGINS` in backend `.env`:
```bash
CORS_ORIGINS=http://localhost:5173,http://localhost:3000
```

Restart the backend after changing `.env` files.

---

## Production Deployment

For production deployment:

1. **Change default credentials** in `.env`
   - Use strong `SECRET_KEY` (generate with `openssl rand -hex 32`)
   - Use strong `ADMIN_PASSWORD`

2. **Build frontend for production**
   ```bash
   cd ui
   npm run build
   # Outputs to ui/dist/
   ```

3. **Run backend with production settings**
   ```bash
   uvicorn app.main:app --host 0.0.0.0 --port 8000 --workers 4
   ```

4. **Use environment variables instead of `.env` file**

5. **Set up reverse proxy** (nginx/Apache) for HTTPS

6. **Configure proper CORS origins** for your production domain

7. **Set up database backups** (H2 file is in `bank_api/bank.db.mv.db`)

---

## Default Credentials

### Admin User
- **Email**: `admin@example.com`
- **Password**: `admin123` (or value from `ADMIN_PASSWORD` in `.env`)
- **Role**: ADMIN
- **Status**: ACTIVE

**Security Note**: Change the admin password in production by updating the `ADMIN_PASSWORD` environment variable before first run.

---

## Support

For issues and questions:
- Check the [README.md](README.md) for architecture details
- Review [RUNNING_STATUS.md](RUNNING_STATUS.md) for current system status
- Check API docs at http://localhost:8000/docs
- Review browser console and server logs for error messages

---

**Happy Banking! 🏦**
