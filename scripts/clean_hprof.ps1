param(
  [switch]$Stage
)
# Clean local heap dumps and ensure they're ignored
Write-Host "[clean_hprof] Removing local *.hprof files..."
Get-ChildItem -Recurse -Filter *.hprof -ErrorAction SilentlyContinue | ForEach-Object {
  try { Remove-Item $_.FullName -Force -ErrorAction Stop; Write-Host "  - removed:" $_.FullName } catch {}
}
# Ensure .gitignore contains *.hprof
$gitignore = Join-Path (Get-Location) ".gitignore"
if (-not (Test-Path $gitignore)) { "" | Out-File -FilePath $gitignore -Encoding utf8 }
$line = "*.hprof"
if (-not (Select-String -Path $gitignore -Pattern "^\Q$line\E$" -SimpleMatch -Quiet)) {
  Add-Content $gitignore "`n# Heap dumps`n$line"
  Write-Host "[clean_hprof] Added '*.hprof' to .gitignore"
}
if ($Stage) {
  git add -A
  git commit -m "chore: auto-clean *.hprof and update .gitignore" 2>$null
}
Write-Host "[clean_hprof] Done."