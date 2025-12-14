package com.sparkiai.app.network

import android.content.Context
import android.util.Log
import com.google.auth.oauth2.GoogleCredentials
import com.google.auth.oauth2.AccessToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.util.Collections

/**
 * Handles OAuth 2.0 authentication for Vertex AI
 * Required for Lyria music generation
 */
class VertexAIAuth(private val context: Context) {

    companion object {
        private const val TAG = "VertexAIAuth"
        private const val VERTEX_AI_SCOPE = "https://www.googleapis.com/auth/cloud-platform"

        // Service account JSON will be embedded in the app
        // This is a placeholder - you'll add your actual service account JSON
        private const val SERVICE_ACCOUNT_JSON_FILENAME = "vertex_ai_service_account.json"
    }

    private var cachedCredentials: GoogleCredentials? = null
    private var tokenExpirationTime: Long = 0

    /**
     * Get a valid access token for Vertex AI
     * Handles token refresh automatically
     */
    suspend fun getAccessToken(): String? = withContext(Dispatchers.IO) {
        try {
            // Check if we have cached credentials and token is still valid
            val currentTime = System.currentTimeMillis()
            if (cachedCredentials != null && currentTime < tokenExpirationTime) {
                val token = cachedCredentials?.accessToken?.tokenValue
                if (token != null) {
                    Log.d(TAG, "✅ Using cached access token")
                    return@withContext token
                }
            }

            // Load credentials from service account JSON
            val credentials = loadCredentials()
            if (credentials == null) {
                Log.e(TAG, "❌ Failed to load credentials")
                return@withContext null
            }

            // Refresh the token
            credentials.refreshIfExpired()
            cachedCredentials = credentials

            // Get the access token
            val accessToken = credentials.accessToken
            if (accessToken != null) {
                // Cache the token for 50 minutes (tokens valid for 1 hour)
                tokenExpirationTime = currentTime + (50 * 60 * 1000)

                Log.d(TAG, "✅ Access token obtained successfully")
                return@withContext accessToken.tokenValue
            }

            Log.e(TAG, "❌ No access token available")
            return@withContext null

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error getting access token: ${e.message}", e)
            return@withContext null
        }
    }

    /**
     * Load Google credentials from service account JSON
     */
    private fun loadCredentials(): GoogleCredentials? {
        return try {
            // Try to load from assets first
            val jsonStream = try {
                context.assets.open(SERVICE_ACCOUNT_JSON_FILENAME)
            } catch (e: Exception) {
                Log.w(TAG, "Service account JSON not found in assets, trying hardcoded")

                // Fallback: Use hardcoded service account JSON
                // You'll replace this with your actual service account JSON content
                val serviceAccountJson = getServiceAccountJson()
                if (serviceAccountJson.isBlank()) {
                    Log.e(TAG, "❌ No service account JSON configured")
                    return null
                }

                ByteArrayInputStream(serviceAccountJson.toByteArray())
            }

            val credentials = GoogleCredentials
                .fromStream(jsonStream)
                .createScoped(Collections.singleton(VERTEX_AI_SCOPE))

            Log.d(TAG, "✅ Credentials loaded successfully")
            credentials

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error loading credentials: ${e.message}", e)
            null
        }
    }

    /**
     * Get service account JSON content
     *
     * IMPORTANT: Replace this with your actual service account JSON
     *
     * To get your service account JSON:
     * 1. Go to: https://console.cloud.google.com/iam-admin/serviceaccounts
     * 2. Click "Create Service Account"
     * 3. Name: "lyria-music-generation"
     * 4. Grant role: "Vertex AI User"
     * 5. Click "Create Key" → JSON
     * 6. Copy the JSON content here
     */
    private fun getServiceAccountJson(): String {
        // TODO: Add your service account JSON here
        // For now, return empty - you'll add this in the setup guide
        return ""
    }

    /**
     * Check if authentication is configured
     */
    fun isConfigured(): Boolean {
        return try {
            // Try to load credentials
            val testStream = try {
                context.assets.open(SERVICE_ACCOUNT_JSON_FILENAME)
            } catch (e: Exception) {
                val json = getServiceAccountJson()
                if (json.isBlank()) return false
                ByteArrayInputStream(json.toByteArray())
            }

            testStream.close()
            true
        } catch (e: Exception) {
            false
        }
    }
}
