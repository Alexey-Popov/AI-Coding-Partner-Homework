#!/bin/bash

# Banking Transactions API - Run Script

echo "Starting Banking Transactions API (Copilot Module)..."
echo "Building the application..."

# Navigate to the root project directory
cd "$(dirname "$0")/../.."

# Build the application
./gradlew :banking-transactions-api-copilot:clean :banking-transactions-api-copilot:build -x test

if [ $? -ne 0 ]; then
    echo "Build failed. Exiting."
    exit 1
fi

# Run the application
echo "Starting the server on port 3000..."
echo "Press Ctrl+C to stop the server"
./gradlew :banking-transactions-api-copilot:bootRun
