# 🔧 Fix "API Issue" - Grant Vertex AI Permission

## 🎯 The Problem

Your service account JSON is correct, but the service account **doesn't have permission** to use
Vertex AI.

Looking at your JSON:

```json
"client_email": "904707581552-compute@developer.gserviceaccount.com"
```

This is the **default compute service account**. It needs the "Vertex AI User" role.

---

## ⚡ Quick Fix (2 Minutes)

### Option 1: Grant Permission to Current Service Account

**Go to**: https://console.cloud.google.com/iam-admin/iam

1. **Find** the service account:
   ```
   904707581552-compute@developer.gserviceaccount.com
   ```

2. **Click the pencil icon** (✏️) to edit

3. **Click "ADD ANOTHER ROLE"**

4. **Search for**: "Vertex AI User"

5. **Select**: "Vertex AI User"

6. **Click "SAVE"**

7. **Done!** Now rebuild and test.

---

### Option 2: Create New Service Account (Recommended)

**Why**: Better to have a dedicated service account for music generation

**Go to**: https://console.cloud.google.com/iam-admin/serviceaccounts

1. **Click**: "+ CREATE SERVICE ACCOUNT"

2. **Name**: `lyria-music-generator`

3. **Role**: "Vertex AI User"

4. **Create JSON key** from KEYS tab

5. **Replace** your current JSON file with the new one

6. **Rebuild** app

---

## 🚀 I Recommend Option 1 (Faster)

Just grant "Vertex AI User" role to your existing service account:

**URL**: https://console.cloud.google.com/iam-admin/iam

**Find**: `904707581552-compute@developer.gserviceaccount.com`

**Add role**: "Vertex AI User"

**Save**

**Then rebuild app**:

```bash
cd C:\Users\Jerry\AndroidStudioProjects\SparkiFire
.\gradlew.bat clean
.\gradlew.bat installDebug
```

**Test again** - should work! 🎵

---

## ✅ How to Verify Permission

### Go to IAM Page

https://console.cloud.google.com/iam-admin/iam

### Find Your Service Account

```
904707581552-compute@developer.gserviceaccount.com
```

### Check Roles Column

Should include:

```
✓ Vertex AI User
```

If missing, add it!

---

## 🎯 Quick Action

**Right now**:

1. Click: https://console.cloud.google.com/iam-admin/iam
2. Edit service account (pencil icon)
3. Add role: "Vertex AI User"
4. Save
5. Rebuild app
6. Test!

**Time**: 2 minutes  
**Result**: Working music generation! 🎵

---

## 📊 After Fixing

### In Logs (should see):

```
✅ Credentials loaded successfully
✅ Access token obtained successfully
✅ Lyria API call successful
✅ Music generated successfully
```

### In App (should see):

```
Sparki: 🎵 Your music is ready! 🎶
        This was FREE! You have 9 free songs remaining.
```

---

## 🐛 If Still Having Issues

### Check These URLs:

**1. Vertex AI enabled?**
https://console.cloud.google.com/apis/library/aiplatform.googleapis.com
Should say: "API enabled" ✅

**2. Service account has role?**
https://console.cloud.google.com/iam-admin/iam
Should show: "Vertex AI User" ✅

**3. Billing enabled?**
https://console.cloud.google.com/billing
Should have linked account ✅

---

## ✅ Summary

**Problem**: Service account lacks "Vertex AI User" role  
**Solution**: Grant the role in IAM settings  
**Time**: 2 minutes  
**Then**: Rebuild and test!

**DO THIS NOW**:
https://console.cloud.google.com/iam-admin/iam
→ Edit service account
→ Add "Vertex AI User" role
→ Save
→ Rebuild app
→ Test! 🎵

---

**You're one permission away from working music generation!** 🚀✨
