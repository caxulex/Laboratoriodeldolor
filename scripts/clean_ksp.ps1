# Removes KSP/build caches that can become corrupted and runs a Gradle clean
# Run from repository root: .\scripts\clean_ksp.ps1

Write-Host "Removing common KSP/build caches..."
$paths = @(
    "./app/build/kspCaches",
    "./app/build/generated",
    "./build/kspCaches",
    "./.gradle/ksp",
    "./.gradle/caches/modules-2/files-2.1"
)

foreach ($p in $paths) {
    if (Test-Path $p) {
        Write-Host "Deleting: $p"
        Remove-Item -LiteralPath $p -Recurse -Force -ErrorAction SilentlyContinue
    }
}

Write-Host "Running gradle clean..."
& .\gradlew.bat clean --no-daemon --console=plain

Write-Host "Done. Now re-run your build, e.g. .\gradlew.bat :app:assembleDebug --no-daemon --console=plain"
