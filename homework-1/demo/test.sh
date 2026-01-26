#!/bin/bash

echo "🧪 Running Tests for Banking Transactions API..."
echo "================================================"

# Navigate to the project directory
cd "$(dirname "$0")/.."

# Run tests
echo "Running unit and integration tests..."
mvn clean test

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ All tests passed!"
else
    echo ""
    echo "❌ Some tests failed. Please check the output above."
    exit 1
fi
