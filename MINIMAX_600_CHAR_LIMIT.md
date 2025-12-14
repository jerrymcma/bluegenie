# Minimax Music 600-Character Limit - What You Need to Know

## 🚨 Important Limitation

The **Minimax Music-1.5 API** has a **hard limit of 600 characters** for lyrics. This is an API
restriction we cannot change.

---

## ✅ What Works Great

### **Style Descriptions** (RECOMMENDED)

Users can type simple prompts and get amazing results:

- ✅ "make a love song"
- ✅ "upbeat pop music, energetic"
- ✅ "jazz, smooth, romantic, slow tempo"
- ✅ "rock anthem with powerful vocals"

**Result:** AI generates placeholder lyrics + music in that style. **This works perfectly!**

---

### **Short Lyrics** (600 chars max)

Users can provide their own lyrics if kept short:

- ✅ Max: **600 characters** (~10-15 lines)
- ✅ Use tags: `[Verse]`, `[Chorus]`, `[Bridge]`
- ✅ AI sings the exact lyrics with music

**Example (works):**

```
[Verse]
Walking down the street
Feeling so complete
Love is in the air tonight

[Chorus]
We're dancing all night long
To our favorite song
Everything feels right
```

---

## ❌ What Doesn't Work

- ❌ **Full song lyrics** (over 600 characters)
- ❌ **Multiple verses + chorus + bridge** (usually >600 chars)
- ❌ **Long poems or full lyrical compositions**

---

## 🎵 Current Configuration

### What Users Get:

- **5 FREE songs** (30-60 seconds each)
- **$0.02 per song** after free tier
- **High-quality MP3** with vocals
- **Format:** Music singing the prompt/lyrics

### Technical Limits:

- **Max lyrics:** 600 characters (API limit)
- **Duration:** ~30-60 seconds per generation
- **Output:** MP3 format
- **Generation time:** 30-60 seconds

---

## 📝 Recommended UI Updates

### 1. Text Field

```kotlin
maxLength = 600  // Not 2000
supportingText = "${text.length}/600 characters"
```

### 2. Placeholder Text

```
"Option 1 - Short Lyrics (max 600 chars):
[Verse]
Your lyrics...

Option 2 - Style Description:
Make a love song"
```

### 3. Info Card

```
• Duration: ~30-60 seconds per generation
• Lyrics: Max 600 characters (10-15 lines)
• Style prompts work great (e.g., 'make a love song')
• Format: High-quality MP3 with vocals
```

---

## 🎯 User Expectations

**Set these expectations clearly:**

1. ✅ Each generation = **30-60 seconds** of music
2. ✅ Style descriptions work **perfectly**
3. ✅ Short lyrics = **AI sings them**
4. ✅ Long lyrics won't work (API limit)
5. ✅ **5 free songs**, then $0.02 each

---

## 💡 Best Practices

### For Users Who Want Longer Songs:

1. Generate multiple 30-60 second clips
2. Each clip can have different lyrics (within 600 chars)
3. User can stitch them together externally

### For Users Who Want Full Lyrics:

1. Split into chunks of 600 characters
2. Generate multiple songs
3. Or just use **style descriptions** (works amazing!)

---

## ✅ What's Already Working

Your test: **"make a love song"**

- ✅ Generated a great song
- ✅ AI created lyrics automatically
- ✅ Music + vocals in that style
- ✅ **This is the ideal use case!**

---

## 🎉 Bottom Line

The 600-character limit is fine because:

1. **Style descriptions work perfectly** (most users will use this)
2. Short lyrics still work (10-15 lines)
3. 30-60 seconds is normal for AI music
4. Quality is excellent
5. Cheap pricing ($0.02 per song)

**Focus on promoting style descriptions** rather than full lyrics!
