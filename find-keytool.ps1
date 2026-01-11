$javaCmd = Get-Command java -ErrorAction Stop
$javaPath = $javaCmd.Source
$javaParent = Split-Path $javaPath -Parent

# If this is the javapath symlink directory, we need to find the real Java installation
if ($javaParent -like "*javapath*") {
    # Try to find keytool through environment or registry
    $javaHomeEnv = [System.Environment]::GetEnvironmentVariable("JAVA_HOME", "Machine")
    if ($javaHomeEnv) {
        $keytoolPath = Join-Path $javaHomeEnv "bin\keytool.exe"
        if (Test-Path $keytoolPath) {
            Write-Output $keytoolPath
            exit 0
        }
    }
    
    # Search common Java installation locations
    $searchPaths = @(
        "C:\Program Files\Java",
        "C:\Program Files\Eclipse Adoptium",
        "C:\Program Files (x86)\Java"
    )
    
    foreach ($searchPath in $searchPaths) {
        if (Test-Path $searchPath) {
            $keytool = Get-ChildItem -Path $searchPath -Filter "keytool.exe" -Recurse -ErrorAction SilentlyContinue | Select-Object -First 1
            if ($keytool) {
                Write-Output $keytool.FullName
                exit 0
            }
        }
    }
} else {
    # Normal Java installation
    $javaHome = Split-Path $javaParent -Parent
    $keytoolPath = Join-Path $javaHome "bin\keytool.exe"
    if (Test-Path $keytoolPath) {
        Write-Output $keytoolPath
        exit 0
    }
}

Write-Output "NOT_FOUND"
exit 1
