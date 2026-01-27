#!/bin/bash

# Banking Transactions API - Start Script
# This script starts the Node.js server

echo "========================================"
echo "  Banking Transactions API - Startup"
echo "========================================"
echo ""

# Check if Node.js is installed
if ! command -v node &> /dev/null
then
    echo "❌ Error: Node.js is not installed."
    echo "Please install Node.js from https://nodejs.org/"
    exit 1
fi

echo "✅ Node.js version: $(node --version)"
echo "✅ npm version: $(npm --version)"
echo ""

# Check if we're in the correct directory
if [ ! -f "package.json" ]; then
    echo "⚠️  Warning: package.json not found."
    echo "Make sure you're running this script from the homework-1 directory."
    echo ""
    read -p "Continue anyway? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]
    then
        exit 1
    fi
fi

# Check if node_modules exists
if [ ! -d "node_modules" ]; then
    echo "📦 Installing dependencies..."
    npm install
    if [ $? -ne 0 ]; then
        echo "❌ Failed to install dependencies"
        exit 1
    fi
    echo ""
fi

# Start the server
echo "🚀 Starting the server..."
echo "Press Ctrl+C to stop"
echo ""

npm start
