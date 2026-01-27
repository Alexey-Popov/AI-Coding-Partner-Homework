#!/bin/bash

echo "🚀 Starting Banking Transactions API..."
echo

# Navigate to src directory
cd "$(dirname "$0")/../src" || exit 1

# Check if Node.js is installed
if ! command -v node &> /dev/null; then
    echo "❌ Error: Node.js is not installed"
    echo "Please install Node.js from https://nodejs.org/"
    exit 1
fi

# Check if dependencies are installed
if [ ! -d "node_modules" ]; then
    echo "📦 Installing dependencies..."
    npm install
    echo
fi

# Start the server
echo "✅ Dependencies ready"
echo "🌐 Starting server on port 3000..."
echo "📡 API will be available at http://localhost:3000"
echo
echo "💡 Server will show:"
echo "   • Transactions in memory (should be 0 on fresh start)"
echo "   • Server start time"
echo
echo "Press Ctrl+C to stop the server"
echo "---"
echo

# Run with nodemon for development (auto-restart on changes)
if [ -f "node_modules/.bin/nodemon" ]; then
    npm run dev
else
    # Fallback to regular node
    npm start
fi
