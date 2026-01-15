# 🚀 Quick Start Guide - SparkiFire Web

## For Windows Users (Super Easy!)

### Method 1: Double-Click to Start ⚡

1. Double-click `start.bat` in the `sparkifire-web` folder
2. Wait for it to install dependencies (first time only)
3. Browser will open automatically at `http://localhost:3000`
4. Start chatting! 🔥

### Method 2: PowerShell/CMD

```bash
cd sparkifire-web
npm install
npm run dev
```

## 🔴 If You Get a Firewall Warning

When you first run the server, Windows might show a firewall popup:

**"Windows Defender Firewall has blocked some features of Node.js"**

### Quick Fix:

1. Click "Allow access" on Private networks
2. (Optional) Also check Public networks if needed
3. Click "Allow access" button

### Already Blocked? Unblock Node.js:

1. Press `Windows + R`
2. Type `firewall.cpl` and press Enter
3. Click "Allow an app or feature through Windows Defender Firewall"
4. Click "Change settings" (needs admin)
5. Find "Node.js" in the list
6. Check the boxes next to it (Private and/or Public)
7. Click OK

## 🌐 Accessing the App

### On the same computer:

- `http://localhost:3000`
- `http://127.0.0.1:3000`

### From other devices (phone, tablet):

1. Find your computer's IP address:
   ```bash
   ipconfig
   ```
   Look for "IPv4 Address" (e.g., 192.168.1.100)

2. On the other device, open browser:
   ```
   http://192.168.1.100:3000
   ```

## ❓ Troubleshooting

### Port 3000 is busy?

Don't worry! Vite will automatically use port 3001, 3002, etc.
Just look at the terminal output to see which port it chose.

### "Cannot find module" error?

```bash
cd sparkifire-web
rm -rf node_modules
npm install
```

### Still can't access?

1. Check if the server is running (you should see "Local: http://localhost:3000" in terminal)
2. Try turning off Windows Firewall temporarily (for testing)
3. Make sure you're in the `sparkifire-web` folder
4. Run as Administrator: Right-click PowerShell → "Run as Administrator"

## ✅ System Requirements

- Windows 10/11
- Node.js 18 or higher (check with `node --version`)
- Modern browser (Chrome, Edge, Firefox, or Safari)
- Internet connection (for AI responses)

## 🎤 Voice Features

**For best experience with voice input:**

- Use Chrome or Edge browser
- Allow microphone permission when prompted
- Make sure your microphone is working

## 📱 Mobile Access

The web app works great on phones and tablets!
Just access it through your mobile browser using your computer's IP address.

## 🎉 You're Ready!

Once the server starts, you'll see:

```
  VITE v5.0.8  ready in 500 ms

  ➜  Local:   http://localhost:3000/
  ➜  Network: http://192.168.1.100:3000/
  ➜  press h to show help
```

Open the URL in your browser and start chatting with Sparki! 🔥

---

**Need more help?** Check the main `README.md` for detailed instructions.
