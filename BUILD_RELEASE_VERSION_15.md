# Building Release Version 15 - Fixed Demo Mode Issue

## Quick Start

The ProGuard configuration has been fixed. Follow these steps to build and deploy:

### Step 1: Update Version Number

Open `app/build.gradle.kts` and update:

```kotlin
defaultConfig {
    versionCode = 15  // Change from 14 to 15
    versionName = "1.5.0"  // Change from 1.4.0 to 1.5.0
}
```

### Step 2: Build Release Bundle

**In Android Studio:**

1. **Clean the project:**
    - Menu: `Build` → `Clean Project`
    - Wait for completion

2. **Build signed bundle:**
    - Menu: `Build` → `Generate Signed Bundle / APK`
    - Select: `Android App Bundle`
    - Click: `Next`

3. **Select key:**
    - Key store path: `sparkifire-release.jks`
    - Key store password: (from `keystore.properties`)
    - Key alias: `sparkifire`
    - Key password: (from `keystore.properties`)
    - Click: `Next`

4. **Build release:**
    - Build variant: `release`
    - Click: `Finish`

**Or via Terminal:**

```powershell
# Navigate to project root
cd C:\Users\Jerry\AndroidStudioProjects\SparkiFire

# Clean and build
.\gradlew clean
.\gradlew :app:bundleRelease
```

### Step 3: Locate the AAB File

The release bundle will be at:

```
app\release\app-release.aab
```

### Step 4: Upload to Play Console

1. Go to: https://play.google.com/console
2. Select: **Sparki AI**
3. Navigate to: `Release` → `Testing` → `Closed testing`
4. Click: `Create new release`
5. Upload: `app-release.aab`
6. Release name: `Version 1.5.0 - Fixed AI in production`
7. Release notes:
   ```
   Fixed: AI model now works correctly in production release
   - Resolved API key loading issue
   - Real Claude AI responses enabled
   - All personalities working correctly
   ```
8. Click: `Save` → `Review release` → `Start rollout to Closed testing`

### Step 5: Test the Release

1. **Wait for processing** (typically 5-30 minutes)
2. **Update on test device:**
    - Open Play Store on your test device
    - Go to: `My apps & games` → `Updates`
    - Update Sparki AI
3. **Test the app:**
    - Open Sparki
    - Send a message: "Hello, what's the weather like?"
    - You should get a real AI response (not demo mode!)

## What Was Fixed

### The Problem

ProGuard rules referenced wrong package name:

- ❌ Old: `com.example.sparkifire`
- ✅ New: `com.sparkiai.app`

### The Impact

- Debug builds worked (no ProGuard)
- Release builds failed (ProGuard stripped API keys)
- Play Store version fell back to demo mode

### The Solution

Updated `proguard-rules.pro` with correct package names:

```proguard
# Keep BuildConfig with API keys
-keep class com.sparkiai.app.BuildConfig { *; }

# Keep app classes
-keep class com.sparkiai.app.model.** { *; }
-keep class com.sparkiai.app.network.** { *; }
-keep class com.sparkiai.app.repository.** { *; }
-keep class com.sparkiai.app.viewmodel.** { *; }
-keep class com.sparkiai.app.utils.** { *; }
```

## Verification Checklist

Before uploading to Play Store:

- [ ] Version code updated to 15
- [ ] Version name updated to 1.5.0
- [ ] Clean build completed
- [ ] Release bundle generated successfully
- [ ] AAB file exists at `app/release/app-release.aab`
- [ ] File size is reasonable (typically 5-15 MB)

After uploading to Play Store:

- [ ] Release created in Play Console
- [ ] Release notes added
- [ ] Rollout started to closed testing
- [ ] App updated on test device
- [ ] Real AI responses working (not demo mode)
- [ ] Multiple personalities tested
- [ ] No error messages about API keys

## Expected Results

### ✅ Success Signs:

- Real Claude AI responses
- Natural, contextual answers
- Different personalities work correctly
- No "demo mode" messages
- No "API key not configured" messages
- Responses in 2-5 seconds

### ❌ If Still Having Issues:

**1. Check Logcat after release install:**

```
ClaudeAI: API Key length: [should be > 100]
ClaudeAI: API Key starts with: sk-ant-api
ClaudeAI: API Key configured successfully
```

**2. Verify BuildConfig was kept by ProGuard:**

After building release, check:

```
app\build\generated\source\buildConfig\release\com\sparkiai\app\BuildConfig.java
```

Should contain:

```java
public static final String CLAUDE_API_KEY = "sk-ant-api03-...";
```

**3. Check ProGuard mapping:**

```
app\build\outputs\mapping\release\configuration.txt
```

Look for:

```
-keep class com.sparkiai.app.BuildConfig { *; }
```

## Current Configuration

### API Keys (from gradle.properties):

- ✅ CLAUDE_API_KEY: Configured
- ✅ GEMINI_API_KEY: Configured
- ✅ OPENAI_API_KEY: Configured
- ✅ TAVILY_API_KEY: Configured

### Active AI Model:

- **Claude 3 Haiku** (fast and cost-effective)
- Model: `claude-3-haiku-20240307`
- Max tokens: 1024
- Real-time search: Enabled via Tavily

### Build Configuration:

- Namespace: `com.sparkiai.app`
- Min SDK: 24
- Target SDK: 36
- Compile SDK: 36
- Signing: Release keystore configured

## Troubleshooting

### "Demo mode" still appearing:

1. **Verify version:**
    - Check app version in Play Console
    - Ensure you've updated to the new version
    - Check: Settings → Apps → Sparki → Version 1.5.0

2. **Clear app data:**
    - Settings → Apps → Sparki
    - Storage → Clear storage
    - Reopen app and test

3. **Check API key validity:**
    - Go to: https://console.anthropic.com/
    - Verify API key is active
    - Check credit balance

### Build errors:

1. **"Keystore not found":**
    - Verify `keystore.properties` exists
    - Check `sparkifire-release.jks` path

2. **"Duplicate class" error:**
    - Run: `.\gradlew clean`
    - Then rebuild

3. **"BuildConfig not found":**
    - File → Sync Project with Gradle Files
    - Build → Rebuild Project

## Technical Notes

### ProGuard/R8 Configuration:

- **Minification**: Enabled for release
- **Shrinking**: Enabled for release
- **Obfuscation**: Partial (keep rules applied)
- **Optimization**: Default Android optimization

### Protected Classes:

All classes in these packages are protected from obfuscation:

- `com.sparkiai.app.model.**`
- `com.sparkiai.app.network.**`
- `com.sparkiai.app.repository.**`
- `com.sparkiai.app.viewmodel.**`
- `com.sparkiai.app.utils.**`
- `com.sparkiai.app.BuildConfig`

### API Key Security:

- Keys stored in `gradle.properties` (not in version control)
- Embedded in BuildConfig during build
- Protected from obfuscation by ProGuard rules
- Never exposed in UI or logs (only first 10 characters logged)

## Build Output Locations

### Release Bundle (AAB):

```
app\release\app-release.aab
```

### Release APK (if built):

```
app\build\outputs\apk\release\app-release.apk
```

### ProGuard Mappings:

```
app\build\outputs\mapping\release\
├── configuration.txt
├── mapping.txt
├── seeds.txt
└── usage.txt
```

### BuildConfig (Generated):

```
app\build\generated\source\buildConfig\release\com\sparkiai\app\BuildConfig.java
```

## Release History

- **v1.4.0 (Build 14)**: Claude AI with Tavily search (had demo mode issue)
- **v1.5.0 (Build 15)**: Fixed ProGuard rules - AI works in production ✅

## Next Steps After Successful Release

1. Monitor crash reports in Play Console
2. Check user feedback from closed testers
3. Verify API usage and costs in Claude console
4. Consider expanding to open testing if stable
5. Plan for production release to all users

---

**Ready to build!** Follow the steps above to create and deploy version 1.5.0. 🚀
