#!/bin/bash

# Banking Transactions API - Node.js/Express Run Script

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
NODEJS_DIR="$(dirname "$SCRIPT_DIR")"

cd "$NODEJS_DIR"

echo "==================================="
echo "Banking Transactions API (Node.js)"
echo "==================================="

# Check if Node.js is installed
if ! command -v node &> /dev/null; then
    echo "Node.js is not installed. Please install Node.js first."
    exit 1
fi

# Install dependencies if node_modules doesn't exist
if [ ! -d "node_modules" ]; then
    echo "Installing dependencies..."
    npm install
fi

# Start the API
echo ""
echo "Starting server on http://localhost:3000"
echo "Press Ctrl+C to stop the server"
echo ""
npm start
