@echo off
setlocal

REM Set correct JAVA_HOME
set "JAVA_HOME=C:\Program Files\Java\jdk-21.0.9+10"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo Building SparkiFire AAB...
echo Using JAVA_HOME: %JAVA_HOME%
echo.

REM Clean and build
call gradlew.bat clean bundleRelease

if %ERRORLEVEL% EQU 0 (
    echo.
    echo Build successful!
    
    REM Create release directory
    if not exist sparki-releases mkdir sparki-releases
    if not exist %USERPROFILE%\Desktop\sparki-releases mkdir %USERPROFILE%\Desktop\sparki-releases
    
    REM Copy AAB file
    if exist app\build\outputs\bundle\release\app-release.aab (
        copy app\build\outputs\bundle\release\app-release.aab sparki-releases\sparkifire-v40-1.0.7.aab
        copy app\build\outputs\bundle\release\app-release.aab %USERPROFILE%\Desktop\sparki-releases\sparkifire-v40-1.0.7.aab
        echo.
        echo AAB files copied to:
        echo   - sparki-releases\sparkifire-v40-1.0.7.aab
        echo   - %USERPROFILE%\Desktop\sparki-releases\sparkifire-v40-1.0.7.aab
    )
) else (
    echo Build failed!
    exit /b 1
)

endlocal
