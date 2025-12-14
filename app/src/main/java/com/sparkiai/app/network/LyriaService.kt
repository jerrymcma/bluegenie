package com.sparkiai.app.network

import android.util.Log
import com.sparkiai.app.BuildConfig
import com.sparkiai.app.config.FeatureFlags
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import android.util.Base64

/**
 * Lyria Music Generation Service
 * Generates actual music files from text descriptions
 *
 * Lyria is Google's music generation model available through Vertex AI
 * Cost: ~$0.06 per 30-second track
 */
class LyriaService(private var vertexAuth: VertexAIAuth? = null) {

    // Project configuration for Vertex AI Lyria endpoint
    private val projectId: String = FeatureFlags.LyriaConfig.PROJECT_ID
    private val location: String = FeatureFlags.LyriaConfig.LOCATION
    private val modelName: String = FeatureFlags.LyriaConfig.MODEL_NAME

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)  // Longer timeout for music generation
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Initialize with context for OAuth authentication
     */
    fun initialize(context: android.content.Context) {
        // No-op for API key based auth; keep method for backwards compatibility
        if (vertexAuth == null) {
            vertexAuth = VertexAIAuth(context)
        }
    }

    /**
     * Generate music from a text prompt
     *
     * @param prompt Text description of the music to generate
     *               Example: "An upbeat electronic dance track with energetic beats"
     * @param negativePrompt Optional - things to avoid in the music
     *                       Example: "vocals, slow tempo"
     * @param seed Optional - for reproducible generation
     * @return MusicGenerationResult containing the audio data or error
     */
    suspend fun generateMusic(
        prompt: String,
        negativePrompt: String? = null,
        seed: Int? = null
    ): MusicGenerationResult = withContext(Dispatchers.IO) {
        try {
            if (!FeatureFlags.ENABLE_LYRIA_MUSIC_GENERATION) {
                return@withContext MusicGenerationResult.Error(
                    "Music generation is currently disabled. Enable it in FeatureFlags."
                )
            }

            Log.d("LyriaService", "🎵 Generating music with prompt: $prompt")
            if (!negativePrompt.isNullOrBlank()) {
                Log.d("LyriaService", "🚫 Negative prompt: $negativePrompt")
            }

            // Build the request body for Lyria API
            val requestBody = JSONObject().apply {
                put("instances", JSONArray().apply {
                    put(JSONObject().apply {
                        put("prompt", prompt)
                        if (!negativePrompt.isNullOrBlank()) {
                            put("negative_prompt", negativePrompt)
                        }
                        if (seed != null) {
                            put("seed", seed)
                        }
                    })
                })
                put("parameters", JSONObject().apply {
                    // Parameters can be added here if needed
                    // For now, using defaults
                })
            }

            // Get OAuth2 access token for Vertex AI
            val accessToken = vertexAuth?.getAccessToken()
            if (accessToken.isNullOrBlank()) {
                Log.e("LyriaService", "❌ Missing OAuth access token for Vertex AI")
                return@withContext MusicGenerationResult.Error(
                    "Authentication for music generation is not configured. " +
                            "Please complete the Lyria OAuth setup (service account)."
                )
            }

            // Vertex AI endpoint for Lyria (requires OAuth access token, not API key)
            val url = "https://$location-aiplatform.googleapis.com/v1/projects/$projectId" +
                    "/locations/$location/publishers/google/models/$modelName:predict"

            Log.d("LyriaService", "📡 Calling Lyria API: $url")

            val request = Request.Builder()
                .url(url)
                .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer $accessToken")
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful && responseBody != null) {
                Log.d("LyriaService", "✅ Lyria API call successful (${response.code})")
                Log.d("LyriaService", "📄 Response preview: ${responseBody.take(200)}...")

                val jsonResponse = JSONObject(responseBody)
                Log.d(
                    "LyriaService",
                    "🔑 Response has keys: ${jsonResponse.keys().asSequence().toList()}"
                )

                val predictions = jsonResponse.optJSONArray("predictions")

                if (predictions != null && predictions.length() > 0) {
                    Log.d("LyriaService", "📦 Found ${predictions.length()} prediction(s)")

                    val firstPrediction = predictions.getJSONObject(0)
                    Log.d(
                        "LyriaService",
                        "🔑 Prediction has keys: ${firstPrediction.keys().asSequence().toList()}"
                    )

                    // Try multiple possible field names for audio content
                    var audioContent = firstPrediction.optString("audioContent")
                    if (audioContent.isBlank()) {
                        audioContent = firstPrediction.optString("bytesBase64Encoded")
                        Log.d(
                            "LyriaService",
                            "📝 Using bytesBase64Encoded field instead of audioContent"
                        )
                    }
                    if (audioContent.isBlank()) {
                        audioContent = firstPrediction.optString("bytes")
                        Log.d("LyriaService", "📝 Using bytes field")
                    }

                    val mimeType = firstPrediction.optString("mimeType", "audio/wav")

                    Log.d("LyriaService", "🎵 Audio data length: ${audioContent.length}")
                    Log.d("LyriaService", "🎵 mimeType: $mimeType")

                    if (audioContent.isNotBlank()) {
                        Log.d("LyriaService", "🎵 Decoding base64 audio...")

                        // Decode base64 audio data
                        val audioBytes = Base64.decode(audioContent, Base64.DEFAULT)

                        Log.d(
                            "LyriaService",
                            "✅ Music generated successfully! Size: ${audioBytes.size} bytes"
                        )

                        return@withContext MusicGenerationResult.Success(
                            audioData = audioBytes,
                            mimeType = mimeType,
                            durationSeconds = FeatureFlags.LyriaConfig.DURATION_SECONDS,
                            prompt = prompt
                        )
                    } else {
                        Log.e("LyriaService", "❌ audioContent is blank or null")
                    }
                } else {
                    Log.e("LyriaService", "❌ No predictions array found")
                    Log.e("LyriaService", "📄 Full response: $responseBody")
                }

                Log.e("LyriaService", "❌ Failed to extract audio from response")
                return@withContext MusicGenerationResult.Error(
                    "Failed to extract audio from response. Check logs for details."
                )

            } else {
                val errorMessage = "Lyria API failed (${response.code}): $responseBody"
                Log.e("LyriaService", "❌ $errorMessage")

                // Log the full error for debugging
                Log.e("LyriaService", "📄 Full error response: $responseBody")
                Log.e("LyriaService", "🎵 Prompt that failed: $prompt")

                // Parse error for user-friendly message
                val userMessage = when (response.code) {
                    400 -> {
                        // Check if it's a safety filter issue
                        if (responseBody?.contains("flagged", ignoreCase = true) == true) {
                            "The prompt was flagged by safety filters. Try a different description or simplify your request."
                        } else {
                            "Invalid request. Try rephrasing your music description."
                        }
                    }
                    401, 403 -> "Authentication failed. Please check your Lyria OAuth setup."
                    404 -> "Lyria model not found. Ensure Vertex AI is enabled in your project."
                    429 -> "Rate limit exceeded. Please try again in a moment."
                    500, 502, 503 -> "Server error. Please try again later."
                    else -> "Music generation failed. Please try again."
                }

                return@withContext MusicGenerationResult.Error(userMessage)
            }

        } catch (e: Exception) {
            Log.e("LyriaService", "❌ Exception generating music: ${e.message}", e)
            return@withContext MusicGenerationResult.Error(
                "Error generating music: ${e.message}"
            )
        }
    }

    /**
     * Check if Lyria service is properly configured
     */
    fun isConfigured(): Boolean {
        return FeatureFlags.ENABLE_LYRIA_MUSIC_GENERATION &&
                projectId.isNotBlank() &&
                projectId != "your-project-id"
    }

    /**
     * Get the status of Lyria service
     */
    fun getServiceStatus(): LyriaServiceStatus {
        return when {
            !FeatureFlags.ENABLE_LYRIA_MUSIC_GENERATION ->
                LyriaServiceStatus.Disabled("Music generation is disabled in settings")

            projectId.isBlank() || projectId == "your-project-id" ->
                LyriaServiceStatus.NotConfigured("Project ID not configured")

            else ->
                LyriaServiceStatus.Ready
        }
    }
}

/**
 * Result of a music generation request
 */
sealed class MusicGenerationResult {
    data class Success(
        val audioData: ByteArray,
        val mimeType: String,
        val durationSeconds: Int,
        val prompt: String
    ) : MusicGenerationResult() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Success

            if (!audioData.contentEquals(other.audioData)) return false
            if (mimeType != other.mimeType) return false
            if (durationSeconds != other.durationSeconds) return false
            if (prompt != other.prompt) return false

            return true
        }

        override fun hashCode(): Int {
            var result = audioData.contentHashCode()
            result = 31 * result + mimeType.hashCode()
            result = 31 * result + durationSeconds
            result = 31 * result + prompt.hashCode()
            return result
        }
    }

    data class Error(val message: String) : MusicGenerationResult()
}

/**
 * Status of Lyria service configuration
 */
sealed class LyriaServiceStatus {
    object Ready : LyriaServiceStatus()
    data class Disabled(val reason: String) : LyriaServiceStatus()
    data class NotConfigured(val reason: String) : LyriaServiceStatus()
}
