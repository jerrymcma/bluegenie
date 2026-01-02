# 💳 Stripe Integration Guide for Android

## ✅ Simple Approach: WebView Checkout

Since you're already using Stripe in the web app, we'll use a **WebView** to show the Stripe checkout. This is the easiest and most maintainable approach.

---

## 🔑 Using Same Keys as Web App

### Supabase Keys (Safe to Reuse)
```properties
# In local.properties - these are the SAME as your web app
SUPABASE_URL=https://dvrrgfrclkxoseioywek.supabase.co
SUPABASE_ANON_KEY=eyJhbGc... (your actual anon key)
```

**Why it's safe:**
- ✅ Supabase anon keys are **designed to be public**
- ✅ They're already in your web app's client-side JavaScript
- ✅ Security comes from Row Level Security (RLS) policies
- ✅ Same key works for web, Android, iOS

### Stripe Keys (Safe to Reuse)
```properties
# In local.properties - these are the SAME as your web app
STRIPE_PUBLISHABLE_KEY=pk_live_... (or pk_test_...)
```

**Why it's safe:**
- ✅ Publishable keys are **meant to be public**
- ✅ They're already in your web app's frontend
- ✅ They can only create checkout sessions, not charge cards
- ⚠️ **NEVER** add your Secret Key (sk_live_...) to Android app

---

## 📝 Implementation Steps

### Step 1: Add Stripe Key to Build Config

```kotlin
// In app/build.gradle.kts
buildConfigField(
    "String",
    "STRIPE_PUBLISHABLE_KEY",
    "\"${localProperties.getProperty("STRIPE_PUBLISHABLE_KEY", "")}\""
)
```

### Step 2: Create Stripe Checkout Helper

```kotlin
// File: app/src/main/java/com/sparkiai/app/utils/StripeCheckoutHelper.kt
package com.sparkiai.app.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts

object StripeCheckoutHelper {
    
    /**
     * Open Stripe checkout in browser
     * Simple approach - redirects to your web app's payment page
     */
    fun openCheckout(context: Context, userId: String, userEmail: String) {
        // Use your web app's checkout URL
        val checkoutUrl = "https://your-web-app.com/checkout?userId=$userId&email=$userEmail"
        
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(checkoutUrl))
        context.startActivity(intent)
    }
    
    /**
     * Alternative: Use WebView for in-app checkout
     * Better UX - user stays in app
     */
    fun openCheckoutWebView(
        activity: ComponentActivity,
        userId: String,
        userEmail: String,
        onSuccess: () -> Unit,
        onCancel: () -> Unit
    ) {
        // Create WebView dialog/activity
        val checkoutUrl = "https://your-web-app.com/checkout?userId=$userId&email=$userEmail"
        
        // TODO: Show WebView with checkoutUrl
        // Monitor URL changes for success/cancel callbacks
    }
}
```

### Step 3: Update ChatViewModel

```kotlin
// In ChatViewModel.kt
class ChatViewModel(...) {
    
    private val _showPaymentSheet = MutableStateFlow(false)
    val showPaymentSheet: StateFlow<Boolean> = _showPaymentSheet.asStateFlow()
    
    fun startPremiumCheckout() {
        val userId = supabaseService.getCurrentUserId()
        val userEmail = supabaseService.getCurrentUserEmail()
        
        if (userId == null || userEmail == null) {
            // Show sign-in modal first
            _showSignInModal.value = true
            return
        }
        
        // Trigger payment flow
        _showPaymentSheet.value = true
    }
    
    suspend fun confirmPremiumPurchase() {
        // After successful payment, reload user profile
        val userId = supabaseService.getCurrentUserId() ?: return
        val email = supabaseService.getCurrentUserEmail() ?: return
        
        val result = supabaseService.getUserProfile(userId, email)
        result.onSuccess { profile ->
            val subscription = supabaseService.buildSubscription(profile)
            _subscription.value = subscription
            _showUpgradeModal.value = false
        }
    }
}
```

### Step 4: Add to ChatScreen

```kotlin
// In ChatScreen.kt
@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    val showPaymentSheet by viewModel.showPaymentSheet.collectAsState()
    val context = LocalContext.current
    
    // Show payment when needed
    LaunchedEffect(showPaymentSheet) {
        if (showPaymentSheet) {
            StripeCheckoutHelper.openCheckout(
                context = context,
                userId = viewModel.getCurrentUserId(),
                userEmail = viewModel.getCurrentUserEmail()
            )
            
            // After user returns to app, check if premium was activated
            viewModel.confirmPremiumPurchase()
        }
    }
}
```

---

## 🌐 Recommended: Use Your Web App's Checkout

### Option A: External Browser (Simplest)

**Pros:**
- ✅ Easiest to implement (5 minutes)
- ✅ No WebView complexity
- ✅ Users trust browser more
- ✅ Stripe's security features work fully

**Cons:**
- ❌ User leaves app temporarily

**Implementation:**
```kotlin
fun upgradeToPremium() {
    val url = "https://your-domain.com/checkout?userId=${userId}"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}
```

### Option B: WebView (Better UX)

**Pros:**
- ✅ User stays in app
- ✅ More seamless experience
- ✅ Can detect success/failure

**Cons:**
- ❌ More code to write
- ❌ Need to handle URL redirects

---

## 🔄 Payment Flow

```
User clicks "Upgrade to Premium"
  ↓
Android app opens browser/WebView
  ↓
Shows your web app's Stripe checkout
  ↓
User completes payment
  ↓
Stripe webhook updates Supabase (is_premium = true)
  ↓
User returns to Android app
  ↓
App reloads user profile from Supabase
  ↓
Premium features unlocked! ✨
```

---

## ✅ What You Need from Web App

1. **Checkout URL**: The URL to your web app's payment page
   - Example: `https://sparkifire.com/checkout`
   - Should accept query params: `userId` and `email`

2. **Success URL**: Where to redirect after payment
   - Example: `https://sparkifire.com/success`
   - Android can detect this URL and close WebView

3. **Cancel URL**: Where to redirect if user cancels
   - Example: `https://sparkifire.com/cancel`

---

## 🔐 Security Checklist

- ✅ **Supabase Anon Key**: Safe to use (public by design)
- ✅ **Stripe Publishable Key**: Safe to use (public by design)
- ❌ **Stripe Secret Key**: NEVER in Android app (server-side only)
- ✅ **Row Level Security**: Ensure RLS enabled in Supabase
- ✅ **Webhook Signing**: Verify Stripe webhooks with signature

---

## 📝 Add to local.properties

```properties
# Supabase (same as web app)
SUPABASE_URL=https://dvrrgfrclkxoseioywek.supabase.co
SUPABASE_ANON_KEY=your-actual-anon-key-here

# Stripe (same as web app)
STRIPE_PUBLISHABLE_KEY=pk_live_... (or pk_test_...)

# Your web app URL
WEB_APP_URL=https://your-domain.com
```

---

## 🧪 Testing

1. **Test with Stripe Test Mode:**
   - Use `pk_test_...` publishable key
   - Use test card: `4242 4242 4242 4242`
   - Verify premium activates in Supabase

2. **Test the Flow:**
   - Click upgrade button
   - Complete payment in browser/WebView
   - Return to app
   - Verify premium features unlock

---

## 💡 Pro Tip: Deep Linking

To automatically return users to your app after payment, set up a deep link:

1. Add deep link in `AndroidManifest.xml`:
```xml
<intent-filter>
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    <data android:scheme="sparkifire"
          android:host="payment-success" />
</intent-filter>
```

2. Use in Stripe success URL:
```
sparkifire://payment-success
```

3. App automatically opens after payment! ✨

---

## ❓ FAQ

**Q: Do I need Stripe's Android SDK?**  
A: No! Using WebView/browser is simpler and more maintainable.

**Q: Is it safe to have Stripe key in BuildConfig?**  
A: Yes! Publishable keys are meant to be public. They're already in your web app's HTML.

**Q: What about Google Play Billing?**  
A: Not needed. Subscriptions via web are allowed by Google Play policies.

**Q: Can users subscribe on web and use premium on Android?**  
A: Yes! Both use the same Supabase backend, so premium status syncs automatically.

---

## 🚀 Next Steps

1. Add Stripe publishable key to `local.properties`
2. Create `StripeCheckoutHelper.kt` 
3. Add "Upgrade" button handler to open checkout
4. Test with Stripe test mode
5. Deploy! 🎉
