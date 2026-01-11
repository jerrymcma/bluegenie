@echo off
cd /d C:\Users\Jerry\AndroidStudioProjects\SparkiFire
echo Verifying keystore file...
keytool -list -v -keystore sparkifire-release.jks -storepass sparkifire123
pause
