# SparkiFire Pre-Launch Checklist

## 📋 Technical Setup

### Build Configuration

- [ ] ✅ Release build configuration added to `build.gradle.kts`
- [ ] ✅ ProGuard rules configured in `proguard-rules.pro`
- [ ] ✅ Code minification enabled (`isMinifyEnabled = true`)
- [ ] ✅ Resource shrinking enabled (`isShrinkResources = true`)
- [ ] Version info set (`versionCode = 1`, `versionName = "1.0.0"`)

### Keystore & Signing

- [ ] Generate release keystore (`sparkifire-release.jks`)
- [ ] Create `keystore.properties` file with signing credentials
- [ ] Backup keystore file in multiple secure locations
- [ ] Document keystore passwords in password manager
- [ ] Verify keystore is in `.gitignore`

### API Keys & Security

- [ ] Gemini API key in `local.properties`
- [ ] Verify API key works in release build
- [ ] Ensure `local.properties` is in `.gitignore`
- [ ] No hardcoded API keys in source code
- [ ] No sensitive data in version control

### Build & Test

- [ ] Clean and rebuild project
- [ ] Build release APK successfully
- [ ] Build release AAB successfully
- [ ] Install and test release APK on physical device
- [ ] Test on multiple Android versions (if possible)
- [ ] Test on different screen sizes
- [ ] Verify ProGuard hasn't broken anything

---

## 🧪 Feature Testing

### Core Functionality

- [ ] All 9 AI personalities work correctly
- [ ] Text messages send and receive responses
- [ ] Voice input works (microphone permission)
- [ ] Voice-to-text conversion accurate
- [ ] Image selection from gallery works
- [ ] Camera photo capture works
- [ ] Image analysis returns correct responses
- [ ] Text + image combined messages work
- [ ] Personality switching works smoothly
- [ ] App doesn't crash during normal use

### UI/UX Testing

- [ ] All text is correct (no typos)
- [ ] "Personalities" button shows in top right
- [ ] Placeholder text: "Say hello, ask anything..."
- [ ] All personality descriptions correct
- [ ] All personality icons display correctly
- [ ] Color scheme looks good
- [ ] Sparki Ultimate has dark red-orange background
- [ ] Spacing in personality selector looks good
- [ ] App is responsive and smooth
- [ ] No layout issues on different screen sizes

### Permissions

- [ ] Internet permission works
- [ ] Microphone permission requested only when needed
- [ ] Camera permission requested only when needed
- [ ] Photo library permission requested only when needed
- [ ] App works without optional permissions
- [ ] Permission denial handled gracefully
- [ ] Clear explanation shown for each permission

### Error Handling

- [ ] Network errors handled gracefully
- [ ] API errors show user-friendly messages
- [ ] No crashes on empty input
- [ ] Image upload errors handled
- [ ] Voice input errors handled
- [ ] Loading states show correctly
- [ ] Timeout handling works

---

## 📱 Google Play Console Setup

### Account & App Creation

- [ ] Google Play Developer account created ($25 fee paid)
- [ ] Account verified
- [ ] New app created in Play Console
- [ ] App name set: "SparkiFire"
- [ ] Default language selected

### Store Listing

- [ ] App title: "SparkiFire - AI Chat Assistant" (max 30 chars)
- [ ] Short description written (max 80 chars)
- [ ] Full description written (max 4000 chars)
- [ ] App category selected (Productivity)
- [ ] Tags/keywords added
- [ ] Contact email set up
- [ ] Contact email tested and working

### Graphics & Assets

- [ ] App icon created (512x512 PNG)
- [ ] Feature graphic created (1024x500 PNG/JPG)
- [ ] Minimum 2 screenshots taken (1080x1920)
- [ ] Screenshot captions written
- [ ] All images look professional
- [ ] Images show key features clearly
- [ ] Consider promo video (optional)

### Privacy & Compliance

- [ ] Privacy Policy written
- [ ] Privacy Policy hosted on public URL
- [ ] Privacy Policy URL added to Play Console
- [ ] Data Safety section completed
- [ ] Content rating questionnaire completed
- [ ] Target audience declared (13+)
- [ ] App content declarations completed

### Distribution

- [ ] Countries selected for distribution
- [ ] Pricing set (Free)
- [ ] In-app purchases declared (None for now)
- [ ] Ads declared (No ads)
- [ ] Primary category confirmed
- [ ] Content rating received

---

## 📄 Documentation

### User-Facing

- [ ] Privacy Policy completed and hosted
- [ ] Support email set up
- [ ] Consider creating FAQ page
- [ ] Consider creating website/landing page
- [ ] Social media accounts (optional)

### Developer Documentation

- [ ] ✅ `RELEASE_INSTRUCTIONS.md` created
- [ ] ✅ `PRIVACY_POLICY_TEMPLATE.md` created
- [ ] ✅ `PLAY_STORE_LISTING.md` created
- [ ] ✅ `PRE_LAUNCH_CHECKLIST.md` created (this file)
- [ ] README.md updated (optional)
- [ ] Changelog started (optional)

---

## 🔐 Security Final Checks

### Code Security

- [ ] No API keys in code
- [ ] No passwords in code
- [ ] No debug logs in release build
- [ ] HTTPS for all network calls
- [ ] Input validation in place
- [ ] No SQL injection risks (N/A - no SQL)
- [ ] No XSS risks (N/A - mobile app)

### File Security

- [ ] Keystore backed up securely
- [ ] Keystore NOT in git
- [ ] `keystore.properties` NOT in git
- [ ] `local.properties` NOT in git
- [ ] Sensitive files in `.gitignore`

---

## 🚀 Release Build Process

### Building

- [ ] Follow instructions in `RELEASE_INSTRUCTIONS.md`
- [ ] Generate signed AAB for Play Store
- [ ] Generate signed APK for testing
- [ ] Verify signature with: `jarsigner -verify -verbose -certs app-release.aab`
- [ ] Note the AAB file size

### Final Testing

- [ ] Install release APK on test device
- [ ] Complete full app walkthrough
- [ ] Test all personalities one more time
- [ ] Test voice input
- [ ] Test image upload
- [ ] Test on slow network
- [ ] Test airplane mode (expect errors)
- [ ] Verify app icon shows correctly
- [ ] Verify app name shows correctly

---

## 📤 Upload & Submission

### Play Console Upload

- [ ] Create release in Production track
- [ ] Upload signed AAB file
- [ ] Fill in release notes (What's New)
- [ ] Set rollout percentage (start with 100% or 20% for testing)
- [ ] Review all information one final time
- [ ] Check for any warnings or errors
- [ ] Submit for review

### Post-Submission

- [ ] Monitor review status daily
- [ ] Respond quickly to any questions from Google
- [ ] Keep test device handy for any issues
- [ ] Prepare for potential rejection (common first time)
- [ ] Have fixes ready for common rejection reasons

---

## 📊 Post-Launch Monitoring (After Approval)

### Week 1

- [ ] Check crash reports daily
- [ ] Respond to user reviews
- [ ] Monitor download numbers
- [ ] Check app performance metrics
- [ ] Monitor API usage/costs
- [ ] Check rating score
- [ ] Gather user feedback

### Ongoing

- [ ] Set up alerts for crashes
- [ ] Plan feature updates
- [ ] Consider user suggestions
- [ ] Monitor competitors
- [ ] Track monetization threshold
- [ ] Plan premium features
- [ ] Regular updates (monthly or quarterly)

---

## 🎯 Launch Day Tasks

### Before Approval

- [ ] Prepare social media posts
- [ ] Prepare email to friends/family
- [ ] Consider press release (optional)
- [ ] Product Hunt submission plan (optional)
- [ ] Reddit post plan (optional)

### Launch Day (After Approval)

- [ ] Share on social media
- [ ] Email to personal network
- [ ] Post in relevant communities
- [ ] Update website/portfolio
- [ ] Monitor for issues
- [ ] Celebrate! 🎉

---

## ⚠️ Common Issues & Solutions

### "App Not Compatible"

- Check minSdk version (currently 24)
- Verify targetSdk (currently 36)
- Check device requirements

### Crashes on Startup

- Check ProGuard rules
- Verify API key is present
- Check logs: `adb logcat`

### API Not Working

- Verify Gemini API key
- Check network connectivity
- Verify API quotas/limits
- Check for API deprecations

### Low Rating

- Respond to negative reviews professionally
- Fix reported bugs quickly
- Ask happy users for reviews
- Improve features based on feedback

---

## 📞 Support Resources

### If You Need Help

**Google Play Console Help**:

- https://support.google.com/googleplay/android-developer

**Android Developer Docs**:

- https://developer.android.com/studio/publish

**Gemini API Docs**:

- https://ai.google.dev/docs

**Community Support**:

- Stack Overflow
- Reddit: r/androiddev
- Android Developers Discord

---

## ✅ Final Pre-Submission Review

Go through this checklist one more time:

1. **Build**: Release APK/AAB built and tested? ✓
2. **Testing**: All features work in release build? ✓
3. **Content**: All text correct, no typos? ✓
4. **Graphics**: All images ready and uploaded? ✓
5. **Privacy**: Privacy policy written and hosted? ✓
6. **Compliance**: All forms completed? ✓
7. **Security**: No sensitive data exposed? ✓
8. **Backup**: Keystore backed up securely? ✓

**If all checked YES → You're ready to submit! 🚀**

---

**Remember**: The first submission often gets rejected for minor issues. Don't worry! It's normal
and part of the process. Learn from feedback and resubmit.

**Good luck! You've got this! 🔥**
