@echo off
echo ========================================
echo Getting SHA-1 from Release Keystore
echo ========================================
echo.

set KEYTOOL="C:\Program Files\Java\jdk-25\bin\keytool.exe"
set KEYSTORE=sparkifire-release.jks
set ALIAS=key0
set STOREPASS=chipper
set KEYPASS=chipper

echo Using keytool: %KEYTOOL%
echo.

%KEYTOOL% -list -v -keystore %KEYSTORE% -alias %ALIAS% -storepass %STOREPASS% -keypass %KEYPASS% | findstr /i "SHA1 SHA256"

echo.
echo ========================================
echo COPY THE SHA1 LINE ABOVE
echo ========================================
echo Package name: com.sparkiai.app
echo.
pause
