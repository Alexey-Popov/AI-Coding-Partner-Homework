#!/bin/bash

# ============================================
# Banking Transactions API - Startup Script
# ============================================

echo "Starting Banking Transactions API..."
echo ""

# Navigate to project root (parent of demo directory)
cd "$(dirname "$0")/.." || exit 1

# Check if Maven is installed
if ! command -v mvn &> /dev/null
then
    echo "Maven is not installed. Please install Maven first."
    echo "   Visit: https://maven.apache.org/install.html"
    exit 1
fi

# Check if Java is installed
if ! command -v java &> /dev/null
then
    echo "Java is not installed. Please install Java 21 or higher."
    echo "   Visit: https://adoptium.net/"
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 21 ]; then
    echo "Java 21 or higher is required. Current version: $JAVA_VERSION"
    exit 1
fi

echo "Maven found: $(mvn -version | head -n 1)"
echo "Java found: $(java -version 2>&1 | head -n 1)"
echo ""

# Clean and build the project
echo "Building the project..."
mvn clean compile dependency:copy-dependencies -DoutputDirectory=target/dependency -DincludeScope=runtime -q

if [ $? -eq 0 ]; then
    echo ""
    echo "Build successful!"
    echo ""
    echo "Starting the application on http://localhost:3000/api/v1"
    echo "Health check: http://localhost:3000/api/v1/actuator/health"
    echo ""
    echo "Once started, you can test the API using:"
    echo "   - demo/sample-requests.http (VS Code REST Client)"
    echo "   - demo/sample-requests.sh (curl commands)"
    echo ""
    echo "Press Ctrl+C to stop the application"
    echo ""

    # Run the application directly with Java
    java -cp "target/classes:target/dependency/*" com.banking.api.BankingApiApplication
else
    echo ""
    echo "Build failed. Please check the errors above."
    exit 1
fi
