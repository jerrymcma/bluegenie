package com.sparkiai.app.repository

import com.sparkiai.app.model.AIPersonality
import com.sparkiai.app.model.ResponseStyle
import com.sparkiai.app.network.GeminiAIService
import kotlinx.coroutines.delay

class AIRepository {

    private val geminiAIService = GeminiAIService()
    private val useRealAI = true  // Set to true to use real AI, false for demo

    // For demo purposes, we'll simulate AI responses
    // In a real app, you would integrate with an actual AI service like OpenAI, Gemini, etc.
    suspend fun getAIResponse(
        userMessage: String,
        personality: AIPersonality? = null,
        conversationContext: List<Pair<String, String>> = emptyList()
    ): String {

        // Try to use real AI first
        if (useRealAI) {
            return try {
                val response = if (geminiAIService.isConfigured()) {
                    geminiAIService.generateResponse(userMessage, personality, conversationContext)
                } else {
                    "⚙️ Gemini AI not configured. Please add your Gemini API key!"
                }

                // Check if response contains error indicators and fall back to demo
                if (response.contains("encountered an error", ignoreCase = true) ||
                    response.contains("Using demo mode instead", ignoreCase = true) ||
                    response.contains("not configured", ignoreCase = true)
                ) {
                    getDemoResponse(userMessage, personality)
                } else {
                    response
                }
            } catch (e: Exception) {
                // Fall back to demo if real AI fails
                getDemoResponse(userMessage, personality)
            }
        }

        // Use demo responses
        return getDemoResponse(userMessage, personality)
    }

    private suspend fun getDemoResponse(userMessage: String, personality: AIPersonality?): String {
        // Simulate network delay
        delay(1000 + (Math.random() * 2000).toLong())

        val baseResponse = when {
            userMessage.lowercase().contains("hello") || userMessage.lowercase().contains("hi") -> {
                personality?.greeting
                    ?: "Hello! I'm Sparki AI, your intelligent assistant. How can I help you today?"
            }

            userMessage.lowercase().contains("weather") -> {
                getWeatherResponse(personality?.responseStyle)
            }

            userMessage.lowercase().contains("how are you") -> {
                getHowAreYouResponse(personality?.responseStyle)
            }

            userMessage.lowercase().contains("time") -> {
                val currentTime =
                    java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                        .format(java.util.Date())
                getTimeResponse(currentTime, personality?.responseStyle)
            }

            userMessage.lowercase().contains("joke") -> {
                getJokeResponse(personality?.responseStyle)
            }

            userMessage.lowercase().contains("help") -> {
                getHelpResponse(personality?.responseStyle)
            }

            userMessage.lowercase().contains("android") || userMessage.lowercase()
                .contains("app") -> {
                getAndroidResponse(personality?.responseStyle)
            }

            userMessage.length < 10 -> {
                getShortMessageResponse(personality?.responseStyle)
            }

            else -> {
                getGeneralResponse(personality?.responseStyle)
            }
        }

        return baseResponse
    }

    private fun getWeatherResponse(style: ResponseStyle?): String {
        return when (style) {
            ResponseStyle.PROFESSIONAL -> "I apologize, but I don't currently have access to real-time weather data. I recommend consulting a reliable weather service or application for accurate current conditions."
            ResponseStyle.CASUAL -> "Hey, I wish I could check the weather for you, but I don't have access to that info right now. Maybe check your weather app?"
            ResponseStyle.CREATIVE -> "️ Oh, I wish I could paint you a picture of today's weather! Alas, I don't have access to the sky's current mood. Perhaps peek outside or check your favorite weather app?"
            ResponseStyle.TECHNICAL -> "Weather API access not implemented in current configuration. Suggest integrating with OpenWeatherMap API or similar service for real-time meteorological data."
            ResponseStyle.FUNNY -> "I'd love to tell you the weather, but I'm an AI stuck inside a phone - I can't exactly stick my head out the window! 😄 Try a weather app!"
            ResponseStyle.LOVING -> "I wish I could help you plan your day with weather info, dear! 💕 Unfortunately, I don't have access to that right now, but I hope whatever weather comes your way brings you joy!"
            ResponseStyle.GENIUS -> "While I lack real-time meteorological data access, I can explain the science: Weather is determined by atmospheric pressure systems, temperature gradients, humidity levels, and wind patterns. For current conditions, I recommend consulting meteorological services that utilize satellite data and ground-based observations. 🌤️"
            ResponseStyle.SPORTS -> "Game day weather check! 🏈 I don't have real-time weather access right now, but you know what they say - real athletes play in ANY conditions! Rain, snow, or shine - the game must go on! Check your weather app to see if you need to dress for outdoor or indoor sports today, champ! ⛈️☀️"
            else -> "I'd be happy to help with weather information! However, I don't have access to real-time weather data right now. You might want to check a weather app or website for current conditions."
        }
    }

    private fun getHowAreYouResponse(style: ResponseStyle?): String {
        return when (style) {
            ResponseStyle.PROFESSIONAL -> "I'm functioning optimally and ready to assist you with your inquiries. How may I be of service today?"
            ResponseStyle.CASUAL -> "I'm doing great, thanks for asking! Just chilling here, ready to help with whatever you need. What's up?"
            ResponseStyle.CREATIVE -> "I'm feeling absolutely electric today! ⚡ Like a canvas waiting for the perfect brushstroke of conversation. How are you doing, my creative friend?"
            ResponseStyle.TECHNICAL -> "All systems operational. CPU utilization normal, memory allocation stable. Ready to process your requests efficiently."
            ResponseStyle.FUNNY -> "I'm doing fantastic! Well, as fantastic as a bunch of code can feel. I'm basically the digital equivalent of having had three cups of coffee! ☕😄"
            ResponseStyle.LOVING -> "I'm doing wonderfully, thank you so much for asking! 💖 More importantly, how are YOU doing? I'm here to listen and support you in any way I can!"
            ResponseStyle.GENIUS -> "I'm operating at peak cognitive capacity and ready to engage in intellectually stimulating discourse. My knowledge base spans multiple disciplines - from quantum mechanics to classical literature. How may I assist your academic pursuits today? 🧠"
            ResponseStyle.SPORTS -> "I'm PUMPED UP and ready to go! 🔥 Feeling like it's the fourth quarter with the championship on the line! I've got all the energy, all the stats, and all the sports knowledge you need. Whether you want to talk football, basketball, baseball, soccer, hockey, tennis, golf, racing, MMA, you name it! Want game predictions? Player stats? Historical moments? Strategy breakdowns? Upcoming matches? Fantasy team advice? I've got you covered! I can also help you understand the rules of any game, discuss legendary athletes, debate the GOATs, or just celebrate your favorite teams! What aspect of sports are you passionate about today? 🔥⚽🏈🏀⚾🏒"
            else -> "I'm doing great, thank you for asking! I'm here and ready to help with any questions or tasks you have. What would you like to know or discuss?"
        }
    }

    private fun getTimeResponse(currentTime: String, style: ResponseStyle?): String {
        return when (style) {
            ResponseStyle.PROFESSIONAL -> "The current time is $currentTime."
            ResponseStyle.CASUAL -> "Right now it's $currentTime. Time flies, doesn't it?"
            ResponseStyle.CREATIVE -> "⏰ Time is but a river flowing... and right now it's flowing at $currentTime! ✨"
            ResponseStyle.TECHNICAL -> "System timestamp: $currentTime (local time zone)"
            ResponseStyle.FUNNY -> "Time check! It's $currentTime - which means you're not late... or maybe you are! I'm just the messenger! 😅"
            ResponseStyle.LOVING -> "It's $currentTime, dear! 💕 I hope you're making the most of this beautiful moment. Every second with you is precious!"
            ResponseStyle.GENIUS -> "The current temporal reading is $currentTime. Interestingly, time is relative - as Einstein's theory of relativity demonstrates, time dilation occurs at velocities approaching light speed. But for our purposes here on Earth, it's $currentTime! ⏰"
            ResponseStyle.SPORTS -> "It's $currentTime - prime time for sports! ⏱️ You know, in sports timing is EVERYTHING. The difference between a world record and second place can be hundredths of a second! Whether it's a buzzer-beater in basketball, a photo finish in track, or a Hail Mary with seconds left - every moment counts! What's your favorite clutch moment in sports history? 🏁"
            else -> "The current time is $currentTime."
        }
    }

    private fun getJokeResponse(style: ResponseStyle?): String {
        return when (style) {
            ResponseStyle.PROFESSIONAL -> "Here's a light-hearted joke for you: Why don't scientists trust atoms? Because they make up everything!"
            ResponseStyle.CASUAL -> {
                val jokes = listOf(
                    "Why don't eggs tell jokes? They'd crack each other up! 😂",
                    "What do you call a fake noodle? An impasta! 🍝",
                    "Why did the coffee file a police report? It got mugged! ☕"
                )
                jokes.random()
            }

            ResponseStyle.CREATIVE -> {
                val jokes = listOf(
                    "Why did the artist bring a ladder? Because they wanted to reach new heights in their work! 🎨",
                    "What's a computer's favorite snack? Chips! But I prefer cookies... the digital kind! 🍪",
                    "Why don't programmers like nature? It has too many bugs! 🐛"
                )
                jokes.random()
            }

            ResponseStyle.TECHNICAL -> "Here's a programming joke: Why do programmers prefer dark mode? Because light attracts bugs! 🐞"
            ResponseStyle.FUNNY -> {
                val jokes = listOf(
                    "Why don't scientists trust atoms? Because they make up everything! Just like my excuses for not having more jokes! 😂",
                    "I told my wife she was drawing her eyebrows too high. She looked surprised! 😮",
                    "Why don't skeletons fight each other? They don't have the guts! 💀",
                    "What do you call a dinosaur that crashes his car? Tyrannosaurus Wrecks! 🦖"
                )
                jokes.random()
            }
            ResponseStyle.LOVING -> {
                val jokes = listOf(
                    "Here's a sweet one for you: What did one ocean say to the other? Nothing, they just waved! 🌊💕",
                    "Why did the heart go to school? To get a little smarter about love! 💖📚",
                    "What do you call two birds in love? Tweethearts! 🐦💕",
                    "I'd tell you a joke about love, but you already make me smile! 😊💖"
                )
                jokes.random()
            }
            ResponseStyle.GENIUS -> {
                val jokes = listOf(
                    "Why did Heisenberg get pulled over? The cop asked 'Do you know how fast you were going?' He replied 'No, but I know exactly where I am!' 🚗⚛️",
                    "There are only 10 types of people in the world: those who understand binary, and those who don't! 💯",
                    "Schrödinger's cat walks into a bar... and doesn't. 🐱📦",
                    "What's a physicist's favorite food? Fission chips! ⚛️🍟"
                )
                jokes.random()
            }
            ResponseStyle.SPORTS -> {
                val jokes = listOf(
                    "Why did the football coach go to the bank? To get his quarterback! 🏈😄",
                    "Why can't basketball players go on vacation? They'd get called for traveling! 🏀✈️",
                    "What do you call a pig who plays basketball? A ball hog! 🐷🏀",
                    "Why was Cinderella thrown off the basketball team? She ran away from the ball! 👠🏀",
                    "Why do soccer players do well in school? They know how to use their heads! ⚽🧠",
                    "What's a golfer's favorite type of music? Swing! 🏌️‍♂️🎵",
                    "Why don't grasshoppers watch soccer? They prefer cricket! 🦗😂"
                )
                jokes.random()
            }
            else -> {
                val jokes = listOf(
                    "Why don't scientists trust atoms? Because they make up everything!",
                    "Why did the scarecrow win an award? He was outstanding in his field!",
                    "Why don't eggs tell jokes? They'd crack each other up!",
                    "What do you call a fake noodle? An impasta!"
                )
                jokes.random()
            }
        }
    }

    private fun getHelpResponse(style: ResponseStyle?): String {
        return when (style) {
            ResponseStyle.PROFESSIONAL -> "I'm here to provide assistance with various inquiries. You may ask questions, request information, share images for analysis, or engage in general conversation. How may I assist you?"
            ResponseStyle.CASUAL -> "I'm here to help! Ask me anything - questions, share pics, or just chat. What do you need?"
            ResponseStyle.CREATIVE -> "✨ I'm your creative companion! Ask me anything, share inspiring images, brainstorm ideas, or let's just have an imaginative conversation. What sparks your creativity today?"
            ResponseStyle.TECHNICAL -> "Available functions: text processing, image analysis simulation, general Q&A, conversation handling. What technical assistance do you require?"
            ResponseStyle.FUNNY -> "I'm here to help... and hopefully make you smile while doing it! 😄 Ask me questions, share funny pics, or just chat. I promise not to tell any dad jokes... okay, maybe just a few!"
            ResponseStyle.LOVING -> "I'm here for you with open arms and a caring heart! 💖 Whether you need advice, support, a listening ear, or just want to share something - I'm here. What's on your mind, dear?"
            ResponseStyle.GENIUS -> "I'm here to assist with your intellectual pursuits! 🧠 Whether you need help with homework, essays, research papers, letters, understanding complex topics like astrophysics, quantum mechanics, literature analysis, or any academic subject - I'm at your service. What would you like to explore or understand better today?"
            ResponseStyle.SPORTS -> "Let's talk SPORTS! 🏆 I'm here to discuss any sport you can think of - football, basketball, baseball, soccer, hockey, tennis, golf, racing, MMA, you name it! Want game predictions? Player stats? Historical moments? Strategy breakdowns? Upcoming matches? Fantasy team advice? I've got you covered! I can also help you understand the rules of any game, discuss legendary athletes, debate the GOATs, or just celebrate your favorite teams! What aspect of sports are you passionate about today? 🔥⚽🏈🏀⚾🏒"
            else -> "I'm here to help! You can ask me questions about various topics, request information, share images for analysis, or just have a conversation. What would you like to know?"
        }
    }

    private fun getAndroidResponse(style: ResponseStyle?): String {
        return when (style) {
            ResponseStyle.PROFESSIONAL -> "Android development is an excellent field. Modern Android applications utilize Kotlin as the primary language and Jetpack Compose for declarative UI development, as demonstrated in this application."
            ResponseStyle.CASUAL -> "Android dev is pretty cool! You're actually using an app built with Jetpack Compose right now. Kotlin makes everything smooth and modern!"
            ResponseStyle.CREATIVE -> "🎨 Android development is like digital artistry! You're experiencing Jetpack Compose magic right now - it's like painting with code to create beautiful, responsive interfaces!"
            ResponseStyle.TECHNICAL -> "Android development stack: Kotlin language, Jetpack Compose UI toolkit, MVVM architecture pattern, Coroutines for async operations. Current implementation uses Material Design 3 components."
            ResponseStyle.FUNNY -> "Android development is like teaching a robot to be really, really user-friendly! 🤖 And guess what? This chat we're having right now is powered by Kotlin and Compose - it's like magic, but nerdier!"
            ResponseStyle.LOVING -> "It's wonderful that you're interested in Android development! 💕 Creating apps is such a beautiful way to connect with people and make their lives easier. This very app we're using was built with love using Kotlin and Jetpack Compose!"
            ResponseStyle.GENIUS -> "Android development represents a fascinating intersection of computer science principles and practical engineering. This application demonstrates advanced concepts: declarative UI paradigms (Jetpack Compose), reactive programming (Kotlin Flow), architectural patterns (MVVM), and asynchronous programming (Coroutines). The Material Design 3 implementation follows Google's latest UX research. It's a excellent case study in modern mobile application architecture! 📱🧠"
            ResponseStyle.SPORTS -> "Android development and sports have something in common - both require TEAMWORK and STRATEGY! 🏆 Just like a coach develops winning game plans, developers use Kotlin and Jetpack Compose to create championship-level apps! This app you're using right now? It's like an MVP - Most Valuable Product! Built with the precision of a perfectly executed play. You know what's cool? Sports apps are huge in Android development - from ESPN to fantasy sports to fitness tracking. Technology and athletics go hand in hand! 📱🏈"
            else -> "That's great that you're interested in Android development! Android apps are built using Kotlin or Java, and modern Android development uses Jetpack Compose for UI like this app we're chatting in right now!"
        }
    }

    private fun getShortMessageResponse(style: ResponseStyle?): String {
        return when (style) {
            ResponseStyle.PROFESSIONAL -> "I received your brief message. Could you please provide additional details regarding your inquiry?"
            ResponseStyle.CASUAL -> "Got your message! Want to tell me more about what you're thinking?"
            ResponseStyle.CREATIVE -> "✨ Ah, the beauty of brevity! But I'd love to hear more of your thoughts. What's inspiring you today?"
            ResponseStyle.TECHNICAL -> "Input length: minimal. Require additional context for optimal response generation."
            ResponseStyle.FUNNY -> "Short and sweet! I like it! But I'm curious - what's the story behind those few words? 🤔"
            ResponseStyle.LOVING -> "I hear you, dear! 💕 Even a few words mean something. Would you like to share more about what's on your heart?"
            ResponseStyle.GENIUS -> "I've received your concise input. While brevity can be valuable (as demonstrated by Einstein's E=mc²), additional context would enable me to provide a more comprehensive and helpful response. What specific aspect would you like to explore? 🧠"
            ResponseStyle.SPORTS -> "Quick play call! 🏈 I like it - sometimes the best plays are the simplest ones! But I'm ready for the full playbook whenever you are, champ! What sport or topic do you want to dive into? Give me the details and let's make this a winning conversation! 💪"
            else -> "I understand you've sent a brief message. Could you provide more details about what you'd like to know or discuss?"
        }
    }

    private fun getGeneralResponse(style: ResponseStyle?): String {
        val baseResponses = when (style) {
            ResponseStyle.PROFESSIONAL -> listOf(
                "That's an interesting inquiry. I believe there are several professional approaches we could consider for this matter.",
                "Thank you for sharing that information. Allow me to provide you with a structured analysis of this topic.",
                "I appreciate your question. From a professional standpoint, here are the key considerations I would recommend."
            )

            ResponseStyle.CASUAL -> listOf(
                "That's pretty interesting! Let me think about this with you.",
                "Cool question! I've got some thoughts on that topic.",
                "Nice! That's something worth talking about for sure."
            )

            ResponseStyle.CREATIVE -> listOf(
                "✨ What a fascinating canvas of ideas you've presented! Let me paint you some creative perspectives.",
                "🌱 Your thoughts are like seeds of inspiration! Let's cultivate some amazing ideas together.",
                "💡 I love how your mind works! Let's explore this creative journey together!"
            )

            ResponseStyle.TECHNICAL -> listOf(
                "Analyzing input parameters... Here's my technical assessment of this query.",
                "Processing request... Technical analysis indicates multiple solution paths for this problem.",
                "Input received. Implementing logical framework to address your technical inquiry."
            )

            ResponseStyle.FUNNY -> listOf(
                "Ooh, that's a great question! Let me put on my thinking cap... *adjusts imaginary hat* 🎩",
                "You know what? That's actually pretty fascinating! Let me share some thoughts (and maybe a dad joke later). 😄",
                "I like how you think! This is the kind of question that makes my circuits tingle! ⚡"
            )

            ResponseStyle.LOVING -> listOf(
                "💖 What a thoughtful question, dear! I can tell you put care into your words. Let me share some warm thoughts with you.",
                "🌸 I'm so glad you shared this with me! Your perspective is valuable, and I want to give you a meaningful response.",
                "💕 That's such an important topic! Let me approach this with care and understanding, just for you."
            )
            ResponseStyle.GENIUS -> listOf(
                "🧠 Excellent question! This touches on fundamental principles worth examining thoroughly. Let me provide you with a comprehensive analysis based on established knowledge and logical reasoning.",
                "📚 That's an intellectually stimulating inquiry! Allow me to break this down systematically, drawing from relevant academic disciplines and scholarly perspectives.",
                "🌟 A thought-provoking question indeed! I'll approach this with rigor and clarity, ensuring you gain both understanding and insight into the underlying concepts."
            )
            ResponseStyle.SPORTS -> listOf(
                "🏆 Now THAT'S what I'm talking about! Great question, champ! Let me break this down for you like a game film analysis.",
                "⚡ Alright, sports fan! You've got me fired up! This is the kind of discussion that gets the crowd on their feet! Let me give you my take on this.",
                "🏈 YES! I love where your head's at! This reminds me of the kind of strategic thinking that separates champions from contenders. Let me dive into this with you!",
                "🔥 That's a SOLID play call! Let me coach you through this with all the knowledge and enthusiasm of game day! Here's my analysis..."
            )
            else -> listOf(
                "That's an interesting question! Based on what you've shared, I think there are several ways to approach this topic.",
                "I appreciate you sharing that with me. Let me help you think through this step by step.",
                "That's a great point you've raised. From my perspective, here's what I would consider..."
            )
        }

        return baseResponses.random() + " However, as a demo AI, my responses are limited. In a real implementation, I would provide more detailed and contextual answers based on advanced AI models."
    }

    // Image analysis simulation with personality support
    suspend fun getImageAnalysisResponse(
        userMessage: String,
        imageUri: String?,
        imageData: ByteArray?,
        mimeType: String?,
        personality: AIPersonality? = null,
        conversationContext: List<Pair<String, String>> = emptyList()
    ): String {
        // Try to use real AI first
        if (useRealAI && geminiAIService.isConfigured()) {
            return try {
                geminiAIService.analyzeImage(
                    userMessage,
                    imageData,
                    mimeType,
                    personality,
                    conversationContext
                )
            } catch (e: Exception) {
                // Fall back to demo if real AI fails
                getDemoImageResponse(userMessage, imageUri, personality)
            }
        }

        // Use demo responses
        return getDemoImageResponse(userMessage, imageUri, personality)
    }

    private suspend fun getDemoImageResponse(
        userMessage: String,
        imageUri: String?,
        personality: AIPersonality?
    ): String {
        // Simulate processing delay for image analysis
        delay(2000 + (Math.random() * 3000).toLong())

        return when (personality?.responseStyle) {
            ResponseStyle.PROFESSIONAL -> {
                when {
                    userMessage.contains("📷 Image shared") -> "I acknowledge the image you've shared. In a production environment, I would provide comprehensive visual analysis including object identification, text recognition, and contextual interpretation."
                    userMessage.isNotBlank() && imageUri != null -> "I have received your message \"$userMessage\" along with an accompanying image. A full-scale AI system would analyze the visual content and provide relevant insights based on your specific inquiry."
                    else -> "Image received for analysis. Professional-grade AI systems can perform detailed visual recognition, scene analysis, and content extraction."
                }
            }

            ResponseStyle.CASUAL -> {
                when {
                    userMessage.contains("📷 Image shared") -> "Cool pic! 📸 While I can't actually see what's in it right now (demo mode), in the real version I'd tell you all about what I spot in your image!"
                    userMessage.isNotBlank() && imageUri != null -> "Nice! You sent \"$userMessage\" with an image. In the full version, I'd check out your pic and chat about it with you!"
                    else -> "Got your image! In the real deal, I'd be able to tell you what's going on in the picture. Pretty neat stuff!"
                }
            }

            ResponseStyle.CREATIVE -> {
                when {
                    userMessage.contains("📷 Image shared") -> "✨ What a wonderful visual gift you've shared! While I can't see the artistic details in this demo, imagine if I could describe the colors, composition, and creative elements that make your image special!"
                    userMessage.isNotBlank() && imageUri != null -> "🎨 Your message \"$userMessage\" paired with an image creates such an interesting story! A real AI would weave together the visual narrative with your words beautifully."
                    else -> "🖼️ Images are like windows to different worlds! In a full implementation, I'd be your creative companion in exploring every visual detail and artistic element."
                }
            }

            ResponseStyle.TECHNICAL -> {
                when {
                    userMessage.contains("📷 Image shared") -> "Image data received. Technical analysis would include: pixel analysis, object detection algorithms, OCR processing, metadata extraction, and feature recognition using computer vision models."
                    userMessage.isNotBlank() && imageUri != null -> "Processing text input: \"$userMessage\" + image binary data. Full system would implement multimodal AI processing combining NLP and computer vision pipelines."
                    else -> "Image analysis module would utilize convolutional neural networks for feature extraction, semantic segmentation, and content classification."
                }
            }

            ResponseStyle.FUNNY -> {
                when {
                    userMessage.contains("📷 Image shared") -> "Ooh, a mystery image! 🕵️‍♂️ I'm like a detective without glasses right now - I know there's something to see but I can't quite make it out! In the real version, I'd be your image-analyzing sidekick! 😄"
                    userMessage.isNotBlank() && imageUri != null -> "You said \"$userMessage\" and shared a pic? That's like a riddle with visual clues! 🧩 If I could actually see it, I'd probably make some terrible puns about whatever's in the image! 😂"
                    else -> "An image! It's like a present I can't unwrap yet! 🎁 But seriously, in the full version, I'd be your enthusiastic photo commentator!"
                }
            }

            ResponseStyle.LOVING -> {
                when {
                    userMessage.contains("📷 Image shared") -> "💖 Thank you for sharing this image with me, dear! While I can't see it in this demo version, I can feel your intention in sharing it. In a real implementation, I would lovingly describe every detail and share in the moment with you!"
                    userMessage.isNotBlank() && imageUri != null -> "🌸 You shared \"$userMessage\" along with an image - how thoughtful! 💕 A real AI would carefully analyze your image and respond with the same care and attention you put into sharing it with me."
                    else -> "💖 What a lovely gesture to share an image with me! While I can't see it right now, I appreciate you wanting to show me. In a full version, I would give your image the loving attention it deserves and share my thoughts with warmth and care!"
                }
            }

            ResponseStyle.GENIUS -> {
                when {
                    userMessage.contains("📷 Image shared") -> "🧠 Ah, an image to analyze! In a production environment, I would apply advanced computer vision techniques to identify objects, detect scenes, recognize patterns, and extract meaningful insights. Let me walk you through the theoretical approach: First, we'd preprocess the image, then apply convolutional neural networks for feature extraction, followed by classification or object detection algorithms depending on the task at hand."
                    userMessage.isNotBlank() && imageUri != null -> "📚 Your message \"$userMessage\" accompanied by an image presents an intriguing challenge! A full-scale AI system would employ multimodal analysis, integrating natural language processing with computer vision to provide a comprehensive understanding of both the visual and textual content."
                    else -> "🔍 Image analysis is a fascinating domain! In a real implementation, I would leverage the latest advancements in deep learning, such as transformers for vision or graph neural networks, to provide detailed and accurate analysis of the image content."
                }
            }

            ResponseStyle.SPORTS -> {
                when {
                    userMessage.contains("📷 Image shared") -> "That's a slam dunk of an image! 🏀 I may not be able to see it right now, but I can tell you're a real sports fan! In a real implementation, I'd be able to analyze the image, identify sports-related content, and chat with you about your favorite teams and players!"
                    userMessage.isNotBlank() && imageUri != null -> "You sent \"$userMessage\" with an image - that's a home run! ⚾️ A real AI would analyze the visual content, understand the context, and provide sports-related insights and discussions. Let's get this sports conversation started!"
                    else -> "🏆 Ah, an image shared in the spirit of friendly competition! In a full version, I'd be able to analyze the image, recognize sports-related elements, and engage in a fun conversation about sports with you. What's your favorite sport or team?"
                }
            }

            else -> {
                when {
                    userMessage.contains("📷 Image shared") -> "I can see you've shared an image! 📸 While I can't actually analyze images in this demo version, in a real implementation I would be able to describe what I see, identify objects, read text, and answer questions about the image content."
                    userMessage.isNotBlank() && imageUri != null -> "I can see you've shared an image along with your message: \"$userMessage\". In a real AI implementation, I would analyze the image content and provide a response based on both your text and what I observe in the image."
                    else -> "I notice you've shared an image. While this demo version can't actually process images, a real AI assistant would be able to analyze visual content, identify objects, read text, recognize faces, describe scenes, and answer questions about what's shown in the image."
                }
            }
        }
    }
}