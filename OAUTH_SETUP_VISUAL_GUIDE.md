# 🔐 OAuth Service Account - Visual Step-by-Step Guide

## 🎯 What You Need to Do

Create a service account in Google Cloud and add the JSON key to your app.

---

## 📸 Step-by-Step with Visual Guide

### STEP 1: Go to Service Accounts Page

**URL**: https://console.cloud.google.com/iam-admin/serviceaccounts

You'll see a page that says:

```
Service Accounts
[+ CREATE SERVICE ACCOUNT button at top]
```

---

### STEP 2: Create Service Account

Click the **"+ CREATE SERVICE ACCOUNT"** button (blue, at top)

You'll see a form with 3 steps:

---

### STEP 3: Service Account Details (Page 1 of 3)

Fill in:

```
Service account name: lyria-music-generator

Service account ID: lyria-music-generator
                   (auto-filled from name)

Service account description: 
  "Service account for Lyria music generation in Sparki app"
```

Click **"CREATE AND CONTINUE"** (blue button at bottom)

---

### STEP 4: Grant Permissions (Page 2 of 3)

You'll see:

```
Grant this service account access to project (optional)

[Select a role dropdown]
```

**Click the "Select a role" dropdown**

In the search box, type: **"Vertex AI User"**

You'll see:

```
☐ Vertex AI Administrator
☑ Vertex AI User  ← SELECT THIS ONE!
☐ Vertex AI Viewer
```

Click **"Vertex AI User"**

Then click **"CONTINUE"** (blue button)

---

### STEP 5: Grant Users Access (Page 3 of 3)

You'll see:

```
Grant users access to this service account (optional)
```

**Just click "DONE"** (skip this step - not needed)

---

### STEP 6: Create JSON Key

You'll be back at the Service Accounts list. You should see:

```
Service Accounts (1)

Email: lyria-music-generator@your-project.iam.gserviceaccount.com
Name: lyria-music-generator
```

**Click on your new service account email** (the blue link)

You'll see the service account details page.

**Click the "KEYS" tab** (near top)

Click **"ADD KEY"** → **"Create new key"**

A popup appears:

```
Create private key for "lyria-music-generator"

Key type:
○ P12
● JSON  ← SELECT THIS!
```

Click **"CREATE"**

**A JSON file downloads automatically!** 📥

The file will be named something like:

```
your-project-name-a1b2c3d4e5f6.json
```

---

### STEP 7: Add JSON to Your App

#### Option A: Assets Folder (Recommended)

1. **In Android Studio**, right-click on `app/src/main/`

2. Select: **New → Directory**

3. Type: `assets` (if it doesn't exist)

4. **Find your downloaded JSON file** in your Downloads folder

5. **Rename it** to: `vertex_ai_service_account.json`

6. **Drag and drop** or copy it into: `app/src/main/assets/`

Final location:

```
app/src/main/assets/vertex_ai_service_account.json
```

#### Option B: Hardcode in VertexAIAuth.kt

1. **Open the JSON file** in a text editor

2. **Copy ALL the content** (should look like):

```json
{
  "type": "service_account",
  "project_id": "sparkifire-ai-123456",
  "private_key_id": "abc123def456...",
  "private_key": "-----BEGIN PRIVATE KEY-----\nMIIEvQIB...\n-----END PRIVATE KEY-----\n",
  "client_email": "lyria-music-generator@sparkifire-ai-123456.iam.gserviceaccount.com",
  "client_id": "1234567890...",
  "auth_uri": "https://accounts.google.com/o/oauth2/auth",
  "token_uri": "https://oauth2.googleapis.com/token",
  "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
  "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/lyria-music-generator%40..."
  "universe_domain": "googleapis.com"
}
```

3. **Open**: `app/src/main/java/com/sparkiai/app/network/VertexAIAuth.kt`

4. **Find line ~105** (the `getServiceAccountJson()` function)

5. **Replace**:

```kotlin
private fun getServiceAccountJson(): String {
    return ""  // ← Replace this
}
```

**With**:

```kotlin
private fun getServiceAccountJson(): String {
    return """
    PASTE YOUR ENTIRE JSON HERE
    """.trimIndent()
}
```

**Example**:

```kotlin
private fun getServiceAccountJson(): String {
    return """
    {
      "type": "service_account",
      "project_id": "sparkifire-ai-123456",
      "private_key_id": "a1b2c3d4...",
      "private_key": "-----BEGIN PRIVATE KEY-----\nMIIEvQIB...\n-----END PRIVATE KEY-----\n",
      "client_email": "lyria-music-generator@sparkifire-ai-123456.iam.gserviceaccount.com",
      "client_id": "1234567890",
      "auth_uri": "https://accounts.google.com/o/oauth2/auth",
      "token_uri": "https://oauth2.googleapis.com/token",
      "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
      "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/lyria-music-generator%40sparkifire-ai-123456.iam.gserviceaccount.com",
      "universe_domain": "googleapis.com"
    }
    """.trimIndent()
}
```

---

### STEP 8: Update .gitignore (Security!)

Make sure your `.gitignore` includes:

```
# Service Account JSON (NEVER COMMIT!)
vertex_ai_service_account.json
**/vertex_ai_service_account.json
*.json
app/src/main/assets/vertex_ai_service_account.json
```

This prevents accidentally committing your private key!

---

### STEP 9: Sync Gradle & Build

```bash
# Sync to download Google Auth libraries
./gradlew build

# Clean and rebuild
./gradlew clean
./gradlew installDebug
```

**Wait for build to complete** (~1-2 minutes)

---

### STEP 10: Test Music Generation!

1. **Open Sparki app**
2. **Select "Magic Spark Music Composer"**
3. **Click "Generate Music"** button
4. **Enter prompt**: "Upbeat electronic dance track"
5. **Click "Generate"**
6. **Wait 10-30 seconds**
7. **Success!** 🎵 Real music file created!

---

## ✅ Verification Checklist

### Before Testing

- [ ] Service account created in Google Cloud
- [ ] "Vertex AI User" role assigned
- [ ] JSON key file downloaded
- [ ] JSON file renamed to `vertex_ai_service_account.json`
- [ ] JSON file added to `app/src/main/assets/` (Option A)
  OR JSON content pasted in `VertexAIAuth.kt` (Option B)
- [ ] Project ID in FeatureFlags matches JSON
- [ ] `.gitignore` updated (security!)
- [ ] Gradle synced (`./gradlew build`)
- [ ] App rebuilt (`./gradlew clean && ./gradlew installDebug`)

### After Testing

- [ ] Music generation works
- [ ] Logs show "✅ Credentials loaded successfully"
- [ ] Logs show "✅ Access token obtained"
- [ ] Logs show "✅ Music generated successfully"
- [ ] WAV file created in library
- [ ] Can play music (when playback implemented)
- [ ] Usage counter works (decrements)

---

## 🎯 Quick Reference

### Service Account Details

**What to create**:

- **Name**: lyria-music-generator
- **Role**: Vertex AI User
- **Key type**: JSON

**Where it goes**:

- **Option A**: `app/src/main/assets/vertex_ai_service_account.json`
- **Option B**: Hardcoded in `VertexAIAuth.kt`

### URLs You Need

**Create service account**:
https://console.cloud.google.com/iam-admin/serviceaccounts

**Enable Vertex AI**:
https://console.cloud.google.com/apis/library/aiplatform.googleapis.com

**Your project**:
https://console.cloud.google.com/

---

## 🔧 Advanced: Multiple Environments

### Development vs Production

**Development**:

```
Service account: lyria-dev@project.iam.gserviceaccount.com
JSON file: vertex_ai_service_account_dev.json
```

**Production**:

```
Service account: lyria-prod@project.iam.gserviceaccount.com  
JSON file: vertex_ai_service_account_prod.json
```

Update `VertexAIAuth.kt` to use different files based on build type.

---

## 🎉 You're Almost There!

**What you've done**:
✅ Added Google Auth dependencies  
✅ Created VertexAIAuth handler  
✅ Updated LyriaService to use OAuth  
✅ Integrated with ChatViewModel

**What you need to do**:

1. Create service account (5 min)
2. Download JSON key
3. Add to project
4. Build & test!

**Then**:
🎵 **REAL MUSIC GENERATION WORKS!** 🎉

---

**Total time**: ~10 minutes  
**Difficulty**: Medium (just follow steps carefully)  
**Result**: Working Lyria music generation with OAuth!

**Let's do this!** 🚀🎵
