<#
Safe cleanup script for unused resources reported in lint-baseline.xml
- Scans the lint baseline for issues with id="UnusedResources"
- Extracts resource identifiers like R.drawable.foo, R.mipmap.bar, R.drawable.icon
- For drawable/mipmap image resources it finds matching files under app/src/main/res and moves them to a backup directory
- Leaves value resources (strings, colors, plurals) alone and prints them for manual review

Usage (PowerShell):
  .\cleanup_unused_resources.ps1 -LintBaseline "..\lint-baseline.xml" -ProjectRoot ".."

Defaults assume you run this script from the repo root inside the provided workspace.
This script is conservative: it only moves files that physically exist in res folders and keeps a backup copy.
#>

param(
    [string] $LintBaseline = "./lint-baseline.xml",
    [string] $ProjectRoot = "./",
    [string] $ResPath = "app/src/main/res",
    [string] $BackupDir = "./git-backups/unused-resources-backup-$(Get-Date -Format yyyyMMddHHmmss)"
)

if (-not (Test-Path $LintBaseline)) {
    Write-Error "Lint baseline not found at path: $LintBaseline"
    exit 1
}

Write-Host "Reading lint baseline: $LintBaseline"
[xml]$xml = Get-Content $LintBaseline -Raw

$nodes = $xml.SelectNodes("//issue[@id='UnusedResources']")
if ($nodes -eq $null -or $nodes.Count -eq 0) {
    Write-Host "No UnusedResources entries found in lint baseline. Exiting."
    exit 0
}

# Prepare backup directories
$absBackup = Resolve-Path -LiteralPath $BackupDir -ErrorAction SilentlyContinue
if (-not $absBackup) {
    New-Item -ItemType Directory -Path $BackupDir | Out-Null
}

$projectRootAbs = Resolve-Path -LiteralPath $ProjectRoot
$resFull = Join-Path $projectRootAbs.Path $ResPath
if (-not (Test-Path $resFull)) {
    Write-Error "Resource folder not found: $resFull"
    exit 1
}

Write-Host "Scanning $($nodes.Count) UnusedResources entries..."

$drawableMatches = @()
$otherResources = @()

foreach ($n in $nodes) {
    $msg = $n.GetAttribute('message')
    if (-not $msg) { continue }
    # message looks like: The resource `R.drawable.foo` appears to be unused
    if ($msg -match 'R\.([a-zA-Z0-9_]+)\.([a-zA-Z0-9_]+)') {
        $type = $matches[1]
        $name = $matches[2]
        $entry = @{ Type = $type; Name = $name; Message = $msg }
        if ($type -in @('drawable','mipmap','raw','drawable-nodpi','drawable-anydpi')) {
            $drawableMatches += $entry
        } else {
            $otherResources += $entry
        }
    } else {
        # fallback: print full message for manual inspection
        $otherResources += @{ Type = 'unknown'; Name = $msg; Message = $msg }
    }
}

Write-Host "Found $($drawableMatches.Count) image resources and $($otherResources.Count) other resources referenced as unused."

if ($drawableMatches.Count -gt 0) {
    Write-Host "Preparing to move image files to backup: $BackupDir"
}

# For each drawable/mipmap candidate, find matching files and move them
foreach ($entry in $drawableMatches) {
    $name = $entry.Name
    $type = $entry.Type
    # search for files named exactly name.* in res subfolders (e.g., drawable-*/name.png, drawable/name.xml)
    $found = Get-ChildItem -Path $resFull -Recurse -File -Include "$name.*" -ErrorAction SilentlyContinue
    if ($found -and $found.Count -gt 0) {
        foreach ($f in $found) {
            $relative = $f.FullName.Substring($projectRootAbs.Path.Length).TrimStart('\','/')
            $targetDir = Join-Path $BackupDir ($(Split-Path $f.DirectoryName -Leaf))
            if (-not (Test-Path $targetDir)) { New-Item -ItemType Directory -Path $targetDir | Out-Null }
            $targetPath = Join-Path $targetDir $f.Name
            Write-Host "Moving: $relative -> $targetPath"
            Move-Item -LiteralPath $f.FullName -Destination $targetPath
        }
    } else {
        Write-Host "No files found for resource $type.$name"
    }
}

# Print other resources (strings, colors, plurals) for manual review
if ($otherResources.Count -gt 0) {
    Write-Host "\nOther resource entries (strings/colors/plurals) flagged as unused — review manually."
    $otherResources | Select-Object Type, Name, Message | ForEach-Object {
        Write-Host "- $($_.Type): $($_.Name)"
    }
    Write-Host "\nTo remove string/color/plurals, either edit the appropriate values XML files or run a targeted XML edit — do this only after verifying the resource is truly unused."
}

Write-Host "Cleanup complete. Backup location: $BackupDir"
Write-Host "Run a build now to verify there are no missing resource references. If something was moved incorrectly, you can restore files from the backup folder."

exit 0
