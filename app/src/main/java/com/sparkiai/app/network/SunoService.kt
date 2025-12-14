package com.sparkiai.app.network

import android.util.Log
import com.sparkiai.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Suno AI Music Generation Service
 * Generates high-quality music with vocals and instrumentals from text descriptions
 *
 * Suno is specifically designed for creating full songs with lyrics, vocals, and production.
 * It's one of the best AI music generation services available.
 *
 * Features:
 * - Full song generation (up to 2 minutes)
 * - Vocals with lyrics (or instrumental)
 * - Multiple genres and styles
 * - High-quality production
 * - Simple API
 *
 * Pricing:
 * - Free: 500 credits (~50 songs) to test
 * - Pro: $10/month for 2,500 credits
 * - Premier: $30/month for 10,000 credits
 *
 * API Docs: https://suno.ai/api/docs
 */
class SunoService {

    private val apiKey: String = BuildConfig.SUNO_API_KEY
    private val baseUrl: String = "https://api.suno.ai/v1"

    private val client = OkHttpClient.Builder()
        .connectTimeout(180, TimeUnit.SECONDS)  // Music generation takes time
        .readTimeout(180, TimeUnit.SECONDS)
        .writeTimeout(180, TimeUnit.SECONDS)
        .build()

    /**
     * Generate music from a text prompt using Suno AI
     *
     * @param prompt Text description of the music
     *               Example: "upbeat pop song about summer love, catchy chorus"
     * @param makeInstrumental If true, generates instrumental only (no vocals)
     * @param customLyrics Optional custom lyrics for the song
     * @return MusicGenerationResult containing the audio data or error
     */
    suspend fun generateMusic(
        prompt: String,
        makeInstrumental: Boolean = false,
        customLyrics: String? = null
    ): MusicGenerationResult = withContext(Dispatchers.IO) {
        try {
            if (apiKey.isBlank() || apiKey == "YOUR_SUNO_API_KEY") {
                return@withContext MusicGenerationResult.Error(
                    "Suno API key not configured. Sign up at https://suno.ai/api"
                )
            }

            Log.d("Suno", "🎵 Generating music with Suno AI")
            Log.d("Suno", "📝 Prompt: $prompt")
            Log.d("Suno", "🎹 Instrumental: $makeInstrumental")

            // Step 1: Create generation request
            val requestJson = JSONObject().apply {
                put("prompt", prompt)
                put("make_instrumental", makeInstrumental)
                if (!customLyrics.isNullOrBlank()) {
                    put("custom_lyrics", customLyrics)
                }
                put("wait_audio", false)  // We'll poll for completion
            }

            val createRequest = Request.Builder()
                .url("$baseUrl/generate")
                .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .build()

            Log.d("Suno", "📡 Creating generation request...")
            val createResponse = client.newCall(createRequest).execute()

            if (!createResponse.isSuccessful) {
                val errorBody = createResponse.body?.string()
                Log.e("Suno", "❌ Create request failed (${createResponse.code}): $errorBody")

                val errorMessage = parseErrorMessage(errorBody, createResponse.code)
                return@withContext MusicGenerationResult.Error(errorMessage)
            }

            val createResponseBody = createResponse.body?.string()
            val createJson = JSONObject(createResponseBody ?: "{}")

            // Get the generation IDs
            val clips = createJson.optJSONArray("clips")
            if (clips == null || clips.length() == 0) {
                return@withContext MusicGenerationResult.Error(
                    "No clips returned from Suno API"
                )
            }

            // Use the first clip
            val clipId = clips.getJSONObject(0).getString("id")
            Log.d("Suno", "✅ Generation started. Clip ID: $clipId")

            // Step 2: Poll for completion
            var attempts = 0
            val maxAttempts = 60  // 60 attempts * 5 seconds = 5 minutes max

            while (attempts < maxAttempts) {
                delay(5000)  // Wait 5 seconds between checks
                attempts++

                Log.d("Suno", "⏳ Checking status (attempt $attempts/$maxAttempts)...")

                val statusRequest = Request.Builder()
                    .url("$baseUrl/clips/$clipId")
                    .get()
                    .addHeader("Authorization", "Bearer $apiKey")
                    .build()

                val statusResponse = client.newCall(statusRequest).execute()

                if (!statusResponse.isSuccessful) {
                    Log.w("Suno", "⚠️ Status check failed, retrying...")
                    continue
                }

                val statusBody = statusResponse.body?.string()
                val statusJson = JSONObject(statusBody ?: "{}")

                val status = statusJson.optString("status", "")
                Log.d("Suno", "📊 Status: $status")

                when (status) {
                    "complete" -> {
                        // Get the audio URL
                        val audioUrl = statusJson.optString("audio_url", null)
                        if (audioUrl.isNullOrBlank()) {
                            return@withContext MusicGenerationResult.Error(
                                "Audio URL not found in response"
                            )
                        }

                        Log.d("Suno", "🎵 Audio ready! Downloading from: $audioUrl")

                        // Download the audio
                        val downloadRequest = Request.Builder()
                            .url(audioUrl)
                            .get()
                            .build()

                        val downloadResponse = client.newCall(downloadRequest).execute()

                        if (!downloadResponse.isSuccessful) {
                            return@withContext MusicGenerationResult.Error(
                                "Failed to download audio from Suno"
                            )
                        }

                        val audioBytes = downloadResponse.body?.bytes()

                        if (audioBytes == null || audioBytes.isEmpty()) {
                            return@withContext MusicGenerationResult.Error(
                                "Received empty audio from Suno"
                            )
                        }

                        // Get duration from metadata
                        val duration = statusJson.optInt("duration", 45)

                        Log.d(
                            "Suno",
                            "✅ Music downloaded! Size: ${audioBytes.size} bytes, Duration: ${duration}s"
                        )

                        return@withContext MusicGenerationResult.Success(
                            audioData = audioBytes,
                            mimeType = "audio/mpeg",  // Suno returns MP3
                            durationSeconds = duration,
                            prompt = prompt
                        )
                    }

                    "failed", "error" -> {
                        val errorMsg = statusJson.optString("error_message", "Generation failed")
                        Log.e("Suno", "❌ Generation failed: $errorMsg")
                        return@withContext MusicGenerationResult.Error(errorMsg)
                    }

                    "queued", "generating" -> {
                        // Still processing, continue polling
                        Log.d("Suno", "⏳ Still processing...")
                        continue
                    }

                    else -> {
                        Log.d("Suno", "❓ Unknown status: $status, continuing to poll...")
                        continue
                    }
                }
            }

            // Timeout
            Log.e("Suno", "⏱️ Timeout waiting for music generation")
            return@withContext MusicGenerationResult.Error(
                "Music generation timed out. Please try again."
            )

        } catch (e: Exception) {
            Log.e("Suno", "❌ Exception generating music: ${e.message}", e)
            return@withContext MusicGenerationResult.Error(
                "Error generating music: ${e.message}"
            )
        }
    }

    /**
     * Parse error message from API response
     */
    private fun parseErrorMessage(errorBody: String?, statusCode: Int): String {
        val jsonError = try {
            if (!errorBody.isNullOrBlank()) {
                val errorJson = JSONObject(errorBody)
                errorJson.optString("message", null)
                    ?: errorJson.optString("error", null)
                    ?: errorJson.optString("detail", null)
            } else null
        } catch (e: Exception) {
            null
        }

        return jsonError ?: when (statusCode) {
            400 -> "Invalid request. Try rephrasing your music description."
            401 -> "Authentication failed. Please check your Suno API key."
            402, 403 -> "Insufficient credits. Please upgrade your Suno plan or purchase more credits."
            429 -> "Rate limit exceeded. Please try again in a moment."
            500, 502, 503 -> "Suno server error. Please try again later."
            else -> "Music generation failed (${statusCode}). Please try again."
        }
    }

    /**
     * Check if Suno service is properly configured
     */
    fun isConfigured(): Boolean {
        return apiKey.isNotBlank() && apiKey != "YOUR_SUNO_API_KEY"
    }

    /**
     * Get the status of Suno service
     */
    fun getServiceStatus(): String {
        return when {
            apiKey.isBlank() || apiKey == "YOUR_SUNO_API_KEY" ->
                "Suno API key not configured. Sign up at https://suno.ai/api"

            else ->
                "Ready"
        }
    }
}
