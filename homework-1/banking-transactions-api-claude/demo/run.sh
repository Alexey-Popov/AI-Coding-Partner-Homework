#!/bin/bash

# Banking Transactions API - Run Script
# This script builds and runs the Spring Boot application

set -e

# Navigate to the root project directory where gradlew is located
cd "$(dirname "$0")/../.."

# Check if port 8080 is already in use
if lsof -Pi :8080 -sTCP:LISTEN -t >/dev/null 2>&1 ; then
    echo "⚠️  Port 8080 is already in use!"
    echo ""
    echo "Finding processes using port 8080..."
    PIDS=$(lsof -ti :8080)

    if [ -n "$PIDS" ]; then
        echo "The following process(es) are using port 8080:"
        lsof -i :8080
        echo ""
        read -p "Do you want to stop these processes? (y/n) " -n 1 -r
        echo ""
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            echo "Stopping processes on port 8080..."
            kill -9 $PIDS 2>/dev/null || true
            echo "✅ Processes stopped."
            sleep 2
        else
            echo "❌ Cannot start application while port 8080 is in use."
            echo "Please stop the existing application manually or use a different port."
            exit 1
        fi
    fi
fi

echo "🔨 Building the application..."
./gradlew :banking-transactions-api-claude:build -x test

echo ""
echo "🚀 Starting the Banking Transactions API..."
echo "📡 Server will be available at http://localhost:8080"
echo ""
echo "Press Ctrl+C to stop the server"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

./gradlew :banking-transactions-api-claude:bootRun
