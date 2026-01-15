@echo off
echo ========================================
echo SparkiFire Quicker Build & Run (Debug)
echo ========================================
echo.

REM Set JAVA_HOME to Android Studio's embedded JDK if it exists
if exist "C:\Program Files\Android\Android Studio\jbr" (
    set JAVA_HOME=C:\Program Files\Android\Android Studio\jbr
    set PATH=%JAVA_HOME%\bin;%PATH%
)

echo Using Java from: %JAVA_HOME%
echo.

echo Step 1: Building and Installing Debug APK...
echo (Skipping Lint, Tests, and R8/ProGuard)
echo.

call gradlew.bat installDebug -x lint -x test -x lintVitalRelease -x testDebugUnitTest -x testReleaseUnitTest

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo !!!!!!!!! BUILD FAILED !!!!!!!!!
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo Step 2: Launching App on Emulator/Device...
adb shell am start -n com.sparkiai.app/.MainActivity

echo.
echo ========================================
echo DONE! The app should be running now.
echo ========================================
echo.
pause
