# 🔒 SECURITY UPDATE REQUIRED - API Keys Exposed

## ⚠️ IMMEDIATE ACTION NEEDED

The following API keys were exposed in this conversation and need to be **rotated immediately**:

### 1. Replicate API Key
- **Exposed Key**: `r8_S9WYNjprWZTbjDkG1oSyw8XxBJcz3wD0IemMw`
- **Action**: Delete this key and generate a new one at https://replicate.com/account/api-tokens
- **Update in**: `local.properties` → `REPLICATE_API_KEY=your-new-key-here`

### 2. Gemini API Key
- **Exposed Key**: `AIzaSyB8qR93covv2E5OrOb_JplIF7jNoecks9c`
- **Action**: Delete this key and generate a new one at https://aistudio.google.com/app/apikey
- **Update in**: `local.properties` → `GEMINI_API_KEY=your-new-key-here`

### 3. Google OAuth Client ID
- **Exposed Key**: `904707581552-v6bm1v1nasleev9l2394b6psv6ns3s8k.apps.googleusercontent.com`
- **Action**: This is less critical but consider rotating at https://console.cloud.google.com/apis/credentials
- **Update in**: `local.properties` → `GOOGLE_CLIENT_ID=your-new-client-id-here`

### 4. Supabase Credentials (SAFE - No Rotation Needed)
- **URL**: `https://dvrrgfrclkxoseioywek.supabase.co`
- **Anon Key**: This is **safe** - it's already public in your web app
- **Why it's safe**: 
  - ✅ Anon keys are **designed to be public** (they're in client-side code)
  - ✅ Security comes from Row Level Security (RLS) policies in Supabase
  - ✅ Same key used for web app, Android, and iOS
- **Action Required**: 
  - ✅ Verify RLS (Row Level Security) is enabled on all tables
  - ✅ Test that policies work correctly (users can only access their own data)

## 📝 How to Add Keys to local.properties

Add these lines to your `local.properties` file:

```properties
# Supabase (use SAME keys as web app - they're safe to reuse)
SUPABASE_URL=https://dvrrgfrclkxoseioywek.supabase.co
SUPABASE_ANON_KEY=eyJhbGc... (your actual anon key from web app)

# Stripe (use SAME publishable key as web app - safe to reuse)
STRIPE_PUBLISHABLE_KEY=pk_live_... (or pk_test_... from web app)

# After rotating, add NEW keys:
REPLICATE_API_KEY=your-NEW-replicate-key
GEMINI_API_KEY=your-NEW-gemini-key
GOOGLE_CLIENT_ID=your-client-id
```

**Important**: 
- ✅ Supabase and Stripe keys from web app are **safe to reuse**
- ✅ These keys are meant to be public (they're in client-side code)
- ⚠️ Only rotate the API keys that were exposed (Replicate, Gemini)

## 🔐 Best Practices Going Forward

### For local.properties:
- ✅ Never commit `local.properties` to git (already in .gitignore)
- ✅ Store sensitive keys here only
- ✅ Use empty string defaults in build.gradle.kts
- ✅ Share keys securely (password manager, not chat)

### For Supabase:
- ✅ Always use Row Level Security (RLS) policies
- ✅ Anon key can be public IF RLS is properly configured
- ✅ Never expose service role key (should only be server-side)

### General:
- ✅ Rotate keys immediately after exposure
- ✅ Use environment variables for CI/CD
- ✅ Monitor API usage for anomalies
- ✅ Set up billing alerts

## 🚀 After Rotating Keys

1. Update all keys in `local.properties`
2. Delete this file: `SECURITY_UPDATE_REQUIRED.md`
3. Rebuild the app
4. Test that everything works
5. Monitor API usage for any issues

## 📞 Need Help?

- Replicate: https://replicate.com/docs
- Google AI Studio: https://ai.google.dev/gemini-api/docs
- Supabase: https://supabase.com/docs
