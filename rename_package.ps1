$files = Get-ChildItem -Path 'C:\Users\Jerry\AndroidStudioProjects\SparkiFire\app\src' -Filter '*.kt' -Recurse
foreach($file in $files) {
    $content = Get-Content $file.FullName -Raw
    $newContent = $content -replace 'com\.sparkiai\.app', 'com.bluegenie.app'
    if($content -ne $newContent) {
        Set-Content $file.FullName -Value $newContent
        Write-Host "Updated: $($file.FullName)"
    }
}
