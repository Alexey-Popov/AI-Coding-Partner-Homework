#!/bin/bash

# Banking Transactions API - Validation Test Suite
# This script tests all validation requirements from Task 2

BASE_URL="http://localhost:3000/api/v1"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  Validation Test Suite${NC}"
echo -e "${BLUE}========================================${NC}\n"

# Counter for tests
TOTAL_TESTS=0
PASSED_TESTS=0

# Function to run a test
run_test() {
    local test_name=$1
    local expected_status=$2
    local response=$3
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    local http_code=$(echo "$response" | tail -n 1)
    local body=$(echo "$response" | sed '$d')
    
    echo -e "${YELLOW}HTTP Status: ${http_code}${NC}"
    echo "Response:"
    echo "$body" | jq '.' 2>/dev/null || echo "$body"
    
    if [ "$http_code" = "$expected_status" ]; then
        echo -e "${GREEN}✓ Test passed: $test_name${NC}\n"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        echo -e "${RED}✗ Test failed: Expected $expected_status, got $http_code${NC}\n"
    fi
    
    sleep 0.5
}

# Check if API is running
echo -e "${YELLOW}Checking if API is running...${NC}"
curl -s "${BASE_URL}/transactions" > /dev/null 2>&1
if [ $? -ne 0 ]; then
    echo -e "${RED}Error: API is not running. Please start it with: mvn spring-boot:run${NC}\n"
    exit 1
fi
echo -e "${GREEN}✓ API is running${NC}\n"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  AMOUNT VALIDATION TESTS${NC}"
echo -e "${BLUE}========================================${NC}\n"

# Test 1: Negative amount
echo -e "${BLUE}Test 1: Negative Amount${NC}"
echo "Expected: 400 Bad Request - Amount must be positive"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "toAccount": "ACC-67890",
    "amount": -100.00,
    "currency": "USD",
    "type": "transfer"
  }')
run_test "Negative amount" "400" "$RESPONSE"

# Test 2: Zero amount
echo -e "${BLUE}Test 2: Zero Amount${NC}"
echo "Expected: 400 Bad Request - Amount must be greater than zero"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "toAccount": "ACC-67890",
    "amount": 0,
    "currency": "USD",
    "type": "transfer"
  }')
run_test "Zero amount" "400" "$RESPONSE"

# Test 3: Too many decimal places (more than 2)
echo -e "${BLUE}Test 3: Amount with More Than 2 Decimal Places${NC}"
echo "Expected: 400 Bad Request - Amount must have maximum 2 decimal places"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "toAccount": "ACC-67890",
    "amount": 100.999,
    "currency": "USD",
    "type": "transfer"
  }')
run_test "Too many decimal places" "400" "$RESPONSE"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  ACCOUNT FORMAT VALIDATION TESTS${NC}"
echo -e "${BLUE}========================================${NC}\n"

# Test 4: Invalid account format - missing ACC prefix
echo -e "${BLUE}Test 4: Invalid Account Format - Missing Prefix${NC}"
echo "Expected: 400 Bad Request - Account must follow format ACC-XXXXX"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "12345",
    "toAccount": "ACC-67890",
    "amount": 100.00,
    "currency": "USD",
    "type": "transfer"
  }')
run_test "Invalid account format - missing prefix" "400" "$RESPONSE"

# Test 5: Invalid account format - wrong length
echo -e "${BLUE}Test 5: Invalid Account Format - Wrong Length${NC}"
echo "Expected: 400 Bad Request - Account must be exactly 9 characters (ACC-XXXXX)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-123",
    "toAccount": "ACC-67890",
    "amount": 100.00,
    "currency": "USD",
    "type": "transfer"
  }')
run_test "Invalid account format - wrong length" "400" "$RESPONSE"

# Test 6: Invalid account format - special characters
echo -e "${BLUE}Test 6: Invalid Account Format - Special Characters${NC}"
echo "Expected: 400 Bad Request - Account must contain only alphanumeric characters"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12@45",
    "toAccount": "ACC-67890",
    "amount": 100.00,
    "currency": "USD",
    "type": "transfer"
  }')
run_test "Invalid account format - special characters" "400" "$RESPONSE"

# Test 7: Invalid account format - lowercase
echo -e "${BLUE}Test 7: Invalid Account Format - Lowercase${NC}"
echo "Expected: 400 Bad Request (or accept and convert to uppercase)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "acc-12345",
    "toAccount": "ACC-67890",
    "amount": 100.00,
    "currency": "USD",
    "type": "transfer"
  }')
run_test "Invalid account format - lowercase" "400" "$RESPONSE"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  CURRENCY VALIDATION TESTS${NC}"
echo -e "${BLUE}========================================${NC}\n"

# Test 8: Invalid currency code - not ISO 4217
echo -e "${BLUE}Test 8: Invalid Currency Code${NC}"
echo "Expected: 400 Bad Request - Currency must be valid ISO 4217 code"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "toAccount": "ACC-67890",
    "amount": 100.00,
    "currency": "INVALID",
    "type": "transfer"
  }')
run_test "Invalid currency code" "400" "$RESPONSE"

# Test 9: Currency code - wrong length
echo -e "${BLUE}Test 9: Currency Code - Wrong Length${NC}"
echo "Expected: 400 Bad Request - Currency must be 3 characters"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "toAccount": "ACC-67890",
    "amount": 100.00,
    "currency": "US",
    "type": "transfer"
  }')
run_test "Currency code - wrong length" "400" "$RESPONSE"

# Test 10: Currency code - lowercase
echo -e "${BLUE}Test 10: Currency Code - Lowercase${NC}"
echo "Expected: 400 Bad Request (or accept and convert to uppercase)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "toAccount": "ACC-67890",
    "amount": 100.00,
    "currency": "usd",
    "type": "transfer"
  }')
run_test "Currency code - lowercase" "400" "$RESPONSE"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  TRANSACTION TYPE VALIDATION TESTS${NC}"
echo -e "${BLUE}========================================${NC}\n"

# Test 11: Invalid transaction type
echo -e "${BLUE}Test 11: Invalid Transaction Type${NC}"
echo "Expected: 400 Bad Request - Type must be deposit, withdrawal, or transfer"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "toAccount": "ACC-67890",
    "amount": 100.00,
    "currency": "USD",
    "type": "invalid_type"
  }')
run_test "Invalid transaction type" "400" "$RESPONSE"

# Test 12: Transaction type - wrong case
echo -e "${BLUE}Test 12: Transaction Type - Uppercase${NC}"
echo "Expected: 400 Bad Request (or accept and convert to lowercase)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "toAccount": "ACC-67890",
    "amount": 100.00,
    "currency": "USD",
    "type": "TRANSFER"
  }')
run_test "Transaction type - uppercase" "400" "$RESPONSE"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  MISSING REQUIRED FIELDS TESTS${NC}"
echo -e "${BLUE}========================================${NC}\n"

# Test 13: Missing amount
echo -e "${BLUE}Test 13: Missing Amount${NC}"
echo "Expected: 400 Bad Request - Amount is required"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "toAccount": "ACC-67890",
    "currency": "USD",
    "type": "transfer"
  }')
run_test "Missing amount" "400" "$RESPONSE"

# Test 14: Missing currency
echo -e "${BLUE}Test 14: Missing Currency${NC}"
echo "Expected: 400 Bad Request - Currency is required"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "toAccount": "ACC-67890",
    "amount": 100.00,
    "type": "transfer"
  }')
run_test "Missing currency" "400" "$RESPONSE"

# Test 15: Missing type
echo -e "${BLUE}Test 15: Missing Transaction Type${NC}"
echo "Expected: 400 Bad Request - Type is required"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "toAccount": "ACC-67890",
    "amount": 100.00,
    "currency": "USD"
  }')
run_test "Missing transaction type" "400" "$RESPONSE"

# Test 16: Empty request body
echo -e "${BLUE}Test 16: Empty Request Body${NC}"
echo "Expected: 400 Bad Request - Multiple required fields missing"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/transactions" \
  -H "Content-Type: application/json" \
  -d '{}')
run_test "Empty request body" "400" "$RESPONSE"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  BUSINESS RULE VALIDATION TESTS${NC}"
echo -e "${BLUE}========================================${NC}\n"

# Test 17: Transfer with same fromAccount and toAccount
echo -e "${BLUE}Test 17: Transfer to Same Account${NC}"
echo "Expected: 400 Bad Request - Transfer requires different accounts"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "toAccount": "ACC-12345",
    "amount": 100.00,
    "currency": "USD",
    "type": "transfer"
  }')
run_test "Transfer to same account" "400" "$RESPONSE"

# Test 18: Deposit without toAccount
echo -e "${BLUE}Test 18: Deposit Without toAccount${NC}"
echo "Expected: 400 Bad Request - Deposit requires toAccount"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 100.00,
    "currency": "USD",
    "type": "deposit"
  }')
run_test "Deposit without toAccount" "400" "$RESPONSE"

# Test 19: Withdrawal without fromAccount
echo -e "${BLUE}Test 19: Withdrawal Without fromAccount${NC}"
echo "Expected: 400 Bad Request - Withdrawal requires fromAccount"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 100.00,
    "currency": "USD",
    "type": "withdrawal"
  }')
run_test "Withdrawal without fromAccount" "400" "$RESPONSE"

# Test 20: Transfer without fromAccount
echo -e "${BLUE}Test 20: Transfer Without fromAccount${NC}"
echo "Expected: 400 Bad Request - Transfer requires both accounts"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "toAccount": "ACC-67890",
    "amount": 100.00,
    "currency": "USD",
    "type": "transfer"
  }')
run_test "Transfer without fromAccount" "400" "$RESPONSE"

# Test 21: Transfer without toAccount
echo -e "${BLUE}Test 21: Transfer Without toAccount${NC}"
echo "Expected: 400 Bad Request - Transfer requires both accounts"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "amount": 100.00,
    "currency": "USD",
    "type": "transfer"
  }')
run_test "Transfer without toAccount" "400" "$RESPONSE"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  MULTIPLE VALIDATION ERRORS TEST${NC}"
echo -e "${BLUE}========================================${NC}\n"

# Test 22: Multiple validation errors at once
echo -e "${BLUE}Test 22: Multiple Validation Errors${NC}"
echo "Expected: 400 Bad Request - Multiple error details (negative amount + invalid currency + invalid account)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "INVALID",
    "toAccount": "ACC-67890",
    "amount": -50.999,
    "currency": "XXX",
    "type": "transfer"
  }')
run_test "Multiple validation errors" "400" "$RESPONSE"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  VALID TRANSACTION TEST${NC}"
echo -e "${BLUE}========================================${NC}\n"

# Test 23: Valid transaction after all validation tests
echo -e "${BLUE}Test 23: Valid Transaction${NC}"
echo "Expected: 201 Created - Transaction created successfully"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-11111",
    "toAccount": "ACC-22222",
    "amount": 150.50,
    "currency": "USD",
    "type": "transfer"
  }')
run_test "Valid transaction" "201" "$RESPONSE"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  EDGE CASES${NC}"
echo -e "${BLUE}========================================${NC}\n"

# Test 24: Very large amount
echo -e "${BLUE}Test 24: Very Large Amount${NC}"
echo "Expected: 201 Created (or 400 if max limit enforced)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-33333",
    "toAccount": "ACC-44444",
    "amount": 999999999.99,
    "currency": "USD",
    "type": "transfer"
  }')
run_test "Very large amount" "201" "$RESPONSE"

# Test 25: Very small amount (but valid)
echo -e "${BLUE}Test 25: Very Small Amount${NC}"
echo "Expected: 201 Created"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-55555",
    "toAccount": "ACC-66666",
    "amount": 0.01,
    "currency": "USD",
    "type": "transfer"
  }')
run_test "Very small amount" "201" "$RESPONSE"

# Test 26: Deposit with valid data
echo -e "${BLUE}Test 26: Valid Deposit${NC}"
echo "Expected: 201 Created"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "toAccount": "ACC-77777",
    "amount": 1000.00,
    "currency": "EUR",
    "type": "deposit"
  }')
run_test "Valid deposit" "201" "$RESPONSE"

# Test 27: Withdrawal with valid data
echo -e "${BLUE}Test 27: Valid Withdrawal${NC}"
echo "Expected: 201 Created"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-88888",
    "amount": 250.75,
    "currency": "GBP",
    "type": "withdrawal"
  }')
run_test "Valid withdrawal" "201" "$RESPONSE"

# Summary
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  VALIDATION TEST SUITE SUMMARY${NC}"
echo -e "${BLUE}========================================${NC}"
echo -e "Total Tests: ${TOTAL_TESTS}"
echo -e "${GREEN}Passed: ${PASSED_TESTS}${NC}"
echo -e "${RED}Failed: $((TOTAL_TESTS - PASSED_TESTS))${NC}"
echo ""

if [ $PASSED_TESTS -eq $TOTAL_TESTS ]; then
    echo -e "${GREEN}✓ All validation tests passed!${NC}"
    exit 0
else
    echo -e "${YELLOW}⚠ Some tests failed. Review the output above.${NC}"
    exit 1
fi
