#!/bin/bash

echo "🌌 Initiating Quantum Build Sequence 🌌"
echo "===================================="

# Stop existing daemons to ensure clean quantum state
echo "Collapsing existing gradle wave functions..."
./gradlew --stop

# Nuclear clean to remove all observer effects
echo "Removing quantum entanglements..."
rm -rf build
rm -rf app/build
rm -rf .gradle
rm -rf ~/.gradle/caches/modules-2/files-2.1/com.google.dagger
rm -rf ~/.gradle/caches/transforms-*

# The Schrödinger delay - allows classpath to achieve coherence
echo "Allowing quantum states to stabilize..."
sleep 2.718

# Build with controlled observation to prevent wave function collapse
echo "Observing build reality carefully..."
./gradlew assembleDebug \
  --no-build-cache \
  --no-daemon \
  --max-workers=1 \
  -Dkotlin.compiler.execution.strategy=in-process \
  -Dorg.gradle.jvmargs="-Xmx3072m -XX:+UseG1GC"

echo ""
echo "✨ Quantum build sequence complete!"