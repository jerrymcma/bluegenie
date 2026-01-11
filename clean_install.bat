@echo off
setlocal
cd /d "%~dp0"
echo ========================================
echo      SparkiFire Clean Install Tool
echo ========================================

echo.
echo 1. Checking Environment...
echo ----------------------------------------
set "JBR_PATH=C:\Program Files\Android\Android Studio\jbr"
if exist "%JBR_PATH%\bin\java.exe" (
    echo [OK] Found Android Studio JBR
    set "JAVA_HOME=%JBR_PATH%"
    set "PATH=%JBR_PATH%\bin;%PATH%"
) else (
    echo [WARN] JBR not found, relying on system PATH
)

echo.
echo 2. Uninstalling Old Version...
echo ----------------------------------------
echo (It is okay if this fails if the app is not installed)
adb uninstall com.sparkiai.app

echo.
echo 3. Cleaning Project...
echo ----------------------------------------
if exist app\build\outputs\apk\debug\app-debug.apk (
    del app\build\outputs\apk\debug\app-debug.apk
)
call gradlew.bat clean

echo.
echo 4. Building New Version...
echo ----------------------------------------
call gradlew.bat assembleDebug

echo.
echo 5. Verifying Build...
echo ----------------------------------------
if exist "app\build\outputs\apk\debug\app-debug.apk" (
    echo [SUCCESS] APK generated successfully!
) else (
    echo [ERROR] APK not found! Build failed.
    pause
    exit /b 1
)

echo.
echo 6. Installing to Device...
echo ----------------------------------------
echo Make sure your phone is connected and unlocked!
adb install -r "app\build\outputs\apk\debug\app-debug.apk"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo      INSTALLATION SUCCESSFUL!
    echo ========================================
    echo.
    echo Please open the app on your phone.
    echo You should see "Welcome to Sparki AI (v1.6)!"
) else (
    echo.
    echo [ERROR] Installation failed.
    echo Check if your phone is connected and USB Debugging is ON.
)

pause
