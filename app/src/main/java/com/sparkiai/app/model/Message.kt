package com.sparkiai.app.model

data class Message(
    val id: String = java.util.UUID.randomUUID().toString(),
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUri: String? = null,
    val fileUri: String? = null,
    val fileName: String? = null,
    val messageType: MessageType = MessageType.TEXT,
    val personalityId: String? = null,
    val isBookmarked: Boolean = false // Track if the message is bookmarked
)

enum class MessageType {
    TEXT,
    IMAGE,
    TEXT_WITH_IMAGE,
    FILE,
    TEXT_WITH_FILE
}