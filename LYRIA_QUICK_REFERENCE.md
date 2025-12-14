# 🎵 Lyria Music Generation - Quick Reference

## ⚡ Quick Start (2 Minutes)

### 1. Enable Lyria

```kotlin:app/src/main/java/com/sparkiai/app/config/FeatureFlags.kt
const val ENABLE_LYRIA_MUSIC_GENERATION = true  // ✅ Set to true
```

### 2. Add Your Project ID

```kotlin:app/src/main/java/com/sparkiai/app/config/FeatureFlags.kt
const val PROJECT_ID = "your-project-id-here"  // ← Your Google Cloud project
```

### 3. Enable Vertex AI

- Go to: https://console.cloud.google.com/apis/library/aiplatform.googleapis.com
- Click "ENABLE"

### 4. Build & Test

```bash
./gradlew clean && ./gradlew installDebug
```

---

## 🎛️ Toggle On/Off

### Turn OFF (Lyrics Only - FREE)

```kotlin
const val ENABLE_LYRIA_MUSIC_GENERATION = false
```

Rebuild app. Music Composer = lyrics only, $0 cost.

### Turn ON (Music Generation - Freemium)

```kotlin
const val ENABLE_LYRIA_MUSIC_GENERATION = true
```

Rebuild app. Music Composer = lyrics + music files.

---

## 💰 Pricing

| Feature | Cost |
|---------|------|
| Lyrics, chords, guidance | FREE (always) |
| First 10 songs | FREE |
| Songs 11+ | $0.06 each |

---

## 📁 Files You Created

| File | Purpose |
|------|---------|
| `FeatureFlags.kt` | Configuration & toggle |
| `LyriaService.kt` | API integration |
| `MusicGenerationTracker.kt` | Usage & cost tracking |
| `MusicLibraryManager.kt` | File storage |
| `GeneratedMusic.kt` | Data model |

---

## 🎵 What Users Get

Each generated track:

- ✨ 30 seconds of instrumental music
- 📝 Lyrics from Music Composer
- 💾 High-quality WAV (48kHz)
- ▶️ Playable in-app
- ⬇️ Downloadable
- 📚 Saved in library

---

## ⚙️ Configuration Quick Reference

### Freemium Settings

```kotlin:FeatureFlags.kt
FREE_SONGS_LIMIT = 10              // Change free tier
COST_PER_SONG_CENTS = 6            // $0.06 per song
SHOW_UPGRADE_PROMPT_AT = 8         // Show at song 8
```

### Library Settings

```kotlin:FeatureFlags.kt
MAX_LIBRARY_SONGS = 50             // Keep last 50 tracks
ENABLE_IN_APP_PLAYBACK = true      // Play in app
ALLOW_MUSIC_DOWNLOAD = true        // Download feature
```

### API Settings

```kotlin:FeatureFlags.kt
PROJECT_ID = "your-project-id"     // Google Cloud project
LOCATION = "us-central1"           // API region
MODEL_NAME = "lyria-002"           // Model version
```

---

## 🧪 Test Prompts

```
"Create an upbeat electronic dance track"
"Make a calm acoustic guitar melody"
"Generate energetic hip-hop beats"
"Create a sad piano ballad"
"Make upbeat jazz with saxophone"
```

---

## 📊 Check Status

### In Code

```kotlin
val service = LyriaService()
val status = service.getServiceStatus()

when (status) {
    is Ready -> "Music generation ready!"
    is Disabled -> "Feature disabled: ${status.reason}"
    is NotConfigured -> "Setup needed: ${status.reason}"
}
```

### Check Usage

```kotlin
val tracker = MusicGenerationTracker(context)
val stats = tracker.getUsageStats()

println("Total songs: ${stats.totalGenerated}")
println("Free remaining: ${stats.freeRemaining}")
println("Total cost: ${stats.totalCostFormatted}")
```

---

## 🔧 Common Tasks

### Change Free Song Limit

```kotlin
const val FREE_SONGS_LIMIT = 20  // Give 20 free songs instead of 10
```

### Change Price

```kotlin
const val COST_PER_SONG_CENTS = 10  // Charge $0.10 instead of $0.06
```

### Disable Cost Display

```kotlin
const val SHOW_COST_ESTIMATE = false
const val SHOW_FREE_SONGS_COUNTER = false
```

### Increase Library Size

```kotlin
const val MAX_LIBRARY_SONGS = 100  // Keep 100 tracks instead of 50
```

---

## 🐛 Troubleshooting

### "Music generation unavailable"

→ Check `ENABLE_LYRIA_MUSIC_GENERATION = true`  
→ Verify `PROJECT_ID` is set  
→ Enable Vertex AI in Google Cloud

### "Authentication failed"

→ Check API key in `local.properties`  
→ Verify Vertex AI permissions

### "Model not found"

→ Ensure Vertex AI API is enabled  
→ Verify region is correct

### Generation takes long

→ Normal: 10-30 seconds  
→ Check network connection

---

## 📖 Documentation

- **Setup Guide**: `LYRIA_SETUP_COMPLETE_GUIDE.md`
- **API Details**: `LYRIA_API_INTEGRATION.md`
- **This File**: Quick reference

---

## 🎯 Key Features

✅ Real music generation (30s tracks)  
✅ Freemium model (10 free songs)  
✅ Easy on/off toggle (one flag)  
✅ Usage tracking (costs & limits)  
✅ Music library (file storage)  
✅ Cost transparency (show users)  
✅ Production ready

---

## 💡 Pro Tips

1. **Test first** with `ENABLE_LYRIA_MUSIC_GENERATION = true`
2. **Monitor costs** in first week of release
3. **Adjust free tier** based on user feedback
4. **Keep PROJECT_ID private** (already in gitignore)
5. **Enable billing** in Google Cloud before launch

---

## 📞 Support

**Error logs**: Look for "LyriaService" tag  
**Config**: All in `FeatureFlags.kt`  
**Toggle**: Change one flag, rebuild  
**Docs**: See full guides in root directory

---

## ✅ Pre-Launch Checklist

- [ ] Set correct `PROJECT_ID`
- [ ] Enable Vertex AI API
- [ ] Test music generation
- [ ] Verify free tier tracking
- [ ] Test cost calculations
- [ ] Check library saves files
- [ ] Test playback works
- [ ] Enable billing in Google Cloud
- [ ] Set desired `FREE_SONGS_LIMIT`
- [ ] Configure `COST_PER_SONG_CENTS`

---

## 🚀 Ready to Launch!

**To Enable**: `ENABLE_LYRIA_MUSIC_GENERATION = true`  
**To Disable**: `ENABLE_LYRIA_MUSIC_GENERATION = false`  
**Cost**: $0 (free tier) → $0.06/song  
**Setup Time**: ~5 minutes  
**Status**: ✅ **READY TO USE**

---

**Quick Links**:

- Google Cloud Console: https://console.cloud.google.com/
- Vertex AI API: https://console.cloud.google.com/apis/library/aiplatform.googleapis.com
- Lyria Docs: https://cloud.google.com/vertex-ai/generative-ai/docs/music/

**Last Updated**: December 2024  
**Version**: 1.0
