# Google Sign-In Setup Guide for SparkiFire

## What I've Done

I've set up Google Sign-In for your SparkiFire app. Here's what was added:

### 1. **Added Dependencies** ✅

- Added Google Play Services Auth library to `app/build.gradle.kts`
- Version: `com.google.android.gms:play-services-auth:21.0.0`

### 2. **Created Client ID Configuration** ✅

- Added `GOOGLE_CLIENT_ID` field to `local.properties`
- Added `GOOGLE_CLIENT_ID` as a BuildConfig field in `app/build.gradle.kts`

### 3. **Created GoogleSignInManager** ✅

- Location: `app/src/main/java/com/example/sparkifire/utils/GoogleSignInManager.kt`
- This class handles all Google Sign-In operations
- **THIS IS WHERE YOUR CLIENT ID IS USED** (line 21)

### 4. **Created Login Screen** ✅

- Location: `app/src/main/java/com/example/sparkifire/ui/screens/LoginScreen.kt`
- Beautiful Material 3 UI with Google Sign-In button
- Handles sign-in flow and errors

### 5. **Updated MainActivity** ✅

- App now starts with Login Screen
- After successful login, navigates to Chat Screen

---

## What You Need to Do Now

### Step 1: Get Your OAuth Client ID from Google Console

1. **Go to Google Cloud Console**: https://console.cloud.google.com/

2. **Create/Select Project**
    - Create a new project or select existing one

3. **Configure OAuth Consent Screen**
    - Go to **APIs & Services** → **OAuth consent screen**
    - Choose **External**
    - Fill in:
        - App name: **SparkiFire**
        - User support email: Your email
        - Developer contact email: Your email
    - Click **Save and Continue**

4. **Create OAuth Client ID**
    - Go to **APIs & Services** → **Credentials**
    - Click **+ CREATE CREDENTIALS** → **OAuth client ID**
    - Select **Android**
    - Fill in:
        - **Package name**: `com.example.sparkifire`
        - **SHA-1 certificate fingerprint**:
          ```
          4A:54:C8:78:1C:05:BA:AE:16:5F:A6:62:27:DF:0B:0F:57:D8:B8:46
          ```
    - Click **Create**

5. **Copy Your Client ID**
    - You'll get a client ID like: `123456789-abcdefg.apps.googleusercontent.com`
    - **COPY THIS!**

### Step 2: Add Client ID to Your Project

1. **Open**: `local.properties`

2. **Replace** this line:
   ```
   GOOGLE_CLIENT_ID=YOUR_CLIENT_ID_HERE.apps.googleusercontent.com
   ```

   **With** your actual client ID:
   ```
   GOOGLE_CLIENT_ID=123456789-abcdefg.apps.googleusercontent.com
   ```

### Step 3: Sync and Build

1. **Sync Gradle**:
    - Click the "Sync Now" button in Android Studio, or
    - Go to **File** → **Sync Project with Gradle Files**

2. **Build and Run**:
    - Run the app on your device or emulator
    - You should see the new Login Screen!

---

## Where the Client ID is Used

The client ID you get from Google Console is used in this file:

**`app/src/main/java/com/example/sparkifire/utils/GoogleSignInManager.kt`** (line 20-23):

```kotlin
val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
    .requestIdToken(BuildConfig.GOOGLE_CLIENT_ID)  // <-- YOUR CLIENT ID IS USED HERE
    .requestEmail()
    .build()
```

The `BuildConfig.GOOGLE_CLIENT_ID` automatically loads from your `local.properties` file.

---

## Testing Google Sign-In

1. **Run the app**
2. **Click "Sign in with Google"**
3. **Select your Google account**
4. **Grant permissions**
5. **You'll be redirected to the Chat Screen**

---

## Important Notes

### For Release Build (Later)

When you create a release build with your keystore, you'll need to:

1. Get the SHA-1 from your release keystore
2. Add another OAuth Client ID in Google Console with that SHA-1
3. Same package name, different SHA-1

### Security

- ✅ `local.properties` is already in `.gitignore` (never committed to Git)
- ✅ Client ID is loaded securely through BuildConfig
- ✅ No hardcoded credentials in your code

---

## Troubleshooting

### "Sign-in failed" Error

- Make sure Client ID in `local.properties` matches Google Console
- Make sure SHA-1 in Google Console matches your debug keystore
- Make sure package name is `com.example.sparkifire`

### Linter Errors

- Normal before Gradle sync
- Will disappear after syncing Gradle successfully

### "Developer Error" on Sign-In

- Means OAuth Client ID is not configured correctly
- Double-check SHA-1, package name, and client ID

---

## Your Debug Keystore Info (for reference)

```
Package Name: com.example.sparkifire
SHA-1: 4A:54:C8:78:1C:05:BA:AE:16:5F:A6:62:27:DF:0B:0F:57:D8:B8:46
SHA-256: 67:87:66:3F:3F:E5:46:DA:C6:DF:94:15:86:86:84:EF:1B:5E:23:B4:3A:F0:F5:AF:2F:09:D5:62:9C:93:32:90
```

Use these values when creating your OAuth Client ID in Google Console!
