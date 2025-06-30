# Building NebulaFiles in Termux

Due to limitations of the Termux environment, there are several approaches to build this app:

## Option 1: Use a Build Server (Recommended)

1. Push the code to GitHub:
```bash
git init
git add .
git commit -m "Initial commit"
git remote add origin https://github.com/yourusername/NebulaFiles.git
git push -u origin main
```

2. Use GitHub Actions to build (see `.github/workflows/build.yml`)

3. Download the APK from GitHub releases

## Option 2: Use Termux Ubuntu (proot)

1. Install Ubuntu in Termux:
```bash
pkg install proot-distro
proot-distro install ubuntu
proot-distro login ubuntu
```

2. Inside Ubuntu, install build tools:
```bash
apt update
apt install openjdk-17-jdk wget unzip
```

3. Download Android SDK:
```bash
wget https://dl.google.com/android/repository/commandlinetools-linux-10406996_latest.zip
unzip commandlinetools-linux-10406996_latest.zip
```

4. Build the project:
```bash
cd /path/to/NebulaFiles
./gradlew assembleDebug
```

## Option 3: Remote Build via SSH

1. Set up SSH to a machine with Android Studio/SDK
2. Use rsync to sync code
3. Build remotely and pull back APK

## Option 4: Pre-built APK

I've created the project structure. You can:
1. Transfer to a desktop with Android Studio
2. Open the project
3. Build normally

## Why Direct Termux Build is Difficult

1. **No Kotlin Compiler**: Termux doesn't package `kotlinc`
2. **Limited Android SDK**: Only basic tools like `aapt`, `dx`, `apksigner`
3. **No Gradle Android Plugin**: Requires full Android SDK
4. **Compose Compiler**: Needs special Kotlin compiler plugins

## Quick Desktop Build

If you have access to a desktop:

```bash
# Clone the project
git clone <your-repo>
cd NebulaFiles

# Build
./gradlew assembleDebug

# Install
adb install app/build/outputs/apk/debug/app-debug.apk
```

The project is fully configured and ready to build on any system with:
- JDK 17+
- Android SDK
- Internet connection (for dependency downloads)