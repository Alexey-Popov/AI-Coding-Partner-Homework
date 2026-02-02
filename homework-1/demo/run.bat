@echo off
echo Building and running Banking Transactions API...
cd /d "%~dp0.."
call gradlew.bat run
pause
