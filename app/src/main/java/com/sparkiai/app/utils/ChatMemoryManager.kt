package com.sparkiai.app.utils

import android.content.Context
import android.content.SharedPreferences
import com.sparkiai.app.model.Message
import com.sparkiai.app.model.MessageType
import org.json.JSONArray
import org.json.JSONObject

class ChatMemoryManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "chat_memory_prefs",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val MAX_MESSAGES_PER_PERSONALITY = 500 // Messages saved for user viewing
        private const val MAX_CONTEXT_MESSAGES = 50 // Messages sent to AI for context
        const val AUTO_RESET_MESSAGE =
            "We've reached the max number of messages, and the chat has restarted. Kindly refresh my memory...what were we saying?"
    }

    /**
     * Save messages for a specific personality
     */
    fun saveMessages(personalityId: String, messages: List<Message>) {
        val key = "messages_$personalityId"
        val jsonArray = JSONArray()

        // Only save the most recent messages to avoid storage overflow
        val messagesToSave = messages.takeLast(MAX_MESSAGES_PER_PERSONALITY)

        messagesToSave.forEach { message ->
            val jsonObject = JSONObject().apply {
                put("id", message.id)
                put("content", message.content)
                put("isFromUser", message.isFromUser)
                put("timestamp", message.timestamp)
                put("imageUri", message.imageUri ?: "")
                put("fileUri", message.fileUri ?: "")
                put("fileName", message.fileName ?: "")
                put("messageType", message.messageType.name)
                put("personalityId", message.personalityId ?: personalityId)
                put("isBookmarked", message.isBookmarked)
            }
            jsonArray.put(jsonObject)
        }

        prefs.edit().putString(key, jsonArray.toString()).apply()
    }

    /**
     * Load messages for a specific personality
     */
    fun loadMessages(personalityId: String): List<Message> {
        val key = "messages_$personalityId"
        val jsonString = prefs.getString(key, null) ?: return emptyList()

        return try {
            val jsonArray = JSONArray(jsonString)
            val messages = mutableListOf<Message>()

            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val message = Message(
                    id = jsonObject.getString("id"),
                    content = jsonObject.getString("content"),
                    isFromUser = jsonObject.getBoolean("isFromUser"),
                    timestamp = jsonObject.getLong("timestamp"),
                    imageUri = jsonObject.getString("imageUri").takeIf { it.isNotBlank() },
                    fileUri = jsonObject.optString("fileUri").takeIf { it.isNotBlank() },
                    fileName = jsonObject.optString("fileName").takeIf { it.isNotBlank() },
                    messageType = MessageType.valueOf(jsonObject.getString("messageType")),
                    personalityId = jsonObject.optString("personalityId", personalityId),
                    isBookmarked = jsonObject.optBoolean("isBookmarked", false)
                )
                messages.add(message)
            }

            messages
        } catch (e: Exception) {
            android.util.Log.e("ChatMemoryManager", "Error loading messages: ${e.message}")
            emptyList()
        }
    }

    /**
     * Clear messages for a specific personality
     */
    fun clearMessages(personalityId: String) {
        val key = "messages_$personalityId"
        prefs.edit().remove(key).apply()
    }

    /**
     * Clear all messages for all personalities
     */
    fun clearAllMessages() {
        prefs.edit().clear().apply()
    }

    /**
     * Get conversation context for AI (recent messages formatted as conversation history)
     */
    fun getConversationContext(personalityId: String): List<Pair<String, String>> {
        val messages = loadMessages(personalityId)

        // Get the most recent messages for context
        val recentMessages = messages.takeLast(MAX_CONTEXT_MESSAGES)

        // Format as pairs of (role, content) for AI context
        return recentMessages.mapNotNull { message ->
            if (message.content.isNotBlank() && !message.content.startsWith("📷")) {
                val role = if (message.isFromUser) "user" else "assistant"
                role to message.content
            } else {
                null
            }
        }
    }

    /**
     * Get message count for a personality
     */
    fun getMessageCount(personalityId: String): Int {
        return loadMessages(personalityId).size
    }

    /**
     * Check if personality has chat history
     */
    fun hasHistory(personalityId: String): Boolean {
        return getMessageCount(personalityId) > 0
    }

    /**
     * Check if we need to auto-reset due to message limit
     */
    fun shouldAutoReset(personalityId: String): Boolean {
        return getMessageCount(personalityId) >= MAX_MESSAGES_PER_PERSONALITY
    }

    /**
     * Get the maximum message limit
     */
    fun getMaxMessageLimit(): Int {
        return MAX_MESSAGES_PER_PERSONALITY
    }
}