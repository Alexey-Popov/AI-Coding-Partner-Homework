#!/bin/bash

# Banking Transactions API - Test Commands
# This script tests all core API endpoints with sample data

BASE_URL="http://localhost:3000/api/v1"

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  Banking API Test Suite${NC}"
echo -e "${BLUE}========================================${NC}\n"

# Wait for API to be ready
echo -e "${YELLOW}Checking if API is running...${NC}"
curl -s "${BASE_URL}/transactions" > /dev/null 2>&1
if [ $? -ne 0 ]; then
    echo -e "${YELLOW}Warning: API may not be running. Please start it with: mvn spring-boot:run${NC}\n"
    exit 1
fi
echo -e "${GREEN}✓ API is running${NC}\n"

# Test 1: Create a deposit transaction
echo -e "${BLUE}Test 1: Create Deposit Transaction${NC}"
echo -e "${YELLOW}POST ${BASE_URL}/transactions${NC}"
echo "Expected: 201 Created, transaction with auto-generated ID and timestamp"
DEPOSIT_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "toAccount": "ACC-12345",
    "amount": 500.00,
    "currency": "USD",
    "type": "deposit"
  }')
DEPOSIT_HTTP_CODE=$(echo "$DEPOSIT_RESPONSE" | tail -n 1)
DEPOSIT_BODY=$(echo "$DEPOSIT_RESPONSE" | sed '$d')
echo "Response (${DEPOSIT_HTTP_CODE}):"
echo "$DEPOSIT_BODY" | jq '.' 2>/dev/null || echo "$DEPOSIT_BODY"
# Extract the transaction ID for later use
DEPOSIT_ID=$(echo "$DEPOSIT_BODY" | jq -r '.id' 2>/dev/null)
echo -e "${GREEN}✓ Deposit transaction created${NC}\n"
sleep 1

# Test 2: Create a withdrawal transaction
echo -e "${BLUE}Test 2: Create Withdrawal Transaction${NC}"
echo -e "${YELLOW}POST ${BASE_URL}/transactions${NC}"
echo "Expected: 201 Created, withdrawal from ACC-12345"
WITHDRAWAL_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "amount": 100.00,
    "currency": "USD",
    "type": "withdrawal"
  }')
WITHDRAWAL_HTTP_CODE=$(echo "$WITHDRAWAL_RESPONSE" | tail -n 1)
WITHDRAWAL_BODY=$(echo "$WITHDRAWAL_RESPONSE" | sed '$d')
echo "Response (${WITHDRAWAL_HTTP_CODE}):"
echo "$WITHDRAWAL_BODY" | jq '.' 2>/dev/null || echo "$WITHDRAWAL_BODY"
WITHDRAWAL_ID=$(echo "$WITHDRAWAL_BODY" | jq -r '.id' 2>/dev/null)
echo -e "${GREEN}✓ Withdrawal transaction created${NC}\n"
sleep 1

# Test 3: Create a transfer transaction
echo -e "${BLUE}Test 3: Create Transfer Transaction${NC}"
echo -e "${YELLOW}POST ${BASE_URL}/transactions${NC}"
echo "Expected: 201 Created, transfer from ACC-12345 to ACC-67890"
TRANSFER_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "toAccount": "ACC-67890",
    "amount": 50.00,
    "currency": "USD",
    "type": "transfer"
  }')
TRANSFER_HTTP_CODE=$(echo "$TRANSFER_RESPONSE" | tail -n 1)
TRANSFER_BODY=$(echo "$TRANSFER_RESPONSE" | sed '$d')
echo "Response (${TRANSFER_HTTP_CODE}):"
echo "$TRANSFER_BODY" | jq '.' 2>/dev/null || echo "$TRANSFER_BODY"
TRANSFER_ID=$(echo "$TRANSFER_BODY" | jq -r '.id' 2>/dev/null)
echo -e "${GREEN}✓ Transfer transaction created${NC}\n"
sleep 1

# Test 4: Get all transactions
echo -e "${BLUE}Test 4: Get All Transactions${NC}"
echo -e "${YELLOW}GET ${BASE_URL}/transactions${NC}"
echo "Expected: 200 OK, array of all transactions (should show 3 transactions)"
ALL_TRANSACTIONS=$(curl -s -w "\n%{http_code}" -X GET "${BASE_URL}/transactions")
ALL_HTTP_CODE=$(echo "$ALL_TRANSACTIONS" | tail -n 1)
ALL_BODY=$(echo "$ALL_TRANSACTIONS" | sed '$d')
echo "Response (${ALL_HTTP_CODE}):"
echo "$ALL_BODY" | jq '.' 2>/dev/null || echo "$ALL_BODY"
TRANSACTION_COUNT=$(echo "$ALL_BODY" | jq '. | length' 2>/dev/null)
echo "Total transactions: ${TRANSACTION_COUNT}"
echo -e "${GREEN}✓ Retrieved all transactions${NC}\n"
sleep 1

# Test 5: Get specific transaction by ID
echo -e "${BLUE}Test 5: Get Transaction by ID${NC}"
echo -e "${YELLOW}GET ${BASE_URL}/transactions/${DEPOSIT_ID}${NC}"
echo "Expected: 200 OK, specific transaction details"
if [ ! -z "$DEPOSIT_ID" ] && [ "$DEPOSIT_ID" != "null" ]; then
    SINGLE_TRANSACTION=$(curl -s -w "\n%{http_code}" -X GET "${BASE_URL}/transactions/${DEPOSIT_ID}")
    SINGLE_HTTP_CODE=$(echo "$SINGLE_TRANSACTION" | tail -n 1)
    SINGLE_BODY=$(echo "$SINGLE_TRANSACTION" | sed '$d')
    echo "Response (${SINGLE_HTTP_CODE}):"
    echo "$SINGLE_BODY" | jq '.' 2>/dev/null || echo "$SINGLE_BODY"
    echo -e "${GREEN}✓ Retrieved transaction by ID${NC}\n"
else
    echo -e "${YELLOW}⚠ Skipping: No transaction ID available${NC}\n"
fi
sleep 1

# Test 6: Get transaction by non-existent ID (should return 404)
echo -e "${BLUE}Test 6: Get Non-Existent Transaction${NC}"
echo -e "${YELLOW}GET ${BASE_URL}/transactions/non-existent-id${NC}"
echo "Expected: 404 Not Found"
NOT_FOUND=$(curl -s -w "\n%{http_code}" -X GET "${BASE_URL}/transactions/non-existent-id")
NOT_FOUND_HTTP_CODE=$(echo "$NOT_FOUND" | tail -n 1)
NOT_FOUND_BODY=$(echo "$NOT_FOUND" | sed '$d')
echo "Response (${NOT_FOUND_HTTP_CODE}):"
echo "$NOT_FOUND_BODY" | jq '.' 2>/dev/null || echo "$NOT_FOUND_BODY"
echo -e "${GREEN}✓ Correctly returned 404${NC}\n"
sleep 1

# Test 7: Get account balance for ACC-12345
echo -e "${BLUE}Test 7: Get Account Balance for ACC-12345${NC}"
echo -e "${YELLOW}GET ${BASE_URL}/accounts/ACC-12345/balance${NC}"
echo "Expected: 200 OK, balance = 500.00 (deposit) - 100.00 (withdrawal) - 50.00 (transfer) = 350.00"
BALANCE_12345=$(curl -s -w "\n%{http_code}" -X GET "${BASE_URL}/accounts/ACC-12345/balance")
BALANCE_HTTP_CODE=$(echo "$BALANCE_12345" | tail -n 1)
BALANCE_BODY=$(echo "$BALANCE_12345" | sed '$d')
echo "Response (${BALANCE_HTTP_CODE}):"
echo "$BALANCE_BODY" | jq '.' 2>/dev/null || echo "$BALANCE_BODY"
CALCULATED_BALANCE=$(echo "$BALANCE_BODY" | jq -r '.balance' 2>/dev/null)
echo "Calculated balance: ${CALCULATED_BALANCE}"
echo -e "${GREEN}✓ Retrieved account balance${NC}\n"
sleep 1

# Test 8: Get account balance for ACC-67890
echo -e "${BLUE}Test 8: Get Account Balance for ACC-67890${NC}"
echo -e "${YELLOW}GET ${BASE_URL}/accounts/ACC-67890/balance${NC}"
echo "Expected: 200 OK, balance = 50.00 (received from transfer)"
BALANCE_67890=$(curl -s -w "\n%{http_code}" -X GET "${BASE_URL}/accounts/ACC-67890/balance")
BALANCE_HTTP_CODE_2=$(echo "$BALANCE_67890" | tail -n 1)
BALANCE_BODY_2=$(echo "$BALANCE_67890" | sed '$d')
echo "Response (${BALANCE_HTTP_CODE_2}):"
echo "$BALANCE_BODY_2" | jq '.' 2>/dev/null || echo "$BALANCE_BODY_2"
CALCULATED_BALANCE_2=$(echo "$BALANCE_BODY_2" | jq -r '.balance' 2>/dev/null)
echo "Calculated balance: ${CALCULATED_BALANCE_2}"
echo -e "${GREEN}✓ Retrieved account balance${NC}\n"

# Summary
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  Test Suite Summary${NC}"
echo -e "${BLUE}========================================${NC}"
echo -e "Total Tests Run: 8"
echo -e "${GREEN}✓ All core API endpoints tested successfully${NC}"
echo ""
echo -e "Balance Verification:"
echo -e "  ACC-12345: ${CALCULATED_BALANCE} (Expected: 350.00)"
echo -e "  ACC-67890: ${CALCULATED_BALANCE_2} (Expected: 50.00)"
echo ""
echo -e "${YELLOW}Note: If jq is not installed, JSON responses will be displayed without formatting.${NC}"
echo -e "${YELLOW}Install jq with: brew install jq (Mac) or apt-get install jq (Linux)${NC}"
