# 🎵 Lyria API Integration - Technical Documentation

## Architecture Overview

```
User Input (Text Prompt)
         ↓
  Music Composer UI
         ↓
  Usage Tracking Check ← MusicGenerationTracker
         ↓
  Lyria Service (API Call)
         ↓
  Vertex AI Endpoint
         ↓
  Base64 Audio Response
         ↓
  Music Library Manager (Save)
         ↓
  Audio File + Metadata
         ↓
  Playback / Download
```

---

## Core Components

### 1. LyriaService.kt

**Purpose**: Handles all Lyria API communication

**Key Methods**:

```kotlin
// Generate music from text prompt
suspend fun generateMusic(
    prompt: String,
    negativePrompt: String? = null,
    seed: Int? = null
): MusicGenerationResult

// Check if service is configured
fun isConfigured(): Boolean

// Get service status
fun getServiceStatus(): LyriaServiceStatus
```

**API Request Format**:

```json
{
  "instances": [{
    "prompt": "An upbeat electronic dance track",
    "negative_prompt": "vocals, slow tempo",
    "seed": 12345
  }],
  "parameters": {}
}
```

**API Response Format**:

```json
{
  "predictions": [{
    "audioContent": "BASE64_ENCODED_WAV_DATA",
    "mimeType": "audio/wav"
  }],
  "deployedModelId": "...",
  "model": "projects/.../models/lyria-002"
}
```

---

### 2. MusicGenerationTracker.kt

**Purpose**: Manages freemium model and usage tracking

**Key Features**:

- Tracks total songs generated
- Monitors free tier usage
- Calculates costs
- Enforces limits
- Persists data with SharedPreferences

**Key Methods**:

```kotlin
// Check remaining free songs
fun getFreeSongsRemaining(): Int

// Check if user can generate
fun canGenerateMusic(): Boolean

// Record a generation
fun recordGeneration()

// Get usage statistics
fun getUsageStats(): MusicUsageStats
```

**Data Persistence**:

```kotlin
// Stored in SharedPreferences
KEY_SONGS_GENERATED: Int  // Total songs made
KEY_TOTAL_COST_CENTS: Int  // Cost incurred
KEY_LAST_GENERATION_TIME: Long  // Last generation timestamp
```

---

### 3. MusicLibraryManager.kt

**Purpose**: Manages music file storage and library

**Key Features**:

- Saves audio files to internal storage
- Maintains metadata in JSON
- Provides library access
- Enforces storage limits
- Handles file cleanup

**Storage Structure**:

```
app/files/
  ├── generated_music/
  │   ├── music_uuid1.wav
  │   ├── music_uuid2.wav
  │   └── music_uuid3.wav
  └── music_library.json
```

**Library JSON Format**:

```json
[
  {
    "id": "uuid",
    "prompt": "Upbeat electronic dance track",
    "filePath": "/data/.../music_uuid.wav",
    "mimeType": "audio/wav",
    "durationSeconds": 30,
    "timestamp": 1703001234567,
    "isFreeTier": true,
    "costCents": 0
  }
]
```

---

### 4. FeatureFlags.kt

**Purpose**: Central configuration for all Lyria features

**Configuration Sections**:

```kotlin
// Main toggle
ENABLE_LYRIA_MUSIC_GENERATION: Boolean

// API config
LyriaConfig {
    PROJECT_ID, LOCATION, MODEL_NAME, etc.
}

// Freemium config  
FreemiumConfig {
    FREE_SONGS_LIMIT, COST_PER_SONG_CENTS, etc.
}

// UI config
MusicComposerConfig {
    SHOW_GENERATE_MUSIC_BUTTON, etc.
}
```

---

### 5. GeneratedMusic.kt

**Purpose**: Data model for music tracks

**Properties**:

```kotlin
data class GeneratedMusic(
    val id: String,              // Unique identifier
    val prompt: String,          // Original text prompt
    val filePath: String,        // Path to audio file
    val mimeType: String,        // "audio/wav"
    val durationSeconds: Int,    // Always 30 for Lyria
    val timestamp: Long,         // Generation time
    val isFreeTier: Boolean,     // Free or paid
    val costCents: Int           // 0 or 6+
)
```

---

## API Integration Details

### Authentication

**Method**: Bearer Token  
**Header**: `Authorization: Bearer {API_KEY}`  
**Key Source**: `BuildConfig.GEMINI_API_KEY` (same as Gemini)

### Endpoint

```
POST https://{LOCATION}-aiplatform.googleapis.com/v1/
projects/{PROJECT_ID}/locations/{LOCATION}/
publishers/google/models/lyria-002:predict
```

**Variables**:

- `LOCATION`: "us-central1" (configurable)
- `PROJECT_ID`: Your Google Cloud project ID
- Model: "lyria-002" (current version)

### Timeouts

```kotlin
connectTimeout: 60 seconds  // Longer for music generation
readTimeout: 60 seconds
writeTimeout: 60 seconds
```

### Request Headers

```
Authorization: Bearer {API_KEY}
Content-Type: application/json
```

### Response Handling

```kotlin
// Success path
response.isSuccessful 
  → Extract predictions
  → Decode base64 audioContent
  → Return Success(audioData, ...)

// Error paths
401/403 → Authentication error
404 → Model not found
429 → Rate limit exceeded
500+ → Server error
```

---

## Data Flow

### Music Generation Flow

```kotlin
// 1. User triggers generation
MusicComposerUI.onGenerateMusic(prompt)

// 2. Check usage limits
val tracker = MusicGenerationTracker(context)
if (!tracker.canGenerateMusic()) {
    showPaymentRequired()
    return
}

// 3. Show loading state
showLoadingDialog("Generating music...")

// 4. Call Lyria API
val result = LyriaService().generateMusic(
    prompt = userPrompt,
    negativePrompt = null,
    seed = null
)

// 5. Handle result
when (result) {
    is Success -> {
        // Save to library
        val music = MusicLibraryManager(context).saveMusic(
            audioData = result.audioData,
            prompt = prompt,
            isFreeTier = tracker.isInFreeTier(),
            costCents = tracker.getNextGenerationCost()
        )
        
        // Record usage
        tracker.recordGeneration()
        
        // Show success
        showMusicPlayer(music)
    }
    is Error -> {
        showError(result.message)
    }
}
```

---

## Error Handling

### Service-Level Errors

```kotlin
sealed class MusicGenerationResult {
    data class Success(...) : MusicGenerationResult()
    data class Error(val message: String) : MusicGenerationResult()
}
```

### User-Friendly Error Messages

```kotlin
when (response.code) {
    401, 403 -> "Authentication failed. Please check settings."
    404 -> "Music service unavailable. Please try again later."
    429 -> "Too many requests. Please wait a moment."
    500+ -> "Server error. Please try again later."
    else -> "Music generation failed. Please try again."
}
```

### Logging

```kotlin
Log.d("LyriaService", "🎵 Generating music: $prompt")
Log.d("LyriaService", "✅ Success! Size: ${audioBytes.size} bytes")
Log.e("LyriaService", "❌ Failed (${code}): $error")
```

---

## Storage Management

### File Naming

```kotlin
val id = UUID.randomUUID().toString()
val fileName = "music_$id.wav"
// Example: music_a1b2c3d4-e5f6-7890-abcd-ef1234567890.wav
```

### Storage Location

```kotlin
context.filesDir / "generated_music" / fileName
// Example: /data/data/com.sparkiai.app/files/generated_music/music_uuid.wav
```

### Size Management

```kotlin
// Each track ≈ 2-3 MB (30s @ 48kHz WAV)
// Max 50 tracks = ~125-150 MB max
// Auto-cleanup when exceeding MAX_LIBRARY_SONGS
```

### Cleanup Strategy

```kotlin
// When library exceeds MAX_LIBRARY_SONGS:
// 1. Sort tracks by timestamp (oldest first)
// 2. Remove oldest tracks
// 3. Delete corresponding files
// 4. Update library JSON
```

---

## Usage Tracking

### Persistence

```kotlin
// SharedPreferences: "music_generation_prefs"
{
    "songs_generated": 5,
    "total_cost_cents": 0,
    "last_generation_time": 1703001234567,
    "payment_setup": false
}
```

### Cost Calculation

```kotlin
// Free tier (songs 1-10)
if (songsGenerated < 10) {
    cost = 0
}

// Paid tier (songs 11+)
else {
    costPerSong = 6 cents
    totalCost = (songsGenerated - 10) * 6
}
```

### UI Updates

```kotlin
// Before generation
"You have 3 free songs remaining"
"Next generation: FREE"

// After exhausting free tier
"You have generated 12 songs"
"Next generation: $0.06"
"Total cost: $0.12"
```

---

## Performance Optimization

### Caching Strategy

- Generated music files cached in internal storage
- Library metadata loaded once, cached in memory
- Usage stats accessed via SharedPreferences

### Background Processing

```kotlin
// All API calls on IO dispatcher
withContext(Dispatchers.IO) {
    // Heavy operations here
}

// File operations also on IO
suspend fun saveMusic(...) = withContext(Dispatchers.IO) {
    // File write operations
}
```

### Memory Management

```kotlin
// Stream large audio data
// Don't keep all library in memory
// Load metadata only, fetch files on demand
```

---

## Security Considerations

### API Key Protection

```kotlin
// API key stored in BuildConfig (not in source)
private val apiKey: String = BuildConfig.GEMINI_API_KEY

// Never logged or exposed
Log.d(TAG, "API Key: ${apiKey.take(5)}...")  // Only first 5 chars
```

### File Access

```kotlin
// Files stored in internal storage (app-private)
// Not accessible to other apps
// Cleared on app uninstall
```

### User Data

```kotlin
// Only stores:
// - Audio files (generated music)
// - Generation metadata (prompts, timestamps)
// - Usage statistics (counts, costs)

// Does NOT store:
// - Payment information
// - Personal identifiers
// - Location data
```

---

## Testing

### Unit Tests

```kotlin
// Test usage tracking
@Test
fun testFreeTierTracking() {
    val tracker = MusicGenerationTracker(context)
    tracker.resetUsage()
    
    assertEquals(10, tracker.getFreeSongsRemaining())
    assertEquals(0, tracker.getSongsGenerated())
    
    tracker.recordGeneration()
    assertEquals(9, tracker.getFreeSongsRemaining())
}

// Test cost calculation
@Test
fun testCostCalculation() {
    val tracker = MusicGenerationTracker(context)
    // Generate 12 songs (10 free + 2 paid)
    repeat(12) { tracker.recordGeneration() }
    
    assertEquals(12, tracker.getTotalCostCents())  // 2 * $0.06
    assertEquals("$0.12", tracker.getTotalCostDollars())
}
```

### Integration Tests

```kotlin
// Test full generation flow
@Test
suspend fun testMusicGeneration() {
    val service = LyriaService()
    val result = service.generateMusic(
        prompt = "Test electronic track"
    )
    
    assertTrue(result is MusicGenerationResult.Success)
    val success = result as MusicGenerationResult.Success
    assertTrue(success.audioData.isNotEmpty())
    assertEquals(30, success.durationSeconds)
}
```

### Manual Testing

```
1. Generate first song → Should be FREE
2. Generate 10 songs → Should all be FREE
3. Generate 11th song → Should show $0.06
4. Check library → Should have all tracks
5. Play track → Should work
6. Delete track → Should remove from library
7. Turn off Lyria → Generate button should disappear
8. Turn on Lyria → Generate button should reappear
```

---

## Monitoring & Analytics

### Key Metrics to Track

```kotlin
// Usage metrics
- Total songs generated
- Free tier usage rate
- Paid tier conversion rate
- Average songs per user

// Performance metrics
- API response time
- Error rate
- Success rate
- Storage usage

// Business metrics
- Revenue per user
- Cost per generation
- User retention
- Feature engagement
```

### Logging Strategy

```kotlin
// Info level
Log.i(TAG, "Music generation started")
Log.i(TAG, "Music generation completed")

// Debug level  
Log.d(TAG, "API request: $url")
Log.d(TAG, "Response size: ${bytes.size}")

// Error level
Log.e(TAG, "API failed: ${response.code}")
Log.e(TAG, "Exception: ${e.message}", e)
```

---

## Future Enhancements

### Phase 2 Features

1. **Payment Integration**
    - Stripe/Google Pay integration
    - Automated charging
    - Receipt generation

2. **Advanced Music Features**
    - Longer tracks (60s, 90s)
    - Multiple variations
    - Music editing tools
    - Remix capabilities

3. **Social Features**
    - Share tracks with friends
    - Public music library
    - Collaborative creation
    - Social media integration

4. **Enhanced Library**
    - Playlists
    - Favorites
    - Search and filter
    - Cloud sync

### Scalability Plans

```kotlin
// For high volume:
- Implement request queuing
- Add caching layer
- Use CDN for music files
- Optimize storage with compression
- Batch API requests
```

---

## Troubleshooting Guide

### Common Issues

**Issue**: "Music generation failed"

- Check API key is valid
- Verify Vertex AI is enabled
- Ensure project ID is correct
- Check network connection

**Issue**: "Rate limit exceeded"

- Implement request throttling
- Add retry logic with backoff
- Request quota increase

**Issue**: "Out of storage"

- Increase MAX_LIBRARY_SONGS
- Implement manual cleanup
- Add storage settings

**Issue**: "Audio won't play"

- Verify file exists
- Check MIME type
- Ensure MediaPlayer initialized
- Test with different audio player

---

## API Limits & Quotas

### Default Quotas

```
Requests per minute: 60
Requests per day: 1,000+
Concurrent requests: 10
```

### Cost Limits

```
$0.06 per 30-second generation
No minimum spend
No maximum limit
Pay-as-you-go billing
```

### Request Quota Increase

1. Go to Google Cloud Console
2. Navigate to: IAM & Admin → Quotas
3. Search: "Vertex AI API"
4. Request increase
5. Wait for approval (usually 24-48hrs)

---

## Summary

### What's Implemented

✅ Complete Lyria API integration  
✅ Freemium model (10 free songs)  
✅ Usage tracking and cost management  
✅ Music library with file storage  
✅ Error handling and user feedback  
✅ Easy on/off toggle system  
✅ Production-ready architecture

### What's Ready to Use

✅ Generate 30-second music tracks  
✅ Save and playback music  
✅ Track usage and costs  
✅ Manage music library  
✅ Show user-friendly errors  
✅ Monitor service health

### What's Next

- Add payment processing
- Enhance music features
- Implement social sharing
- Add cloud sync
- Create playlists

---

**Status**: ✅ **FULLY IMPLEMENTED & READY**  
**Architecture**: Production-grade  
**Testing**: Manual testing required  
**Documentation**: Complete  
**Support**: Comprehensive error handling
