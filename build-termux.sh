#!/bin/bash

# Build script for Termux without full Android SDK

echo "Building NebulaFiles for Termux..."

# Set ANDROID_HOME to Termux's android tools location
export ANDROID_HOME="/data/data/com.termux/files/usr"
export PATH="$ANDROID_HOME/bin:$PATH"

# Create a minimal local.properties
cat > local.properties << EOF
sdk.dir=/data/data/com.termux/files/usr
EOF

# Download required Android platform tools if needed
PLATFORM_DIR="platforms/android-35"
if [ ! -d "$PLATFORM_DIR" ]; then
    echo "Creating minimal Android platform structure..."
    mkdir -p "$PLATFORM_DIR"
    
    # Create android.jar stub (we'll use ecj's rt.jar as base)
    if [ -f "/data/data/com.termux/files/usr/share/java/ecj.jar" ]; then
        cp "/data/data/com.termux/files/usr/share/java/ecj.jar" "$PLATFORM_DIR/android.jar"
    fi
fi

# Try building with minimal configuration
echo "Attempting build with Termux tools..."

# First, let's create a simple APK using aapt and dx directly
echo "Building using direct Android tools..."

# 1. Create R.java from resources
mkdir -p build/gen
aapt package -f -m \
    -J build/gen \
    -M app/src/main/AndroidManifest.xml \
    -S app/src/main/res \
    -I /data/data/com.termux/files/usr/share/android.jar \
    --min-sdk-version 26 \
    --target-sdk-version 35

# 2. Compile Kotlin/Java code
echo "Compiling code..."
mkdir -p build/classes

# Find all Kotlin files
find app/src/main/java -name "*.kt" > build/sources.txt

# Compile with kotlinc if available
if command -v kotlinc &> /dev/null; then
    kotlinc -cp "/data/data/com.termux/files/usr/share/android.jar" \
        -d build/classes \
        @build/sources.txt
else
    echo "kotlinc not found, trying with ecj..."
    # Convert Kotlin to Java would be needed here
fi

# 3. Convert to DEX
echo "Converting to DEX..."
dx --dex --output=build/classes.dex build/classes

# 4. Package APK
echo "Packaging APK..."
aapt package -f \
    -M app/src/main/AndroidManifest.xml \
    -S app/src/main/res \
    -I /data/data/com.termux/files/usr/share/android.jar \
    -F build/app-unsigned.apk \
    --min-sdk-version 26 \
    --target-sdk-version 35

# Add DEX to APK
cd build && zip -r app-unsigned.apk classes.dex && cd ..

# 5. Sign APK
echo "Signing APK..."
if [ ! -f "debug.keystore" ]; then
    keytool -genkey -v -keystore debug.keystore \
        -storepass android -alias androiddebugkey \
        -keypass android -keyalg RSA -keysize 2048 \
        -validity 10000 -dname "CN=Android Debug,O=Android,C=US"
fi

apksigner sign --ks debug.keystore \
    --ks-pass pass:android \
    --key-pass pass:android \
    --out build/NebulaFiles-debug.apk \
    build/app-unsigned.apk

echo "Build complete! APK is at build/NebulaFiles-debug.apk"