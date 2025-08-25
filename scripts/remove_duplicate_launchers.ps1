# Moves density-specific adaptive icon XMLs out of res/mipmap-* into a backup folder
# Safe: only affects files named ic_launcher.xml and ic_launcher_round.xml outside mipmap-anydpi-v26

param(
    [string]$ResDir = "$PSScriptRoot\..\app\src\main\res",
    [string]$BackupDir = "$PSScriptRoot\backup-launcher-xmls"
)

$ResDir = (Resolve-Path $ResDir).Path
if (-not (Test-Path $ResDir)) {
    Write-Error "Resource dir not found: $ResDir"
    exit 1
}

if (-not (Test-Path $BackupDir)) { New-Item -ItemType Directory -Path $BackupDir | Out-Null }
$log = "$PSScriptRoot\remove-duplicate-launchers.log"
"Log start: $(Get-Date -Format o)" | Out-File $log -Encoding UTF8

$files = Get-ChildItem -Path $ResDir -Recurse -Include 'ic_launcher.xml','ic_launcher_round.xml' |
    Where-Object { $_.DirectoryName -notmatch 'mipmap-anydpi-v26' }

if ($files.Count -eq 0) {
    "No duplicate adaptive XMLs found." | Tee-Object -FilePath $log -Append
    exit 0
}

foreach ($f in $files) {
    try {
        $target = Join-Path $BackupDir ("{0}_{1}{2}" -f $f.Directory.Name, $f.Name, (Get-Date -Format "yyyyMMddHHmmss"))
        Move-Item -LiteralPath $f.FullName -Destination $target -Force
        "MOVED: $($f.FullName) -> $target" | Tee-Object -FilePath $log -Append
    } catch {
        "FAILED: $($f.FullName) - $($_.Exception.Message)" | Tee-Object -FilePath $log -Append
    }
}

"Log end: $(Get-Date -Format o)" | Out-File $log -Append -Encoding UTF8

Write-Output "Done. Log: $log; Backup dir: $BackupDir"
