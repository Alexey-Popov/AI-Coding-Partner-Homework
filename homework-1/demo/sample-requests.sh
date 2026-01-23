#!/bin/bash
# Sample cURL requests for Banking Transaction API

BASE_URL="http://localhost:3000"

echo "🏦 Banking Transaction API - Sample Requests"
echo "=============================================="
echo ""

# Health Check
echo "1️⃣  Testing Health Check..."
curl -X GET "$BASE_URL/health" -H "Content-Type: application/json"
echo -e "\n"

# Create transactions
echo "2️⃣  Creating a deposit transaction..."
curl -X POST "$BASE_URL/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "toAccount": "ACC-12345",
    "amount": 1000.50,
    "currency": "USD",
    "type": "deposit"
  }'
echo -e "\n\n"

echo "3️⃣  Creating a withdrawal transaction..."
curl -X POST "$BASE_URL/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "amount": 250.00,
    "currency": "USD",
    "type": "withdrawal"
  }'
echo -e "\n\n"

echo "4️⃣  Creating a transfer transaction..."
curl -X POST "$BASE_URL/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "toAccount": "ACC-67890",
    "amount": 500.75,
    "currency": "EUR",
    "type": "transfer"
  }'
echo -e "\n\n"

# List transactions
echo "5️⃣  Listing all transactions..."
curl -X GET "$BASE_URL/transactions" -H "Content-Type: application/json"
echo -e "\n\n"

# Get account balance
echo "6️⃣  Getting account balance..."
curl -X GET "$BASE_URL/accounts/ACC-12345/balance" -H "Content-Type: application/json"
echo -e "\n\n"

# Get summary
echo "7️⃣  Getting transaction summary..."
curl -X GET "$BASE_URL/summary" -H "Content-Type: application/json"
echo -e "\n\n"

# Filter transactions by account
echo "8️⃣  Filtering transactions by account..."
curl -X GET "$BASE_URL/transactions?accountId=ACC-12345" -H "Content-Type: application/json"
echo -e "\n"
