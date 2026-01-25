#!/bin/bash

# Sample API Requests for Banking Transactions API
# These are curl commands to test the API

API_URL="http://localhost:3000"

echo "🏦 Banking Transactions API - Sample Requests"
echo "=============================================="
echo ""

# 1. Health check
echo "1️⃣  Health Check"
echo "Command: curl $API_URL/health"
curl -X GET "$API_URL/health" -H "Content-Type: application/json"
echo -e "\n\n"

# 2. Create a transfer transaction
echo "2️⃣  Create a Transfer Transaction"
echo "Command: curl -X POST $API_URL/transactions -d '{...}'"
curl -X POST "$API_URL/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "toAccount": "ACC-67890",
    "amount": 150.50,
    "currency": "USD",
    "type": "transfer"
  }'
echo -e "\n\n"

# 3. Create a deposit transaction
echo "3️⃣  Create a Deposit Transaction"
echo "Command: curl -X POST $API_URL/transactions -d '{...}'"
curl -X POST "$API_URL/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-BANK",
    "toAccount": "ACC-12345",
    "amount": 500,
    "currency": "USD",
    "type": "deposit"
  }'
echo -e "\n\n"

# 4. Create a withdrawal transaction
echo "4️⃣  Create a Withdrawal Transaction"
echo "Command: curl -X POST $API_URL/transactions -d '{...}'"
curl -X POST "$API_URL/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "toAccount": "ACC-BANK",
    "amount": 50.75,
    "currency": "USD",
    "type": "withdrawal"
  }'
echo -e "\n\n"

# 5. Get all transactions
echo "5️⃣  Get All Transactions"
echo "Command: curl $API_URL/transactions"
curl -X GET "$API_URL/transactions" -H "Content-Type: application/json"
echo -e "\n\n"

# 6. Get transactions for specific account
echo "6️⃣  Get Transactions for Specific Account"
echo "Command: curl '$API_URL/transactions?accountId=ACC-12345'"
curl -X GET "$API_URL/transactions?accountId=ACC-12345" -H "Content-Type: application/json"
echo -e "\n\n"

# 7. Get transactions by type
echo "7️⃣  Get Transactions by Type (transfer)"
echo "Command: curl '$API_URL/transactions?type=transfer'"
curl -X GET "$API_URL/transactions?type=transfer" -H "Content-Type: application/json"
echo -e "\n\n"

# 8. Get account balance
echo "8️⃣  Get Account Balance"
echo "Command: curl $API_URL/accounts/ACC-12345/balance"
curl -X GET "$API_URL/accounts/ACC-12345/balance" -H "Content-Type: application/json"
echo -e "\n\n"

# 9. Get account summary
echo "9️⃣  Get Account Summary"
echo "Command: curl $API_URL/accounts/ACC-12345/summary"
curl -X GET "$API_URL/accounts/ACC-12345/summary" -H "Content-Type: application/json"
echo -e "\n\n"

# 10. Export transactions as CSV
echo "🔟 Export Transactions as CSV"
echo "Command: curl '$API_URL/transactions/export?format=csv'"
curl -X GET "$API_URL/transactions/export?format=csv" -H "Content-Type: application/json"
echo -e "\n\n"

# 11. Validation error example
echo "1️⃣1️⃣  Validation Error Example (Invalid Amount)"
echo "Command: curl -X POST $API_URL/transactions -d '{...}'"
curl -X POST "$API_URL/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "toAccount": "ACC-67890",
    "amount": -100,
    "currency": "USD",
    "type": "transfer"
  }'
echo -e "\n\n"

# 12. Account format validation error
echo "1️⃣2️⃣  Validation Error Example (Invalid Account Format)"
echo "Command: curl -X POST $API_URL/transactions -d '{...}'"
curl -X POST "$API_URL/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "INVALID",
    "toAccount": "ACC-67890",
    "amount": 100,
    "currency": "USD",
    "type": "transfer"
  }'
echo -e "\n\n"

echo "✅ Sample requests completed!"
