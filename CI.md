Continuous Integration (GitHub Actions)

This repository includes two non-destructive CI workflows in `.github/workflows/`:

- `ci.yml` — Runs on push and PR for all branches. It performs:
  - `assembleDebug` (build debug APK)
  - `test` (unit tests)
  - `lint` (lint checks)
  - uploads `app-debug.apk` as an artifact

- `release-aab.yml` — Manual workflow (workflow_dispatch) to build a signed AAB.
  - Intended for repository administrators to run when producing a release AAB.
  - Expects GitHub Secrets to be configured:
    - `KEYSTORE_PASSWORD` — password for the keystore
    - `KEY_ALIAS`        — alias of the signing key
    - `KEY_PASSWORD`     — password for the key alias

Notes and usage

- The release workflow writes a minimal `key.properties` using the secrets and expects a `keystore/release.keystore` file to be present in the repository or available via a secure fetch step you add.
- Do NOT commit real keystores or plaintext secrets to the repository. Use GitHub Secrets and protected artifacts.

Local quick-check

Run this locally to mirror the CI basic checks (PowerShell):

```powershell
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-21.0.8.9-hotspot'
$env:PATH = "$env:JAVA_HOME\bin;" + $env:PATH
.\gradlew.bat clean :app:assembleDebug :app:test :app:lint
```

If you want I can:

- Add a small placeholder adaptive icon set under `app/src/main/res/mipmap-*/` to satisfy Play Store requirements.
- Add a workflow step that runs `./gradlew :app:bundleRelease` in a gated branch using uploaded keystore from a private artifact store.

