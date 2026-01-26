@echo off
echo ============================================
echo    Banking Transactions API - Test Script
echo ============================================
echo.

set BASE_URL=http://localhost:3000

echo [1] Health Check
echo ----------------
curl -s %BASE_URL%/api/health
echo.
echo.

echo [2] Create Deposit Transaction
echo ------------------------------
curl -s -X POST %BASE_URL%/transactions -H "Content-Type: application/json" -d "{\"fromAccount\":\"ACC-00000\",\"toAccount\":\"ACC-12345\",\"amount\":1000,\"currency\":\"USD\",\"type\":\"deposit\"}"
echo.
echo.

echo [3] Create Transfer Transaction
echo -------------------------------
curl -s -X POST %BASE_URL%/transactions -H "Content-Type: application/json" -d "{\"fromAccount\":\"ACC-12345\",\"toAccount\":\"ACC-67890\",\"amount\":250.50,\"currency\":\"USD\",\"type\":\"transfer\"}"
echo.
echo.

echo [4] Create Withdrawal Transaction
echo ---------------------------------
curl -s -X POST %BASE_URL%/transactions -H "Content-Type: application/json" -d "{\"fromAccount\":\"ACC-12345\",\"toAccount\":\"ACC-00000\",\"amount\":100,\"currency\":\"EUR\",\"type\":\"withdrawal\"}"
echo.
echo.

echo [5] Get All Transactions
echo ------------------------
curl -s %BASE_URL%/transactions
echo.
echo.

echo [6] Get Transaction by ID (TXN-000001)
echo --------------------------------------
curl -s %BASE_URL%/transactions/TXN-000001
echo.
echo.

echo [7] Get Account Balance (ACC-12345)
echo -----------------------------------
curl -s %BASE_URL%/accounts/ACC-12345/balance
echo.
echo.

echo [8] Get Account Balance (ACC-67890)
echo -----------------------------------
curl -s %BASE_URL%/accounts/ACC-67890/balance
echo.
echo.

echo [9] Filter by Account ID
echo ------------------------
curl -s "%BASE_URL%/transactions?accountId=ACC-12345"
echo.
echo.

echo [10] Filter by Type (transfer)
echo ------------------------------
curl -s "%BASE_URL%/transactions?type=transfer"
echo.
echo.

echo [11] Test Validation - Invalid Amount (negative)
echo ------------------------------------------------
curl -s -X POST %BASE_URL%/transactions -H "Content-Type: application/json" -d "{\"fromAccount\":\"ACC-12345\",\"toAccount\":\"ACC-67890\",\"amount\":-50,\"currency\":\"USD\",\"type\":\"transfer\"}"
echo.
echo.

echo [12] Test Validation - Invalid Account Format
echo ----------------------------------------------
curl -s -X POST %BASE_URL%/transactions -H "Content-Type: application/json" -d "{\"fromAccount\":\"INVALID\",\"toAccount\":\"ACC-67890\",\"amount\":100,\"currency\":\"USD\",\"type\":\"transfer\"}"
echo.
echo.

echo [13] Test Validation - Invalid Currency
echo ---------------------------------------
curl -s -X POST %BASE_URL%/transactions -H "Content-Type: application/json" -d "{\"fromAccount\":\"ACC-12345\",\"toAccount\":\"ACC-67890\",\"amount\":100,\"currency\":\"FAKE\",\"type\":\"transfer\"}"
echo.
echo.

echo [14] Export CSV
echo ---------------
curl -s "%BASE_URL%/transactions/export?format=csv"
echo.
echo.

echo ============================================
echo    All tests completed!
echo ============================================
pause
