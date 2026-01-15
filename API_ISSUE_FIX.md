# Fixing Gemini API "Requests Blocked" Error

The error `Requests from this Android client application <empty> are blocked` indicates that your API Key in Google Cloud Console has "Android apps" restrictions enabled, but the app is failing to send its package name or signature verification.

## 🚀 Quick Fix Steps

### 1. Check API Key Restrictions (Most Likely Cause)
1. Go to [Google AI Studio](https://aistudio.google.com/) or [Google Cloud Console](https://console.cloud.google.com/apis/credentials).
2. Find the API Key you are using (`GEMINI_API_KEY`).
3. Click to edit the key.
4. Look at **"Application restrictions"**.
   - If it is set to **"Android apps"**, this is causing the issue because the debug build signature is not matching or the package name is not being sent correctly.
5. **Temporary Fix:** Change the restriction to **"None"** and click **Save**.
   - Wait 1-2 minutes and try the app again. It should work immediately.

### 2. If you want to keep Android Restrictions
If you must keep the Android restriction, you need to add your **Debug SHA-1 fingerprint** to the allowed list in the API Key settings.

1. Run this command in the project root to get your SHA-1:
   ```powershell
   ./gradlew signingReport
   ```
2. Look for the `debug` variant SHA-1.
3. In Google Cloud Console, add a new item under "Android apps".
   - Package name: `com.sparkiai.app`
   - SHA-1: (The one you copied from step 1)
4. Click **Save**.

### 3. Update Instructions
We have updated the Gemini SDK to version `0.9.0` which handles headers better. Sync your project with Gradle files:
1. Click **Sync Now** in the yellow bar in Android Studio.
2. Rebuild and run the app.

### 4. Verify Local Properties
Ensure your `local.properties` file has the correct key:
```properties
GEMINI_API_KEY=your_actual_api_key_here
```
