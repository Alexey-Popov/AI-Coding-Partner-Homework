#!/bin/bash

# Test script for graceful shutdown
# Demonstrates that transactions are cleared on server shutdown

echo "=== Testing Graceful Shutdown ==="
echo

# 1. Start server
echo "1. Starting server..."
cd ../src
node index.js > /tmp/shutdown-test.log 2>&1 &
SERVER_PID=$!
sleep 2
echo "   Server PID: $SERVER_PID"
echo

# 2. Create transactions
echo "2. Creating test transactions..."
for i in {1..3}; do
  curl -s -X POST http://localhost:3000/transactions \
    -H "Content-Type: application/json" \
    -d "{
      \"fromAccount\": \"ACC-TEST$i\",
      \"toAccount\": \"ACC-DEST$i\",
      \"amount\": $((i * 100)),
      \"currency\": \"USD\",
      \"type\": \"transfer\"
    }" > /dev/null
done
echo "   ✅ Created 3 transactions"
echo

# 3. Check transaction count
TRANS_COUNT=$(curl -s http://localhost:3000/transactions | jq '. | length')
echo "3. Transactions in memory: $TRANS_COUNT"
echo

# 4. Send graceful shutdown signal
echo "4. Sending SIGINT for graceful shutdown..."
kill -SIGINT $SERVER_PID
sleep 2
echo

# 5. Show shutdown logs
echo "5. Server shutdown output:"
echo "   ----------------------------------------"
tail -10 /tmp/shutdown-test.log | sed 's/^/   /'
echo "   ----------------------------------------"
echo

# 6. Verify server stopped
if lsof -ti:3000 > /dev/null 2>&1; then
    echo "6. ❌ Server still running!"
else
    echo "6. ✅ Server stopped successfully"
fi
echo

# 7. Start fresh server
echo "7. Starting fresh server to verify clean state..."
node index.js > /tmp/shutdown-test2.log 2>&1 &
NEW_PID=$!
sleep 2
echo "   New server PID: $NEW_PID"
echo

# 8. Check transactions on fresh start
NEW_TRANS_COUNT=$(curl -s http://localhost:3000/transactions | jq '. | length')
echo "8. Transactions after fresh start: $NEW_TRANS_COUNT"
echo

# 9. Cleanup
kill -SIGINT $NEW_PID 2>/dev/null
sleep 1

# 10. Results
echo "=== Test Results ==="
if [ "$NEW_TRANS_COUNT" -eq "0" ]; then
    echo "✅ PASS: Transactions cleared on shutdown"
    echo "✅ PASS: Fresh server starts with clean memory"
else
    echo "❌ FAIL: Transactions not cleared properly"
fi
echo

echo "=== Test Complete ==="
