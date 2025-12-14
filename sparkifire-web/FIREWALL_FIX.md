# 🔥 Windows Firewall Fix for SparkiFire Web

## The Problem You Had Last Night

When running `npm run dev`, Windows Firewall can block Node.js from accepting network connections,
preventing the web server from starting or being accessible.

## ✅ Permanent Solutions (Pick One)

### Solution 1: Allow Node.js in Firewall (RECOMMENDED)

This is the proper way to fix it:

1. **Open Windows Defender Firewall Settings:**
    - Press `Windows + R`
    - Type: `firewall.cpl`
    - Press Enter

2. **Allow Node.js:**
    - Click "Allow an app or feature through Windows Defender Firewall"
    - Click "Change settings" (you'll need admin rights)
    - Click "Allow another app..." button
    - Click "Browse..." button
    - Navigate to: `C:\Program Files\nodejs\node.exe`
    - Click "Add"
    - Make sure both "Private" and "Public" are checked
    - Click "OK"

3. **Done!** Node.js can now accept connections.

### Solution 2: Run as Administrator

Sometimes running as admin bypasses the issue:

1. Right-click on PowerShell or Command Prompt
2. Select "Run as Administrator"
3. Navigate to the project:
   ```powershell
   cd C:\Users\Jerry\AndroidStudioProjects\SparkiFire\sparkifire-web
   npm run dev
   ```

### Solution 3: Use a Different Port

If port 3000 is being blocked specifically:

Edit `vite.config.ts`:

```typescript
server: {
  port: 5173, // Try this port instead
  host: '0.0.0.0',
  open: true
}
```

### Solution 4: Temporarily Disable Firewall (NOT RECOMMENDED)

**Only for testing! Turn it back on when done!**

1. Press `Windows + R`
2. Type: `firewall.cpl`
3. Click "Turn Windows Defender Firewall on or off"
4. Select "Turn off" for Private network only
5. Click OK
6. **REMEMBER TO TURN IT BACK ON!**

## 🎯 What the Vite Config Already Does

I've configured the Vite server to be more firewall-friendly:

```typescript
server: {
  port: 3000,
  host: '0.0.0.0',      // Accept connections from any interface
  open: true,            // Auto-open browser
  strictPort: false      // Try next port if 3000 is busy
}
```

This means:

- ✅ It will try other ports if 3000 is blocked/busy
- ✅ You can access it via localhost, 127.0.0.1, or your IP
- ✅ It opens the browser automatically

## 🔍 How to Verify It's Working

After starting the server, you should see:

```
  VITE v5.0.8  ready in 500 ms

  ➜  Local:   http://localhost:3000/
  ➜  Network: http://192.168.1.100:3000/
  ➜  press h to show help
```

Try all these URLs in your browser:

- `http://localhost:3000`
- `http://127.0.0.1:3000`
- `http://192.168.1.100:3000` (use your actual IP)

If **any** of them work, you're good! The firewall might just be blocking external access but
allowing localhost.

## 🆘 Still Not Working?

### Check if the server is actually running:

```powershell
# In the terminal where you ran npm run dev
# You should see "VITE ready" message
# If not, check for error messages
```

### Check if Node.js is installed:

```powershell
node --version
# Should show v18.x.x or higher
```

### Check if you're in the right directory:

```powershell
pwd
# Should show: .../SparkiFire/sparkifire-web
```

### Try a fresh install:

```powershell
cd sparkifire-web
Remove-Item -Recurse -Force node_modules
Remove-Item package-lock.json
npm install
npm run dev
```

## 💡 Pro Tips

1. **First Time Running:** Windows will ask for permission - always say "Allow"
2. **Best Browser:** Use Chrome or Edge for full voice features
3. **Mobile Access:** Use your computer's IP address (shown in the Vite output)
4. **Debugging:** Open browser DevTools (F12) to see any errors

## 📞 Quick Reference

| Problem | Solution |
|---------|----------|
| Firewall blocking | Allow Node.js in firewall settings |
| Port 3000 busy | Vite will auto-try 3001, 3002, etc. |
| Can't access localhost | Try 127.0.0.1 or your IP address |
| Permission denied | Run PowerShell as Administrator |
| Module not found | Delete node_modules and reinstall |

## ✅ Success Indicators

You'll know it's working when:

- ✅ Terminal shows "VITE ready" message
- ✅ Browser opens automatically
- ✅ You see the SparkiFire chat interface
- ✅ No error messages in browser console

## 🎉 Once It's Working

After you fix the firewall issue once, it should work every time after that! Node.js will remember
the firewall exception.

---

**Need more help?** Check the main README.md or QUICK_START.md
