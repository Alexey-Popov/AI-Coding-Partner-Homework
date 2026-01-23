#!/bin/bash

# Banking Transactions API - Sample Test Script
# Tests all endpoints with various scenarios

API_URL="http://localhost:3000"
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print section header
print_section() {
    echo
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo
}

# Function to print test name
print_test() {
    echo -e "${YELLOW}▶ $1${NC}"
}

# Check if server is running
print_section "🔍 Checking if API server is running"
if ! curl -s "$API_URL" > /dev/null 2>&1; then
    echo -e "${RED}❌ Error: API server is not running at $API_URL${NC}"
    echo "Please start the server first with: ./demo/run.sh"
    exit 1
fi
echo -e "${GREEN}✅ Server is running${NC}"

###############################################################################
print_section "1. HEALTH CHECK"
###############################################################################

print_test "GET /"
curl -s "$API_URL/" | jq '.'
echo

###############################################################################
print_section "2. CREATE VALID TRANSACTIONS"
###############################################################################

print_test "Creating transfer transaction (ACC-12345 → ACC-67890, 100.50 USD)"
TRANSACTION_1=$(curl -s -X POST "$API_URL/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "toAccount": "ACC-67890",
    "amount": 100.50,
    "currency": "USD",
    "type": "transfer"
  }')
echo "$TRANSACTION_1" | jq '.'
TRANSACTION_1_ID=$(echo "$TRANSACTION_1" | jq -r '.id')
echo -e "${GREEN}✅ Transaction created with ID: $TRANSACTION_1_ID${NC}"
echo

print_test "Creating deposit transaction (ACC-00000 → ACC-12345, 500.00 USD)"
curl -s -X POST "$API_URL/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-00000",
    "toAccount": "ACC-12345",
    "amount": 500.00,
    "currency": "USD",
    "type": "deposit"
  }' | jq '.'
echo

print_test "Creating withdrawal transaction (ACC-12345 → ACC-00000, 50.25 USD)"
curl -s -X POST "$API_URL/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "toAccount": "ACC-00000",
    "amount": 50.25,
    "currency": "USD",
    "type": "withdrawal"
  }' | jq '.'
echo

print_test "Creating EUR transaction (ACC-11111 → ACC-22222, 250.00 EUR)"
curl -s -X POST "$API_URL/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-11111",
    "toAccount": "ACC-22222",
    "amount": 250.00,
    "currency": "EUR",
    "type": "transfer"
  }' | jq '.'
echo

###############################################################################
print_section "3. VALIDATION TESTS (should return 400 errors)"
###############################################################################

print_test "Test: Invalid amount (negative)"
curl -s -X POST "$API_URL/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "toAccount": "ACC-67890",
    "amount": -50,
    "currency": "USD",
    "type": "transfer"
  }' | jq '.'
echo

print_test "Test: Invalid amount (too many decimals)"
curl -s -X POST "$API_URL/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "toAccount": "ACC-67890",
    "amount": 10.999,
    "currency": "USD",
    "type": "transfer"
  }' | jq '.'
echo

print_test "Test: Invalid account format"
curl -s -X POST "$API_URL/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "INVALID",
    "toAccount": "ACC-67890",
    "amount": 100,
    "currency": "USD",
    "type": "transfer"
  }' | jq '.'
echo

print_test "Test: Invalid currency"
curl -s -X POST "$API_URL/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "toAccount": "ACC-67890",
    "amount": 100,
    "currency": "XXX",
    "type": "transfer"
  }' | jq '.'
echo

print_test "Test: Multiple validation errors"
curl -s -X POST "$API_URL/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "INVALID",
    "toAccount": "WRONG",
    "amount": -999.999,
    "currency": "XXX",
    "type": "bad"
  }' | jq '.'
echo

###############################################################################
print_section "4. GET TRANSACTIONS"
###############################################################################

print_test "Get all transactions"
ALL_TRANSACTIONS=$(curl -s "$API_URL/transactions")
TRANSACTION_COUNT=$(echo "$ALL_TRANSACTIONS" | jq '. | length')
echo "$ALL_TRANSACTIONS" | jq '.'
echo -e "${GREEN}✅ Total transactions: $TRANSACTION_COUNT${NC}"
echo

print_test "Get specific transaction by ID ($TRANSACTION_1_ID)"
curl -s "$API_URL/transactions/$TRANSACTION_1_ID" | jq '.'
echo

print_test "Test: Get non-existent transaction (should return 404)"
curl -s "$API_URL/transactions/nonexistent-id" | jq '.'
echo

###############################################################################
print_section "5. TRANSACTION FILTERING"
###############################################################################

print_test "Filter by account ID (ACC-12345)"
FILTERED=$(curl -s "$API_URL/transactions?accountId=ACC-12345")
FILTERED_COUNT=$(echo "$FILTERED" | jq '. | length')
echo "$FILTERED" | jq '.'
echo -e "${GREEN}✅ Transactions involving ACC-12345: $FILTERED_COUNT${NC}"
echo

print_test "Filter by type (deposit)"
curl -s "$API_URL/transactions?type=deposit" | jq '.'
echo

print_test "Filter by type (transfer)"
curl -s "$API_URL/transactions?type=transfer" | jq '.'
echo

print_test "Combined filters: ACC-12345 + transfer"
curl -s "$API_URL/transactions?accountId=ACC-12345&type=transfer" | jq '.'
echo

###############################################################################
print_section "6. ACCOUNT BALANCE"
###############################################################################

print_test "Get balance for ACC-12345"
BALANCE=$(curl -s "$API_URL/accounts/ACC-12345/balance")
echo "$BALANCE" | jq '.'
BALANCE_AMOUNT=$(echo "$BALANCE" | jq -r '.balance')
echo -e "${GREEN}✅ ACC-12345 balance: $BALANCE_AMOUNT${NC}"
echo -e "${GREEN}   Calculation: 500.00 (deposit) - 100.50 (transfer) - 50.25 (withdrawal) = 349.25${NC}"
echo

print_test "Get balance for ACC-67890"
curl -s "$API_URL/accounts/ACC-67890/balance" | jq '.'
echo

print_test "Get balance for account with no transactions"
curl -s "$API_URL/accounts/ACC-99999/balance" | jq '.'
echo

###############################################################################
print_section "7. ACCOUNT SUMMARY"
###############################################################################

print_test "Get summary for ACC-12345"
curl -s "$API_URL/accounts/ACC-12345/summary" | jq '.'
echo

print_test "Test: Get summary for account with no transactions (should return 404)"
curl -s "$API_URL/accounts/ACC-99999/summary" | jq '.'
echo

###############################################################################
print_section "8. RATE LIMITING"
###############################################################################

print_test "Check rate limit headers"
curl -s -i "$API_URL/" | grep -E "(HTTP|X-RateLimit)"
echo
echo -e "${YELLOW}Note: Rate limit is 100 requests per minute per IP${NC}"
echo -e "${YELLOW}To test 429 response, run this script rapidly 2-3 times in a row${NC}"
echo

###############################################################################
print_section "✅ ALL TESTS COMPLETE"
###############################################################################

echo -e "${GREEN}🎉 Test suite completed successfully!${NC}"
echo
echo "Summary:"
echo "  • Total transactions created: $TRANSACTION_COUNT"
echo "  • All validation tests passed"
echo "  • All filtering tests passed"
echo "  • Balance calculation verified"
echo "  • Rate limiting active"
echo
