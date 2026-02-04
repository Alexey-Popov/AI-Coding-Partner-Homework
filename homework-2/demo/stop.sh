#!/bin/bash

# Customer Support Ticket System - Stop Script
# This script stops the running Spring Boot application on port 8080

set -e

echo "🔍 Looking for processes on port 8080..."

PIDS=$(lsof -ti :8080 2>/dev/null || true)

if [ -z "$PIDS" ]; then
    echo "✅ No application running on port 8080."
    exit 0
fi

echo "Found process(es) on port 8080:"
lsof -i :8080
echo ""

echo "Stopping process(es)..."
kill -9 $PIDS 2>/dev/null || true

echo "✅ Application stopped."
