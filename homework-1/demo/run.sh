#!/bin/bash

# Banking Transactions API - Run Script

echo "Starting Banking Transactions API..."
echo "Building the application..."

# Build the application
./gradlew clean build -x test

# Run the application
echo "Starting the server on port 3000..."
./gradlew bootRun
