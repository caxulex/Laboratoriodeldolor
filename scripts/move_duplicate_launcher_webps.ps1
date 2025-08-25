# Moves density-specific ic_launcher webp files out of res/mipmap-* into a backup folder
# Safe: only affects files named ic_launcher.webp and ic_launcher_round.webp outside mipmap-anydpi-v26

param(
    [string]$ResDir = "$PSScriptRoot\..\app\src\main\res",
    [string]$BackupDir = "$PSScriptRoot\backup-launcher-webps"
)

$ResDir = (Resolve-Path $ResDir).Path
if (-not (Test-Path $ResDir)) {
    Write-Error "Resource dir not found: $ResDir"
    exit 1
}

if (-not (Test-Path $BackupDir)) { New-Item -ItemType Directory -Path $BackupDir | Out-Null }
$log = "$PSScriptRoot\move-duplicate-launcher-webps.log"
"Log start: $(Get-Date -Format o)" | Out-File $log -Encoding UTF8

$files = Get-ChildItem -Path $ResDir -Recurse -Include 'ic_launcher.webp','ic_launcher_round.webp' |
    Where-Object { $_.DirectoryName -notmatch 'mipmap-anydpi-v26' }

if ($files.Count -eq 0) {
    "No duplicate launcher webp files found." | Tee-Object -FilePath $log -Append
    exit 0
}

foreach ($f in $files) {
    try {
        $timestamp = Get-Date -Format "yyyyMMddHHmmss"
        $safeName = "{0}_{1}{2}" -f $f.Directory.Name, $f.BaseName, $timestamp
        $ext = $f.Extension
        $target = Join-Path $BackupDir ($safeName + $ext)
        Move-Item -LiteralPath $f.FullName -Destination $target -Force
        "MOVED: $($f.FullName) -> $target" | Tee-Object -FilePath $log -Append
    } catch {
        "FAILED: $($f.FullName) - $($_.Exception.Message)" | Tee-Object -FilePath $log -Append
    }
}

"Log end: $(Get-Date -Format o)" | Out-File $log -Append -Encoding UTF8

Write-Output "Done. Log: $log; Backup dir: $BackupDir"
