# Quick Webhook Test Guide

## 🚀 Quick Test (30 seconds)

### Test from Stripe Dashboard

1. Go to: https://dashboard.stripe.com/webhooks
2. Click on your webhook endpoint
3. Click "Send test webhook" button
4. Select `checkout.session.completed`
5. Click "Send test webhook"
6. ✅ Look for **200 OK** response

### Check Vercel Logs

1. Go to: https://vercel.com/jerry-mcmahons-projects/sparkifire-web
2. Click "Functions" tab
3. Click `stripe-webhook.js`
4. Look for recent invocations
5. ✅ Should see: `✅ Webhook verified successfully`

## 🧪 Test with Stripe CLI (Advanced)

### Setup (one-time)

```powershell
# Install Stripe CLI (if not already installed)
scoop install stripe

# Login to Stripe
stripe login
```

### Forward webhooks to local dev

```powershell
# Start your dev server
cd sparkifire-web
npm run dev

# In another terminal, forward webhooks
stripe listen --forward-to localhost:5173/api/stripe-webhook
```

### Trigger test events

```powershell
# Test checkout completion
stripe trigger checkout.session.completed

# Test subscription creation
stripe trigger customer.subscription.created

# Test subscription update
stripe trigger customer.subscription.updated
```

## 📊 What to Look For

### ✅ Success Logs

```
✅ Webhook verified successfully (live mode): checkout.session.completed Event ID: evt_xxx
💳 Processing checkout.session.completed: cs_xxx
Customer details: { customerEmail: 'user@example.com', userId: 'xxx', sessionId: 'cs_xxx' }
✅ Premium activated successfully for user: xxx Email: user@example.com
✅ Event evt_xxx processed successfully
```

### ❌ Error Logs

```
❌ Webhook signature verification failed
❌ Missing stripe-signature header
❌ Unable to resolve customer identity
```

## 🔍 Debugging

### Check Environment Variables

```powershell
# In Vercel dashboard, verify these are set:
# - STRIPE_WEBHOOK_SECRET (or STRIPE_LIVE_WEBHOOK_SECRET)
# - STRIPE_SECRET_KEY (or STRIPE_LIVE_SECRET_KEY)
# - SUPABASE_SERVICE_KEY
# - SUPABASE_URL
```

### Verify Webhook Secret

1. Go to Stripe Dashboard → Webhooks
2. Click "Reveal" next to signing secret
3. Copy the secret (starts with `whsec_`)
4. Verify it matches your Vercel environment variable

### Check Webhook URL

Make sure it's exactly: `https://sparkiai.app/api/stripe-webhook`
- No trailing slash
- Must be HTTPS
- Must match domain exactly

## 🔄 Redeploy if Needed

```powershell
cd sparkifire-web
vercel --prod
```

## 📱 Test End-to-End

1. Open your app: https://sparkiai.app
2. Sign in with Google
3. Navigate to subscription/premium purchase
4. Complete checkout (use test mode)
5. Verify premium status activates
6. Check Supabase `user_profiles` table

## 🎯 Success Criteria

- ✅ Webhook returns 200 status
- ✅ No errors in Vercel logs
- ✅ User gets premium in Supabase
- ✅ No error emails from Stripe

## 📞 Support

If issues persist:
1. Check Vercel function logs
2. Check Stripe webhook events log
3. Verify all environment variables are set
4. Try redeploying with `vercel --prod`
