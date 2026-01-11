@echo off
echo Getting SHA-1 fingerprint...
echo.

set JAVA_BIN=C:\Program Files\Common Files\Oracle\Java\javapath

if exist "%JAVA_BIN%\keytool.exe" (
    "%JAVA_BIN%\keytool.exe" -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android | findstr /i "SHA1 SHA256"
) else (
    echo keytool not found at: %JAVA_BIN%
    echo Trying to use keytool from PATH...
    keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android | findstr /i "SHA1 SHA256"
)

echo.
echo Copy the SHA1 fingerprint above and use it in Google Cloud Console
echo Package name: com.sparkiai.app
pause
