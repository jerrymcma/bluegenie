# 🎵 Quick Fix: Replicate Music Generation

## The Problem

sparkiai.app says "can't reach replicate" when generating music.

## The Solution

Created a serverless API proxy to handle Replicate calls securely without CORS issues.

## Deploy Now (5 Minutes)

### Option 1: Automatic Deploy via Git

```powershell
cd C:\Users\Jerry\AndroidStudioProjects\SparkiFire

git add sparkifire-web/api/
git add sparkifire-web/src/services/musicService.ts
git add sparkifire-web/api/package.json
git commit -m "Fix: Add serverless proxy for Replicate API"
git push origin main
```

Vercel will automatically deploy the changes.

### Option 2: Manual Deploy

```powershell
cd C:\Users\Jerry\AndroidStudioProjects\SparkiFire\sparkifire-web
npx vercel --prod
```

## Critical: Set Environment Variables in Vercel

⚠️ **THIS IS REQUIRED OR IT WON'T WORK** ⚠️

1. Go to: https://vercel.com/dashboard
2. Open your **sparkifire-web** project
3. Go to **Settings** → **Environment Variables**
4. Add this variable:

**Variable Name:** `VITE_REPLICATE_API_KEY`
**Value:** `r8_Ev4seCPXpkRBRSGt6fsXtCMmaoM5CA63mcqNM`
**Environments:** Check all three boxes (Production, Preview, Development)

5. Click **Save**
6. Go to **Deployments** tab
7. Click **Redeploy** on the latest deployment

## Test It

1. Go to https://sparkiai.app
2. Click the music icon (🎵)
3. Type: "Upbeat pop song about sunshine"
4. Click "Generate"
5. ✅ Should work now! (Wait 30-60 seconds for generation)

## What Changed?

### Files Added:

- `sparkifire-web/api/music.js` - Serverless function that proxies Replicate API
- `sparkifire-web/api/package.json` - Dependencies for the serverless function

### Files Updated:

- `sparkifire-web/src/services/musicService.ts` - Now uses `/api/music` instead of direct API calls

### Why This Fixes It:

**Before:**

```
Browser → Replicate API ❌ CORS Error
```

**After:**

```
Browser → Your Server → Replicate API ✅ Works!
```

## Verification Checklist

After deployment:

- [ ] Go to Vercel Dashboard → Functions tab
- [ ] You should see `/api/music` listed
- [ ] Test music generation at sparkiai.app
- [ ] Open browser console (F12) - no CORS errors
- [ ] Music generates successfully (30-60 seconds)
- [ ] Download link appears

## Troubleshooting

**Still not working?**

1. **Check environment variable is set:**
    - Vercel Dashboard → Settings → Environment Variables
    - Must see `VITE_REPLICATE_API_KEY`

2. **Redeploy:**
    - Deployments tab → Latest deployment → Redeploy

3. **Check function logs:**
    - Vercel Dashboard → Functions → `/api/music` → Logs

4. **Clear browser cache:**
    - Press Ctrl+Shift+R on sparkiai.app

## Quick Deploy Script

I created a deploy script for you:

```powershell
cd sparkifire-web
.\deploy.bat
```

This will build and deploy automatically.

---

**Status:** ✅ Ready to deploy

**Build Status:** ✅ Compiled successfully (no errors)

**Time Required:** 5 minutes

**What to do now:**

1. Set environment variable in Vercel (if not already set)
2. Run deploy script or push to Git
3. Test at sparkiai.app
