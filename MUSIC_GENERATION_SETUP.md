# Music Generation Setup - Replicate (Minimax Music-1.5)

## ✅ Successfully Switched from Stability AI to Replicate

Your app now uses **Minimax Music-1.5** via Replicate for music generation!

---

## 🎵 What Changed

### Before: Stability AI (Stable Audio)

- ❌ Ran out of credits immediately
- Cost: ~$0.10-0.20 per generation
- Duration: 30-90 seconds

### After: Replicate (Minimax Music-1.5)

- ✅ Pay-as-you-go pricing
- ✅ ~$0.015 per generation (much cheaper!)
- ✅ **Full songs with vocals and instrumentals**
- ✅ Auto-generated lyrics based on your prompt
- ✅ No monthly subscription needed

---

## 📋 Setup Complete

### ✅ API Key Configured

Your Replicate API key is already set in `local.properties`:

```
REPLICATE_API_KEY=r8_TZCN0zoHDm4Xh7nd7xDoonhv6aqz0IO37RuSXo
```

### ✅ Code Updated

- Created `ReplicateService.kt` with Minimax Music-1.5 integration
- Updated `ChatViewModel.kt` to use Replicate instead of Stability AI
- Updated `app/build.gradle.kts` to include Replicate API key

---

## 🚀 How to Use

### 1. Switch to Music Composer Personality

In your app, switch to the **Music Composer (Lyria)** personality.

### 2. Generate Music

Simply describe the music you want:

**Good Examples:**

- "Jazz, Smooth Jazz, Romantic, Dreamy"
- "upbeat pop song, catchy melody, energetic vocals"
- "mellow acoustic guitar, peaceful and relaxing"
- "electronic dance music with energetic beats"
- "rock ballad with emotional vocals"

**The AI will:**

1. Enhance your prompt (if it's too short)
2. Generate full song with vocals
3. Auto-create lyrics that match the mood
4. Return a complete MP3 file

### 3. Cost Tracking

Your app tracks usage with the free song system:

- First few songs are FREE
- After free tier: ~$0.015 per song

---

## 🎼 Technical Details

### Model: Minimax Music-1.5

- **Platform**: Replicate
- **Documentation**: https://replicate.com/minimax/music-1.5
- **Output**: MP3 format, full songs (~30-180 seconds)
- **Features**: Vocals + instrumentals + auto-generated lyrics

### API Details

- **Endpoint**: `https://api.replicate.com/v1/models/minimax/music-1.5/predictions`
- **Method**: POST with polling for completion
- **Auth**: Bearer token in header
- **Timeout**: 2 minutes max

### Service File

Location: `app/src/main/java/com/sparkiai/app/network/ReplicateService.kt`

Key features:

- Async prediction creation
- Polling for completion (2-second intervals)
- Automatic audio download
- Error handling with retry support

---

## 💡 Tips for Best Results

1. **Be Descriptive**: Instead of "happy song", try "upbeat pop music, cheerful vocals, bright
   melody"

2. **Specify Genre**: The AI works best with clear genre tags:
    - "Jazz, Smooth Jazz, Romantic"
    - "Rock, Alternative Rock, Energetic"
    - "Electronic, House, Dance"

3. **Add Mood**: Include emotional descriptors:
    - "melancholic", "joyful", "peaceful", "intense"

4. **Use Commas**: Separate different attributes:
    - "Pop, upbeat, female vocals, summer vibes"

---

## 🐛 Troubleshooting

### "Replicate API key not configured"

- Check that `local.properties` has `REPLICATE_API_KEY` (not `REPLICATE_API_TOKEN`)
- Rebuild the project: `./gradlew clean build`

### "Music generation timed out"

- The model can take up to 2 minutes
- If it consistently times out, check your Replicate account status
- Check API quota at https://replicate.com/account

### "Content filter blocked your request"

- Rephrase your prompt to focus on musical style
- Avoid specific song titles or artist names
- Use generic mood/genre descriptors

### Build Errors

If you get build errors after changes:

```bash
./gradlew clean build --no-daemon
```

---

## 📊 Cost Comparison

| Service | Cost per Song | Monthly Subscription | Quality |
|---------|--------------|---------------------|---------|
| **Replicate (Minimax)** | **$0.015** | **None** | **Full songs with vocals** ✅ |
| Stability AI | $0.10-0.20 | None | Instrumental only |
| Suno AI | $0.004 | $10/month | Full songs with vocals |
| Google Lyria | $0.06 | None | Complex setup required |

---

## ✅ What's Working Now

1. ✅ API key configured
2. ✅ Service integrated
3. ✅ ChatViewModel updated
4. ✅ Build successful
5. ✅ Ready to generate music!

---

## 🎵 Ready to Test!

Run your app and try generating music with prompts like:

- "upbeat electronic dance music"
- "mellow jazz piano, smooth and relaxing"
- "rock anthem with powerful vocals"

The music will be saved to your library automatically! 🎧

---

## 📝 Notes

- Minimax Music-1.5 generates better quality than MusicGen
- Auto-generates lyrics based on your prompt
- No need to provide custom lyrics (but you can if you want to extend the API)
- Pay only for what you use - no subscriptions!

---

**Last Updated**: Successfully migrated from Stability AI to Replicate
**Status**: ✅ Ready to use
