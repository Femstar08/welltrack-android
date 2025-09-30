# WellTrack Technology Stack

## Build System

- **Gradle**: Kotlin DSL with version catalogs (`gradle/libs.versions.toml`)
- **Android Gradle Plugin**: 8.11.1
- **Kotlin**: 2.0.21 with Compose compiler plugin

## Core Technologies

- **Android**: API 26-36, targeting Android 15+ with 16KB page size support
- **Jetpack Compose**: Modern UI toolkit with Material 3 design
- **Kotlin**: Primary language with coroutines for async operations
- **Hilt**: Dependency injection framework

## Key Libraries

- **Supabase**: Backend-as-a-Service (auth, database, storage, realtime)
- **Room**: Local database (currently disabled in build)
- **Retrofit + OkHttp**: HTTP networking with logging interceptor
- **Health Connect**: Android health data integration
- **ML Kit**: Text recognition for OCR functionality
- **CameraX**: Camera integration for recipe scanning
- **WorkManager**: Background task scheduling
- **Coil**: Image loading for Compose
- **JSoup**: Web scraping for recipe imports

## Security & Authentication

- **Encrypted SharedPreferences**: Secure local storage
- **Biometric Authentication**: Fingerprint/face unlock
- **Supabase Auth**: User authentication and session management

## Testing

- **JUnit 4**: Unit testing framework
- **Mockito/MockK**: Mocking frameworks
- **Espresso**: UI testing
- **Robolectric**: Android unit tests
- **Hilt Testing**: DI testing support

## Common Commands

### Build & Run

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Install debug on device
./gradlew installDebug

# Run tests
./gradlew test
./gradlew connectedAndroidTest
```

### Environment Setup

```bash
# Copy environment template
cp .env.example .env
# Edit .env with your API keys
```

## Build Variants

- **debug**: Development build with logging enabled
- **staging**: Pre-production testing environment
- **release**: Production build with ProGuard optimization

## Architecture Notes

- Excludes x86_64 architecture due to ML Kit 16KB alignment issues
- Uses KSP for annotation processing (Hilt, Room)
- Environment variables loaded from `.env` file into BuildConfig
