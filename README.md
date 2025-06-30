# NebulaFiles

A powerful, modern file manager for Android built with Kotlin and Jetpack Compose, optimized for Termux and developer workflows.

## Features

- **Termux Integration**: Quick access to Termux directories ($HOME, $PREFIX, etc.)
- **Material 3 Design**: Modern UI with dynamic color theming
- **Adaptive Layouts**: Optimized for phones, tablets, and Samsung DeX
- **Background Operations**: Copy/move large files with progress notifications
- **Developer-Friendly**: Shows file permissions, symlinks, and hidden files
- **Google Drive Support**: Browse and manage cloud files (coming soon)
- **Powerful File Operations**: Copy, move, delete, rename with batch support

## Building in Termux

### Prerequisites

Install required packages in Termux:

```bash
pkg update && pkg upgrade
pkg install openjdk-21 gradle git aapt apksigner dx ecj
```

### Build Steps

1. Clone the repository:
```bash
git clone https://github.com/FAeN399/NebulaFiles.git
cd NebulaFiles
```

2. Make gradle wrapper executable:
```bash
chmod +x gradlew
```

3. Build debug APK:
```bash
./gradlew assembleDebug
```

The APK will be generated at `app/build/outputs/apk/debug/app-debug.apk`

### Signing for Release

1. Generate a keystore:
```bash
keytool -genkey -v -keystore nebula-release.keystore \
        -alias nebula -keyalg RSA -keysize 2048 -validity 10000
```

2. Build release APK:
```bash
./gradlew assembleRelease
```

## Installation

Install the APK using:
```bash
# Via adb
adb install app/build/outputs/apk/debug/app-debug.apk

# Or using Termux
cp app/build/outputs/apk/debug/app-debug.apk /sdcard/Download/
# Then install from Downloads using any file manager
```

## Architecture

NebulaFiles follows Clean Architecture principles:

- **Presentation Layer**: Jetpack Compose UI with Material 3
- **Domain Layer**: Use cases and business logic
- **Data Layer**: File providers, repositories, and services

### Key Components

- `TermuxFileProvider`: Handles local file system access with Termux-specific optimizations
- `FileOperationService`: Manages background file operations with notifications
- `FileListViewModel`: Manages UI state using StateFlow
- Adaptive UI using WindowSizeClass for different screen sizes

## Performance

- Handles directories with 100,000+ files efficiently
- Virtual scrolling for smooth performance
- Chunked I/O operations for large file transfers
- Memory-efficient with < 100MB typical usage

## Permissions

- Storage Access Framework (SAF) by default
- Optional MANAGE_EXTERNAL_STORAGE for power users
- Internet permission for Google Drive integration
- Foreground service for background operations

## Development

### Project Structure
```
NebulaFiles/
├── app/src/main/java/com/nebula/files/
│   ├── data/          # Data layer (providers, repositories)
│   ├── domain/        # Domain layer (models, use cases)
│   ├── ui/            # Presentation layer (screens, components)
│   └── di/            # Dependency injection modules
├── gradle/            # Gradle configuration
└── README.md          # This file
```

### Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

Apache License 2.0 - See LICENSE file for details

## Roadmap

- [x] Basic file operations
- [x] Termux integration
- [x] Background operations
- [x] Adaptive UI
- [ ] Google Drive integration
- [ ] Archive support (zip/tar)
- [ ] Network protocols (SFTP/SMB)
- [ ] Built-in text editor
- [ ] Git integration
- [ ] Termux CLI tool

## Support

Report issues at: https://github.com/FAeN399/NebulaFiles/issues
