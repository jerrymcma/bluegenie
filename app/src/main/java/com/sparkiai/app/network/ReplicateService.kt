package com.sparkiai.app.network

import android.util.Log
import com.sparkiai.app.BuildConfig
import com.sparkiai.app.config.FeatureFlags
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Replicate Music Generation Service
 * Generates music using Minimax Music 1.5 model via Replicate API
 *
 * Replicate is a platform for running ML models via API.
 * Minimax Music 1.5 is a powerful music generation model that creates full songs with vocals.
 *
 * Features:
 * - High-quality music generation with vocals and lyrics
 * - Full song generation (up to ~3 minutes)
 * - Pay-as-you-go pricing (~$0.015 per generation)
 * - No monthly subscription required
 * - Simple REST API
 * - Supports custom lyrics
 *
 * Cost: ~$0.015 per generation (pay only for what you use)
 * API Docs: https://replicate.com/minimax/music-1.5
 *
 * Setup:
 * 1. Sign up at https://replicate.com
 * 2. Get your API token from https://replicate.com/account/api-tokens
 * 3. Add REPLICATE_API_KEY to local.properties
 */
class ReplicateService {

    // Read API key from local.properties via BuildConfig
    private val apiKey: String = BuildConfig.REPLICATE_API_KEY
    private val baseUrl: String = "https://api.replicate.com/v1"

    // Using Minimax Music-1.5 - generates full songs with vocals from lyrics
    private val modelPath = "minimax/music-1.5"

    init {
        Log.d("Replicate", "🔑 API Key loaded: ${apiKey.take(20)}... (length: ${apiKey.length})")
        Log.d("Replicate", "🔑 Full API Key: $apiKey")
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(120, TimeUnit.SECONDS)  // Longer timeout for music generation
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    /**
     * Generate music from a text prompt using Minimax Music 1.5 via Replicate
     *
     * @param prompt Text description of the music style/genre
     *               Example: "Jazz, Smooth Jazz, Romantic, Dreamy"
     * @param durationSeconds Not used for Minimax (generates full songs)
     * @param temperature Not used for Minimax
     * @param topK Not used for Minimax
     * @param topP Not used for Minimax
     * @param model Not used for Minimax
     * @return MusicGenerationResult containing the audio data or error
     */
    suspend fun generateMusic(
        prompt: String,
        durationSeconds: Int = 30,
        temperature: Float = 1.0f,
        topK: Int = 250,
        topP: Float = 0.0f,
        model: String = "melody"
    ): MusicGenerationResult = withContext(Dispatchers.IO) {
        try {
            if (!FeatureFlags.ENABLE_LYRIA_MUSIC_GENERATION) {
                return@withContext MusicGenerationResult.Error(
                    "Music generation is currently disabled. Enable it in FeatureFlags."
                )
            }

            if (apiKey.isBlank() || apiKey == "your-replicate-api-key-here") {
                return@withContext MusicGenerationResult.Error(
                    "Replicate API key not configured. Get one at https://replicate.com/account/api-tokens"
                )
            }

            Log.d("Replicate", "🎵 Generating music with Minimax Music 1.5 via Replicate")
            Log.d("Replicate", "📝 Prompt: $prompt")

            // Step 1: Create a prediction (lyrics will be auto-generated from prompt)
            val predictionId = createPrediction(prompt = prompt, lyrics = null)

            if (predictionId == null) {
                return@withContext MusicGenerationResult.Error(
                    "Failed to start music generation. Please try again."
                )
            }

            Log.d("Replicate", "🆔 Prediction ID: $predictionId")
            Log.d("Replicate", "⏳ Waiting for music generation to complete...")

            // Step 2: Poll for completion (with timeout)
            val maxAttempts = 60  // 60 attempts * 2 seconds = 2 minute timeout
            var attempts = 0

            while (attempts < maxAttempts) {
                delay(2000)  // Wait 2 seconds between polls
                attempts++

                val result = checkPrediction(predictionId)

                when (result) {
                    is PredictionResult.Completed -> {
                        Log.d("Replicate", "✅ Music generation completed!")

                        // Download the audio file
                        val audioBytes = downloadAudio(result.outputUrl)

                        if (audioBytes != null) {
                            Log.d("Replicate", "✅ Audio downloaded: ${audioBytes.size} bytes")
                            return@withContext MusicGenerationResult.Success(
                                audioData = audioBytes,
                                mimeType = "audio/mpeg",  // MP3 format from Minimax
                                durationSeconds = 30,  // Typical duration for generated songs
                                prompt = prompt
                            )
                        } else {
                            return@withContext MusicGenerationResult.Error(
                                "Failed to download generated audio"
                            )
                        }
                    }

                    is PredictionResult.Failed -> {
                        Log.e("Replicate", "❌ Prediction failed: ${result.error}")
                        return@withContext MusicGenerationResult.Error(
                            "Music generation failed: ${result.error}"
                        )
                    }

                    is PredictionResult.Processing -> {
                        Log.d("Replicate", "⏳ Still processing... (attempt $attempts/$maxAttempts)")
                        // Continue polling
                    }

                    is PredictionResult.Error -> {
                        return@withContext MusicGenerationResult.Error(result.message)
                    }
                }
            }

            // Timeout
            return@withContext MusicGenerationResult.Error(
                "Music generation timed out after ${maxAttempts * 2} seconds. Please try again."
            )

        } catch (e: Exception) {
            Log.e("Replicate", "❌ Exception generating music: ${e.message}", e)
            return@withContext MusicGenerationResult.Error(
                "Error generating music: ${e.message}"
            )
        }
    }

    /**
     * Create a prediction (start music generation)
     * @return Prediction ID if successful, null otherwise
     */
    private fun createPrediction(
        prompt: String,
        lyrics: String?
    ): String? {
        try {
            // For Minimax Music-1.5: 
            // - "lyrics" field is REQUIRED (the actual song lyrics) - MAX 600 CHARACTERS
            // - "prompt" field describes the music style/genre

            // Parse input: format is "lyrics|style" or just "lyrics" or just "style"
            val parts = prompt.trim().split("|", limit = 2)
            var userLyrics = parts.getOrNull(0)?.trim() ?: ""
            var musicStyle = parts.getOrNull(1)?.trim() ?: ""

            // If only one part provided, determine if it's lyrics or style
            if (parts.size == 1) {
                val singleInput = parts[0].trim()
                val hasMultipleLines = singleInput.lines().size >= 4
                val hasLyricStructure = singleInput.contains("[Verse]", ignoreCase = true) ||
                        singleInput.contains("[Chorus]", ignoreCase = true) ||
                        singleInput.contains("[Bridge]", ignoreCase = true)
                val hasNewlines = singleInput.contains("\n")
                val isLongText = singleInput.length > 100

                // If it looks like lyrics, use as lyrics
                if (hasLyricStructure || (hasMultipleLines && hasNewlines) || isLongText) {
                    Log.d("Replicate", "🎤 Detected LYRICS")
                    userLyrics = singleInput
                    musicStyle = ""
                } else {
                    // Otherwise treat as style description
                    Log.d("Replicate", "🎵 Detected STYLE description")
                    userLyrics = ""
                    musicStyle = singleInput
                }
            }

            val input = JSONObject().apply {
                // Use provided lyrics or placeholder
                val finalLyrics = if (userLyrics.isNotBlank()) {
                    // Minimax has a 600 character limit for lyrics
                    if (userLyrics.length > 600) {
                        Log.w("Replicate", "⚠️ Lyrics exceed 600 char limit, truncating...")
                        userLyrics.take(600)
                    } else {
                        userLyrics
                    }
                } else {
                    // Use placeholder lyrics if no user lyrics provided
                    """[Verse]
La la la la la
Oh oh oh oh oh
Yeah yeah yeah yeah

[Chorus]
La la la la la
Oh oh oh oh oh
Yeah yeah yeah yeah

[Verse]
La la la la la
Oh oh oh oh oh
Yeah yeah yeah yeah

[Chorus]
La la la la la
Oh oh oh oh oh
Yeah yeah yeah yeah"""
                }

                put("lyrics", finalLyrics)

                // Use provided style or default
                val finalStyle = if (musicStyle.isNotBlank()) {
                    musicStyle
                } else {
                    "Pop, catchy melody, modern production"  // Default style
                }
                put("prompt", finalStyle)
            }

            val requestBody = JSONObject().apply {
                put("input", input)
            }

            Log.d("Replicate", "📤 Creating prediction with Minimax Music-01")
            Log.d("Replicate", "📝 Request body: ${requestBody.toString()}")
            Log.d(
                "Replicate",
                "🔑 Auth Header: Bearer ${apiKey.take(20)}... (full key length: ${apiKey.length})")

            val request = Request.Builder()
                .url("$baseUrl/models/$modelPath/predictions")
                .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "wait")  // Wait for completion
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            Log.d("Replicate", "📤 Request URL: ${request.url}")
            Log.d("Replicate", "📤 Response Code: ${response.code}")
            Log.d("Replicate", "📤 Response Headers: ${response.headers}")

            if (response.isSuccessful && responseBody != null) {
                val json = JSONObject(responseBody)
                return json.optString("id")
            } else {
                Log.e(
                    "Replicate",
                    "❌ Failed to create prediction: ${response.code} - $responseBody"
                )
                return null
            }

        } catch (e: Exception) {
            Log.e("Replicate", "❌ Exception creating prediction: ${e.message}", e)
            return null
        }
    }

    /**
     * Check the status of a prediction
     */
    private fun checkPrediction(predictionId: String): PredictionResult {
        try {
            val request = Request.Builder()
                .url("$baseUrl/predictions/$predictionId")
                .get()
                .addHeader("Authorization", "Bearer $apiKey")
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful && responseBody != null) {
                val json = JSONObject(responseBody)
                val status = json.optString("status")

                return when (status) {
                    "succeeded" -> {
                        // Output can be a string URL or an array of URLs
                        val output = json.opt("output")
                        val outputUrl = when {
                            output is String -> output
                            output is org.json.JSONArray && output.length() > 0 -> output.getString(
                                0
                            )

                            else -> null
                        }

                        if (outputUrl != null) {
                            PredictionResult.Completed(outputUrl)
                        } else {
                            PredictionResult.Failed("No output URL in response")
                        }
                    }

                    "failed", "canceled" -> {
                        val error = json.optString("error", "Unknown error")
                        PredictionResult.Failed(error)
                    }

                    "starting", "processing" -> {
                        PredictionResult.Processing
                    }

                    else -> {
                        PredictionResult.Processing  // Unknown status, keep waiting
                    }
                }
            } else {
                Log.e("Replicate", "❌ Failed to check prediction: ${response.code} - $responseBody")
                return PredictionResult.Error("Failed to check prediction status: ${response.code}")
            }

        } catch (e: Exception) {
            Log.e("Replicate", "❌ Exception checking prediction: ${e.message}", e)
            return PredictionResult.Error("Error checking prediction: ${e.message}")
        }
    }

    /**
     * Download audio from URL
     */
    private fun downloadAudio(url: String): ByteArray? {
        try {
            Log.d("Replicate", "⬇️ Downloading audio from: $url")

            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                return response.body?.bytes()
            } else {
                Log.e("Replicate", "❌ Failed to download audio: ${response.code}")
                return null
            }

        } catch (e: Exception) {
            Log.e("Replicate", "❌ Exception downloading audio: ${e.message}", e)
            return null
        }
    }

    /**
     * Check if Replicate service is properly configured
     */
    fun isConfigured(): Boolean {
        return FeatureFlags.ENABLE_LYRIA_MUSIC_GENERATION &&
                apiKey.isNotBlank() &&
                apiKey != "your-replicate-api-key-here"
    }

    /**
     * Get the status of Replicate service
     */
    fun getServiceStatus(): String {
        return when {
            !FeatureFlags.ENABLE_LYRIA_MUSIC_GENERATION ->
                "Music generation is disabled in settings"

            apiKey.isBlank() || apiKey == "your-replicate-api-key-here" ->
                "Replicate API key not configured. Get one at https://replicate.com/account/api-tokens"

            else ->
                "Ready"
        }
    }
}

/**
 * Internal result type for prediction status
 */
private sealed class PredictionResult {
    data class Completed(val outputUrl: String) : PredictionResult()
    data class Failed(val error: String) : PredictionResult()
    object Processing : PredictionResult()
    data class Error(val message: String) : PredictionResult()
}
