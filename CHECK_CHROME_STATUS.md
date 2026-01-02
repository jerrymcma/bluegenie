# How to Check if Chrome is Disabled

## On Physical Device (Samsung SM-A366U)

### Method 1: Check in Settings

1. **Open Settings**
2. **Go to Apps** (or Applications)
3. **Find Chrome**:
   - Tap on search icon
   - Type "Chrome"
   - Or scroll to find "Chrome"
4. **Check the status**:
   - If you see **"Disabled"** button → Chrome is disabled
   - If you see **"Enable"** button → Chrome is currently disabled, tap to enable
   - If you see **"Disable"** button → Chrome is enabled (working)
   - If Chrome is not in the list → Chrome is not installed

### Method 2: Check App Drawer

1. **Open app drawer** (swipe up from home screen)
2. **Look for Chrome icon**:
   - If you see it → Chrome is installed
   - If you don't see it → Chrome might be disabled or not installed

### Method 3: Check Default Apps

1. **Settings → Apps → Default apps**
2. **Browser app**:
   - Check what's set as default browser
   - If nothing is set → No browser is configured
   - Common browsers: Chrome, Samsung Internet, Firefox

## On Android Emulator

### Check Installed Browsers

1. **Open app drawer** in emulator
2. **Look for**:
   - Chrome icon
   - Browser icon
   - Any web browser

### If No Browser Found

1. **Check if your AVD has Google Play**:
   - In Android Studio: Tools → AVD Manager
   - Look at your AVD - does it have Play Store icon?
   - If no Play Store icon → Create new AVD with Google Play image

2. **Install Chrome**:
   - Open Play Store on emulator
   - Search "Chrome"
   - Install

## Using ADB Commands (Advanced)

If you want to check from your computer:

### List All Browsers
```bash
adb shell pm list packages | findstr browser
```

### Check Chrome Specifically
```bash
adb shell pm list packages | findstr chrome
```

Expected output if Chrome is installed:
```
package:com.android.chrome
```

### Check if Chrome is Enabled
```bash
adb shell pm list packages -d | findstr chrome
```

- If this returns nothing → Chrome is enabled ✅
- If this returns `com.android.chrome` → Chrome is disabled ❌

### Enable Chrome via ADB
```bash
adb shell pm enable com.android.chrome
```

### Check Samsung Internet (Alternative Browser)
```bash
adb shell pm list packages | findstr samsung
```

Look for:
```
package:com.sec.android.app.sbrowser
```
This is Samsung Internet Browser

## What to Look For

### Chrome is Installed and Enabled ✅
- Chrome appears in app drawer
- You can open Chrome
- Settings shows Chrome as "Enabled"

### Chrome is Disabled ❌
- Chrome doesn't appear in app drawer
- Settings shows "Enable" button for Chrome
- ADB shows Chrome in disabled packages list

### Chrome Not Installed ❌
- Chrome doesn't appear anywhere
- Not in Settings → Apps list
- ADB doesn't show Chrome package

### Samsung Internet (Alternative)
Samsung devices come with their own browser called "Samsung Internet" or "Internet"
- Look for globe icon in app drawer
- Package name: `com.sec.android.app.sbrowser`
- This should also work for opening Stripe checkout

## Quick Diagnostic

Run this on your device/emulator via ADB:

```bash
# Check for ANY browser packages
adb shell pm list packages | findstr -i "browser chrome firefox samsung"

# Check which packages are disabled
adb shell pm list packages -d | findstr -i "browser chrome"

# Check default browser
adb shell cmd role get-holders android.app.role.BROWSER
```

## Most Likely Scenarios

### Scenario 1: Samsung Internet is Default
- Chrome might be installed but Samsung Internet is the default
- Both should work for opening URLs
- Our app should work with either

### Scenario 2: Chrome is Disabled
- Chrome is installed but disabled
- Enable it via Settings → Apps → Chrome → Enable

### Scenario 3: Using Emulator Without Google Play
- AVD doesn't have Play Store
- No browser installed by default
- Solution: Create new AVD with Google Play image

### Scenario 4: Chrome Not Installed
- Rare on physical devices
- Common on minimal emulators
- Solution: Install Chrome from Play Store

## After Checking

Once you've determined the Chrome status:

1. **If Chrome is disabled** → Enable it
2. **If Chrome not installed** → Install from Play Store  
3. **If Samsung Internet available** → It should work (test the app)
4. **If no browser at all** → Install Chrome or any browser

Then reinstall the app and test:
```bash
cd C:/Users/Jerry/AndroidStudioProjects/SparkiFire
./gradlew installDebug
```

## Expected Behavior After Fix

With a browser installed and enabled:
```
Logcat output:
🔍 Found 1 apps that can handle URL
✅ Stripe checkout opened successfully
```

Then browser should open with Stripe checkout page.
