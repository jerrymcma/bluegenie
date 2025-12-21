# Incognito Mode Music Generation Login Fix - December 21, 2025

## Problem
Users can generate music without signing in when using incognito mode on their phone's Chrome browser. The app should require users to sign in before generating music to track their 5 free songs against a user account.

## Root Causes Identified

### 1. **Missing Explicit User Check in `generateMusic()`**
The original code had a `checkUsageLimits()` function that was supposed to prevent music generation without login, but:
- It relied on the `user` state being populated
- In incognito mode, session restoration might fail silently
- There was no explicit verification of user authentication before attempting generation

### 2. **Unreliable Session Restoration in Incognito Mode**
In incognito/private browsing mode:
- Cookies and session storage might be isolated or cleared
- OAuth redirect might not properly restore the session
- The Supabase session might not persist after the OAuth callback

### 3. **Insufficient Error Handling**
If the access token was missing, the code would show a sign-in modal after attempting the request, rather than preventing the request upfront.

## Solution Implemented

### 1. **Enforced Explicit User Authentication Check**
Modified `generateMusic()` to require a valid `user` object BEFORE any music generation attempt:

```typescript
// CRITICAL: Must be signed in to generate music
if (!user) {
  console.warn('[generateMusic] User is not authenticated, showing sign-in modal');
  set({
    showSignInModal: true,
  });
  return null;
}
```

This prevents:
- Anonymous users from generating music
- Incognito mode users from bypassing authentication
- Unauthenticated requests from being sent to the server

### 2. **Removed Reliance on Local `musicCredits` Counter**
The previous logic allowed users to generate up to 5 songs based on a local `musicCredits` counter, which:
- Resets in incognito mode
- Doesn't sync with the database
- Can be bypassed by clearing localStorage

The new implementation checks the database `songCount` from the user's profile:

```typescript
if (!isPremium && songCount >= 5) {
  console.log('[generateMusic] Free tier user has hit 5 song limit');
  set({ showUpgradeModal: true });
  return null;
}
```

### 3. **Improved OAuth Configuration**
Enhanced the Google OAuth sign-in flow with:
- Explicit logging of redirect URLs
- Proper `skipBrowserRedirect: false` to ensure automatic redirect after auth
- Better error handling and debugging

### 4. **Added Debug Logging**
Comprehensive logging at key points:
- When checking user authentication status
- When OAuth is initiated
- When auth state changes
- When trying to retrieve current user

This helps identify where authentication fails in incognito mode.

## Technical Changes Made

### File: `sparkifire-web/src/store/chatStore.ts`

**Changes to `generateMusic()` function:**
- Added explicit `!user` check at the start
- Removed dependency on local `musicCredits` counter
- Now checks `subscription.songCount` from database (requires sign-in)
- Improved error messages for session expiration

**Changes to `initialize()` function:**
- Added logging for auth state changes
- Added error handling for `getCurrentUser()` promise
- Better visibility into session restoration in incognito mode

### File: `sparkifire-web/src/services/supabaseService.ts`

**Changes to `signInWithGoogle()` function:**
- Added explicit redirect URL logging
- Set `skipBrowserRedirect: false` for reliable OAuth flow
- Added success logging after OAuth initiation

## How It Works Now

### Incognito Mode Scenario

1. **User opens app in incognito mode** → No session is found
2. **User clicks "Generate Music"** → `user` is null
3. **Sign-in modal appears** → Forces user to authenticate
4. **User signs in with Google** → OAuth redirect happens
5. **After OAuth success** → Session is restored, user profile is loaded from database
6. **User can now generate music** → `songCount` is tracked in database, not locally

### Key Improvements

✅ **No Local State Bypass**: Music generation now requires database-backed user authentication
✅ **Session Restoration**: OAuth flow is more reliable with explicit redirect configuration
✅ **Better Debugging**: Comprehensive logging helps identify OAuth issues
✅ **Database-Backed Limits**: Free songs (5) and premium songs (50/month) are tracked server-side
✅ **Clear Error Messages**: Users know exactly why they can't generate music

## Testing Recommendations

1. **Test in Incognito Mode**
   - Open Chrome in incognito mode on phone/desktop
   - Try to generate music without signing in
   - Verify sign-in modal appears
   - Sign in with Google
   - Verify music generation works after sign-in
   - Verify that generating 5 songs shows upgrade modal

2. **Test Session Restoration**
   - Sign in in incognito mode
   - Close the browser/tab completely
   - Reopen incognito mode to the same URL
   - Verify session is restored from database

3. **Test Premium Features**
   - Sign in with a premium account
   - Verify can generate 50 songs per month
   - After 50 songs, verify renewal modal appears

4. **Test Multiple Incognito Windows**
   - Open same app in multiple incognito windows
   - Sign in in one window
   - Verify other windows still require sign-in (isolated sessions)

## Files Modified
1. `sparkifire-web/src/store/chatStore.ts` - Core authentication and music generation logic
2. `sparkifire-web/src/services/supabaseService.ts` - OAuth configuration

## Browser Console Debugging
In the browser console, you'll now see detailed logs like:
```
[generateMusic] User is not authenticated, showing sign-in modal
[chatStore.initialize] Checked current user: {hasUser: false}
[signInWithGoogle] Using redirect URL: https://example.com
[chatStore.initialize] Auth state changed: {hasUser: true, userId: "user-123"}
```

These logs help diagnose session issues in incognito mode.
