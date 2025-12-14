@echo off
echo ========================================
echo   SparkiFire Web - Starting Server
echo ========================================
echo.

cd /d "%~dp0"

echo Checking if node_modules exists...
if not exist "node_modules\" (
    echo Installing dependencies...
    call npm install
    echo.
)

echo Starting development server...
echo.
echo The app will open in your browser at:
echo http://localhost:3000
echo.
echo If you have firewall issues:
echo 1. Allow Node.js through Windows Firewall
echo 2. Or try accessing via: http://127.0.0.1:3000
echo 3. Check README.md for detailed solutions
echo.
echo Press Ctrl+C to stop the server
echo ========================================
echo.

call npm run dev

pause
