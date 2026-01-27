#!/bin/bash

# Banking Transactions API - Sample Requests Script
# This script demonstrates the API functionality using curl

BASE_URL="http://localhost:3000"

echo "🏦 Banking Transactions API - Sample Requests"
echo "=============================================="
echo ""

# Color codes for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 1. Get API Information
echo -e "${YELLOW}1. Getting API Information${NC}"
curl -s $BASE_URL | jq '.'
echo ""
sleep 1

# 2. Create a Deposit Transaction
echo -e "${YELLOW}2. Creating a Deposit Transaction${NC}"
DEPOSIT_RESPONSE=$(curl -s -X POST $BASE_URL/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "toAccount": "ACC-12345",
    "amount": 500.00,
    "currency": "USD",
    "type": "deposit"
  }')
echo $DEPOSIT_RESPONSE | jq '.'
TRANSACTION_ID=$(echo $DEPOSIT_RESPONSE | jq -r '.id')
echo -e "${GREEN}✓ Transaction created with ID: $TRANSACTION_ID${NC}"
echo ""
sleep 1

# 3. Create a Transfer Transaction
echo -e "${YELLOW}3. Creating a Transfer Transaction${NC}"
curl -s -X POST $BASE_URL/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "toAccount": "ACC-67890",
    "amount": 100.50,
    "currency": "USD",
    "type": "transfer"
  }' | jq '.'
echo ""
sleep 1

# 4. Create a Withdrawal Transaction
echo -e "${YELLOW}4. Creating a Withdrawal Transaction${NC}"
curl -s -X POST $BASE_URL/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "amount": 50.25,
    "currency": "USD",
    "type": "withdrawal"
  }' | jq '.'
echo ""
sleep 1

# 5. Get All Transactions
echo -e "${YELLOW}5. Getting All Transactions${NC}"
curl -s $BASE_URL/transactions | jq '.'
echo ""
sleep 1

# 6. Get Specific Transaction by ID
echo -e "${YELLOW}6. Getting Transaction by ID: $TRANSACTION_ID${NC}"
curl -s $BASE_URL/transactions/$TRANSACTION_ID | jq '.'
echo ""
sleep 1

# 7. Get Transactions for Specific Account
echo -e "${YELLOW}7. Getting Transactions for Account ACC-12345${NC}"
curl -s "$BASE_URL/transactions?accountId=ACC-12345" | jq '.'
echo ""
sleep 1

# 8. Get Account Balance
echo -e "${YELLOW}8. Getting Balance for Account ACC-12345${NC}"
curl -s $BASE_URL/accounts/ACC-12345/balance | jq '.'
echo ""
sleep 1

# 9. Get Account Summary
echo -e "${YELLOW}9. Getting Summary for Account ACC-12345${NC}"
curl -s $BASE_URL/accounts/ACC-12345/summary | jq '.'
echo ""
sleep 1

# 10. Test Validation - Invalid Amount
echo -e "${YELLOW}10. Testing Validation - Invalid Amount (negative)${NC}"
curl -s -X POST $BASE_URL/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "toAccount": "ACC-67890",
    "amount": -100,
    "currency": "USD",
    "type": "transfer"
  }' | jq '.'
echo ""
sleep 1

# 11. Test Validation - Invalid Currency
echo -e "${YELLOW}11. Testing Validation - Invalid Currency${NC}"
curl -s -X POST $BASE_URL/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "toAccount": "ACC-67890",
    "amount": 100.50,
    "currency": "XXX",
    "type": "transfer"
  }' | jq '.'
echo ""
sleep 1

echo -e "${GREEN}=============================================="
echo -e "✓ All sample requests completed!${NC}"
