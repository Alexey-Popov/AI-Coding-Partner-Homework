#!/bin/bash

# Banking Transactions API - Test Script
# This script installs dependencies (if needed) and runs the test suite

echo "=========================================="
echo "  Banking Transactions API - Tests"
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

# Run test suite
echo "Running test suite..."

# Prefer project test script (npm test), which uses node --test per package.json
if npm run | grep -q " test"; then
  npm test
  EXIT_CODE=$?
else
  # Fallback to direct node --test invocation
  node --test tests/**/*.test.js
  EXIT_CODE=$?
fi

if [ $EXIT_CODE -eq 0 ]; then
  echo "\nAll tests passed."
else
  echo "\nSome tests failed (exit code: $EXIT_CODE)."
fi

exit $EXIT_CODE
