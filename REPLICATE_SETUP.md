# Replicate Music Generation Setup

## What Changed?

We've switched from **Stability AI (Stable Audio)** to **Replicate (MusicGen)** for music generation
due to immediate credit exhaustion with Stability AI.

## Why Replicate?

✅ **Better Value**: Pay-as-you-go pricing (~$0.01-0.05 per generation)  
✅ **No Monthly Subscription**: Only pay for what you use  
✅ **High Quality**: Uses Meta's MusicGen model  
✅ **Simple API**: Easy HTTP REST API  
✅ **30 Second Tracks**: Perfect for testing and demos

## Setup Instructions

### 1. Get Your API Token

1. Go to https://replicate.com
2. Sign up for a free account
3. Go to https://replicate.com/account/api-tokens
4. Copy your API token (starts with `r8_...`)

### 2. Add to local.properties

Your token is already configured in `local.properties`:

```properties
REPLICATE_API_KEY=r8_TZCN0zoHDm4Xh7nd7xDoonhv6aqz0IO37RuSXo
```

✅ **Already configured!**

### 3. Pricing

Replicate uses pay-as-you-go pricing:

- **~$0.01-0.05 per generation** (30 seconds of music)
- No monthly subscription
- Only charged for actual usage
- Much cheaper than Stability AI's subscription model

### 4. How It Works

1. **User requests music** with a text prompt
2. **MusicGen generates** 30 seconds of high-quality instrumental music
3. **Saved to library** for playback, download, and sharing

### 5. Model Details

We're using **Meta's MusicGen Melody** model:

- High-quality stereo audio
- WAV format output
- 30-second duration max
- Supports multiple genres and styles
- Instrumental music (no vocals)

### 6. Example Prompts

Good prompts for MusicGen:

- ✅ "upbeat electronic dance music with energetic beats and synths"
- ✅ "mellow acoustic guitar, peaceful and relaxing"
- ✅ "jazz fusion with saxophone and groovy bass"
- ✅ "epic orchestral music with strings and horns"
- ✅ "lo-fi hip hop beats, chill and atmospheric"

## Technical Implementation

### Files Changed

1. **`ReplicateService.kt`** - New service for Replicate API
2. **`ChatViewModel.kt`** - Updated to use ReplicateService
3. **`build.gradle.kts`** - Added REPLICATE_API_KEY configuration
4. **`local.properties`** - Contains your API key

### Architecture

```
User Input → ChatViewModel → ReplicateService → MusicGen API
                ↓
         MusicLibraryManager
                ↓
         Audio Saved & Playable
```

## Testing

To test music generation:

1. Open the app
2. Select the **Music Composer** personality
3. Enter a music prompt like: "upbeat electronic music with synths"
4. Wait ~30-60 seconds for generation
5. Music will be saved to your library

## Troubleshooting

### "Replicate API key not configured"

- Check that `REPLICATE_API_KEY` is in `local.properties`
- Rebuild the project: `./gradlew clean build`

### "Music generation failed"

- Check your Replicate account balance
- Ensure API token is valid and active
- Try a simpler prompt

### Generation takes too long

- MusicGen takes 30-60 seconds to generate
- This is normal for AI music generation
- A loading indicator shows progress

## Cost Comparison

| Service | Model | Cost | Free Tier |
|---------|-------|------|-----------|
| **Replicate** | MusicGen | $0.01-0.05/gen | First $5 free |
| Stability AI | Stable Audio | Ran out immediately | ❌ None |
| Suno AI | Proprietary | $10/month | 500 credits |
| Google Lyria | Vertex AI | $0.06/30s | Complex setup |

**Winner**: Replicate for best balance of cost, quality, and simplicity! 🏆

## Next Steps

Your app is now ready to generate music with Replicate! 🎵

Just build and run the app to test it out.
