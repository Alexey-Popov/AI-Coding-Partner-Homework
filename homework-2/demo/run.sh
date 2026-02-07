#!/bin/bash
# Run Customer Support Ticket System API Server (Linux/Mac)

# Change to parent directory so relative paths resolve
cd "$(dirname "$0")/.."

echo "Installing dependencies (using python -m pip)..."
python -m pip install -r requirements.txt

echo ""
echo "Starting server on http://localhost:8000"
echo "API docs available at http://localhost:8000/docs"
echo ""

REM_CMD="python -m uvicorn src.main:app --reload --host 0.0.0.0 --port 8000"
eval $REM_CMD
