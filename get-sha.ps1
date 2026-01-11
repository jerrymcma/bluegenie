# Get SHA-1 from release keystore
$keystoreFile = "sparkifire-release.jks"
$keystorePass = "chipper"
$keyAlias = "key0"
$keyPass = "chipper"

Write-Host "===================================" -ForegroundColor Cyan
Write-Host "Getting SHA-1 from Release Keystore" -ForegroundColor Cyan
Write-Host "===================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Keystore: $keystoreFile"
Write-Host "Alias: $keyAlias"
Write-Host ""

# Find keytool
$keytoolPaths = @(
    "C:\Program Files\Common Files\Oracle\Java\javapath\keytool.exe",
    "C:\Program Files\Java\jdk*\bin\keytool.exe",
    "C:\Program Files\Eclipse Adoptium\jdk*\bin\keytool.exe"
)

$keytool = $null
foreach ($path in $keytoolPaths) {
    $resolved = Get-Item $path -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($resolved) {
        $keytool = $resolved.FullName
        break
    }
}

# Try keytool in PATH
if (-not $keytool) {
    $keytool = (Get-Command keytool -ErrorAction SilentlyContinue).Source
}

if (-not $keytool) {
    Write-Host "ERROR: keytool not found" -ForegroundColor Red
    Write-Host "Please install Java JDK" -ForegroundColor Red
    exit 1
}

Write-Host "Using keytool: $keytool" -ForegroundColor Green
Write-Host ""
Write-Host "Extracting SHA fingerprints..." -ForegroundColor Yellow
Write-Host ""

# Run keytool and extract SHA
& $keytool -list -v -keystore $keystoreFile -alias $keyAlias -storepass $keystorePass -keypass $keyPass | Select-String -Pattern "SHA1|SHA256"

Write-Host ""
Write-Host "===================================" -ForegroundColor Cyan
Write-Host "NEXT STEPS:" -ForegroundColor Cyan
Write-Host "===================================" -ForegroundColor Cyan
Write-Host "1. Copy the SHA1 fingerprint above" -ForegroundColor White
Write-Host "2. Go to: https://console.cloud.google.com/" -ForegroundColor White
Write-Host "3. Navigate to: APIs & Services > Credentials" -ForegroundColor White
Write-Host "4. Create OAuth client ID > Android" -ForegroundColor White
Write-Host "5. Enter:" -ForegroundColor White
Write-Host "   - Package name: com.sparkiai.app" -ForegroundColor Yellow
Write-Host "   - SHA-1: (paste the SHA1 from above)" -ForegroundColor Yellow
Write-Host "6. Click CREATE" -ForegroundColor White
Write-Host ""
