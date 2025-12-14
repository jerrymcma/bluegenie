# ⚡ ACTION: Setup OAuth for Lyria (10 Minutes)

## 🎯 Why You're Getting "API Failure"

**The problem**: Vertex AI (Lyria) needs OAuth 2.0 tokens, not just an API key.

**The solution**: Create a service account and add its JSON key to your app.

---

## 🚀 Do This NOW (10 Minutes)

### 1. Create Service Account (5 min)

**Go to**: https://console.cloud.google.com/iam-admin/serviceaccounts

**Click**: "CREATE SERVICE ACCOUNT" (blue button)

**Fill in**:

```
Name: lyria-music-generator
ID: lyria-music-generator (auto-filled)
Description: Service account for Lyria music generation
```

**Click**: "CREATE AND CONTINUE"

**Grant role**:

- Click "Select a role"
- Search: "Vertex AI User"
- Select: "Vertex AI User"
- Click "CONTINUE"

**Click**: "DONE" (skip step 3)

---

### 2. Download JSON Key (2 min)

**On the service accounts page**, find your new account:

```
lyria-music-generator@your-project.iam.gserviceaccount.com
```

**Click** the email (blue link)

**Click** the "KEYS" tab

**Click** "ADD KEY" → "Create new key"

**Select** "JSON"

**Click** "CREATE"

**JSON file downloads!** 📥

---

### 3. Add JSON to App (2 min)

#### Easy Way (Recommended):

1. **Rename downloaded JSON** to: `vertex_ai_service_account.json`

2. **Create folder** (if doesn't exist):
    - In Android Studio
    - Right-click `app/src/main/`
    - New → Directory
    - Name: `assets`

3. **Drop JSON file** into: `app/src/main/assets/`

Final path:

```
app/src/main/assets/vertex_ai_service_account.json
```

---

### 4. Sync & Build (2 min)

```bash
# Sync Gradle (downloads Google Auth libraries)
./gradlew build

# Clean and rebuild
./gradlew clean
./gradlew installDebug
```

---

### 5. Test! (1 min)

1. Open app
2. Select "Magic Spark Music Composer"
3. Click "Generate Music"
4. Enter: "Upbeat electronic dance track"
5. Click "Generate"
6. **Should work now!** 🎵✅

---

## 🔍 Quick Verification

### Check Service Account Created

https://console.cloud.google.com/iam-admin/serviceaccounts

Should see:

```
✓ lyria-music-generator
  Role: Vertex AI User
  Keys: 1
```

### Check JSON File in Project

```
SparkiFire/
  └── app/
      └── src/
          └── main/
              └── assets/
                  └── vertex_ai_service_account.json  ← HERE!
```

### Check Logs (Android Studio)

After trying to generate, look for:

```
✅ Credentials loaded successfully
✅ Access token obtained successfully
✅ Music generated successfully
```

Or errors to fix:

```
❌ Service account JSON not found
❌ Failed to load credentials
❌ Failed to get access token
```

---

## 🐛 If Still Not Working

### Error: "Service account JSON not found"

**Check**:

- File exists in `app/src/main/assets/`
- File named exactly: `vertex_ai_service_account.json`
- Gradle synced
- App rebuilt

### Error: "Failed to load credentials"

**Check**:

- JSON file is valid JSON (paste into https://jsonlint.com/)
- All fields present from downloaded file
- No editing errors

### Error: "Authentication failed" (401/403)

**Check**:

- Service account has "Vertex AI User" role
- Project ID in FeatureFlags matches JSON `project_id`
- Vertex AI API is enabled
- Billing is enabled

---

## 📁 What You'll Have

After setup:

```
app/src/main/assets/
  └── vertex_ai_service_account.json  (your OAuth credentials)

app/src/main/java/com/sparkiai/app/network/
  ├── VertexAIAuth.kt  (OAuth handler - already created!)
  └── LyriaService.kt  (uses OAuth - already updated!)
```

Plus Google Auth libraries (auto-downloaded by Gradle).

---

## ✅ Success Looks Like

### In Logs

```
LyriaService: 🎵 Generating music with prompt: Upbeat electronic...
VertexAIAuth: ✅ Credentials loaded successfully
VertexAIAuth: ✅ Access token obtained successfully
LyriaService: 📡 Calling Lyria API: https://us-central1-...
LyriaService: ✅ Lyria API call successful
LyriaService: 🎵 Music generated successfully! Size: 5242880 bytes
ChatViewModel: ✅ Music generated successfully: a1b2c3d4-...
```

### In App

```
User: 🎵 Generate music: Upbeat electronic dance track

Sparki: 🎶 Creating your music... (10-30 seconds)

[15 seconds later...]

Sparki: 🎵 Your music is ready! 🎶
        Format: High-quality WAV (48kHz)
        This was FREE! You have 9 free songs remaining. 🎉
        
        [Music file available in library]
```

---

## 🎯 The Files You Need

### From Google Cloud

1. ✅ Service account created
2. ✅ JSON key downloaded
3. ✅ Renamed to `vertex_ai_service_account.json`

### In Your Project

1. ✅ JSON in `app/src/main/assets/` folder
2. ✅ VertexAIAuth.kt (already created!)
3. ✅ LyriaService.kt (already updated!)
4. ✅ Dependencies added (already done!)

### Gradle Files

1. ✅ `build.gradle.kts` - Google Auth dependencies added
2. ✅ `gradle.properties` - Service account path noted

---

## 🔒 Security Reminder

### DO THIS:

**Add to `.gitignore`**:

```
# Service Account (NEVER COMMIT!)
vertex_ai_service_account.json
**/vertex_ai_service_account.json
app/src/main/assets/vertex_ai_service_account.json
```

**Verify**:

```bash
# Make sure JSON won't be committed
git status

# Should NOT see:
# app/src/main/assets/vertex_ai_service_account.json
```

---

## 💰 Cost Reminder

**Once working**:

- Songs 1-10: FREE (per user)
- Songs 11+: $0.06 each
- Billed monthly by Google Cloud
- You can see usage in Google Cloud Console

---

## 🎉 Summary

**Problem**: API key doesn't work (needs OAuth)  
**Solution**: Service account with JSON key  
**Time**: 10 minutes  
**Steps**: 5 simple steps above  
**Result**: Real music generation! 🎵

---

## 📞 Need Help?

**Full guide**: `OAUTH_SERVICE_ACCOUNT_SETUP.md`  
**Visual guide**: `OAUTH_SETUP_VISUAL_GUIDE.md`  
**Check logs**: Look for "VertexAIAuth" and "LyriaService" tags

---

**NOW**: Follow steps 1-5 above!  
**THEN**: Test music generation!  
**RESULT**: Working Lyria! 🚀🎵✨
