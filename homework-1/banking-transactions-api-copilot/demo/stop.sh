#!/bin/bash

# Banking Transactions API - Stop Script

echo "Stopping Banking Transactions API (Copilot Module)..."

# Find the process running on port 3000
PID=$(lsof -ti:3000)

if [ -z "$PID" ]; then
    echo "No process found running on port 3000."
    exit 0
fi

echo "Found process(es) with PID: $PID"
echo "Terminating process(es)..."

# Kill the process
kill -9 $PID

if [ $? -eq 0 ]; then
    echo "Successfully stopped the Banking Transactions API."
else
    echo "Failed to stop the process. You may need to run this script with sudo."
    exit 1
fi

