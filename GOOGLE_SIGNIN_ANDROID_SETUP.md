# Google Sign-In Setup for Android

## The Issue
Google Sign-In works on the web app but fails on Android because:
- **Web apps** use a Web application OAuth client ID
- **Android apps** need an Android OAuth client ID with SHA-1 fingerprint

The ID token returns `null` because your Android app isn't registered in Google Cloud Console.

## Step 1: Get Your SHA-1 Fingerprint

### For Debug Build
Open PowerShell in your project directory and run:
```powershell
cd C:\Users\Jerry\AndroidStudioProjects\SparkiFire
.\gradlew signingReport
```

Look for the **SHA-1** under `Variant: debug`. Copy it. It looks like:
```
SHA1: A1:B2:C3:D4:E5:F6:...
```

### For Release Build (when you publish to Play Store)
You'll also need the SHA-1 from your release keystore. Run:
```powershell
keytool -list -v -keystore path\to\your-release-key.keystore -alias your-key-alias
```

Or get it from Google Play Console after uploading your app.

## Step 2: Create Android OAuth Client in Google Cloud Console

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Select your project (the same one your web app uses)
3. Navigate to **APIs & Services** → **Credentials**
4. Click **+ CREATE CREDENTIALS** → **OAuth client ID**
5. Select **Application type**: **Android**
6. Fill in:
   - **Name**: `SparkiFire Android` (or any name you prefer)
   - **Package name**: `com.sparkiai.app` (MUST match your app exactly)
   - **SHA-1 certificate fingerprint**: Paste the SHA-1 from Step 1
7. Click **CREATE**

## Step 3: Update local.properties

After creating the Android OAuth client, you should see two client IDs in Google Cloud Console:
- **Web application client ID** (ends with `.apps.googleusercontent.com`) - for your web app
- **Android client ID** (also ends with `.apps.googleusercontent.com`) - for your Android app

Update your `local.properties`:
```properties
# Web Client ID - used for Supabase ID token authentication
# This should be your Web application client ID from Google Cloud Console
GOOGLE_WEB_CLIENT_ID=904707581552-2jjbaem1erkm56mc75trk75mcragkn6g.apps.googleusercontent.com

# Android Client ID - the Android OAuth client you just created
# (This is automatically matched by Google Play Services using SHA-1, so it's not strictly required in code)
GOOGLE_CLIENT_ID=904707581552-1p3f97q4hu7h8mkulu0jhj7tidjuak7i.apps.googleusercontent.com
```

**IMPORTANT**: The `GOOGLE_WEB_CLIENT_ID` must match the Web application client ID from Google Cloud Console. This is what Supabase uses to verify the ID token.

## Step 4: Verify Your Configuration

Your `GoogleSignInOptions` should request both:
1. **Server Auth Code** - for server-side exchanges
2. **ID Token** - for Supabase authentication

This is already set up correctly in your `GoogleSignInManager.kt`:
```kotlin
val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
    .requestServerAuthCode(webClientId) // Request server auth code for server exchanges
    .requestIdToken(webClientId)        // Request ID token for Supabase auth
    .requestEmail()
    .build()
```

## Step 5: Test the Fix

1. **Clean and rebuild** your app:
   ```powershell
   .\gradlew clean
   .\gradlew build
   ```

2. **Uninstall the old app** from your device/emulator (important!)
   - This clears cached credentials

3. **Install and run** the new build

4. **Try signing in** - you should now see the ID token properly returned

## Step 6: Add Release SHA-1 Before Publishing

Before publishing to Play Store, you MUST also add:
1. Your **Upload key SHA-1** (from Play Console)
2. Your **Release keystore SHA-1** (if using local signing)

Both go in the same Android OAuth client in Google Cloud Console.

## Debugging

If it still doesn't work, check the logs:
```
adb logcat | findstr /i "GoogleSignIn ChatScreen"
```

Look for:
- ✅ `ID token: ✓ Present` - Success!
- ❌ `ID token: ✗ Missing` - Still a configuration issue

Common issues:
1. **Wrong package name** - Must be exactly `com.sparkiai.app`
2. **Wrong SHA-1** - Make sure you copied the debug SHA-1 for testing
3. **App not uninstalled** - Old cached credentials interfere
4. **Google Play Services out of date** - Update it on your device

## How Google Sign-In Works on Android

1. **Your app** requests sign-in with Google Play Services
2. **Google Play Services** matches your app's package name + SHA-1
3. **Google** finds the matching Android OAuth client
4. **Google** returns account + **ID token** (because you requested it)
5. **Your app** sends the ID token to Supabase
6. **Supabase** verifies the token using your Web Client ID
7. ✅ **User is signed in!**

## Summary

- ✅ **Web app works** - Your Supabase config is correct
- ❌ **Android fails** - Missing Android OAuth client with SHA-1
- 🔧 **Fix** - Create Android OAuth client in Google Cloud Console
- 📱 **Result** - ID token will be returned and sign-in will work!

## Need Help?

If you're still having issues:
1. Check the logs for specific error codes
2. Verify your SHA-1 matches exactly
3. Make sure you uninstalled the old app before testing
4. Confirm your package name is `com.sparkiai.app`
