@echo off
REM Banking Transaction API - Populate Test Data
REM Creates 10 random transactions for testing

setlocal enabledelayedexpansion

set BASE_URL=http://localhost:3000

echo.
echo ========================================================
echo Banking Transaction API - Populating Test Data
echo ========================================================
echo Creating 10 random transactions...
echo.

REM Wait for server to be ready
timeout /t 1 /nobreak >nul

REM Transaction 1: Deposit USD to ACC-12345
echo [1/10] Creating deposit transaction (USD 1000 to ACC-12345)...
curl -s -X POST %BASE_URL%/transactions ^
  -H "Content-Type: application/json" ^
  -d "{\"toAccount\":\"ACC-12345\",\"amount\":1000.00,\"currency\":\"USD\",\"type\":\"deposit\"}"
echo.

REM Transaction 2: Deposit EUR to ACC-67890
echo [2/10] Creating deposit transaction (EUR 500 to ACC-67890)...
curl -s -X POST %BASE_URL%/transactions ^
  -H "Content-Type: application/json" ^
  -d "{\"toAccount\":\"ACC-67890\",\"amount\":500.00,\"currency\":\"EUR\",\"type\":\"deposit\"}"
echo.

REM Transaction 3: Transfer from ACC-12345 to ACC-67890
echo [3/10] Creating transfer transaction (USD 200 from ACC-12345 to ACC-67890)...
curl -s -X POST %BASE_URL%/transactions ^
  -H "Content-Type: application/json" ^
  -d "{\"fromAccount\":\"ACC-12345\",\"toAccount\":\"ACC-67890\",\"amount\":200.00,\"currency\":\"USD\",\"type\":\"transfer\"}"
echo.

REM Transaction 4: Withdrawal from ACC-12345
echo [4/10] Creating withdrawal transaction (USD 150 from ACC-12345)...
curl -s -X POST %BASE_URL%/transactions ^
  -H "Content-Type: application/json" ^
  -d "{\"fromAccount\":\"ACC-12345\",\"amount\":150.00,\"currency\":\"USD\",\"type\":\"withdrawal\"}"
echo.

REM Transaction 5: Deposit GBP to ACC-ABC12
echo [5/10] Creating deposit transaction (GBP 750 to ACC-ABC12)...
curl -s -X POST %BASE_URL%/transactions ^
  -H "Content-Type: application/json" ^
  -d "{\"toAccount\":\"ACC-ABC12\",\"amount\":750.50,\"currency\":\"GBP\",\"type\":\"deposit\"}"
echo.

REM Transaction 6: Transfer from ACC-67890 to ACC-12345
echo [6/10] Creating transfer transaction (EUR 100 from ACC-67890 to ACC-12345)...
curl -s -X POST %BASE_URL%/transactions ^
  -H "Content-Type: application/json" ^
  -d "{\"fromAccount\":\"ACC-67890\",\"toAccount\":\"ACC-12345\",\"amount\":100.00,\"currency\":\"EUR\",\"type\":\"transfer\"}"
echo.

REM Transaction 7: Deposit JPY to ACC-XYZ99
echo [7/10] Creating deposit transaction (JPY 50000 to ACC-XYZ99)...
curl -s -X POST %BASE_URL%/transactions ^
  -H "Content-Type: application/json" ^
  -d "{\"toAccount\":\"ACC-XYZ99\",\"amount\":50000.00,\"currency\":\"JPY\",\"type\":\"deposit\"}"
echo.

REM Transaction 8: Withdrawal from ACC-ABC12
echo [8/10] Creating withdrawal transaction (GBP 250 from ACC-ABC12)...
curl -s -X POST %BASE_URL%/transactions ^
  -H "Content-Type: application/json" ^
  -d "{\"fromAccount\":\"ACC-ABC12\",\"amount\":250.00,\"currency\":\"GBP\",\"type\":\"withdrawal\"}"
echo.

REM Transaction 9: Transfer from ACC-12345 to ACC-ABC12
echo [9/10] Creating transfer transaction (USD 75.50 from ACC-12345 to ACC-ABC12)...
curl -s -X POST %BASE_URL%/transactions ^
  -H "Content-Type: application/json" ^
  -d "{\"fromAccount\":\"ACC-12345\",\"toAccount\":\"ACC-ABC12\",\"amount\":75.50,\"currency\":\"USD\",\"type\":\"transfer\"}"
echo.

REM Transaction 10: Deposit CAD to ACC-12345
echo [10/10] Creating deposit transaction (CAD 300.25 to ACC-12345)...
curl -s -X POST %BASE_URL%/transactions ^
  -H "Content-Type: application/json" ^
  -d "{\"toAccount\":\"ACC-12345\",\"amount\":300.25,\"currency\":\"CAD\",\"type\":\"deposit\"}"
echo.

echo.
echo ========================================================
echo ✓ Test data population complete!
echo ========================================================
echo.
echo Verifying transactions...
curl -s %BASE_URL%/transactions | findstr "count"
echo.
echo.
echo To view all transactions, visit: http://localhost:3000
echo Or run: curl http://localhost:3000/transactions
echo.

endlocal
