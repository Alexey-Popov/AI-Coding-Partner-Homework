#!/bin/bash

# Banking Transactions API - Sample Requests (Python/FastAPI)
# Run these commands to test the API

BASE_URL="http://localhost:3000"

echo "==================================="
echo "Banking Transactions API - Testing"
echo "==================================="
echo ""

# 1. Health Check
echo "1. Health Check"
echo "---------------"
curl -s "$BASE_URL/health" | python3 -m json.tool
echo ""

# 2. Create a deposit transaction
echo "2. Create Deposit Transaction"
echo "-----------------------------"
curl -s -X POST "$BASE_URL/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "toAccount": "ACC-12345",
    "amount": 1000.00,
    "currency": "USD",
    "type": "deposit"
  }' | python3 -m json.tool
echo ""

# 3. Create another deposit
echo "3. Create Another Deposit"
echo "-------------------------"
curl -s -X POST "$BASE_URL/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "toAccount": "ACC-67890",
    "amount": 500.00,
    "currency": "EUR",
    "type": "deposit"
  }' | python3 -m json.tool
echo ""

# 4. Create a transfer transaction
echo "4. Create Transfer Transaction"
echo "------------------------------"
curl -s -X POST "$BASE_URL/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "toAccount": "ACC-67890",
    "amount": 150.50,
    "currency": "USD",
    "type": "transfer"
  }' | python3 -m json.tool
echo ""

# 5. Create a withdrawal
echo "5. Create Withdrawal Transaction"
echo "--------------------------------"
curl -s -X POST "$BASE_URL/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "amount": 100.00,
    "currency": "USD",
    "type": "withdrawal"
  }' | python3 -m json.tool
echo ""

# 6. Get all transactions
echo "6. Get All Transactions"
echo "-----------------------"
curl -s "$BASE_URL/transactions" | python3 -m json.tool
echo ""

# 7. Get transactions filtered by account
echo "7. Filter by Account ID"
echo "-----------------------"
curl -s "$BASE_URL/transactions?accountId=ACC-12345" | python3 -m json.tool
echo ""

# 8. Get transactions filtered by type
echo "8. Filter by Type (transfer)"
echo "----------------------------"
curl -s "$BASE_URL/transactions?type=transfer" | python3 -m json.tool
echo ""

# 9. Get account balance
echo "9. Get Account Balance"
echo "----------------------"
curl -s "$BASE_URL/accounts/ACC-12345/balance" | python3 -m json.tool
echo ""

# 10. Get account summary
echo "10. Get Account Summary"
echo "-----------------------"
curl -s "$BASE_URL/accounts/ACC-12345/summary" | python3 -m json.tool
echo ""

# 11. Calculate interest
echo "11. Calculate Interest (5% for 30 days)"
echo "----------------------------------------"
curl -s "$BASE_URL/accounts/ACC-12345/interest?rate=0.05&days=30" | python3 -m json.tool
echo ""

# 12. Export transactions as CSV
echo "12. Export Transactions as CSV"
echo "------------------------------"
curl -s "$BASE_URL/transactions/export?format=csv"
echo ""
echo ""

# 13. Test validation error (invalid account format)
echo "13. Test Validation Error (Invalid Account)"
echo "--------------------------------------------"
curl -s -X POST "$BASE_URL/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "toAccount": "INVALID",
    "amount": 100.00,
    "currency": "USD",
    "type": "deposit"
  }' | python3 -m json.tool
echo ""

# 14. Test validation error (negative amount)
echo "14. Test Validation Error (Negative Amount)"
echo "--------------------------------------------"
curl -s -X POST "$BASE_URL/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "toAccount": "ACC-12345",
    "amount": -50.00,
    "currency": "USD",
    "type": "deposit"
  }' | python3 -m json.tool
echo ""

echo "==================================="
echo "All tests completed!"
echo "==================================="
