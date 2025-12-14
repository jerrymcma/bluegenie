# Building Version 13 AAB - Gemini with Google Search Grounding

## Version Info

- **Version Code**: 13
- **Version Name**: 1.3.0
- **AI Provider**: Google Gemini (with Google Search grounding for real-time information)
- **Build Name**: `sparki-app-release-gemini-version13.aab`

## What Changed in Version 13

1. ✅ Switched from OpenAI back to Gemini
2. ✅ Restored Google Search grounding for real-time information
3. ✅ All personalities work with live web search
4. ✅ Gemini will dynamically use Google Search when needed for current events

## Building the AAB in Android Studio

### Step 1: Sync Gradle

1. Click **File > Sync Project with Gradle Files**
2. Wait for sync to complete

### Step 2: Build the Release AAB

1. Go to **Build > Generate Signed Bundle / APK**
2. Select **Android App Bundle**
3. Click **Next**
4. Select your keystore (should auto-fill from keystore.properties):
    - Key store path: `sparkifire-release.jks`
    - Key store password: (from your keystore.properties)
    - Key alias: `sparkifire`
    - Key password: (from your keystore.properties)
5. Click **Next**
6. Select **release** build variant
7. Click **Finish**

### Step 3: Locate the AAB File

The AAB will be generated at:

```
app/release/app-release.aab
```

### Step 4: Copy to Desktop with New Name

1. Navigate to `app/release/` folder
2. Copy `app-release.aab`
3. Paste to your Desktop
4. Rename to: **`sparki-app-release-gemini-version13.aab`**

## Alternative: Build via Command Line

If you prefer command line:

```batch
cd C:\Users\Jerry\AndroidStudioProjects\SparkiFire
gradlew.bat bundleRelease
```

Then copy from:

```
app\build\outputs\bundle\release\app-release.aab
```

To Desktop as:

```
sparki-app-release-gemini-version13.aab
```

## Upload to Play Console

### Release Notes for Version 13:

```
Version 1.3.0 - Real-Time Intelligence Update

✨ NEW: Real-time information access with Google Search grounding
🔍 Sparki can now answer questions about current events, latest news, and up-to-date information
🎯 Improved response accuracy for time-sensitive queries
🚀 Enhanced AI capabilities across all personalities
💬 Better conversation quality with live web search

Bug fixes and performance improvements
```

### Testing Before Upload:

1. Install the AAB on a test device
2. Ask Sparki: "Who is the current US president?"
3. Ask: "What are the latest tech news?"
4. Verify responses include current 2025 information

## Technical Details

### AI Configuration:

- **Model**: Gemini 2.0 Flash (with fallback to 1.5 Flash/Pro)
- **Search**: Always-on Google Search grounding
- **Tokens**: Max 1024 output tokens
- **Temperature**: 0.6 (balanced for factual + conversational)

### Gemini API Key:

```
GEMINI_API_KEY=AIzaSyBRED_ihN3OHefLpif4WTBBPmNCnu3lTlM
```

### How Google Search Grounding Works:

1. User asks a question
2. Gemini analyzes if real-time info is needed
3. If yes → Automatically searches Google
4. Synthesizes search results with AI response
5. Provides accurate, current answer

### Cost Considerations:

- Gemini API pricing passes through Google Cloud
- Monitor usage in Google Cloud Console
- Consider usage limits for closed testing phase

## Rollout Strategy

### Phase 1: Internal Testing (Now)

- Deploy to closed testing track
- Monitor API usage and costs
- Collect feedback from existing testers

### Phase 2: Open Testing (Next Week)

- If billing is stable and usage is manageable
- Expand to more testers
- Continue monitoring

### Phase 3: Production (Future)

- Once confident in costs and stability
- Full public release

## Monitoring Checklist

After deploying Version 13:

- [ ] Check Gemini API usage in Google Cloud Console
- [ ] Monitor costs daily for first week
- [ ] Collect tester feedback on response quality
- [ ] Verify real-time information is working
- [ ] Check for any API errors in logs
- [ ] Test all personality modes
- [ ] Verify voice input/output still works
- [ ] Test image sharing functionality

## Backup Plan

If Gemini billing issues persist:

1. We have Claude integration ready (needs $5 credit)
2. We have OpenAI integration as fallback (no real-time search)
3. Demo mode always available as ultimate fallback

## Success Metrics

Version 13 is successful if:

- ✅ Real-time information queries work correctly
- ✅ API costs stay within acceptable range
- ✅ Testers report improved response quality
- ✅ No increase in demo mode fallbacks
- ✅ All personalities maintain their character

---

**Ready to build!** Follow the steps above and you'll have your Version 13 AAB ready to upload to
Play Console! 🚀

Good luck with the release! 🎉
