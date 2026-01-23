@echo off
REM Banking Transaction API - Test Script
REM This script will make sample requests to the API

setlocal enabledelayedexpansion

set BASE_URL=http://localhost:3000

echo.
echo ========================================
echo Banking Transaction API - Test Script
echo ========================================
echo.

REM Wait for server to be ready
timeout /t 2 /nobreak

echo Testing Health Check...
curl -s %BASE_URL%/health
echo.
echo.

echo Creating deposit transaction...
curl -s -X POST %BASE_URL%/transactions ^
  -H "Content-Type: application/json" ^
  -d "{\"toAccount\": \"ACC-12345\", \"amount\": 1000.50, \"currency\": \"USD\", \"type\": \"deposit\"}"
echo.
echo.

echo Creating withdrawal transaction...
curl -s -X POST %BASE_URL%/transactions ^
  -H "Content-Type: application/json" ^
  -d "{\"fromAccount\": \"ACC-12345\", \"amount\": 250, \"currency\": \"USD\", \"type\": \"withdrawal\"}"
echo.
echo.

echo Listing all transactions...
curl -s %BASE_URL%/transactions
echo.
echo.

echo Getting account balance...
curl -s %BASE_URL%/accounts/ACC-12345/balance
echo.
echo.

echo Getting transaction summary...
curl -s %BASE_URL%/summary
echo.
echo.
