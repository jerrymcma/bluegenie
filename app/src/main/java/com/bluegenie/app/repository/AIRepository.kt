package com.bluegenie.app.repository

import android.content.Context
import com.bluegenie.app.model.AIPersonality
import com.bluegenie.app.network.GeminiAIService

class AIRepository(context: Context) {

    private val geminiAIService = GeminiAIService(context)

    suspend fun getAIResponse(
        userMessage: String,
        personality: AIPersonality? = null,
        conversationContext: List<Pair<String, String>> = emptyList()
    ): String {
        return geminiAIService.generateResponse(userMessage, personality, conversationContext)
    }

    suspend fun getImageAnalysisResponse(
        userMessage: String,
        imageUri: String?,
        imageData: ByteArray?,
        mimeType: String?,
        personality: AIPersonality? = null,
        conversationContext: List<Pair<String, String>> = emptyList()
    ): String {
        return geminiAIService.analyzeImage(
            userMessage,
            imageData,
            mimeType,
            personality,
            conversationContext
        )
    }
}
