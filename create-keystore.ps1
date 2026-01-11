
$keystorePath = "C:\Users\Jerry\AndroidStudioProjects\SparkiFire\sparkifire-release.jks"

# Remove existing keystore if it exists
if (Test-Path $keystorePath) {
    Remove-Item $keystorePath -Force
    Write-Host "Removed existing keystore"
}

# Create keystore using keytool
Write-Host "Creating keystore..."
$process = Start-Process -FilePath keytool -ArgumentList `
    "-genkey", `
    "-v", `
    "-keystore", $keystorePath, `
    "-keyalg", "RSA", `
    "-keysize", "2048", `
    "-validity", "10000", `
    "-alias", "sparkifire", `
    "-storepass", "sparkifire123", `
    "-keypass", "sparkifire123", `
    "-dname", "CN=SparkiFire" `
    -NoNewWindow -Wait -PassThru

Write-Host "Process exit code: $($process.ExitCode)"

# Verify keystore was created
if (Test-Path $keystorePath) {
    $fileInfo = Get-Item $keystorePath
    Write-Host "Keystore created successfully!"
    Write-Host "Path: $($fileInfo.FullName)"
    Write-Host "Size: $($fileInfo.Length) bytes"
} else {
    Write-Host "ERROR: Keystore file not created!"
}
