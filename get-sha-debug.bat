@echo off
echo ========================================
echo Getting SHA-1 from Release Keystore
echo ========================================
echo.

set KEYTOOL="C:\Program Files\Java\jdk-25\bin\keytool.exe"
set KEYSTORE=sparkifire-release.jks

echo Using keytool: %KEYTOOL%
echo Keystore: %KEYSTORE%
echo.

echo Running keytool command...
%KEYTOOL% -list -v -keystore %KEYSTORE% -alias key0 -storepass chipper -keypass chipper

echo.
echo ========================================
echo Done
echo ========================================
pause
