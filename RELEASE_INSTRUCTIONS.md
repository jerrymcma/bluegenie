# SparkiFire - Release Build Instructions

## 📦 Pre-Release Setup (One-Time Only)

### Step 1: Generate Release Keystore

Run this command in your project root directory:

```bash
keytool -genkey -v -keystore sparkifire-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias sparkifire
```

**Important:**

- You will be asked to create a password - **SAVE THIS PASSWORD SECURELY!**
- You will be asked for your name, organization, etc. - fill these out
- The keystore file will be created in your project root
- **BACKUP THIS FILE SECURELY** - You need it for ALL future updates!
- If you lose this file, you cannot update your app on Google Play Store

### Step 2: Create keystore.properties File

Create a file named `keystore.properties` in your project root directory with this content:

```properties
storeFile=sparkifire-release.jks
storePassword=YOUR_KEYSTORE_PASSWORD
keyAlias=sparkifire
keyPassword=YOUR_KEY_PASSWORD
```

Replace:

- `YOUR_KEYSTORE_PASSWORD` with the password you created in Step 1
- `YOUR_KEY_PASSWORD` with the key password (usually the same as store password)

**Important:** This file is already in `.gitignore` - never commit it to version control!

### Step 3: Verify local.properties

Ensure your `local.properties` file contains your Gemini API key:

```properties
sdk.dir=C\:\\Users\\YourName\\AppData\\Local\\Android\\sdk
GEMINI_API_KEY=your_actual_gemini_api_key_here
```

---

## 🚀 Building Release APK/AAB

### Option A: Build via Android Studio (Recommended)

1. In Android Studio, go to: **Build** → **Generate Signed Bundle / APK**
2. Select **Android App Bundle** (required for Google Play)
3. Click **Next**
4. Choose your keystore file (`sparkifire-release.jks`)
5. Enter your passwords
6. Select **release** build variant
7. Check both signature versions (V1 and V2)
8. Click **Finish**

The signed AAB will be in: `app/release/app-release.aab`

### Option B: Build via Command Line

#### Build Release AAB (for Google Play Store):

```bash
./gradlew bundleRelease
```

#### Build Release APK (for testing):

```bash
./gradlew assembleRelease
```

Output locations:

- AAB: `app/build/outputs/bundle/release/app-release.aab`
- APK: `app/build/outputs/apk/release/app-release.apk`

---

## ✅ Pre-Upload Checklist

Before uploading to Google Play Console:

- [ ] Test the release APK on multiple devices
- [ ] Verify all personalities work correctly
- [ ] Test voice input and image upload features
- [ ] Check that API key is working
- [ ] Verify app icon and name appear correctly
- [ ] Test on different screen sizes
- [ ] Check for any crashes or bugs
- [ ] Verify permissions are requested properly
- [ ] Test camera and gallery image selection
- [ ] Ensure all text is correct (no typos)

---

## 📱 Version Management

### For Updates (Increment these before building):

In `app/build.gradle.kts`:

```kotlin
versionCode = 2  // Increment by 1 for each release
versionName = "1.0.1"  // Update according to semantic versioning
```

**Semantic Versioning:**

- MAJOR.MINOR.PATCH (e.g., 1.0.0)
- Increment MAJOR for breaking changes
- Increment MINOR for new features
- Increment PATCH for bug fixes

---

## 🔐 Security Reminders

**NEVER commit to Git:**

- `sparkifire-release.jks`
- `keystore.properties`
- `local.properties` (contains API keys)

**BACKUP securely:**

- Your keystore file (`sparkifire-release.jks`)
- Your keystore passwords
- Store in multiple secure locations (encrypted cloud, USB drive, password manager)

**If you lose your keystore:**

- You CANNOT update your existing app
- You would need to publish as a completely new app
- Users would have to uninstall and reinstall

---

## 🐛 Troubleshooting

### Build fails with "Keystore not found"

- Ensure `sparkifire-release.jks` is in your project root
- Check the path in `keystore.properties` is correct

### Build fails with "Wrong password"

- Double-check passwords in `keystore.properties`
- Ensure no extra spaces in the properties file

### App crashes in release but works in debug

- Check ProGuard rules in `proguard-rules.pro`
- Look at crash logs: `adb logcat`
- May need to add keep rules for specific classes

### "API Key not found" error

- Verify `GEMINI_API_KEY` is in `local.properties`
- Do a Clean Build: **Build** → **Clean Project** → **Rebuild Project**

---

## 📊 Testing Release Build

Install release APK on device:

```bash
adb install app/build/outputs/apk/release/app-release.apk
```

View logs while testing:

```bash
adb logcat | grep SparkiFire
```

---

## 🎉 Ready for Upload

Once your AAB is built and tested:

1. Go to Google Play Console
2. Navigate to your app → Release → Production
3. Create new release
4. Upload the `app-release.aab` file
5. Fill in release notes
6. Submit for review

**Good luck with your launch! 🚀**
