# Verify build script for local development
# Runs clean, assemble, unit tests and lint to catch regressions early
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

Write-Host "Running full verification: clean, assembleDebug, testDebugUnitTest, lint"
if (Test-Path ".\gradlew") {
    .\gradlew clean :app:assembleDebug :app:testDebugUnitTest :app:lint --no-daemon
} else {
    Write-Error "gradlew not found in repo root"
}
