package com.bluegenie.app.config

/**
 * Feature flags for experimental or optional features
 * Change these values to enable/disable features without code changes
 */
object FeatureFlags {

    /**
     * 🎵 Enable/Disable Lyria Music Generation
     *
     * Set to TRUE to enable actual music file generation!
     * Set to FALSE to use lyrics-only mode (free)
     *
     * When TRUE: Music Composer can generate actual music files via Lyria API
     * When FALSE: Music Composer only provides lyrics, chords, and guidance
     *
     * Cost Impact:
     * - FALSE: $0 (uses existing Gemini)
     * - TRUE: $0.06 per 30-second music track generated (after free tier)
     *
     * How to toggle:
     * 1. Change this value to true/false
     * 2. Rebuild app
     * 3. That's it! No other code changes needed
     */
    const val ENABLE_LYRIA_MUSIC_GENERATION = false  // Disabled - using Replicate instead

    enum class MusicProvider {
        LYRIA,
        SUNO,
        REPLICATE
    }

    /**
     * Lyria API Configuration
     * Only used when ENABLE_LYRIA_MUSIC_GENERATION = true
     */
    object LyriaConfig {
        // Your Google Cloud project ID (must match service account JSON)
        // Get from: https://console.cloud.google.com/
        const val PROJECT_ID = "gen-lang-client-0580949460"  // Matches service account JSON

        // Vertex AI region (usually "us-central1")
        const val LOCATION = "us-central1"

        // Lyria model name
        const val MODEL_NAME = "lyria-002"

        // Music generation length (~30-60 seconds per generation)
        const val DURATION_SECONDS = 60

        // Whether to show beta/experimental UI indicators
        const val SHOW_BETA_BADGE = true

        // Sample rate for generated audio (Lyria uses 48kHz)
        const val SAMPLE_RATE = 48000

        // Audio format
        const val AUDIO_FORMAT = "wav"
    }

    /**
     * 💰 Premium Subscription Model Configuration
     * Matches Web App monetization:
     * - 5 free songs with Google login
     * - $5/month Premium: 50 songs + all personalities unlocked
     */
    object PremiumConfig {
        // Number of free songs per user (requires Google sign-in)
        const val FREE_SONGS_LIMIT = 5

        // Premium subscription details
        const val PREMIUM_SONGS_PER_PERIOD = 50
        const val PREMIUM_PERIOD_DAYS = 30
        const val PREMIUM_PRICE_USD = 5.00

        // Show remaining free songs counter
        const val SHOW_FREE_SONGS_COUNTER = true

        // Require sign-in for song generation (to track free songs)
        const val REQUIRE_SIGNIN_FOR_SONGS = true

        // Free personalities (available without premium)
        val FREE_PERSONALITIES = setOf("default", "music_composer")
    }

    /**
     * Music Composer Feature Variations
     */
    object MusicComposerConfig {
        val ACTIVE_MUSIC_PROVIDER = MusicProvider.REPLICATE

        private val isLyriaProvider = ACTIVE_MUSIC_PROVIDER == MusicProvider.LYRIA
        private val isReplicateProvider = ACTIVE_MUSIC_PROVIDER == MusicProvider.REPLICATE

        val IS_MUSIC_GENERATION_ENABLED = if (isLyriaProvider) {
            ENABLE_LYRIA_MUSIC_GENERATION
        } else {
            true
        }

        // Show "Generate Music" button when music generation is enabled
        val SHOW_GENERATE_MUSIC_BUTTON = IS_MUSIC_GENERATION_ENABLED

        val SUPPORTS_LONG_LYRICS = ACTIVE_MUSIC_PROVIDER == MusicProvider.SUNO

        // Minimax Music 1.5 via Replicate: 600 char max for lyrics field
        // Suno: 2000 char max
        // Lyria: 600 char max
        val MAX_PROMPT_CHARACTERS = when (ACTIVE_MUSIC_PROVIDER) {
            MusicProvider.REPLICATE -> 600  // Minimax Music 1.5 limit
            MusicProvider.SUNO -> 2000      // Suno allows longer lyrics
            MusicProvider.LYRIA -> 600      // Lyria limit
        }

        val DEFAULT_TRACK_DURATION_SECONDS = when (ACTIVE_MUSIC_PROVIDER) {
            MusicProvider.SUNO -> 90
            MusicProvider.REPLICATE -> 120
            MusicProvider.LYRIA -> LyriaConfig.DURATION_SECONDS
        }

        // Show disclaimer about music generation
        const val SHOW_EXPERIMENTAL_DISCLAIMER = true

        // Allow users to download generated music
        const val ALLOW_MUSIC_DOWNLOAD = true

        // Show music generation cost estimate to users
        const val SHOW_COST_ESTIMATE = true

        // Enable audio playback in-app
        const val ENABLE_IN_APP_PLAYBACK = true

        // Show music library/history
        const val SHOW_MUSIC_LIBRARY = true

        // Maximum songs to store in library
        const val MAX_LIBRARY_SONGS = 50
    }
}

