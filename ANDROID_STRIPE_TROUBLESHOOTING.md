# Android Stripe Checkout Troubleshooting Guide

## Problem
The Android app cannot open Stripe checkout, while the web app works perfectly.

## Recent Changes Made

### 1. Enhanced Logging in `StripeCheckoutHelper.kt`
- Added detailed logging at every step of the checkout process
- Added validation for checkout URL before opening browser
- Added check for browser availability on device
- Improved error messages with specific error types

### 2. Enhanced Error Handling in `ChatViewModel.kt`
- Added comprehensive logging for checkout initiation
- Improved user-facing error messages
- Modal now reopens on error so user can retry
- Better context validation

### 3. Added Network Security Configuration
- Created `network_security_config.xml` to ensure proper HTTPS handling
- Configured AndroidManifest to use the network security config
- This ensures Android trusts system certificates for HTTPS connections

## How to Debug

### Step 1: Check the Logs
After clicking the "Upgrade for $5" button, check Android Studio Logcat for:

1. **ChatViewModel logs** (filter by "ChatViewModel"):
   ```
   🛒 Starting premium checkout
   Context: ✓ or ✗
   User ID: [user-id]
   Email: [email]
   ```

2. **StripeCheckoutHelper logs** (filter by "StripeCheckout"):
   ```
   🛒 Creating Stripe checkout session for user: [user-id]
   Email: [email]
   Web App URL: [url]
   🌐 Calling API: [api-url]
   📤 Request body: [json]
   📥 API response code: [code]
   ✅ API response body: [response]
   ✅ Checkout URL: [stripe-url]
   ✅ Stripe checkout opened successfully in browser
   ```

### Step 2: Common Issues and Solutions

#### Issue 1: Network Error / Cannot Connect
**Symptoms**: Logs show connection errors or timeouts

**Solutions**:
1. Check device internet connection
2. Try on Wi-Fi instead of mobile data (or vice versa)
3. Check if firewall/VPN is blocking the connection
4. Verify `WEB_APP_URL` in `local.properties` is correct

#### Issue 2: Empty or Invalid Checkout URL
**Symptoms**: API returns 200 but URL is empty or invalid

**Solutions**:
1. Check web app API endpoint `/api/create-checkout`
2. Verify Stripe keys are configured in web app
3. Check web app logs for errors

#### Issue 3: No Browser Available
**Symptoms**: Error message "No browser app available"

**Solutions**:
1. Install Chrome, Firefox, or any web browser on the device
2. Check if default browser is set in device settings

#### Issue 4: API Returns Error (4xx or 5xx)
**Symptoms**: API response code is not 200

**Solutions**:
1. Check web app server logs
2. Verify API endpoint is deployed and working
3. Test API endpoint manually with curl or Postman:
   ```bash
   curl -X POST https://sparkiai.app/api/create-checkout \
     -H "Content-Type: application/json" \
     -d '{"userId":"test-user","customerEmail":"test@example.com"}'
   ```

### Step 3: Verify Configuration

Check these configuration values:

1. **local.properties**:
   ```
   WEB_APP_URL=https://sparkiai.app
   ```

2. **Web app is accessible**:
   - Open https://sparkiai.app in browser
   - Try the upgrade flow on web app
   - Check if `/api/create-checkout` endpoint exists

3. **Supabase authentication**:
   - User must be signed in
   - Check logs for "User ID" and "Email" values

### Step 4: Test with Debug Build

1. Build and install debug APK:
   ```bash
   ./gradlew installDebug
   ```

2. Open Android Studio Logcat

3. Filter by:
   - `StripeCheckout` for Stripe-specific logs
   - `ChatViewModel` for checkout flow logs
   - `System.err` for any uncaught errors

4. Try the upgrade flow and watch the logs in real-time

### Step 5: Test API Manually

You can test the API endpoint manually to ensure it's working:

**PowerShell**:
```powershell
$body = @{
    userId = "test-user-id"
    customerEmail = "test@example.com"
} | ConvertTo-Json

Invoke-WebRequest -Uri "https://sparkiai.app/api/create-checkout" `
    -Method POST `
    -Headers @{"Content-Type"="application/json"} `
    -Body $body
```

**Expected Response**:
```json
{
  "url": "https://checkout.stripe.com/c/pay/cs_test_..."
}
```

## Configuration Checklist

- [ ] `WEB_APP_URL` in `local.properties` is set to `https://sparkiai.app`
- [ ] Device has internet connection
- [ ] Device has a web browser installed
- [ ] User is signed in with Google
- [ ] Web app API endpoint is working
- [ ] Stripe keys are configured in web app
- [ ] Network security config is properly configured

## Expected Flow

1. User clicks "Upgrade for $5" button
2. Modal closes
3. Chat shows "Opening payment page... 💳"
4. API call is made to `https://sparkiai.app/api/create-checkout`
5. API returns Stripe checkout URL
6. Browser opens with Stripe checkout page
7. Chat shows "✅ Payment page opened! Complete your purchase..."
8. User completes payment in browser
9. User returns to app
10. App checks premium status and updates UI

## Still Not Working?

If you've tried all the steps above and it's still not working:

1. **Share the logs**: Copy the Logcat output (filtered by "StripeCheckout" and "ChatViewModel")
2. **Check web app**: Verify the web app checkout works on the same device's browser
3. **Try different network**: Test on different Wi-Fi or mobile data
4. **Check device settings**: Ensure no battery optimization or data saver is blocking the app

## Quick Fixes to Try

1. **Rebuild the app**:
   ```bash
   ./gradlew clean
   ./gradlew installDebug
   ```

2. **Clear app data**:
   - Go to Settings > Apps > SparkiFire
   - Clear Storage & Cache
   - Sign in again

3. **Test on different device/emulator**:
   - Sometimes device-specific issues occur
   - Try on Android emulator or different physical device

## Contact Support

If none of the above solutions work, please provide:
1. Complete Logcat logs (filtered by "StripeCheckout" and "ChatViewModel")
2. Android version and device model
3. Whether web app checkout works on the same device
4. Network type (Wi-Fi, mobile data, VPN, etc.)
