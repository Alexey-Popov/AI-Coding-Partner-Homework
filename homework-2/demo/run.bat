@echo off
REM Run Customer Support Ticket System API Server (Windows)

REM Change to parent directory (homework-2) so relative paths resolve
pushd ..

echo Installing dependencies (using python -m pip)...
python -m pip install -r requirements.txt

echo.
echo Starting server on http://localhost:8000
echo API docs available at http://localhost:8000/docs
echo.

REM Use python -m uvicorn to avoid relying on PATH
python -m uvicorn src.main:app --reload --host 0.0.0.0 --port 8000

popd
