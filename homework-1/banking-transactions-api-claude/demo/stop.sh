#!/bin/bash

# Banking Transactions API - Stop Script
# This script stops the running Banking Transactions API

echo "🛑 Stopping Banking Transactions API..."

# Find all processes using port 8080
PIDS=$(lsof -ti :8080 2>/dev/null)

if [ -z "$PIDS" ]; then
    echo "✅ No application is running on port 8080"
else
    echo "Found processes on port 8080: $PIDS"

    # Kill the processes
    for PID in $PIDS; do
        echo "Stopping process $PID..."
        kill -15 $PID 2>/dev/null || kill -9 $PID 2>/dev/null
    done

    # Wait a moment for processes to stop
    sleep 2

    # Verify they're stopped
    REMAINING=$(lsof -ti :8080 2>/dev/null)
    if [ -z "$REMAINING" ]; then
        echo "✅ Successfully stopped all processes on port 8080"
    else
        echo "⚠️  Some processes are still running. Forcing kill..."
        kill -9 $REMAINING 2>/dev/null
        echo "✅ Forced stop complete"
    fi
fi

# Also stop any gradle daemon processes related to banking
echo ""
echo "Checking for gradle daemons..."
GRADLE_PIDS=$(ps aux | grep -i "banking.*bootRun" | grep -v grep | awk '{print $2}')

if [ -n "$GRADLE_PIDS" ]; then
    echo "Found gradle processes: $GRADLE_PIDS"
    kill -9 $GRADLE_PIDS 2>/dev/null || true
    echo "✅ Stopped gradle processes"
else
    echo "✅ No gradle processes found"
fi

echo ""
echo "🎉 Cleanup complete!"

