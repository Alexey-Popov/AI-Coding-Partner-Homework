#!/bin/bash

# Banking Transactions API - Sample curl Requests
# This script demonstrates all API endpoints

BASE_URL="http://localhost:3000"

echo "========================================"
echo "  Banking Transactions API - Test Suite"
echo "========================================"
echo ""

# Color codes for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Function to print section headers
print_section() {
    echo ""
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}========================================${NC}"
}

# Function to print test descriptions
print_test() {
    echo ""
    echo -e "${GREEN}➤ $1${NC}"
}

# Wait for user to press Enter
pause() {
    read -p "Press Enter to continue..."
}

# Test if server is running
print_section "Checking Server Connection"
if curl -s "$BASE_URL" > /dev/null 2>&1; then
    echo -e "${GREEN}✅ Server is running at $BASE_URL${NC}"
else
    echo -e "${RED}❌ Server is not responding at $BASE_URL${NC}"
    echo "Please start the server first using: npm start"
    exit 1
fi

pause

# 1. Create Transfer Transaction
print_section "1. Creating Transfer Transaction"
print_test "POST /transactions - Transfer 100.50 USD from ACC-12345 to ACC-67890"
curl -X POST "$BASE_URL/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "toAccount": "ACC-67890",
    "amount": 100.50,
    "currency": "USD",
    "type": "transfer"
  }' | jq .
pause

# 2. Create Deposit Transaction
print_section "2. Creating Deposit Transaction"
print_test "POST /transactions - Deposit 500.00 USD to ACC-12345"
curl -X POST "$BASE_URL/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-00000",
    "toAccount": "ACC-12345",
    "amount": 500.00,
    "currency": "USD",
    "type": "deposit"
  }' | jq .
pause

# 3. Create Withdrawal Transaction
print_section "3. Creating Withdrawal Transaction"
print_test "POST /transactions - Withdraw 50.25 USD from ACC-67890"
curl -X POST "$BASE_URL/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-67890",
    "toAccount": "ACC-00000",
    "amount": 50.25,
    "currency": "USD",
    "type": "withdrawal"
  }' | jq .
pause

# 4. Get All Transactions
print_section "4. Getting All Transactions"
print_test "GET /transactions"
curl -X GET "$BASE_URL/transactions" | jq .
pause

# 5. Get Account Balance
print_section "5. Getting Account Balance"
print_test "GET /accounts/ACC-12345/balance"
curl -X GET "$BASE_URL/accounts/ACC-12345/balance" | jq .
pause

# 6. Filter by Account
print_section "6. Filtering Transactions by Account"
print_test "GET /transactions?accountId=ACC-12345"
curl -X GET "$BASE_URL/transactions?accountId=ACC-12345" | jq .
pause

# 7. Filter by Type
print_section "7. Filtering Transactions by Type"
print_test "GET /transactions?type=transfer"
curl -X GET "$BASE_URL/transactions?type=transfer" | jq .
pause

# 8. Filter by Date Range
print_section "8. Filtering Transactions by Date Range"
print_test "GET /transactions?from=2024-01-01&to=2024-12-31"
curl -X GET "$BASE_URL/transactions?from=2024-01-01&to=2024-12-31" | jq .
pause

# 9. Test Validation - Invalid Amount
print_section "9. Testing Validation - Invalid Amount (Negative)"
print_test "POST /transactions - Should fail with negative amount"
curl -X POST "$BASE_URL/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "toAccount": "ACC-67890",
    "amount": -50.00,
    "currency": "USD",
    "type": "transfer"
  }' | jq .
pause

# 10. Test Validation - Invalid Currency
print_section "10. Testing Validation - Invalid Currency"
print_test "POST /transactions - Should fail with invalid currency"
curl -X POST "$BASE_URL/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "toAccount": "ACC-67890",
    "amount": 100.00,
    "currency": "INVALID",
    "type": "transfer"
  }' | jq .
pause

# 11. Test Validation - Invalid Account Format
print_section "11. Testing Validation - Invalid Account Format"
print_test "POST /transactions - Should fail with invalid account format"
curl -X POST "$BASE_URL/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "INVALID",
    "toAccount": "ACC-67890",
    "amount": 100.00,
    "currency": "USD",
    "type": "transfer"
  }' | jq .
pause

# 12. Create EUR Transaction
print_section "12. Creating Transaction with EUR"
print_test "POST /transactions - Transfer 250.75 EUR"
curl -X POST "$BASE_URL/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-11111",
    "toAccount": "ACC-22222",
    "amount": 250.75,
    "currency": "EUR",
    "type": "transfer"
  }' | jq .
pause

# 13. Create GBP Transaction
print_section "13. Creating Transaction with GBP"
print_test "POST /transactions - Transfer 150.00 GBP"
curl -X POST "$BASE_URL/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-33333",
    "toAccount": "ACC-44444",
    "amount": 150.00,
    "currency": "GBP",
    "type": "transfer"
  }' | jq .

echo ""
print_section "Test Suite Completed!"
echo -e "${GREEN}✅ All tests executed successfully${NC}"
echo ""
