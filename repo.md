# SparkiFire Repository Overview

## Summary
Multi-platform AI assistant with chat, voice, image understanding, and music generation. Includes native Android (Jetpack Compose) app, React/Vite web app with Vercel serverless APIs, and an iOS handoff project. Supabase handles auth/profile data; Stripe manages premium subscriptions.

## Modules
- `app/`: Android app (Kotlin, Jetpack Compose)
- `sparkifire-web/`: React + TypeScript web app (Vite, Tailwind, Zustand)
- `sparkifire-web/api/`: Vercel serverless functions for chat, music, and Stripe
- `api/`: legacy Vercel stubs (unused handlers)
- `handoff/SparkiFire-iOS-Handoff/`: Xcode handoff project
- Build/ops scripts: `gradlew*`, `build_release*.bat`, `clean_install*.bat`, `vercel.json`

## Tech Stack
- Android: Kotlin 2.0.21, Compose BOM 2024.12.01 (compiler 1.5.15), MVVM, Coroutines/Flow 1.8.1, Retrofit 2.11, OkHttp 4.12, Coil 2.7, Supabase postgrest/gotrue 2.0.3, Google Gemini client 0.9.0, Google Play Services Auth 21.0.0
- Web: React 18, TypeScript 5.2, Vite 5, Tailwind 3.4, Zustand 4.4, Axios 1.6, Supabase JS 2.89, Stripe JS 8.6 (server uses Stripe 20.x)
- Serverless: Node/Vercel functions using axios, Groq chat completions, Stripe checkout/webhooks, Supabase service role

## Android App
- Package: `com.sparkiai.app`; versionName `1.0.7`, versionCode `40`; compile/target SDK 36, min SDK 24
- Core files: `MainActivity.kt`, `viewmodel/ChatViewModel.kt`, `repository/AIRepository.kt`, models in `model/`, UI in `ui/components` and `ui/screens`
- Services: `network/GeminiAIService.kt` (chat & image analysis), music providers (`ReplicateService`, `SunoService`, optional `LyriaService`), `SupabaseService` for auth/subscriptions
- Utilities: `ChatMemoryManager`, `MusicGenerationTracker`, `MusicLibraryManager`, `MusicPlayer`, `StripeCheckoutHelper`, `VoiceManager`
- Feature flags: `config/FeatureFlags.kt` (music provider defaults to Replicate; Suno support, premium quotas, library limits)

## Web App (`sparkifire-web`)
- Entry: `src/main.tsx`, `src/App.tsx`, `src/app/ChatScreen.tsx`
- State: `src/store/chatStore.ts` (messages, personalities, voice state, music credits/library, Supabase auth, premium checkout)
- UI components: chat input/bubbles, personality selector, music generation & library dialogs, premium/upgrade and sign-in modals, typing indicator
- Services: `geminiService.ts` (calls Vercel chat function), `musicService.ts` + `generatedMusicService.ts`, `voiceService.ts`, `supabaseService.ts`, `stripeService.ts`, storage helpers
- Config/data: `config/stripe.ts` (publishable keys, price IDs, mode), `config/supabase.ts` (Supabase client), `data/personalities.ts`
- Styling/build: `index.css`, Tailwind via `tailwind.config.js`, Vite config in `vite.config.ts`
- Scripts: `npm run dev`, `npm run build` (tsc + Vite), `npm run lint`, `npm run test`

## Serverless API (`sparkifire-web/api`)
- `gemini.js`: Groq chat completions with personality prompts and optional conversation context; CORS enabled
- `music.js`/`music-handler.js`: music generation handling
- Stripe: `create-checkout.js`, `confirm-checkout.js`, `stripe-webhook.js` using `_lib/stripeConfig.js`
- Supabase admin helpers: `_lib/supabaseAdmin.js`, `_lib/profileHelpers.js`
- Env highlights: `GROQ_API_KEY` (or `VITE_GROQ_API_KEY`), `SUPABASE_URL`, `SUPABASE_SERVICE_KEY`, Stripe secrets (`STRIPE_SECRET_KEY`/test variants, webhook secret, price IDs, optional intro discount vars), optional `STRIPE_MODE` and publishable keys for client

## Configuration
- Android `local.properties` keys: `GEMINI_API_KEY`, `GOOGLE_CLIENT_ID`, `GOOGLE_WEB_CLIENT_ID`, `SUPABASE_URL`, `SUPABASE_ANON_KEY`, `STRIPE_PUBLISHABLE_KEY`, `STABILITY_API_KEY`, `REPLICATE_API_KEY`, `SUNO_API_KEY`, `WEB_APP_URL`
- Android signing: `keystore.properties` for release signing (referenced in `app/build.gradle.kts`)
- Web env (Vite): `VITE_STRIPE_MODE`, `VITE_STRIPE_LIVE_PUBLISHABLE_KEY`/`VITE_STRIPE_TEST_PUBLISHABLE_KEY`, `VITE_STRIPE_LIVE_PRICE_ID`/`VITE_STRIPE_TEST_PRICE_ID`, optional `VITE_STRIPE_INTRO_PERCENT_OFF`, `VITE_GROQ_API_KEY`; Supabase currently hardcoded in `src/config/supabase.ts` (update for production)
- Serverless env: Stripe secrets, webhook secret, price IDs, Supabase service key, Groq API key

## Build & Run
- Android: `./gradlew assembleDebug` for dev; `./gradlew assembleRelease` for signed release (requires keystore + local.properties). Compose, BuildConfig enabled, ProGuard on release with resource shrinking.
- Web: `cd sparkifire-web && npm install`; `npm run dev` for HMR; `npm run build` for production; `npm run lint` and `npm run test` available
- Vercel: deploys `sparkifire-web` with serverless functions in `sparkifire-web/api`

## Notable Features
- Multi-personality chat with voice input/output and image analysis (Gemini)
- Music generation pipeline with quotas, library, download, and playback; default provider Replicate, optional Suno/Lyria via flags
- Supabase-backed auth/profile tracking; Stripe premium checkout with webhook verification
- Message bookmarking/favorites, auto-reset safety, chat persistence on both platforms
- Responsive UI: Compose Material 3 on Android, Tailwind on Web
