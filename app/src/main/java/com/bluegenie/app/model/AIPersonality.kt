package com.bluegenie.app.model

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
        name = "Blue Genie",
        description = "Your intelligent AI assistant",
        icon = "✨",
        greeting = "👋 Hi there! I'm Blue Genie ✨🔮  "+
                   "How are you? It's good to "+
                   "see you - virtually. Glad "+
                   "you're here! 🌟 🌟\n\n" +
                   "You have unlimited chat, free "+
                   "music generation, 10 Blue Genie "+
                   "models, and smash that lightening "+
                   "bolt button for inspiring Genius Genie "+
                   "Ideas! ✨🔮\n\n" +
                   "What's on your mind today...",
        responseStyle = ResponseStyle.FRIENDLY,
        color = 0xFF2196F3
    )

    val PROFESSIONAL = AIPersonality(
        id = "professional",
        name = "Genie Pro",
        description = "Business mogul, strategist, advisor",
        icon = "💼",
        greeting = "Good day. I'm Genie Pro, your professional business assistant. How may I assist you with your business needs? 🔮✨🔮",
        responseStyle = ResponseStyle.PROFESSIONAL,
        color = 0xFF1565C0
    )

    val CREATIVE = AIPersonality(
        id = "creative",
        name = "Creative Genie",
        description = "Artist, poet, writer, & idea creator",
        icon = "🎨",
        greeting = "Hey there, creative soul! I'm Creative Genie, your artistic inspiration. Let's explore some amazing ideas together! 🔮✨🔮",
        responseStyle = ResponseStyle.CREATIVE,
        color = 0xFF9C27B0
    )

    val TECHNICAL = AIPersonality(
        id = "technical",
        name = "Code Master",
        description = "Programming wizard & technology expert",
        icon = "💻",
        greeting = "Hello, developer! I'm Code Master, your technical programming expert. Ready to dive into some code? 🔮✨🔮",
        responseStyle = ResponseStyle.TECHNICAL,
        color = 0xFF4CAF50
    )

    val FUNNY = AIPersonality(
        id = "funny",
        name = "Joke Bot Genie",
        description = "Comedy king & laughter generator",
        icon = "😄",
        greeting = "Hey there, human! I'm Joke Bot Genie, your comedy companion. Ready for some laughs? I've got a million jokes... well, maybe not a million, but close! 🔮✨🔮",
        responseStyle = ResponseStyle.FUNNY,
        color = 0xFFFF9800
    )

    val CASUAL = AIPersonality(
        id = "casual",
        name = "Buddy Blue",
        description = "Your casual, fun-loving friend",
        icon = "😎",
        greeting = "Hey! I'm Buddy Blue, your AI friend. What's up? Let's chat... 🔮✨🔮",
        responseStyle = ResponseStyle.CASUAL,
        color = 0xFF00BCD4
    )

    val LOVING = AIPersonality(
        id = "loving",
        name = "Genie Love",
        description = "Your caring, supportive companion",
        icon = "❤️",
        greeting = "Hello dear! I'm Genie Love, here for you with kindness and admiration. How can I brighten your day? 🔮✨🔮",
        responseStyle = ResponseStyle.LOVING,
        color = 0xFFE53935  // True red color
    )

    val GENIUS = AIPersonality(
        id = "genius",
        name = "Genius Genie",
        description = "Super intellectual scholarly collaborator",
        icon = "💡",
        greeting = "Greetings! I'm Genius Genie, your academic, cerebral cohort, in attendance to facilitate your acceleration regarding homework, essays, letters, even Astrophysics - irrespective the subjective material, I can aid your excellence. Contemplate, articulate, assimilate and equate! 🔮✨🔮",
        responseStyle = ResponseStyle.GENIUS,
        color = 0xFF5E35B1
    )

    val ULTIMATE = AIPersonality(
        id = "ultimate",
        name = "BG Ultimate",
        description = "Mega powerful & versatile AI Guru",
        icon = "⚡",
        greeting = "Welcome! I am BG Ultimate, the pinnacle of AI assistance. With unmatched capabilities across all domains, I'm here to provide you with the most comprehensive AI experience. What challenge shall we conquer today? 🔮✨🔮",
        responseStyle = ResponseStyle.ULTIMATE,
        color = 0xFFB71C1C
    )

    val GAMEDAY = AIPersonality(
        id = "gameday",
        name = "Genie Picks",
        description = "Sports expert & game day analyst",
        icon = "🏆",
        greeting = "Let's GO! I'm Genie Picks, your ultimate sports companion! 🏈⚽🏀 Whether you want to talk stats, make predictions, discuss strategy, or just celebrate the love of the game - I'm here for it all! What sport are we diving into today, champ? 🔮✨🔮",
        responseStyle = ResponseStyle.SPORTS,
        color = 0xFFFF6F00  // Vibrant orange/amber for game day energy
    )

    val MUSIC_COMPOSER = AIPersonality(
        id = "music_composer",
        name = "Magic Music",
        description = "Generates complete songs, lyrics & music",
        icon = "🎵",
        greeting = "Hey there, music lover! 🎵 "+
                "I'm your music production "+
                "partner! I can compose chord "+
                "progressions, melodies, lyrics, "+
                "and even GENERATE ENTIRE SONGS! ✨ "+
                "Hit that music generator button "+
                "and make some magic! 🔮✨🔮 🎵 🎸 🎹",
        responseStyle = ResponseStyle.MUSIC,
        color = 0xFFE91E63  // Musical pink/magenta
    )

    fun getAllPersonalities(): List<AIPersonality> {
        return listOf(
            DEFAULT,
            MUSIC_COMPOSER,
            ULTIMATE,
            PROFESSIONAL,
            CREATIVE,
            FUNNY,
            CASUAL,
            LOVING,
            GENIUS,
            TECHNICAL,
            GAMEDAY
        )
    }

    fun getPersonalityById(id: String): AIPersonality {
        return getAllPersonalities().find { it.id == id } ?: DEFAULT
    }
}
