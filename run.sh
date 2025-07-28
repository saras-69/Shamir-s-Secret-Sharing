#!/bin/bash
# Build and run script for Shamir's Secret Sharing

echo "🔨 Compiling Shamir's Secret Sharing Implementation..."
javac FinalShamirSecretSharing.java

if [ $? -eq 0 ]; then
    echo "✅ Compilation successful!"
    echo "🚀 Running the program..."
    echo ""
    java FinalShamirSecretSharing
else
    echo "❌ Compilation failed!"
    exit 1
fi
