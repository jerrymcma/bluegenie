# 🔐 OAuth Service Account Setup for Lyria

## Why You Need This

Lyria on Vertex AI requires **OAuth 2.0 authentication** using a service account. An API key alone
won't work.

---

## 📋 Complete Setup (10 Minutes)

### Step 1: Create Service Account (5 minutes)

1. **Go to Service Accounts page**:
   https://console.cloud.google.com/iam-admin/serviceaccounts

2. **Select your project** (top dropdown)

3. **Click "CREATE SERVICE ACCOUNT"**

4. **Fill in details**:
    - **Service account name**: `lyria-music-generator`
    - **Service account ID**: `lyria-music-generator` (auto-filled)
    - **Description**: "Service account for Lyria music generation in Sparki app"
    - Click **"CREATE AND CONTINUE"**

5. **Grant permissions**:
    - Click "Select a role"
    - Search for: **"Vertex AI User"**
    - Select: **"Vertex AI User"**
    - Click **"CONTINUE"**

6. **Skip optional** (Grant users access):
    - Click **"DONE"**

7. **Create JSON key**:
    - Find your new service account in the list
    - Click the **three dots** (⋮) on the right
    - Select **"Manage keys"**
    - Click **"ADD KEY"** → **"Create new key"**
    - Select **"JSON"**
    - Click **"CREATE"**
    - **JSON file downloads** automatically! 📥

---

### Step 2: Add JSON to Your Project (2 minutes)

You have **two options**:

#### Option A: Put JSON in Assets Folder (Easier)

1. **Open the downloaded JSON file**
2. **Rename it** to: `vertex_ai_service_account.json`
3. **Create assets folder** (if it doesn't exist):
   ```
   app/src/main/assets/
   ```
4. **Move the JSON file** there:
   ```
   app/src/main/assets/vertex_ai_service_account.json
   ```
5. **Done!** The app will auto-load it

#### Option B: Hardcode JSON in VertexAIAuth.kt (More Secure)

1. **Open**: `app/src/main/java/com/sparkiai/app/network/VertexAIAuth.kt`

2. **Find the `getServiceAccountJson()` function** (around line 105)

3. **Replace the empty string** with your JSON content:

```kotlin
private fun getServiceAccountJson(): String {
    return """
    {
      "type": "service_account",
      "project_id": "your-project-id",
      "private_key_id": "abc123...",
      "private_key": "-----BEGIN PRIVATE KEY-----\nYOUR_KEY_HERE\n-----END PRIVATE KEY-----\n",
      "client_email": "lyria-music-generator@your-project.iam.gserviceaccount.com",
      "client_id": "123456789...",
      "auth_uri": "https://accounts.google.com/o/oauth2/auth",
      "token_uri": "https://oauth2.googleapis.com/token",
      "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
      "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/..."
    }
    """.trimIndent()
}
```

**Just copy your entire JSON file content** into the triple quotes!

---

### Step 3: Update Project ID in FeatureFlags (30 seconds)

Make sure your `PROJECT_ID` in `FeatureFlags.kt` matches the `project_id` in your service account
JSON!

```kotlin:36:36:app/src/main/java/com/sparkiai/app/config/FeatureFlags.kt
const val PROJECT_ID = "your-actual-project-id"  // Must match JSON!
```

---

### Step 4: Sync & Build (3 minutes)

1. **Sync Gradle** (to download Google Auth libraries):
    - In Android Studio: File → Sync Project with Gradle Files
    - Or run: `./gradlew build`

2. **Clean and rebuild**:
   ```bash
   ./gradlew clean
   ./gradlew installDebug
   ```

---

### Step 5: Test! (2 minutes)

1. Open app
2. Select "Magic Spark Music Composer"
3. Click "Generate Music"
4. Enter: "Upbeat electronic dance track"
5. Click "Generate"
6. **Should work now!** 🎉

---

## 🔍 Verify Setup

### Check Service Account

```bash
# In Google Cloud Console
https://console.cloud.google.com/iam-admin/serviceaccounts

# Should see:
✓ lyria-music-generator@your-project.iam.gserviceaccount.com
  Role: Vertex AI User
  Keys: 1 active
```

### Check JSON File

Your JSON should have these fields:

```json
{
  "type": "service_account",
  "project_id": "your-project-id",
  "private_key_id": "...",
  "private_key": "-----BEGIN PRIVATE KEY-----\n...",
  "client_email": "...@...iam.gserviceaccount.com",
  ...
}
```

### Check Project ID Match

```
Service Account JSON: "project_id": "my-project-123"
FeatureFlags.kt:      PROJECT_ID = "my-project-123"
                                    ↑
                          MUST MATCH EXACTLY!
```

---

## 🐛 Troubleshooting

### "Authentication failed"

**Check**:

- [ ] Service account created?
- [ ] JSON file downloaded?
- [ ] JSON file in correct location?
- [ ] Project ID matches in both places?
- [ ] Vertex AI User role granted?
- [ ] Gradle synced after adding dependencies?

### "Service account JSON not found"

**If using Option A (assets folder)**:

```
Check file exists at:
app/src/main/assets/vertex_ai_service_account.json
                     ↑
                     Exact filename!
```

**If using Option B (hardcoded)**:

- Check you pasted the JSON correctly
- Must be valid JSON (no syntax errors)
- Include all fields from downloaded JSON

### "Failed to load credentials"

**Check**:

- JSON is valid (paste into https://jsonlint.com/)
- No extra spaces or line breaks
- All quotes are correct
- private_key includes the full key

### "Model not found"

**Check**:

- Vertex AI API is enabled
- Project ID is correct
- Region is "us-central1"
- Model name is "lyria-002"

---

## 📊 What Gets Called

### Authentication Flow

```
1. App needs to call Lyria
2. VertexAIAuth.getAccessToken() called
3. Loads service account JSON
4. Creates GoogleCredentials object
5. Requests OAuth access token from Google
6. Token returned (valid for 1 hour)
7. Token cached for 50 minutes
8. Token used in Authorization header
9. Lyria API call succeeds! 🎉
```

### API Call

```http
POST https://us-central1-aiplatform.googleapis.com/v1/
     projects/YOUR_PROJECT_ID/locations/us-central1/
     publishers/google/models/lyria-002:predict

Headers:
  Authorization: Bearer ya29.c.b0Aaekm1K... (OAuth token)
  Content-Type: application/json

Body:
{
  "instances": [{
    "prompt": "Upbeat electronic dance track"
  }]
}
```

---

## 🔒 Security Best Practices

### DO:

✅ Add `vertex_ai_service_account.json` to `.gitignore`  
✅ Keep JSON file private (never commit to Git)  
✅ Use assets folder or hardcode securely  
✅ Rotate keys if compromised

### DON'T:

❌ Commit JSON to version control  
❌ Share JSON file publicly  
❌ Include JSON in screenshots  
❌ Email JSON file

---

## 📁 File Locations

### Service Account JSON (Choose One)

**Option A - Assets folder**:

```
app/src/main/assets/vertex_ai_service_account.json
```

**Option B - Hardcoded**:

```
app/src/main/java/com/sparkiai/app/network/VertexAIAuth.kt
(in getServiceAccountJson() function)
```

### Code Files

```
app/src/main/java/com/sparkiai/app/network/
  ├── VertexAIAuth.kt (OAuth handler)
  └── LyriaService.kt (updated to use OAuth)
```

---

## ✅ Verification Checklist

Before testing:

- [ ] Service account created in Google Cloud
- [ ] "Vertex AI User" role granted
- [ ] JSON key file downloaded
- [ ] JSON file added to project (Option A or B)
- [ ] Project ID matches in FeatureFlags.kt
- [ ] Vertex AI API enabled
- [ ] Gradle synced (dependencies downloaded)
- [ ] App rebuilt
- [ ] Ready to test!

---

## 🎵 After Setup

Once working, users can:

- Generate actual 30-second music tracks
- Get high-quality WAV files (48kHz)
- Play in-app
- Download to device
- Build music library
- 10 FREE songs, then $0.06 each

---

## 🚀 Quick Commands

```bash
# Sync Gradle (download Google Auth libraries)
./gradlew build

# Clean and rebuild
./gradlew clean
./gradlew installDebug

# Test
# → Open app
# → Music Composer
# → Generate Music
# → Should work! 🎉
```

---

## 💡 Pro Tips

1. **Test with small prompt first**: "Happy music"
2. **Check logs** for detailed errors
3. **Verify JSON is valid** before adding
4. **Keep service account key safe** (never commit!)
5. **Can create multiple keys** if needed (one per environment)

---

## 📞 Still Having Issues?

### Check Android Studio Logcat

Filter for: `LyriaService` or `VertexAIAuth`

Look for:

```
✅ Credentials loaded successfully
✅ Access token obtained successfully  
✅ Music generated successfully
```

Or errors:

```
❌ Failed to load credentials
❌ Failed to get access token
❌ Lyria API failed (401)
```

### Common Error Codes

- **401/403**: Authentication problem (check JSON, role)
- **404**: Model not found (check Vertex AI enabled, project ID)
- **429**: Rate limit (wait a moment, try again)
- **500+**: Google server error (try again later)

---

## 🎉 Once Working

You'll have:

- ✅ Full Lyria music generation
- ✅ Real 30-second WAV files
- ✅ 10 free songs per user
- ✅ Music library
- ✅ Complete feature ready!

**The OAuth setup is the last piece!**

---

**Next**: Follow Step 1-5 above, then test!  
**Time**: ~10 minutes total  
**Result**: Working music generation! 🎵✨
