# Quick Start Guide - Release Your App Now! 🚀

This is the streamlined version. For full details, see `RELEASE_INSTRUCTIONS.md`.

## Step 1: Generate Your Keystore (5 minutes)

Open terminal/PowerShell in your project root and run:

```powershell
keytool -genkey -v -keystore sparkifire-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias sparkifire
```

**Answer the prompts:**

- Password: Choose a strong password (SAVE THIS!)
- Re-enter password: Same password
- First and last name: Your name
- Organizational unit: Your team/company (or "Independent")
- Organization: Your company (or leave blank)
- City: Your city
- State: Your state
- Country code: US (or your country)
- Is this correct? yes

**CRITICAL**: Back up the `sparkifire-release.jks` file that was just created!

---

## Step 2: Create keystore.properties (2 minutes)

Create a new file called `keystore.properties` in your project root with this content:

```properties
storeFile=sparkifire-release.jks
storePassword=YOUR_PASSWORD_FROM_STEP_1
keyAlias=sparkifire
keyPassword=YOUR_PASSWORD_FROM_STEP_1
```

Replace `YOUR_PASSWORD_FROM_STEP_1` with the actual password you created.

---

## Step 3: Build Release APK (2 minutes)

### Option A: Using Android Studio

1. **Build** → **Generate Signed Bundle / APK**
2. Select **Android App Bundle**
3. Click **Next**
4. Click **Create new...** or choose existing keystore
5. Browse to `sparkifire-release.jks`
6. Enter your password (from Step 1)
7. Key alias: `sparkifire`
8. Enter key password (same as store password)
9. Click **Next**
10. Select **release**
11. Check **V1** and **V2** signature versions
12. Click **Finish**

### Option B: Using Terminal (PowerShell)

```powershell
./gradlew bundleRelease
```

If using Command Prompt on Windows:

```cmd
gradlew.bat bundleRelease
```

**Your AAB file will be at:**
`app\build\outputs\bundle\release\app-release.aab`

---

## Step 4: Test on Device (10 minutes)

Build APK for testing:

```powershell
./gradlew assembleRelease
```

**Your APK file will be at:**
`app\build\outputs\apk\release\app-release.apk`

**Install it:**

1. Copy APK to your Android device
2. Enable "Install from Unknown Sources" in device settings
3. Open the APK file and install
4. Test ALL features thoroughly

---

## Step 5: Upload to Google Play Console (15 minutes)

1. Go to [Google Play Console](https://play.google.com/console)
2. Create/select your app
3. Go to **Production** → **Create new release**
4. Upload the `app-release.aab` file
5. Fill in release notes (see `PLAY_STORE_LISTING.md`)
6. Review and submit

---

## Common Issues

### "Keystore not found"

- Make sure `sparkifire-release.jks` is in your project root
- Check the path in `keystore.properties`

### "Wrong password"

- Double-check passwords in `keystore.properties`
- No extra spaces or quotes

### Build fails

- Clean project: **Build** → **Clean Project**
- Rebuild: **Build** → **Rebuild Project**
- Try again

### Release APK crashes but debug works

- Check ProGuard rules (already configured)
- Check logs: `adb logcat`

---

## What You Need for Google Play Store

Before uploading, prepare these:

✅ **Already Done:**

- [x] App built and tested
- [x] Release configuration set up
- [x] ProGuard rules configured

📝 **You Need to Prepare:**

- [ ] App icon (512x512 PNG)
- [ ] Feature graphic (1024x500 PNG/JPG)
- [ ] At least 2 screenshots (1080x1920)
- [ ] Privacy Policy (use template in `PRIVACY_POLICY_TEMPLATE.md`)
- [ ] Privacy Policy hosted online (GitHub Pages, your website, etc.)
- [ ] App description (see `PLAY_STORE_LISTING.md`)
- [ ] Support email address

---

## Timeline Estimate

| Task | Time |
|------|------|
| Generate keystore | 5 min |
| Create keystore.properties | 2 min |
| Build release APK/AAB | 2-5 min |
| Test on device | 10-30 min |
| Prepare graphics | 1-3 hours |
| Write/host privacy policy | 30-60 min |
| Set up Play Console | 30-60 min |
| Upload and submit | 15-30 min |
| **Total** | **3-6 hours** |

Google review: 1-7 days (usually 2-3 days)

---

## Next Steps After This

1. ✅ Build release APK/AAB (you just did this!)
2. 📸 Take screenshots of your app
3. 🎨 Create app icon and feature graphic
4. 📝 Write and host privacy policy
5. 📱 Set up Google Play Console
6. 📤 Upload and submit

**See `PRE_LAUNCH_CHECKLIST.md` for the complete checklist!**

---

## Need Help?

- Full instructions: `RELEASE_INSTRUCTIONS.md`
- Complete checklist: `PRE_LAUNCH_CHECKLIST.md`
- Store listing templates: `PLAY_STORE_LISTING.md`
- Privacy policy template: `PRIVACY_POLICY_TEMPLATE.md`

**You've got this! 🔥**
