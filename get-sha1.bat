@echo off
echo ===================================
echo Getting SHA-1 Fingerprint
echo ===================================
echo.

echo Running gradlew signingReport...
echo.

call gradlew.bat signingReport

echo.
echo ===================================
echo INSTRUCTIONS:
echo ===================================
echo 1. Look for "Variant: debug" section above
echo 2. Copy the SHA1 fingerprint
echo 3. Go to Google Cloud Console
echo 4. Create Android OAuth client with:
echo    - Package: com.sparkiai.app
echo    - SHA-1: (paste the copied fingerprint)
echo.
echo Press any key to exit...
pause > nul
