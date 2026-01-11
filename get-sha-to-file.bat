@echo off
"C:\Program Files\Java\jdk-25\bin\keytool.exe" -list -v -keystore sparkifire-release.jks -alias key0 -storepass chipper -keypass chipper > keystore-output.txt 2>&1
type keystore-output.txt
echo.
echo Output saved to keystore-output.txt
pause
