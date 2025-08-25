Signing (local setup)

This project expects a local `key.properties` (at the repo root) containing your release signing credentials. Do NOT commit this file.

1. Generate a keystore (example):

   keytool -genkeypair -v -keystore keystore/release.keystore -alias release_key -keyalg RSA -keysize 2048 -validity 10000

2. Copy `key.properties.template` -> `key.properties` and fill values:

   storeFile=keystore/release.keystore
   storePassword=...your store password...
   keyAlias=release_key
   keyPassword=...your key password...

3. Build a signed bundle locally:

   .\gradlew.bat :app:bundleRelease

On CI, configure the same properties as protected secrets and write them into `key.properties` at runtime before the build.
