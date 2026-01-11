@echo off
setlocal

set KEYSTORE_FILE=sparkifire-release.jks
set KEYSTORE_PASS=chipper
set KEY_ALIAS=key0
set KEY_PASS=chipper

echo ===================================
echo Getting SHA-1 from Release Keystore
echo ===================================
echo.
echo Keystore: %KEYSTORE_FILE%
echo Alias: %KEY_ALIAS%
echo.

REM Try multiple Java locations
set KEYTOOL_FOUND=0

if exist "C:\Program Files\Common Files\Oracle\Java\javapath\keytool.exe" (
    set KEYTOOL="C:\Program Files\Common Files\Oracle\Java\javapath\keytool.exe"
    set KEYTOOL_FOUND=1
)

if %KEYTOOL_FOUND%==0 (
    REM Try to use keytool from PATH
    where keytool >nul 2>&1
    if %ERRORLEVEL%==0 (
        set KEYTOOL=keytool
        set KEYTOOL_FOUND=1
    )
)

if %KEYTOOL_FOUND%==0 (
    echo ERROR: keytool not found
    echo Please install Java JDK or add it to your PATH
    pause
    exit /b 1
)

echo Using keytool: %KEYTOOL%
echo.
echo Extracting SHA fingerprints...
echo.

%KEYTOOL% -list -v -keystore %KEYSTORE_FILE% -alias %KEY_ALIAS% -storepass %KEYSTORE_PASS% -keypass %KEY_PASS% | findstr /i "SHA1 SHA256"

echo.
echo ===================================
echo NEXT STEPS:
echo ===================================
echo 1. Copy the SHA1 fingerprint above
echo 2. Go to: https://console.cloud.google.com/
echo 3. Navigate to: APIs ^& Services ^> Credentials
echo 4. Create OAuth client ID ^> Android
echo 5. Enter:
echo    - Package name: com.sparkiai.app
echo    - SHA-1: (paste the SHA1 from above)
echo 6. Click CREATE
echo.
pause
