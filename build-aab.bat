@echo off
cd /d C:\Users\Jerry\AndroidStudioProjects\SparkiFire
echo Building release AAB...
call gradlew bundleRelease
if %ERRORLEVEL% EQU 0 (
    echo Build completed successfully
    echo AAB location: app\release\app-release.aab
) else (
    echo Build failed with error code %ERRORLEVEL%
)
pause
