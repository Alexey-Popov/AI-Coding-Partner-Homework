#!/bin/bash

echo "🚀 Building and Running Banking Transactions API..."
echo "=================================================="

# Navigate to the project directory
cd "$(dirname "$0")/.."

# Clean and build the project
echo "📦 Building the project..."
mvn clean install -DskipTests

if [ $? -eq 0 ]; then
    echo "✅ Build successful!"
    echo ""
    echo "🏃 Starting the application..."
    echo "The API will be available at: http://localhost:8080"
    echo "Press Ctrl+C to stop the application"
    echo ""
    mvn spring-boot:run
else
    echo "❌ Build failed! Please check the errors above."
    exit 1
fi
