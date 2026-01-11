@echo off
echo Setting JAVA_HOME to Android Studio's JBR...
set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo JAVA_HOME is now: %JAVA_HOME%
java -version

echo.
echo Cleaning project...
call gradlew clean

echo.
echo Building Debug APK...
call gradlew assembleDebug

echo.
echo If build was successful, you can install it using:
echo adb install -r app\build\outputs\apk\debug\app-debug.apk
pause