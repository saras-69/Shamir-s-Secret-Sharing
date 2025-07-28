@echo off
REM Build and run script for Windows

echo 🔨 Compiling Shamir's Secret Sharing Implementation...
javac FinalShamirSecretSharing.java

if %ERRORLEVEL% EQU 0 (
    echo ✅ Compilation successful!
    echo 🚀 Running the program...
    echo.
    java FinalShamirSecretSharing
) else (
    echo ❌ Compilation failed!
    pause
)
