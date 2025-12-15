@echo off
echo ========================================
echo Deploying SparkiFire Web App to Vercel
echo ========================================
echo.

echo Step 1: Building project...
call npm run build
if errorlevel 1 (
    echo Build failed!
    pause
    exit /b 1
)

echo.
echo Step 2: Deploying to Vercel...
call npx vercel --prod

echo.
echo ========================================
echo Deployment Complete!
echo ========================================
echo.
echo Next steps:
echo 1. Go to https://vercel.com/dashboard
echo 2. Check Settings ^> Environment Variables
echo 3. Verify VITE_REPLICATE_API_KEY is set
echo 4. Test at https://sparkiai.app
echo.
pause
