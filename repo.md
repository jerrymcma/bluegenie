# SparkiFire Repository Overview

## Project Summary

**SparkiFire** is a multi-platform AI assistant application built with multiple personalities, voice interaction, image analysis, and music generation capabilities. The project includes:
- **Android App** - Native Android application using Jetpack Compose and Kotlin
- **Web App** - React/TypeScript web version deployed on Vercel
- **Backend Services** - Supabase for authentication and database, Stripe for payments

### Key Highlights
- 🔥 Multi-platform (Android & Web)
- 🎤 Voice input/output with speech recognition and TTS
- 🎵 AI-powered music generation (Stable Audio, Replicate, Suno APIs)
- 🎭 6 unique AI personalities with custom behaviors
- 💳 Stripe payment integration for premium features
- 👤 Google OAuth authentication
- 📱 Responsive Material Design 3 UI
- 🔐 Secure API key management

---

## Repository Structure

```
SparkiFire/
├── app/                          # Android application (Kotlin + Jetpack Compose)
│   ├── src/main/
│   │   ├── java/com/sparkiai/app/
│   │   │   ├── config/          # Feature flags and configuration
│   │   │   ├── model/           # Data models (Message, AIPersonality, etc.)
│   │   │   ├── network/         # API services (Gemini, Lyria, Replicate, Suno, etc.)
│   │   │   ├── repository/      # AIRepository for data abstraction
│   │   │   ├── ui/
│   │   │   │   ├── components/  # Reusable UI components
│   │   │   │   ├── screens/     # Screen-level compositions
│   │   │   │   └── theme/       # Material Design 3 theme
│   │   │   ├── utils/           # Utility helpers (VoiceManager, StripeCheckoutHelper, etc.)
│   │   │   ├── viewmodel/       # ChatViewModel for state management
│   │   │   └── MainActivity.kt   # App entry point
│   │   └── AndroidManifest.xml   # App permissions and configuration
│   ├── build.gradle.kts          # Android build configuration
│   └── proguard-rules.pro         # ProGuard rules for release builds
│
├── sparkifire-web/               # React web application
│   ├── src/
│   │   ├── components/           # React components (ChatInput, MessageBubble, etc.)
│   │   ├── config/               # Supabase and Stripe configuration
│   │   ├── data/                 # Static data (personalities)
│   │   ├── services/             # External service integrations (voiceService)
│   │   ├── store/                # Zustand state management
│   │   ├── types/                # TypeScript type definitions
│   │   ├── App.tsx               # Root component
│   │   └── main.tsx              # Entry point
│   ├── public/                   # Static assets
│   ├── package.json              # Dependencies and scripts
│   ├── vite.config.ts            # Vite build configuration
│   ├── tailwind.config.js         # Tailwind CSS configuration
│   ├── tsconfig.json              # TypeScript configuration
│   └── vercel.json               # Vercel deployment configuration
│
├── api/                          # Vercel serverless functions
│   ├── gemini.js                 # Google Gemini AI endpoint
│   ├── music.js                  # Music generation endpoint
│   ├── create-checkout.js        # Stripe checkout creation
│   ├── confirm-checkout.js       # Stripe checkout confirmation
│   └── stripe-webhook.js         # Webhook for Stripe events
│
├── handoff/                      # iOS handoff project (Xcode)
│   └── SparkiFire-iOS-Handoff/   # iOS version (incomplete)
│
├── gradle/                       # Gradle wrapper and version catalog
│   └── libs.versions.toml         # Centralized dependency versions
│
├── build.gradle.kts              # Root Gradle configuration
├── settings.gradle.kts           # Gradle settings
├── gradle.properties             # Gradle properties
├── gradlew & gradlew.bat         # Gradle wrapper scripts
│
└── [Documentation Files]         # Extensive guides and setup docs
    ├── README.md                 # Main project documentation
    ├── QUICK_START.md            # Quick start guide
    ├── IOS_DEVELOPMENT_GUIDE.md  # iOS setup documentation
    ├── MONETIZATION_PLAN.md      # Payment and premium features
    └── [50+ other guides]        # Various implementation guides
```

---

## Technology Stack

### Android App
- **Language**: Kotlin 2.0.21
- **UI Framework**: Jetpack Compose (Material Design 3)
- **Architecture**: MVVM with Repository pattern
- **Async**: Kotlin Coroutines & Flow
- **Networking**: Retrofit 2.11.0 + OkHttp
- **Database**: Supabase (postgrest-kt)
- **Authentication**: Google Sign-In + Supabase GoTrue
- **Image Loading**: Coil 2.7.0
- **Serialization**: Kotlin Serialization + Gson
- **Min SDK**: 24 | **Target SDK**: 36

### Web App
- **Framework**: React 18.2.0 + TypeScript 5.2.2
- **Build Tool**: Vite 5.0.8
- **State Management**: Zustand 4.4.7
- **Styling**: Tailwind CSS 3.4.0
- **HTTP Client**: Axios 1.6.2
- **UI Icons**: Lucide React 0.294.0
- **Database**: Supabase JS SDK 2.89.0
- **Payments**: Stripe JS 8.6.0
- **Deployment**: Vercel

### Backend & APIs
- **Authentication**: Supabase (PostgreSQL + GoTrue)
- **Payments**: Stripe (checkout sessions, webhooks)
- **AI Services**: 
  - Google Gemini (chat AI)
  - Stable Audio (music generation)
  - Replicate (music generation)
  - Suno (premium music generation)
  - Lyria (alternative music API)
- **Serverless Functions**: Vercel Edge Functions

---

## Key Features

### 1. **AI Chat with Multiple Personalities**
Six unique AI personalities:
1. **SparkiFire** (default) - Friendly assistant
2. **Alex Pro** - Professional consultant
3. **Luna Creative** - Artistic companion
4. **Code Master** - Programming expert
5. **Joke Bot** - Humorous entertainer
6. **Buddy** - Casual friend

**Files**: `AIPersonality.kt`, `ChatViewModel.kt`, `AIRepository.kt`

### 2. **Voice Interaction**
- Speech-to-Text with Android Speech Recognition
- Text-to-Speech with system TTS engine
- Real-time listening indicators
- Audio response playback on messages

**Files**: `VoiceManager.kt`, `voiceService.ts`

### 3. **Image Analysis**
- Camera capture integration
- Gallery image selection
- Multi-modal message support (text + image)
- Image preview before sending

**Files**: `ChatViewModel.kt` (image handling), `ImagePayload.kt`

### 4. **Music Generation**
Multiple music generation APIs:
- **Stable Audio**: Fast music generation
- **Replicate MusicGen**: High-quality synthesis
- **Suno API**: Premium creative generation
- **Lyria**: Alternative music API

**Files**: 
- `StableAudioService.kt`, `ReplicateService.kt`, `SunoService.kt`, `LyriaService.kt`
- `MusicGenerationTracker.kt`, `MusicLibraryManager.kt`, `MusicPlayer.kt`

### 5. **Authentication & Subscriptions**
- Google OAuth 2.0 sign-in
- Supabase authentication
- Stripe premium subscriptions
- User profile management

**Files**: 
- `GoogleSignInManager.kt`, `SupabaseService.kt`
- `StripeCheckoutHelper.kt`, `Subscription.kt`
- `/api/create-checkout.js`, `/api/confirm-checkout.js`, `/api/stripe-webhook.js`

### 6. **Chat Memory Management**
- Conversation history tracking
- User profile persistence
- Chat metadata storage

**Files**: `ChatMemoryManager.kt`, `chatStore.ts`

---

## Core Files & Components

### Android App

#### Models (`app/src/main/java/.../model/`)
- **`Message.kt`** - Chat message data class with content, sender, timestamp
- **`AIPersonality.kt`** - Personality definitions with colors, styles, greetings
- **`GeneratedMusic.kt`** - Generated music metadata
- **`Subscription.kt`** - User subscription/premium status

#### Network Services (`app/src/main/java/.../network/`)
- **`GeminiAIService.kt`** (29KB) - Google Gemini API integration for chat
- **`StableAudioService.kt`** - Stable Audio API for music generation
- **`ReplicateService.kt`** - Replicate API for MusicGen
- **`SunoService.kt`** - Suno API for premium music generation
- **`LyriaService.kt`** - Lyria music API integration
- **`SupabaseService.kt`** - Supabase database operations
- **`VertexAIAuth.kt`** - Google Vertex AI authentication

#### Repository & State (`app/src/main/java/.../`)
- **`repository/AIRepository.kt`** (34KB) - Core business logic for AI responses, music generation
- **`viewmodel/ChatViewModel.kt`** (51KB) - UI state management with StateFlow

#### Utilities (`app/src/main/java/.../utils/`)
- **`VoiceManager.kt`** - Speech recognition and TTS
- **`GoogleSignInManager.kt`** - OAuth sign-in flow
- **`StripeCheckoutHelper.kt`** - Stripe payment integration
- **`MusicGenerationTracker.kt`** - Track generation requests and quota
- **`MusicLibraryManager.kt`** - Manage user's generated music
- **`MusicPlayer.kt`** - Audio playback control
- **`ChatMemoryManager.kt`** - Chat history persistence

#### UI Components (`app/src/main/java/.../ui/`)
- **`components/`** - Reusable Compose components
- **`screens/`** - Full screen layouts
- **`theme/`** - Material Design 3 colors, typography, theme

### Web App

#### State Management (`sparkifire-web/src/store/`)
- **`chatStore.ts`** - Zustand store for messages, personalities, user state

#### Components (`sparkifire-web/src/components/`)
- **`ChatInput.tsx`** - Message input with file/voice support
- **`MessageBubble.tsx`** - Message display with styling
- **`PersonalitySelector.tsx`** - Select AI personality
- **`MusicGenerationDialog.tsx`** - Music generation UI
- **`MusicLibraryDialog.tsx`** - View/manage generated music
- **`SignInModal.tsx`** - Google OAuth login flow
- **`PremiumUpgradeModal.tsx`** - Subscription upgrade
- **`TypingIndicator.tsx`** - Loading state animation

#### Services (`sparkifire-web/src/services/`)
- **`voiceService.ts`** - Web Speech API for voice input/output

#### Configuration (`sparkifire-web/src/config/`)
- **`supabase.ts`** - Supabase client initialization
- **`stripe.ts`** - Stripe configuration

#### Data (`sparkifire-web/src/data/`)
- **`personalities.ts`** - AI personality definitions matching Android

### API Functions (`api/`)
- **`gemini.js`** - Vercel function for Gemini API (proxies requests)
- **`music.js`** - Music generation API endpoint
- **`create-checkout.js`** - Stripe checkout session creation
- **`confirm-checkout.js`** - Confirm payment status
- **`stripe-webhook.js`** - Handle Stripe events (payment confirmation)

---

## Build & Deployment

### Android Build
```bash
# Located in: app/build.gradle.kts
# Version: 3.0.0 (versionCode: 31)
# Requires: local.properties with API keys (GEMINI_API_KEY, GOOGLE_CLIENT_ID, etc.)
```

**Build Configuration**:
- Release: Minified, proguard enabled, signed with keystore
- Debug: Not minified, unsigned for development
- Compilation SDK: 36 | Target SDK: 36 | Min SDK: 24

**API Keys Required** (in `local.properties`):
- `GEMINI_API_KEY` - Google Gemini API
- `GOOGLE_CLIENT_ID` - Google OAuth Android
- `GOOGLE_WEB_CLIENT_ID` - Google OAuth Web
- `SUPABASE_URL` & `SUPABASE_ANON_KEY` - Database
- `STRIPE_PUBLISHABLE_KEY` - Payment processing
- `STABILITY_API_KEY` - Stable Audio
- `REPLICATE_API_KEY` - MusicGen
- `SUNO_API_KEY` - Suno API
- `WEB_APP_URL` - Web app domain for Stripe redirects

### Web Build
```bash
# vite build           # Production build
# npm run dev          # Development with HMR
# npm run lint         # ESLint check
# npm run test         # Vitest runner
```

**Deployment**: Vercel (automatic on GitHub push)

---

## Project Statistics

- **Android App**: 
  - Main code: ~1MB across multiple services
  - Largest files: ChatViewModel (51KB), AIRepository (34KB), GeminiAIService (29KB)
  - Total dependencies: 25+

- **Web App**: 
  - 29+ TypeScript/React files
  - 40+ dependencies
  - Styling: Tailwind CSS (3.4.0)

- **Documentation**: 
  - 80+ markdown files with setup guides
  - Covers Android, iOS, Web, API setup, troubleshooting

---

## Key Configuration Files

| File | Purpose |
|------|---------|
| `gradle/libs.versions.toml` | Centralized Android dependency versions |
| `app/build.gradle.kts` | Android app build configuration & API keys |
| `sparkifire-web/package.json` | Web app dependencies & scripts |
| `sparkifire-web/vite.config.ts` | Vite build configuration |
| `sparkifire-web/tailwind.config.js` | Tailwind CSS theming |
| `gradle.properties` | Gradle build properties |
| `vercel.json` | Vercel deployment settings |

---

## Development Workflow

### Android Development
1. Load API keys in `local.properties`
2. Sync Gradle in Android Studio
3. Run on emulator/device: Shift+F10
4. Build release: `./gradlew assembleRelease`
5. Deploy AAB to Play Store

### Web Development
1. `npm install` dependencies
2. `npm run dev` - Start dev server with HMR
3. `npm run build` - Build for production
4. Deploy to Vercel (automatic)

### API Setup
- Supabase: PostgreSQL database, user auth, profile management
- Stripe: Payment processing with webhook validation
- Gemini API: AI chat capabilities
- Stable Audio/Replicate/Suno: Music generation APIs

---

## Notable Features & Implementations

### ✨ Advanced Features
1. **Streaming Responses** - Real-time AI responses
2. **Music Generation Quota** - Track user generation limits
3. **Premium Subscriptions** - Stripe integration with usage limits
4. **Chat History** - Persistent message storage
5. **Grounding/Search** - Real-time web search integration
6. **Demo Mode** - Play Store demo mode support

### 🔐 Security Measures
- API keys stored in `local.properties` (not committed)
- Stripe webhook signature verification
- Google OAuth 2.0 authentication
- Supabase RLS (Row Level Security)
- BuildConfig fields for sensitive data

### 📱 Cross-Platform Compatibility
- Android: Min API 24 (Android 7.0)
- Web: Modern browsers (React 18)
- Responsive design with Compose and Tailwind
- Shared personality definitions

---

## Common Tasks

### Adding a New AI Personality
1. Add entry to `AIPersonality.kt` (Android)
2. Add to `personalities.ts` (Web)
3. Update system prompt in `AIRepository.kt`
4. Customize colors in theme files

### Integrating a New AI Service
1. Create `[Service]Service.kt` in network directory
2. Add API key to build configuration
3. Implement in `AIRepository.kt`
4. Add UI for service selection if needed

### Adding Music Generation API
1. Create service file in network directory
2. Add API key configuration
3. Implement in `AIRepository.kt` music methods
4. Add quota tracking to `MusicGenerationTracker.kt`

---

## Useful Documentation Links

- **Android Setup**: `IOS_DEVELOPMENT_GUIDE.md`, `ANDROID_TEAM_HANDOFF_GUIDE.md`
- **Music Features**: `MUSIC_COMPOSER_MASTER_INDEX.md`, `MUSIC_SPARK_COMPLETE_SUMMARY.md`
- **Monetization**: `MONETIZATION_PLAN.md`, `STRIPE_INTEGRATION_GUIDE.md`
- **Deployment**: `BUILD_VERSION_16_INSTRUCTIONS.md`, `RELEASE_INSTRUCTIONS.md`
- **API Setup**: `OAUTH_SETUP_VISUAL_GUIDE.md`, `QUICK_GEMINI_GROUNDING_TEST.md`

---

## Current Version
- **Version**: 3.0.0 (Android) | 1.0.0 (Web)
- **Last Updated**: January 2026
- **Platforms**: Android (native), Web (React)

---

## Quick Start
1. **Android**: Clone → Open in Android Studio → Add `local.properties` → Run
2. **Web**: `npm install` → `npm run dev` → Open http://localhost:5173
3. **Backend**: Requires Supabase and Stripe accounts with proper configuration

---

*Generated: January 2, 2026*
