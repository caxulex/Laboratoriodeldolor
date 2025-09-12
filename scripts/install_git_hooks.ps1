# Install a pre-commit hook that blocks committing .hprof files
$hookDir = Join-Path (Get-Location) ".git/hooks"
if (-not (Test-Path $hookDir)) {
  Write-Error ".git/hooks directory not found. Run this script from the repo root."
  exit 1
}
$hook = @"
#!/usr/bin/env pwsh
# Block committing heap dumps
$files = git diff --cached --name-only
$bad = $files | Where-Object { $_ -match "\.hprof$" }
if ($bad) {
  Write-Host "ERROR: .hprof files detected in commit:" -ForegroundColor Red
  $bad | ForEach-Object { Write-Host "  - $_" -ForegroundColor Red }
  Write-Host "Remove or ignore these files (scripts/clean_hprof.ps1) before committing." -ForegroundColor Yellow
  exit 1
}
exit 0
"@
$hookPath = Join-Path $hookDir "pre-commit"
$hook | Out-File -FilePath $hookPath -Encoding utf8
# Mark as executable where relevant (Git Bash, etc.)
try { & icacls $hookPath /grant Everyone:RX  | Out-Null } catch {}
Write-Host "Installed pre-commit hook to block .hprof files."