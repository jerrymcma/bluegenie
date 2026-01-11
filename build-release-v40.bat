@echo off
setlocal enabledelayedexpansion

cd /d C:\Users\Jerry\AndroidStudioProjects\SparkiFire

echo ========================================
echo Building SparkiFire Release Version 40
echo ========================================
echo.

REM Check keystore file
echo Checking keystore file...
if exist sparkifire-release77.jks (
    echo [OK] Keystore file found
    for %%A in (sparkifire-release77.jks) do echo     Size: %%~zA bytes
) else (
    echo [ERROR] Keystore file NOT found!
    echo Please ensure sparkifire-release77.jks exists in the project root
    goto :error
)
echo.

REM Check keystore.properties
echo Checking keystore.properties...
if exist keystore.properties (
    echo [OK] keystore.properties found
    type keystore.properties
) else (
    echo [ERROR] keystore.properties NOT found!
    goto :error
)
echo.

REM Check version in build.gradle.kts
echo Checking version in build.gradle.kts...
findstr /c:"versionCode = 40" app\build.gradle.kts >nul
if !ERRORLEVEL! EQU 0 (
    echo [OK] Version code is set to 40
) else (
    echo [WARNING] Version code might not be set to 40 - continuing anyway
)
findstr /c:"versionName = \"1.0.7\"" app\build.gradle.kts >nul
if !ERRORLEVEL! EQU 0 (
    echo [OK] Version name is set to 1.0.7
) else (
    echo [WARNING] Version name might not be set to 1.0.7 - continuing anyway
)
echo.

REM Clean and build
echo Starting gradle build...
echo Cleaning previous builds...
call gradlew clean
echo.

echo Building release AAB bundle...
call gradlew bundleRelease

if !ERRORLEVEL! EQU 0 (
    echo.
    echo [OK] Build completed successfully
    echo.
    
    REM Check for AAB file
    if exist app\build\outputs\bundle\release\app-release.aab (
        echo [OK] AAB file created successfully
        for %%A in (app\build\outputs\bundle\release\app-release.aab) do echo     Size: %%~zA bytes
        
        REM Create release directory structure
        echo.
        echo Creating release directories...
        if not exist sparki-releases mkdir sparki-releases
        if not exist %USERPROFILE%\Desktop\sparki-releases mkdir %USERPROFILE%\Desktop\sparki-releases
        
        REM Copy to local release folder
        echo Copying to sparki-releases\sparkifire-v40-1.0.7.aab...
        copy app\build\outputs\bundle\release\app-release.aab sparki-releases\sparkifire-v40-1.0.7.aab
        
        REM Copy to Desktop
        echo Copying to Desktop\sparki-releases\sparkifire-v40-1.0.7.aab...
        copy app\build\outputs\bundle\release\app-release.aab %USERPROFILE%\Desktop\sparki-releases\sparkifire-v40-1.0.7.aab
        
        if !ERRORLEVEL! EQU 0 (
            echo [OK] Files copied successfully!
            echo.
            echo Files available at:
            echo     Local: sparki-releases\sparkifire-v40-1.0.7.aab
            echo     Desktop: %USERPROFILE%\Desktop\sparki-releases\sparkifire-v40-1.0.7.aab
            echo.
            echo Ready to upload to Google Play Console!
        ) else (
            echo [ERROR] Failed to copy files (exit code !ERRORLEVEL!)
            goto :error
        )
    ) else (
        echo [ERROR] AAB file not created!
        echo Checking alternate location...
        if exist app\release\app-release.aab (
            echo [OK] Found AAB in alternate location
            copy app\release\app-release.aab sparki-releases\sparkifire-v40-1.0.7.aab
            copy app\release\app-release.aab %USERPROFILE%\Desktop\sparki-releases\sparkifire-v40-1.0.7.aab
            echo [OK] Files copied from alternate location
        ) else (
            echo [ERROR] AAB file not found in any expected location!
            goto :error
        )
    )
) else (
    echo [ERROR] Build failed (exit code !ERRORLEVEL!)
    goto :error
)

echo.
echo ========================================
echo Build process completed successfully!
echo Version: 1.0.7 (Build 40)
echo Ready for Google Play Console upload
echo ========================================
pause
exit /b 0

:error
echo.
echo ========================================
echo Build process FAILED!
echo ========================================
pause
exit /b 1