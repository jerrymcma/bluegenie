Write-Host "Searching for Java installations..." -ForegroundColor Cyan
Write-Host ""

# Method 1: Check registry
Write-Host "Checking Windows Registry..." -ForegroundColor Yellow
$regPaths = @(
    'HKLM:\SOFTWARE\JavaSoft\Java Development Kit',
    'HKLM:\SOFTWARE\JavaSoft\JDK',
    'HKLM:\SOFTWARE\WOW6432Node\JavaSoft\Java Development Kit',
    'HKLM:\SOFTWARE\WOW6432Node\JavaSoft\JDK'
)

foreach ($regPath in $regPaths) {
    try {
        $versions = Get-ChildItem -Path $regPath -ErrorAction SilentlyContinue
        foreach ($version in $versions) {
            $props = Get-ItemProperty -Path $version.PSPath -ErrorAction SilentlyContinue
            if ($props.JavaHome) {
                Write-Host "  Found: $($props.JavaHome)" -ForegroundColor Green
            }
        }
    } catch {}
}

# Method 2: Search common locations
Write-Host ""
Write-Host "Searching common installation directories..." -ForegroundColor Yellow
$searchPaths = @(
    "C:\Program Files\Java",
    "C:\Program Files\Eclipse Adoptium",
    "C:\Program Files\AdoptOpenJDK",
    "C:\Program Files\Zulu",
    "C:\Program Files\Microsoft",
    "C:\Program Files (x86)\Java"
)

foreach ($path in $searchPaths) {
    if (Test-Path $path) {
        $jdks = Get-ChildItem -Path $path -Filter "*jdk*" -Directory -ErrorAction SilentlyContinue
        foreach ($jdk in $jdks) {
            $javaBin = Join-Path $jdk.FullName "bin\java.exe"
            if (Test-Path $javaBin) {
                Write-Host "  Found: $($jdk.FullName)" -ForegroundColor Green
            }
        }
    }
}

# Method 3: Use current java command
Write-Host ""
Write-Host "Checking current java command..." -ForegroundColor Yellow
$javaCmd = Get-Command java -ErrorAction SilentlyContinue
if ($javaCmd) {
    Write-Host "  Java executable: $($javaCmd.Source)" -ForegroundColor Green
    
    # Try to get JAVA_HOME from parent directories
    $javaBin = Split-Path $javaCmd.Source -Parent
    $javaHome = Split-Path $javaBin -Parent
    
    if (Test-Path (Join-Path $javaHome "bin\keytool.exe")) {
        Write-Host "  JAVA_HOME should be: $javaHome" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "Use this path for gradle.properties" -ForegroundColor Yellow
        Write-Output $javaHome
    }
}

Write-Host ""
