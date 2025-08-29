# fix_java_env.ps1
# Attempts to locate a JDK installation on Windows and prints suggested commands to set JAVA_HOME.
# Usage: Open PowerShell as Administrator (for persistent changes) or run in your terminal to set JAVA_HOME for the session.

Write-Host "Checking for Java (java -version)..." -ForegroundColor Cyan
$javaVersion = & java -version 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "Java is already available:" -ForegroundColor Green
    Write-Host $javaVersion
    exit 0
}

Write-Host "java not found on PATH. Searching common JDK locations..." -ForegroundColor Yellow
$possible = @(
    "C:\\Program Files\\Java",
    "C:\\Program Files (x86)\\Java",
    "C:\\Program Files\\AdoptOpenJDK",
    "C:\\Program Files\\Zulu",
    "C:\\Program Files\\Amazon Corretto",
    "C:\\Program Files\\Eclipse Adoptium"
)

$found = $null
foreach ($base in $possible) {
    if (Test-Path $base) {
        Get-ChildItem -Path $base -Directory -ErrorAction SilentlyContinue | ForEach-Object {
            # prefer jdk folders (contain "jdk" and a bin\java.exe)
            $candidate = $_.FullName
            $javaExe = Join-Path $candidate "bin\\java.exe"
            if (Test-Path $javaExe) {
                $found = $candidate
                return
            }
        }
    }
}

if ($found -ne $null) {
    Write-Host "Found JDK at: $found" -ForegroundColor Green
    Write-Host "To set JAVA_HOME for this session, run (PowerShell):" -ForegroundColor Cyan
    Write-Host "`$env:JAVA_HOME = '$found'" -ForegroundColor White
    Write-Host "`$env:Path = '$found\\bin;' + `$env:Path" -ForegroundColor White
    Write-Host "Then verify with: java -version" -ForegroundColor Cyan
    Write-Host "To set JAVA_HOME permanently (Admin), run this in an elevated PowerShell:" -ForegroundColor Yellow
    Write-Host "[System.Environment]::SetEnvironmentVariable('JAVA_HOME', '$found', 'Machine')" -ForegroundColor White
    Write-Host "[System.Environment]::SetEnvironmentVariable('Path', '$found\\bin;' + [System.Environment]::GetEnvironmentVariable('Path','Machine'), 'Machine')" -ForegroundColor White
} else {
    Write-Host "Could not locate a JDK in common locations. Please install a JDK (Corretto, Adoptium, Zulu, or Oracle) and run this script again." -ForegroundColor Red
    Write-Host "Download: https://adoptium.net/ or https://aws.amazon.com/corretto/" -ForegroundColor Cyan
}
