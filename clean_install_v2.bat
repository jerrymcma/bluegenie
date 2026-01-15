@echo off
setlocal
cd /d "%~dp0"
echo ========================================
echo      Blue Genie Clean Install Tool v2
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

REM Setup ADB Path
set "ADB_PATH=adb"
set "SDK_ADB=C:\Users\Jerry\AppData\Local\Android\Sdk\platform-tools\adb.exe"

if exist "%SDK_ADB%" (
    echo [OK] Found ADB at: %SDK_ADB%
    set "ADB_PATH=%SDK_ADB%"
) else (
    echo [WARN] ADB not found in SDK path. relying on system PATH...
)

echo.
echo 2. Uninstalling Old Version...
echo ----------------------------------------
echo (It is okay if this fails if the app is not installed)
"%ADB_PATH%" uninstall com.bluegenie.app

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
"%ADB_PATH%" install -r "app\build\outputs\apk\debug\app-debug.apk"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo      INSTALLATION SUCCESSFUL!
    echo ========================================
    echo.
    echo Please open the app on your phone.
    echo You should see "Welcome to Blue Genie (v1.6)!"
) else (
    echo.
    echo [ERROR] Installation failed.
    echo Check if your phone is connected and USB Debugging is ON.
)

pause
