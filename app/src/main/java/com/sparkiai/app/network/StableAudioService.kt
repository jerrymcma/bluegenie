package com.sparkiai.app.network

import android.util.Log
import com.sparkiai.app.BuildConfig
import com.sparkiai.app.config.FeatureFlags
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Stable Audio Music Generation Service (Stability AI)
 * Generates high-quality music from text descriptions using Stable Audio 2.0
 *
 * Stable Audio is Stability AI's official music generation model that creates
 * professional-quality audio including vocals and instrumentals.
 *
 * Features:
 * - High-quality audio generation (30-90 seconds)
 * - Vocals and instrumentals supported
 * - Multiple genres and styles
 * - Commercial use allowed
 * - Minimal content filtering
 * - Official API with documentation
 *
 * Cost: ~$0.10-0.20 per generation
 * API Docs: https://platform.stability.ai/docs/api-reference#tag/Audio/operation/audioGeneration
 */
class StableAudioService {

    // Read API key from local.properties via BuildConfig
    private val apiKey: String = "sk-VUX2uDWrzUHjlQAE2ZjfBbXFdiFwUA3Pqw7QsleQFJmuHOuI"
    private val baseUrl: String = "https://api.stability.ai"

    private val client = OkHttpClient.Builder()
        .connectTimeout(120, TimeUnit.SECONDS)  // Longer timeout for music generation
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    /**
     * Generate music from a text prompt using Stable Audio
     *
     * @param prompt Text description of the music
     *               Example: "upbeat comedic song with funky groove, playful vocals, saxophone"
     * @param durationSeconds Length of the audio (30-90 seconds)
     * @return MusicGenerationResult containing the audio data or error
     */
    suspend fun generateMusic(
        prompt: String,
        durationSeconds: Int = 45  // Default 45 seconds
    ): MusicGenerationResult = withContext(Dispatchers.IO) {
        try {
            if (!FeatureFlags.ENABLE_LYRIA_MUSIC_GENERATION) {
                return@withContext MusicGenerationResult.Error(
                    "Music generation is currently disabled. Enable it in FeatureFlags."
                )
            }

            if (apiKey.isBlank() || apiKey == "your-stability-api-key-here") {
                return@withContext MusicGenerationResult.Error(
                    "Stability AI API key not configured. Please add your API key."
                )
            }

            // Clamp duration to valid range
            val validDuration = durationSeconds.coerceIn(30, 90)

            Log.d("StableAudio", "🎵 Generating music with Stable Audio 2.0")
            Log.d("StableAudio", "📝 Prompt: $prompt")
            Log.d("StableAudio", "⏱️ Duration: $validDuration seconds")

            // Build multipart form data request for Stable Audio 2.0
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("prompt", prompt)
                .addFormDataPart("output_format", "mp3")  // Options: mp3, wav
                .addFormDataPart("duration", validDuration.toString())
                .build()

            // Correct endpoint for Stable Audio 2.0
            val url = "$baseUrl/v2beta/audio/stable-audio-2/text-to-audio"
            Log.d("StableAudio", "📡 Calling Stable Audio 2.0 API: $url")

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Accept", "audio/*")  // Expect audio response
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val audioBytes = response.body?.bytes()

                if (audioBytes != null && audioBytes.isNotEmpty()) {
                    Log.d(
                        "StableAudio",
                        "✅ Music generated successfully! Size: ${audioBytes.size} bytes"
                    )

                    return@withContext MusicGenerationResult.Success(
                        audioData = audioBytes,
                        mimeType = "audio/mpeg",  // MP3 format
                        durationSeconds = validDuration,
                        prompt = prompt
                    )
                } else {
                    Log.e("StableAudio", "❌ Empty audio response")
                    return@withContext MusicGenerationResult.Error(
                        "Received empty audio from Stable Audio API"
                    )
                }
            } else {
                val errorBody = response.body?.string()
                val errorMessage = "Stable Audio API failed (${response.code}): $errorBody"
                Log.e("StableAudio", "❌ $errorMessage")

                // Try to parse error JSON
                val userMessage = try {
                    if (!errorBody.isNullOrBlank()) {
                        val errorJson = JSONObject(errorBody)
                        errorJson.optString("message", null) ?: errorJson.optString("error", null)
                    } else null
                } catch (e: Exception) {
                    null
                }

                // Fallback to status code-based messages
                val finalMessage = userMessage ?: when (response.code) {
                    400 -> "Invalid request. Try rephrasing your music description."
                    401 -> "Authentication failed. Please check your Stability AI API key."
                    402, 403 -> "Insufficient credits. Please check your Stability AI account balance."
                    429 -> "Rate limit exceeded. Please try again in a moment."
                    500, 502, 503 -> "Stability AI server error. Please try again later."
                    else -> "Music generation failed (${response.code}). Please try again."
                }

                return@withContext MusicGenerationResult.Error(finalMessage)
            }

        } catch (e: Exception) {
            Log.e("StableAudio", "❌ Exception generating music: ${e.message}", e)
            return@withContext MusicGenerationResult.Error(
                "Error generating music: ${e.message}"
            )
        }
    }

    /**
     * Check if Stable Audio service is properly configured
     */
    fun isConfigured(): Boolean {
        return FeatureFlags.ENABLE_LYRIA_MUSIC_GENERATION &&
                apiKey.isNotBlank() &&
                apiKey != "your-stability-api-key-here"
    }

    /**
     * Get the status of Stable Audio service
     */
    fun getServiceStatus(): String {
        return when {
            !FeatureFlags.ENABLE_LYRIA_MUSIC_GENERATION ->
                "Music generation is disabled in settings"

            apiKey.isBlank() || apiKey == "your-stability-api-key-here" ->
                "Stability AI API key not configured"

            else ->
                "Ready"
        }
    }
}
