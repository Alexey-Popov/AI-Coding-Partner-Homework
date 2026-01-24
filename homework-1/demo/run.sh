#!/bin/bash

echo "🏦 Banking Transactions API - Starting Server"
echo "=============================================="
echo ""

# Navigate to the homework-1 directory if not already there
cd "$(dirname "$0")/.."

# Check if node_modules exists
if [ ! -d "node_modules" ]; then
    echo "📦 Installing dependencies..."
    npm install
    echo ""
fi

echo "🚀 Starting the server..."
echo ""

npm start
