# 📱 Android App Monetization Implementation Guide

## 🎯 Overview

This guide documents the migration from **pay-as-you-go ($0.06/song)** to the **same Premium subscription model as the Web App**.

### New Monetization Model (Matching Web App):
- ✅ **5 FREE songs** (requires Google Sign-In to track)
- ✅ **$5/month Premium**: 50 songs per month + all 11 personalities unlocked
- ✅ **Free tier**: Only Sparki & Magic Music Spark unlocked
- ✅ **Premium tier**: All 11 personalities + 50 songs/month

---

## 📋 Implementation Checklist

### ✅ Completed

1. **Created Subscription Data Models**
   - `app/src/main/java/com/sparkiai/app/model/Subscription.kt`
   - `UserSubscription` data class
   - `UserProfile` data class
   - `PremiumConstants` object

2. **Added Supabase Integration**
   - Updated `app/build.gradle.kts` with Supabase dependencies
   - Created `app/src/main/java/com/sparkiai/app/network/SupabaseService.kt`
   - Syncs with same backend as Web App

3. **Created UI Components**
   - `app/src/main/java/com/sparkiai/app/ui/components/SubscriptionModals.kt`
   - `SignInModal` - Prompts Google Sign-In
   - `PremiumUpgradeModal` - Shows upgrade benefits

4. **Updated PersonalitySelector**
   - Locks premium personalities (9 out of 11)
   - Shows lock icon on premium personalities
   - Free: Sparki + Magic Music Spark only
   - Premium: All 11 personalities

5. **Updated FeatureFlags**
   - Removed `FreemiumConfig` (pay-as-you-go)
   - Added `PremiumConfig` (subscription model)
   - Removed `COST_PER_SONG_CENTS` pricing

### 🚧 Remaining Tasks

You'll need to complete these manually:

6. **Update ChatViewModel** (High Priority)
   - Add subscription state management
   - Replace `MusicGenerationTracker` with `SupabaseService`
   - Check `subscription.isPremium` instead of `musicTracker.canGenerateMusic()`
   - Add Google Sign-In flow
   - Show modals based on subscription state

7. **Update ChatScreen/MainActivity** (High Priority)
   - Integrate `SignInModal` and `PremiumUpgradeModal`
   - Handle Google Sign-In result
   - Pass subscription state to `PersonalitySelectorDialog`
   - Initialize `SupabaseService`

8. **Update MusicGenerationUI** (Medium Priority)
   - Update `MusicUsageStatsCard` to show "5 free songs" or "X/50 songs"
   - Remove pay-as-you-go cost display
   - Show "Upgrade to Premium" when limit reached
   - Remove `$0.06` references

9. **Add Stripe Integration** (Medium Priority)
   - Use web view to redirect to Stripe checkout
   - Or implement Android In-App Billing (Google Play)
   - Handle payment confirmation
   - Call `supabaseService.activatePremium(userId)`

10. **Update AIPersonality Greetings** (Low Priority)
    - Update locked personality greetings to mention premium
    - Match web app greeting formatting

---

## 🔑 Setup Instructions

### 1. Add Keys to local.properties

```properties
# Add these to your local.properties file:
SUPABASE_URL=YOUR_SUPABASE_URL_HERE
SUPABASE_ANON_KEY=YOUR_SUPABASE_ANON_KEY_HERE
```

### 2. Sync Gradle

After adding keys, sync Gradle to rebuild with the new configuration.

### 3. Test Google Sign-In

Ensure Google Sign-In is working properly:
- Check `BuildConfig.GOOGLE_CLIENT_ID` is set
- Test sign-in flow
- Verify Supabase auth integration

---

## 📝 Key Changes Summary

### Before (Pay-as-you-go):
```kotlin
// Old model
- 5 free songs
- Then $0.06 per song (pay-as-you-go)
- All personalities unlocked
- No sign-in required
- Local tracking only
```

### After (Premium Subscription):
```kotlin
// New model (matches web app)
- 5 free songs (requires Google Sign-In)
- $5/month Premium: 50 songs + all personalities
- Only 2 personalities free (Sparki + Magic Music Spark)
- Sign-in required for song generation
- Supabase backend sync
```

---

## 🔄 User Flow

### New User:
1. Opens app → Can chat with Sparki (default) for free
2. Tries to generate music → **Sign-In Modal appears**
3. Signs in with Google → Gets 5 free songs
4. Uses 5 songs → **Premium Upgrade Modal appears**
5. Upgrades to Premium → Gets 50 songs/month + all personalities

### Existing User (with songs generated):
1. Opens app → Auto-signed in
2. Can generate up to 5 total songs
3. After 5 songs → **Premium Upgrade Modal**

---

## 🎨 UI Changes

### Personality Selector:
- **Free** personalities show normally (2 total)
- **Locked** personalities show:
  - Grayed out appearance
  - Lock icon (🔒)
  - Gradient lock badge
  - Click → Shows upgrade modal

### Music Generation:
- Shows "X of 5 free songs" (non-premium)
- Shows "X of 50 songs this period" (premium)
- No more `$0.06` pricing display
- No more "pay-as-you-go" messaging

### Modals:
- **Sign-In Modal**: Google Sign-In prompt
- **Premium Upgrade Modal**: $5/month benefits

---

## 🧪 Testing Checklist

### Test Scenarios:
- [ ] New user can chat with Sparki without sign-in
- [ ] Trying to generate music shows sign-in modal
- [ ] After sign-in, user gets 5 free songs
- [ ] After 5 songs, upgrade modal appears
- [ ] Premium personalities show lock icon
- [ ] Clicking locked personality shows upgrade modal
- [ ] Premium user sees all personalities unlocked
- [ ] Premium user sees "X/50 songs" counter
- [ ] Sign-out clears subscription state

---

## 🌐 Web App Parity

The Android app now matches the web app:

| Feature | Web App | Android App (New) |
|---------|---------|-------------------|
| Free Songs | 5 (with sign-in) | 5 (with sign-in) ✅ |
| Premium Price | $5/month | $5/month ✅ |
| Premium Songs | 50/month | 50/month ✅ |
| Free Personalities | 2 (Sparki, Magic Music) | 2 (Sparki, Magic Music) ✅ |
| Premium Personalities | All 11 | All 11 ✅ |
| Backend | Supabase | Supabase ✅ |
| Auth | Google OAuth | Google Sign-In ✅ |

---

## 📚 File Reference

### New Files Created:
1. `app/src/main/java/com/sparkiai/app/model/Subscription.kt`
2. `app/src/main/java/com/sparkiai/app/network/SupabaseService.kt`
3. `app/src/main/java/com/sparkiai/app/ui/components/SubscriptionModals.kt`
4. `MONETIZATION_IMPLEMENTATION_GUIDE.md` (this file)
5. `SECURITY_UPDATE_REQUIRED.md` (API key rotation guide)

### Modified Files:
1. `app/build.gradle.kts` - Added Supabase dependencies
2. `app/src/main/java/com/sparkiai/app/config/FeatureFlags.kt` - New premium config
3. `app/src/main/java/com/sparkiai/app/ui/components/PersonalitySelector.kt` - Added locks

### Files to Update (Your Tasks):
1. `app/src/main/java/com/sparkiai/app/viewmodel/ChatViewModel.kt`
2. `app/src/main/java/com/sparkiai/app/ui/screens/ChatScreen.kt`
3. `app/src/main/java/com/sparkiai/app/MainActivity.kt`
4. `app/src/main/java/com/sparkiai/app/ui/components/MusicGenerationUI.kt`

---

## 💡 Implementation Tips

### 1. Start with ChatViewModel
The ChatViewModel is the heart of the subscription system. Add:
```kotlin
private val supabaseService: SupabaseService
private val _subscription = MutableStateFlow(UserSubscription())
private val _showSignInModal = MutableStateFlow(false)
private val _showUpgradeModal = MutableStateFlow(false)
```

### 2. Google Sign-In Flow
```kotlin
// In ChatViewModel or Activity
fun signIn() {
    // 1. Get Google ID Token
    // 2. Call supabaseService.signInWithGoogle(idToken)
    // 3. Load user profile
    // 4. Update subscription state
}
```

### 3. Check Before Music Generation
```kotlin
fun generateMusic(prompt: String) {
    if (!supabaseService.isSignedIn()) {
        _showSignInModal.value = true
        return
    }
    
    if (!subscription.isPremium && subscription.songCount >= 5) {
        _showUpgradeModal.value = true
        return
    }
    
    // Generate music...
}
```

---

## 🔐 Security Notes

1. **NEVER commit local.properties** - Already in .gitignore
2. **Rotate exposed API keys** - See `SECURITY_UPDATE_REQUIRED.md`
3. **Supabase RLS** - Ensure Row Level Security policies are enabled
4. **Anon key is public** - But always verify RLS policies

---

## 📞 Support

If you encounter issues:
- Check that all keys are properly set in `local.properties`
- Verify Supabase connection
- Test Google Sign-In separately
- Check Android logs for errors

---

## ✨ Next Steps

1. Read `SECURITY_UPDATE_REQUIRED.md` and **rotate API keys**
2. Add Supabase credentials to `local.properties`
3. Implement remaining tasks (6-10 above)
4. Test thoroughly
5. Deploy to Play Store

Good luck! 🚀
