@echo off
setlocal enabledelayedexpansion

cd /d C:\Users\Jerry\AndroidStudioProjects\SparkiFire

echo ========================================
echo Building SparkiFire Release Version 35
echo ========================================
echo.

REM Check keystore file
echo Checking keystore file...
if exist sparkifire-release.jks (
    echo [OK] Keystore file found
    for %%A in (sparkifire-release.jks) do echo     Size: %%~zA bytes
) else (
    echo [ERROR] Keystore file NOT found!
    echo Creating keystore now...
    keytool -genkey -v -keystore sparkifire-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias sparkifire -storepass sparkifire123 -keypass sparkifire123 -dname "CN=SparkiFire"
    if !ERRORLEVEL! EQU 0 (
        echo [OK] Keystore created successfully
    ) else (
        echo [ERROR] Failed to create keystore (exit code !ERRORLEVEL!)
        goto :error
    )
)
echo.

REM Check keystore.properties
echo Checking keystore.properties...
if exist keystore.properties (
    echo [OK] keystore.properties found
    type keystore.properties
) else (
    echo [ERROR] keystore.properties NOT found!
)
echo.

REM Check version in build.gradle.kts
echo Checking version in build.gradle.kts...
findstr /c:"versionCode = 35" app\build.gradle.kts >nul
if !ERRORLEVEL! EQU 0 (
    echo [OK] Version code is set to 35
) else (
    echo [ERROR] Version code not set to 35!
)
echo.

REM Clean and build
echo Starting gradle build...
call gradlew clean
echo.
call gradlew bundleRelease

if !ERRORLEVEL! EQU 0 (
    echo.
    echo [OK] Build completed successfully
    echo.
    
    REM Check for AAB file
    if exist app\release\app-release.aab (
        echo [OK] AAB file created
        for %%A in (app\release\app-release.aab) do echo     Size: %%~zA bytes
        
        REM Copy to Desktop
        echo.
        echo Copying to Desktop\sparki-releases\version35.aab...
        if not exist %USERPROFILE%\Desktop\sparki-releases mkdir %USERPROFILE%\Desktop\sparki-releases
        copy app\release\app-release.aab %USERPROFILE%\Desktop\sparki-releases\version35.aab
        
        if !ERRORLEVEL! EQU 0 (
            echo [OK] File copied successfully!
            echo     Location: %USERPROFILE%\Desktop\sparki-releases\version35.aab
        ) else (
            echo [ERROR] Failed to copy file (exit code !ERRORLEVEL!)
            goto :error
        )
    ) else (
        echo [ERROR] AAB file not created!
        goto :error
    )
) else (
    echo [ERROR] Build failed (exit code !ERRORLEVEL!)
    goto :error
)

echo.
echo ========================================
echo Build process completed successfully!
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
