@echo off
echo.
echo ======================================================
echo  WINDOWS DEFENDER EXCLUSIONS SETUP - AUTOMATED GUIDE
echo ======================================================
echo.

echo Step 1: Opening Windows Security...
echo Press Win+I to open Settings, then navigate to:
echo Privacy ^& Security ^> Windows Security ^> Virus ^& threat protection
echo.
echo OR simply run this command (will open Windows Security):
start ms-settings:windowsdefender

echo.
echo Step 2: In Windows Security window:
echo 1. Click "Virus ^& threat protection"
echo 2. Click "Manage settings" under "Virus ^& threat protection settings"
echo 3. Scroll down to "Exclusions" 
echo 4. Click "Add or remove exclusions"
echo 5. Click "Add an exclusion" ^> "Folder"
echo.

echo Step 3: Add these 3 folders one by one:
echo.
echo FOLDER 1: %USERPROFILE%\.gradle
echo Full path: %USERPROFILE%\.gradle
echo.
echo FOLDER 2: %LOCALAPPDATA%\Android  
echo Full path: %LOCALAPPDATA%\Android
echo.
echo FOLDER 3: C:\Users\caxul\AndroidStudioProjects
echo Full path: C:\Users\caxul\AndroidStudioProjects
echo.

echo Step 4: After adding all exclusions, you can test the build:
echo cd "C:\Users\caxul\AndroidStudioProjects\Laboratoriodeldolor"
echo .\gradlew clean build --no-daemon
echo.

echo ======================================================
echo IMPORTANT: You need to add ALL THREE folders above
echo to Windows Defender exclusions for Android builds to work!
echo ======================================================
echo.
echo Press any key to open Windows Security now...
pause