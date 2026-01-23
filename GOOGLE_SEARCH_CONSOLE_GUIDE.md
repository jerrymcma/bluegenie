# Blue Genie SEO & Google Search Console Setup Guide

This guide helps you establish the SEO presence for **Blue Genie** (`bluegeniemagic.com`) and optimize Google Search visibility for both your web and Android apps.

## ✅ What I've Done for You
1.  **Web App SEO**:
    *   **Updated `sitemap.xml`**: Points to `https://bluegeniemagic.com` and includes Privacy/Terms pages.
    *   **Updated `robots.txt`**: Points to the new sitemap.
    *   **Created `assetlinks.json`**: Prepared the Digital Asset Links file needed for Android App verification.

2.  **Android App SEO**:
    *   **Updated `AndroidManifest.xml`**: Added the Deep Link configuration for `bluegeniemagic.com`. This allows your app to handle web links directly.

---

## 🚀 Phase 1: Google Search Console (GSC) Setup

### 1. Add the New Property
1.  Go to [Google Search Console](https://search.google.com/search-console).
2.  Click the property dropdown (top left) > **"Add property"**.
3.  **Crucial Step**: Choose **"Domain"** type.
4.  Enter: `bluegeniemagic.com`
5.  Click **Continue**.

### 2. Verify Ownership (DNS Method)
1.  Copy the **TXT record** provided by GSC.
2.  Log in to your domain registrar (e.g., GoDaddy, Namecheap, Vercel).
3.  Add a new DNS record:
    *   **Type**: `TXT`
    *   **Host**: `@`
    *   **Value**: Paste the code.
4.  Save and wait 5-10 mins.
5.  Click **Verify** in GSC.

### 3. Submit Your Sitemap
1.  In GSC sidebar, go to **Indexing** > **Sitemaps**.
2.  Enter `sitemap.xml`.
3.  Click **Submit**.

---

## 📱 Phase 2: Android App Links (Critical Step)

For your Android app to open automatically when users click `bluegeniemagic.com` links in Google Search, you must complete this step.

### 1. Get Your SHA-256 Fingerprint
Since I cannot access your secure signing keys, you need to copy this value from Google Play Console:
1.  Go to [Google Play Console](https://play.google.com/console).
2.  Select **Blue Genie**.
3.  Go to **Release** > **Setup** > **App signing**.
4.  Look for **"App signing key certificate"**.
5.  Copy the **SHA-256 certificate fingerprint**.

### 2. Update `assetlinks.json`
1.  Open the file `bluegenie-web/public/.well-known/assetlinks.json` in your code editor.
2.  Replace the text `"REPLACE_WITH_YOUR_RELEASE_KEY_SHA256"` with your copied fingerprint.
    *   Example: `"AA:BB:CC:11:22..."`
3.  **Deploy your web app**.
    *   This file must be accessible at `https://bluegeniemagic.com/.well-known/assetlinks.json`.

### 3. Link in Google Play Console
1.  In Google Play Console, go to **Setup** > **App content**.
2.  Find **"Deep links"**.
3.  Add `bluegeniemagic.com`.
4.  Google will verify the `assetlinks.json` file you just deployed.

---

## 🔄 Phase 3: Transition & Optimization

### 1. Transition from Sparki AI
*   **Change of Address**: In GSC, use the "Change of Address" tool in the old `sparkiai.app` property to move signals to `bluegeniemagic.com`.
*   **Redirects**: Ensure `sparkiai.app` redirects to the new domain.

### 2. Immediate SEO Actions
*   **Request Indexing**: In GSC, inspect `https://bluegeniemagic.com/` and click **"Request Indexing"**.
*   **Social**: Update all social media profiles to the new URL.
