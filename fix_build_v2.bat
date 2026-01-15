@echo off
setlocal
cd /d "%~dp0"
echo ========================================
echo      SparkiFire Build Fix ^& Diagnose
echo ========================================

echo.
echo 1. Checking Environment...
echo ----------------------------------------

REM Try to find Android Studio JBR
set "JBR_PATH=C:\Program Files\Android\Android Studio\jbr"
if exist "%JBR_PATH%\bin\java.exe" (
    echo [OK] Found Android Studio JBR at: "%JBR_PATH%"
    set "JAVA_HOME=%JBR_PATH%"
    set "PATH=%JBR_PATH%\bin;%PATH%"
) else (
    echo [WARN] Could not find Android Studio JBR at default location.
    echo        Trying to unset JAVA_HOME to let Gradle find Java...
    set "JAVA_HOME="
)

echo.
echo JAVA_HOME is now: "%JAVA_HOME%"
echo.
java -version
echo.

echo 2. Cleaning Project...
echo ----------------------------------------
if exist gradlew.bat (
    echo [OK] Found gradlew.bat
) else (
    echo [ERROR] gradlew.bat not found in %CD%
    goto :Error
)

call gradlew.bat clean
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Clean failed!
    goto :Error
)

echo.
echo 3. Building Debug APK...
echo ----------------------------------------
call gradlew.bat assembleDebug
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Build failed!
    goto :Error
)

echo.
echo ========================================
echo      BUILD SUCCESSFUL!  (v1.6)
echo ========================================
echo.
echo Please run the app on your device now.
echo.
echo If you have a device connected, I will try to install it...
call gradlew.bat installDebug
echo.
pause
exit /b 0

:Error
echo.
echo ========================================
echo      BUILD FAILED
echo ========================================
echo.
echo Please copy the error message above and share it.
pause
exit /b 1
