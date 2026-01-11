# Fix JAVA_HOME Error

## Problem
JAVA_HOME is set to a directory that doesn't exist:
`C:\Program Files\Eclipse Adoptium\jdk-21.0.9.10-hotspot`

## Solution

### Option 1: Use Android Studio's Gradle (Recommended)

1. In Android Studio, open **File** → **Settings** → **Build, Execution, Deployment** → **Build Tools** → **Gradle**
2. Under **Gradle JDK**, select **Embedded JDK** (or any valid JDK from the dropdown)
3. Click **OK**
4. In Android Studio's terminal, run:
   ```bash
   ./gradlew signingReport
   ```

### Option 2: Fix System JAVA_HOME

1. Search for "Environment Variables" in Windows Start menu
2. Click "Edit the system environment variables"
3. Click "Environment Variables" button
4. Under "System variables", find `JAVA_HOME`
5. Click "Edit"
6. Delete the invalid path OR point it to a valid Java installation
7. Click OK on all dialogs
8. **Restart Android Studio** and your terminal

### Option 3: Use Gradle GUI in Android Studio

1. Click the **Gradle** tool window (elephant icon) on the right side
2. Navigate to: **SparkiFire** → **app** → **Tasks** → **android** → **signingReport**
3. Double-click **signingReport**
4. The SHA-1 will appear in the Build output at the bottom

## What You Need

After getting the SHA-1 fingerprint:

1. Go to https://console.cloud.google.com/
2. Navigate to **APIs & Services** → **Credentials**
3. Create **OAuth client ID** → **Android**
4. Enter:
   - Package name: `com.sparkiai.app`
   - SHA-1: (your fingerprint)
5. Click CREATE

This will fix the "unable to sign in with google" error.
