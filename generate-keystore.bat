@echo off
cd /d C:\Users\Jerry\AndroidStudioProjects\SparkiFire
keytool -genkey -v -keystore sparkifire-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias sparkifire -storepass sparkifire123 -keypass sparkifire123 -dname "CN=SparkiFire"
echo Keystore generation complete
pause
