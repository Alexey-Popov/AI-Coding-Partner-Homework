@echo off
REM Banking Transactions API - Start Script (Windows)
REM This script starts the Node.js server

echo ========================================
echo   Banking Transactions API - Startup
echo ========================================
echo.

REM Check if Node.js is installed
where node >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Node.js is not installed.
    echo Please install Node.js from https://nodejs.org/
    pause
    exit /b 1
)

echo Node.js version:
node --version
echo npm version:
npm --version
echo.

REM Check if we're in the correct directory
if not exist "package.json" (
    echo WARNING: package.json not found.
    echo Make sure you're running this script from the homework-1 directory.
    echo.
    pause
)

REM Check if node_modules exists
if not exist "node_modules" (
    echo Installing dependencies...
    call npm install
    if %ERRORLEVEL% NEQ 0 (
        echo Failed to install dependencies
        pause
        exit /b 1
    )
    echo.
)

REM Start the server
echo Starting the server...
echo Press Ctrl+C to stop
echo.

npm start
