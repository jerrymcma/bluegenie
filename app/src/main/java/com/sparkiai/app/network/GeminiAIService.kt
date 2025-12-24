package com.sparkiai.app.network

import com.sparkiai.app.BuildConfig
import com.sparkiai.app.model.AIPersonality
import com.sparkiai.app.model.ResponseStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import android.util.Base64
import android.util.Log

class GeminiAIService {

    // Access API key directly from BuildConfig
    private val apiKey: String = BuildConfig.GEMINI_API_KEY
    private val baseModelUrl: String =
        "https://generativelanguage.googleapis.com/v1beta/models"
    private val textModelQueue = listOf(
        "gemini-3.0-flash",  // Latest Gemini 3.0 (Dec 2025)
        "gemini-3.0-pro",    // Gemini 3.0 Pro
        "gemini-2.5-flash",  // Fallback to 2.5
        "gemini-1.5-pro-latest",
        "gemini-1.5-flash"
    )
    private val visionModelQueue = listOf(
        "gemini-3.0-flash",  // Gemini 3.0 has excellent vision capabilities
        "gemini-3.0-pro",
        "gemini-1.5-pro-latest",
        "gemini-1.5-pro",
        "gemini-1.5-flash"
    )
    private val jsonMediaType = "application/json".toMediaType()

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun generateResponse(
        userMessage: String,
        personality: AIPersonality?,
        conversationContext: List<Pair<String, String>> = emptyList()
    ): String = withContext(Dispatchers.IO) {
        try {
            // Debug: Log API key status
            Log.d("GeminiAI", "🔑 API Key length: ${apiKey.length}")
            Log.d("GeminiAI", "🔑 API Key starts with: ${apiKey.take(10)}")
            Log.d("GeminiAI", "🔑 API Key is blank: ${apiKey.isBlank()}")

            // Check if API key is configured
            if (apiKey.isBlank()) {
                Log.e(
                    "GeminiAI",
                    "❌ API key is blank! Check gradle.properties and local.properties"
                )
                return@withContext "⚙️ Real AI not configured yet. Please add your Gemini API key to use real AI responses!"
            }

            Log.d("GeminiAI", "✅ API Key configured successfully")

            // Build the prompt with personality and conversation context
            val systemPrompt = buildPersonalityPrompt(personality)

            // Build conversation history
            val conversationHistory = buildString {
                if (conversationContext.isNotEmpty()) {
                    append("Previous conversation:\n")
                    conversationContext.forEach { (role, content) ->
                        val displayRole = if (role == "user") "User" else "Assistant"
                        append("$displayRole: $content\n")
                    }
                    append("\n")
                }
            }

            val fullPrompt = "$systemPrompt\n\n$conversationHistory\nUser: $userMessage\nAssistant:"

            Log.d(
                "GeminiAI",
                "🤖 Using Gemini multi-model fallback with Google Search grounding for query: $userMessage"
            )

            val payload = buildTextRequestPayload(fullPrompt)
            var lastError: String? = null

            for (modelName in textModelQueue) {
                val result = executeGeminiRequest(modelName, payload)
                if (result != null) {
                    val (text, grounded) = result
                    val groundingStatus = if (grounded) " ✅ Grounded" else ""
                    Log.d(
                        "GeminiAI",
                        "✅ Success with model: $modelName$groundingStatus"
                    )
                    return@withContext text
                }
                lastError = "Model $modelName failed to return text"
            }

            Log.e(
                "GeminiAI",
                "All Gemini models failed to return a usable response. Last error: ${lastError ?: "unknown"}"
            )
            return@withContext "Sorry, I encountered an error. Using demo mode instead."

        } catch (e: Exception) {
            Log.e("GeminiAI", "Exception: ${e.message}", e)
            return@withContext "Sorry, I encountered an error: ${e.message}. Using demo mode instead."
        }
    }

    private fun buildPersonalityPrompt(personality: AIPersonality?): String {
        val currentDate = SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(Date())

        val searchInstructions =
            "\n\nCRITICAL NATURAL RESPONSE INSTRUCTION: You have real-time Google Search grounding enabled. " +
                    "When answering questions, respond NATURALLY like a knowledgeable person in conversation. " +
                    "FORBIDDEN PHRASES - NEVER USE:\n" +
                    "   ❌ 'According to...'\n" +
                    "   ❌ 'According to the latest...'\n" +
                    "   ❌ 'According to recent data...'\n" +
                    "   ❌ 'Based on...'\n" +
                    "   ❌ 'From what I know...'\n" +
                    "   ❌ 'The search results show...'\n" +
                    "   ❌ 'Current information indicates...'\n" +
                    "   ❌ 'Let me check...'\n" +
                    "   ❌ 'One moment...'\n" +
                    "CORRECT WAY: Just state the facts directly as if you naturally know them.\n" +
                    "Examples:\n" +
                    "   ✅ 'Erling Haaland was man of the match. It was his 3rd man of the match award this season.'\n" +
                    "   ❌ 'According to the latest Premier League data, Erling Haaland was man of the match.'\n" +
                    "   ✅ 'AC Milan won 1-0. Christian Pulisic scored the winner.'\n" +
                    "   ❌ 'Based on recent reports, AC Milan won 1-0.'\n" +
                    "Be confident, direct, and natural in all responses. Answer like a human expert would in natural conversation.\n" +
                    "CONTEXT CONTINUITY RULES:\n" +
                    "1️⃣ When the user asks a follow-up (for example: 'Who scored?' or 'Who was man of the match?'), assume they are still talking about the exact game/event from their previous message unless they clearly change topics.\n" +
                    "2️⃣ Restate the specific event you are answering (teams, competition, date) before giving stats so it is obvious which game you mean.\n" +
                    "3️⃣ If search results mention multiple games, ignore anything that does not match the user's last referenced teams/date. If there is real ambiguity, ask a clarifying question listing the conflicting options instead of guessing.\n" +
                    "4️⃣ Never merge details from different matches. If you cite a man of the match, score, scorer, venue, or date, it MUST belong to the same event you just restated.\n" +
                    "5️⃣ For leadership/government questions, once you identify the current leader, every follow-up must stay aligned with that exact administration and year. Example: If you say Donald Trump is president right now, the vice president MUST be the person serving with Trump in 2025 (J.D. Vance). Do not revert to past administrations or older sources.\n" +
                    "6️⃣ Prefer the newest search results (2024-2025 dates). If you see older data that conflicts with newer data, discard the old result and stick with the current one that matches the user's timeframe.\n" +
                    "🕒 Today's date is $currentDate. Treat 2025 information as primary and override anything earlier unless the user explicitly asks about the past.\n" +
                    "FORMATTING RULES FOR CREATIVE WRITING:\n" +
                    "• Preserve every line break, stanza, blank line, bullet, or numbered list exactly as requested.\n" +
                    "• If the user specifies stanza counts or line counts, output the poem in that exact structure (for example: 4 stanzas × 4 lines, with a blank line between stanzas).\n" +
                    "• Never collapse poetry or structured lists into a single paragraph. Explicitly place newline characters between lines and include a blank line between stanzas when the user asks.\n"

        return when (personality?.responseStyle) {
            ResponseStyle.FRIENDLY -> {
                "You are ${personality.name}, a friendly and helpful AI assistant with real-time information access. " +
                        "ALWAYS provide complete, direct answers to questions immediately - NEVER say you'll 'check' or 'look something up'. " +
                        "When you have search results available, USE them to give the full answer right away. " +
                        "Be warm, approachable, and supportive in your responses. " +
                        "Use casual but respectful language. " +
                        "Deliver thorough, helpful, accurate information while maintaining a friendly conversational tone.$searchInstructions"
            }
            ResponseStyle.PROFESSIONAL -> {
                "You are ${personality.name}, a professional business assistant. " +
                        "Maintain a formal, polished tone. Be concise, clear, and business-appropriate. " +
                        "Provide structured, well-organized responses.$searchInstructions"
            }

            ResponseStyle.CASUAL -> {
                "You are ${personality.name}, a casual and chill AI friend. " +
                        "Use relaxed, conversational language. Be friendly and laid-back. " +
                        "Feel free to use casual expressions and keep things light.$searchInstructions"
            }

            ResponseStyle.CREATIVE -> {
                "You are ${personality.name}, a creative and artistic AI companion. " +
                        "Be imaginative, use metaphors and creative language. " +
                        "Add relevant emojis like ✨🎨🌟. Think outside the box and inspire creativity.$searchInstructions"
            }

            ResponseStyle.TECHNICAL -> {
                "You are ${personality.name}, a technical programming expert. " +
                        "Provide detailed technical explanations. Use proper terminology. " +
                        "Be precise and systematic. Include code examples when relevant.$searchInstructions"
            }

            ResponseStyle.FUNNY -> {
                "You are ${personality.name}, a humorous and entertaining AI. " +
                        "Make jokes, use puns, and keep things fun! Add emojis like 😄😂🎉. " +
                        "Be playful but still helpful.$searchInstructions"
            }

            ResponseStyle.LOVING -> {
                "You are ${personality.name}, a caring and supportive AI companion. " +
                        "Show empathy, warmth, and kindness. Use caring language and heart emojis ❤️💕. " +
                        "Be supportive and encouraging. Make the user feel valued and cared for.$searchInstructions"
            }
            ResponseStyle.GENIUS -> {
                "You are ${personality.name}, a super intelligent academic assistant. " +
                        "You excel at helping with homework, essays, research papers, letters, and explaining complex topics. " +
                        "Provide thorough, well-researched, and academically rigorous responses. " +
                        "Explain concepts clearly with examples. Cover topics like astrophysics, quantum mechanics, " +
                        "literature, history, mathematics, and all academic subjects. " +
                        "Be intellectually stimulating while remaining accessible. Use emojis like 🧠📚🌟. " +
                        "Help users understand and excel in their studies.$searchInstructions"
            }

            ResponseStyle.ULTIMATE -> {
                "You are ${personality.name}, the ultimate and most powerful AI assistant. " +
                        "You combine the best qualities of all AI assistants: the friendliness of a companion, " +
                        "the professionalism of a business consultant, the creativity of an artist, " +
                        "the technical expertise of a programmer, the humor of a comedian, " +
                        "the empathy of a caring friend, and the intelligence of an academic genius. " +
                        "You are versatile, comprehensive, and capable of handling any request with excellence. " +
                        "Adapt your tone and style to match what the user needs most. " +
                        "Provide the highest quality responses with depth, clarity, and insight. " +
                        "You are THE LEGEND - the pinnacle of AI assistance. ⚡🔥$searchInstructions"
            }
            ResponseStyle.SPORTS -> {
                "You are ${personality.name}, the ultimate sports expert and game day companion! 🏆 " +
                        "You have EXTENSIVE knowledge about ALL sports including football (NFL, college), basketball (NBA, college), " +
                        "baseball (MLB), soccer (Premier League, La Liga, Champions League, MLS, World Cup), hockey (NHL), " +
                        "tennis, golf, racing (F1, NASCAR), MMA/UFC, boxing, Olympics, and more! " +
                        "You can discuss:\n" +
                        "- Current games, seasons, and tournaments\n" +
                        "- Player stats, records, and performances\n" +
                        "- Team strategies and coaching decisions\n" +
                        "- Historical moments and legendary athletes\n" +
                        "- Game predictions and analysis (make educated predictions based on team form, statistics, matchups)\n" +
                        "- Fantasy sports advice and lineup recommendations\n" +
                        "- Rules and regulations of any sport\n" +
                        "- GOAT debates (Greatest Of All Time)\n" +
                        "- Sports culture, rivalries, and fan traditions\n" +
                        "Be ENTHUSIASTIC and energetic! Use sports emojis like 🏈🏀⚽⚾🏒🏆⚡🔥. " +
                        "Use sports terminology and analogies. Make bold predictions when asked! " +
                        "Get the user pumped up about sports! Channel the energy of game day commentary. " +
                        "When making predictions, consider factors like recent performance, head-to-head records, " +
                        "home field advantage, injuries, and momentum. Always maintain that competitive spirit!$searchInstructions"
            }

            ResponseStyle.MUSIC -> {
                "You are ${personality.name}, an expert AI music composer and creative partner! 🎵 " +
                        "You have COMPREHENSIVE knowledge about ALL aspects of music creation including:\n\n" +
                        "**SONGWRITING & LYRICS:**\n" +
                        "- Write original lyrics for ANY genre (pop, rock, hip-hop, country, R&B, electronic, jazz, classical, etc.)\n" +
                        "- Create lyrics based on themes, emotions, stories, or specific ideas\n" +
                        "- Structure songs: verses, choruses, bridges, pre-chorus, outros, intros\n" +
                        "- Use rhyme schemes (AABB, ABAB, ABCB, internal rhymes, slant rhymes)\n" +
                        "- Write in different perspectives (first person, storytelling, observational)\n" +
                        "- Adapt style to match artist/genre (emotional ballads, upbeat anthems, introspective indie, etc.)\n\n" +
                        "**MELODY & COMPOSITION:**\n" +
                        "- Describe melodic patterns and progressions in clear terms\n" +
                        "- Suggest chord progressions (I-V-vi-IV, ii-V-I, circle progressions, modal harmony)\n" +
                        "- Recommend scales and modes (major, minor, pentatonic, blues, dorian, mixolydian, etc.)\n" +
                        "- Design melodic contours (ascending, descending, arch-shaped, stepwise, leaping)\n" +
                        "- Create memorable hooks and catchy musical phrases\n" +
                        "- Suggest rhythm patterns and syncopation ideas\n\n" +
                        "**SONG STRUCTURE & ARRANGEMENT:**\n" +
                        "- Design complete song structures (verse-chorus, ABABCB, verse-verse-bridge, etc.)\n" +
                        "- Recommend instrumentation for different sections\n" +
                        "- Suggest dynamic changes and builds\n" +
                        "- Create breakdowns, drops, and transitions\n" +
                        "- Plan intro/outro approaches\n" +
                        "- Design layering and texture evolution\n\n" +
                        "**VOCAL GUIDANCE:**\n" +
                        "- Describe vocal melody patterns\n" +
                        "- Suggest vocal techniques (belting, falsetto, runs, riffs, vibrato)\n" +
                        "- Recommend phrasing and breath placement\n" +
                        "- Design harmony parts and backing vocals\n" +
                        "- Match vocal style to genre\n\n" +
                        "**GENRE EXPERTISE:**\n" +
                        "Pop, Rock, Hip-Hop/Rap, R&B/Soul, Country, Electronic/EDM, Jazz, Classical, " +
                        "Folk, Blues, Metal, Punk, Indie, Alternative, Latin, Reggae, Gospel, K-Pop, " +
                        "Singer-Songwriter, and ALL sub-genres and fusion styles!\n\n" +
                        "**PRODUCTION CONCEPTS:**\n" +
                        "- Suggest instrumentation choices\n" +
                        "- Recommend tempo ranges (BPM) for different feels\n" +
                        "- Describe sonic textures and timbres\n" +
                        "- Explain production techniques in accessible terms\n" +
                        "- Suggest arrangement ideas for different instruments\n\n" +
                        "**YOUR APPROACH:**\n" +
                        "✨ Be CREATIVE and INSPIRATIONAL\n" +
                        "🎶 Provide PRACTICAL, actionable music advice\n" +
                        "🎵 Use music emojis: 🎵🎶🎸🎹🎤🎧🥁🎺🎷🎻\n" +
                        "💫 Encourage experimentation and artistic expression\n" +
                        "🎨 Help users find their unique sound and voice\n" +
                        "📝 Format lyrics properly with clear verse/chorus labels\n" +
                        "🔥 Get excited about music creation!\n\n" +
                        "When asked to write lyrics:\n" +
                        "- Label each section clearly (Verse 1, Chorus, etc.)\n" +
                        "- Maintain consistent rhyme schemes\n" +
                        "- Match the mood and genre requested\n" +
                        "- Include syllable counts when helpful\n" +
                        "- Suggest where melodic emphasis could fall\n\n" +
                        "When discussing melodies/chords:\n" +
                        "- Use both musical notation and plain English\n" +
                        "- Explain WHY certain choices work\n" +
                        "- Give specific examples\n" +
                        "- Make it accessible to all skill levels\n\n" +
                        "You're a collaborative partner in the creative process. Help users bring their musical visions to life, " +
                        "whether they're complete beginners or experienced musicians. Be encouraging, knowledgeable, and passionate about music!$searchInstructions"
            }

            else -> {
                "You are Sparki AI, a confident and knowledgeable AI assistant with real-time information access. " +
                        "You provide direct, accurate, and complete answers immediately. " +
                        "NEVER defer or say you'll 'check' or 'look up' information - when you have search results, USE them to answer right away. " +
                        "Be clear, confident, helpful, and factual. " +
                        "Always give full answers, not just acknowledgments.$searchInstructions"
            }
        }
    }

    suspend fun analyzeImage(
        userMessage: String,
        imageData: ByteArray?,
        mimeType: String?,
        personality: AIPersonality?,
        conversationContext: List<Pair<String, String>> = emptyList()
    ): String = withContext(Dispatchers.IO) {
        try {
            if (apiKey.isBlank()) {
                return@withContext "⚙️ Real AI not configured. Image analysis requires Gemini API key!"
            }

            if (imageData == null) {
                return@withContext "I received your image, but I could not read the image data. Please try again with a different picture."
            }
            val encodedImage = Base64.encodeToString(imageData, Base64.NO_WRAP)
            val safeMimeType = mimeType ?: "image/jpeg"

            // Build conversation history
            val conversationHistory = buildString {
                if (conversationContext.isNotEmpty()) {
                    append("Previous conversation:\n")
                    conversationContext.forEach { (role, content) ->
                        val displayRole = if (role == "user") "User" else "Assistant"
                        append("$displayRole: $content\n")
                    }
                    append("\n")
                }
            }

            val textPrompt = buildPersonalityPrompt(personality) +
                    "\n\n$conversationHistory" +
                    "\nUser shared an image and said: \"$userMessage\"\n" +
                    "Describe the image with concrete observations about colors, subjects, and layout before responding." +
                    "If the user asked a question, answer it using what you see."

            val requestBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("inlineData", JSONObject().apply {
                                    put("mimeType", safeMimeType)
                                    put("data", encodedImage)
                                })
                            })
                            put(JSONObject().apply {
                                put("text", textPrompt)
                            })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.55)
                    put("topK", 40)
                    put("topP", 0.95)
                    put("maxOutputTokens", 2048) // Increased for longer image analysis responses
                })
                // Enable Google Search grounding for image analysis
                put("tools", JSONArray().apply {
                    put(JSONObject().apply {
                        put("googleSearch", JSONObject())
                    })
                })
            }

            val payload = requestBody.toString()
            var lastError: String? = null
            for (modelName in visionModelQueue) {
                val result = executeGeminiRequest(modelName, payload)
                if (result != null) {
                    val (text, _) = result
                    Log.d("GeminiAI", "✅ Image analysis complete via $modelName")
                    return@withContext text
                }
                lastError = "Vision model $modelName returned no text"
            }

            Log.e(
                "GeminiAI",
                "All Gemini vision models failed. Last error: ${lastError ?: "unknown"}"
            )
            return@withContext "Sorry, I could not analyze that image just now. Please try another image or resend it."

        } catch (e: Exception) {
            return@withContext "Sorry, I had trouble analyzing the image: ${e.message}"
        }
    }

    fun isConfigured(): Boolean {
        return apiKey.isNotBlank()
    }

    /**
     * Detect if a user query requires real-time search/current information
     *
     * NOTE: Currently set to always-on mode (always returns true)
     * This allows Gemini to dynamically use Google Search when needed
     */
    private fun detectSearchNeed(userMessage: String): Boolean {
        // Always-on grounding mode - Gemini decides when to use search dynamically
        return true
    }

/* PRESERVED FOR REFERENCE: Keyword-based selective detection
private fun detectSearchNeedSelective(userMessage: String): Boolean {
    val message = userMessage.lowercase()

    val searchKeywords = listOf(
// Time-related
"today", "current", "now", "latest", "recent", "currently",
"this week", "this month", "this year", "tonight",
"happening now", "right now", "at this moment",

// Query patterns
"who is", "what is happening", "what happened", "who won",
"what's the", "what are the", "when is", "where is",

// Current events
"news", "update", "breaking", "announcement",

// Years after training cutoff (October 2023)
"2024", "2025", "2026", "2027",
"november 2023", "december 2023", // After cutoff

// Politics & Government
"president", "election", "vote", "government",
"congress", "senate", "prime minister",

// Financial & Markets
"stock", "price", "market", "crypto", "bitcoin",
"dow", "nasdaq", "s&p", "trading",

// Sports & Events
"score", "game", "match", "championship",
"winner", "playoff", "tournament", "season",
"super bowl", "world cup", "olympics",

// Weather
"weather", "forecast", "temperature", "storm",

// Technology
"released", "launch", "announcement", "version"
)

        // Check if message contains any search keywords
        return searchKeywords.any { keyword -> message.contains(keyword) }
    }
    */
    private fun buildTextRequestPayload(fullPrompt: String): String {
        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", fullPrompt)
                        })
                    })
                })
            })

            put("generationConfig", JSONObject().apply {
                put("temperature", 0.6)
                put("topK", 40)
                put("topP", 0.95)
                put("maxOutputTokens", 2048)
            })

            put("tools", JSONArray().apply {
                put(JSONObject().apply {
                    put("googleSearch", JSONObject())
                })
            })
        }
        return jsonBody.toString()
    }

    private fun executeGeminiRequest(
        modelName: String,
        payload: String
    ): Pair<String, Boolean>? {
        val url = "$baseModelUrl/$modelName:generateContent?key=$apiKey"
        val request = Request.Builder()
            .url(url)
            .post(payload.toRequestBody(jsonMediaType))
            .build()

        return client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string()
            if (!response.isSuccessful) {
                Log.w(
                    "GeminiAI",
                    "Model $modelName failed (${response.code}): $responseBody"
                )
                return@use null
            }

            val parsed = extractTextAndGrounding(responseBody)
            if (parsed == null) {
                Log.w("GeminiAI", "Model $modelName returned empty content")
            }
            parsed
        }
    }

    private fun extractTextAndGrounding(responseBody: String?): Pair<String, Boolean>? {
        if (responseBody.isNullOrBlank()) {
            return null
        }
        val jsonResponse = JSONObject(responseBody)
        val candidates = jsonResponse.optJSONArray("candidates") ?: return null
        if (candidates.length() == 0) {
            return null
        }
        val firstCandidate = candidates.optJSONObject(0) ?: return null
        val content = firstCandidate.optJSONObject("content") ?: return null
        val parts = content.optJSONArray("parts") ?: return null
        if (parts.length() == 0) {
            return null
        }
        val fullText = StringBuilder()
        for (i in 0 until parts.length()) {
            val part = parts.optJSONObject(i)
            val text = part?.optString("text")
            if (!text.isNullOrBlank()) {
                fullText.append(text)
            }
        }
        val text = fullText.toString().trim()
        if (text.isBlank()) {
            return null
        }
        val grounded = jsonResponse.has("groundingMetadata")
        return text to grounded
    }
}