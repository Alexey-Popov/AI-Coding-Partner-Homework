#!/bin/bash

# Banking Transactions API - Sample Requests using curl
# Make sure the API is running on http://localhost:8080

BASE_URL="http://localhost:8080/api"

echo "========================================"
echo "Banking Transactions API - Sample Tests"
echo "========================================"
echo ""

# 1. Create a DEPOSIT transaction
echo "1. Creating DEPOSIT transaction..."
curl -X POST "$BASE_URL/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": null,
    "toAccount": "ACC-12345",
    "amount": 1000.00,
    "currency": "USD",
    "type": "DEPOSIT"
  }' | jq '.'
echo ""

# 2. Create a WITHDRAWAL transaction
echo "2. Creating WITHDRAWAL transaction..."
curl -X POST "$BASE_URL/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "toAccount": null,
    "amount": 150.50,
    "currency": "USD",
    "type": "WITHDRAWAL"
  }' | jq '.'
echo ""

# 3. Create a TRANSFER transaction
echo "3. Creating TRANSFER transaction..."
RESPONSE=$(curl -s -X POST "$BASE_URL/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "toAccount": "ACC-67890",
    "amount": 250.75,
    "currency": "USD",
    "type": "TRANSFER"
  }')
echo "$RESPONSE" | jq '.'
TRANSACTION_ID=$(echo "$RESPONSE" | jq -r '.id')
echo ""

# 4. Get all transactions
echo "4. Getting all transactions..."
curl -s "$BASE_URL/transactions" | jq '.'
echo ""

# 5. Get transaction by ID
if [ ! -z "$TRANSACTION_ID" ]; then
  echo "5. Getting transaction by ID: $TRANSACTION_ID"
  curl -s "$BASE_URL/transactions/$TRANSACTION_ID" | jq '.'
  echo ""
fi

# 6. Get transactions filtered by account
echo "6. Getting transactions for account ACC-12345..."
curl -s "$BASE_URL/transactions?accountId=ACC-12345" | jq '.'
echo ""

# 7. Get transactions filtered by type
echo "7. Getting DEPOSIT transactions..."
curl -s "$BASE_URL/transactions?type=DEPOSIT" | jq '.'
echo ""

# 8. Get account balance
echo "8. Getting balance for account ACC-12345..."
curl -s "$BASE_URL/accounts/ACC-12345/balance" | jq '.'
echo ""

# 9. Get account summary
echo "9. Getting summary for account ACC-12345..."
curl -s "$BASE_URL/accounts/ACC-12345/summary" | jq '.'
echo ""

# 10. Calculate interest
echo "10. Calculating interest (5% for 30 days)..."
curl -s "$BASE_URL/accounts/ACC-12345/interest?rate=0.05&days=30" | jq '.'
echo ""

echo "========================================"
echo "Testing Validation Errors"
echo "========================================"
echo ""

# 11. Invalid account format
echo "11. Testing invalid account format..."
curl -s -X POST "$BASE_URL/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "INVALID",
    "toAccount": "ACC-67890",
    "amount": 100.00,
    "currency": "USD",
    "type": "TRANSFER"
  }' | jq '.'
echo ""

# 12. Negative amount
echo "12. Testing negative amount..."
curl -s -X POST "$BASE_URL/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "toAccount": "ACC-67890",
    "amount": -100.00,
    "currency": "USD",
    "type": "TRANSFER"
  }' | jq '.'
echo ""

# 13. Invalid currency
echo "13. Testing invalid currency..."
curl -s -X POST "$BASE_URL/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "toAccount": "ACC-67890",
    "amount": 100.00,
    "currency": "XYZ",
    "type": "TRANSFER"
  }' | jq '.'
echo ""

# 14. Get non-existent transaction
echo "14. Testing non-existent transaction..."
curl -s "$BASE_URL/transactions/non-existent-id" | jq '.'
echo ""

echo "========================================"
echo "All tests completed!"
echo "========================================"
