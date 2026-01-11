@echo off
echo Running signing report...
echo.

set JAVA_HOME=

cd /d "%~dp0"
call gradlew.bat signingReport --no-daemon

echo.
echo Done. Check output above for SHA1 and SHA256 fingerprints.
pause
