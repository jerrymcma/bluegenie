# Quick Guide: Building SparkiFire with Claude AI

## ✅ Configuration Complete!

Your SparkiFire Android app is now configured to use Claude AI with your $5 credit.

## Build Instructions

### In Android Studio:

1. **Sync Gradle Files**
    - Click: `File > Sync Project with Gradle Files`
    - Wait for sync to complete

2. **Clean and Rebuild**
    - Click: `Build > Clean Project`
    - Wait for clean to finish
    - Click: `Build > Rebuild Project`
    - Wait for rebuild to complete

3. **Run on Device/Emulator**
    - Click the green **Run** button (▶️)
    - Or press `Shift + F10`

4. **Or Build APK**
    - Click: `Build > Build Bundle(s) / APK(s) > Build APK(s)`
    - Wait for build to complete
    - APK will be at: `app/build/outputs/apk/debug/app-debug.apk`

## What Was Changed

✅ **AIRepository.kt** - Now uses `ClaudeAIService` instead of `OpenAIService`

The app will now use your Claude API key for all AI conversations!

## Test Your Setup

1. Open the app
2. Select any AI personality (e.g., Sparki, Coach, Brain)
3. Send a message like "Hello! Tell me about yourself"
4. You should get a response from Claude AI within 2-5 seconds

## Check if Claude is Working

Open **Logcat** in Android Studio and filter for "ClaudeAI":

✅ **Success messages:**

```
ClaudeAI: API Key configured successfully
ClaudeAI: Success with Claude 3.5 Sonnet
```

❌ **Error messages:**

```
ClaudeAI: API key is blank!
ClaudeAI: API Error (XXX): ...
```

## Your API Keys (Already Configured)

- ✅ **Claude:** `sk-ant-api03-ElikjpfBZlmVmKXCpbaKM...` ($5 credit)
- ℹ️ **OpenAI:** Available if you want to switch back
- ℹ️ **Gemini:** Available if you want to switch back

## Monitor Your Claude Usage

Visit: https://console.anthropic.com/

You can see your:

- Remaining credit ($5 total)
- API usage statistics
- Request history

## Cost Estimate

With $5 credit on Claude:

- **~150-250 messages** (depending on complexity)
- **Average cost:** $0.02-0.03 per message

## Need Help?

If you see any errors:

1. Check that API key is in `local.properties`
2. Sync Gradle files
3. Clean and rebuild project
4. Check Logcat for specific error messages

## Ready to Go! 🚀

Your app is configured and ready to use Claude AI. Just build and run it in Android Studio!
