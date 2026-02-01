@echo off
REM Banking Transactions API - Run Script (Windows)
REM This script starts the FastAPI server with auto-reload enabled

echo =========================================
echo   Banking Transactions API
echo =========================================
echo.
echo Starting the server...
echo.

REM Check if virtual environment exists
if not exist "venv\" (
    echo Virtual environment not found!
    echo Please run the following commands first:
    echo   python -m venv venv
    echo   venv\Scripts\activate
    echo   pip install -r requirements.txt
    exit /b 1
)

REM Activate virtual environment
echo Activating virtual environment...
call venv\Scripts\activate

REM Change to project root directory (parent of demo/)
cd /d "%~dp0\.."

REM Start the server
echo.
echo Starting FastAPI server on http://localhost:8000
echo.
echo Available endpoints:
echo   - API Documentation (Swagger): http://localhost:8000/docs
echo   - API Documentation (ReDoc):   http://localhost:8000/redoc
echo   - API Root:                    http://localhost:8000/
echo   - Health Check:                http://localhost:8000/health
echo.
echo Press CTRL+C to stop the server
echo =========================================
echo.

REM Run uvicorn with auto-reload
cd src
uvicorn main:app --reload --host 0.0.0.0 --port 8000
