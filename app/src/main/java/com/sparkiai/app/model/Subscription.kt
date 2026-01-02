package com.sparkiai.app.model

/**
 * User subscription data model matching web app structure
 */
data class UserSubscription(
    val isPremium: Boolean = false,
    val messageCount: Int = 0,
    val songCount: Int = 0,
    val songsThisPeriod: Int = 0,
    val subscriptionStartDate: String? = null,
    val periodStartDate: String? = null,
    val needsRenewal: Boolean = false
)

/**
 * User profile data from Supabase matching web app structure
 */
data class UserProfile(
    val id: String,
    val email: String,
    val messageCount: Int = 0,
    val songCount: Int = 0,
    val isPremium: Boolean = false,
    val subscriptionStartDate: String? = null,
    val songsThisPeriod: Int = 0,
    val periodStartDate: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

/**
 * Premium subscription constants
 */
object PremiumConstants {
    const val FREE_SONGS_LIMIT = 5
    const val PREMIUM_SONGS_PER_PERIOD = 50
    const val PREMIUM_PERIOD_DAYS = 30
    const val PREMIUM_PRICE_USD = 5.00
}
