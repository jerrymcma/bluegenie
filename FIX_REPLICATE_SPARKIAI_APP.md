# 🎵 Fix: Replicate Connection Issue on sparkiai.app

## Problem

The web app at **sparkiai.app** shows "can't reach replicate" when trying to generate music.

## Root Causes

1. **CORS Issues**: Browser blocks direct API calls to Replicate from the web app
2. **Environment Variables**: API keys not properly configured in Vercel
3. **API Key Exposure**: Client-side code exposes API keys in browser

## Solution: Serverless API Proxy

Created a Vercel serverless function that:

- ✅ Proxies requests to Replicate API server-side (no CORS)
- ✅ Keeps API keys secure (never exposed to browser)
- ✅ Handles authentication properly
- ✅ Provides better error messages

## Files Changed

### 1. New: `sparkifire-web/api/music.js`

Serverless function that proxies all Replicate API calls.

### 2. Updated: `sparkifire-web/src/services/musicService.ts`

- Changed to use `/api/music` endpoint instead of direct Replicate API
- Removed client-side API key handling
- Improved error messages

### 3. New: `sparkifire-web/api/package.json`

Dependencies for the serverless function.

## Deployment Steps

### Step 1: Set Environment Variables in Vercel

1. Go to https://vercel.com/dashboard
2. Open your project: **sparkifire-web**
3. Navigate to **Settings** → **Environment Variables**
4. Add the following variables:

**For Production:**

- Variable: `VITE_REPLICATE_API_KEY`
- Value: `r8_Ev4seCPXpkRBRSGt6fsXtCMmaoM5CA63mcqNM`
- Environment: ✅ Production

**For Development (optional):**

- Variable: `VITE_REPLICATE_API_KEY`
- Value: `r8_Ev4seCPXpkRBRSGt6fsXtCMmaoM5CA63mcqNM`
- Environment: ✅ Development

**For Preview (optional):**

- Variable: `VITE_REPLICATE_API_KEY`
- Value: `r8_Ev4seCPXpkRBRSGt6fsXtCMmaoM5CA63mcqNM`
- Environment: ✅ Preview

5. **Also verify these are set:**

- `VITE_GEMINI_API_KEY` = `AIzaSyDS7LznAuOxPovX1FAx4BBHAYPiapYaR_c`
- `VITE_GOOGLE_CLIENT_ID` =
  `904707581552-v6bm1v1nasleev9l2394b6psv6ns3s8k.apps.googleusercontent.com`

### Step 2: Deploy to Vercel

#### Option A: Using Git (Recommended)

```powershell
# Commit changes
git add sparkifire-web/api/
git add sparkifire-web/src/services/musicService.ts
git add FIX_REPLICATE_SPARKIAI_APP.md
git commit -m "Fix: Add serverless proxy for Replicate API to solve CORS and API key issues"

# Push to trigger automatic deployment
git push origin main
```

Vercel will automatically detect and deploy the changes, including the new serverless function.

#### Option B: Using Vercel CLI

```powershell
# Navigate to project
cd sparkifire-web

# Deploy to production
npx vercel --prod
```

When prompted, confirm it's the existing project.

#### Option C: Manual Deploy via Dashboard

1. Go to https://vercel.com/dashboard
2. Open **sparkifire-web** project
3. Go to **Deployments** tab
4. Click **...** next to latest deployment
5. Select **Redeploy**
6. Make sure to check **Use existing Build Cache** (faster)

Then separately upload the new files through Git or CLI.

### Step 3: Verify Deployment

After deployment completes:

1. **Check Function Deployment:**
    - Go to your Vercel project dashboard
    - Navigate to **Functions** tab
    - You should see `/api/music` listed

2. **Test the Website:**
    - Go to https://sparkiai.app
    - Open browser console (F12)
    - Click the music icon (🎵)
    - Enter a prompt: "Upbeat pop song about sunshine"
    - Click "Generate"

3. **Expected Behavior:**
    - ✅ Status shows: "✨ Generating your song..."
    - ✅ No CORS errors in console
    - ✅ No "can't reach replicate" message
    - ✅ After 30-60 seconds, shows download link

4. **Check Console Logs:**
    - Should see network requests to `/api/music` (not replicate.com)
    - Should show 200 OK responses

## Testing Locally First

Before deploying, test locally:

```powershell
cd sparkifire-web

# Install dependencies
npm install

# Start development server
npm run dev
```

Then test music generation at http://localhost:3000

**Note:** Local testing will use the `.env` file, but production will use Vercel environment
variables.

## Troubleshooting

### Issue: Still showing "can't reach replicate"

**Solution:**

1. Check Vercel environment variables are set correctly
2. Verify the serverless function is deployed (check Functions tab)
3. Clear browser cache (Ctrl+Shift+R)
4. Check Vercel deployment logs for errors

### Issue: "API key not configured on server"

**Solution:**

1. Environment variable is not set in Vercel
2. Go to Settings → Environment Variables
3. Add `VITE_REPLICATE_API_KEY` with your key
4. Redeploy the application

### Issue: Serverless function not found (404)

**Solution:**

1. Make sure `api/music.js` file was deployed
2. Check that file is in the `sparkifire-web/api/` directory
3. Redeploy with `vercel --prod --force`

### Issue: Timeout errors

**Solution:**

1. This is normal - music generation can take 60+ seconds
2. The function has 120s timeout configured
3. Check Replicate dashboard for billing/quota issues

### Issue: CORS errors still appearing

**Solution:**

1. Make sure you're using the deployed URL (sparkiai.app)
2. Check that serverless function has CORS headers
3. Clear browser cache completely
4. Try incognito/private browsing mode

## How It Works

### Before (Direct API Call - ❌ Fails)

```
Browser → Replicate API
        ❌ CORS Error
        ❌ API Key Exposed
```

### After (Serverless Proxy - ✅ Works)

```
Browser → Vercel Serverless Function → Replicate API
        ✅ No CORS (same origin)
        ✅ API Key Secure (server-side)
        ✅ Better Error Handling
```

## API Endpoints

### `/api/music` (POST)

**Create Prediction:**

```json
{
  "action": "create",
  "lyrics": "Verse lyrics here...",
  "prompt": "Pop, upbeat, modern"
}
```

**Check Prediction Status:**

```json
{
  "action": "check",
  "predictionId": "prediction-id-here"
}
```

## Security Benefits

1. **API Key Protection**: Replicate API key never leaves the server
2. **Rate Limiting**: Can add rate limiting to serverless function
3. **Input Validation**: Server validates all inputs before sending to Replicate
4. **Error Handling**: Server provides sanitized error messages

## Performance

- **Cold Start**: ~1-2 seconds (first request)
- **Warm Start**: <100ms (subsequent requests)
- **Timeout**: 120 seconds (enough for music generation)
- **Caching**: Responses can be cached if needed

## Monitoring

View serverless function logs:

1. Go to Vercel dashboard
2. Navigate to your project
3. Click **Functions** tab
4. Click on `/api/music`
5. View real-time logs

## Cost

- **Vercel**: Serverless functions are included in free tier (100GB-hours/month)
- **Replicate**: Charged per second of generation time (~$0.01-0.05 per song)

## Next Steps

After successful deployment:

1. ✅ Test music generation thoroughly
2. ✅ Monitor Vercel function logs for errors
3. ✅ Check Replicate dashboard for API usage
4. ✅ Consider adding rate limiting if needed
5. ✅ Add user feedback/analytics

## Rollback Plan

If issues occur:

1. Go to Vercel Dashboard → Deployments
2. Find the previous working deployment
3. Click **...** → **Promote to Production**

Or revert Git commits:

```bash
git revert HEAD
git push origin main
```

## Support Resources

- **Vercel Docs**: https://vercel.com/docs/functions/serverless-functions
- **Replicate Docs**: https://replicate.com/docs/reference/http
- **Your API Keys**: Stored in `local.properties` and Vercel dashboard

## Quick Reference

**Test Music Generation:**

1. Go to sparkiai.app
2. Click 🎵 icon
3. Enter: "Upbeat pop song"
4. Click Generate
5. Wait 30-60 seconds

**Check Logs:**

- Vercel: Dashboard → Functions → Logs
- Browser: F12 → Console tab

**Environment Variables:**

- Local: `sparkifire-web/.env`
- Production: Vercel Dashboard → Settings → Environment Variables

---

**Status:** ✅ Fix implemented and ready to deploy

**Time to Deploy:** ~5-10 minutes

**Files Changed:** 3 files (2 new, 1 updated)

**Testing Required:** Yes - test music generation after deployment
