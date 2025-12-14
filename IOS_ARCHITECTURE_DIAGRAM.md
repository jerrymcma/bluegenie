# SparkiFire iOS - Architecture Diagram

**Visual guide to app structure and data flow**

---

## 🏗️ Overall Architecture (MVVM Pattern)

```
┌─────────────────────────────────────────────────────────────┐
│                         iOS APP                             │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐ │
│  │                      VIEWS (SwiftUI)                   │ │
│  │                                                        │ │
│  │  ┌────────────┐  ┌──────────────┐  ┌───────────────┐ │ │
│  │  │ ChatView   │  │ MessageBubble│  │ Personality   │ │ │
│  │  │            │  │              │  │ Selector      │ │ │
│  │  └─────┬──────┘  └──────────────┘  └───────────────┘ │ │
│  │        │                                              │ │
│  └────────┼──────────────────────────────────────────────┘ │
│           │ @StateObject / @Published                      │
│           ▼                                                │
│  ┌────────────────────────────────────────────────────────┐│
│  │              VIEW MODEL (ObservableObject)             ││
│  │                                                        ││
│  │  ┌──────────────────────────────────────────────────┐ ││
│  │  │            ChatViewModel                         │ ││
│  │  │                                                  │ ││
│  │  │  • @Published messages: [Message]               │ ││
│  │  │  • @Published isLoading: Bool                   │ ││
│  │  │  • @Published currentPersonality                │ ││
│  │  │                                                  │ ││
│  │  │  • sendMessage()                                │ ││
│  │  │  • changePersonality()                          │ ││
│  │  │  • startFresh()                                 │ ││
│  │  └────────────┬─────────────────────────────────────┘ ││
│  │               │                                       ││
│  └───────────────┼───────────────────────────────────────┘│
│                  │                                         │
│                  ▼                                         │
│  ┌─────────────────────────────────────────────────────┐  │
│  │                  SERVICES LAYER                      │  │
│  │                                                      │  │
│  │  ┌──────────────┐  ┌───────────────┐  ┌──────────┐ │  │
│  │  │AIRepository  │  │VoiceManager   │  │Memory    │ │  │
│  │  │              │  │               │  │Manager   │ │  │
│  │  └──────┬───────┘  └───────────────┘  └──────────┘ │  │
│  │         │                                           │  │
│  │         ▼                                           │  │
│  │  ┌──────────────┐                                  │  │
│  │  │GeminiAI      │                                  │  │
│  │  │Service       │                                  │  │
│  │  └──────┬───────┘                                  │  │
│  │         │                                           │  │
│  └─────────┼───────────────────────────────────────────┘  │
│            │                                               │
└────────────┼───────────────────────────────────────────────┘
             │
             ▼
   ┌─────────────────────┐
   │   Gemini API        │
   │  (External Service) │
   └─────────────────────┘
```

---

## 📱 Screen Flow

```
┌─────────────────────────────────────────────────────────┐
│                    App Launch                           │
│                                                         │
│  1. Initialize ChatViewModel                           │
│  2. Load saved messages from ChatMemoryManager         │
│  3. Display ChatView                                   │
└─────────────────┬───────────────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────────────────┐
│                   CHAT VIEW                             │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ Header                                              │ │
│ │ ┌──────────────────┐     ┌─────────────────────┐  │ │
│ │ │ Personality Name │     │ Personalities ✨    │  │ │
│ │ └──────────────────┘     └─────────────────────┘  │ │
│ └─────────────────────────────────────────────────────┘ │
│                                                         │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ Messages List (ScrollView + LazyVStack)            │ │
│ │                                                     │ │
│ │  ┌──────────────────┐                              │ │
│ │  │ User Message     │  [Sent by user]              │ │
│ │  └──────────────────┘                              │ │
│ │                                                     │ │
│ │  ┌──────────────────┐                              │ │
│ │  │ AI Message   🔊  │  [AI response]               │ │
│ │  └──────────────────���                              │ │
│ │                                                     │ │
│ │  [More messages...]                                │ │
│ │                                                     │ │
│ │  ┌──────────────────┐                              │ │
│ │  │ Loading...       │  [When AI is responding]     │ │
│ │  └──────────────────┘                              │ │
│ └─────────────────────────────────────────────────────┘ │
│                                                         │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ Input Area                                          │ │
│ │                                                     │ │
│ │  [Image Preview]  [if selected]                    │ │
│ │                                                     │ │
│ │  ┌─────────────────────────────────────┐           │ │
│ │  │ Text Input Field                    │           │ │
│ │  │ (3 lines minimum)                   │           │ │
│ │  └─────────────────────────────────────┘           │ │
│ │                                                     │ │
│ │  [📷]  [🎤]  [➤]  [+]                              │ │
│ │  Photo  Mic  Send  Fresh                           │ │
│ └─────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
```

---

## 🔄 Data Flow

### 1. User Sends Message

```
User types text
     │
     ▼
Taps Send Button
     │
     ▼
ChatView.sendMessage()
     │
     ▼
ChatViewModel.sendMessage(content: "Hello")
     │
     ├─▶ Add user Message to @Published messages array
     │   (UI updates automatically)
     │
     ├─▶ Set isLoading = true
     │   (Loading indicator appears)
     │
     ▼
AIRepository.getAIResponse(message, personality, context)
     │
     ▼
GeminiAIService.generateContent(prompt, history)
     │
     ▼
HTTP POST to Gemini API
     │
     ▼
Receive AI response
     │
     ▼
ChatViewModel receives response
     │
     ├─▶ Add AI Message to @Published messages array
     │   (UI updates automatically)
     │
     ├─▶ Set isLoading = false
     │   (Loading indicator disappears)
     │
     ▼
ChatMemoryManager.saveMessages()
     │
     ▼
Messages persisted to UserDefaults
```

---

### 2. User Changes Personality

```
User taps "Personalities ✨"
     │
     ▼
PersonalitySelectorView appears
     │
     ▼
User selects "Luna Creative"
     │
     ▼
ChatViewModel.changePersonality(lunaCreative)
     │
     ├─▶ Save current messages
     │   ChatMemoryManager.saveMessages(currentPersonality.id)
     │
     ├─▶ Update @Published currentPersonality
     │   (UI header updates automatically)
     │
     ├─▶ Load new personality's messages
     │   ChatMemoryManager.loadMessages(newPersonality.id)
     │
     ▼
@Published messages array updated
     │
     ▼
UI displays new personality's conversation history
```

---

### 3. Voice Input Flow

```
User taps Microphone button
     │
     ▼
VoiceManager.requestAuthorization()
     │
     ├─▶ Permission granted?
     │   Yes → Continue
     │   No  → Show error
     │
     ▼
VoiceManager.startListening()
     │
     ├─▶ Set @Published isListening = true
     │   (Listening indicator appears)
     │
     ├─▶ Configure AVAudioSession
     │
     ├─▶ Start SFSpeechRecognizer
     │
     ▼
Speech recognized in real-time
     │
     ▼
Update @Published recognizedText
     │
     ▼
ChatView observes change
     │
     ▼
Update text input field
     │
     ▼
User taps Send (or mic again to stop)
     │
     ▼
VoiceManager.stopListening()
     │
     ▼
Set @Published isListening = false
     │
     ▼
Proceed with sendMessage() flow
```

---

### 4. Image Capture Flow

```
User taps Photo button
     │
     ▼
Show action sheet (Camera / Gallery)
     │
     ├─▶ User selects Camera
     │   │
     │   ▼
     │   UIImagePickerController (Camera)
     │   │
     │   ▼
     │   User takes photo
     │   │
     │   ▼
     │   Return UIImage
     │
     └─▶ User selects Gallery
         │
         ▼
         PHPickerViewController
         │
         ▼
         User selects photo
         │
         ▼
         Return UIImage
     │
     ▼
Display image preview in input area
     │
     ▼
User adds text (optional) and taps Send
     │
     ▼
ChatViewModel.sendMessage(
    content: "What's in this image?",
    imageUri: imageUri,
    messageType: .textWithImage
)
     │
     ▼
Convert UIImage to base64 Data
     │
     ▼
GeminiAIService.generateContent(prompt, imageData)
     │
     ▼
POST to Gemini Vision API
     │
     ▼
Receive image analysis response
     │
     ▼
Display AI's description/analysis
```

---

## 📦 Data Models

### Message Model

```swift
struct Message: Identifiable, Codable {
    let id: String          // UUID
    let content: String     // Message text
    let isFromUser: Bool    // true = user, false = AI
    let timestamp: Date     // When sent
    let imageUri: String?   // Optional image
    let messageType: MessageType
    let personalityId: String
}

enum MessageType: String, Codable {
    case text
    case image
    case textWithImage
}
```

### AIPersonality Model

```swift
struct AIPersonality: Identifiable, Codable {
    let id: String          // "sparkifire", "alex_pro", etc.
    let name: String        // "SparkiFire", "Alex Pro", etc.
    let description: String // Short description
    let greeting: String    // Welcome message
    let responseStyle: ResponseStyle
    let color: Color        // Theme color
}

enum ResponseStyle: String, Codable {
    case friendly
    case professional
    case creative
    case technical
    case humorous
    case casual
}
```

---

## 🗄️ Persistence Structure

### UserDefaults Storage

```
UserDefaults
│
├─ "messages_sparkifire"
│  └─ JSON Array of Message objects
│
├─ "last_saved_sparkifire"
│  └─ Date (timestamp)
│
├─ "messages_alex_pro"
│  └─ JSON Array of Message objects
│
├─ "last_saved_alex_pro"
│  └─ Date (timestamp)
│
├─ "messages_luna_creative"
│  └─ JSON Array of Message objects
│
└─ ... (for each personality)
```

### Auto-Reset Logic

```
On each sendMessage():
│
▼
Check: (now - last_saved) >= 24 hours?
│
├─ Yes:
│  │
│  ▼
│  Clear messages for this personality
│  │
│  ▼
│  Add auto-reset message:
│  "Memory refreshed! Starting new conversation."
│  │
│  ▼
│  Update last_saved to now
│
└─ No:
   │
   ▼
   Continue normally
```

---

## 🎤 Voice Architecture

### Speech Recognition (Input)

```
┌─────────────────────────────────────┐
│       VoiceManager                  │
│                                     │
│  ┌───────────────────────────────┐ │
│  │   SFSpeechRecognizer          │ │
│  │   (iOS Speech Framework)      │ │
│  └───────────────────────────────┘ │
│                │                   │
│                ▼                   │
│  ┌───────────────────────────────┐ │
│  │   AVAudioEngine               │ │
│  │   (Capture audio)             │ │
│  └───────────────────────────────┘ │
│                │                   │
│                ▼                   │
│  ┌───────────────────────────────┐ │
│  │   Recognition Task            │ │
│  │   (Process audio → text)      │ │
│  └───────────────────────────────┘ │
│                │                   │
└────────────────┼───────────────────┘
                 │
                 ▼
         @Published recognizedText
                 │
                 ▼
            ChatView updates
```

### Text-to-Speech (Output)

```
┌─────────────────────────────────────┐
│       VoiceManager                  │
│                                     │
│  ┌───────────────────────────────┐ │
│  │   AVSpeechSynthesizer         │ │
│  │   (iOS Speech Synthesis)      │ │
│  └───────────────────────────────┘ │
│                │                   │
│                ▼                   │
│  ┌───────────────────────────────┐ │
│  │   AVSpeechUtterance           │ │
│  │   (Text → Audio)              │ │
│  └───────────────────────────────┘ │
│                │                   │
│                ▼                   │
│  ┌───────────────────────────────┐ │
│  │   Audio Output                │ │
│  │   (Speaker)                   │ │
│  └───────────────────────────────┘ │
│                │                   │
└────────────────┼───────────────────┘
                 │
                 ▼
         @Published isSpeaking
                 │
                 ▼
            ChatView updates
```

---

## 🌐 API Request Structure

### Text Message Request

```
POST https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=API_KEY

Body:
{
  "contents": [
    // Conversation history (last 10 messages)
    {
      "role": "user",
      "parts": [{ "text": "Previous user message" }]
    },
    {
      "role": "model",
      "parts": [{ "text": "Previous AI response" }]
    },
    // Current message
    {
      "role": "user",
      "parts": [{ "text": "Current user message" }]
    }
  ],
  "generationConfig": {
    "temperature": 0.7,
    "maxOutputTokens": 1024
  }
}
```

### Image Analysis Request

```
POST https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=API_KEY

Body:
{
  "contents": [
    {
      "role": "user",
      "parts": [
        {
          "inlineData": {
            "mimeType": "image/jpeg",
            "data": "BASE64_ENCODED_IMAGE_DATA"
          }
        },
        {
          "text": "What do you see in this image?"
        }
      ]
    }
  ]
}
```

---

## 🔄 Reactive Updates

### SwiftUI Observation Pattern

```
┌────────────────────────────────────────┐
│     ChatViewModel                      │
│     (ObservableObject)                 │
│                                        │
│  @Published var messages: [Message]   │◀────┐
│  @Published var isLoading: Bool       │     │
│  @Published var currentPersonality    │     │
└────────────────────────────────────────┘     │
                                               │
                                               │ Observes
                                               │ changes
                                               │
┌────────────────────────────────────────┐     │
│     ChatView                           │     │
│                                        │     │
│  @StateObject var viewModel           │─────┘
│                                        │
│  Body automatically rebuilds when      │
│  any @Published property changes       │
└────────────────────────────────────────┘
```

---

## 🎯 Key Architecture Principles

### 1. **Separation of Concerns**

- Views: Display UI only
- ViewModel: Business logic
- Services: External operations
- Models: Data structures

### 2. **Reactive Programming**

- @Published properties
- Views observe changes
- Automatic UI updates

### 3. **Async/Await**

- Network calls don't block UI
- Task { } for concurrent operations
- MainActor for UI updates

### 4. **Persistence**

- Automatic save after each message
- Per-personality storage
- UserDefaults for simple data

### 5. **Dependency Injection**

- Services injected into ViewModel
- Easy to test and mock
- Flexible and maintainable

---

## 📊 Component Relationships

```
ChatView
    │
    ├─▶ Uses ChatViewModel
    │       │
    │       ├─▶ Uses AIRepository
    │       │       │
    │       │       └─▶ Uses GeminiAIService
    │       │
    │       └─▶ Uses ChatMemoryManager
    │
    ├─▶ Uses VoiceManager
    │
    └─▶ Uses MessageBubble (multiple instances)
```

---

## 🔐 Security Architecture

```
┌────────────────────────────────┐
│  Secrets.plist                 │
│  (NOT in Git)                  │
│                                │
│  GEMINI_API_KEY = "..."        │
└────────┬───────────────────────┘
         │
         ▼
┌────────────────────────────────┐
│  Config.swift                  │
│                                │
│  static let apiKey = load()    │
└────────┬───────────────────────┘
         │
         ▼
┌────────────────────────────────┐
│  GeminiAIService               │
│                                │
│  Uses Config.apiKey in         │
│  API requests                  │
└────────────────────────────────┘
```

---

## 🎨 UI Component Tree

```
ChatView
│
├── HeaderView
│   ├── Text (Personality Name)
│   └── Button (Personalities ✨)
│
├── ScrollView
│   └── LazyVStack
│       ├── WelcomeMessage (if empty)
│       ├── MessageBubble (for each message)
│       │   ├── AsyncImage (if image)
│       │   ├── Text (message content)
│       │   └── Button (speaker icon)
│       └── TypingIndicator (if loading)
│
└── InputView
    ├── ImagePreview (if image selected)
    ├── VoiceIndicator (if listening)
    ├── TextEditor (input field)
    └── ActionButtons
        ├── Button (photo)
        ├── Button (microphone)
        ├── Button (send)
        └── Button (start fresh)
```

---

## 🚀 This Architecture Provides

✅ **Scalability** - Easy to add features  
✅ **Testability** - Components are isolated  
✅ **Maintainability** - Clear structure  
✅ **Reactivity** - Automatic UI updates  
✅ **Performance** - Efficient SwiftUI  
✅ **Flexibility** - Easy to modify

---

**Use this diagram as your architectural guide!** 🏗️

Refer to **IOS_TECHNICAL_MAPPING.md** for code implementations.
