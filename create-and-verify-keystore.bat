@echo off
setlocal enabledelayedexpansion

cd /d C:\Users\Jerry\AndroidStudioProjects\SparkiFire

echo ========================================
echo Creating SparkiFire Release Keystore
echo ========================================
echo.

REM Remove old keystore if it exists
if exist sparkifire-release.jks (
    echo Removing old keystore...
    del /f sparkifire-release.jks
)

REM Create new keystore using simple password
echo Creating new keystore with keytool...
keytool -genkey -v -keystore sparkifire-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias sparkifire -storepass sparkifire123 -keypass sparkifire123 -dname "CN=SparkiFire"

echo.
echo Verifying keystore creation...
if exist sparkifire-release.jks (
    echo Keystore file created successfully
    echo.
    echo Listing keystore contents:
    keytool -list -v -keystore sparkifire-release.jks -storepass sparkifire123
) else (
    echo ERROR: Keystore file was not created!
)

echo.
pause
