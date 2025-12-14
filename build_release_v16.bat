@echo off
echo ========================================
echo Building SparkiFire Version 16 Release
echo ========================================
echo.

REM Set JAVA_HOME to Android Studio's embedded JDK
set JAVA_HOME=C:\Program Files\Android\Android Studio\jbr
set PATH=%JAVA_HOME%\bin;%PATH%

echo Using Java from: %JAVA_HOME%
echo.

REM Clean and build the release bundle
echo Step 1: Cleaning project...
call gradlew.bat clean

echo.
echo Step 2: Building signed release bundle...
call gradlew.bat bundleRelease

echo.
echo ========================================
echo Build Complete!
echo ========================================
echo.
echo Bundle location: app\release\app-release.aab
echo.
echo Copying to Desktop as sparki-release-version16.aab...
copy app\release\app-release.aab "%USERPROFILE%\Desktop\sparki-release-version16.aab"

echo.
echo ========================================
echo DONE! Bundle saved to Desktop
echo ========================================
echo File: sparki-release-version16.aab
echo Version: 1.6.0 (Code 16)
echo.
pause
