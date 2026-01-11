@echo off
echo Trying different passwords for the keystore...
echo.

set KEYTOOL=C:\Progra~1\Java\jdk-25\bin\keytool.exe

echo Trying password: chipper
%KEYTOOL% -list -v -keystore sparkifire-release.jks -alias key0 -storepass chipper -keypass chipper 2>nul | findstr "SHA1"
if %ERRORLEVEL%==0 goto :found

echo Trying password: android
%KEYTOOL% -list -v -keystore sparkifire-release.jks -alias key0 -storepass android -keypass android 2>nul | findstr "SHA1"
if %ERRORLEVEL%==0 goto :found

echo Trying password: sparkifire
%KEYTOOL% -list -v -keystore sparkifire-release.jks -alias key0 -storepass sparkifire -keypass sparkifire 2>nul | findstr "SHA1"
if %ERRORLEVEL%==0 goto :found

echo Trying password: password
%KEYTOOL% -list -v -keystore sparkifire-release.jks -alias key0 -storepass password -keypass password 2>nul | findstr "SHA1"
if %ERRORLEVEL%==0 goto :found

echo Trying password: (blank)
%KEYTOOL% -list -v -keystore sparkifire-release.jks -alias key0 -storepass "" -keypass "" 2>nul | findstr "SHA1"
if %ERRORLEVEL%==0 goto :found

echo.
echo None of the common passwords worked.
echo You need to remember the password you used when creating the keystore.
echo.
pause
exit /b 1

:found
echo.
echo ========================================
echo SUCCESS! Found the correct password!
echo ========================================
pause
