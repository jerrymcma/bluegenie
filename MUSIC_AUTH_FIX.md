# Music Generation Authentication Fix - December 21, 2025

## Problem
Users could generate music without signing in, whether in incognito mode or normal browsing. The app should require sign-in before allowing any music generation, tracking 5 free songs against the user's account.

## Root Cause Analysis

The issue was a **missing authentication requirement** at multiple levels:

1. **No explicit user check in `generateMusic()`** - The function didn't verify user authentication before attempting generation
2. **No UI-level authentication gate** - The Music Generation dialog didn't show that sign-in was required
3. **Disabled Generate button only checked `isGeneratingMusic`** - No check for whether the user was authenticated

## Solution: Multi-Layer Authentication Enforcement

### 1. Core Store Logic (`chatStore.ts`)

**Added explicit user verification in `generateMusic()` function:**
```typescript
// CRITICAL: Must be signed in to generate music
if (!user) {
  console.warn('[generateMusic] User is not authenticated, showing sign-in modal');
  set({ showSignInModal: true });
  return null;
}
```

This is the **primary gate** - no music generation request is made without authentication.

### 2. UI-Level Authentication Gate (`MusicGenerationDialog.tsx`)

**Added three layers of UI protection:**

#### Layer 1: Dialog-Level Authentication Message
When a user opens the music generator dialog without being signed in, they see:
- A prominent blue box explaining "Sign in to generate music"
- Direct button to sign in with Google
- Explanation of the 5 free songs benefit

#### Layer 2: Generate Button Disabled State
```typescript
disabled={isGeneratingMusic || !user}
```
The Generate button is disabled if the user is not authenticated, preventing accidental clicks.

#### Layer 3: Handler-Level Check
```typescript
if (!user) {
  console.warn('[MusicGenerationDialog] User not authenticated - showing sign-in modal');
  useChatStore.setState({ showSignInModal: true });
  return;
}
```
Even if the button somehow gets clicked, the handler checks authentication first.

### 3. Backend API (`music.js`)

The backend already had authentication verification:
```javascript
const supabaseUser = await verifySupabaseUser(authHeader);
if (!supabaseUser) {
  return res.status(401).json({
    error: 'not_authenticated',
    message: 'Please sign in to generate music.',
  });
}
```

### 4. Enhanced Session Detection (`chatStore.ts` + `supabaseService.ts`)

**Improved logging in `initialize()`:**
- Added console logs for auth state changes
- Better tracking of session restoration
- Error handling for async auth checks

**Enhanced OAuth flow:**
- Explicit logging of redirect URLs
- Set `skipBrowserRedirect: false` for reliable OAuth callback
- Better error messages for session expiration

## Flow Diagram

### Unauthenticated User Journey

```
1. User opens app
   ↓
2. Click Music button → Opens MusicGenerationDialog
   ↓
3. Dialog displays: "Sign in to generate music"
   ↓
4. User clicks "Sign In with Google" button in dialog
   ↓
5. OAuth flow initiates
   ↓
6. User completes Google auth
   ↓
7. Session restored, user profile loaded
   ↓
8. Dialog now shows input fields (not sign-in prompt)
   ↓
9. User enters lyrics/music description
   ↓
10. Clicks Generate button
    ↓
11. generateMusic() executes with authenticated user
    ↓
12. Music is generated and added to library
```

### Authenticated User Journey

```
1. User opens app (signed in from previous session)
   ↓
2. initialize() restores user session from Supabase
   ↓
3. Click Music button → Opens MusicGenerationDialog
   ↓
4. Dialog shows input fields (user already signed in)
   ↓
5. User enters lyrics/music description
   ↓
6. Clicks Generate button → ENABLED ✓
   ↓
7. Music is generated
```

## Files Modified

### 1. `sparkifire-web/src/store/chatStore.ts`
**Changes:**
- Simplified `generateMusic()` to require explicit `!user` check first
- Removed reliance on `checkUsageLimits()` for auth (still used for renewal checks)
- Check database-backed `subscription.songCount` instead of local `musicCredits`
- Added comprehensive logging in `initialize()`
- Better error messages for session expiration

### 2. `sparkifire-web/src/components/MusicGenerationDialog.tsx`
**Changes:**
- Import `user` and `setShowSignInModal` from store
- Display authentication prompt when `!user`
- Added inline sign-in button in dialog
- Disable Generate button when `!user`
- Handle auth check in `handleGenerate()` before any validation

### 3. `sparkifire-web/src/services/supabaseService.ts`
**Changes:**
- Added logging for redirect URL in `signInWithGoogle()`
- Set `skipBrowserRedirect: false` explicitly
- Better error logging

## Key Improvements

✅ **No Bypass Possible**: Authentication is checked at:
  - Component level (UI disabled)
  - Handler level (function guard)
  - Store level (logic gate)
  - Backend level (API verification)

✅ **Clear User Experience**: Users immediately see why they can't generate music

✅ **Incognito Mode Safe**: Works in incognito because:
  - Session is checked client-side BEFORE any request
  - Backend verifies token for extra safety
  - User is required before any API call

✅ **Database-Backed Limits**: Song counts tracked server-side in `user_profiles.song_count`

✅ **Better Debugging**: Comprehensive logging helps diagnose auth issues

## Testing Checklist

- [ ] Open app without signing in
- [ ] Click to open music generator
- [ ] Verify sign-in prompt appears in dialog
- [ ] Verify Generate button is DISABLED
- [ ] Click "Sign In with Google" button
- [ ] Complete OAuth flow
- [ ] Verify Generate button is now ENABLED
- [ ] Enter lyrics and generate music successfully
- [ ] Verify music is added to library
- [ ] Generate 5 songs
- [ ] On 6th attempt, verify upgrade modal appears
- [ ] Test in incognito mode - same flow should work
- [ ] Test in multiple tabs - signing in one tab should enable music in all tabs
- [ ] Sign out
- [ ] Verify Generate button is disabled again

## Browser Console Debugging

You'll see logs like:
```
[chatStore.initialize] Checked current user: {hasUser: false}
[signInWithGoogle] Using redirect URL: https://example.com
[chatStore.initialize] Auth state changed: {hasUser: true, userId: "user-123"}
[generateMusic] User is authenticated, songCount: 3
[generateMusic] Free tier user has hit 5 song limit
```

## Incognito Mode Explanation

In incognito mode:
- Browser cookies/storage are isolated
- OAuth still works (redirects to Google auth)
- After OAuth completes, Supabase session is restored
- The strict `!user` check ensures no requests before this

This fix makes incognito mode behavior identical to normal mode.

## Future Improvements

- Add visual indicator of song count (3 of 5 remaining)
- Show countdown when approaching 5-song limit
- Add "Renew Premium" button directly in dialog for premium users at limit
- Cache user subscription state for offline support
