#!/bin/bash

# Banking Transactions API - Run Script
# This script installs dependencies and starts the API server

echo "=========================================="
echo "  Banking Transactions API"
echo "=========================================="
echo ""

# Navigate to homework-1 directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/.."

# Check if Node.js is installed
if ! command -v node &> /dev/null; then
    echo "Error: Node.js is not installed."
    echo "Please install Node.js from https://nodejs.org/"
    exit 1
fi

echo "Node.js version: $(node --version)"
echo "npm version: $(npm --version)"
echo ""

# Install dependencies if node_modules doesn't exist
if [ ! -d "node_modules" ]; then
    echo "Installing dependencies..."
    npm install
    echo ""
fi

# Start the server
echo "Starting the API server..."
echo "API will be available at http://localhost:3000"
echo ""
echo "Press Ctrl+C to stop the server"
echo "=========================================="
echo ""

npm start
