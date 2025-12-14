# Stable Audio Setup Guide

SparkiFire now uses **Stable Audio 2.0** from Stability AI for music generation!

## ✅ What Changed

- **Replaced:** Google's Lyria (too restrictive, censorship issues)
- **New Service:** Stable Audio 2.0 (Stability AI)
- **Benefits:**
    - ✅ Official API with proper documentation
    - ✅ High-quality music generation (vocals + instrumentals)
    - ✅ Minimal content filtering (no unreasonable censorship)
    - ✅ Commercial use allowed
    - ✅ 30-90 second tracks
    - ✅ Multiple genres and styles

## 🔑 Setup Instructions

### 1. Add Your Stability AI API Key

You already have a Stability AI API key. Add it to your `local.properties` file:

```properties
# In: local.properties
STABILITY_API_KEY=sk-your-actual-stability-api-key-here
```

### 2. Rebuild the Project

After adding the API key to `local.properties`, rebuild the project:

```bash
./gradlew clean build
```

Or in Android Studio: **Build → Rebuild Project**

### 3. That's It!

The music generation will now use Stable Audio instead of Lyria. Everything else works the same!

---

## 📖 API Documentation

- **Stable Audio API Docs:** https://platform.stability.ai/docs/api-reference#tag/Audio
- **Endpoint:** `https://api.stability.ai/v2beta/stable-audio/generate/music`
- **Format:** Multipart form data
- **Parameters:**
    - `prompt`: Text description of the music
    - `duration`: Length in seconds (30-90)
    - `output_format`: "mp3" or "wav"

---

## 💰 Pricing

- **Cost:** ~$0.10-0.20 per generation
- **Duration:** 30-90 seconds per track
- **Billing:** Credits deducted from your Stability AI account

---

## 🎵 Example Prompts

**✅ Good Prompts:**

- "upbeat comedic song with funky groove, playful vocals, cheeky attitude, saxophone and bass"
- "emotional ballad with piano and strings, slow tempo, heartfelt female vocals"
- "energetic rock anthem with electric guitars, driving drums, powerful vocals"
- "smooth jazz instrumental, saxophone lead, walking bass, laid-back tempo"

**❌ Avoid:**

- Too vague: "happy music"
- Just titles: "My Song Name"
- Too short descriptions

---

## 🔧 Technical Details

### Files Updated:

1. `app/src/main/java/com/sparkiai/app/network/StableAudioService.kt` - New service
2. `app/src/main/java/com/sparkiai/app/viewmodel/ChatViewModel.kt` - Updated to use Stable Audio
3. `app/build.gradle.kts` - Added STABILITY_API_KEY config
4. Removed: `SunoService.kt` (old service)

### Configuration:

- Feature flag: `FeatureFlags.ENABLE_LYRIA_MUSIC_GENERATION` (kept same name for compatibility)
- Default duration: 45 seconds
- Output format: MP3
- API timeout: 120 seconds

---

## 🎯 Usage

The Music Composer personality will automatically use Stable Audio when:

1. Feature flag is enabled
2. API key is configured
3. User has credits/free songs available

**No code changes needed in the UI!** Everything works the same way as before.

---

## ❓ Troubleshooting

### "API key not configured"

- Make sure you added `STABILITY_API_KEY` to `local.properties`
- Rebuild the project after adding the key

### "Insufficient credits"

- Check your Stability AI account balance
- Add credits at https://platform.stability.ai/

### "Rate limit exceeded"

- Stability AI has rate limits on the free tier
- Upgrade your plan or wait a moment

---

## 🎉 Benefits Over Lyria

| Feature | Lyria (Google) | Stable Audio (Stability AI) |
|---------|----------------|----------------------------|
| **Censorship** | ❌ Very restrictive | ✅ Minimal filtering |
| **API Status** | ⚠️ Limited access | ✅ Official public API |
| **Quality** | ✅ Good | ✅ Excellent |
| **Duration** | 30 seconds | 30-90 seconds |
| **Vocals** | ✅ Yes | ✅ Yes |
| **Commercial Use** | ⚠️ Unclear | ✅ Allowed |
| **Documentation** | ❌ Limited | ✅ Comprehensive |

---

**Your music generation is now powered by Stable Audio! 🎵✨**
