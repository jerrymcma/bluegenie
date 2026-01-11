@echo off
setlocal

REM Unset JAVA_HOME
set JAVA_HOME=

echo Building SparkiFire AAB (Version 40 / 1.0.7)...
echo.

cd /d C:\Users\Jerry\AndroidStudioProjects\SparkiFire

call gradlew clean bundleRelease

if %ERRORLEVEL% EQU 0 (
    echo.
    echo [OK] Build completed successfully
    echo.
    
    if not exist sparki-releases mkdir sparki-releases
    if not exist %USERPROFILE%\Desktop\sparki-releases mkdir %USERPROFILE%\Desktop\sparki-releases
    
    if exist app\build\outputs\bundle\release\app-release.aab (
        echo [OK] AAB file created
        for %%A in (app\build\outputs\bundle\release\app-release.aab) do echo Size: %%~zA bytes
        
        copy app\build\outputs\bundle\release\app-release.aab sparki-releases\sparkifire-v40-1.0.7.aab
        copy app\build\outputs\bundle\release\app-release.aab %USERPROFILE%\Desktop\sparki-releases\sparkifire-v40-1.0.7.aab
        
        echo.
        echo Files available at:
        echo   - sparki-releases\sparkifire-v40-1.0.7.aab
        echo   - %USERPROFILE%\Desktop\sparki-releases\sparkifire-v40-1.0.7.aab
        echo.
        echo Ready to upload to Google Play Console!
    ) else (
        echo [ERROR] AAB file not found
        exit /b 1
    )
) else (
    echo [ERROR] Build failed
    exit /b 1
)

endlocal
