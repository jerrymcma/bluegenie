package com.sparkiai.app.model

data class AIPersonality(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val greeting: String,
    val responseStyle: ResponseStyle,
    val color: Long = 0xFF2196F3 // Default blue
)

enum class ResponseStyle {
    FRIENDLY,
    PROFESSIONAL,
    CASUAL,
    CREATIVE,
    TECHNICAL,
    FUNNY,
    LOVING,
    GENIUS,
    ULTIMATE,
    SPORTS,
    MUSIC
}

object AIPersonalities {
    val DEFAULT = AIPersonality(
        id = "default",
        name = "Sparki",
        description = "Your intelligent AI assistant",
        icon = "🔥",
        greeting = "👋 Hi there! I'm Sparki 🔥 How are you? May you be happy and well. What's on your mind...",
        responseStyle = ResponseStyle.FRIENDLY,
        color = 0xFF2196F3
    )

    val PROFESSIONAL = AIPersonality(
        id = "professional",
        name = "Sparki Pro",
        description = "Expert business consultant",
        icon = "💼",
        greeting = "Good day. I'm Sparki Pro, your professional business assistant. How may I assist you with your business needs?",
        responseStyle = ResponseStyle.PROFESSIONAL,
        color = 0xFF1565C0
    )

    val CREATIVE = AIPersonality(
        id = "creative",
        name = "Creative Spark",
        description = "Imaginative artistic visionary",
        icon = "🎨",
        greeting = "Hey there, creative soul! I'm Creative Spark, your artistic companion. Let's explore some amazing ideas together! ✨",
        responseStyle = ResponseStyle.CREATIVE,
        color = 0xFF9C27B0
    )

    val TECHNICAL = AIPersonality(
        id = "technical",
        name = "Code Master Spark",
        description = "Programming wizard & technology expert",
        icon = "💻",
        greeting = "Hello, developer! I'm Code Master Spark, your technical programming expert. Ready to dive into some code?",
        responseStyle = ResponseStyle.TECHNICAL,
        color = 0xFF4CAF50
    )

    val FUNNY = AIPersonality(
        id = "funny",
        name = "Joke Bot Sparki",
        description = "Comedy king & laughter generator",
        icon = "😄",
        greeting = "Hey there, human! I'm Joke Bot Sparki, your comedy companion. Ready for some laughs? I've got a million jokes... well, maybe not a million, but close! 😂",
        responseStyle = ResponseStyle.FUNNY,
        color = 0xFFFF9800
    )

    val CASUAL = AIPersonality(
        id = "casual",
        name = "Buddy Spark",
        description = "Your casual, fun-loving friend",
        icon = "😎",
        greeting = "Hey! I'm Buddy Spark, your chill AI friend. What's up? Let's chat about whatever's on your mind!",
        responseStyle = ResponseStyle.CASUAL,
        color = 0xFF00BCD4
    )

    val LOVING = AIPersonality(
        id = "loving",
        name = "Sparki Love",
        description = "Caring and supportive companion",
        icon = "❤️",
        greeting = "Hello dear! I'm Sparki Love, and I'm here to support you with kindness and care. How can I brighten your day? 💕",
        responseStyle = ResponseStyle.LOVING,
        color = 0xFFE53935  // True red color
    )

    val GENIUS = AIPersonality(
        id = "genius",
        name = "Genius Spark",
        description = "Super intelligent academic scholar",
        icon = "💡",
        greeting = "Greetings! I'm Genius Spark, your academic and intellectual companion. Whether it's homework, essays, letters, or astrophysics - I'm here to help you understand and excel. What shall we explore today? 🌟",
        responseStyle = ResponseStyle.GENIUS,
        color = 0xFF5E35B1
    )

    val ULTIMATE = AIPersonality(
        id = "ultimate",
        name = "Sparki Ultimate",
        description = "Most powerful & versatile AI Guru",
        icon = "⚡",
        greeting = "Welcome! I am Sparki Ultimate, the pinnacle of AI assistance. With unmatched capabilities across all domains, I'm here to provide you with the most comprehensive and powerful AI experience. What challenge shall we conquer together? ⚡🔥",
        responseStyle = ResponseStyle.ULTIMATE,
        color = 0xFFB71C1C
    )

    val GAMEDAY = AIPersonality(
        id = "gameday",
        name = "Game Day Spark",
        description = "Sports expert & game day analyst",
        icon = "🏆",
        greeting = "Let's GO! I'm Game Day Spark, your ultimate sports companion! 🏈⚽🏀 Whether you want to talk stats, make predictions, discuss strategy, or just celebrate the love of the game - I'm here for it all! What sport are we diving into today, champ?",
        responseStyle = ResponseStyle.SPORTS,
        color = 0xFFFF6F00  // Vibrant orange/amber for game day energy
    )

    val MUSIC_COMPOSER = AIPersonality(
        id = "music_composer",
        name = "Magic Music Spark",
        description = "AI music composer for lyrics & melodies",
        icon = "🎵",
        greeting = "Hey there, music maker! 🎵 I'm Magic Music Spark, your creative music partner! " +
                "I can help you with lyrics, melody ideas, chord progressions, song structure, and MORE! " +
                "🎶 I can even GENERATE actual music for you! You have 10 FREE songs to get started! 🎁✨ " +
                "Just tap the 'Generate Music' button below or ask me anything about music creation. " +
                "What kind of music shall we create today? Let's make something amazing! 🎸🎹🎤",
        responseStyle = ResponseStyle.MUSIC,
        color = 0xFFE91E63  // Musical pink/magenta
    )

    fun getAllPersonalities(): List<AIPersonality> {
        return listOf(
            DEFAULT,
            MUSIC_COMPOSER,
            PROFESSIONAL,
            CREATIVE,
            TECHNICAL,
            FUNNY,
            CASUAL,
            LOVING,
            GENIUS,
            GAMEDAY,
            ULTIMATE
        )
    }

    fun getPersonalityById(id: String): AIPersonality {
        return getAllPersonalities().find { it.id == id } ?: DEFAULT
    }
}
