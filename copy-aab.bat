@echo off
setlocal

echo Copying AAB file to Desktop...

set SOURCE=C:\Users\Jerry\AndroidStudioProjects\SparkiFire\app\build\outputs\bundle\release\app-release.aab
set DEST_DIR=C:\Users\Jerry\Desktop\sparki-releases
set DEST=%DEST_DIR%\version35.aab

if not exist "%SOURCE%" (
    echo ERROR: Source file not found: %SOURCE%
    pause
    exit /b 1
)

if not exist "%DEST_DIR%" (
    echo Creating destination directory...
    mkdir "%DEST_DIR%"
)

echo Copying %SOURCE%
echo to %DEST%
echo.

copy "%SOURCE%" "%DEST%"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo SUCCESS! File copied to:
    echo %DEST%
    echo.
    for %%A in ("%DEST%") do echo File size: %%~zA bytes
) else (
    echo ERROR: Failed to copy file (exit code %ERRORLEVEL%)
)

pause
