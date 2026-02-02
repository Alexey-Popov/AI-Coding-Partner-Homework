@echo off
echo Building and running Banking Transactions API...
cd /d "%~dp0.."
call gradle run
pause
