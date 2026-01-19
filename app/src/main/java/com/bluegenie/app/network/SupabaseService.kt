package com.bluegenie.app.network

// ===============================================================================
// SupabaseService - DISABLED
// ===============================================================================
// This entire file is disabled because the Blue Genie app is now completely free
// with no sign-in or subscription requirements.
//
// Previously handled:
// - Google Sign-In authentication
// - User profile management
// - Premium subscription tracking
// - Song count limits
//
// All of these features have been removed to make the app free for all users.
// ===============================================================================

/*
import android.content.Context
import android.util.Log
import com.bluegenie.app.BuildConfig
import com.bluegenie.app.model.UserProfile
import com.bluegenie.app.model.UserSubscription
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.builtin.IDToken
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Calendar
import java.util.Date

class SupabaseService(context: Context) {

    private val supabase = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY
    ) {
        install(Auth)
        install(Postgrest)
    }

    companion object {
        private const val TAG = "SupabaseService"
        private const val TABLE_USER_PROFILES = "user_profiles"
    }

    suspend fun signInWithGoogle(idToken: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔐 Signing in with Google ID token (length: ${idToken.length})")
            Log.d(TAG, "   Token preview: ${idToken.take(50)}...")
            
            supabase.auth.signInWith(IDToken) {
                this.idToken = idToken
                this.provider = Google
            }
            
            Log.d(TAG, "✅ Successfully signed in with Google via Supabase")
            
            val user = supabase.auth.currentUserOrNull()
            if (user != null) {
                Log.d(TAG, "✅ User authenticated: ${user.email}")
                Result.success(Unit)
            } else {
                Log.e(TAG, "❌ Sign in succeeded but no user returned")
                Result.failure(Exception("No user after sign in"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error signing in with Google: ${e.message}", e)
            Log.e(TAG, "   Exception type: ${e.javaClass.simpleName}")
            Log.e(TAG, "   Stack trace: ${e.stackTraceToString()}")
            
            val helpfulMessage = when {
                e.message?.contains("invalid", ignoreCase = true) == true -> 
                    "Invalid ID token. This may be a configuration issue with Google Cloud Console."
                e.message?.contains("network", ignoreCase = true) == true -> 
                    "Network error. Check your internet connection."
                e.message?.contains("unauthorized", ignoreCase = true) == true -> 
                    "Unauthorized. The Web Client ID may not be authorized for Supabase."
                else -> e.message ?: "Unknown error"
            }
            
            Log.e(TAG, "   Helpful message: $helpfulMessage")
            Result.failure(Exception(helpfulMessage, e))
        }
    }

    suspend fun signOut(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            supabase.auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error signing out", e)
            Result.failure(e)
        }
    }

    fun getCurrentUserId(): String? {
        return supabase.auth.currentUserOrNull()?.id
    }

    fun getCurrentUserEmail(): String? {
        return supabase.auth.currentUserOrNull()?.email
    }

    fun isSignedIn(): Boolean {
        return supabase.auth.currentUserOrNull() != null
    }

    suspend fun getUserProfile(userId: String, email: String): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val existingProfile = supabase.from(TABLE_USER_PROFILES)
                .select(columns = Columns.ALL) {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeSingleOrNull<UserProfileDto>()

            if (existingProfile != null) {
                Log.d(TAG, "Found existing user profile for $userId")
                return@withContext Result.success(existingProfile.toUserProfile())
            }

            Log.d(TAG, "Creating new user profile for $userId")
            val newProfile = UserProfileDto(
                id = userId,
                email = email,
                messageCount = 0,
                songCount = 0,
                isPremium = false,
                subscriptionStartDate = null,
                songsThisPeriod = 0,
                periodStartDate = null
            )

            supabase.from(TABLE_USER_PROFILES)
                .insert(newProfile)

            Result.success(newProfile.toUserProfile())
        } catch (e: Exception) {
            Log.e(TAG, "Error getting/creating user profile", e)
            Result.failure(e)
        }
    }

    suspend fun incrementMessageCount(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val profile = supabase.from(TABLE_USER_PROFILES)
                .select(columns = Columns.list("message_count")) {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeSingleOrNull<MessageCountDto>()

            val currentCount = profile?.messageCount ?: 0

            supabase.from(TABLE_USER_PROFILES)
                .update({
                    set("message_count", currentCount + 1)
                }) {
                    filter {
                        eq("id", userId)
                    }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error incrementing message count", e)
            Result.failure(e)
        }
    }

    suspend fun incrementSongCount(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val profile = supabase.from(TABLE_USER_PROFILES)
                .select(columns = Columns.list("song_count", "songs_this_period")) {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeSingleOrNull<SongCountDto>()

            val currentSongCount = profile?.songCount ?: 0
            val currentSongsThisPeriod = profile?.songsThisPeriod ?: 0

            supabase.from(TABLE_USER_PROFILES)
                .update({
                    set("song_count", currentSongCount + 1)
                    set("songs_this_period", currentSongsThisPeriod + 1)
                }) {
                    filter {
                        eq("id", userId)
                    }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error incrementing song count", e)
            Result.failure(e)
        }
    }

    suspend fun activatePremium(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val now = System.currentTimeMillis()
            supabase.from(TABLE_USER_PROFILES)
                .update({
                    set("is_premium", true)
                    set("subscription_start_date", now)
                    set("period_start_date", now)
                    set("songs_this_period", 0)
                }) {
                    filter {
                        eq("id", userId)
                    }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error activating premium", e)
            Result.failure(e)
        }
    }

    fun checkSubscriptionRenewal(profile: UserProfile): Boolean {
        if (!profile.isPremium) return false

        val periodStart = profile.periodStartDate?.toLongOrNull() 
            ?: profile.subscriptionStartDate?.toLongOrNull()
            ?: return false
            
        val now = System.currentTimeMillis()
        val daysSinceStart = (now - periodStart) / (1000 * 60 * 60 * 24)

        return daysSinceStart >= 30 || profile.songsThisPeriod >= 50
    }

    suspend fun renewSubscription(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val now = System.currentTimeMillis()
            supabase.from(TABLE_USER_PROFILES)
                .update({
                    set("period_start_date", now)
                    set("songs_this_period", 0)
                }) {
                    filter {
                        eq("id", userId)
                    }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error renewing subscription", e)
            Result.failure(e)
        }
    }

    fun buildSubscription(profile: UserProfile): UserSubscription {
        return UserSubscription(
            isPremium = profile.isPremium,
            messageCount = profile.messageCount,
            songCount = profile.songCount,
            songsThisPeriod = profile.songsThisPeriod,
            subscriptionStartDate = profile.subscriptionStartDate,
            periodStartDate = profile.periodStartDate,
            needsRenewal = checkSubscriptionRenewal(profile)
        )
    }
}

@Serializable
data class UserProfileDto(
    @SerialName("id") val id: String,
    @SerialName("email") val email: String,
    @SerialName("message_count") val messageCount: Int = 0,
    @SerialName("song_count") val songCount: Int = 0,
    @SerialName("is_premium") val isPremium: Boolean = false,
    @SerialName("subscription_start_date") val subscriptionStartDate: String? = null,
    @SerialName("songs_this_period") val songsThisPeriod: Int = 0,
    @SerialName("period_start_date") val periodStartDate: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
) {
    fun toUserProfile(): UserProfile {
        return UserProfile(
            id = id,
            email = email,
            messageCount = messageCount,
            songCount = songCount,
            isPremium = isPremium,
            subscriptionStartDate = subscriptionStartDate,
            songsThisPeriod = songsThisPeriod,
            periodStartDate = periodStartDate,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}

@Serializable
data class MessageCountDto(
    @SerialName("message_count") val messageCount: Int = 0
)

@Serializable
data class SongCountDto(
    @SerialName("song_count") val songCount: Int = 0,
    @SerialName("songs_this_period") val songsThisPeriod: Int = 0
)
*/
