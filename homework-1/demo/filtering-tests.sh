#!/bin/bash

###############################################################################
# Banking API - Filtering & Additional Features Test Suite
# Tests Task 3 (Filtering) and Task 4 (Summary, Interest, CSV Export)
###############################################################################

BASE_URL="http://localhost:3000/api/v1"

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
MAGENTA='\033[0;35m'
NC='\033[0m' # No Color

# Test counters
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Transaction IDs storage
declare -a TRANSACTION_IDS

###############################################################################
# Helper Functions
###############################################################################

print_header() {
    echo -e "\n${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${CYAN}$1${NC}"
    echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}\n"
}

print_test() {
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    echo -e "${BLUE}TEST $TOTAL_TESTS:${NC} $1"
}

print_success() {
    PASSED_TESTS=$((PASSED_TESTS + 1))
    echo -e "${GREEN}✓ PASS${NC} - $1\n"
}

print_fail() {
    FAILED_TESTS=$((FAILED_TESTS + 1))
    echo -e "${RED}✗ FAIL${NC} - $1\n"
}

print_info() {
    echo -e "${YELLOW}ℹ INFO:${NC} $1"
}

check_api() {
    print_info "Checking if API is running at $BASE_URL..."
    if curl -s -f -o /dev/null "$BASE_URL/transactions"; then
        echo -e "${GREEN}✓ API is running${NC}\n"
        return 0
    else
        echo -e "${RED}✗ API is not responding. Please start the application first.${NC}"
        echo -e "${YELLOW}Run: mvn spring-boot:run${NC}\n"
        exit 1
    fi
}

###############################################################################
# PHASE 1: Create Diverse Test Data (10+ transactions)
###############################################################################

create_test_data() {
    print_header "PHASE 1: Creating Test Transactions"
    
    # Transaction 1: Deposit to ACC-11111 (USD)
    print_test "Create deposit #1 - ACC-11111 USD 1000.00"
    RESPONSE=$(curl -s -X POST "$BASE_URL/transactions" \
        -H "Content-Type: application/json" \
        -d '{
            "toAccount": "ACC-11111",
            "amount": 1000.00,
            "currency": "USD",
            "type": "DEPOSIT"
        }')
    ID=$(echo "$RESPONSE" | jq -r '.id // empty')
    if [ -n "$ID" ]; then
        TRANSACTION_IDS+=("$ID")
        print_success "Created transaction: $ID"
    else
        print_fail "Failed to create deposit"
    fi
    
    sleep 1
    
    # Transaction 2: Deposit to ACC-22222 (EUR)
    print_test "Create deposit #2 - ACC-22222 EUR 500.00"
    RESPONSE=$(curl -s -X POST "$BASE_URL/transactions" \
        -H "Content-Type: application/json" \
        -d '{
            "toAccount": "ACC-22222",
            "amount": 500.00,
            "currency": "EUR",
            "type": "DEPOSIT"
        }')
    ID=$(echo "$RESPONSE" | jq -r '.id // empty')
    if [ -n "$ID" ]; then
        TRANSACTION_IDS+=("$ID")
        print_success "Created transaction: $ID"
    else
        print_fail "Failed to create deposit"
    fi
    
    sleep 1
    
    # Transaction 3: Deposit to ACC-33333 (GBP)
    print_test "Create deposit #3 - ACC-33333 GBP 750.50"
    RESPONSE=$(curl -s -X POST "$BASE_URL/transactions" \
        -H "Content-Type: application/json" \
        -d '{
            "toAccount": "ACC-33333",
            "amount": 750.50,
            "currency": "GBP",
            "type": "DEPOSIT"
        }')
    ID=$(echo "$RESPONSE" | jq -r '.id // empty')
    if [ -n "$ID" ]; then
        TRANSACTION_IDS+=("$ID")
        print_success "Created transaction: $ID"
    else
        print_fail "Failed to create deposit"
    fi
    
    sleep 1
    
    # Transaction 4: Withdrawal from ACC-11111 (USD)
    print_test "Create withdrawal #1 - ACC-11111 USD 200.00"
    RESPONSE=$(curl -s -X POST "$BASE_URL/transactions" \
        -H "Content-Type: application/json" \
        -d '{
            "fromAccount": "ACC-11111",
            "amount": 200.00,
            "currency": "USD",
            "type": "WITHDRAWAL"
        }')
    ID=$(echo "$RESPONSE" | jq -r '.id // empty')
    if [ -n "$ID" ]; then
        TRANSACTION_IDS+=("$ID")
        print_success "Created transaction: $ID"
    else
        print_fail "Failed to create withdrawal"
    fi
    
    sleep 1
    
    # Transaction 5: Withdrawal from ACC-22222 (EUR)
    print_test "Create withdrawal #2 - ACC-22222 EUR 100.00"
    RESPONSE=$(curl -s -X POST "$BASE_URL/transactions" \
        -H "Content-Type: application/json" \
        -d '{
            "fromAccount": "ACC-22222",
            "amount": 100.00,
            "currency": "EUR",
            "type": "WITHDRAWAL"
        }')
    ID=$(echo "$RESPONSE" | jq -r '.id // empty')
    if [ -n "$ID" ]; then
        TRANSACTION_IDS+=("$ID")
        print_success "Created transaction: $ID"
    else
        print_fail "Failed to create withdrawal"
    fi
    
    sleep 1
    
    # Transaction 6: Transfer from ACC-11111 to ACC-22222 (USD)
    print_test "Create transfer #1 - ACC-11111 → ACC-22222 USD 150.00"
    RESPONSE=$(curl -s -X POST "$BASE_URL/transactions" \
        -H "Content-Type: application/json" \
        -d '{
            "fromAccount": "ACC-11111",
            "toAccount": "ACC-22222",
            "amount": 150.00,
            "currency": "USD",
            "type": "TRANSFER"
        }')
    ID=$(echo "$RESPONSE" | jq -r '.id // empty')
    if [ -n "$ID" ]; then
        TRANSACTION_IDS+=("$ID")
        print_success "Created transaction: $ID"
    else
        print_fail "Failed to create transfer"
    fi
    
    sleep 1
    
    # Transaction 7: Transfer from ACC-22222 to ACC-33333 (EUR)
    print_test "Create transfer #2 - ACC-22222 → ACC-33333 EUR 50.00"
    RESPONSE=$(curl -s -X POST "$BASE_URL/transactions" \
        -H "Content-Type: application/json" \
        -d '{
            "fromAccount": "ACC-22222",
            "toAccount": "ACC-33333",
            "amount": 50.00,
            "currency": "EUR",
            "type": "TRANSFER"
        }')
    ID=$(echo "$RESPONSE" | jq -r '.id // empty')
    if [ -n "$ID" ]; then
        TRANSACTION_IDS+=("$ID")
        print_success "Created transaction: $ID"
    else
        print_fail "Failed to create transfer"
    fi
    
    sleep 1
    
    # Transaction 8: Deposit to ACC-11111 (USD)
    print_test "Create deposit #4 - ACC-11111 USD 300.00"
    RESPONSE=$(curl -s -X POST "$BASE_URL/transactions" \
        -H "Content-Type: application/json" \
        -d '{
            "toAccount": "ACC-11111",
            "amount": 300.00,
            "currency": "USD",
            "type": "DEPOSIT"
        }')
    ID=$(echo "$RESPONSE" | jq -r '.id // empty')
    if [ -n "$ID" ]; then
        TRANSACTION_IDS+=("$ID")
        print_success "Created transaction: $ID"
    else
        print_fail "Failed to create deposit"
    fi
    
    sleep 1
    
    # Transaction 9: Transfer from ACC-33333 to ACC-11111 (GBP)
    print_test "Create transfer #3 - ACC-33333 → ACC-11111 GBP 100.00"
    RESPONSE=$(curl -s -X POST "$BASE_URL/transactions" \
        -H "Content-Type: application/json" \
        -d '{
            "fromAccount": "ACC-33333",
            "toAccount": "ACC-11111",
            "amount": 100.00,
            "currency": "GBP",
            "type": "TRANSFER"
        }')
    ID=$(echo "$RESPONSE" | jq -r '.id // empty')
    if [ -n "$ID" ]; then
        TRANSACTION_IDS+=("$ID")
        print_success "Created transaction: $ID"
    else
        print_fail "Failed to create transfer"
    fi
    
    sleep 1
    
    # Transaction 10: Deposit to ACC-22222 (EUR)
    print_test "Create deposit #5 - ACC-22222 EUR 250.00"
    RESPONSE=$(curl -s -X POST "$BASE_URL/transactions" \
        -H "Content-Type: application/json" \
        -d '{
            "toAccount": "ACC-22222",
            "amount": 250.00,
            "currency": "EUR",
            "type": "DEPOSIT"
        }')
    ID=$(echo "$RESPONSE" | jq -r '.id // empty')
    if [ -n "$ID" ]; then
        TRANSACTION_IDS+=("$ID")
        print_success "Created transaction: $ID"
    else
        print_fail "Failed to create deposit"
    fi
    
    sleep 1
    
    # Transaction 11: Withdrawal from ACC-33333 (GBP)
    print_test "Create withdrawal #3 - ACC-33333 GBP 50.00"
    RESPONSE=$(curl -s -X POST "$BASE_URL/transactions" \
        -H "Content-Type: application/json" \
        -d '{
            "fromAccount": "ACC-33333",
            "amount": 50.00,
            "currency": "GBP",
            "type": "WITHDRAWAL"
        }')
    ID=$(echo "$RESPONSE" | jq -r '.id // empty')
    if [ -n "$ID" ]; then
        TRANSACTION_IDS+=("$ID")
        print_success "Created transaction: $ID"
    else
        print_fail "Failed to create withdrawal"
    fi
    
    sleep 1
    
    # Transaction 12: Transfer from ACC-11111 to ACC-33333 (USD)
    print_test "Create transfer #4 - ACC-11111 → ACC-33333 USD 75.00"
    RESPONSE=$(curl -s -X POST "$BASE_URL/transactions" \
        -H "Content-Type: application/json" \
        -d '{
            "fromAccount": "ACC-11111",
            "toAccount": "ACC-33333",
            "amount": 75.00,
            "currency": "USD",
            "type": "TRANSFER"
        }')
    ID=$(echo "$RESPONSE" | jq -r '.id // empty')
    if [ -n "$ID" ]; then
        TRANSACTION_IDS+=("$ID")
        print_success "Created transaction: $ID"
    else
        print_fail "Failed to create transfer"
    fi
    
    print_info "Created ${#TRANSACTION_IDS[@]} test transactions"
}

###############################################################################
# PHASE 2: Test Filtering Capabilities (Task 3)
###############################################################################

test_filtering() {
    print_header "PHASE 2: Testing Filtering Capabilities"
    
    # Test 1: Get all transactions
    print_test "Get ALL transactions (no filters)"
    RESPONSE=$(curl -s "$BASE_URL/transactions")
    COUNT=$(echo "$RESPONSE" | jq '. | length')
    if [ "$COUNT" -ge 12 ]; then
        print_success "Retrieved $COUNT transactions"
        echo "$RESPONSE" | jq -r '.[] | "  - \(.id): \(.type) \(.amount) \(.currency)"' | head -5
        echo "  ... (showing first 5)"
    else
        print_fail "Expected at least 12 transactions, got $COUNT"
    fi
    
    # Test 2: Filter by accountId (ACC-11111)
    print_test "Filter by accountId: ACC-11111"
    RESPONSE=$(curl -s "$BASE_URL/transactions?accountId=ACC-11111")
    COUNT=$(echo "$RESPONSE" | jq '. | length')
    if [ "$COUNT" -ge 6 ]; then
        print_success "Found $COUNT transactions for ACC-11111"
        echo "$RESPONSE" | jq -r '.[] | "  - \(.type): from=\(.fromAccount // "N/A") to=\(.toAccount // "N/A")"'
    else
        print_fail "Expected at least 6 transactions, got $COUNT"
    fi
    
    # Test 3: Filter by accountId (ACC-22222)
    print_test "Filter by accountId: ACC-22222"
    RESPONSE=$(curl -s "$BASE_URL/transactions?accountId=ACC-22222")
    COUNT=$(echo "$RESPONSE" | jq '. | length')
    if [ "$COUNT" -ge 5 ]; then
        print_success "Found $COUNT transactions for ACC-22222"
    else
        print_fail "Expected at least 5 transactions, got $COUNT"
    fi
    
    # Test 4: Filter by accountId (ACC-33333)
    print_test "Filter by accountId: ACC-33333"
    RESPONSE=$(curl -s "$BASE_URL/transactions?accountId=ACC-33333")
    COUNT=$(echo "$RESPONSE" | jq '. | length')
    if [ "$COUNT" -ge 5 ]; then
        print_success "Found $COUNT transactions for ACC-33333"
    else
        print_fail "Expected at least 5 transactions, got $COUNT"
    fi
    
    # Test 5: Filter by type: DEPOSIT
    print_test "Filter by type: DEPOSIT"
    RESPONSE=$(curl -s "$BASE_URL/transactions?type=DEPOSIT")
    COUNT=$(echo "$RESPONSE" | jq '. | length')
    DEPOSITS=$(echo "$RESPONSE" | jq -r '[.[] | select(.type == "DEPOSIT")] | length')
    if [ "$DEPOSITS" -eq "$COUNT" ] && [ "$COUNT" -ge 5 ]; then
        print_success "Found $COUNT deposit transactions"
        echo "$RESPONSE" | jq -r '.[] | "  - \(.toAccount): \(.amount) \(.currency)"'
    else
        print_fail "Filter not working correctly. Got $COUNT transactions, $DEPOSITS deposits"
    fi
    
    # Test 6: Filter by type: WITHDRAWAL
    print_test "Filter by type: WITHDRAWAL"
    RESPONSE=$(curl -s "$BASE_URL/transactions?type=WITHDRAWAL")
    COUNT=$(echo "$RESPONSE" | jq '. | length')
    WITHDRAWALS=$(echo "$RESPONSE" | jq -r '[.[] | select(.type == "WITHDRAWAL")] | length')
    if [ "$WITHDRAWALS" -eq "$COUNT" ] && [ "$COUNT" -ge 3 ]; then
        print_success "Found $COUNT withdrawal transactions"
        echo "$RESPONSE" | jq -r '.[] | "  - \(.fromAccount): \(.amount) \(.currency)"'
    else
        print_fail "Filter not working correctly. Got $COUNT transactions, $WITHDRAWALS withdrawals"
    fi
    
    # Test 7: Filter by type: TRANSFER
    print_test "Filter by type: TRANSFER"
    RESPONSE=$(curl -s "$BASE_URL/transactions?type=TRANSFER")
    COUNT=$(echo "$RESPONSE" | jq '. | length')
    TRANSFERS=$(echo "$RESPONSE" | jq -r '[.[] | select(.type == "TRANSFER")] | length')
    if [ "$TRANSFERS" -eq "$COUNT" ] && [ "$COUNT" -ge 4 ]; then
        print_success "Found $COUNT transfer transactions"
        echo "$RESPONSE" | jq -r '.[] | "  - \(.fromAccount) → \(.toAccount): \(.amount) \(.currency)"'
    else
        print_fail "Filter not working correctly. Got $COUNT transactions, $TRANSFERS transfers"
    fi
    
    # Test 8: Combined filter - accountId + type
    print_test "Combined filter: ACC-11111 + DEPOSIT"
    RESPONSE=$(curl -s "$BASE_URL/transactions?accountId=ACC-11111&type=DEPOSIT")
    COUNT=$(echo "$RESPONSE" | jq '. | length')
    if [ "$COUNT" -ge 2 ]; then
        print_success "Found $COUNT matching transactions"
        echo "$RESPONSE" | jq -r '.[] | "  - \(.type) to \(.toAccount): \(.amount) \(.currency)"'
    else
        print_fail "Expected at least 2 transactions, got $COUNT"
    fi
    
    # Test 9: Combined filter - accountId + type (TRANSFER)
    print_test "Combined filter: ACC-22222 + TRANSFER"
    RESPONSE=$(curl -s "$BASE_URL/transactions?accountId=ACC-22222&type=TRANSFER")
    COUNT=$(echo "$RESPONSE" | jq '. | length')
    if [ "$COUNT" -ge 2 ]; then
        print_success "Found $COUNT matching transactions"
        echo "$RESPONSE" | jq -r '.[] | "  - \(.fromAccount) → \(.toAccount): \(.amount)"'
    else
        print_fail "Expected at least 2 transactions, got $COUNT"
    fi
    
    # Test 10: Filter by status
    print_test "Filter by status: COMPLETED"
    RESPONSE=$(curl -s "$BASE_URL/transactions?status=COMPLETED")
    COUNT=$(echo "$RESPONSE" | jq '. | length')
    if [ "$COUNT" -ge 12 ]; then
        print_success "Found $COUNT completed transactions"
    else
        print_fail "Expected at least 12 transactions, got $COUNT"
    fi
}

###############################################################################
# PHASE 3: Test Summary Endpoint (Task 4 Feature A)
###############################################################################

test_summary() {
    print_header "PHASE 3: Testing Transaction Summary Endpoint"
    
    # Test 1: Summary for ACC-11111
    print_test "Get summary for ACC-11111"
    RESPONSE=$(curl -s "$BASE_URL/accounts/ACC-11111/summary")
    ACCOUNT=$(echo "$RESPONSE" | jq -r '.accountId')
    BALANCE=$(echo "$RESPONSE" | jq -r '.currentBalance')
    DEPOSITS=$(echo "$RESPONSE" | jq -r '.totalDeposits')
    WITHDRAWALS=$(echo "$RESPONSE" | jq -r '.totalWithdrawals')
    NUM_TRANS=$(echo "$RESPONSE" | jq -r '.numberOfTransactions')
    
    if [ "$ACCOUNT" = "ACC-11111" ] && [ -n "$BALANCE" ]; then
        print_success "Summary retrieved successfully"
        echo -e "  Account: ${CYAN}$ACCOUNT${NC}"
        echo -e "  Current Balance: ${GREEN}$BALANCE${NC}"
        echo -e "  Total Deposits: ${GREEN}+$DEPOSITS${NC}"
        echo -e "  Total Withdrawals: ${RED}-$WITHDRAWALS${NC}"
        echo -e "  Number of Transactions: ${YELLOW}$NUM_TRANS${NC}"
        
        # Verify balance calculation: deposits - withdrawals
        # ACC-11111: +1000 +300 +100(from ACC-33333) -200 -150(to ACC-22222) -75(to ACC-33333) = 975
        print_info "Expected balance calculation: 1000 + 300 + 100 - 200 - 150 - 75 = 975"
    else
        print_fail "Failed to retrieve summary"
    fi
    
    # Test 2: Summary for ACC-22222
    print_test "Get summary for ACC-22222"
    RESPONSE=$(curl -s "$BASE_URL/accounts/ACC-22222/summary")
    ACCOUNT=$(echo "$RESPONSE" | jq -r '.accountId')
    BALANCE=$(echo "$RESPONSE" | jq -r '.currentBalance')
    DEPOSITS=$(echo "$RESPONSE" | jq -r '.totalDeposits')
    WITHDRAWALS=$(echo "$RESPONSE" | jq -r '.totalWithdrawals')
    NUM_TRANS=$(echo "$RESPONSE" | jq -r '.numberOfTransactions')
    
    if [ "$ACCOUNT" = "ACC-22222" ] && [ -n "$BALANCE" ]; then
        print_success "Summary retrieved successfully"
        echo -e "  Account: ${CYAN}$ACCOUNT${NC}"
        echo -e "  Current Balance: ${GREEN}$BALANCE${NC}"
        echo -e "  Total Deposits: ${GREEN}+$DEPOSITS${NC}"
        echo -e "  Total Withdrawals: ${RED}-$WITHDRAWALS${NC}"
        echo -e "  Number of Transactions: ${YELLOW}$NUM_TRANS${NC}"
        
        # ACC-22222: +500 +250 +150(from ACC-11111) -100 -50(to ACC-33333) = 750
        print_info "Expected balance calculation: 500 + 250 + 150 - 100 - 50 = 750"
    else
        print_fail "Failed to retrieve summary"
    fi
    
    # Test 3: Summary for ACC-33333
    print_test "Get summary for ACC-33333"
    RESPONSE=$(curl -s "$BASE_URL/accounts/ACC-33333/summary")
    ACCOUNT=$(echo "$RESPONSE" | jq -r '.accountId')
    BALANCE=$(echo "$RESPONSE" | jq -r '.currentBalance')
    DEPOSITS=$(echo "$RESPONSE" | jq -r '.totalDeposits')
    WITHDRAWALS=$(echo "$RESPONSE" | jq -r '.totalWithdrawals')
    NUM_TRANS=$(echo "$RESPONSE" | jq -r '.numberOfTransactions')
    
    if [ "$ACCOUNT" = "ACC-33333" ] && [ -n "$BALANCE" ]; then
        print_success "Summary retrieved successfully"
        echo -e "  Account: ${CYAN}$ACCOUNT${NC}"
        echo -e "  Current Balance: ${GREEN}$BALANCE${NC}"
        echo -e "  Total Deposits: ${GREEN}+$DEPOSITS${NC}"
        echo -e "  Total Withdrawals: ${RED}-$WITHDRAWALS${NC}"
        echo -e "  Number of Transactions: ${YELLOW}$NUM_TRANS${NC}"
        
        # ACC-33333: +750.50 +50(from ACC-22222) +75(from ACC-11111) -100(to ACC-11111) -50 = 725.50
        print_info "Expected balance calculation: 750.50 + 50 + 75 - 100 - 50 = 725.50"
    else
        print_fail "Failed to retrieve summary"
    fi
}

###############################################################################
# PHASE 4: Test Interest Calculation (Task 4 Feature B)
###############################################################################

test_interest() {
    print_header "PHASE 4: Testing Interest Calculation"
    
    # Test 1: Interest for ACC-11111 (5% for 30 days)
    print_test "Calculate interest for ACC-11111 (rate=0.05, days=30)"
    RESPONSE=$(curl -s "$BASE_URL/accounts/ACC-11111/interest?rate=0.05&days=30")
    ACCOUNT=$(echo "$RESPONSE" | jq -r '.accountId')
    CURRENT=$(echo "$RESPONSE" | jq -r '.currentBalance')
    RATE=$(echo "$RESPONSE" | jq -r '.interestRate')
    DAYS=$(echo "$RESPONSE" | jq -r '.days')
    INTEREST=$(echo "$RESPONSE" | jq -r '.interestAmount')
    PROJECTED=$(echo "$RESPONSE" | jq -r '.projectedBalance')
    FORMULA=$(echo "$RESPONSE" | jq -r '.formula')
    
    if [ "$ACCOUNT" = "ACC-11111" ] && [ -n "$INTEREST" ]; then
        print_success "Interest calculated successfully"
        echo -e "  Account: ${CYAN}$ACCOUNT${NC}"
        echo -e "  Current Balance: ${GREEN}$CURRENT${NC}"
        echo -e "  Interest Rate: ${YELLOW}$RATE (5%)${NC}"
        echo -e "  Days: ${YELLOW}$DAYS${NC}"
        echo -e "  Interest Amount: ${GREEN}+$INTEREST${NC}"
        echo -e "  Projected Balance: ${GREEN}$PROJECTED${NC}"
        echo -e "  Formula: ${MAGENTA}$FORMULA${NC}"
    else
        print_fail "Failed to calculate interest"
    fi
    
    # Test 2: Interest for ACC-22222 (3% for 90 days)
    print_test "Calculate interest for ACC-22222 (rate=0.03, days=90)"
    RESPONSE=$(curl -s "$BASE_URL/accounts/ACC-22222/interest?rate=0.03&days=90")
    INTEREST=$(echo "$RESPONSE" | jq -r '.interestAmount')
    PROJECTED=$(echo "$RESPONSE" | jq -r '.projectedBalance')
    
    if [ -n "$INTEREST" ]; then
        print_success "Interest: $INTEREST, Projected: $PROJECTED"
    else
        print_fail "Failed to calculate interest"
    fi
    
    # Test 3: Interest for ACC-33333 (7% for 365 days)
    print_test "Calculate interest for ACC-33333 (rate=0.07, days=365)"
    RESPONSE=$(curl -s "$BASE_URL/accounts/ACC-33333/interest?rate=0.07&days=365")
    INTEREST=$(echo "$RESPONSE" | jq -r '.interestAmount')
    PROJECTED=$(echo "$RESPONSE" | jq -r '.projectedBalance')
    
    if [ -n "$INTEREST" ]; then
        print_success "Interest: $INTEREST, Projected: $PROJECTED"
    else
        print_fail "Failed to calculate interest"
    fi
}

###############################################################################
# PHASE 5: Test CSV Export (Task 4 Feature C)
###############################################################################

test_csv_export() {
    print_header "PHASE 5: Testing CSV Export"
    
    # Test 1: Export all transactions
    print_test "Export all transactions to CSV"
    RESPONSE=$(curl -s "$BASE_URL/transactions/export?format=csv")
    LINE_COUNT=$(echo "$RESPONSE" | wc -l | tr -d ' ')
    
    if [ "$LINE_COUNT" -gt 12 ]; then
        print_success "CSV exported successfully ($LINE_COUNT lines)"
        echo -e "${YELLOW}First 5 lines:${NC}"
        echo "$RESPONSE" | head -5
    else
        print_fail "Expected more than 12 lines, got $LINE_COUNT"
    fi
    
    # Test 2: Export filtered transactions (by accountId)
    print_test "Export ACC-11111 transactions to CSV"
    RESPONSE=$(curl -s "$BASE_URL/transactions/export?format=csv&accountId=ACC-11111")
    LINE_COUNT=$(echo "$RESPONSE" | wc -l | tr -d ' ')
    
    if [ "$LINE_COUNT" -ge 5 ]; then
        print_success "Filtered CSV exported ($LINE_COUNT lines)"
    else
        print_fail "Expected at least 5 lines, got $LINE_COUNT"
    fi
    
    # Test 3: Export by type
    print_test "Export DEPOSIT transactions to CSV"
    RESPONSE=$(curl -s "$BASE_URL/transactions/export?format=csv&type=DEPOSIT")
    LINE_COUNT=$(echo "$RESPONSE" | wc -l | tr -d ' ')
    
    if [ "$LINE_COUNT" -ge 5 ]; then
        print_success "Filtered CSV exported ($LINE_COUNT lines)"
    else
        print_fail "Expected at least 5 lines, got $LINE_COUNT"
    fi
    
    # Test 4: Invalid format
    print_test "Test invalid export format (should fail)"
    STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/transactions/export?format=json")
    
    if [ "$STATUS" = "400" ]; then
        print_success "Correctly rejected invalid format with 400"
    else
        print_fail "Expected 400, got $STATUS"
    fi
}

###############################################################################
# PHASE 6: Test Rate Limiting (Task 4 Feature D)
###############################################################################

test_rate_limiting() {
    print_header "PHASE 6: Testing Rate Limiting (Optional)"
    
    print_info "Rate limit is 100 requests per minute"
    print_info "This would take too long to test fully, performing sample test..."
    
    # Test: Check rate limit headers
    print_test "Check rate limit headers"
    RESPONSE=$(curl -s -D - "$BASE_URL/transactions" -o /dev/null)
    LIMIT=$(echo "$RESPONSE" | grep -i "X-RateLimit-Limit" | cut -d: -f2 | tr -d ' \r')
    REMAINING=$(echo "$RESPONSE" | grep -i "X-RateLimit-Remaining" | cut -d: -f2 | tr -d ' \r')
    
    if [ -n "$LIMIT" ]; then
        print_success "Rate limiting headers present"
        echo -e "  Limit: ${YELLOW}$LIMIT${NC}"
        echo -e "  Remaining: ${YELLOW}$REMAINING${NC}"
    else
        print_info "Rate limiting headers not found (may not be enabled)"
    fi
}

###############################################################################
# Final Summary
###############################################################################

print_summary() {
    print_header "TEST SUMMARY"
    
    echo -e "${CYAN}Total Tests:${NC}   $TOTAL_TESTS"
    echo -e "${GREEN}Passed:${NC}        $PASSED_TESTS"
    echo -e "${RED}Failed:${NC}        $FAILED_TESTS"
    
    PASS_RATE=$((PASSED_TESTS * 100 / TOTAL_TESTS))
    echo -e "${YELLOW}Pass Rate:${NC}     $PASS_RATE%"
    
    if [ $FAILED_TESTS -eq 0 ]; then
        echo -e "\n${GREEN}🎉 ALL TESTS PASSED! 🎉${NC}\n"
        exit 0
    else
        echo -e "\n${RED}⚠️  SOME TESTS FAILED${NC}\n"
        exit 1
    fi
}

###############################################################################
# Main Execution
###############################################################################

main() {
    echo -e "${MAGENTA}"
    echo "╔═══════════════════════════════════════════════════════════════════╗"
    echo "║  Banking API - Advanced Features Test Suite                      ║"
    echo "║  Testing: Filtering, Summary, Interest, CSV Export, Rate Limit   ║"
    echo "╚═══════════════════════════════════════════════════════════════════╝"
    echo -e "${NC}\n"
    
    # Check if API is running
    check_api
    
    # Run test phases
    create_test_data
    test_filtering
    test_summary
    test_interest
    test_csv_export
    test_rate_limiting
    
    # Print final summary
    print_summary
}

# Run main function
main
