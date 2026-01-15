# ✅ SparkiFire Web - Setup Checklist

## Pre-Flight Check

Before starting, verify:

- [ ] Node.js is installed (version 18+)
  ```powershell
  node --version
  ```

- [ ] You're in the SparkiFire project root
  ```powershell
  pwd
  # Should show: .../AndroidStudioProjects/SparkiFire
  ```

- [ ] You can see the `sparkifire-web` folder
  ```powershell
  ls sparkifire-web
  ```

## Installation Steps

- [ ] Navigate to web directory
  ```powershell
  cd sparkifire-web
  ```

- [ ] Install dependencies (first time only)
  ```powershell
  npm install
  ```
  ⏱️ Takes 1-2 minutes, downloads ~200MB

- [ ] Verify installation completed
    - [ ] No error messages in terminal
    - [ ] `node_modules` folder created
    - [ ] `package-lock.json` created

## First Run

- [ ] Start the development server
  ```powershell
  npm run dev
  ```

- [ ] Watch for Windows Firewall popup
    - [ ] If it appears, click "Allow access"
    - [ ] Check "Private networks" box
    - [ ] Click "Allow access" button

- [ ] Verify server started
    - [ ] Terminal shows "VITE ready" message
    - [ ] Shows Local URL (http://localhost:3000)
    - [ ] Shows Network URL (http://192.168.x.x:3000)

- [ ] Browser opens automatically
    - [ ] If not, manually open: http://localhost:3000

## Testing Features

Once the app loads, test these:

### Basic UI

- [ ] See SparkiFire logo and "Sparki" header
- [ ] See welcome message with greeting
- [ ] See message input box at bottom
- [ ] See "Personalities ✨" button at top

### Personalities

- [ ] Click "Personalities ✨" button
- [ ] Modal opens with 10 personality cards
- [ ] Can click on different personalities
- [ ] Modal closes when selecting one
- [ ] Personality name updates in header

### Chat Features

- [ ] Type a message in the input box
- [ ] Click "Send" button
- [ ] Message appears in blue bubble
- [ ] "AI is typing" indicator appears
- [ ] AI response appears in gradient bubble
- [ ] Can click speaker icon on AI message
- [ ] Text-to-speech plays the response

### Voice Input (Chrome/Edge only)

- [ ] Click microphone button
- [ ] "Listening..." indicator appears
- [ ] Browser asks for microphone permission (first time)
- [ ] Click "Allow"
- [ ] Speak a message
- [ ] Text appears in input box
- [ ] Can send the message

### Image Upload

- [ ] Click image/photo button
- [ ] Dropdown shows Camera and Gallery options
- [ ] Click Gallery
- [ ] File picker opens
- [ ] Select an image
- [ ] Image preview appears above input
- [ ] Can send message with image

### Persistence

- [ ] Send a few messages
- [ ] Refresh the page (F5)
- [ ] Messages are still there
- [ ] Switch to different personality
- [ ] Switch back
- [ ] Original messages restored

### Start Fresh

- [ ] Click the "+" button
- [ ] Confirmation dialog appears
- [ ] Click "Confirm"
- [ ] Messages cleared
- [ ] Greeting message appears

## Troubleshooting Checklist

If something doesn't work:

### Server Won't Start

- [ ] Check Node.js version: `node --version`
- [ ] Delete and reinstall:
  ```powershell
  Remove-Item -Recurse node_modules
  Remove-Item package-lock.json
  npm install
  ```
- [ ] Try different port:
  ```powershell
  npm run dev -- --port 3001
  ```

### Firewall Blocking

- [ ] Read `FIREWALL_FIX.md`
- [ ] Open Windows Defender Firewall
- [ ] Allow Node.js through firewall
- [ ] Restart server

### Browser Issues

- [ ] Try different URL:
    - [ ] http://localhost:3000
    - [ ] http://127.0.0.1:3000
    - [ ] http://192.168.x.x:3000 (your IP)
- [ ] Clear browser cache (Ctrl+Shift+Delete)
- [ ] Try different browser (Chrome, Edge)
- [ ] Check browser console (F12) for errors

### Voice Not Working

- [ ] Using Chrome or Edge browser?
- [ ] Microphone permission granted?
- [ ] Microphone working in other apps?
- [ ] Try HTTPS in production (localhost works too)

### AI Not Responding

- [ ] Check `.env` file has API keys
- [ ] Check browser console (F12) for errors
- [ ] Verify internet connection
- [ ] Check Gemini API key is valid

## Success Criteria

You'll know everything is working when:

- ✅ Server starts without errors
- ✅ Browser opens automatically
- ✅ Can see and interact with UI
- ✅ Can send messages and get AI responses
- ✅ Can switch between personalities
- ✅ Messages persist after refresh
- ✅ Voice input works (Chrome/Edge)
- ✅ Text-to-speech works
- ✅ Can upload images
- ✅ No errors in browser console

## Quick Reference Commands

```powershell
# First time setup
cd sparkifire-web
npm install
npm run dev

# Subsequent runs
cd sparkifire-web
npm run dev

# Stop server
Press Ctrl+C in terminal

# Fresh install
cd sparkifire-web
Remove-Item -Recurse node_modules
Remove-Item package-lock.json
npm install

# Build for production
npm run build

# Preview production build
npm run preview
```

## Documentation Reference

If you need help:

- **Quick Start:** `QUICK_START.md`
- **Firewall Issues:** `FIREWALL_FIX.md`
- **Full Documentation:** `README.md`
- **What's Different:** `DIFFERENCES_FROM_LAST_NIGHT.md`
- **Project Overview:** `../WEB_VERSION_COMPLETE.md`
- **Quick Root Guide:** `../START_WEB_VERSION.md`

## Status Tracking

Mark your progress:

- [ ] Installation completed
- [ ] Server starts successfully
- [ ] Firewall configured
- [ ] Basic chat working
- [ ] All personalities working
- [ ] Voice features tested
- [ ] Image upload tested
- [ ] Ready to use!

## 🎉 All Done?

If you checked all the boxes in "Success Criteria", congratulations!

Your SparkiFire web version is fully functional! 🔥🚀

Time to chat with Sparki on the web! 💬✨
