#!/bin/bash

echo "🔥 NUCLEAR CLEAN PROTOCOL INITIATED 🔥"
echo "=================================="

# Stop any running gradle daemons
echo "Stopping Gradle daemons..."
./gradlew --stop

# Clean project
echo "Cleaning project..."
./gradlew clean

# Remove all build directories
echo "Removing build directories..."
rm -rf build
rm -rf app/build
rm -rf .gradle

# Clear gradle caches
echo "Clearing Gradle caches..."
rm -rf ~/.gradle/caches/modules-2/files-2.1/com.google.dagger
rm -rf ~/.gradle/caches/transforms-*
rm -rf ~/.gradle/caches/build-cache-*

# Clear IDE caches
echo "Clearing IDE caches..."
rm -rf .idea/caches
rm -rf .idea/libraries

# Remove generated files
echo "Removing generated files..."
find . -name "*.hprof" -delete
find . -name "*.log" -delete

echo ""
echo "✅ Nuclear clean complete!"
echo ""
echo "Now run:"
echo "./gradlew assembleDebug --no-build-cache --no-daemon --refresh-dependencies"