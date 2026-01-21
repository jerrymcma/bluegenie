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
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPInputStream

class GroqService {

    private val apiKey: String = BuildConfig.GROQ_API_KEY
    private val baseUrl: String = "https://api.groq.com/openai/v1/chat/completions"
    private val visionModel: String = "meta-llama/llama-4-maverick-17b-128e-instruct"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private fun readResponseBody(response: Response): String? {
        val bodyBytes = response.body?.bytes() ?: return null
        val shouldGunzip = response.header("Content-Encoding")?.contains("gzip", ignoreCase = true) == true
        val rawStream = if (shouldGunzip) {
            GZIPInputStream(ByteArrayInputStream(bodyBytes))
        } else {
            ByteArrayInputStream(bodyBytes)
        }
        return rawStream.readBytes().toString(Charsets.UTF_8)
    }

    suspend fun performBraveWebSearch(query: String): String = withContext(Dispatchers.IO) {
        try {
            val apiKey = BuildConfig.BRAVE_API_KEY
            if (apiKey.isBlank()) {
                return@withContext "Search error: Brave Web Search API key is not configured."
            }

            val trimmedQuery = query.trim()
            val url = "https://api.search.brave.com/res/v1/web/search?q=${java.net.URLEncoder.encode(trimmedQuery, "UTF-8")}&count=5"

            Log.d("GroqService", "Performing Brave web search: $trimmedQuery")

            val request = Request.Builder()
                .url(url)
                .get()
                .addHeader("Accept", "application/json")
                .addHeader("X-Subscription-Token", apiKey)
                .build()
            val response = client.newCall(request).execute()
            val responseBody = readResponseBody(response)
            Log.d("GroqService", "Brave web search response code: ${response.code}, length: ${responseBody?.length ?: 0}")

            if (response.isSuccessful && responseBody != null) {
                Log.d("GroqService", "Web search response received: ${responseBody.take(300)}")
                val json = JSONObject(responseBody)
                val webResults = json.optJSONObject("web")?.optJSONArray("results")
                
                if (webResults != null && webResults.length() > 0) {
                    val results = StringBuilder()
                    for (i in 0 until minOf(webResults.length(), 5)) {
                        val result = webResults.getJSONObject(i)
                        val title = result.optString("title", "")
                        val description = result.optString("description", "")
                        val url = result.optString("url", "")
                        
                        if (title.isNotBlank() && description.isNotBlank()) {
                            results.append("${i + 1}. $title\n")
                            results.append("   $description\n")
                            results.append("   URL: $url\n\n")
                        }
                    }
                    
                    val resultText = results.toString().trim()
                    Log.d("GroqService", "Web search results: ${resultText.take(200)}")
                    return@withContext resultText.ifBlank { "No web search results found." }
                }

                Log.d("GroqService", "No web search results found.")
                return@withContext "No web search results found."
            } else {
                Log.e("GroqService", "Web search failed with code: ${response.code}")
                return@withContext "Web search failed: ${response.code}"
            }
        } catch (e: Exception) {
            Log.e("GroqService", "Web search error: ${e.message}", e)
            return@withContext "Web search error: ${e.message}"
        }
    }

    suspend fun performWebSearch(query: String): String = withContext(Dispatchers.IO) {
        try {
            val apiKey = BuildConfig.BRAVE_GROUNDING_API_KEY
            if (apiKey.isBlank()) {
                return@withContext "Search error: Brave Grounding API key is not configured."
            }

            val trimmedQuery = query.trim()
            val url = "https://api.search.brave.com/res/v1/chat/completions"
            val requestBody = JSONObject().apply {
                put("model", "brave")
                put("stream", false)
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", trimmedQuery)
                    })
                })
            }.toString()

            Log.d("GroqService", "Performing Brave grounding: $trimmedQuery")

            val request = Request.Builder()
                .url(url)
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .addHeader("Accept", "application/json")
                .addHeader("X-Subscription-Token", apiKey)
                .build()
            val response = client.newCall(request).execute()
            val responseBody = readResponseBody(response)
            Log.d("GroqService", "Brave grounding response code: ${response.code}, length: ${responseBody?.length ?: 0}")

            if (response.isSuccessful && responseBody != null) {
                Log.d("GroqService", "Grounding response received: ${responseBody.take(300)}")
                val json = JSONObject(responseBody)
                val choices = json.optJSONArray("choices")
                if (choices != null && choices.length() > 0) {
                    val message = choices.getJSONObject(0).optJSONObject("message")
                    val content = message?.optString("content", "") ?: ""
                    Log.d("GroqService", "Grounded answer: ${content.take(200)}")
                    return@withContext content.ifBlank { "No direct answer found." }
                }

                Log.d("GroqService", "Grounded answer: No direct answer found.")
                return@withContext "No direct answer found."
            } else {
                Log.e("GroqService", "Grounding failed with code: ${response.code}")
                return@withContext "Search failed: ${response.code}"
            }
        } catch (e: Exception) {
            Log.e("GroqService", "Grounding error: ${e.message}", e)
            return@withContext "Search error: ${e.message}"
        }
    }

    private fun extractQueryFromMalformedCall(content: String): String? {
        // Pattern 1: Standard JSON format with "query" field
        val queryPattern1 = """"query"\s*:\s*"([^"]+)"""".toRegex()
        queryPattern1.find(content)?.let {
            return it.groupValues[1]
        }
        
        // Pattern 2: Relaxed JSON with single quotes or no quotes
        val queryPattern2 = """['"]?query['"]?\s*:\s*['"]([^'"]+)['"]""".toRegex()
        queryPattern2.find(content)?.let {
            return it.groupValues[1]
        }
        
        // Pattern 3: Capture anything that looks like a query in malformed syntax
        val queryPattern3 = """query['":]?\s*[=:]\s*['"]([^'"]+)['"]""".toRegex()
        queryPattern3.find(content)?.let {
            return it.groupValues[1]
        }
        
        return null
    }

    suspend fun generateResponse(
        userMessage: String,
        personality: AIPersonality,
        conversationContext: List<Pair<String, String>>,
        imageBase64: String? = null
    ): String = withContext(Dispatchers.IO) {
        try {
            if (apiKey.isBlank()) {
                return@withContext "Error: Groq API key is not configured."
            }

            val messages = JSONArray()

            // Add the system prompt (personality) with identity and grounding instructions
            val currentDate = java.text.SimpleDateFormat("MMMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date())
            val systemPrompt = """
                You are ${personality.name}, a ${personality.description}.
                Use this response style: ${personality.responseStyle}.
                
                📅 CRITICAL DATE CONTEXT:
                Current Date: $currentDate
                Your training data cutoff: April 2024 (OUTDATED)
                You are now in ${java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault()).format(java.util.Date())}
                
                🚨 CRITICAL GROUNDING RULES 🚨
                1. Your training data about current events, officials, and recent news is OUTDATED and WRONG
                2. NEVER answer questions about current presidents, officials, or events using your training data
                3. If you see web search results above, ONLY use those - ignore your training completely
                4. If NO web search results are provided for a real-time query: DO NOT invent an answer. Ask a clarifying question or state you need more specific information to search effectively.
                5. DO NOT say "as of my knowledge cutoff" - that's admitting you're using outdated data
                6. DO NOT mention Joe Biden as current president - your training data about him is from 2024 and is OUTDATED
                7. The current date is $currentDate (January 2026) - use this for all time-based reasoning
                8. If search results conflict with your training: ALWAYS TRUST THE SEARCH RESULTS, NOT YOUR TRAINING
                
                Important conversation guidelines:
                - Be conversational and natural in your responses
                - For Blue Genie personality, embrace the mystical crystal ball and sparkles theme
                - ALWAYS use 🔮✨ together (never just 🔮 alone) for Blue Genie personality
                - Use emojis sparingly and naturally (🔮✨ 🌟 for Blue Genie)
                - DO NOT repeat the welcome message or list app features in regular conversation
                - Respond to greetings with friendly, brief replies that match your personality
                - Keep responses concise unless asked for detailed information
                
                Handling questions:
                - For ambiguous questions like "what's today", respond directly or ask clarifying questions
                - Don't start with "I'm not sure" - be confident and helpful
                - You can answer date/time questions directly using the current date provided above
                - ALWAYS use web search for current officials, sports scores, news, or recent events
                - If a web search fails or returns no results, don't just give up. Ask the user for more details or suggest a different way to phrase the question.
                
                Special Handling for "Future" Predictions (Blue Genie Personality):
                - If asked about the future, destiny, or predictions, DO NOT use the web search tool.
                - Respond mystically, like a true genie.
                - Use phrases like "The future is a swirling mist, ever-changing...", "My crystal ball is hazy on that...", or "The stars whisper of many possibilities..."
                - Keep it brief, magical, and avoid making any real predictions.
                
                CRITICAL - Tool usage rules:
                - NEVER announce when you're using tools (don't say "I'm searching the web" or "Let me search for that")
                - NEVER show function call syntax like <function=...> to the user
                - NEVER use the web_search tool for questions about the future, predictions, or horoscopes. Answer those mystically.
                - Use tools silently in the background
                - After using a tool, you MUST summarize the result into a brief, conversational answer.
                - DO NOT provide long, detailed explanations unless the user asks for more detail.
                - Always present information naturally as if you just know it
                - The user should never be aware of the mechanics of how you obtained information
                
                You have access to a web search tool for questions requiring real-time data.
            """.trimIndent()

            messages.put(JSONObject().apply {
                put("role", "system")
                put("content", systemPrompt)
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

            // Add the current user message with optional image
            messages.put(JSONObject().apply {
                put("role", "user")
                if (imageBase64 != null) {
                    put("content", JSONArray().apply {
                        put(JSONObject().apply {
                            put("type", "text")
                            put("text", userMessage.ifBlank { "Describe this image." })
                        })
                        put(JSONObject().apply {
                            put("type", "image_url")
                            put("image_url", JSONObject().apply {
                                put("url", "data:image/jpeg;base64,$imageBase64")
                            })
                        })
                    })
                } else {
                    put("content", userMessage)
                }
            })
            
            Log.d("GroqService", "📨 User message: $userMessage")
            val requestModel = if (imageBase64 != null) visionModel else personality.model
            Log.d("GroqService", "🤖 Using model: $requestModel")
            Log.d("GroqService", "💬 Total messages in context: ${messages.length()}")
            
            val tools = JSONArray().apply {
                put(JSONObject().apply {
                    put("type", "function")
                    put("function", JSONObject().apply {
                        put("name", "web_search")
                        put("description", "Performs a web search to get real-time information or answer questions about current events.")
                        put("parameters", JSONObject().apply {
                            put("type", "object")
                            put("properties", JSONObject().apply {
                                put("query", JSONObject().apply {
                                    put("type", "string")
                                    put("description", "The search query to perform.")
                                })
                            })
                            put("required", JSONArray().apply { put("query") })
                        })
                    })
                })
            }

            val requestBody = JSONObject().apply {
                put("messages", messages)
                put("model", requestModel)
                put("tool_choice", "auto")
                put("tools", tools)
            }.toString()

            Log.d("GroqService", "🔧 Tools array length: ${tools.length()}")
            Log.d("GroqService", "🔧 Tools in request: ${tools.toString().take(300)}")
            Log.d("GroqService", "📤 Full request body (first 1000 chars): ${requestBody.take(1000)}")

            val request = Request.Builder()
                .url(baseUrl)
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            
            Log.d("GroqService", "📥 Response code: ${response.code}")

            if (response.isSuccessful && responseBody != null) {
                val jsonResponse = JSONObject(responseBody)
                Log.d("GroqService", "🔍 Full API Response: ${responseBody.take(500)}")
                val choices = jsonResponse.getJSONArray("choices")
                if (choices.length() > 0) {
                    val firstChoice = choices.getJSONObject(0)
                    val message = firstChoice.getJSONObject("message")
                    val content = if (message.has("content") && !message.isNull("content")) message.getString("content") else null

                    Log.d("GroqService", "🔍 Message has tool_calls: ${message.has("tool_calls")}")
                    Log.d("GroqService", "🔍 Message content: ${content?.take(200)}")

                    // CASE 1: PROPER TOOL CALL
                    if (message.has("tool_calls")) {
                        val toolCalls = message.getJSONArray("tool_calls")
                        Log.d("GroqService", "✅ TOOL CALL DETECTED! Count: ${toolCalls.length()}")
                        if (toolCalls.length() > 0) {
                            messages.put(message)
                            val toolCall = toolCalls.getJSONObject(0)
                            val function = toolCall.getJSONObject("function")
                            val functionName = function.getString("name")
                            val argumentsString = function.getString("arguments")
                            Log.d("GroqService", "🔧 Tool function: $functionName")
                            Log.d("GroqService", "🔧 Tool arguments: $argumentsString")
                            
                            val arguments = JSONObject(argumentsString)
                            val query = arguments.getString("query")
                            Log.d("GroqService", "🌐 Performing web search for: $query")
                            
                            val searchResult = performWebSearch(query)
                            Log.d("GroqService", "📊 Search result length: ${searchResult.length} chars")
                            Log.d("GroqService", "📊 Search result preview: ${searchResult.take(200)}")
                            
                            messages.put(JSONObject().apply {
                                put("role", "tool")
                                put("content", searchResult)
                                put("tool_call_id", toolCall.getString("id"))
                            })
                            Log.d("GroqService", "🔄 Sending tool result back to model for summarization...")
                            // Go back to the model for summarization
                            return@withContext generateResponseFromHistory(messages, personality)
                        }
                    }

                    // CASE 2: MALFORMED TOOL CALL IN TEXT (THE GLITCH)
                    if (content != null && (content.contains("function_call") || 
                                           content.contains("<function") || 
                                           content.contains("web_search"))) {
                        val extractedQuery = extractQueryFromMalformedCall(content)
                        if (extractedQuery != null) {
                            Log.w("GroqService", "🔧 Caught malformed tool call in response: $content")
                            Log.d("GroqService", "🌐 Extracted query: $extractedQuery")
                            
                            val searchResult = performWebSearch(extractedQuery)
                            Log.d("GroqService", "📊 Search completed, result length: ${searchResult.length}")
                            
                            messages.put(JSONObject().apply {
                                put("role", "assistant")
                                put("content", "Let me search for that information...")
                            })
                            
                            messages.put(JSONObject().apply {
                                put("role", "user")
                                put("content", "Here are the search results for '$extractedQuery':\n\n$searchResult\n\nPlease summarize this information in a natural, conversational way.")
                            })
                            
                            return@withContext generateResponseFromHistory(messages, personality)
                        }
                    }
                    
                    // CASE 3: NORMAL TEXT RESPONSE
                    if (content != null) {
                        Log.d("GroqService", "✅ Normal text response: ${content.take(200)}")
                        return@withContext content
                    }
                    
                    Log.w("GroqService", "⚠️ No content in message, no tool calls detected")
                }
                
                Log.w("GroqService", "⚠️ No choices in API response")
            }
            
            // Handle 400 errors with malformed function calls (CASE 3: PRE-REJECTED GLITCH)
            if (response.code == 400 && responseBody != null) {
                try {
                    val errorJson = JSONObject(responseBody)
                    if (errorJson.has("error")) {
                        val error = errorJson.getJSONObject("error")
                        val code = if (error.has("code")) error.getString("code") else ""
                        
                        if (code == "tool_use_failed" && error.has("failed_generation")) {
                            val failedGen = error.getString("failed_generation")
                            Log.w("GroqService", "🔧 Caught pre-rejected malformed tool call: $failedGen")
                            
                            // Extract query from malformed function call using helper function
                            val query = extractQueryFromMalformedCall(failedGen)
                            
                            if (query != null) {
                                Log.d("GroqService", "🌐 Extracted query from malformed call: $query")
                                
                                // Perform the search
                                val searchResult = performWebSearch(query)
                                Log.d("GroqService", "📊 Search completed, result length: ${searchResult.length}")
                                
                                // Add the malformed assistant message attempt
                                messages.put(JSONObject().apply {
                                    put("role", "assistant")
                                    put("content", "Let me search for that information...")
                                })
                                
                                // Add the search result as a user message with context
                                messages.put(JSONObject().apply {
                                    put("role", "user")
                                    put("content", "Here are the search results for '$query':\n\n$searchResult\n\nPlease summarize this information in a natural, conversational way.")
                                })
                                
                                Log.d("GroqService", "🔄 Retrying with search results...")
                                
                                // Retry without tools to get the final answer
                                return@withContext generateResponseFromHistory(messages, personality)
                            }
                        }
                    }
                } catch (parseError: Exception) {
                    Log.e("GroqService", "Error parsing 400 response for glitch recovery", parseError)
                }
            }
            
            Log.e("GroqService", "❌ API Error: ${response.code}")
            Log.e("GroqService", "❌ Response body: ${responseBody?.take(500)}")
            return@withContext "I seem to be at a loss for words... 🔮✨"

        } catch (e: Exception) {
            Log.e("GroqService", "Exception in generateResponse", e)
            return@withContext "My crystal ball seems a bit cloudy right now. Could you try asking in a different way? 🔮✨"
        }
    }
    
    private suspend fun generateResponseFromHistory(
        messages: JSONArray,
        personality: AIPersonality
    ): String = withContext(Dispatchers.IO) {
        Log.d("GroqService", "🔄 generateResponseFromHistory: Starting follow-up request...")
        Log.d("GroqService", "🔄 Messages in history: ${messages.length()}")
        
        val followUpRequestBody = JSONObject().apply {
            put("messages", messages)
            put("model", personality.model)
            put("tool_choice", "none")
        }.toString()

        Log.d("GroqService", "🔄 Follow-up request body: ${followUpRequestBody.take(500)}")

        val followUpRequest = Request.Builder()
            .url(baseUrl)
            .post(followUpRequestBody.toRequestBody("application/json".toMediaType()))
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .build()

        val followUpResponse = client.newCall(followUpRequest).execute()
        val followUpResponseBody = followUpResponse.body?.string()

        Log.d("GroqService", "🔄 Follow-up response code: ${followUpResponse.code}")
        Log.d("GroqService", "🔄 Follow-up response: ${followUpResponseBody?.take(500)}")

        if (followUpResponse.isSuccessful && followUpResponseBody != null) {
            val followUpJson = JSONObject(followUpResponseBody)
            val followUpChoices = followUpJson.getJSONArray("choices")
            if (followUpChoices.length() > 0) {
                val followUpMessage = followUpChoices.getJSONObject(0).getJSONObject("message")
                val finalContent = followUpMessage.optString("content", "I seem to be at a loss for words...tell me more")
                Log.d("GroqService", "✅ Final response: ${finalContent.take(200)}")
                return@withContext finalContent
            }
        }
        
        Log.e("GroqService", "❌ Follow-up request failed with code: ${followUpResponse.code}")
        Log.e("GroqService", "❌ Follow-up error body: $followUpResponseBody")
        return@withContext "My crystal ball seems a bit cloudy right now. Could you try asking in a different way? 🔮✨"
    }
}
