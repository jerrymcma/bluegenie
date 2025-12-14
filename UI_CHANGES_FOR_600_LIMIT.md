# UI Changes Needed for 600-Character Limit

## Changes to Make in `MusicGenerationUI.kt`

### 1. Update TextField Character Limit

**Line ~253-258**

Change from:

```kotlin
onValueChange = { 
    // Allow up to 2000 characters for full song lyrics
    if (it.length <= 2000) {
        musicPrompt = it
    }
}
```

To:

```kotlin
onValueChange = { 
    // Minimax API limit: 600 characters max
    if (it.length <= 600) {
        musicPrompt = it
    }
}
```

---

### 2. Update Placeholder Text

**Line ~266-273**

Change from:

```kotlin
"Option 1 - Full Lyrics:\n" +
    "[Verse]\nYour lyrics...\n" +
    "[Chorus]\nYour chorus...\n\n" +
    "Option 2 - Style Description:\n" +
    "Jazz, smooth, romantic, slow tempo"
```

To:

```kotlin
"Option 1 - Short Lyrics (max 600 chars):\n" +
    "[Verse]\nYour lyrics...\n" +
    "[Chorus]\nYour chorus...\n\n" +
    "Option 2 - Style Description:\n" +
    "Make a love song"
```

---

### 3. Update Max Lines

**Line ~275**

Change from:

```kotlin
maxLines = 20,  // Increased to 20 lines for full lyrics
```

To:

```kotlin
maxLines = 12,  // 600 chars ≈ 10-12 lines
```

---

### 4. Update Character Counter

**Line ~283-287**

Change from:

```kotlin
supportingText = {
    Text(
        text = "${musicPrompt.length}/2000 characters",
        fontSize = 10.sp,
        color = if (musicPrompt.length > 1900) Color.Red else Color.Gray
    )
}
```

To:

```kotlin
supportingText = {
    Text(
        text = "${musicPrompt.length}/600 characters",
        fontSize = 10.sp,
        color = when {
            musicPrompt.length > 600 -> Color.Red
            musicPrompt.length > 550 -> Color(0xFFFF9800)  // Orange warning
            else -> Color.Gray
        }
    )
}
```

---

### 5. Update Info Card

**Line ~318-322**

Change from:

```kotlin
text = "• Duration: ~2 minutes (full song)\n" +
       "• LYRICS: Sings your exact words with music\n" +
       "• STYLE: Generates music in that genre/mood\n" +
       "• Format: High-quality MP3\n" +
       "• Generation time: ~30-60 seconds"
```

To:

```kotlin
text = "• Duration: ~30-60 seconds per generation\n" +
       "• Lyrics: Max 600 characters (10-15 lines)\n" +
       "• Style prompts work great! (e.g., 'make a love song')\n" +
       "• Format: High-quality MP3 with vocals\n" +
       "• Generation time: ~30-60 seconds"
```

---

### 6. Update Tips Card

**Line ~344-349**

Change from:

```kotlin
text = "Use [Verse], [Chorus], [Bridge] tags to structure your song. The AI will sing your exact lyrics with music!"
```

To:

```kotlin
text = "• Simple prompts work best: 'make a love song', 'upbeat pop'\n" +
       "• For lyrics: Keep under 600 chars, use [Verse]/[Chorus] tags\n" +
       "• Each generation = ~30-60 sec of music with vocals"
```

---

## ✅ Already Done

These files have already been updated:

- ✅ `FeatureFlags.kt` - Updated to 60 seconds duration
- ✅ `FeatureFlags.kt` - Updated free tier to 5 songs
- ✅ `ChatViewModel.kt` - Updated messaging

---

## 🎯 Summary

Just need to update **`MusicGenerationUI.kt`** with the 6 changes above to:

1. Enforce 600-character limit
2. Show accurate character counter
3. Set realistic expectations
4. Highlight that style descriptions work great!

The app will work perfectly with these changes! 🎉
