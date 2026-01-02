# Stripe Webhook Fix - December 26, 2025

## Problem
Stripe webhooks were failing with errors when trying to deliver events to `https://sparkiai.app/api/stripe-webhook`. The webhook endpoint was not properly configured to handle Stripe's signature verification.

## Root Cause
The main issues were:

1. **Body Parser Configuration**: Vercel serverless functions automatically parse request bodies as JSON, but Stripe webhooks require the **raw request body** (as a Buffer) to verify the signature.

2. **Missing Error Handling**: The webhook wasn't properly logging errors or handling edge cases.

3. **Configuration Missing**: The `bodyParser: false` configuration wasn't exported properly.

## Solution Applied

### 1. Updated `api/stripe-webhook.js`

**Key Changes:**

- **Added raw body parser**: Created `getRawBody()` helper function to read the raw request body as a Buffer
- **Disabled body parsing**: Added proper export configuration to disable Vercel's automatic body parsing
- **Enhanced error handling**: Added comprehensive error logging and try-catch blocks
- **Improved logging**: Added emoji-based logging for better visibility in logs
- **Better validation**: Check for signature header and webhook secret before processing

### 2. Configuration Export

```javascript
export const config = {
  api: {
    bodyParser: false,
  },
};
```

This tells Vercel NOT to parse the body, allowing us to access the raw body for signature verification.

### 3. Raw Body Handling

```javascript
async function getRawBody(req) {
  const chunks = [];
  for await (const chunk of req) {
    chunks.push(typeof chunk === 'string' ? Buffer.from(chunk) : chunk);
  }
  return Buffer.concat(chunks);
}
```

This streams the raw request body and converts it to a Buffer that Stripe can use for signature verification.

## Testing the Webhook

### Local Testing with Stripe CLI

1. **Install Stripe CLI** (if not already installed):
   ```bash
   # Windows with Scoop
   scoop bucket add stripe https://github.com/stripe/scoop-stripe-cli.git
   scoop install stripe
   ```

2. **Login to Stripe**:
   ```bash
   stripe login
   ```

3. **Forward webhooks to local dev server**:
   ```bash
   stripe listen --forward-to localhost:5173/api/stripe-webhook
   ```

4. **Trigger test events**:
   ```bash
   stripe trigger checkout.session.completed
   ```

### Production Testing

1. **Deploy to Vercel**:
   ```bash
   cd sparkifire-web
   vercel --prod
   ```

2. **Update Stripe Dashboard**:
   - Go to [Stripe Dashboard > Webhooks](https://dashboard.stripe.com/webhooks)
   - Verify endpoint URL: `https://sparkiai.app/api/stripe-webhook`
   - Ensure these events are selected:
     - `checkout.session.completed`
     - `customer.subscription.created`
     - `customer.subscription.updated`
     - `customer.subscription.deleted`

3. **Test with Stripe Dashboard**:
   - In Stripe Dashboard, go to your webhook endpoint
   - Click "Send test webhook"
   - Select `checkout.session.completed`
   - Verify you get a 200 response

## Environment Variables Required

Make sure these are set in Vercel:

- `STRIPE_SECRET_KEY` or `STRIPE_LIVE_SECRET_KEY` - Your Stripe secret key
- `STRIPE_WEBHOOK_SECRET` or `STRIPE_LIVE_WEBHOOK_SECRET` - Webhook signing secret from Stripe Dashboard
- `SUPABASE_SERVICE_KEY` - Supabase service role key for admin operations
- `SUPABASE_URL` - Your Supabase project URL

## Verifying the Fix

After deployment, check:

1. **Vercel Logs**: Go to Vercel dashboard → Your project → Functions → stripe-webhook.js
   - You should see: `✅ Webhook verified successfully`
   - Look for successful event processing logs

2. **Stripe Dashboard**: Go to Webhooks → Your endpoint
   - Recent events should show 200 status codes
   - Click on individual events to see request/response details

3. **Test a Real Purchase**:
   - Complete a test checkout on your app
   - Verify user gets premium status in Supabase
   - Check logs in both Vercel and Stripe

## Expected Log Output

Successful webhook processing should show:

```
✅ Webhook verified successfully (live mode): checkout.session.completed Event ID: evt_xxx
💳 Processing checkout.session.completed: cs_xxx
Customer details: { customerEmail: 'user@example.com', userId: 'xxx', sessionId: 'cs_xxx' }
✅ Premium activated successfully for user: xxx Email: user@example.com
✅ Event evt_xxx processed successfully
```

## Common Issues and Solutions

### Issue: "No signatures found matching the expected signature"
**Solution**: Webhook secret is wrong or not set. Check `STRIPE_WEBHOOK_SECRET` in Vercel environment variables.

### Issue: "Missing stripe-signature header"
**Solution**: Request is not coming from Stripe. Ensure the webhook URL in Stripe Dashboard matches exactly.

### Issue: "Webhook not configured"
**Solution**: `STRIPE_WEBHOOK_SECRET` environment variable is not set in Vercel.

### Issue: Still getting errors after deploy
**Solution**: 
1. Redeploy: `vercel --prod`
2. Clear Vercel cache
3. Check that the signing secret matches what's in Stripe Dashboard
4. Verify webhook URL is exactly `https://sparkiai.app/api/stripe-webhook`

## Next Steps

1. **Deploy the fix**: Deploy to production with `vercel --prod`
2. **Monitor logs**: Watch Vercel function logs for the next few webhook events
3. **Test thoroughly**: Create a test purchase to verify premium activation works
4. **Stripe will retry**: Stripe will automatically retry the failed events once the webhook is working

## Files Modified

- `sparkifire-web/api/stripe-webhook.js` - Main webhook handler with raw body parsing

## Documentation

For more information:
- [Stripe Webhooks Documentation](https://stripe.com/docs/webhooks)
- [Vercel Serverless Functions](https://vercel.com/docs/concepts/functions/serverless-functions)
- [Stripe Webhook Best Practices](https://stripe.com/docs/webhooks/best-practices)
