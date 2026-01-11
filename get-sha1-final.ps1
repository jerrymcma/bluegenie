# Find keytool and extract SHA-1 from keystore
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Getting SHA-1 from Release Keystore" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$keystoreFile = "sparkifire-release.jks"
$keystorePass = "chipper"
$keyAlias = "key0"
$keyPass = "chipper"

# Find keytool
$keytool = $null

# Method 1: Search in Java installation directories
$searchPaths = @(
    "C:\Program Files\Java\*\bin\keytool.exe",
    "C:\Program Files\Eclipse Adoptium\*\bin\keytool.exe",
    "C:\Program Files\AdoptOpenJDK\*\bin\keytool.exe",
    "C:\Program Files\Microsoft\*\bin\keytool.exe",
    "C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe",
    "C:\Program Files (x86)\Java\*\bin\keytool.exe"
)

foreach ($pattern in $searchPaths) {
    $found = Get-Item $pattern -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($found) {
        $keytool = $found.FullName
        Write-Host "Found keytool: $keytool" -ForegroundColor Green
        break
    }
}

# Method 2: Check if keytool is in PATH
if (-not $keytool) {
    $cmd = Get-Command keytool -ErrorAction SilentlyContinue
    if ($cmd) {
        $keytool = $cmd.Source
        Write-Host "Found keytool in PATH: $keytool" -ForegroundColor Green
    }
}

if (-not $keytool) {
    Write-Host "ERROR: keytool not found on this system" -ForegroundColor Red
    Write-Host ""
    Write-Host "Please install Java JDK or use Android Studio's Gradle panel:" -ForegroundColor Yellow
    Write-Host "  SparkiFire -> app -> Tasks -> android -> signingReport" -ForegroundColor White
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host ""
Write-Host "Extracting SHA fingerprints..." -ForegroundColor Yellow
Write-Host ""

# Run keytool
$output = & $keytool -list -v -keystore $keystoreFile -alias $keyAlias -storepass $keystorePass -keypass $keyPass 2>&1

# Extract SHA lines
$shaLines = $output | Select-String -Pattern "SHA1|SHA256"

if ($shaLines) {
    $shaLines | ForEach-Object {
        Write-Host $_.Line -ForegroundColor Green
    }
    
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "COPY THE SHA1 LINE ABOVE" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "Package name: com.sparkiai.app" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Next steps:" -ForegroundColor White
    Write-Host "1. Go to: https://console.cloud.google.com/" -ForegroundColor White
    Write-Host "2. APIs & Services -> Credentials" -ForegroundColor White
    Write-Host "3. CREATE CREDENTIALS -> OAuth client ID -> Android" -ForegroundColor White
    Write-Host "4. Enter package name and SHA-1" -ForegroundColor White
} else {
    Write-Host "ERROR: Could not read keystore" -ForegroundColor Red
    Write-Host "Make sure sparkifire-release.jks exists in: $PWD" -ForegroundColor Yellow
}

Write-Host ""
Read-Host "Press Enter to exit"
