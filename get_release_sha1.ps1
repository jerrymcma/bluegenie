$keytool = "C:\Program Files\Java\jdk-25\bin\keytool.exe"
$keystore = "C:\Users\Jerry\AndroidStudioProjects\SparkiFire\sparkifire-release77.jks"
& $keytool -list -v -keystore $keystore -alias key1 -storepass chipper1 -keypass chipper1 | Select-String "SHA1"