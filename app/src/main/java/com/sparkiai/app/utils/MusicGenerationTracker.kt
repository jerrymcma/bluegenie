package com.sparkiai.app.utils

import android.content.Context
import android.content.SharedPreferences
import com.sparkiai.app.config.FeatureFlags

/**
 * Tracks music generation usage for freemium model
 * - Tracks free songs remaining
 * - Manages pay-as-you-go after free tier
 * - Persists usage data
 */
class MusicGenerationTracker(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "music_generation_prefs",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_SONGS_GENERATED = "songs_generated"
        private const val KEY_FREE_TIER_EXHAUSTED = "free_tier_exhausted"
        private const val KEY_TOTAL_COST_CENTS = "total_cost_cents"
        private const val KEY_LAST_GENERATION_TIME = "last_generation_time"
    }

    /**
     * Get number of songs generated (total)
     */
    fun getSongsGenerated(): Int {
        return prefs.getInt(KEY_SONGS_GENERATED, 0)
    }

    /**
     * Get number of free songs remaining
     */
    fun getFreeSongsRemaining(): Int {
        val generated = getSongsGenerated()
        val limit = FeatureFlags.FreemiumConfig.FREE_SONGS_LIMIT
        return maxOf(0, limit - generated)
    }

    /**
     * Check if user has free songs available
     */
    fun hasFreeSongsAvailable(): Boolean {
        return getFreeSongsRemaining() > 0
    }

    /**
     * Check if user is in free tier
     */
    fun isInFreeTier(): Boolean {
        return getSongsGenerated() < FeatureFlags.FreemiumConfig.FREE_SONGS_LIMIT
    }

    /**
     * Check if free tier is exhausted
     */
    fun isFreeTierExhausted(): Boolean {
        return !isInFreeTier()
    }

    /**
     * Check if user should see upgrade prompt
     */
    fun shouldShowUpgradePrompt(): Boolean {
        val generated = getSongsGenerated()
        val promptThreshold = FeatureFlags.FreemiumConfig.SHOW_UPGRADE_PROMPT_AT
        val limit = FeatureFlags.FreemiumConfig.FREE_SONGS_LIMIT

        return generated >= promptThreshold && generated < limit &&
                FeatureFlags.FreemiumConfig.SHOW_FREE_SONGS_COUNTER
    }

    /**
     * Check if user can generate music
     * Returns true if:
     * - In free tier, OR
     * - Payment setup allowed without payment
     */
    fun canGenerateMusic(): Boolean {
        if (isInFreeTier()) {
            return true
        }

        // After free tier, check if generation is allowed without payment
        return FeatureFlags.FreemiumConfig.ALLOW_FREE_TIER_WITHOUT_PAYMENT || hasPaymentSetup()
    }

    /**
     * Record a successful music generation
     */
    fun recordGeneration() {
        val currentCount = getSongsGenerated()
        prefs.edit().apply {
            putInt(KEY_SONGS_GENERATED, currentCount + 1)
            putLong(KEY_LAST_GENERATION_TIME, System.currentTimeMillis())

            // Track cost if beyond free tier
            if (!isInFreeTier()) {
                val currentCost = getTotalCostCents()
                val costPerSong = FeatureFlags.FreemiumConfig.COST_PER_SONG_CENTS
                putInt(KEY_TOTAL_COST_CENTS, currentCost + costPerSong)
            }

            apply()
        }
    }

    /**
     * Get total cost incurred (in cents)
     */
    fun getTotalCostCents(): Int {
        return prefs.getInt(KEY_TOTAL_COST_CENTS, 0)
    }

    /**
     * Get total cost incurred (formatted as dollars)
     */
    fun getTotalCostDollars(): String {
        val cents = getTotalCostCents()
        val dollars = cents / 100.0
        return String.format("$%.2f", dollars)
    }

    /**
     * Get cost for next generation
     */
    fun getNextGenerationCost(): Int {
        return if (isInFreeTier()) {
            0
        } else {
            FeatureFlags.FreemiumConfig.COST_PER_SONG_CENTS
        }
    }

    /**
     * Get cost for next generation (formatted)
     */
    fun getNextGenerationCostFormatted(): String {
        val cents = getNextGenerationCost()
        if (cents == 0) {
            return "FREE"
        }
        val dollars = cents / 100.0
        return String.format("$%.2f", dollars)
    }

    /**
     * Get last generation time
     */
    fun getLastGenerationTime(): Long {
        return prefs.getLong(KEY_LAST_GENERATION_TIME, 0)
    }

    /**
     * Reset usage (for testing or user request)
     */
    fun resetUsage() {
        prefs.edit().apply {
            putInt(KEY_SONGS_GENERATED, 0)
            putInt(KEY_TOTAL_COST_CENTS, 0)
            putBoolean(KEY_FREE_TIER_EXHAUSTED, false)
            putLong(KEY_LAST_GENERATION_TIME, 0)
            apply()
        }
    }

    /**
     * Check if payment is setup (placeholder for future payment integration)
     */
    private fun hasPaymentSetup(): Boolean {
        // TODO: Implement actual payment check when payment system is integrated
        // For now, return false to block after free tier
        return prefs.getBoolean("payment_setup", false)
    }

    /**
     * Set payment setup status (for future integration)
     */
    fun setPaymentSetup(isSetup: Boolean) {
        prefs.edit().putBoolean("payment_setup", isSetup).apply()
    }

    /**
     * Get usage statistics
     */
    fun getUsageStats(): MusicUsageStats {
        return MusicUsageStats(
            totalGenerated = getSongsGenerated(),
            freeRemaining = getFreeSongsRemaining(),
            isInFreeTier = isInFreeTier(),
            totalCostCents = getTotalCostCents(),
            totalCostFormatted = getTotalCostDollars(),
            nextGenerationCost = getNextGenerationCost(),
            nextGenerationCostFormatted = getNextGenerationCostFormatted(),
            canGenerate = canGenerateMusic(),
            shouldShowUpgrade = shouldShowUpgradePrompt()
        )
    }
}

/**
 * Usage statistics data class
 */
data class MusicUsageStats(
    val totalGenerated: Int,
    val freeRemaining: Int,
    val isInFreeTier: Boolean,
    val totalCostCents: Int,
    val totalCostFormatted: String,
    val nextGenerationCost: Int,
    val nextGenerationCostFormatted: String,
    val canGenerate: Boolean,
    val shouldShowUpgrade: Boolean
)
