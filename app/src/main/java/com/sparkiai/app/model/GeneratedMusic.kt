package com.sparkiai.app.model

import java.io.Serializable

/**
 * Data model for generated music tracks
 */
data class GeneratedMusic(
    val id: String,
    val prompt: String,
    val filePath: String,  // Path to saved audio file
    val mimeType: String,
    val durationSeconds: Int,
    val timestamp: Long,
    val isFreeTier: Boolean,  // Whether this was generated in free tier
    val costCents: Int = 0  // Cost for this generation (0 if free)
) : Serializable {

    /**
     * Get filename from filepath
     */
    fun getFileName(): String {
        return filePath.substringAfterLast("/")
    }

    /**
     * Get formatted duration (MM:SS)
     */
    fun getFormattedDuration(): String {
        val minutes = durationSeconds / 60
        val seconds = durationSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    /**
     * Get formatted cost
     */
    fun getFormattedCost(): String {
        if (costCents == 0) {
            return "FREE"
        }
        val dollars = costCents / 100.0
        return String.format("$%.2f", dollars)
    }

    /**
     * Get shortened prompt for display
     */
    fun getShortPrompt(maxLength: Int = 50): String {
        return if (prompt.length > maxLength) {
            "${prompt.take(maxLength)}..."
        } else {
            prompt
        }
    }
}
