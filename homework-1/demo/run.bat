@echo off
REM ============================================
REM Banking Transactions API - Startup Script
REM ============================================

echo.
echo Starting Banking Transactions API...
echo.

REM Check if Maven is installed
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo Maven is not installed. Please install Maven first.
    echo Visit: https://maven.apache.org/install.html
    exit /b 1
)

REM Check if Java is installed
where java >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo Java is not installed. Please install Java 17 or higher.
    echo Visit: https://adoptium.net/
    exit /b 1
)

echo Maven found
echo Java found
echo.

REM Clean and build the project
echo Building the project...
call mvn clean package -DskipTests

if %ERRORLEVEL% EQU 0 (
    echo.
    echo Build successful!
    echo.
    echo Starting the application on http://localhost:3000/api/v1
    echo Health check: http://localhost:3000/api/v1/actuator/health
    echo.
    echo Once started, you can test the API using:
    echo    - demo/sample-requests.http (VS Code REST Client)
    echo    - demo/sample-requests.sh (curl commands)
    echo    - demo/validation-tests.sh (automated tests)
    echo    - demo/filtering-tests.sh (filtering tests)
    echo.
    echo Press Ctrl+C to stop the application
    echo.
    
    REM Run the application
    call mvn spring-boot:run
) else (
    echo.
    echo Build failed. Please check the errors above.
    exit /b 1
)
