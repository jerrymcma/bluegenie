# Fixing "Demo Mode" Issue - Action Required! 🔧

## Why Sparki Says "Demo Mode"

The app is still running the **OLD compiled version** that uses OpenAI. The code changes I made need
to be **rebuilt** to take effect.

## SOLUTION: Follow These Steps EXACTLY

### Step 1: Close the App (If Running)

- Stop the app if it's currently running on your device/emulator

### Step 2: In Android Studio - CLEAN BUILD

```
1. Click: Build > Clean Project
   (Wait for it to complete - you'll see "BUILD SUCCESSFUL" at bottom)

2. Click: Build > Rebuild Project  
   (Wait for it to complete - this takes 1-2 minutes)

3. Click: File > Invalidate Caches / Restart
   (Choose "Invalidate and Restart")
   (Android Studio will restart)
```

### Step 3: After Android Studio Restarts

```
4. Click: File > Sync Project with Gradle Files
   (Wait for sync to complete)

5. Click the green Run button (▶️)
   (Or press Shift + F10)
```

### Step 4: Test Again

- Open the app
- Send a message to Sparki
- You should now see Claude AI responses!

## What to Look For

### ✅ SUCCESS - You'll see:

- Natural, intelligent responses from Claude
- Responses appear in 2-5 seconds
- No "demo mode" messages

### ❌ STILL DEMO MODE - Check Logcat:

In Android Studio, click "Logcat" at the bottom, then filter for "ClaudeAI":

**If you see:**

```
ClaudeAI: API Key length: 0
ClaudeAI: API key is blank!
```

**This means:** The API key isn't being loaded during build

**Fix:** Continue to "Advanced Fix" below

## Advanced Fix (If Still Not Working)

If you still see demo mode after rebuilding, the API keys might not be loading from properties
files.

### Check 1: Verify gradle.properties

Open `gradle.properties` and make sure it has:

```properties
CLAUDE_API_KEY=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
```

### Check 2: Look at Logcat Logs

Filter Logcat for "ClaudeAI" and look for:

**✅ Working:**

```
ClaudeAI: API Key length: 123
ClaudeAI: API Key starts with: sk-ant-api
ClaudeAI: API Key configured successfully
ClaudeAI: Sending request with model: claude-3-5-sonnet-20241022
ClaudeAI: Success with Claude 3.5 Sonnet
```

**❌ Not Working:**

```
ClaudeAI: API Key length: 0
ClaudeAI: API key is blank!
```

### Check 3: Is BuildConfig Being Generated?

In Android Studio, try to open:

```
app/build/generated/source/buildConfig/debug/com/sparkiai/app/BuildConfig.java
```

Look for:

```java
public static final String CLAUDE_API_KEY = "sk-ant-api03-...";
```

If it's empty (`""`), the key isn't being loaded from gradle.properties.

## Emergency Fix: Hardcode the Key Temporarily

If the API key still isn't loading, we can temporarily hardcode it to test:

**Edit:** `app/src/main/java/com/sparkiai/app/network/ClaudeAIService.kt`

**Change line 19 from:**

```kotlin
private val apiKey: String = BuildConfig.CLAUDE_API_KEY
```

**To:**

```kotlin
private val apiKey: String = "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"
```

Then rebuild and run. **This is only for testing!** Don't publish the app with a hardcoded key.

## Most Common Issues

### Issue 1: "Demo mode" with working internet

**Cause:** Old build still running  
**Fix:** Clean + Rebuild + Restart Android Studio

### Issue 2: API key is blank in logs

**Cause:** gradle.properties not being read  
**Fix:**

1. Verify gradle.properties has the key
2. File > Sync Project with Gradle Files
3. Build > Clean Project
4. Build > Rebuild Project

### Issue 3: "API Error (401)"

**Cause:** Invalid or expired API key  
**Fix:** Check your Claude account at https://console.anthropic.com/

### Issue 4: "API Error (429)"

**Cause:** Out of credits or rate limited  
**Fix:** Check your credit balance at https://console.anthropic.com/

## Verification Steps

After rebuilding, verify these in order:

1. **Logcat shows:** `ClaudeAI: API Key configured successfully` ✅
2. **Logcat shows:** `ClaudeAI: Sending request with model: claude-3-5-sonnet-20241022` ✅
3. **Logcat shows:** `ClaudeAI: Success with Claude 3.5 Sonnet` ✅
4. **App shows:** Real AI responses (not demo messages) ✅

## Quick Troubleshooting Commands

Run these in Android Studio Terminal:

```bash
# Check if gradle.properties has the key
cat gradle.properties | Select-String "CLAUDE"

# Force Gradle to re-sync
./gradlew --stop
./gradlew clean
```

## Still Not Working?

If after all this you still see demo mode:

1. **Share the Logcat output** - Filter for "ClaudeAI" and share what you see
2. **Check the BuildConfig** - Tell me if BuildConfig.CLAUDE_API_KEY is populated
3. **Try the emergency hardcode fix** above to isolate the issue

## Summary

**The #1 reason for demo mode:** Old build still running

**The #1 fix:** Clean + Rebuild + Restart

```
Build > Clean Project
Build > Rebuild Project  
File > Invalidate Caches / Restart
```

Then run the app again! 🚀
