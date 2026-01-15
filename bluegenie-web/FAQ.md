# ❓ SparkiFire Web - Frequently Asked Questions

## Installation & Setup

### Q: Do I need to install anything before running the web app?

**A:** Yes, you need Node.js version 18 or higher. Check with:

```powershell
node --version
```

If you don't have it, download from: https://nodejs.org

### Q: How long does the first installation take?

**A:** First time running `npm install` takes 1-2 minutes and downloads about 200MB of dependencies.
Subsequent starts are instant.

### Q: Can I run the web and Android apps at the same time?

**A:** Yes! They're completely independent. Your Android app in Android Studio and the web app can
both run simultaneously without any conflicts.

### Q: Will this affect my Android app in any way?

**A:** No! The Android app is completely unchanged. The web version is a separate, standalone
application in its own folder.

## Running the App

### Q: What's the easiest way to start the web app?

**A:** Double-click `start.bat` in the `sparkifire-web` folder (Windows only). For other systems or
manual start:

```powershell
cd sparkifire-web
npm run dev
```

### Q: The browser didn't open automatically, what do I do?

**A:** Manually open your browser and go to `http://localhost:3000`

### Q: Port 3000 is already in use, what now?

**A:** Vite will automatically try port 3001, 3002, etc. Check the terminal output to see which port
it chose.

### Q: How do I stop the server?

**A:** Press `Ctrl+C` in the terminal where the server is running.

### Q: Can I access the web app from my phone?

**A:** Yes! Find your computer's IP address with `ipconfig`, then on your phone's browser go to:
`http://YOUR-IP:3000` (e.g., `http://192.168.1.100:3000`)

## Windows Firewall

### Q: Windows Firewall is blocking Node.js, what do I do?

**A:** When the popup appears, click "Allow access" and check "Private networks". If you already
blocked it, see `FIREWALL_FIX.md` for detailed instructions.

### Q: I accidentally clicked "Block" on the firewall popup!

**A:** No problem! Follow these steps:

1. Press `Windows + R`
2. Type `firewall.cpl` and press Enter
3. Click "Allow an app or feature through Windows Defender Firewall"
4. Click "Change settings"
5. Find Node.js and check the boxes
6. Click OK

### Q: Should I turn off Windows Firewall completely?

**A:** No! Just allow Node.js through it. Turning off the firewall reduces your computer's security.

## Features & Functionality

### Q: Do all 10 personalities work the same as in the Android app?

**A:** Yes! They have the exact same names, descriptions, greetings, and response styles.

### Q: Does the web version use the same AI as the Android app?

**A:** Yes! Both use the same Gemini API with always-on Google Search grounding.

### Q: Will my chat history sync between Android and web?

**A:** No. Each platform stores its own chat history locally. Android uses SharedPreferences, web
uses LocalStorage.

### Q: How long does chat history last?

**A:** Forever, unless you clear it or it auto-resets after 24 hours of inactivity (same as
Android).

### Q: Can I use voice input on any browser?

**A:** Voice input (speech-to-text) works best on Chrome and Edge. Safari has limited support.
Firefox doesn't support it yet.

### Q: Does text-to-speech work on all browsers?

**A:** Yes! Text-to-speech works on all modern browsers (Chrome, Edge, Safari, Firefox).

### Q: Can I upload images like in the Android app?

**A:** Yes! Click the image button and choose Camera or Gallery. Camera access depends on your
device having a camera.

## API & Configuration

### Q: Do I need to set up API keys?

**A:** No! Your API keys were automatically copied from `local.properties` to the `.env` file.

### Q: Are my API keys secure?

**A:** Yes! The `.env` file is in `.gitignore`, so it won't be committed to version control. Keep it
private.

### Q: Can I use different AI providers (Claude, OpenAI)?

**A:** The groundwork is there, but currently only Gemini is implemented. You can add support for
other providers in `src/services/`.

### Q: What happens if my Gemini API key runs out of quota?

**A:** You'll see an error message. Check your Gemini API console for quota limits and billing.

### Q: Can I use the app without an API key?

**A:** No, the web app requires a valid Gemini API key for AI responses.

## Technical Questions

### Q: What if I get "Cannot find module" errors?

**A:** Delete and reinstall dependencies:

```powershell
cd sparkifire-web
Remove-Item -Recurse node_modules
Remove-Item package-lock.json
npm install
```

### Q: The styles aren't loading (no colors/design)?

**A:** This means Tailwind CSS isn't working. Make sure you installed all dependencies:

```powershell
npm install
```

### Q: I see TypeScript errors, what do I do?

**A:** Usually they're just warnings. The app should still run. If it doesn't, check `tsconfig.json`
is present and run:

```powershell
npm install typescript
```

### Q: How do I see errors in the browser?

**A:** Press `F12` to open Developer Tools, then click the "Console" tab. Any errors will show
there.

### Q: Can I run this on Linux or Mac?

**A:** Yes! The app works on all platforms. Use the manual commands instead of `start.bat`.

## Performance

### Q: The app feels slow, what can I do?

**A:**

1. Check your internet connection (AI responses need internet)
2. Clear browser cache (Ctrl+Shift+Delete)
3. Close other browser tabs
4. Check browser console for errors

### Q: Messages take a long time to get AI responses?

**A:** This is normal! Gemini API responses can take 2-10 seconds depending on:

- Your internet speed
- Gemini API server load
- Length of your message
- Whether grounding (Google Search) is used

### Q: Can I speed up the AI responses?

**A:** Not really - it depends on Gemini API speed. But you can:

- Use shorter messages
- Have better internet connection
- Wait patiently (AI is thinking!)

## Data & Privacy

### Q: Where is my chat history stored?

**A:** In your browser's LocalStorage (client-side only). It never leaves your computer except for
API calls to Gemini.

### Q: What data is sent to Gemini?

**A:** Only your messages and conversation context (last 10 message pairs) for better responses.

### Q: Can I export my chat history?

**A:** Not currently implemented, but you could add this feature by reading from LocalStorage and
saving to a file.

### Q: How do I clear all chat history?

**A:** Open browser DevTools (F12), go to Application tab → Local Storage → delete all
`sparkifire_messages_*` entries. Or use the "Start Fresh" button for current personality.

## Deployment

### Q: Can I deploy this to a website?

**A:** Yes! See `README.md` for deployment instructions for Vercel, Netlify, Firebase, etc.

### Q: Will it work on a phone after deployment?

**A:** Yes! The web app is fully responsive and works on all devices.

### Q: Do I need a server to host it?

**A:** No! After building (`npm run build`), you get static files that can be hosted anywhere (CDN,
S3, GitHub Pages, etc.).

### Q: How much does hosting cost?

**A:** Free! Services like Vercel, Netlify, and GitHub Pages offer free hosting for static sites.

### Q: What about HTTPS for voice features?

**A:** Voice input works on localhost without HTTPS. For production, most free hosting services
provide HTTPS automatically.

## Troubleshooting

### Q: Nothing happens when I run npm run dev?

**A:** Check:

1. Are you in the `sparkifire-web` directory?
2. Did you run `npm install` first?
3. Is Node.js installed? (`node --version`)
4. Check for error messages in terminal

### Q: Browser shows "Cannot connect" or "ERR_CONNECTION_REFUSED"?

**A:** The server isn't running. Make sure:

1. Terminal shows "VITE ready" message
2. Server is running (not stopped with Ctrl+C)
3. Using correct port number
4. Windows Firewall allows Node.js

### Q: White screen with no content?

**A:** Check browser console (F12) for errors. Common causes:

- JavaScript errors
- Failed to load dependencies
- Missing files

### Q: AI isn't responding to my messages?

**A:** Check:

1. Internet connection is working
2. API key is valid in `.env` file
3. Browser console (F12) for error messages
4. Gemini API status: https://status.cloud.google.com

### Q: Voice input button does nothing?

**A:**

1. Are you using Chrome or Edge?
2. Did you allow microphone permission?
3. Is your microphone working in other apps?
4. Check browser console for errors

## Comparison Questions

### Q: Should I use the Android app or web app?

**A:** Both! Use:

- **Android app** for mobile, offline capability
- **Web app** for desktop, cross-platform access

### Q: Which has better performance?

**A:** Android app is faster (native code), but web app is pretty quick too!

### Q: Which has more features?

**A:** They're equal! Both have all 10 personalities and all features.

### Q: Can I contribute features to both?

**A:** Yes! You control both codebases. Add features to either or both.

## Development

### Q: How do I add a new AI personality?

**A:** Edit `src/data/personalities.ts` and add your personality to the list.

### Q: How do I change the colors/theme?

**A:** Edit `tailwind.config.js` for Tailwind colors, or `src/index.css` for custom styles.

### Q: Can I add more AI providers besides Gemini?

**A:** Yes! Create new service files in `src/services/` similar to `geminiService.ts`.

### Q: How do I debug the app?

**A:** Use Chrome DevTools (F12):

- Console: See logs and errors
- Network: See API calls
- Application: See LocalStorage
- Elements: Inspect HTML/CSS

### Q: Where do I make changes to the UI?

**A:** Components are in `src/components/` and `src/app/`. Edit `.tsx` files.

## Still Have Questions?

### Check These Resources:

- **Full Documentation:** `README.md`
- **Quick Start:** `QUICK_START.md`
- **Firewall Issues:** `FIREWALL_FIX.md`
- **Visual Guide:** `VISUAL_GUIDE.md`
- **Setup Checklist:** `CHECKLIST.md`

### For Technical Help:

1. Open an issue on GitHub (if you published it)
2. Check browser console (F12) for specific errors
3. Read error messages carefully - they usually tell you what's wrong
4. Google the error message
5. Check Gemini API documentation

### Common Error Messages:

**"Cannot find module"**
→ Run: `npm install`

**"Port 3000 is already in use"**
→ Vite will try next port, or use: `npm run dev -- --port 3001`

**"Failed to fetch"**
→ Check internet connection and API key

**"Permission denied"**
→ Run PowerShell as Administrator

**"EACCES" errors**
→ Permission issues, try running as Administrator

---

## 💡 Pro Tips

1. **Bookmark** http://localhost:3000 when server is running
2. **Keep terminal open** - don't close it while using the app
3. **Use Chrome DevTools** for debugging (F12)
4. **Check console first** when something doesn't work
5. **Read error messages** - they're usually helpful

## 🎉 Still Stuck?

If you can't find your answer here:

1. Check the specific guide for your issue
2. Look at browser console for errors
3. Verify basic setup (Node.js, dependencies, API keys)
4. Try a fresh install
5. Read the error message carefully

Most issues are solved by:

- ✅ Running `npm install`
- ✅ Allowing Node.js through firewall
- ✅ Being in the right directory
- ✅ Having a valid API key

---

**You got this! 🔥🚀**
