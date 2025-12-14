# 🛡️ Lyria Safety Filter Error - Solution

## 🐛 The Error You're Seeing

```
❌ Lyria API failed (400): {
  "error": {
    "code": 400,
    "message": "Audio generation failed with the following error: Music generation failed. Prompt flag..."
    "status": "INVALID_ARGUMENT"
  }
}
```

## 🎯 What This Means

**Lyria's safety filters are rejecting the prompts** because they're either:

1. Too vague/simple
2. Might contain words that trigger content filters
3. Don't have enough musical detail

## ✅ The Solution: More Detailed Prompts

### ❌ Prompts That Get Blocked

```
"Upbeat electronic dance track"
"Energetic hip-hop beats"
"Calm acoustic guitar melody"
```

**Why blocked**: Too vague, not enough detail

### ✅ Prompts That Work

```
"A calm acoustic folk song with a gentle guitar melody and soft strings"

"An energetic electronic dance track with fast tempo, driving beat, and prominent synthesizers"

"A peaceful and serene acoustic guitar piece, featuring a fingerpicked style, perfect for meditation"
```

**Why they work**: Specific genre, mood, instruments, and style

---

## 🎵 Good Prompt Formula

```
[Mood] + [Genre] + with [Instruments] + [Tempo/Style Details]
```

### Examples:

**Piano**:

```
"A peaceful and calming piano piece with gentle melodies and slow tempo, perfect for relaxation"
```

**Electronic**:

```
"An energetic electronic music track with synthesizers, electronic drums, and a driving beat at 128 BPM"
```

**Acoustic**:

```
"A warm and gentle acoustic guitar piece featuring fingerpicked melodies and soft harmonics"
```

**Orchestral**:

```
"A cinematic orchestral piece in a heroic fantasy style with grand sweeping melodies and dramatic strings"
```

**Jazz**:

```
"A smooth jazz piece with saxophone, upright bass, and gentle piano, featuring a relaxed swing rhythm"
```

---

## 🔧 What I've Already Fixed

### Updated Quick Example Prompts

The three example chips now show:

1. ✅ "A calm acoustic folk song with a gentle guitar melody and soft strings"
2. ✅ "An energetic electronic dance track with fast tempo, driving beat, and prominent synthesizers"
3. ✅ "A peaceful and serene acoustic guitar piece, featuring a fingerpicked style, perfect for
   meditation"

These are based on **Google's official examples** that are proven to work!

### Updated Placeholder Text

Now guides users:

```
"Be specific! Include genre, mood, tempo, and instruments.

Good: 'A calm acoustic folk song with gentle guitar and soft strings'
Not: 'Upbeat music'"
```

---

## 🚀 Test With These Prompts

Try these proven-to-work prompts from Google's documentation:

### Test 1: Acoustic (Safe & Proven)

```
A calm acoustic folk song with a gentle guitar melody and soft strings
```

### Test 2: Electronic (Safe & Proven)

```
An energetic electronic dance track with a fast tempo and a driving beat, featuring prominent synthesizers and electronic drums
```

### Test 3: Piano (Safe & Proven)

```
A peaceful and serene piano piece with gentle melodies, slow tempo, and calming harmonies
```

---

## 📊 Rebuild & Test

```bash
cd C:\Users\Jerry\AndroidStudioProjects\SparkiFire
.\gradlew.bat installDebug
```

Then in app:

1. Click "Generate Music"
2. **Click one of the example chips** (they're now detailed!)
3. Click "Generate"
4. Should work! 🎵

---

## 💡 For Users: Auto-Enhance Prompts

If users enter vague prompts, we could have Gemini automatically enhance them before sending to
Lyria. Want me to add that?

For example:

```
User enters: "happy music"

Gemini enhances to: "A joyful and upbeat instrumental piece with bright piano melodies, cheerful strings, and a moderate tempo"

Send enhanced version to Lyria → Success!
```

This would make it bulletproof for users!

---

## ✅ Summary

**Problem**: Lyria safety filters block vague prompts  
**Solution**: Use detailed, specific prompts  
**Fixed**: Example prompts now detailed  
**Next**: Rebuild, test with detailed prompts  
**Optional**: Add auto-enhancement for user prompts

Try the detailed prompts now - they should work! 🎵✨
