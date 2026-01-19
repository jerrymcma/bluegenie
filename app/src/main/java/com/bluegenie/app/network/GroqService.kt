package com.bluegenie.app.network

import android.util.Log
import com.bluegenie.app.BuildConfig
import com.bluegenie.app.model.AIPersonality
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GroqService {

    private val apiKey: String = BuildConfig.GROQ_API_KEY
    private val baseUrl: String = "https://api.groq.com/openai/v1/chat/completions"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun generateResponse(
        userMessage: String,
        personality: AIPersonality,
        conversationContext: List<Pair<String, String>>
    ): String = withContext(Dispatchers.IO) {
        try {
            if (apiKey.isBlank()) {
                return@withContext "Error: Groq API key is not configured."
            }

            val messages = JSONArray()

            // Add the system prompt (personality)
            messages.put(JSONObject().apply {
                put("role", "system")
                put("content", personality.prompt)
            })

            // Add conversation context
            conversationContext.forEach { (user, model) ->
                messages.put(JSONObject().apply {
                    put("role", "user")
                    put("content", user)
                })
                messages.put(JSONObject().apply {
                    put("role", "assistant")
                    put("content", model)
                })
            }

            // Add the current user message
            messages.put(JSONObject().apply {
                put("role", "user")
                put("content", userMessage)
            })

            val requestBody = JSONObject().apply {
                put("messages", messages)
                put("model", personality.model) // Use the model from the personality
            }.toString()

            val request = Request.Builder()
                .url(baseUrl)
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful && responseBody != null) {
                val jsonResponse = JSONObject(responseBody)
                val choices = jsonResponse.getJSONArray("choices")
                if (choices.length() > 0) {
                    val firstChoice = choices.getJSONObject(0)
                    val message = firstChoice.getJSONObject("message")
                    return@withContext message.getString("content")
                } else {
                    return@withContext "Error: No response from Groq."
                }
            } else {
                Log.e("GroqService", "API Error: ${response.code} - $responseBody")
                return@withContext "Error: Could not get response from Groq. Code: ${response.code}"
            }

        } catch (e: Exception) {
            Log.e("GroqService", "Exception in generateResponse", e)
            return@withContext "Error: An exception occurred - ${e.message}"
        }
    }
}
