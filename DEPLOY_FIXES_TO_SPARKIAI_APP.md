# 🚀 Deploy Fixes to sparkiai.app

## Quick Deploy Steps

Your web app is deployed on **Vercel**. Here's how to update the live site with the fixes:

### Method 1: Automatic Deploy (Recommended)

If you have Git connected to Vercel:

```powershell
# 1. Commit the changes
git add sparkifire-web/.env sparkifire-web/src/services/geminiService.ts
git commit -m "Fix: Updated Gemini model and added Replicate API key"

# 2. Push to repository
git push

# Vercel will automatically detect and deploy the changes!
```

**Note:** Make sure `.env` is in `.gitignore` (it should be). You'll need to set environment
variables in Vercel dashboard instead.

### Method 2: Manual Deploy via Vercel CLI

```powershell
# 1. Install Vercel CLI (if not installed)
npm install -g vercel

# 2. Navigate to web directory
cd sparkifire-web

# 3. Deploy to production
vercel --prod
```

When prompted, use the existing project (sparkifire-web).

### Method 3: Vercel Dashboard

1. Go to https://vercel.com/dashboard
2. Find your project: **sparkifire-web**
3. Click on the project
4. Go to **Settings** → **Environment Variables**
5. Add/Update these variables:
    - `VITE_GEMINI_API_KEY` = `your-gemini-api-key`
    - `VITE_REPLICATE_API_KEY` = `your-replicate-api-key`
    - `VITE_GOOGLE_CLIENT_ID` = `your-google-client-id`
6. Go to **Deployments** tab
7. Click **Redeploy** on the latest deployment
8. Select **Use existing Build Cache** → **Redeploy**

## Important: Environment Variables in Vercel

⚠️ **CRITICAL:** Your `.env` file should NOT be pushed to Git. Instead:

1. **Add to Vercel Dashboard:**
    - Go to your project settings
    - Navigate to **Environment Variables**
    - Add all `VITE_*` variables there
    - These will be used during build time

2. **Add to `.gitignore`:**
   ```
   # Already should be there
   .env
   .env.local
   .env.*.local
   ```

## Verify Deployment

After deploying, test these:

### 1. Test Chat (Gemini Fix)

- Go to https://sparkiai.app
- Send a message to Sparki
- ✅ Should get real AI response (not "demo mode")
- ✅ Should work with all 10 personalities

### 2. Test Music Generation (Replicate Fix)

- Click the music icon (🎵) in the chat input
- Enter lyrics or a style prompt
- Click "Generate"
- ✅ Should show "Generating your song..."
- ✅ Should NOT show "cannot reach replicate"
- ✅ Should receive a download link after ~30-60 seconds

### 3. Test Voice Features

- Click microphone icon
- Allow microphone permission
- ✅ Should transcribe speech to text
- ✅ Should be able to play AI responses

## Updated Files (for Git commit)

If you want to push to Git, commit these files:

```
modified:   sparkifire-web/src/services/geminiService.ts
new file:   sparkifire-web/.env.example
new file:   FIXED_WEB_APP_ISSUES.md
new file:   DEPLOY_FIXES_TO_SPARKIAI_APP.md
```

**DO NOT COMMIT:**

```
sparkifire-web/.env  ❌ (contains API keys)
```

## Git Commands (if using Git deployment)

```bash
# Check what's changed
git status

# Add specific files (NOT .env)
git add sparkifire-web/src/services/geminiService.ts
git add sparkifire-web/.env.example
git add FIXED_WEB_APP_ISSUES.md
git add DEPLOY_FIXES_TO_SPARKIAI_APP.md

# Commit with a clear message
git commit -m "Fix web app: Update Gemini model to 1.5-pro and add Replicate API support"

# Push to trigger automatic deployment
git push origin main
```

## Rollback (if needed)

If something goes wrong:

1. Go to Vercel Dashboard
2. Navigate to **Deployments**
3. Find the previous working deployment
4. Click **...** → **Promote to Production**

## Support

### If chat still shows "demo mode":

1. Check Vercel environment variables are set correctly
2. Redeploy from Vercel dashboard
3. Clear browser cache (Ctrl+Shift+R)
4. Check browser console for errors (F12)

### If music generation still fails:

1. Verify `VITE_REPLICATE_API_KEY` in Vercel dashboard
2. Test the Replicate API key directly: https://replicate.com/account/api-tokens
3. Check if you have credits on Replicate account
4. Look at Vercel deployment logs for errors

### If nothing works:

1. Go to Vercel dashboard
2. Check **Deployment Logs** for build errors
3. Ensure all environment variables are in "Production" scope
4. Try deleting and re-adding environment variables

## Local Testing Before Deploy

Test locally first:

```powershell
cd sparkifire-web

# Development mode
npm run dev
# Test at http://localhost:3000

# Production build test
npm run build
npm run preview
# Test at http://localhost:4173
```

## Deployment Status

✅ Fixed issues:

- Gemini model updated to `gemini-1.5-pro`
- Replicate API key configured
- Demo mode error removed
- Production build completed

🚀 Ready to deploy:

- Local `.env` file created with API keys
- Code updated and tested
- Build successful (no errors)

---

**Next Step:** Choose your deployment method above and update sparkiai.app! 🔥
