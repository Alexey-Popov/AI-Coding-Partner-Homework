# 🏦 Homework 1: Banking Transactions API

> **Student Name**: Yevgen Polukov
> **Date Submitted**: 25 June 2026
> **AI Tools Used**: Claude Code (Claude Sonnet 4.5), Copilot 

---

## 📋 Project Overview

This is a production-ready full-stack banking application with a complete REST API and modern web interface. The project demonstrates clean architecture, type safety, proper error handling, and security best practices.

**Key Features:**
- User authentication with JWT tokens
- Multi-currency account management
- Atomic transfers with currency conversion
- Transaction history with pagination
- OpenAPI/Swagger documentation
- Clean layered architecture
- Full TypeScript UI with React
- Comprehensive test coverage

## ✅ Testing Status

**Both backend and frontend have been fully tested and are 100% operational:**

- ✅ Backend API - All endpoints tested and working ([FIXES_APPLIED.md](FIXES_APPLIED.md))
- ✅ Frontend UI - Complete integration test passed ([FRONTEND_TESTING.md](FRONTEND_TESTING.md))
- ✅ **Docker Compose** - Production-ready deployment tested and working
- ✅ End-to-end workflow validated (register → login → accounts → transfer → history)
- ✅ Python 3.13 compatibility issues resolved
- ✅ H2 JDBC integration fully functional
- ✅ Currency conversion and balance updates verified
- ✅ Swagger UI accessible and functional
- ✅ Database persistence across container restarts

**See detailed test results:**
- Backend fixes: [FIXES_APPLIED.md](FIXES_APPLIED.md)
- Frontend testing: [FRONTEND_TESTING.md](FRONTEND_TESTING.md)
- Docker deployment: [DOCKER_SUMMARY.md](DOCKER_SUMMARY.md)

## 🐳 Docker Deployment (Recommended)

**Production-ready containerized deployment with Docker Compose:**

```bash
# Quick start (3 steps)
cp .env.example .env         # 1. Copy env template
nano .env                    # 2. Set JWT_SECRET
docker compose --profile prod up -d --build  # 3. Deploy!

# Access application
open http://localhost        # UI + API on single port
```

**Features:**
- ✅ Single-command deployment
- ✅ H2 database in dedicated container (TCP mode)
- ✅ Backend serves frontend (Mode A architecture)
- ✅ Persistent data with Docker volumes
- ✅ Health checks and auto-restart
- ✅ Production and development profiles
- ✅ Non-root containers for security

**Documentation:**
- Quick reference: [DOCKER_QUICKREF.md](DOCKER_QUICKREF.md)
- Full guide: [DOCKER_DEPLOYMENT.md](DOCKER_DEPLOYMENT.md)
- Implementation: [DOCKER_SUMMARY.md](DOCKER_SUMMARY.md)

## 🏗️ Architecture

The project consists of two main components:

### Backend (`bank_api/`)
- **Framework**: FastAPI (Python 3.11+)
- **Database**: H2 file-based SQL database (via JDBC)
- **Architecture**: Clean layered design
  - API Layer (controllers/routers)
  - Service Layer (business logic)
  - Domain Layer (entities/value objects)
  - Repository Layer (data access)
  - Infrastructure Layer (DB, security, logging)
- **Authentication**: JWT with bcrypt password hashing
- **Documentation**: Auto-generated OpenAPI/Swagger at `/docs`

### Frontend (`ui/`)
- **Framework**: React 18 + TypeScript
- **Build Tool**: Vite
- **Routing**: React Router
- **State**: Simple auth store with localStorage
- **Pages**: Login, Register, Dashboard, Accounts, Transfer

## 🚀 Quick Start

### Option 1: Docker Compose (Recommended for Production)

**Prerequisites**: Docker and Docker Compose installed

1. **Setup environment**:
```bash
# Copy environment template
cp .env.example .env

# Edit .env and set required values:
# - JWT_SECRET (generate with: python -c "import secrets; print(secrets.token_urlsafe(32))")
# - ADMIN_EMAIL (optional)
# - ADMIN_PASSWORD (optional)
nano .env
```

2. **Run with Docker Compose**:

**Production Profile** (recommended):
```bash
# Build and start all services
docker compose --profile prod up -d --build

# View logs
docker compose logs -f backend

# Check health
curl http://localhost/health

# Stop services
docker compose --profile prod down
```

**Development Profile** (with H2 web console):
```bash
# Start with dev profile
docker compose --profile dev up -d --build

# H2 web console available at http://localhost:8082
# Backend available at http://localhost/
# Frontend served by backend
```

3. **Access the application**:
   - **UI**: http://localhost
   - **API Docs**: http://localhost/docs
   - **Health Check**: http://localhost/health
   - **H2 Console** (dev only): http://localhost:8082
     - JDBC URL: `jdbc:h2:tcp://h2:9092/bank`
     - Username: `sa`
     - Password: (empty)

4. **Database Persistence**:
   - Data persists in Docker volume `h2-data`
   - View volumes: `docker volume ls`
   - Backup data: `docker run --rm -v h2-data:/data -v $(pwd):/backup alpine tar czf /backup/h2-backup.tar.gz -C /data .`
   - Restore data: `docker run --rm -v h2-data:/data -v $(pwd):/backup alpine tar xzf /backup/h2-backup.tar.gz -C /data`

5. **Management Commands**:
```bash
# View running containers
docker compose ps

# View logs for all services
docker compose logs -f

# View backend logs only
docker compose logs -f backend

# Restart services
docker compose --profile prod restart

# Rebuild after code changes
docker compose --profile prod up -d --build

# Remove all containers and volumes (⚠️ deletes database)
docker compose --profile prod down -v
```

**Architecture (Mode A - Single Entrypoint)**:
- ✅ H2 Database runs in dedicated container (TCP server mode)
- ✅ Backend container includes built frontend static files
- ✅ Backend serves both API and UI on port 80
- ✅ All services connected via private `bank-net` network
- ✅ Database persists in Docker volume
- ✅ Non-root runtime for security
- ✅ Health checks and auto-restart enabled

### Option 2: Local Development (Without Docker)

### Prerequisites

1. **Python 3.11+**
2. **Node.js 18+** and npm
3. **Java Runtime Environment** (for H2 database)
4. **H2 Database JAR**

Download H2 JAR (place in project root):
```bash
curl -o h2.jar https://repo1.maven.org/maven2/com/h2database/h2/2.2.224/h2-2.2.224.jar
```

### Backend Setup

```bash
# Navigate to backend
cd bank_api

# Create virtual environment
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate

# Install dependencies
pip install -r requirements.txt

# Configure environment
cp .env.example .env
# Edit .env and set JWT_SECRET and H2_JAR_PATH

# Run the API
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

API available at:
- **Base URL**: http://localhost:8000
- **Swagger UI**: http://localhost:8000/docs
- **Health Check**: http://localhost:8000/health

### Frontend Setup

```bash
# Navigate to UI (new terminal)
cd ui

# Install dependencies
npm install

# Configure environment
cp .env.example .env

# Run dev server
npm run dev
```

UI available at: http://localhost:5173

## 📚 API Documentation

### Authentication Endpoints

#### Register User
```bash
POST /v1/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123",
  "full_name": "John Doe"
}
```

#### Login
```bash
POST /v1/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}

Response:
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "bearer"
}
```

#### Get Current User
```bash
GET /v1/auth/me
Authorization: Bearer {token}
```

### Account Endpoints

#### Create Account
```bash
POST /v1/accounts
Authorization: Bearer {token}
Content-Type: application/json

{
  "currency": "EUR",
  "initial_balance": "1000.00",
  "card_number": "1111 2222 3333 4444"  // optional, auto-generated if omitted
}
```

#### List User Accounts
```bash
GET /v1/accounts
Authorization: Bearer {token}
```

#### Get Account Details
```bash
GET /v1/accounts/{account_id}
Authorization: Bearer {token}
```

#### Get Account Transactions
```bash
GET /v1/accounts/{account_id}/transactions?limit=20&offset=0
Authorization: Bearer {token}
```

### Transfer Endpoints

#### Execute Transfer
```bash
POST /v1/transfers
Authorization: Bearer {token}
Content-Type: application/json

{
  "source_card_number": "1111 2222 3333 4444",
  "target_card_number": "5555 6666 7777 8888",
  "source_currency": "EUR",
  "source_amount": "100.00",
  "target_currency": "USD",
  "fx_rate": "1.1",
  "description": "Payment for services"  // optional
}

Response:
{
  "transaction_id": "uuid",
  "status": "COMPLETED",
  "source_account": { "balance": "900.00", ... },
  "target_account": { "balance": "610.00", ... }
}
```

## 🧪 Testing

### Run Backend Tests

```bash
cd bank_api
pytest tests/ -v
```

Test coverage includes:
- User registration and login
- Account creation
- Successful transfers
- Insufficient funds handling
- Invalid account handling
- Transaction history pagination

### Manual UI Testing Flow

1. Open http://localhost:5173
2. Register new user
3. Create two accounts (EUR and USD)
4. Execute transfer between accounts
5. Verify balances updated
6. View transaction history
7. Test pagination
8. Logout and login again

## 💾 Database

**H2 Database** (file-based SQL):
- **Location**: `./bank.db` (creates `bank.db.mv.db` file)
- **Mode**: PostgreSQL compatibility
- **Tables**:
  - `users` - User accounts with hashed passwords
  - `accounts` - Bank accounts/cards with balances
  - `transactions` - All financial transactions
  - `idempotency_keys` - Idempotency tracking
- **Migrations**: Auto-run on application startup

## 🔒 Security Features

- **Password Hashing**: Bcrypt with salt
- **JWT Tokens**: Short-lived (15 min) access tokens
- **Input Validation**: Pydantic models validate all inputs
- **SQL Injection Protection**: Parameterized queries
- **CORS**: Configurable allowed origins
- **Atomicity**: Database transactions with row-level locking
- **Money Precision**: Decimal-based calculations (no floating-point)

## 📁 Project Structure

```
homework-1/
├── bank_api/                   # Backend API
│   ├── app/
│   │   ├── main.py            # FastAPI application
│   │   ├── api/               # API layer (controllers)
│   │   │   ├── dependencies.py
│   │   │   ├── schemas.py     # Pydantic models
│   │   │   └── v1/            # API v1 routes
│   │   ├── services/          # Business logic layer
│   │   ├── domain/            # Domain models
│   │   ├── repositories/      # Data access layer
│   │   └── infrastructure/    # DB, security, logging
│   ├── tests/                 # Test suite
│   ├── requirements.txt
│   └── README.md
├── ui/                        # Frontend UI
│   ├── src/
│   │   ├── api/              # API client
│   │   ├── auth/             # Auth state management
│   │   ├── components/       # Reusable components
│   │   ├── routes/           # Page components
│   │   ├── app.tsx           # Router configuration
│   │   └── main.tsx          # Entry point
│   ├── package.json
│   └── README.md
├── h2.jar                     # H2 database JAR (download separately)
└── README.md                  # This file
```

## 🎯 Implementation Highlights

### Clean Architecture
- Clear separation of concerns across layers
- No business logic in controllers
- No SQL in services
- Repository pattern for data access
- Dependency injection via FastAPI

### Type Safety
- Full Python type hints
- Pydantic models for validation
- TypeScript throughout frontend
- Compile-time type checking

### Production-Ready
- Structured JSON logging with request IDs
- Comprehensive error handling
- HTTP status codes follow standards
- Health check endpoint
- CORS configuration
- Environment-based configuration

### Money Handling
- Decimal type for all amounts (no float)
- Proper quantization to 4 decimal places
- Safe currency conversion calculations

### Concurrency Safety
- Database transactions for transfers
- Row-level locking (SELECT FOR UPDATE)
- ACID compliance
- Idempotency key support

## 🚀 Deployment

### Docker Compose Deployment (Production)

The application is fully containerized and production-ready:

**Architecture**:
- **H2 Database**: Runs in dedicated container with TCP server mode
- **Backend + Frontend**: Single container serving both API and UI
- **Networking**: Private bridge network, only port 80 exposed
- **Persistence**: Database files stored in Docker volume
- **Security**: Non-root containers, health checks, restart policies

**Production Deployment Steps**:

1. **Set up environment**:
```bash
# Create .env from template
cp .env.example .env

# Generate secure JWT secret
python -c "import secrets; print(secrets.token_urlsafe(32))"

# Edit .env and set:
# - JWT_SECRET=<generated secret>
# - ADMIN_EMAIL=admin@yourdomain.com
# - ADMIN_PASSWORD=<strong password>
# - CORS_ORIGINS=https://yourdomain.com
```

2. **Deploy**:
```bash
# Build and start (production profile)
docker compose --profile prod up -d --build

# Verify health
curl http://localhost/health

# View logs
docker compose logs -f backend
```

3. **SSL/TLS** (recommended):
   - Use a reverse proxy (nginx, Caddy, Traefik) in front
   - Or modify docker-compose.yml to include TLS certificates

4. **Monitoring**:
```bash
# Check container status
docker compose ps

# View resource usage
docker stats

# Check database volume
docker volume inspect homework-1_h2-data
```

**Deployment Variants**:

1. **Separate Deployment**: Deploy backend on server (Railway, Render) and frontend on CDN (Netlify, Vercel)
2. **Unified Deployment**: Use Docker Compose as shown (backend serves frontend)
3. **Kubernetes**: Convert compose to K8s manifests for orchestration

**Quick deployment options:**
1. **Unified (Recommended)**: Single Docker Compose deployment
2. **Separate**: Backend on Python host (Heroku, Railway), Frontend on Netlify/Vercel

## 📖 Additional Resources

- **Backend README**: [bank_api/README.md](bank_api/README.md)
- **Frontend README**: [ui/README.md](ui/README.md)
- **Swagger Docs**: http://localhost:8000/docs (when running)

## 🛠️ Troubleshooting

### H2 Database Issues
```bash
# Verify Java is installed
java -version

# Check H2 JAR path in bank_api/.env
H2_JAR_PATH=./h2.jar
```

### CORS Issues
- Verify `CORS_ORIGINS` in backend `.env` includes `http://localhost:5173`
- Check browser console for specific CORS errors

### Authentication Issues
- Ensure `JWT_SECRET` is set in backend `.env`
- Check token expiry (default 15 minutes)
- Verify Authorization header format: `Bearer {token}`

## 📝 License

MIT License

---

<div align="center">

*This project was completed as part of the AI-Assisted Development course.*

**Tech Stack**: Python • FastAPI • H2 Database • React • TypeScript • Vite

</div>
