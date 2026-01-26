# Quick Start Guide

Get the banking application running in 5 minutes.

## Prerequisites

- Python 3.11+
- Node.js 18+
- Java Runtime Environment

## Option 1: Automated Setup (Recommended)

```bash
# Run the setup script
./setup.sh

# Start both backend and frontend
./start.sh
```

Then open http://localhost:5173 in your browser.

## Option 2: Manual Setup

### Download H2 Database

```bash
curl -o h2.jar https://repo1.maven.org/maven2/com/h2database/h2/2.2.224/h2-2.2.224.jar
```

### Backend Setup

```bash
cd bank_api

# Create virtual environment
python3 -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate

# Install dependencies
pip install -r requirements.txt

# Configure
cp .env.example .env
# Edit .env if needed (default settings work for local dev)

# Start API
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

Backend runs at http://localhost:8000
Swagger docs at http://localhost:8000/docs

### Frontend Setup (New Terminal)

```bash
cd ui

# Install dependencies
npm install

# Configure
cp .env.example .env

# Start dev server
npm run dev
```

Frontend runs at http://localhost:5173

## Option 3: Docker (Optional)

```bash
# Start backend only
docker-compose up

# Backend will be at http://localhost:8000
# Then run frontend separately as above
```

## First Steps

1. **Open http://localhost:5173**
2. **Register** a new account
3. **Create** two accounts (e.g., EUR and USD)
4. **Transfer** money between them
5. **View** transaction history

## Example API Usage

```bash
# Register
curl -X POST http://localhost:8000/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com", "password": "password123"}'

# Login
curl -X POST http://localhost:8000/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com", "password": "password123"}'

# Use the access_token from login response
export TOKEN="your-token-here"

# Create account
curl -X POST http://localhost:8000/v1/accounts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"currency": "EUR", "initial_balance": "1000.00"}'

# List accounts
curl http://localhost:8000/v1/accounts \
  -H "Authorization: Bearer $TOKEN"
```

## Troubleshooting

### "java: command not found"
Install Java: https://www.java.com/download/

### "Cannot connect to API"
- Check backend is running on port 8000
- Check CORS settings in backend `.env`
- Verify frontend `.env` has correct API URL

### "H2 Database error"
- Ensure `h2.jar` is in project root
- Check `H2_JAR_PATH` in backend `.env`
- Verify Java is installed

### "Port already in use"
- Backend: Change port with `--port 8001`
- Frontend: Vite will auto-increment to 5174

## Next Steps

- Read full [README.md](README.md) for detailed documentation
- Check [bank_api/README.md](bank_api/README.md) for API details
- Check [ui/README.md](ui/README.md) for frontend details
- Explore Swagger docs at http://localhost:8000/docs
- Run tests: `cd bank_api && pytest tests/`

## Support

- API Documentation: http://localhost:8000/docs
- Health Check: http://localhost:8000/health
- Check logs in terminal for errors
