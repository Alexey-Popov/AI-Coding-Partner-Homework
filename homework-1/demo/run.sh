#!/bin/bash

# Script to start the Banking Transactions API

echo "🏦 Starting Banking Transactions API..."
echo ""

# Check if running in virtual environment
if [ -z "$VIRTUAL_ENV" ]; then
    echo "⚠️  No virtual environment detected."
    echo "💡 Consider creating one: python3 -m venv venv && source venv/bin/activate"
    echo ""
fi

# Install dependencies
echo "📦 Installing dependencies..."
pip install -r requirements.txt > /dev/null 2>&1

# Start the API server
echo "✅ Starting server on http://localhost:8000"
echo ""
echo "📚 API Documentation available at:"
echo "   - Swagger UI: http://localhost:8000/docs"
echo "   - ReDoc: http://localhost:8000/redoc"
echo ""
echo "Press Ctrl+C to stop the server"
echo ""

# Run the FastAPI app using uvicorn
python3 -m uvicorn src.main:app --host 0.0.0.0 --port 8000 --reload
