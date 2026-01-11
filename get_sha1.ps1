$keytool = "C:\Program Files\Java\jdk-25\bin\keytool.exe"
$keystore = "$env:USERPROFILE\.android\debug.keystore"
& $keytool -list -v -keystore $keystore -alias androiddebugkey -storepass android -keypass android | Select-String "SHA1"