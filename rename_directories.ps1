$basePaths = @(
    'C:\Users\Jerry\AndroidStudioProjects\SparkiFire\app\src\main\java\com',
    'C:\Users\Jerry\AndroidStudioProjects\SparkiFire\app\src\androidTest\java\com',
    'C:\Users\Jerry\AndroidStudioProjects\SparkiFire\app\src\test\java\com'
)

foreach($basePath in $basePaths) {
    if(Test-Path $basePath) {
        $oldPath = Join-Path $basePath 'sparkiai'
        $newPath = Join-Path $basePath 'bluegenie'
        
        if(Test-Path $oldPath) {
            Rename-Item -Path $oldPath -NewName 'bluegenie' -Force
            Write-Host "Renamed: $oldPath to $newPath"
        }
    }
}
