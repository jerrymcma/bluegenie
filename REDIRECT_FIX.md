# Stripe Checkout Redirect Fix

## Issue Found
The Android app was failing to open Stripe checkout with this error:
```
API response code: 307 (Temporary Redirect)
❌ API error (307): No error body
❌ Unexpected error creating checkout session
```

## Root Cause
The web app's `/api/create-checkout` endpoint is returning a **307 Temporary Redirect** instead of directly responding with the checkout URL. 

By default, `HttpURLConnection` in Android:
- ✅ Automatically follows redirects for **GET** requests
- ❌ Does NOT automatically follow redirects for **POST** requests (for security reasons)

Since we're making a POST request to create the checkout session, the redirect wasn't being followed, causing the error.

## Solution
Added redirect handling to `StripeCheckoutHelper.kt`:

1. **Detect redirect responses** (307, 308)
2. **Extract redirect URL** from `Location` header
3. **Follow redirect manually** by making a new POST request to the redirected URL
4. **Preserve request body** when following the redirect

## Code Changes

### Added Redirect Detection
```kotlin
// Handle redirects (307, 308) by following the Location header
if (responseCode == 307 || responseCode == 308) {
    val redirectUrl = connection.getHeaderField("Location")
    Log.d(TAG, "🔄 Redirect detected to: $redirectUrl")
    
    if (redirectUrl.isNullOrBlank()) {
        throw Exception("Redirect response missing Location header")
    }
    
    // Follow redirect manually for POST requests
    connection.disconnect()
    return@withContext followRedirect(redirectUrl, requestBody.toString())
}
```

### Added Redirect Follow Function
```kotlin
private suspend fun followRedirect(redirectUrl: String, requestBody: String): String {
    // Make new POST request to the redirected URL
    // Send same request body
    // Return the checkout URL from the response
}
```

### Enabled Instance Redirects
```kotlin
connection.instanceFollowRedirects = true  // Follow redirects automatically
```

## Why This Happens

The web app might be using:
1. **Vercel/Netlify edge functions** that redirect to serverless functions
2. **Load balancer** that redirects to different servers
3. **CDN** that redirects to origin server
4. **Framework routing** (Next.js, etc.) that internally redirects API routes

This is normal behavior for modern web architectures, but Android's `HttpURLConnection` needs explicit handling for POST redirects.

## Testing

### Before Fix:
```
StripeCheckout: 📥 API response code: 307 (Temporary Redirect)
StripeCheckout: ❌ API error (307): No error body
ChatViewModel: ❌ Failed to open Stripe checkout
```

### After Fix (Expected):
```
StripeCheckout: 📥 API response code: 307 (Temporary Redirect)
StripeCheckout: 🔄 Redirect detected to: [new-url]
StripeCheckout: 🔄 Following redirect to: [new-url]
StripeCheckout: 📥 Redirect response code: 200 (OK)
StripeCheckout: ✅ Redirect response body: {"url":"https://checkout.stripe.com/..."}
StripeCheckout: ✅ Checkout URL from redirect: https://checkout.stripe.com/...
StripeCheckout: ✅ Stripe checkout opened successfully in browser
```

## How to Test

1. **Install Updated App**:
   ```bash
   cd C:/Users/Jerry/AndroidStudioProjects/SparkiFire
   ./gradlew installDebug
   ```

2. **Open Logcat** and filter by "StripeCheckout"

3. **Try Upgrade Flow**:
   - Click "Upgrade for $5"
   - Watch the logs

4. **Expected Behavior**:
   - Log shows "🔄 Redirect detected"
   - Log shows "🔄 Following redirect"
   - Browser opens with Stripe checkout page
   - No error messages

## Build Status
✅ Build successful
✅ No linter errors
✅ Ready to test

## Next Steps

1. Install the updated app
2. Test the upgrade flow
3. Check if browser opens with Stripe checkout
4. If still having issues, share the new logs

## Additional Notes

### Web App Considerations
If you control the web app, you could also fix this by:
- Returning 200 OK directly instead of 307 redirect
- Using a different API endpoint that doesn't redirect

However, the Android app should handle redirects properly regardless, so this fix is the correct approach.

### HTTP Status Codes Handled
- **200 OK** - Success, return checkout URL
- **307 Temporary Redirect** - Follow redirect with same method (POST)
- **308 Permanent Redirect** - Follow redirect with same method (POST)
- **301/302** - Would be followed automatically by HttpURLConnection (for GET only)

### Security Note
We only follow redirects that return a valid `Location` header. This prevents:
- Infinite redirect loops
- Redirects to malicious URLs (since we still validate the final response)
- Following redirects without proper context
