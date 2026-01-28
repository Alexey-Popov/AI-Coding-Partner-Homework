#!/bin/bash

# Banking Transactions API - Sample Requests
# Run: chmod +x demo/requests.sh && ./demo/requests.sh

BASE_URL="http://localhost:3000"
ACCOUNT_ID="ACC-00001"
ACCOUNT_ID_2="ACC-00002"

echo "=========================================="
echo "Banking Transactions API - Test Script"
echo "=========================================="
echo ""

# --------------------------------------------
# TRANSACTIONS
# --------------------------------------------

echo "1. Creating a deposit of \$1000..."
curl -s -X POST "$BASE_URL/transactions" \
  -H "Content-Type: application/json" \
  -d "{\"toAccount\":\"$ACCOUNT_ID\",\"amount\":1000,\"currency\":\"USD\",\"type\":\"deposit\"}" | jq .
echo ""

echo "2. Creating another deposit of \$500..."
curl -s -X POST "$BASE_URL/transactions" \
  -H "Content-Type: application/json" \
  -d "{\"toAccount\":\"$ACCOUNT_ID\",\"amount\":500,\"currency\":\"USD\",\"type\":\"deposit\"}" | jq .
echo ""

echo "3. Creating a withdrawal of \$200..."
curl -s -X POST "$BASE_URL/transactions" \
  -H "Content-Type: application/json" \
  -d "{\"fromAccount\":\"$ACCOUNT_ID\",\"amount\":200,\"currency\":\"USD\",\"type\":\"withdrawal\"}" | jq .
echo ""

echo "4. Creating a transfer of \$150 to $ACCOUNT_ID_2..."
curl -s -X POST "$BASE_URL/transactions" \
  -H "Content-Type: application/json" \
  -d "{\"fromAccount\":\"$ACCOUNT_ID\",\"toAccount\":\"$ACCOUNT_ID_2\",\"amount\":150,\"currency\":\"USD\",\"type\":\"transfer\"}" | jq .
echo ""

echo "5. Getting all transactions..."
curl -s "$BASE_URL/transactions" | jq .
echo ""

echo "6. Getting transactions for account $ACCOUNT_ID..."
curl -s "$BASE_URL/transactions?accountId=$ACCOUNT_ID" | jq .
echo ""

echo "7. Exporting transactions as CSV..."
curl -s "$BASE_URL/transactions/export?format=csv"
echo ""
echo ""

# --------------------------------------------
# ACCOUNTS
# --------------------------------------------

echo "8. Getting account balance..."
curl -s "$BASE_URL/accounts/$ACCOUNT_ID/balance" | jq .
echo ""

echo "9. Getting account summary..."
curl -s "$BASE_URL/accounts/$ACCOUNT_ID/summary" | jq .
echo ""

echo "10. Calculating interest (5% rate, 30 days)..."
curl -s "$BASE_URL/accounts/$ACCOUNT_ID/interest?rate=0.05&days=30" | jq .
echo ""

# --------------------------------------------
# ERROR CASES
# --------------------------------------------

echo "11. Testing error: Account not found..."
curl -s "$BASE_URL/accounts/NONEXISTENT/summary" | jq .
echo ""

echo "12. Testing error: Invalid transaction..."
curl -s -X POST "$BASE_URL/transactions" \
  -H "Content-Type: application/json" \
  -d "{\"amount\":100}" | jq .
echo ""

echo "=========================================="
echo "All tests completed!"
echo "=========================================="
