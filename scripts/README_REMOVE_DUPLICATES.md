Run the helper scripts to back up and remove duplicate launcher resources.

PowerShell (run from repository root in Windows PowerShell / PowerShell Core):

powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\remove_duplicate_launchers.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\move_duplicate_launcher_webps.ps1

Verify remaining launcher files:

Get-ChildItem -Path .\app\src\main\res -Recurse -Filter "ic_launcher*" | Format-Table FullName

Backups will be created under .\scripts\backup-launcher-xmls and .\scripts\backup-launcher-webps with timestamped filenames, and logs are written to .\scripts\remove-duplicate-launchers.log and .\scripts\move-duplicate-launcher-webps.log respectively.
