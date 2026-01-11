@echo off
setlocal
cd /d "%~dp0"
echo ========================================
echo      SparkiFire Diagnostics Tool
echo ========================================

REM Find ADB
set "ADB_PATH=adb"
if exist "C:\Users\%USERNAME%\AppData\Local\Android\Sdk\platform-tools\adb.exe" (
    set "ADB_PATH=C:\Users\%USERNAME%\AppData\Local\Android\Sdk\platform-tools\adb.exe"
)

echo.
echo 1. Checking Connected Devices...
echo ----------------------------------------
"%ADB_PATH%" devices
echo.

echo 2. Checking Installed Package...
echo ----------------------------------------
"%ADB_PATH%" shell dumpsys package com.sparkiai.app | findstr "versionName versionCode"

echo.
echo 3. Force Uninstalling...
echo ----------------------------------------
"%ADB_PATH%" uninstall com.sparkiai.app

echo.
echo ========================================
echo      DIAGNOSTICS COMPLETE
echo ========================================
echo.
echo Please run clean_install_v3.bat again after this.
echo.
pause
