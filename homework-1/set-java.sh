#!/bin/bash

# Script to set up Java environment for this project
# Usage: source set-java.sh
#
# Note: This project is configured to compile with Java 21 bytecode
# but can run on Java 25 or any later version.
#
# The project uses:
# - Maven compiler: release 21 (bytecode version)
# - Spring Boot: 3.4.2
# - Lombok: 1.18.42
#
# Your current Java version should work fine as long as it's >= 21.

echo "Checking Java environment..."
java -version

echo ""
echo "Project Configuration:"
echo "  - Bytecode target: Java 21"
echo "  - Spring Boot: 3.4.2"
echo "  - Compatible with: Java 21+"
echo ""
echo "To run the application:"
echo "  1. mvn clean compile dependency:copy-dependencies -DoutputDirectory=target/dependency -DincludeScope=runtime"
echo "  2. java -cp \"target/classes:target/dependency/*\" com.banking.api.BankingApiApplication"
echo ""
echo "Or use the demo/run.sh script."
