# SparkiFire AI 🔥

A cutting-edge Android AI assistance app built with Jetpack Compose and Kotlin, featuring voice
interaction, image sharing, and 10 AI models.

## ✨ Features

### 🎤 Voice Input/Output

- **Speech-to-Text**: Speak your messages naturally
- **Text-to-Speech**: AI responses read aloud with speaker button
- **Visual Indicators**: See when AI is listening or speaking
- **Seamless Toggle**: Switch between voice and text input instantly

### 📸 Image Sharing & Analysis

- **Camera Integration**: Capture photos directly in-app
- **Gallery Access**: Select images from your device
- **Image Preview**: Review before sending
- **Multi-Modal Messages**: Combine text with images
- **AI Image Understanding**: Simulated visual analysis responses

### 🎭 6 Unique AI Personalities

Choose your favorite AI companion:

1. **SparkiFire** (Default) - Friendly and helpful assistant
2. **Alex Pro** - Professional business consultant
3. **Luna Creative** - Artistic and imaginative companion
4. **Code Master** - Technical programming expert
5. **Joke Bot** - Humorous entertainer
6. **Buddy** - Casual, chill friend

Each personality has:

- Unique greeting and response style
- Custom color theme
- Personality-specific reactions
- Context-aware conversations

### 💬 Interactive Chat Interface

- **Modern UI**: Clean Material Design 3 interface
- **Message Bubbles**: Distinct styles for user and AI messages
- **Auto-Scrolling**: Smooth navigation to latest messages
- **Typing Indicators**: Visual feedback during AI processing
- **Loading States**: Clear communication of app status

### 🎨 Additional Features

- **Edge-to-Edge Design**: Modern immersive experience
- **Dynamic Theming**: App bar color changes with personality
- **Multiple Input Methods**: Text, voice, or image
- **Smooth Animations**: Polished transitions throughout
- **Error Handling**: Graceful degradation and clear messaging

## 🏗️ Architecture

Built with modern Android development best practices:

- **MVVM Pattern**: Clean separation of concerns
- **Jetpack Compose**: Declarative UI framework
- **Kotlin Coroutines**: Efficient async operations
- **StateFlow**: Reactive state management
- **Repository Pattern**: Data layer abstraction
- **Material Design 3**: Latest design guidelines

## 📱 Technology Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Networking**: Retrofit2 (ready for AI API integration)
- **Async**: Kotlin Coroutines and Flow
- **Image Loading**: Coil
- **Speech**: Android Speech Recognition & TTS
- **Camera**: AndroidX Camera & Activity Result APIs

## 📁 Project Structure

```
app/src/main/java/com/example/sparkifire/
├── model/
│   ├── Message.kt                    # Chat message data class
│   ├── AIPersonality.kt              # AI personality definitions
│   └── MessageType.kt                # Message type enum
├── ui/
│   ├── components/
│   │   ├── MessageBubble.kt          # Chat bubble component
│   │   └── PersonalitySelector.kt    # Personality picker UI
│   ├── screens/
│   │   └── ChatScreen.kt             # Main chat interface
│   └── theme/
│       ├── Color.kt                  # Color definitions
│       ├── Theme.kt                  # Material theme setup
│       └── Type.kt                   # Typography
├── viewmodel/
│   └── ChatViewModel.kt              # Chat logic & state
├── repository/
│   └── AIRepository.kt               # AI response generation
├── network/
│   └── AIApiService.kt               # API service interface
├── utils/
│   └── VoiceManager.kt               # Voice I/O handling
└── MainActivity.kt                    # App entry point
```

## 🎯 Key Features Demonstration

### Voice Interaction

```kotlin
// Speak your message
val voiceManager = VoiceManager(context)
voiceManager.startListening()

// AI speaks response
voiceManager.speak("Hello! How can I help you?")
```

### Personality System

```kotlin
// Switch personalities
viewModel.changePersonality(AIPersonalities.CREATIVE)
// Get personality-specific response
aiRepository.getAIResponse("Hello", personality)
```

### Image Handling

```kotlin
// Send image with text
viewModel.sendMessage(
    content = "What do you see?",
    imageUri = imageUri.toString(),
    messageType = MessageType.TEXT_WITH_IMAGE
)
```

## 🚀 Getting Started

### Prerequisites

- Android Studio Hedgehog or later
- Android SDK 24+
- Kotlin 2.0+

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd SparkiFire
   ```

2. **Open in Android Studio**
    - Launch Android Studio
    - Select "Open an existing project"
    - Navigate to the SparkiFire directory

3. **Sync Project**
    - Android Studio will automatically sync Gradle dependencies
    - Wait for sync to complete

4. **Run the App**
    - Connect an Android device or start an emulator (API 24+)
    - Click the Run button ▶️ or press Shift+F10
    - Grant permissions when prompted (microphone, camera, storage)

## 🎮 How to Use

### Text Chat

1. Type your message in the input field
2. Tap the send button or press Enter
3. AI responds based on selected personality

### Voice Chat

1. Tap the microphone icon 🎤
2. Speak your message
3. Tap again to stop listening
4. AI responds (tap speaker icon 🔊 on AI messages for audio)

### Image Sharing

1. Tap the photo library icon 🖼️
2. Choose Camera 📷 or Gallery
3. Select/capture your image
4. Add optional text description
5. Send and get AI analysis

### Personality Selection

1. Tap the person icon 👤 in the top bar
2. Browse available personalities
3. Select your favorite
4. Experience different response styles!

## 📊 Current AI Capabilities

The demo AI currently responds to:

- ✅ Greetings and introductions
- ✅ Time requests
- ✅ Jokes and humor
- ✅ Weather inquiries
- ✅ Android development questions
- ✅ General conversation
- ✅ Image descriptions (simulated)

## 🔮 Ready for Real AI Integration

The app is architected to easily connect with real AI services:

### Supported AI Services

- OpenAI GPT-3.5/4
- Google Gemini
- Anthropic Claude
- Custom AI endpoints

### Integration Steps

1. Add your API key to build configuration
2. Update `AIRepository.kt` with API calls
3. Configure authentication headers
4. Deploy and enjoy real AI conversations!

### Example Integration

```kotlin
// In AIRepository.kt
suspend fun getAIResponse(userMessage: String, personality: AIPersonality?): String {
    val apiService = RetrofitClient.aiApiService
    val response = apiService.getChatCompletion(
        AIRequest(
            message = userMessage,
            personality = personality?.responseStyle
        )
    )
    return response.text
}
```

## 🎨 Customization

### Adding New Personalities

```kotlin
val CUSTOM = AIPersonality(
    id = "custom",
    name = "Your Custom AI",
    description = "Your unique assistant",
    greeting = "Custom greeting message",
    responseStyle = ResponseStyle.FRIENDLY,
    color = 0xFFYOURCOLOR
)
```

### Modifying Themes

Edit `ui/theme/Color.kt` and `Theme.kt` to customize:

- Color schemes
- Typography
- Shapes and spacing

## 📝 Dependencies

Key dependencies in `gradle/libs.versions.toml`:

```toml
[versions]
compose-bom = "2024.12.01"
kotlin = "2.0.21"
retrofit = "2.11.0"
coil = "2.7.0"

[libraries]
# Jetpack Compose
compose-bom
compose-material3
compose-ui

# Architecture Components
lifecycle-viewmodel-compose
navigation-compose

# Networking
retrofit
okhttp

# Image Loading
coil-compose

# Coroutines
kotlinx-coroutines-android
```

## 🛡️ Permissions

Required permissions in `AndroidManifest.xml`:

- `INTERNET` - API communication
- `RECORD_AUDIO` - Voice input
- `CAMERA` - Photo capture
- `READ_MEDIA_IMAGES` - Gallery access
- `READ_EXTERNAL_STORAGE` - Image selection

## 🐛 Troubleshooting

### Voice Recognition Not Working

- Ensure microphone permission is granted
- Check device has speech recognition installed
- Verify internet connection for cloud recognition

### Camera Not Opening

- Grant camera permission in app settings
- Check camera hardware availability
- Verify file provider configuration

### Build Errors

- Sync Gradle files
- Clean and rebuild project
- Invalidate caches and restart Android Studio

## 🤝 Contributing

Contributions are welcome! Areas for enhancement:

- Chat history persistence (Room Database)
- Real-time AI API integration
- Additional personality types
- Voice command recognition
- Multi-language support
- Dark theme implementation

## 📄 License

This project is open source and available under the [MIT License](LICENSE).

## 🙏 Acknowledgments

Built with:

- Jetpack Compose for modern Android UI
- Material Design 3 for beautiful components
- Kotlin Coroutines for smooth async operations
- Coil for efficient image loading
- Android Speech APIs for voice interaction

## 📧 Contact & Support

For questions, suggestions, or issues:

- Open an issue on GitHub
- Review the [FEATURES_COMPLETE.md](FEATURES_COMPLETE.md) document
- Check the inline code documentation

---

## 🎉 What's New in Latest Version

### Version 1.0 - Full Feature Release

- ✅ Complete voice input/output system
- ✅ Image sharing with camera and gallery
- ✅ 6 unique AI personalities
- ✅ Beautiful Material Design 3 UI
- ✅ Smooth animations and transitions
- ✅ Production-ready architecture
- ✅ Comprehensive error handling
- ✅ Optimized performance

---

**Built with ❤️ using Jetpack Compose and Kotlin**

**Ready to chat, speak, share, and explore with AI!** 🚀🔥