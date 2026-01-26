#!/bin/bash

###############################################################################
# Banking Transactions REST API - Sample Requests Script
# This script demonstrates all API endpoints with curl commands
# Color-coded output for better readability
###############################################################################

# Base URL
BASE_URL="http://localhost:3000/api/v1"

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
MAGENTA='\033[0;35m'
NC='\033[0m' # No Color

# Test counter
TEST_COUNT=0

# Function to print section headers
print_section() {
    echo ""
    echo -e "${BLUE}═══════════════════════════════════════════════════════════════════${NC}"
    echo -e "${BLUE}  $1${NC}"
    echo -e "${BLUE}═══════════════════════════════════════════════════════════════════${NC}"
    echo ""
}

# Function to print test info
print_test() {
    TEST_COUNT=$((TEST_COUNT + 1))
    echo -e "${CYAN}TEST $TEST_COUNT: $1${NC}"
    echo -e "${YELLOW}$2${NC}"
    echo ""
}

# Function to execute curl with nice output
execute_curl() {
    echo -e "${MAGENTA}→ Executing...${NC}"
    response=$(eval "$1" 2>&1)
    status=$?
    
    if [ $status -eq 0 ]; then
        echo -e "${GREEN}✓ Response:${NC}"
        echo "$response" | jq '.' 2>/dev/null || echo "$response"
    else
        echo -e "${RED}✗ Error:${NC}"
        echo "$response"
    fi
    echo ""
    sleep 1
}

# Check if server is running
check_server() {
    echo -e "${YELLOW}Checking if server is running...${NC}"
    if curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/transactions" | grep -q "200\|404"; then
        echo -e "${GREEN}✓ Server is running at $BASE_URL${NC}"
        echo ""
    else
        echo -e "${RED}✗ Server is not running or not responding${NC}"
        echo "Please start the server first with: mvn spring-boot:run"
        exit 1
    fi
}

# Check if jq is installed
check_jq() {
    if ! command -v jq &> /dev/null; then
        echo -e "${YELLOW}⚠ Warning: jq is not installed. Output will not be formatted.${NC}"
        echo "Install jq for better output: brew install jq (macOS) or apt-get install jq (Linux)"
        echo ""
    fi
}

# Save transaction IDs for later use
declare -A TRANSACTION_IDS

# Main script
echo -e "${BLUE}"
echo "╔═══════════════════════════════════════════════════════════════════╗"
echo "║                                                                   ║"
echo "║         Banking Transactions REST API - Sample Requests          ║"
echo "║                                                                   ║"
echo "╚═══════════════════════════════════════════════════════════════════╝"
echo -e "${NC}"

check_server
check_jq

###############################################################################
# SECTION 1: CREATE TRANSACTIONS
###############################################################################

print_section "SECTION 1: CREATE TRANSACTIONS"

print_test "Create Deposit Transaction" \
    "POST $BASE_URL/transactions"
execute_curl "curl -s -X POST '$BASE_URL/transactions' \
  -H 'Content-Type: application/json' \
  -d '{
    \"type\": \"deposit\",
    \"toAccount\": \"ACC-12345\",
    \"amount\": 500.00,
    \"currency\": \"USD\"
  }'"

print_test "Create Withdrawal Transaction" \
    "POST $BASE_URL/transactions"
execute_curl "curl -s -X POST '$BASE_URL/transactions' \
  -H 'Content-Type: application/json' \
  -d '{
    \"type\": \"withdrawal\",
    \"fromAccount\": \"ACC-12345\",
    \"amount\": 100.50,
    \"currency\": \"USD\"
  }'"

print_test "Create Transfer Transaction" \
    "POST $BASE_URL/transactions"
execute_curl "curl -s -X POST '$BASE_URL/transactions' \
  -H 'Content-Type: application/json' \
  -d '{
    \"type\": \"transfer\",
    \"fromAccount\": \"ACC-12345\",
    \"toAccount\": \"ACC-67890\",
    \"amount\": 250.75,
    \"currency\": \"USD\"
  }'"

print_test "Create Deposit in EUR" \
    "POST $BASE_URL/transactions"
execute_curl "curl -s -X POST '$BASE_URL/transactions' \
  -H 'Content-Type: application/json' \
  -d '{
    \"type\": \"deposit\",
    \"toAccount\": \"ACC-67890\",
    \"amount\": 1000.00,
    \"currency\": \"EUR\"
  }'"

print_test "Create Withdrawal in GBP" \
    "POST $BASE_URL/transactions"
execute_curl "curl -s -X POST '$BASE_URL/transactions' \
  -H 'Content-Type: application/json' \
  -d '{
    \"type\": \"withdrawal\",
    \"fromAccount\": \"ACC-11111\",
    \"amount\": 75.25,
    \"currency\": \"GBP\"
  }'"

###############################################################################
# SECTION 2: RETRIEVE TRANSACTIONS
###############################################################################

print_section "SECTION 2: RETRIEVE TRANSACTIONS"

print_test "Get All Transactions" \
    "GET $BASE_URL/transactions"
execute_curl "curl -s -X GET '$BASE_URL/transactions'"

print_test "Get Transactions by Account ID" \
    "GET $BASE_URL/transactions?accountId=ACC-12345"
execute_curl "curl -s -X GET '$BASE_URL/transactions?accountId=ACC-12345'"

print_test "Get Transactions by Type (deposit)" \
    "GET $BASE_URL/transactions?type=deposit"
execute_curl "curl -s -X GET '$BASE_URL/transactions?type=deposit'"

print_test "Get Transactions by Date Range" \
    "GET $BASE_URL/transactions?from=2026-01-01T00:00:00&to=2026-12-31T23:59:59"
execute_curl "curl -s -X GET '$BASE_URL/transactions?from=2026-01-01T00:00:00&to=2026-12-31T23:59:59'"

print_test "Get Transactions by Status" \
    "GET $BASE_URL/transactions?status=COMPLETED"
execute_curl "curl -s -X GET '$BASE_URL/transactions?status=COMPLETED'"

print_test "Get Transactions with Multiple Filters" \
    "GET $BASE_URL/transactions?accountId=ACC-12345&type=deposit"
execute_curl "curl -s -X GET '$BASE_URL/transactions?accountId=ACC-12345&type=deposit'"

###############################################################################
# SECTION 3: ACCOUNT BALANCE
###############################################################################

print_section "SECTION 3: ACCOUNT BALANCE"

print_test "Get Balance for Account ACC-12345" \
    "GET $BASE_URL/accounts/ACC-12345/balance"
execute_curl "curl -s -X GET '$BASE_URL/accounts/ACC-12345/balance'"

print_test "Get Balance for Account ACC-67890" \
    "GET $BASE_URL/accounts/ACC-67890/balance"
execute_curl "curl -s -X GET '$BASE_URL/accounts/ACC-67890/balance'"

print_test "Get Balance for Account ACC-11111" \
    "GET $BASE_URL/accounts/ACC-11111/balance"
execute_curl "curl -s -X GET '$BASE_URL/accounts/ACC-11111/balance'"

###############################################################################
# SECTION 4: TRANSACTION SUMMARY (Task 4 Feature A)
###############################################################################

print_section "SECTION 4: TRANSACTION SUMMARY"

print_test "Get Transaction Summary for Account ACC-12345" \
    "GET $BASE_URL/accounts/ACC-12345/summary"
execute_curl "curl -s -X GET '$BASE_URL/accounts/ACC-12345/summary'"

print_test "Get Transaction Summary for Account ACC-67890" \
    "GET $BASE_URL/accounts/ACC-67890/summary"
execute_curl "curl -s -X GET '$BASE_URL/accounts/ACC-67890/summary'"

print_test "Get Transaction Summary for Account ACC-11111" \
    "GET $BASE_URL/accounts/ACC-11111/summary"
execute_curl "curl -s -X GET '$BASE_URL/accounts/ACC-11111/summary'"

###############################################################################
# SECTION 5: INTEREST CALCULATION (Task 4 Feature B)
###############################################################################

print_section "SECTION 5: INTEREST CALCULATION"

print_test "Calculate Interest (5% rate, 30 days)" \
    "GET $BASE_URL/accounts/ACC-12345/interest?rate=0.05&days=30"
execute_curl "curl -s -X GET '$BASE_URL/accounts/ACC-12345/interest?rate=0.05&days=30'"

print_test "Calculate Interest (3.5% rate, 90 days)" \
    "GET $BASE_URL/accounts/ACC-67890/interest?rate=0.035&days=90"
execute_curl "curl -s -X GET '$BASE_URL/accounts/ACC-67890/interest?rate=0.035&days=90'"

print_test "Calculate Interest (2% rate, 365 days)" \
    "GET $BASE_URL/accounts/ACC-11111/interest?rate=0.02&days=365"
execute_curl "curl -s -X GET '$BASE_URL/accounts/ACC-11111/interest?rate=0.02&days=365'"

print_test "Calculate Interest (10% rate, 7 days)" \
    "GET $BASE_URL/accounts/ACC-12345/interest?rate=0.10&days=7"
execute_curl "curl -s -X GET '$BASE_URL/accounts/ACC-12345/interest?rate=0.10&days=7'"

###############################################################################
# SECTION 6: CSV EXPORT (Task 4 Feature C)
###############################################################################

print_section "SECTION 6: CSV EXPORT"

print_test "Export All Transactions to CSV" \
    "GET $BASE_URL/transactions/export?format=csv"
echo -e "${MAGENTA}→ Executing...${NC}"
echo -e "${GREEN}✓ Response (first 20 lines):${NC}"
curl -s -X GET "$BASE_URL/transactions/export?format=csv" | head -n 20
echo ""
echo -e "${YELLOW}(Output truncated for readability)${NC}"
echo ""
sleep 1

print_test "Export Filtered Transactions to CSV (by Account)" \
    "GET $BASE_URL/transactions/export?format=csv&accountId=ACC-12345"
echo -e "${MAGENTA}→ Executing...${NC}"
echo -e "${GREEN}✓ Response:${NC}"
curl -s -X GET "$BASE_URL/transactions/export?format=csv&accountId=ACC-12345"
echo ""
sleep 1

print_test "Export with Invalid Format (should return error)" \
    "GET $BASE_URL/transactions/export?format=json"
execute_curl "curl -s -X GET '$BASE_URL/transactions/export?format=json'"

###############################################################################
# SECTION 7: VALIDATION TESTS
###############################################################################

print_section "SECTION 7: VALIDATION TESTS"

print_test "Invalid Amount (Negative) - Should Return 400" \
    "POST $BASE_URL/transactions"
execute_curl "curl -s -X POST '$BASE_URL/transactions' \
  -H 'Content-Type: application/json' \
  -d '{
    \"type\": \"deposit\",
    \"toAccount\": \"ACC-12345\",
    \"amount\": -100.00,
    \"currency\": \"USD\"
  }'"

print_test "Invalid Amount (Too Many Decimals) - Should Return 400" \
    "POST $BASE_URL/transactions"
execute_curl "curl -s -X POST '$BASE_URL/transactions' \
  -H 'Content-Type: application/json' \
  -d '{
    \"type\": \"deposit\",
    \"toAccount\": \"ACC-12345\",
    \"amount\": 100.123,
    \"currency\": \"USD\"
  }'"

print_test "Invalid Account Number Format - Should Return 400" \
    "POST $BASE_URL/transactions"
execute_curl "curl -s -X POST '$BASE_URL/transactions' \
  -H 'Content-Type: application/json' \
  -d '{
    \"type\": \"deposit\",
    \"toAccount\": \"INVALID123\",
    \"amount\": 100.00,
    \"currency\": \"USD\"
  }'"

print_test "Invalid Currency Code - Should Return 400" \
    "POST $BASE_URL/transactions"
execute_curl "curl -s -X POST '$BASE_URL/transactions' \
  -H 'Content-Type: application/json' \
  -d '{
    \"type\": \"deposit\",
    \"toAccount\": \"ACC-12345\",
    \"amount\": 100.00,
    \"currency\": \"INVALID\"
  }'"

print_test "Invalid Transaction Type - Should Return 400" \
    "POST $BASE_URL/transactions"
execute_curl "curl -s -X POST '$BASE_URL/transactions' \
  -H 'Content-Type: application/json' \
  -d '{
    \"type\": \"payment\",
    \"toAccount\": \"ACC-12345\",
    \"amount\": 100.00,
    \"currency\": \"USD\"
  }'"

print_test "Transfer with Same Accounts - Should Return 400" \
    "POST $BASE_URL/transactions"
execute_curl "curl -s -X POST '$BASE_URL/transactions' \
  -H 'Content-Type: application/json' \
  -d '{
    \"type\": \"transfer\",
    \"fromAccount\": \"ACC-12345\",
    \"toAccount\": \"ACC-12345\",
    \"amount\": 100.00,
    \"currency\": \"USD\"
  }'"

###############################################################################
# SECTION 8: ERROR HANDLING
###############################################################################

print_section "SECTION 8: ERROR HANDLING"

print_test "Get Non-Existent Transaction - Should Return 404" \
    "GET $BASE_URL/transactions/00000000-0000-0000-0000-000000000000"
execute_curl "curl -s -X GET '$BASE_URL/transactions/00000000-0000-0000-0000-000000000000'"

print_test "Calculate Interest with Negative Rate - Should Return 400" \
    "GET $BASE_URL/accounts/ACC-12345/interest?rate=-0.05&days=30"
execute_curl "curl -s -X GET '$BASE_URL/accounts/ACC-12345/interest?rate=-0.05&days=30'"

print_test "Calculate Interest with Zero Days - Should Return 400" \
    "GET $BASE_URL/accounts/ACC-12345/interest?rate=0.05&days=0"
execute_curl "curl -s -X GET '$BASE_URL/accounts/ACC-12345/interest?rate=0.05&days=0'"

###############################################################################
# SECTION 9: RATE LIMITING (Task 4 Feature D)
###############################################################################

print_section "SECTION 9: RATE LIMITING"

print_test "Check Rate Limit Headers" \
    "GET $BASE_URL/transactions (with verbose output)"
echo -e "${MAGENTA}→ Executing with -i to show headers...${NC}"
echo -e "${GREEN}✓ Response headers:${NC}"
curl -s -i -X GET "$BASE_URL/transactions" | grep -E "HTTP|X-RateLimit"
echo ""
echo -e "${YELLOW}Look for: X-RateLimit-Limit, X-RateLimit-Remaining, X-RateLimit-Reset${NC}"
echo ""

###############################################################################
# SUMMARY
###############################################################################

print_section "TEST SUMMARY"

echo -e "${GREEN}✓ Completed $TEST_COUNT tests${NC}"
echo ""
echo -e "${YELLOW}What was tested:${NC}"
echo -e "  • Transaction creation (deposit, withdrawal, transfer)"
echo -e "  • Transaction retrieval (all, by ID, filtered)"
echo -e "  • Account balance calculation"
echo -e "  • Transaction summary endpoint"
echo -e "  • Interest calculation"
echo -e "  • CSV export functionality"
echo -e "  • Validation (amounts, accounts, currencies, types)"
echo -e "  • Error handling (404, 400, business rules)"
echo -e "  • Rate limiting headers"
echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════════════════${NC}"
echo -e "${GREEN}All sample requests completed successfully!${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════════${NC}"
echo ""
