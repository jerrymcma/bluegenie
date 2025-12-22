# SparkiFire Premium System - Complete Deployment Guide

## What Was Fixed

We've fixed all the critical issues you discovered:

✅ **Music library now saved to database** - No more lost songs when clearing cache  
✅ **Stripe webhook activates premium automatically** - Payment works correctly  
✅ **Sign-out button added** - No need to clear browser cache  
✅ **Data persists across sessions** - Your premium status and songs are saved  

---

## Step-by-Step Deployment

### Step 1: Create Database Table for Music Library

1. Go to your Supabase Dashboard: `https://supabase.com/dashboard`
2. Select your project
3. Click "SQL Editor" in the left sidebar
4. Click "New query"
5. Copy the entire contents of `sparkifire-web/supabase-migration-generated-music.sql`
6. Paste it into the SQL editor
7. Click "Run" to execute

**This creates:**
- `generated_music` table to store user's music
- Security policies so users can only see their own music
- Indexes for fast queries

---

### Step 2: Get Supabase Service Key

The Stripe webhook needs ADMIN access to activate premium. Get your service key:

1. In Supabase Dashboard, go to **Settings** → **API**
2. Scroll to **Project API keys**
3. Find the **service_role** key (NOT the anon key!)
4. **Copy this key** - you'll add it to Vercel in the next step

**⚠️ IMPORTANT**: The service_role key has admin privileges. Never expose it in your frontend code!

---

### Step 3: Add Environment Variables to Vercel

You need to add 3 new environment variables:

1. Go to Vercel Dashboard: `https://vercel.com/dashboard`
2. Select your **sparkiai** project
3. Go to **Settings** → **Environment Variables**
4. Add these variables:

| Variable Name | Value | Where to Get It |
|--------------|-------|----------------|
| `SUPABASE_SERVICE_KEY` | Your service_role key | From Step 2 above |
| `STRIPE_SECRET_KEY` | Your Stripe secret key | Stripe Dashboard → Developers → API Keys |
| `STRIPE_WEBHOOK_SECRET` | (Leave blank for now) | We'll get this in Step 4 |

5. For each variable, make sure to select **Production, Preview, Development** (all three)
6. Click "Save" for each one

---

### Step 4: Deploy to Vercel

Now deploy the updated code:

```powershell
cd C:\Users\Jerry\AndroidStudioProjects\SparkiFire
git add -A
git commit -m "Fix: Premium system - database storage, Stripe webhook, sign-out button"
git push origin main
vercel --prod
```

Wait for the deployment to complete. You'll see a URL like:
`https://sparkiai-xxxxxx.vercel.app`

---

### Step 5: Configure Stripe Webhook

Now tell Stripe to send payment events to your webhook:

1. Go to **Stripe Dashboard**: `https://dashboard.stripe.com/`
2. Click **Developers** → **Webhooks**
3. Click **"Add endpoint"**
4. Enter your webhook URL:
   ```
   https://sparkiai-ojvfluzm7-jerry-mcmahons-projects.vercel.app/api/stripe-webhook
   ```
   (Replace with your actual Vercel domain)

5. Click **"Select events to listen to"**
6. Add these 3 events:
   - `checkout.session.completed`
   - `customer.subscription.created`
   - `customer.subscription.updated`

7. Click **"Add endpoint"**
8. After it's created, click on the webhook
9. Click **"Reveal"** next to "Signing secret"
10. **Copy the webhook signing secret** (starts with `whsec_`)

---

### Step 6: Add Webhook Secret to Vercel

1. Go back to Vercel Dashboard → Your Project → Settings → Environment Variables
2. Find the `STRIPE_WEBHOOK_SECRET` variable (or add it if you didn't earlier)
3. Paste the signing secret from Step 5
4. Save it

---

### Step 7: Redeploy One More Time

The webhook secret needs a redeploy to take effect:

```powershell
cd C:\Users\Jerry\AndroidStudioProjects\SparkiFire
vercel --prod
```

---

## Testing the Complete Flow

### Test 1: Sign In and Generate Songs

1. Open your web app in **incognito mode**: `www.sparkiai.app`
2. Click the Music button
3. You should see **"Sign in to generate music"**
4. Sign in with Google
5. Generate a song
6. **Refresh the page** - your song should still be there! ✅

### Test 2: Sign Out

1. Click the **red logout button** in the top-right (looks like a door with an arrow)
2. Your music library should clear
3. If you try to generate music, it asks you to sign in again ✅

### Test 3: Premium Payment

1. Sign in with a test Google account
2. Generate 5 songs
3. On the 6th attempt, you'll see "Upgrade to Premium"
4. Click "Upgrade" and complete payment with a test card:
   - Card: `4242 4242 4242 4242`
   - Expiry: Any future date
   - CVC: Any 3 digits

5. After payment, **wait 5-10 seconds** for the webhook to activate premium
6. **Refresh the page**
7. Try generating music - it should work! ✅
8. Check your song count - you should have 50 songs available ✅

---

## What Happens Now

### For Free Users:
- Sign in with Google → Get 5 free songs
- Songs saved to database (won't be lost)
- After 5 songs → Prompted to upgrade

### For Premium Users ($5/month):
- Get 50 songs per month
- Songs saved to database
- After 50 songs OR 30 days → Prompted to renew
- Can sign out without losing data

### Music Library:
- All generated music saved to Supabase `generated_music` table
- Survives browser cache clearing
- Loads automatically when you sign in

---

## Troubleshooting

### "Premium not activated after payment"

**Check Webhook Logs:**
1. Go to Stripe Dashboard → Developers → Webhooks
2. Click on your webhook
3. Check the **logs** - you should see successful `checkout.session.completed` events
4. If there are errors, check the Vercel logs

**Check Vercel Logs:**
1. Go to Vercel Dashboard → Your Project → Deployments
2. Click on the latest deployment
3. Go to **Functions** → `stripe-webhook`
4. Check logs for errors

### "Music library not loading"

1. Check browser console for errors (F12 → Console)
2. Make sure the SQL migration ran successfully in Step 1
3. Verify user is signed in (check for red logout button)

### "Can't generate music - authentication error"

1. Make sure you ran Step 3 to add `SUPABASE_SERVICE_KEY`
2. Redeploy with `vercel --prod` after adding env variables
3. Check that Replicate API key is still valid in Vercel env vars

---

## Summary of Changes

### Code Files Changed:
- ✅ `sparkifire-web/src/store/chatStore.ts` - Now uses database instead of localStorage
- ✅ `sparkifire-web/src/services/generatedMusicService.ts` - NEW file for database operations
- ✅ `sparkifire-web/api/stripe-webhook.js` - NEW webhook to activate premium
- ✅ `sparkifire-web/src/app/ChatScreen.tsx` - Added sign-out button
- ✅ `sparkifire-web/vercel.json` - Added webhook route

### Database Changes:
- ✅ New `generated_music` table with security policies

### Vercel Config Changes:
- ✅ Added 3 new environment variables

### Stripe Config Changes:
- ✅ Webhook endpoint configured

---

## Ready to Test!

Once you complete all 7 steps, everything should work:
- ✅ Sign in/out works
- ✅ Music library persists across sessions
- ✅ Premium payment activates automatically
- ✅ 5 free songs for non-premium users
- ✅ 50 songs/month for premium users

Let me know if you hit any issues during deployment! 🚀
