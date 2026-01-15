package com.bluegenie.app.viewmodel

import android.app.Application
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluegenie.app.config.FeatureFlags
import com.bluegenie.app.config.FeatureFlags.MusicProvider
import com.bluegenie.app.model.AIPersonalities
import com.bluegenie.app.model.AIPersonality
import com.bluegenie.app.model.GeneratedMusic
import com.bluegenie.app.model.Message
import com.bluegenie.app.model.MessageType
import com.bluegenie.app.model.ResponseStyle
import com.bluegenie.app.model.UserSubscription
// import com.bluegenie.app.network.LyriaService  // Disabled - using Replicate instead
import com.bluegenie.app.network.SupabaseService
import com.bluegenie.app.network.MusicGenerationResult
import com.bluegenie.app.network.ReplicateService
import com.bluegenie.app.network.SunoService
import com.bluegenie.app.repository.AIRepository
import com.bluegenie.app.utils.ChatMemoryManager
import com.bluegenie.app.utils.MusicGenerationTracker
import com.bluegenie.app.utils.MusicLibraryManager
import com.bluegenie.app.utils.MusicUsageStats
import com.bluegenie.app.utils.MusicPlayer
import com.bluegenie.app.utils.StripeCheckoutHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val aiRepository: AIRepository
    private var memoryManager: ChatMemoryManager? = null
    private var musicLibraryManager: MusicLibraryManager? = null
    private var musicTracker: MusicGenerationTracker? = null
    private val replicateService: ReplicateService = ReplicateService()
    // private val lyriaService: LyriaService = LyriaService()  // Disabled - using Replicate instead
    private val sunoService: SunoService = SunoService()
    private var musicPlayer: MusicPlayer? = null


    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _shouldSpeakResponse = MutableStateFlow(false)
    val shouldSpeakResponse: StateFlow<Boolean> = _shouldSpeakResponse.asStateFlow()

    private val _lastAIResponse = MutableStateFlow("")
    val lastAIResponse: StateFlow<String> = _lastAIResponse.asStateFlow()

    private val _currentPersonality = MutableStateFlow(AIPersonalities.DEFAULT)
    val currentPersonality: StateFlow<AIPersonality> = _currentPersonality.asStateFlow()

    private val _availablePersonalities = MutableStateFlow(AIPersonalities.getAllPersonalities())
    val availablePersonalities: StateFlow<List<AIPersonality>> =
        _availablePersonalities.asStateFlow()

    private var applicationContext: Context? = null

    // Music generation state
    private val _isMusicGenerating = MutableStateFlow(false)
    val isMusicGenerating: StateFlow<Boolean> = _isMusicGenerating.asStateFlow()

    private val _musicUsageStats = MutableStateFlow<MusicUsageStats?>(null)
    val musicUsageStats: StateFlow<MusicUsageStats?> = _musicUsageStats.asStateFlow()

    private val _generatedMusicLibrary = MutableStateFlow<List<GeneratedMusic>>(emptyList())
    val generatedMusicLibrary: StateFlow<List<GeneratedMusic>> =
        _generatedMusicLibrary.asStateFlow()

    private val _currentlyPlayingMusic = MutableStateFlow<GeneratedMusic?>(null)
    val currentlyPlayingMusic: StateFlow<GeneratedMusic?> = _currentlyPlayingMusic.asStateFlow()

    private val _isMusicPlaying = MutableStateFlow(false)
    val isMusicPlaying: StateFlow<Boolean> = _isMusicPlaying.asStateFlow()

    // Subscription state
    private var supabaseService: SupabaseService? = null
    
    private val _subscription = MutableStateFlow(UserSubscription())
    val subscription: StateFlow<UserSubscription> = _subscription.asStateFlow()
    
    private val _showSignInModal = MutableStateFlow(false)
    val showSignInModal: StateFlow<Boolean> = _showSignInModal.asStateFlow()
    
    private val _showUpgradeModal = MutableStateFlow(false)
    val showUpgradeModal: StateFlow<Boolean> = _showUpgradeModal.asStateFlow()
    
    init {
        val context = getApplication<Application>().applicationContext
        aiRepository = AIRepository(context)
        initialize(context)
    }

    /**
     * Initialize the ChatViewModel with a context for memory management and music features
     */
    fun initialize(context: Context) {
        if (memoryManager == null) {
            memoryManager = ChatMemoryManager(context)
            musicLibraryManager = MusicLibraryManager(context)
            musicTracker = MusicGenerationTracker(context)
            musicPlayer = MusicPlayer(context)
            applicationContext = context.applicationContext
            
            // Initialize Supabase service for subscription management
            supabaseService = SupabaseService(context)
            
            // Check if user is already signed in
            viewModelScope.launch {
                checkExistingSignIn()
            }

            // Suno service doesn't require initialization

            // Observe music player state
            viewModelScope.launch {
                musicPlayer?.isPlaying?.collect { playing ->
                    _isMusicPlaying.value = playing
                }
            }

            // Load messages for the current personality
            loadMessagesForCurrentPersonality()

            // Load music usage stats if music generation is enabled
            if (FeatureFlags.MusicComposerConfig.ACTIVE_MUSIC_PROVIDER != null) {
                updateMusicUsageStats()
                loadMusicLibrary()
            }
        }
    }

    /**
     * Update music usage statistics
     */
    private fun updateMusicUsageStats() {
        musicTracker?.let { tracker ->
            _musicUsageStats.value = tracker.getUsageStats()
        }
    }

    /**
     * Load music library
     */
    private fun loadMusicLibrary() {
        musicLibraryManager?.let { manager ->
            _generatedMusicLibrary.value = manager.loadLibrary()
        }
    }

    fun toggleFavorite(messageId: String) {
        val currentMessages = _messages.value
        var changed = false
        val updatedMessages = currentMessages.map { message ->
            if (message.id == messageId) {
                changed = true
                message.copy(isBookmarked = !message.isBookmarked)
            } else {
                message
            }
        }
        if (changed) {
            _messages.value = updatedMessages
            saveMessages()
        }
    }

    /**
     * Load messages for the current personality from persistent storage
     */
    private fun loadMessagesForCurrentPersonality() {
        memoryManager?.let { manager ->
            val savedMessages = manager.loadMessages(_currentPersonality.value.id)
            if (savedMessages.isEmpty()) {
                val greetingMessage = Message(
                    content = _currentPersonality.value.greeting,
                    isFromUser = false,
                    personalityId = _currentPersonality.value.id
                )
                _messages.value = listOf(greetingMessage)
                saveMessages()
            } else {
                _messages.value = savedMessages
            }
        }
    }

    /**
     * Save current messages to persistent storage
     */
    private fun saveMessages() {
        memoryManager?.let { manager ->
            manager.saveMessages(_currentPersonality.value.id, _messages.value)
        }
    }

    /**
     * Check if we need to auto-reset and handle it
     */
    private fun handleAutoResetIfNeeded() {
        memoryManager?.let { manager ->
            if (manager.shouldAutoReset(_currentPersonality.value.id)) {
                // Clear messages and add auto-reset message
                manager.clearMessages(_currentPersonality.value.id)
                val autoResetMessage = Message(
                    content = ChatMemoryManager.AUTO_RESET_MESSAGE,
                    isFromUser = false,
                    personalityId = _currentPersonality.value.id
                )
                _messages.value = listOf(autoResetMessage)
                saveMessages()
            }
        }
    }

    fun sendMessage(
        content: String,
        shouldSpeak: Boolean = false,
        imageUri: String? = null,
        fileUri: String? = null,
        fileName: String? = null,
        messageType: MessageType = MessageType.TEXT
    ) {
        if ((content.isBlank() && imageUri == null) || _isLoading.value) return

        // Check for auto-reset before adding new message
        handleAutoResetIfNeeded()

        // Add user message
        val userMessage = Message(
            content = content,
            isFromUser = true,
            imageUri = imageUri,
            fileUri = fileUri,
            fileName = fileName,
            messageType = messageType,
            personalityId = _currentPersonality.value.id
        )
        _messages.value = _messages.value + userMessage
        saveMessages() // Save after adding user message

        // Set voice response preference
        _shouldSpeakResponse.value = shouldSpeak

        // Get conversation context for AI
        val conversationContext =
            memoryManager?.getConversationContext(_currentPersonality.value.id) ?: emptyList()

        // Get AI response
        val aiInputContent = augmentUserMessage(content)

        _isLoading.value = true
        viewModelScope.launch {
            val imagePayload =
                if (messageType == MessageType.IMAGE || messageType == MessageType.TEXT_WITH_IMAGE) {
                    loadImagePayload(imageUri)
                } else {
                    null
                }
            try {
                val result = withTimeoutOrNull(30000) { // 30 second timeout
                    val aiResponse = when (messageType) {
                        MessageType.IMAGE -> aiRepository.getImageAnalysisResponse(
                            aiInputContent,
                            imageUri,
                            imagePayload?.data,
                            imagePayload?.mimeType,
                            _currentPersonality.value,
                            conversationContext
                        )

                        MessageType.TEXT_WITH_IMAGE -> aiRepository.getImageAnalysisResponse(
                            aiInputContent,
                            imageUri,
                            imagePayload?.data,
                            imagePayload?.mimeType,
                            _currentPersonality.value,
                            conversationContext
                        )

                        else -> aiRepository.getAIResponse(
                            aiInputContent,
                            _currentPersonality.value,
                            conversationContext
                        )
                    }
                    aiResponse
                }

                if (result == null) {
                    // Timeout occurred
                    throw Exception("Request timed out.")
                }

                val aiMessage = Message(
                    content = result,
                    isFromUser = false,
                    personalityId = _currentPersonality.value.id
                )
                _messages.value = _messages.value + aiMessage
                saveMessages() // Save after adding AI response

                // Store last AI response for voice output
                _lastAIResponse.value = result
            } catch (e: Exception) {
                val errorMessage = Message(
                    content = "Sorry, I encountered an error: ${e.message}. Please try again.",
                    isFromUser = false,
                    personalityId = _currentPersonality.value.id
                )
                _messages.value = _messages.value + errorMessage
                saveMessages() // Save error message
                _lastAIResponse.value = errorMessage.content
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun changePersonality(personality: AIPersonality) {
        // Save current conversation before switching
        saveMessages()

        _currentPersonality.value = personality

        // Load conversation history for the new personality
        loadMessagesForCurrentPersonality()

        // Add a system message about the personality change only if there are no messages
        if (_messages.value.isEmpty()) {
            val personalityChangeMessage = Message(
                content = personality.greeting,
                isFromUser = false,
                personalityId = personality.id
            )
            _messages.value = _messages.value + personalityChangeMessage
            saveMessages()
        }
    }

    fun getPersonalityGreeting(): String {
        return _currentPersonality.value.greeting
    }

    fun clearShouldSpeak() {
        _shouldSpeakResponse.value = false
    }

    fun clearMessages() {
        _messages.value = emptyList()
        saveMessages() // Save the cleared state
    }

    /**
     * Clear messages for the current personality only
     */
    fun clearCurrentPersonalityMemory() {
        memoryManager?.clearMessages(_currentPersonality.value.id)
        _messages.value = emptyList()
    }

    /**
     * Clear all messages for all personalities
     */
    fun clearAllPersonalitiesMemory() {
        memoryManager?.clearAllMessages()
        _messages.value = emptyList()
    }

    /**
     * Get message count for current personality
     */
    fun getCurrentPersonalityMessageCount(): Int {
        return memoryManager?.getMessageCount(_currentPersonality.value.id) ?: 0
    }

    /**
     * Check if current personality has history
     */
    fun currentPersonalityHasHistory(): Boolean {
        return memoryManager?.hasHistory(_currentPersonality.value.id) ?: false
    }

    /**
     * Start a fresh conversation for the current personality
     */
    fun startFresh() {
        memoryManager?.clearMessages(_currentPersonality.value.id)
        val greetingMessage = Message(
            content = _currentPersonality.value.greeting,
            isFromUser = false,
            personalityId = _currentPersonality.value.id
        )
        _messages.value = listOf(greetingMessage)
        saveMessages()
    }

    // ============ MUSIC GENERATION METHODS ============

    /**
     * Check if current personality is Music Composer
     */
    fun isMusicComposerActive(): Boolean {
        return _currentPersonality.value.responseStyle == ResponseStyle.MUSIC
    }

    /**
     * Check if music generation is enabled and available
     */
    fun isMusicGenerationAvailable(): Boolean {
        if (!isMusicComposerActive()) {
            return false
        }

        return when (FeatureFlags.MusicComposerConfig.ACTIVE_MUSIC_PROVIDER) {
            MusicProvider.SUNO -> sunoService.isConfigured()
            MusicProvider.REPLICATE -> replicateService.isConfigured()
            MusicProvider.LYRIA -> FeatureFlags.ENABLE_LYRIA_MUSIC_GENERATION
        }
    }

    /**
     * Check if user can generate music (has free songs or payment setup)
     */
    fun canGenerateMusic(): Boolean {
        return musicTracker?.canGenerateMusic() ?: false
    }

    /**
     * Generate music from text prompt
     *
     * @param prompt The music description
     * @param useRawPrompt If true, sends prompt exactly as-is without any enhancement.
     *                     Use this when Google's filters are being unreasonable.
     */
    fun generateMusic(prompt: String, useRawPrompt: Boolean = false) {
        if (!isMusicGenerationAvailable()) {
            addSystemMessage("Music generation is not available. Please check configuration.")
            return
        }

        // Check subscription limits
        val sub = _subscription.value
        if (!sub.isPremium && sub.songCount >= 5) {
            // Free tier user has used all 5 songs
            _showUpgradeModal.value = true
            return
        }

        if (sub.isPremium && sub.needsRenewal) {
            // Premium user needs to renew
            _showUpgradeModal.value = true
            return
        }

        _isMusicGenerating.value = true

        viewModelScope.launch {
            try {
                // Add user message showing what music they're generating
                val providerLabel = when (FeatureFlags.MusicComposerConfig.ACTIVE_MUSIC_PROVIDER) {
                    MusicProvider.SUNO -> "Suno"
                    MusicProvider.REPLICATE -> "MusicGen"
                    MusicProvider.LYRIA -> "Lyria"
                }

                val userMessage = Message(
                    content = " Generate music (${providerLabel}): $prompt",
                    isFromUser = true,
                    personalityId = _currentPersonality.value.id
                )
                _messages.value = _messages.value + userMessage
                saveMessages()

                // Show generating status
                val progressCopy = "✨ Generating your magic music...(sparkles) ✨✨"

                val generatingMessage = Message(
                    content = progressCopy,
                    isFromUser = false,
                    personalityId = _currentPersonality.value.id
                )
                _messages.value = _messages.value + generatingMessage
                saveMessages()

                // Enhance the prompt ONLY if user wants enhancement
                // Raw mode sends the exact prompt to bypass Google's censorship
                val finalPrompt = if (useRawPrompt) {
                    Log.d("ChatViewModel", "🎵 RAW MODE: Using exact prompt without enhancement")
                    prompt
                } else {
                    val enhanced = enhanceMusicPrompt(prompt)
                    Log.d("ChatViewModel", "🎵 Original prompt: $prompt")
                    Log.d("ChatViewModel", "🎵 Enhanced prompt: $enhanced")
                    enhanced
                }

                val provider = FeatureFlags.MusicComposerConfig.ACTIVE_MUSIC_PROVIDER

                val result = when (provider) {
                    MusicProvider.SUNO -> generateWithSuno(prompt, finalPrompt, useRawPrompt)
                    MusicProvider.REPLICATE -> generateWithReplicate(finalPrompt, prompt)
                    MusicProvider.LYRIA -> generateWithReplicate(finalPrompt, prompt)  // Lyria disabled, fallback to Replicate
                }

                when (result) {
                    is MusicGenerationResult.Success -> {
                        // Save to library
                        val music = musicLibraryManager?.saveMusic(
                            audioData = result.audioData,
                            prompt = prompt,
                            mimeType = result.mimeType,
                            durationSeconds = result.durationSeconds,
                            isFreeTier = musicTracker?.isInFreeTier() ?: false,
                            costCents = musicTracker?.getNextGenerationCost() ?: 0
                        )

                        // Record generation
                        musicTracker?.recordGeneration()
                        updateMusicUsageStats()
                        loadMusicLibrary()
                        
                        // Increment song count in Supabase
                        supabaseService?.getCurrentUserId()?.let { userId ->
                            viewModelScope.launch {
                                supabaseService?.incrementSongCount(userId)
                                // Reload subscription to update song count
                                reloadUserProfile()
                            }
                        }

                        // Remove generating message and add success message
                        _messages.value = _messages.value.dropLast(1)

                        val sub = _subscription.value
                        val costInfo = if (!sub.isPremium) {
                            val remaining = 5 - sub.songCount
                            if (remaining > 0) {
                                "This was FREE! You have $remaining free songs remaining. 🎉"
                            } else {
                                "Upgrade to Premium for 50 songs per month! 👑"
                            }
                        } else {
                            val remaining = 50 - sub.songsThisPeriod
                            "Premium: $remaining of 50 songs remaining this month. 👑"
                        }

                        val successMessage = Message(
                            content = "🎵 **Your music is ready!** 🎶\n\n" +
                                    "**Features**: Vocals + instrumentals\n\n" +
                                    "$costInfo\n\n" +
                                    "Music saved to your library! Tap the music icon to play, download, or manage your tracks. 🎧",
                            isFromUser = false,
                            personalityId = _currentPersonality.value.id,
                            fileUri = music?.filePath,
                            fileName = music?.getFileName()
                        )
                        _messages.value = _messages.value + successMessage
                        saveMessages()

                        // Play success chime
                        playSuccessChime()

                        Log.d("ChatViewModel", "✅ Music generated successfully: ${music?.id}")
                    }

                    is MusicGenerationResult.Error -> {
                        // Remove generating message and add error
                        _messages.value = _messages.value.dropLast(1)

                        // Check if it's a safety filter issue
                        val isSafetyFilter =
                            result.message.contains("flagged", ignoreCase = true) ||
                                    result.message.contains("safety", ignoreCase = true)

                        val errorMessage = if (isSafetyFilter) {
                            Message(
                                content = "⚠️ **Content Filter Blocked Your Request**\n\n" +
                                        "The AI's content filter flagged your prompt.\n\n" +
                                        "**Workaround: Describe the musical style instead:**\n\n" +
                                        "1. **Focus on mood and genre:**\n" +
                                        "   • \"upbeat electronic dance music with energetic beats\"\n" +
                                        "   • \"mellow acoustic guitar, peaceful and relaxing\"\n" +
                                        "   • \"jazz fusion with saxophone and groovy bass\"\n\n" +
                                        "2. **Use descriptive tags:**\n" +
                                        "   • \"upbeat pop music, catchy melody, modern production\"\n" +
                                        "   • \"ambient atmospheric soundscape, calm and serene\"\n\n" +
                                        "Minimax Music generates full songs with vocals based on the STYLE and MOOD you describe.\n\n" +
                                        "**Try again with a different description!** 🎵",
                                isFromUser = false,
                                personalityId = _currentPersonality.value.id
                            )
                        } else {
                            Message(
                                content = "😔 Oops! I couldn't generate that music right now.\n\n" +
                                        "Error: ${result.message}\n\n" +
                                        "Don't worry! I can still help you with:\n" +
                                        "• Writing lyrics ✍️\n" +
                                        "• Chord progressions 🎹\n" +
                                        "• Song structure 🎼\n" +
                                        "• Music theory 📚\n\n" +
                                        "Want to try a different prompt or would you like help with lyrics instead?",
                                isFromUser = false,
                                personalityId = _currentPersonality.value.id
                            )
                        }

                        _messages.value = _messages.value + errorMessage
                        saveMessages()

                        Log.e("ChatViewModel", "❌ Music generation failed: ${result.message}")
                    }
                }

            } catch (e: Exception) {
                // Remove generating message
                _messages.value = _messages.value.dropLast(1)

                val errorMessage = Message(
                    content = "An unexpected error occurred while generating music. Please try again! 🎵",
                    isFromUser = false,
                    personalityId = _currentPersonality.value.id
                )
                _messages.value = _messages.value + errorMessage
                saveMessages()

                Log.e("ChatViewModel", "Exception in music generation", e)
            } finally {
                _isMusicGenerating.value = false
            }
        }
    }

    /**
     * Delete music from library
     */
    fun deleteMusic(musicId: String) {
        viewModelScope.launch {
            musicLibraryManager?.deleteMusic(musicId)
            loadMusicLibrary()
        }
    }

    /**
     * Set currently playing music
     */
    fun setPlayingMusic(music: GeneratedMusic?) {
        _currentlyPlayingMusic.value = music
    }

    /**
     * Get music file for playback
     */
    fun getMusicFile(musicId: String): java.io.File? {
        return musicLibraryManager?.getAudioFile(musicId)
    }

    /**
     * Play a music track
     */
    fun playMusic(music: GeneratedMusic) {
        val file = java.io.File(music.filePath)
        if (!file.exists()) {
            Log.e("ChatViewModel", "❌ Music file not found: ${music.filePath}")
            addSystemMessage("Music file not found. It may have been deleted.")
            return
        }

        Log.d("ChatViewModel", "🎵 Playing music: ${music.prompt}")
        _currentlyPlayingMusic.value = music
        musicPlayer?.play(file)
    }

    /**
     * Play music by ID
     */
    fun playMusicById(musicId: String) {
        val music = musicLibraryManager?.getMusicById(musicId)
        if (music != null) {
            playMusic(music)
        } else {
            Log.e("ChatViewModel", "❌ Music not found with ID: $musicId")
        }
    }

    /**
     * Stop music playback
     */
    fun stopMusic() {
        musicPlayer?.stop()
        _currentlyPlayingMusic.value = null
    }

    /**
     * Toggle play/pause
     */
    fun toggleMusicPlayPause() {
        musicPlayer?.togglePlayPause()
    }

    /**
     * Clean up music player when ViewModel is cleared
     */
    override fun onCleared() {
        super.onCleared()
        musicPlayer?.release()
        saveMessages()
    }

    /**
     * Add a system message to chat
     */
    private fun addSystemMessage(content: String) {
        val systemMessage = Message(
            content = content,
            isFromUser = false,
            personalityId = _currentPersonality.value.id
        )
        _messages.value = _messages.value + systemMessage
        saveMessages()
    }

    /**
     * Play success chime when music generation completes
     */
    private fun playSuccessChime() {
        applicationContext?.let { context ->
            try {
                val mediaPlayer = MediaPlayer.create(context, com.bluegenie.app.R.raw.success_chime)
                mediaPlayer?.setVolume(0.5f, 0.5f) // 50% volume
                mediaPlayer?.setOnCompletionListener { mp ->
                    mp.release()
                }
                mediaPlayer?.start()
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Failed to play success chime", e)
            }
        }
    }

    private fun augmentUserMessage(originalInput: String): String {
        val lastAssistantMessage = _messages.value.lastOrNull { !it.isFromUser }?.content
            ?: return originalInput

        val normalizedInput = originalInput.lowercase()

        if (normalizedInput.contains("vice president")) {
            val trumpMentionsCurrent = Regex(
                "donald\\s+trump[^.]*current\\s+president",
                RegexOption.IGNORE_CASE
            ).containsMatchIn(lastAssistantMessage)

            if (trumpMentionsCurrent) {
                return buildString {
                    append(
                        "Context anchor: You previously confirmed that Donald Trump is the current President of the United States as of January 20, 2025. " +
                                "The user is still talking about that exact administration. Answer the question using 2025 data and provide the Vice President serving with Donald Trump right now. " +
                                "Do not mention past administrations or older vice presidents.\n\n"
                    )
                    append("User question: ")
                    append(originalInput)
                }
            }
        }

        if (normalizedInput.contains("poem") || normalizedInput.contains("stanza")) {
            val stanzaMatch = Regex("(\\d+)\\s*(stanza|verse)").find(normalizedInput)
            val lineMatch = Regex("(\\d+)\\s*(line|lines)").findAll(normalizedInput).toList()

            val stanzaCount = stanzaMatch?.groups?.get(1)?.value
            val linesPerStanza = lineMatch.getOrNull(0)?.groups?.get(1)?.value

            return buildString {
                append("Follow the exact formatting requested for this poem. ")
                append("If the user asked for specific stanza or line counts, obey them precisely. ")
                append("Insert explicit newline characters between every line and keep a blank line between stanzas. ")
                stanzaCount?.let {
                    append("Output exactly $it stanza(s). ")
                }
                linesPerStanza?.let {
                    append("Each stanza must contain exactly $it line(s). ")
                }
                append("Never merge lines into paragraphs.")
                append("\n\nUser request: ")
                append(originalInput)
            }
        }

        return originalInput
    }

    private suspend fun loadImagePayload(imageUri: String?): ImagePayload? {
        val context = applicationContext ?: return null
        val uri = imageUri?.let { Uri.parse(it) } ?: return null

        return withContext(Dispatchers.IO) {
            try {
                val resolver = context.contentResolver
                val mimeType = resolver.getType(uri) ?: "image/jpeg"
                resolver.openInputStream(uri)?.use { inputStream ->
                    val bytes = inputStream.readBytes()
                    ImagePayload(bytes, mimeType)
                }
            } catch (error: Exception) {
                Log.w("ChatViewModel", "Unable to read image data: ${error.message}")
                null
            }
        }
    }

    /**
     * Enhance a music prompt ONLY if needed for quality
     *
     * NOTE: Stable Audio (Stability AI) has minimal content filtering - much better than Google.
     * Stable Audio generates high-quality music (vocals + instrumentals) based on descriptions.
     * We minimize enhancement to preserve user intent and avoid any potential issues.
     *
     * Enhancement is DISABLED if:
     * - Prompt contains quotation marks (indicates specific song/title)
     * - Prompt is already detailed (>100 chars or >15 words)
     * - Prompt contains "instrumental" keyword
     *
     * When enhancement IS applied, it only adds musical descriptors like genre,
     * instruments, and mood - it NEVER restricts or censors the user's creative intent.
     */
    private suspend fun enhanceMusicPrompt(originalPrompt: String): String {
        // If prompt has quotes, it's a specific title/request - use as-is
        if (originalPrompt.contains("\"") || originalPrompt.contains("'")) {
            Log.d("ChatViewModel", "✅ Prompt has quotes (specific request), using as-is")
            return originalPrompt
        }

        // If user explicitly says "instrumental", they know what they want
        if (originalPrompt.lowercase().contains("instrumental")) {
            Log.d("ChatViewModel", "✅ User specified 'instrumental', using as-is")
            return originalPrompt
        }

        // If prompt is already detailed, use as-is
        if (originalPrompt.length > 100 || originalPrompt.split(" ").size > 15) {
            Log.d("ChatViewModel", "✅ Prompt is detailed enough, using as-is")
            return originalPrompt
        }

        // Only enhance short/vague prompts to improve music quality
        Log.d("ChatViewModel", "🎵 Enhancing short prompt to improve music quality")
        return getIntelligentFallback(originalPrompt)
    }

    /**
     * Intelligent fallback that expands the user's prompt based on detected keywords
     * Preserves user intent while adding helpful musical detail
     * This runs LOCALLY to avoid any AI safety filter issues
     */
    private fun getIntelligentFallback(originalPrompt: String): String {
        val lower = originalPrompt.lowercase()

        // Detect mood descriptors
        val isHappy =
            lower.contains("happy") || lower.contains("joyful") || lower.contains("upbeat") ||
                    lower.contains("cheerful") || lower.contains("bright")
        val isSad =
            lower.contains("sad") || lower.contains("melancholy") || lower.contains("somber") ||
                    lower.contains("dark") || lower.contains("moody") || lower.contains("emotional")
        val isEnergetic =
            lower.contains("energetic") || lower.contains("intense") || lower.contains("powerful") ||
                    lower.contains("fast") || lower.contains("aggressive") || lower.contains("driving")
        val isCalm =
            lower.contains("calm") || lower.contains("peaceful") || lower.contains("relaxing") ||
                    lower.contains("soft") || lower.contains("gentle") || lower.contains("slow")

        // Build enhanced prompt based on genre + mood
        return when {
            lower.contains("electronic") || lower.contains("edm") || lower.contains("techno") ||
                    lower.contains("house") || lower.contains("trance") -> {
                val mood = when {
                    isEnergetic -> "high-energy and driving"
                    isCalm -> "atmospheric and ambient"
                    else -> "dynamic and engaging"
                }
                "$originalPrompt - featuring synthesizers, electronic drums, pulsing bass, and $mood electronic textures"
            }

            lower.contains("acoustic") || lower.contains("folk") -> {
                val mood = when {
                    isHappy -> "bright and uplifting"
                    isSad -> "introspective and emotional"
                    else -> "warm and organic"
                }
                "$originalPrompt - with fingerstyle guitar, natural acoustic instruments, and $mood folk melodies"
            }

            lower.contains("piano") || lower.contains("classical") -> {
                val mood = when {
                    isEnergetic -> "dramatic and powerful"
                    isCalm -> "gentle and serene"
                    else -> "expressive and flowing"
                }
                "$originalPrompt - an $mood piano composition with classical influences and rich harmonies"
            }

            lower.contains("jazz") || lower.contains("swing") || lower.contains("bebop") -> {
                val mood = when {
                    isEnergetic -> "uptempo and swinging"
                    isCalm -> "smooth and laid-back"
                    else -> "sophisticated and groovy"
                }
                "$originalPrompt - featuring $mood jazz instrumentation with piano, bass, and drums"
            }

            lower.contains("rock") || lower.contains("guitar") -> {
                val mood = when {
                    isEnergetic -> "high-octane and aggressive"
                    isCalm -> "melodic and atmospheric"
                    else -> "driving and powerful"
                }
                "$originalPrompt - with $mood electric guitar riffs, solid drums, and bass groove"
            }

            lower.contains("ambient") || lower.contains("chill") || lower.contains("lofi") -> {
                "$originalPrompt - creating an atmospheric soundscape with soft textures, gentle rhythms, and calming sonic layers"
            }

            lower.contains("hip hop") || lower.contains("rap") || lower.contains("beat") ||
                    lower.contains("trap") || lower.contains("boom bap") -> {
                val mood = when {
                    isEnergetic -> "hard-hitting and aggressive"
                    isCalm -> "smooth and laid-back"
                    else -> "modern and groovy"
                }
                "$originalPrompt - a $mood beat with deep 808 bass, crisp drums, and melodic elements"
            }

            lower.contains("orchestral") || lower.contains("cinematic") || lower.contains("epic") ||
                    lower.contains("symphony") -> {
                val mood = when {
                    isEnergetic -> "epic and powerful"
                    isSad -> "emotional and dramatic"
                    else -> "sweeping and majestic"
                }
                "$originalPrompt - a $mood orchestral composition with strings, brass, and dynamic percussion"
            }

            lower.contains("country") || lower.contains("bluegrass") -> {
                "$originalPrompt - featuring acoustic guitar, banjo, fiddle, and authentic country instrumentation with storytelling melodies"
            }

            lower.contains("reggae") || lower.contains("ska") || lower.contains("dub") -> {
                "$originalPrompt - with offbeat guitar rhythms, groovy bass lines, and uplifting reggae vibes"
            }

            lower.contains("metal") || lower.contains("heavy") -> {
                "$originalPrompt - featuring heavy distorted guitars, double bass drums, and intense powerful energy"
            }

            lower.contains("pop") -> {
                val mood = when {
                    isHappy -> "catchy and upbeat"
                    isSad -> "emotional and melodic"
                    else -> "contemporary and polished"
                }
                "$originalPrompt - with $mood pop production, memorable hooks, and modern instrumentation"
            }

            lower.contains("blues") -> {
                "$originalPrompt - featuring soulful guitar, expressive melodies, and authentic blues feel with emotional depth"
            }

            lower.contains("funk") || lower.contains("groove") -> {
                "$originalPrompt - with syncopated rhythms, funky bass lines, tight drums, and infectious groove"
            }

            lower.contains("r&b") || lower.contains("soul") -> {
                "$originalPrompt - featuring smooth rhythms, soulful melodies, and rich harmonies with contemporary production"
            }

            // If it's very short (likely just a mood/feeling), add generic musical context
            originalPrompt.split(" ").size <= 3 -> {
                "$originalPrompt instrumental music - an expressive musical composition with rich melodies, harmonies, and dynamic instrumentation"
            }

            // Generic fallback - just add basic musical context
            else -> {
                "$originalPrompt - an instrumental musical composition with expressive melodies, rich harmonies, and dynamic arrangements"
            }
        }
    }

    private data class ImagePayload(
        val data: ByteArray,
        val mimeType: String
    )

    private suspend fun getSunoMusic(
        prompt: String,
        finalPrompt: String,
        useRawPrompt: Boolean
    ): MusicGenerationResult {
        val result = sunoService.generateMusic(finalPrompt)
        if (result is MusicGenerationResult.Error && !useRawPrompt) {
            if (result.message.contains("flagged", ignoreCase = true)) {
                Log.d("ChatViewModel", " Suno flagged enhanced prompt, retrying raw")
                return sunoService.generateMusic(prompt)
            }
        }
        return result
    }

    private suspend fun getReplicateMusic(
        finalPrompt: String,
        originalPrompt: String
    ): MusicGenerationResult {
        var result = replicateService.generateMusic(prompt = finalPrompt)
        if (result is MusicGenerationResult.Error &&
            result.message.contains("flagged", ignoreCase = true)
        ) {
            Log.d(
                "ChatViewModel",
                " Replicate flagged enhanced prompt, retrying with original"
            )

            result = replicateService.generateMusic(prompt = originalPrompt)
        }
        return result
    }

    // private suspend fun getLyriaMusic(finalPrompt: String): MusicGenerationResult {
    //     applicationContext?.let { context ->
    //         lyriaService.initialize(context)
    //     }
    //     return lyriaService.generateMusic(prompt = finalPrompt)
    // }

    private suspend fun generateWithSuno(
        prompt: String,
        finalPrompt: String,
        useRawPrompt: Boolean
    ): MusicGenerationResult {
        return getSunoMusic(prompt, finalPrompt, useRawPrompt)
    }

    private suspend fun generateWithReplicate(
        finalPrompt: String,
        originalPrompt: String
    ): MusicGenerationResult {
        return getReplicateMusic(finalPrompt, originalPrompt)
    }

    // private suspend fun generateWithLyria(finalPrompt: String): MusicGenerationResult {
    //     return getLyriaMusic(finalPrompt)
    // }

    // ============ SUBSCRIPTION MANAGEMENT METHODS ============

    /**
     * Check if user is already signed in on app start
     */
    private suspend fun checkExistingSignIn() {
        val userId = supabaseService?.getCurrentUserId()
        val email = supabaseService?.getCurrentUserEmail()
        
        if (userId != null && email != null) {
            Log.d("ChatViewModel", "Found existing sign-in: $email")
            reloadUserProfile()
        }
    }

    /**
     * Sign in with Google using ID token
     */
    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            Log.d("ChatViewModel", "🔐 Starting Google sign-in with token")
            
            val result = supabaseService?.signInWithGoogle(idToken)
            result?.onSuccess {
                Log.d("ChatViewModel", "✅ Successfully signed in with Google")
                
                // Load user profile first (before closing modal or showing message)
                delay(500) // Brief delay to ensure Supabase session is ready
                reloadUserProfile()
                
                // Now close modal
                _showSignInModal.value = false
                
                // Show success message (counter will show song count)
                val sub = _subscription.value
                
                if (sub.isPremium) {
                    addSystemMessage("✅ Signed in successfully! Welcome back, Premium member! 👑")
                } else {
                    addSystemMessage("✅ Signed in successfully! You're ready to generate music. 🎉")
                }
            }?.onFailure { error ->
                Log.e("ChatViewModel", "❌ Failed to sign in with Google: ${error.message}", error)
                
                // Provide user-friendly error message
                val userMessage = when {
                    error.message?.contains("invalid", ignoreCase = true) == true -> 
                        "❌ Configuration Error\n\n" +
                        "Your Android app needs to be registered in Google Cloud Console.\n\n" +
                        "Please contact support or try again later."
                    error.message?.contains("network", ignoreCase = true) == true -> 
                        "❌ Network Error\n\n" +
                        "Please check your internet connection and try again."
                    else -> 
                        "❌ Sign In Failed\n\n" +
                        "${error.message}\n\n" +
                        "Please try again or check your internet connection."
                }
                
                addSystemMessage(userMessage)
            }
        }
    }

    /**
     * Sign out current user
     */
    fun signOut() {
        viewModelScope.launch {
            val result = supabaseService?.signOut()
            result?.onSuccess {
                _subscription.value = UserSubscription()
                addSystemMessage("Signed out successfully.")
            }
        }
    }

    /**
     * Check if user is signed in
     */
    fun isUserSignedIn(): Boolean {
        return supabaseService?.isSignedIn() == true
    }

    /**
     * Show sign-in modal
     */
    fun showSignIn() {
        _showSignInModal.value = true
    }

    /**
     * Reload user profile from Supabase
     */
    private suspend fun reloadUserProfile() {
        val userId = supabaseService?.getCurrentUserId() ?: return
        val email = supabaseService?.getCurrentUserEmail() ?: return
        
        val result = supabaseService?.getUserProfile(userId, email)
        result?.onSuccess { profile ->
            val subscription = supabaseService?.buildSubscription(profile)
            if (subscription != null) {
                _subscription.value = subscription
                Log.d("ChatViewModel", "Loaded profile: isPremium=${subscription.isPremium}, songs=${subscription.songCount}")
            }
        }?.onFailure { error ->
            Log.e("ChatViewModel", "Failed to load user profile", error)
        }
    }

    /**
     * Start premium checkout process
     * Opens Stripe checkout in browser
     */
    fun startPremiumCheckout() {
        val context = applicationContext
        val userId = supabaseService?.getCurrentUserId()
        val email = supabaseService?.getCurrentUserEmail()
        
        Log.d("ChatViewModel", "🛒 Starting premium checkout")
        Log.d("ChatViewModel", "   Context: ${if (context != null) "✓" else "✗"}")
        Log.d("ChatViewModel", "   User ID: ${userId ?: "null"}")
        Log.d("ChatViewModel", "   Email: ${email ?: "null"}")
        
        if (userId == null || email == null) {
            Log.e("ChatViewModel", "❌ User not signed in, showing sign-in modal")
            _showSignInModal.value = true
            return
        }
        
        if (context == null) {
            Log.e("ChatViewModel", "❌ Cannot start checkout: context is null")
            addSystemMessage("❌ Unable to open checkout. Please restart the app and try again.")
            return
        }
        
        viewModelScope.launch {
            try {
                Log.d("ChatViewModel", "📱 Initiating Stripe checkout...")
                
                // Close the upgrade modal first
                _showUpgradeModal.value = false
                
                // Show loading message in chat
                addSystemMessage("Opening payment page... 💳")
                
                // Show toast for immediate feedback
                withContext(Dispatchers.Main) {
                    android.widget.Toast.makeText(
                        context,
                        "Connecting to payment server...",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
                
                // Open Stripe checkout in browser
                StripeCheckoutHelper.openCheckout(context, userId, email)
                
                Log.d("ChatViewModel", "✅ Stripe checkout opened for user: $email")
                
                // Add success message
                addSystemMessage("✅ Payment page opened! Complete your purchase in the browser, then return to the app.")
                
                // Show success toast
                withContext(Dispatchers.Main) {
                    android.widget.Toast.makeText(
                        context,
                        "Opening browser...",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
                
                // Set flag to check premium status when user returns
                checkPremiumOnResume = true
                
            } catch (e: Exception) {
                Log.e("ChatViewModel", "❌ Failed to open Stripe checkout: ${e.message}", e)
                
                // Show user-friendly error message
                val userMessage = when {
                    e.message?.contains("network", ignoreCase = true) == true ||
                    e.message?.contains("connect", ignoreCase = true) == true ->
                        "❌ Network Error\n\nCannot connect to payment server.\n\nPlease check your internet connection and try again."
                    
                    e.message?.contains("browser", ignoreCase = true) == true ->
                        "❌ Browser Not Found\n\nNo web browser app is installed.\n\nPlease install a browser (Chrome, Firefox, etc.) and try again."
                    
                    e.message?.contains("timeout", ignoreCase = true) == true ->
                        "❌ Connection Timeout\n\nThe request took too long.\n\nPlease check your internet connection and try again."
                    
                    else ->
                        "❌ Payment Error\n\n${e.message}\n\nPlease try again or contact support if the issue persists."
                }
                
                addSystemMessage(userMessage)
                
                // Show error toast
                withContext(Dispatchers.Main) {
                    android.widget.Toast.makeText(
                        context,
                        "Failed to open checkout: ${e.message?.take(50)}",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
                
                // Re-open the modal so they can try again
                _showUpgradeModal.value = true
            }
        }
    }
    
    // Flag to check premium status when app resumes
    private var checkPremiumOnResume = false
    
    /**
     * Call this from Activity.onResume() to check premium status after payment
     */
    fun onAppResume() {
        if (checkPremiumOnResume) {
            checkPremiumOnResume = false
            viewModelScope.launch {
                delay(500) // Brief delay to ensure webhook has processed
                checkPremiumStatus()
            }
        }
    }

    /**
     * Check premium status after payment
     */
    suspend fun checkPremiumStatus() {
        reloadUserProfile()
    }

    /**
     * Set show sign-in modal
     */
    fun setShowSignInModal(show: Boolean) {
        _showSignInModal.value = show
    }

    /**
     * Set show upgrade modal
     */
    fun setShowUpgradeModal(show: Boolean) {
        _showUpgradeModal.value = show
    }

    /**
     * Get current user ID
     */
    fun getCurrentUserId(): String? {
        return supabaseService?.getCurrentUserId()
    }

    /**
     * Get current user email
     */
    fun getCurrentUserEmail(): String? {
        return supabaseService?.getCurrentUserEmail()
    }
}
