#!/bin/bash

# Banking Application Start Script

set -e

echo "======================================"
echo "Starting Banking Application"
echo "======================================"
echo ""

# Function to cleanup on exit
cleanup() {
    echo ""
    echo "Shutting down..."
    kill $(jobs -p) 2>/dev/null
    exit 0
}

trap cleanup SIGINT SIGTERM

# Start backend
echo "Starting backend on port 8000..."
cd bank_api
source venv/bin/activate
uvicorn app.main:app --host 0.0.0.0 --port 8000 &
BACKEND_PID=$!
cd ..

# Wait for backend to start
echo "Waiting for backend to start..."
sleep 5

# Start frontend
echo "Starting frontend on port 5173..."
cd ui
npm run dev &
FRONTEND_PID=$!
cd ..

echo ""
echo "======================================"
echo "Application Started! 🚀"
echo "======================================"
echo ""
echo "Backend API:  http://localhost:8000"
echo "Swagger Docs: http://localhost:8000/docs"
echo "Frontend UI:  http://localhost:5173"
echo ""
echo "Press Ctrl+C to stop both services"
echo ""

# Wait for processes
wait $BACKEND_PID $FRONTEND_PID
