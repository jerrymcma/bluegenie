# Testing Stripe Checkout on Android

## Quick Test Steps

### 1. Build and Install Debug Build
```bash
cd C:/Users/Jerry/AndroidStudioProjects/SparkiFire
./gradlew clean
./gradlew installDebug
```

### 2. Open Android Studio Logcat
- Open Android Studio
- Go to View > Tool Windows > Logcat
- Connect your device or start emulator
- Click on the filter dropdown and select "Show only selected application"

### 3. Add Custom Filters
Create two custom filters in Logcat:

**Filter 1: Stripe Checkout**
- Name: Stripe Checkout
- Log Tag: StripeCheckout
- Log Level: Debug

**Filter 2: Chat ViewModel**
- Name: Chat ViewModel
- Log Tag: ChatViewModel
- Log Level: Debug

### 4. Test the Flow

1. **Sign in with Google**
   - Open the app
   - Click on "Generate Music" or try to generate a song
   - Sign in when prompted
   - Check logs for successful sign-in

2. **Try to upgrade**
   - Generate 5 free songs (or just click upgrade)
   - Click "Upgrade for $5" button
   - Watch the logs carefully

3. **Expected Logs** (in order):

```
ChatViewModel: 🛒 Starting premium checkout
ChatViewModel:    Context: ✓
ChatViewModel:    User ID: [uuid]
ChatViewModel:    Email: [email@example.com]
ChatViewModel: 📱 Initiating Stripe checkout...
StripeCheckout: 🛒 Creating Stripe checkout session for user: [uuid]
StripeCheckout:    Email: [email@example.com]
StripeCheckout:    Web App URL: https://sparkiai.app
StripeCheckout: 🌐 Calling API: https://sparkiai.app/api/create-checkout
StripeCheckout:    userId: [uuid]
StripeCheckout:    customerEmail: [email@example.com]
StripeCheckout: 📤 Request body: {"userId":"...","customerEmail":"..."}
StripeCheckout: 📥 API response code: 200 (OK)
StripeCheckout: ✅ API response body: {"url":"https://checkout.stripe.com/..."}
StripeCheckout: ✅ Checkout URL: https://checkout.stripe.com/...
StripeCheckout: ✅ Stripe checkout opened successfully in browser
ChatViewModel: ✅ Stripe checkout opened for user: [email]
```

4. **What Should Happen**:
   - Toast message: "Connecting to payment server..."
   - Modal closes
   - Chat message: "Opening payment page... 💳"
   - Browser opens with Stripe checkout page
   - Toast message: "Opening browser..."
   - Chat message: "✅ Payment page opened! Complete your purchase..."

### 5. Common Errors and What They Mean

#### Error: "Network Error / Cannot Connect"
**Logs will show**:
```
StripeCheckout: ❌ Network error: Cannot resolve host
```
or
```
StripeCheckout: ❌ Network error: Connection failed
```

**Solution**: Check device internet connection

#### Error: "Connection Timeout"
**Logs will show**:
```
StripeCheckout: ❌ Network error: Request timed out
```

**Solution**: 
- Check if web app is running
- Check if device has slow/unstable internet
- Try on different network

#### Error: "API Error (4xx or 5xx)"
**Logs will show**:
```
StripeCheckout: ❌ API error (404): {"error":"Not found"}
```

**Solution**:
- Check if web app is deployed
- Check if `/api/create-checkout` endpoint exists
- Check web app server logs

#### Error: "No browser app available"
**Logs will show**:
```
StripeCheckout: ❌ No browser app found to open URL
```

**Solution**: Install Chrome, Firefox, or any browser on device

#### Error: "Invalid checkout URL"
**Logs will show**:
```
StripeCheckout: ❌ Response missing 'url' field
```
or
```
StripeCheckout: ❌ Empty checkout URL in response
```

**Solution**: 
- Check web app Stripe configuration
- Check web app server logs
- Ensure Stripe keys are set in web app

### 6. Manual API Test

Test the API endpoint directly to verify it's working:

**Using PowerShell**:
```powershell
$body = @{
    userId = "test-user-id"
    customerEmail = "test@example.com"
} | ConvertTo-Json

$response = Invoke-WebRequest -Uri "https://sparkiai.app/api/create-checkout" `
    -Method POST `
    -Headers @{"Content-Type"="application/json"} `
    -Body $body `
    -UseBasicParsing

Write-Host "Status Code:" $response.StatusCode
Write-Host "Response:" $response.Content
```

**Expected Response**:
```
Status Code: 200
Response: {"url":"https://checkout.stripe.com/c/pay/cs_test_..."}
```

### 7. Test on Web App (for comparison)

1. Open https://sparkiai.app in browser
2. Sign in with same Google account
3. Try to upgrade to premium
4. Verify it works on web

If web works but Android doesn't:
- Compare the network requests (use Chrome DevTools on web)
- Check if Android is sending the same request
- Look for differences in request headers or body

### 8. Debug Build vs Release Build

**Debug Build** (easier to debug):
```bash
./gradlew installDebug
```
- Logs are visible
- ProGuard is disabled
- Easy to debug

**Release Build** (production-like):
```bash
./gradlew installRelease
```
- Some logs might be stripped
- ProGuard is enabled
- Must be signed with release key

Start with debug build for testing.

### 9. Device Requirements

Make sure your test device has:
- [ ] Internet connection (Wi-Fi or mobile data)
- [ ] Web browser installed (Chrome, Firefox, etc.)
- [ ] Android 7.0+ (API 24+)
- [ ] No VPN or firewall blocking connections
- [ ] Battery optimization disabled for app (optional, but helps)

### 10. Emulator Testing

If testing on emulator:
- Use a recent Android version (API 30+)
- Ensure Play Store is enabled on the AVD
- Make sure Chrome or browser is installed
- Check network settings in emulator

### 11. Real Device Testing

If testing on real device:
- Enable USB debugging
- Install via Android Studio or `adb install`
- Use Logcat over USB for real-time logs
- Test on both Wi-Fi and mobile data

## Troubleshooting Checklist

Go through this checklist if checkout isn't working:

1. **Configuration**
   - [ ] `WEB_APP_URL` in `local.properties` is `https://sparkiai.app`
   - [ ] Gradle sync completed successfully
   - [ ] App has been rebuilt after changing configuration

2. **Network**
   - [ ] Device has internet connection
   - [ ] Can open https://sparkiai.app in device browser
   - [ ] No VPN or firewall blocking requests
   - [ ] Not behind corporate proxy

3. **Authentication**
   - [ ] User is signed in with Google
   - [ ] User ID and email are not null in logs
   - [ ] Supabase session is valid

4. **Device/Environment**
   - [ ] Web browser is installed on device
   - [ ] Android version is 7.0+ (API 24+)
   - [ ] App has INTERNET permission
   - [ ] Network security config is properly set

5. **Backend**
   - [ ] Web app is deployed and accessible
   - [ ] `/api/create-checkout` endpoint exists
   - [ ] Stripe keys are configured in web app
   - [ ] Web app checkout works in browser

## Getting Help

If you've gone through all the steps and it still doesn't work:

1. **Capture full logs**:
   - Select all logs in Logcat
   - Copy to clipboard
   - Save to file

2. **Document the issue**:
   - What device/emulator you're using
   - Android version
   - What network you're on (Wi-Fi, mobile data)
   - Whether web app works on same device
   - Complete error logs

3. **Try simple tests**:
   - Does browser open when clicking a link in chat?
   - Can you open https://sparkiai.app manually in browser?
   - Does Google Sign-In work?
   - Can you generate free songs?

4. **Compare with web**:
   - Open https://sparkiai.app in device browser
   - Try upgrade flow on web
   - Check browser console for errors
   - Compare network requests

## Quick Fixes

Before diving deep into debugging, try these quick fixes:

1. **Rebuild Everything**
   ```bash
   ./gradlew clean
   ./gradlew build
   ./gradlew installDebug
   ```

2. **Clear App Data**
   - Settings > Apps > SparkiFire
   - Storage > Clear Storage
   - Cache > Clear Cache
   - Restart app and sign in again

3. **Restart Device**
   - Sometimes network issues are resolved with a restart

4. **Try Different Network**
   - Switch from Wi-Fi to mobile data (or vice versa)
   - Try different Wi-Fi network

5. **Update Browser**
   - Make sure device browser is up to date
   - Try installing Chrome if not present

## Success Indicators

You'll know it's working when:

✅ Logs show "✅ Stripe checkout opened successfully in browser"
✅ Browser opens automatically with Stripe checkout page
✅ Stripe page shows correct product ($5/month)
✅ User can complete payment
✅ After payment, app shows premium status

## Next Steps After Testing

Once you confirm checkout is working:

1. Test the full payment flow end-to-end
2. Test the webhook to verify premium activation
3. Test renewal flow
4. Test on different devices/Android versions
5. Consider edge cases (no internet, browser crash, etc.)
