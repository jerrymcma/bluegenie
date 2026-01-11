@echo off
echo ========================================
echo Getting SHA-1 from Release Keystore
echo ========================================
echo.

REM Get the directory where java.exe is located
for /f "tokens=*" %%i in ('where java 2^>nul') do set JAVA_EXE=%%i
if not defined JAVA_EXE (
    echo ERROR: Java not found in PATH
    pause
    exit /b 1
)

REM Get the directory containing java.exe
for %%i in ("%JAVA_EXE%") do set JAVA_BIN=%%~dpi

REM Try to find keytool in the same directory as java
set KEYTOOL=%JAVA_BIN%keytool.exe

if not exist "%KEYTOOL%" (
    echo Keytool not found at: %KEYTOOL%
    echo Trying system keytool...
    set KEYTOOL=keytool
)

echo Using: %KEYTOOL%
echo.

REM Extract SHA fingerprints from release keystore
"%KEYTOOL%" -list -v -keystore sparkifire-release.jks -alias key0 -storepass chipper -keypass chipper 2>nul | findstr /i "SHA1 SHA256"

if %ERRORLEVEL% neq 0 (
    echo.
    echo ERROR: Could not read keystore
    echo Make sure sparkifire-release.jks exists in this directory
)

echo.
echo ========================================
echo COPY THE SHA1 LINE ABOVE
echo ========================================
echo Package name: com.sparkiai.app
echo.
echo Next: Add this to Google Cloud Console
echo https://console.cloud.google.com/
echo.
pause
