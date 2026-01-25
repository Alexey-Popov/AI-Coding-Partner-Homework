#!/bin/bash

# Banking Transactions API - Python/FastAPI Startup Script

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PYTHON_DIR="$(dirname "$SCRIPT_DIR")"

cd "$PYTHON_DIR"

echo "==================================="
echo "Banking Transactions API (Python)"
echo "==================================="

# Check if Python is installed
if ! command -v python3 &> /dev/null; then
    echo "Python 3 is not installed. Please install Python 3 first."
    exit 1
fi

# Check if virtual environment exists
if [ ! -d "venv" ]; then
    echo "Creating virtual environment..."
    python3 -m venv venv
fi

# Activate virtual environment
echo "Activating virtual environment..."
source venv/bin/activate

# Install dependencies
echo "Installing dependencies..."
pip install -r requirements.txt --quiet

# Start the server
echo ""
echo "Starting server on http://localhost:3000"
echo "API Documentation: http://localhost:3000/docs"
echo "Press Ctrl+C to stop the server"
echo ""

uvicorn src.main:app --host 0.0.0.0 --port 3000 --reload
