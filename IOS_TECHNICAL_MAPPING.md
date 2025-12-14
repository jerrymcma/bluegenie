# SparkiFire: Android to iOS Technical Mapping

**Purpose:** Direct code translation reference for iOS developer  
**Audience:** iOS developer familiar with Swift/SwiftUI  
**Status:** Technical specification

---

## 📱 Core Models

### Message Model

**Android (Kotlin):**

```kotlin
// Message.kt
data class Message(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUri: String? = null,
    val messageType: MessageType = MessageType.TEXT,
    val personalityId: String = "default"
)

enum class MessageType {
    TEXT, IMAGE, TEXT_WITH_IMAGE
}
```

**iOS (Swift):**

```swift
// Message.swift
struct Message: Identifiable, Codable {
    let id: String
    let content: String
    let isFromUser: Bool
    let timestamp: Date
    let imageUri: String?
    let messageType: MessageType
    let personalityId: String
    
    init(
        id: String = UUID().uuidString,
        content: String,
        isFromUser: Bool,
        timestamp: Date = Date(),
        imageUri: String? = nil,
        messageType: MessageType = .text,
        personalityId: String = "default"
    ) {
        self.id = id
        self.content = content
        self.isFromUser = isFromUser
        self.timestamp = timestamp
        self.imageUri = imageUri
        self.messageType = messageType
        self.personalityId = personalityId
    }
}

enum MessageType: String, Codable {
    case text
    case image
    case textWithImage
}
```

---

### AI Personality Model

**Android (Kotlin):**

```kotlin
// AIPersonality.kt (simplified)
data class AIPersonality(
    val id: String,
    val name: String,
    val description: String,
    val greeting: String,
    val responseStyle: ResponseStyle,
    val color: Long
)

enum class ResponseStyle {
    FRIENDLY, PROFESSIONAL, CREATIVE, TECHNICAL, HUMOROUS, CASUAL
}

object AIPersonalities {
    val DEFAULT = AIPersonality(
        id = "sparkifire",
        name = "SparkiFire",
        description = "Your friendly AI assistant",
        greeting = "Hello! I'm SparkiFire AI...",
        responseStyle = ResponseStyle.FRIENDLY,
        color = 0xFFFF6F00
    )
    
    val PROFESSIONAL = AIPersonality(
        id = "alex_pro",
        name = "Alex Pro",
        description = "Professional business consultant",
        greeting = "Good day. I'm Alex Pro...",
        responseStyle = ResponseStyle.PROFESSIONAL,
        color = 0xFF1976D2
    )
    
    // ... more personalities
    
    fun getAllPersonalities() = listOf(
        DEFAULT, PROFESSIONAL, CREATIVE, TECHNICAL, HUMOROUS, CASUAL
    )
}
```

**iOS (Swift):**

```swift
// AIPersonality.swift
struct AIPersonality: Identifiable, Codable {
    let id: String
    let name: String
    let description: String
    let greeting: String
    let responseStyle: ResponseStyle
    let color: Color
    
    var colorHex: String {
        // Encode Color to hex for storage
    }
    
    init(id: String, name: String, description: String, 
         greeting: String, responseStyle: ResponseStyle, colorHex: String) {
        self.id = id
        self.name = name
        self.description = description
        self.greeting = greeting
        self.responseStyle = responseStyle
        self.color = Color(hex: colorHex)
    }
}

enum ResponseStyle: String, Codable {
    case friendly
    case professional
    case creative
    case technical
    case humorous
    case casual
}

struct AIPersonalities {
    static let sparkiFire = AIPersonality(
        id: "sparkifire",
        name: "SparkiFire",
        description: "Your friendly AI assistant",
        greeting: "Hello! I'm SparkiFire AI...",
        responseStyle: .friendly,
        colorHex: "#FF6F00"
    )
    
    static let alexPro = AIPersonality(
        id: "alex_pro",
        name: "Alex Pro",
        description: "Professional business consultant",
        greeting: "Good day. I'm Alex Pro...",
        responseStyle: .professional,
        colorHex: "#1976D2"
    )
    
    // ... more personalities
    
    static func getAllPersonalities() -> [AIPersonality] {
        return [sparkiFire, alexPro, lunaCreative, codeMaster, jokeBot, buddy]
    }
}

// Color extension for hex support
extension Color {
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let a, r, g, b: UInt64
        switch hex.count {
        case 6: // RGB
            (a, r, g, b) = (255, int >> 16, int >> 8 & 0xFF, int & 0xFF)
        case 8: // ARGB
            (a, r, g, b) = (int >> 24, int >> 16 & 0xFF, int >> 8 & 0xFF, int & 0xFF)
        default:
            (a, r, g, b) = (255, 0, 0, 0)
        }
        self.init(
            .sRGB,
            red: Double(r) / 255,
            green: Double(g) / 255,
            blue:  Double(b) / 255,
            opacity: Double(a) / 255
        )
    }
}
```

---

## 🎯 ViewModel Architecture

### ChatViewModel

**Android (Kotlin):**

```kotlin
// ChatViewModel.kt
class ChatViewModel(
    private val aiRepository: AIRepository = AIRepository(),
    private var memoryManager: ChatMemoryManager? = null
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentPersonality = MutableStateFlow(AIPersonalities.DEFAULT)
    val currentPersonality: StateFlow<AIPersonality> = _currentPersonality.asStateFlow()

    fun initialize(context: Context) {
        if (memoryManager == null) {
            memoryManager = ChatMemoryManager(context)
            loadMessagesForCurrentPersonality()
        }
    }

    fun sendMessage(
        content: String,
        shouldSpeak: Boolean = false,
        imageUri: String? = null,
        messageType: MessageType = MessageType.TEXT
    ) {
        // Add user message
        val userMessage = Message(
            content = content,
            isFromUser = true,
            imageUri = imageUri,
            messageType = messageType,
            personalityId = _currentPersonality.value.id
        )
        _messages.value = _messages.value + userMessage
        
        // Get AI response
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val aiResponse = aiRepository.getAIResponse(
                    content,
                    _currentPersonality.value,
                    memoryManager?.getConversationContext(_currentPersonality.value.id) ?: emptyList()
                )
                
                val aiMessage = Message(
                    content = aiResponse,
                    isFromUser = false,
                    personalityId = _currentPersonality.value.id
                )
                _messages.value = _messages.value + aiMessage
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun changePersonality(personality: AIPersonality) {
        saveMessages()
        _currentPersonality.value = personality
        loadMessagesForCurrentPersonality()
    }

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
}
```

**iOS (Swift):**

```swift
// ChatViewModel.swift
@MainActor
class ChatViewModel: ObservableObject {
    @Published var messages: [Message] = []
    @Published var isLoading: Bool = false
    @Published var currentPersonality: AIPersonality = AIPersonalities.sparkiFire
    
    private let aiRepository: AIRepository
    private let memoryManager: ChatMemoryManager
    
    init() {
        self.aiRepository = AIRepository()
        self.memoryManager = ChatMemoryManager()
        loadMessagesForCurrentPersonality()
    }
    
    func sendMessage(
        content: String,
        shouldSpeak: Bool = false,
        imageUri: String? = nil,
        messageType: MessageType = .text
    ) {
        guard !content.isEmpty || imageUri != nil else { return }
        guard !isLoading else { return }
        
        // Add user message
        let userMessage = Message(
            content: content,
            isFromUser: true,
            imageUri: imageUri,
            messageType: messageType,
            personalityId: currentPersonality.id
        )
        messages.append(userMessage)
        
        // Get AI response
        isLoading = true
        
        Task {
            do {
                let conversationContext = memoryManager.getConversationContext(
                    personalityId: currentPersonality.id
                )
                
                let aiResponse: String
                if messageType == .image || messageType == .textWithImage,
                   let imageUri = imageUri {
                    aiResponse = try await aiRepository.getImageAnalysisResponse(
                        prompt: content,
                        imageUri: imageUri,
                        personality: currentPersonality,
                        conversationContext: conversationContext
                    )
                } else {
                    aiResponse = try await aiRepository.getAIResponse(
                        userMessage: content,
                        personality: currentPersonality,
                        conversationContext: conversationContext
                    )
                }
                
                let aiMessage = Message(
                    content: aiResponse,
                    isFromUser: false,
                    personalityId: currentPersonality.id
                )
                messages.append(aiMessage)
                saveMessages()
                
            } catch {
                let errorMessage = Message(
                    content: "Sorry, I encountered an error. Please try again.",
                    isFromUser: false,
                    personalityId: currentPersonality.id
                )
                messages.append(errorMessage)
            }
            
            isLoading = false
        }
    }
    
    func changePersonality(_ personality: AIPersonality) {
        saveMessages()
        currentPersonality = personality
        loadMessagesForCurrentPersonality()
    }
    
    func startFresh() {
        memoryManager.clearMessages(personalityId: currentPersonality.id)
        let greetingMessage = Message(
            content: currentPersonality.greeting,
            isFromUser: false,
            personalityId: currentPersonality.id
        )
        messages = [greetingMessage]
        saveMessages()
    }
    
    private func loadMessagesForCurrentPersonality() {
        messages = memoryManager.loadMessages(personalityId: currentPersonality.id)
    }
    
    private func saveMessages() {
        memoryManager.saveMessages(messages, personalityId: currentPersonality.id)
    }
}
```

---

## 🌐 API Integration

### Gemini AI Service

**Android (Kotlin):**

```kotlin
// GeminiAIService.kt (key parts)
class GeminiAIService(private val apiKey: String) {
    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta"
    
    suspend fun generateContent(
        prompt: String,
        conversationHistory: List<Message> = emptyList(),
        imageData: String? = null
    ): String {
        val url = "$baseUrl/models/gemini-1.5-flash:generateContent?key=$apiKey"
        
        val requestBody = buildJsonObject {
            put("contents", buildJsonArray {
                // Add conversation history
                conversationHistory.forEach { msg ->
                    add(buildJsonObject {
                        put("role", if (msg.isFromUser) "user" else "model")
                        put("parts", buildJsonArray {
                            add(buildJsonObject {
                                put("text", msg.content)
                            })
                        })
                    })
                }
                
                // Add current message
                add(buildJsonObject {
                    put("role", "user")
                    put("parts", buildJsonArray {
                        if (imageData != null) {
                            add(buildJsonObject {
                                put("inlineData", buildJsonObject {
                                    put("mimeType", "image/jpeg")
                                    put("data", imageData)
                                })
                            })
                        }
                        add(buildJsonObject {
                            put("text", prompt)
                        })
                    })
                })
            })
            
            put("generationConfig", buildJsonObject {
                put("temperature", 0.7)
                put("maxOutputTokens", 1024)
            })
        }
        
        val response = client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(requestBody.toString())
        }
        
        return parseResponse(response)
    }
}
```

**iOS (Swift):**

```swift
// GeminiAIService.swift
class GeminiAIService {
    private let apiKey: String
    private let baseURL = "https://generativelanguage.googleapis.com/v1beta"
    
    init(apiKey: String) {
        self.apiKey = apiKey
    }
    
    func generateContent(
        prompt: String,
        conversationHistory: [Message] = [],
        imageData: Data? = nil
    ) async throws -> String {
        
        let urlString = "\(baseURL)/models/gemini-1.5-flash:generateContent?key=\(apiKey)"
        guard let url = URL(string: urlString) else {
            throw AIError.invalidURL
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        
        // Build request body
        var contents: [[String: Any]] = []
        
        // Add conversation history
        for message in conversationHistory {
            let role = message.isFromUser ? "user" : "model"
            let content: [String: Any] = [
                "role": role,
                "parts": [
                    ["text": message.content]
                ]
            ]
            contents.append(content)
        }
        
        // Add current message
        var currentParts: [[String: Any]] = []
        
        if let imageData = imageData {
            currentParts.append([
                "inlineData": [
                    "mimeType": "image/jpeg",
                    "data": imageData.base64EncodedString()
                ]
            ])
        }
        
        currentParts.append(["text": prompt])
        
        contents.append([
            "role": "user",
            "parts": currentParts
        ])
        
        let requestBody: [String: Any] = [
            "contents": contents,
            "generationConfig": [
                "temperature": 0.7,
                "maxOutputTokens": 1024
            ]
        ]
        
        request.httpBody = try JSONSerialization.data(withJSONObject: requestBody)
        
        // Make request
        let (data, response) = try await URLSession.shared.data(for: request)
        
        guard let httpResponse = response as? HTTPURLResponse,
              httpResponse.statusCode == 200 else {
            throw AIError.requestFailed
        }
        
        return try parseResponse(data)
    }
    
    private func parseResponse(_ data: Data) throws -> String {
        let json = try JSONSerialization.jsonObject(with: data) as? [String: Any]
        
        guard let candidates = json?["candidates"] as? [[String: Any]],
              let firstCandidate = candidates.first,
              let content = firstCandidate["content"] as? [String: Any],
              let parts = content["parts"] as? [[String: Any]],
              let firstPart = parts.first,
              let text = firstPart["text"] as? String else {
            throw AIError.invalidResponse
        }
        
        return text
    }
}

enum AIError: Error {
    case invalidURL
    case requestFailed
    case invalidResponse
}
```

---

## 🎤 Voice Manager

**Android (Kotlin):**

```kotlin
// VoiceManager.kt
class VoiceManager(private val context: Context) {
    private val speechRecognizer: SpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
    private val textToSpeech: TextToSpeech
    
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()
    
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()
    
    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()
    
    fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        speechRecognizer.startListening(intent)
        _isListening.value = true
    }
    
    fun stopListening() {
        speechRecognizer.stopListening()
        _isListening.value = false
    }
    
    fun speak(text: String) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        _isSpeaking.value = true
    }
    
    fun destroy() {
        speechRecognizer.destroy()
        textToSpeech.shutdown()
    }
}
```

**iOS (Swift):**

```swift
// VoiceManager.swift
import Speech
import AVFoundation

class VoiceManager: ObservableObject {
    @Published var isListening: Bool = false
    @Published var isSpeaking: Bool = false
    @Published var recognizedText: String = ""
    
    private var speechRecognizer: SFSpeechRecognizer?
    private var recognitionRequest: SFSpeechAudioBufferRecognitionRequest?
    private var recognitionTask: SFSpeechRecognitionTask?
    private let audioEngine = AVAudioEngine()
    
    private let speechSynthesizer = AVSpeechSynthesizer()
    
    init() {
        speechRecognizer = SFSpeechRecognizer(locale: Locale(identifier: "en-US"))
        speechSynthesizer.delegate = self
    }
    
    func requestAuthorization() async -> Bool {
        await withCheckedContinuation { continuation in
            SFSpeechRecognizer.requestAuthorization { status in
                continuation.resume(returning: status == .authorized)
            }
        }
    }
    
    func startListening() throws {
        // Cancel previous task if any
        recognitionTask?.cancel()
        recognitionTask = nil
        
        // Configure audio session
        let audioSession = AVAudioSession.sharedInstance()
        try audioSession.setCategory(.record, mode: .measurement, options: .duckOthers)
        try audioSession.setActive(true, options: .notifyOthersOnDeactivation)
        
        recognitionRequest = SFSpeechAudioBufferRecognitionRequest()
        guard let recognitionRequest = recognitionRequest else {
            throw VoiceError.recognitionRequestFailed
        }
        
        recognitionRequest.shouldReportPartialResults = true
        
        let inputNode = audioEngine.inputNode
        
        recognitionTask = speechRecognizer?.recognitionTask(with: recognitionRequest) { result, error in
            if let result = result {
                DispatchQueue.main.async {
                    self.recognizedText = result.bestTranscription.formattedString
                }
            }
            
            if error != nil || result?.isFinal == true {
                self.audioEngine.stop()
                inputNode.removeTap(onBus: 0)
                self.recognitionRequest = nil
                self.recognitionTask = nil
                
                DispatchQueue.main.async {
                    self.isListening = false
                }
            }
        }
        
        let recordingFormat = inputNode.outputFormat(forBus: 0)
        inputNode.installTap(onBus: 0, bufferSize: 1024, format: recordingFormat) { buffer, _ in
            recognitionRequest.append(buffer)
        }
        
        audioEngine.prepare()
        try audioEngine.start()
        
        isListening = true
    }
    
    func stopListening() {
        audioEngine.stop()
        recognitionRequest?.endAudio()
        isListening = false
    }
    
    func speak(_ text: String) {
        let utterance = AVSpeechUtterance(string: text)
        utterance.voice = AVSpeechSynthesisVoice(language: "en-US")
        utterance.rate = 0.5
        
        speechSynthesizer.speak(utterance)
        isSpeaking = true
    }
    
    func stopSpeaking() {
        speechSynthesizer.stopSpeaking(at: .immediate)
        isSpeaking = false
    }
    
    func clearRecognizedText() {
        recognizedText = ""
    }
}

extension VoiceManager: AVSpeechSynthesizerDelegate {
    func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer, didFinish utterance: AVSpeechUtterance) {
        DispatchQueue.main.async {
            self.isSpeaking = false
        }
    }
}

enum VoiceError: Error {
    case recognitionRequestFailed
    case audioEngineError
}
```

---

## 💾 Memory Manager

**Android (Kotlin):**

```kotlin
// ChatMemoryManager.kt
class ChatMemoryManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("sparki_chat_memory", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    fun saveMessages(personalityId: String, messages: List<Message>) {
        val json = gson.toJson(messages)
        sharedPreferences.edit()
            .putString("messages_$personalityId", json)
            .putLong("last_saved_$personalityId", System.currentTimeMillis())
            .apply()
    }
    
    fun loadMessages(personalityId: String): List<Message> {
        val json = sharedPreferences.getString("messages_$personalityId", null) ?: return emptyList()
        val type = object : TypeToken<List<Message>>() {}.type
        return gson.fromJson(json, type)
    }
    
    fun clearMessages(personalityId: String) {
        sharedPreferences.edit()
            .remove("messages_$personalityId")
            .remove("last_saved_$personalityId")
            .apply()
    }
    
    fun shouldAutoReset(personalityId: String): Boolean {
        val lastSaved = sharedPreferences.getLong("last_saved_$personalityId", 0)
        val hoursSinceLastSave = (System.currentTimeMillis() - lastSaved) / (1000 * 60 * 60)
        return hoursSinceLastSave >= 24
    }
}
```

**iOS (Swift):**

```swift
// ChatMemoryManager.swift
class ChatMemoryManager {
    private let userDefaults = UserDefaults.standard
    private let encoder = JSONEncoder()
    private let decoder = JSONDecoder()
    
    func saveMessages(_ messages: [Message], personalityId: String) {
        do {
            let data = try encoder.encode(messages)
            userDefaults.set(data, forKey: "messages_\(personalityId)")
            userDefaults.set(Date(), forKey: "last_saved_\(personalityId)")
        } catch {
            print("Failed to save messages: \(error)")
        }
    }
    
    func loadMessages(personalityId: String) -> [Message] {
        guard let data = userDefaults.data(forKey: "messages_\(personalityId)") else {
            return []
        }
        
        do {
            return try decoder.decode([Message].self, from: data)
        } catch {
            print("Failed to load messages: \(error)")
            return []
        }
    }
    
    func clearMessages(personalityId: String) {
        userDefaults.removeObject(forKey: "messages_\(personalityId)")
        userDefaults.removeObject(forKey: "last_saved_\(personalityId)")
    }
    
    func shouldAutoReset(personalityId: String) -> Bool {
        guard let lastSaved = userDefaults.object(forKey: "last_saved_\(personalityId)") as? Date else {
            return false
        }
        
        let hoursSinceLastSave = Date().timeIntervalSince(lastSaved) / 3600
        return hoursSinceLastSave >= 24
    }
    
    func getConversationContext(personalityId: String) -> [Message] {
        let messages = loadMessages(personalityId: personalityId)
        // Return last 10 messages for context
        return Array(messages.suffix(10))
    }
    
    func clearAllMessages() {
        let allPersonalities = AIPersonalities.getAllPersonalities()
        for personality in allPersonalities {
            clearMessages(personalityId: personality.id)
        }
    }
}
```

---

## 🎨 UI Components

### Message Bubble

**Android (Kotlin):**

```kotlin
@Composable
fun MessageBubble(
    message: Message,
    onSpeakClick: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isFromUser) UserMessageBackground else AIMessageBackground
            ),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Image if present
                message.imageUri?.let { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = "Shared image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Message text
                Text(
                    text = message.content,
                    color = if (message.isFromUser) Color.White else TextOnAIMessage
                )
                
                // Speaker icon for AI messages
                if (!message.isFromUser) {
                    IconButton(onClick = { onSpeakClick(message.content) }) {
                        Icon(Icons.Default.VolumeUp, contentDescription = "Speak")
                    }
                }
            }
        }
    }
}
```

**iOS (Swift):**

```swift
// MessageBubble.swift
struct MessageBubble: View {
    let message: Message
    let onSpeakClick: (String) -> Void
    
    var body: some View {
        HStack {
            if message.isFromUser {
                Spacer()
            }
            
            VStack(alignment: message.isFromUser ? .trailing : .leading, spacing: 8) {
                // Image if present
                if let imageUri = message.imageUri,
                   let url = URL(string: imageUri) {
                    AsyncImage(url: url) { image in
                        image
                            .resizable()
                            .aspectRatio(contentMode: .fit)
                            .frame(maxHeight: 200)
                            .cornerRadius(8)
                    } placeholder: {
                        ProgressView()
                    }
                }
                
                // Message text
                Text(message.content)
                    .padding(12)
                    .background(message.isFromUser ? Color.blue : Color(hex: "#E3F2FD"))
                    .foregroundColor(message.isFromUser ? .white : .black)
                    .cornerRadius(18)
                
                // Speaker button for AI messages
                if !message.isFromUser {
                    Button(action: { onSpeakClick(message.content) }) {
                        Image(systemName: "speaker.wave.2.fill")
                            .foregroundColor(.blue)
                    }
                }
            }
            .frame(maxWidth: 280)
            
            if !message.isFromUser {
                Spacer()
            }
        }
        .padding(.horizontal, 8)
        .padding(.vertical, 4)
    }
}
```

---

### Chat Screen

**Android (Kotlin):** See `ChatScreen.kt` (lines 51-614)

**iOS (Swift):**

```swift
// ChatView.swift
struct ChatView: View {
    @StateObject private var viewModel = ChatViewModel()
    @StateObject private var voiceManager = VoiceManager()
    
    @State private var messageText = ""
    @State private var selectedImage: UIImage?
    @State private var showImagePicker = false
    @State private var showCamera = false
    @State private var showPersonalitySelector = false
    @State private var showStartFreshAlert = false
    
    var body: some View {
        VStack(spacing: 0) {
            // Header
            headerView
            
            // Messages List
            ScrollViewReader { proxy in
                ScrollView {
                    LazyVStack(spacing: 8) {
                        if viewModel.messages.isEmpty {
                            WelcomeMessage(personality: viewModel.currentPersonality)
                        } else {
                            ForEach(viewModel.messages) { message in
                                MessageBubble(message: message) { text in
                                    voiceManager.speak(text)
                                }
                                .id(message.id)
                            }
                        }
                        
                        if viewModel.isLoading {
                            TypingIndicator()
                        }
                    }
                    .padding()
                }
                .onChange(of: viewModel.messages.count) { _ in
                    if let lastMessage = viewModel.messages.last {
                        withAnimation {
                            proxy.scrollTo(lastMessage.id, anchor: .bottom)
                        }
                    }
                }
            }
            
            // Input Area
            inputView
        }
        .sheet(isPresented: $showPersonalitySelector) {
            PersonalitySelectorView(
                personalities: AIPersonalities.getAllPersonalities(),
                currentPersonality: viewModel.currentPersonality
            ) { personality in
                viewModel.changePersonality(personality)
                showPersonalitySelector = false
            }
        }
        .alert("Start Fresh", isPresented: $showStartFreshAlert) {
            Button("Cancel", role: .cancel) { }
            Button("Confirm", role: .destructive) {
                viewModel.startFresh()
            }
        } message: {
            Text("Start over? AI will forget this chat.")
        }
    }
    
    private var headerView: some View {
        HStack {
            Text(viewModel.currentPersonality.name)
                .font(.headline)
            
            if voiceManager.isSpeaking {
                Image(systemName: "speaker.wave.2.fill")
                    .foregroundColor(.blue)
            }
            
            Spacer()
            
            Button("Personalities ✨") {
                showPersonalitySelector = true
            }
        }
        .padding()
        .background(Color.white)
        .shadow(radius: 2)
    }
    
    private var inputView: some View {
        VStack(spacing: 8) {
            // Selected image preview
            if let image = selectedImage {
                HStack {
                    Image(uiImage: image)
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                        .frame(height: 60)
                        .cornerRadius(8)
                    
                    Text("Image selected")
                    
                    Spacer()
                    
                    Button(action: { selectedImage = nil }) {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundColor(.red)
                    }
                }
                .padding(.horizontal)
            }
            
            // Voice listening indicator
            if voiceManager.isListening {
                HStack {
                    Image(systemName: "mic.fill")
                        .foregroundColor(.blue)
                    Text("Listening...")
                        .foregroundColor(.blue)
                }
            }
            
            // Text field
            TextEditor(text: $messageText)
                .frame(height: 80)
                .padding(8)
                .overlay(
                    RoundedRectangle(cornerRadius: 8)
                        .stroke(Color.gray, lineWidth: 1)
                )
                .padding(.horizontal)
            
            // Action buttons
            HStack(spacing: 20) {
                // Image picker
                Button(action: { showImagePicker = true }) {
                    Image(systemName: "photo.on.rectangle")
                        .font(.title2)
                }
                
                // Microphone
                Button(action: {
                    if voiceManager.isListening {
                        voiceManager.stopListening()
                    } else {
                        Task {
                            if await voiceManager.requestAuthorization() {
                                try? voiceManager.startListening()
                            }
                        }
                    }
                }) {
                    Image(systemName: voiceManager.isListening ? "mic.slash.fill" : "mic.fill")
                        .font(.title2)
                        .foregroundColor(voiceManager.isListening ? .red : .blue)
                }
                
                // Send button
                Button(action: sendMessage) {
                    Image(systemName: "paperplane.fill")
                        .font(.title2)
                        .padding()
                        .background(Color.blue)
                        .foregroundColor(.white)
                        .clipShape(Circle())
                }
                
                // Start fresh
                Button(action: { showStartFreshAlert = true }) {
                    Image(systemName: "plus.circle")
                        .font(.title2)
                }
            }
            .padding()
        }
        .background(Color.white)
        .shadow(radius: 2)
        .onChange(of: voiceManager.recognizedText) { newValue in
            if !newValue.isEmpty {
                messageText = newValue
                voiceManager.clearRecognizedText()
            }
        }
    }
    
    private func sendMessage() {
        guard !messageText.isEmpty || selectedImage != nil else { return }
        
        let imageUri: String? = nil // Convert selectedImage to URI/data if needed
        
        viewModel.sendMessage(
            content: messageText,
            imageUri: imageUri,
            messageType: selectedImage != nil ? .textWithImage : .text
        )
        
        messageText = ""
        selectedImage = nil
    }
}

struct WelcomeMessage: View {
    let personality: AIPersonality
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Welcome! 🔥")
                .font(.title)
                .fontWeight(.bold)
            
            Text(personality.greeting)
                .font(.body)
            
            Text("You can type, use voice input, or share images.")
                .font(.caption)
                .opacity(0.8)
        }
        .padding()
        .background(Color(hex: "#E3F2FD"))
        .cornerRadius(12)
        .padding()
    }
}

struct TypingIndicator: View {
    var body: some View {
        HStack {
            Text("AI is typing")
            ProgressView()
                .scaleEffect(0.7)
        }
        .padding()
        .background(Color(hex: "#E3F2FD"))
        .cornerRadius(12)
    }
}
```

---

## 🔧 Configuration

### API Key Storage

**Android:**

```kotlin
// local.properties
GEMINI_API_KEY=your_api_key_here
```

**iOS:**

```swift
// Config.swift
struct Config {
    static let geminiAPIKey: String = {
        guard let path = Bundle.main.path(forResource: "Secrets", ofType: "plist"),
              let dict = NSDictionary(contentsOfFile: path),
              let key = dict["GEMINI_API_KEY"] as? String else {
            fatalError("API Key not found")
        }
        return key
    }()
}

// Secrets.plist (not in Git)
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>GEMINI_API_KEY</key>
    <string>YOUR_API_KEY_HERE</string>
</dict>
</plist>
```

---

## ✅ Implementation Checklist

- [ ] Port all 6 personality definitions with exact text
- [ ] Implement Gemini API integration (same endpoints)
- [ ] Build chat UI matching Android design
- [ ] Add voice input using Speech framework
- [ ] Add voice output using AVSpeechSynthesizer
- [ ] Implement camera/photo picker
- [ ] Add persistent storage (UserDefaults or CoreData)
- [ ] Implement auto-reset (24 hours)
- [ ] Add "Start Fresh" functionality
- [ ] Test all personalities
- [ ] Test voice features
- [ ] Test image features
- [ ] Match Android UI styling

---

**Questions?** Reference the Android code directly or ask the Android development team.
