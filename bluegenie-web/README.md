# SparkiFire Web 🔥

Web-based version of SparkiFire AI - Your intelligent AI assistant with multiple personalities.

This is the **web counterpart** to the Android app, built with React, TypeScript, and Vite. It
mirrors all the features and functionality of the Android version.

## ✨ Features

### 🎭 10 Unique AI Personalities

- **Sparki** - Friendly and helpful assistant
- **Sparki Pro** - Professional business consultant
- **Creative Spark** - Artistic and imaginative companion
- **Code Master Spark** - Technical programming expert
- **Joke Bot Sparki** - Comedy king & laughter generator
- **Buddy Spark** - Casual, fun-loving friend
- **Sparki Love** - Caring and supportive companion
- **Genius Spark** - Super intelligent academic scholar
- **Game Day Spark** - Sports expert & game day analyst
- **Sparki Ultimate** - Most powerful & versatile AI Guru

### 💬 Chat Features

- Real-time AI responses powered by Google Gemini
- Always-on Google Search grounding for current information
- Persistent conversation history per personality
- Auto-reset after 24 hours of inactivity
- Start fresh anytime with a single click

### 🎤 Voice & Media

- Speech-to-text input (Web Speech API)
- Text-to-speech output for AI responses
- Image upload and sharing
- Camera capture support (on supported devices)

### 🎨 Modern UI

- Beautiful gradient design
- Responsive layout (works on desktop, tablet, mobile)
- Smooth animations and transitions
- Dark scrollbars
- Clean message bubbles

## 🚀 Quick Start

### Prerequisites

- Node.js 18+ installed
- npm or yarn package manager

### Installation

1. **Navigate to the web directory:**
   ```bash
   cd sparkifire-web
   ```

2. **Install dependencies:**
   ```bash
   npm install
   ```

3. **Environment setup:**
   Your API keys are already configured in `.env` file (copied from `local.properties`):
    - ✅ Gemini API Key
    - ✅ Claude API Key
    - ✅ OpenAI API Key

4. **Start the development server:**
   ```bash
   npm run dev
   ```

5. **Open in browser:**
   The app will automatically open at `http://localhost:3000`

   If port 3000 is busy, Vite will use the next available port (3001, 3002, etc.)

### 🔥 Troubleshooting Windows Firewall

If you had issues with Windows Firewall last night, here are the solutions:

#### Option 1: Allow Node.js through Windows Firewall (Recommended)

1. Press `Windows + R`, type `firewall.cpl`, press Enter
2. Click "Allow an app or feature through Windows Defender Firewall"
3. Click "Change settings" (requires admin)
4. Click "Allow another app..."
5. Browse to `C:\Program Files\nodejs\node.exe`
6. Add it and check both Private and Public boxes
7. Click OK

#### Option 2: Temporarily disable Windows Firewall (For Testing Only)

1. Press `Windows + R`, type `firewall.cpl`, press Enter
2. Click "Turn Windows Defender Firewall on or off"
3. Select "Turn off" for Private network (do NOT turn off for Public)
4. Click OK
5. **Remember to turn it back on after testing!**

#### Option 3: Use Different Port

Edit `vite.config.ts` and change the port:

```typescript
server: {
  port: 5173, // Try a different port
  // ...
}
```

#### Option 4: Run as Administrator

Right-click your terminal (PowerShell/CMD) and select "Run as Administrator", then:

```bash
cd sparkifire-web
npm run dev
```

#### Option 5: Access via Network IP

If localhost doesn't work, try your machine's IP address:

```bash
# Find your IP
ipconfig

# Look for "IPv4 Address" under your active network adapter
# Then access: http://192.168.x.x:3000
```

The server is configured with `host: '0.0.0.0'` so it accepts connections from any interface.

## 📁 Project Structure

```
sparkifire-web/
├── src/
│   ├── app/
│   │   └── ChatScreen.tsx          # Main chat interface
│   ├── components/
│   │   ├── ChatInput.tsx           # Message input with voice & image
│   │   ├── MessageBubble.tsx       # Chat message display
│   │   ├── PersonalitySelector.tsx # Personality picker modal
│   │   ├── TypingIndicator.tsx     # AI typing animation
│   │   └── WelcomeMessage.tsx      # Initial greeting
│   ├── data/
│   │   └── personalities.ts        # AI personality definitions
│   ├── services/
│   │   ├── geminiService.ts        # Gemini API integration
│   │   ├── storageService.ts       # LocalStorage persistence
│   │   └── voiceService.ts         # Web Speech API wrapper
│   ├── store/
│   │   └── chatStore.ts            # Zustand state management
│   ├── types/
│   │   └── index.ts                # TypeScript type definitions
│   ├── App.tsx                     # Root component
│   ├── main.tsx                    # React entry point
│   └── index.css                   # Global styles + Tailwind
├── .env                            # Environment variables (API keys)
├── .env.example                    # Example env file
├── index.html                      # HTML entry point
├── package.json                    # Dependencies & scripts
├── tailwind.config.js              # Tailwind CSS config
├── tsconfig.json                   # TypeScript config
├── vite.config.ts                  # Vite bundler config
└── README.md                       # This file
```

## 🎯 Available Scripts

```bash
# Start development server
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview

# Lint code
npm run lint
```

## 🔧 Technology Stack

- **Framework:** React 18
- **Language:** TypeScript
- **Build Tool:** Vite
- **State Management:** Zustand
- **Styling:** Tailwind CSS
- **Icons:** Lucide React
- **HTTP Client:** Axios
- **AI Service:** Google Gemini API
- **Storage:** Browser LocalStorage
- **Voice:** Web Speech API

## 🌐 Browser Compatibility

### Full Support

- ✅ Chrome 90+
- ✅ Edge 90+
- ✅ Safari 15+
- ✅ Firefox 88+

### Voice Features

Voice input (speech-to-text) requires:

- Chrome/Edge (best support)
- Safari 14.1+
- Not available in Firefox

Text-to-speech works in all modern browsers.

## 🔑 API Keys

The app uses the same API keys as your Android app:

1. **Gemini** (Primary) - For AI responses with Google Search grounding
2. **Claude** (Optional) - Ready for integration
3. **OpenAI** (Optional) - Ready for integration

Keys are loaded from `.env` file which was auto-populated from your `local.properties`.

## 📱 Comparison: Web vs Android

| Feature | Android App | Web App |
|---------|------------|---------|
| AI Personalities | ✅ 10 personalities | ✅ 10 personalities |
| Gemini Integration | ✅ Always-on grounding | ✅ Always-on grounding |
| Voice Input | ✅ Android Speech API | ✅ Web Speech API |
| Voice Output | ✅ TTS | ✅ TTS |
| Image Sharing | ✅ Camera + Gallery | ✅ Camera + Upload |
| Chat Persistence | ✅ Per personality | ✅ Per personality |
| Auto Reset | ✅ 24 hours | ✅ 24 hours |
| Conversation Context | ✅ Last 10 pairs | ✅ Last 10 pairs |
| Offline Support | ✅ Native | ❌ Requires internet |
| Platform | Android 7.0+ | All modern browsers |

## 🚀 Deployment Options

### Option 1: Vercel (Recommended)

```bash
npm install -g vercel
vercel
```

### Option 2: Netlify

```bash
npm run build
# Drag and drop the 'dist' folder to netlify.com
```

### Option 3: GitHub Pages

```bash
npm run build
# Push 'dist' folder to gh-pages branch
```

### Option 4: Your Own Server

```bash
npm run build
# Upload 'dist' folder to your web server
```

## 🔒 Security Notes

- API keys are in `.env` file (git-ignored)
- Never commit `.env` to version control
- For production, use environment variables on your hosting platform
- LocalStorage is used for chat history (client-side only)

## 🐛 Known Issues & Solutions

### Issue: Port 3000 already in use

**Solution:** Vite will automatically try the next available port, or manually specify:

```bash
npm run dev -- --port 3001
```

### Issue: "Cannot find module" errors

**Solution:** Delete node_modules and reinstall:

```bash
rm -rf node_modules package-lock.json
npm install
```

### Issue: Tailwind styles not loading

**Solution:** Make sure PostCSS is configured:

```bash
npm install -D tailwindcss postcss autoprefixer
```

### Issue: Voice input not working

**Solution:**

- Use Chrome or Edge browser
- Allow microphone permissions when prompted
- HTTPS is required for production (works on localhost)

### Issue: Build fails with TypeScript errors

**Solution:** Check tsconfig.json or skip type checking:

```bash
npm run build -- --mode production
```

## 💡 Tips & Best Practices

1. **Development:** Use Chrome DevTools for debugging
2. **Testing:** Test voice features in Chrome first
3. **Performance:** Clear LocalStorage if you have thousands of messages
4. **Mobile:** Works on mobile browsers, but native app recommended for best experience
5. **API Limits:** Be mindful of Gemini API rate limits during development

## 🤝 Contributing

This web version mirrors the Android app. When adding features:

1. Check the Android implementation first
2. Match the behavior and UI patterns
3. Keep both versions in sync
4. Test on multiple browsers

## 📧 Support

If you encounter issues:

1. Check this README's troubleshooting section
2. Review browser console for errors
3. Verify API keys are correctly set in `.env`
4. Make sure you're using a supported browser

## 🎉 What's Working

✅ All 10 AI personalities with unique styles
✅ Real-time Gemini AI responses  
✅ Always-on Google Search grounding
✅ Voice input and output
✅ Image upload and camera capture
✅ Persistent chat history per personality
✅ Auto-reset after 24 hours
✅ Beautiful responsive UI
✅ Smooth animations
✅ Works on desktop, tablet, and mobile browsers

## 🔥 Ready to Chat!

Your web version of SparkiFire is complete and ready to use. It has the same features and AI
capabilities as your Android app!

```bash
cd sparkifire-web
npm install
npm run dev
```

Then open your browser and start chatting! 🚀

---

**Built with ❤️ to mirror the Android experience on the web**
