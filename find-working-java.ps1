$javaCmd = Get-Command java -ErrorAction Stop
$javaExe = $javaCmd.Source

Write-Host "Found java.exe at: $javaExe" -ForegroundColor Green

# Run java -version to get details
Write-Host "`nJava Version:" -ForegroundColor Cyan
& java -version 2>&1

# Try to find the actual Java installation directory
$javaBin = Split-Path $javaExe -Parent

Write-Host "`nJava bin directory: $javaBin" -ForegroundColor Yellow

# Check if this is a symlink directory (like javapath)
if ($javaBin -like "*javapath*") {
    Write-Host "`nThis is a symlink directory. Searching for actual Java installation..." -ForegroundColor Yellow
    
    # Search for Oracle Java installations
    $oracleJava = Get-ChildItem "C:\Program Files\Java" -Filter "jdk*" -Directory -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($oracleJava) {
        $actualJavaHome = $oracleJava.FullName
        Write-Host "Found Oracle JDK: $actualJavaHome" -ForegroundColor Green
        Write-Output $actualJavaHome
        exit 0
    }
}

# If not a symlink, get parent directory
$javaHome = Split-Path $javaBin -Parent
if (Test-Path "$javaHome\bin\javac.exe") {
    Write-Host "`nJAVA_HOME should be: $javaHome" -ForegroundColor Green
    Write-Output $javaHome
} else {
    Write-Host "`nCould not determine JAVA_HOME" -ForegroundColor Red
}
