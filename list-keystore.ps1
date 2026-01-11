$output = & "C:\Program Files\Java\jdk-25\bin\keytool.exe" -list -v -keystore sparkifire-release.jks -alias key0 -storepass chipper -keypass chipper 2>&1

Write-Host "Full output:"
Write-Host "============"
$output | ForEach-Object { Write-Host $_ }

Write-Host ""
Write-Host "SHA lines:"
Write-Host "=========="
$output | Select-String "SHA" | ForEach-Object { Write-Host $_.Line -ForegroundColor Green }
