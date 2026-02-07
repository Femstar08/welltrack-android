# WellTrack Technology Stack

## Build System
- **Gradle**: Kotlin DSL with version catalogs
- **Android Gradle Plugin**: Latest stable version
- **Kotlin**: Primary development language
- **Java Version**: 11 (source and target compatibility)

## Core Framework
- **Android SDK**: Target SDK 36, Min SDK 26
- **Jetpack Compose**: Modern UI toolkit for native Android
- **Material Design 3**: UI components and theming
- **Hilt**: Dependency injection framework
- **Room**: Local database with SQLite backend

## Architecture Pattern
- **Clean Architecture**: Domain, Data, and Presentation layers
- **MVVM**: Model-View-ViewModel with Compose
- **Repository Pattern**: Data abstraction layer
- **Use Cases**: Business logic encapsulation

## Backend & Cloud
- **Supabase**: Backend-as-a-Service (PostgreSQL, Auth, Storage, Realtime)
- **Ktor**: HTTP client for API communication
- **Row Level Security (RLS)**: Database-level security policies

## Health Integrations
- **Android Health Connect**: Primary health data platform
- **Garmin Connect API**: Fitness device integration
- **Samsung Health SDK**: Samsung device integration

## Key Libraries
- **Navigation Compose**: Screen navigation
- **Coil**: Image loading and caching
- **ML Kit**: OCR text recognition
- **CameraX**: Camera functionality
- **WorkManager**: Background task scheduling
- **Biometric**: Fingerprint/face authentication
- **Security Crypto**: Encrypted SharedPreferences

## Testing Framework
- **JUnit 4**: Unit testing
- **Mockito**: Mocking framework
- **Espresso**: UI testing
- **Compose Testing**: Compose-specific UI tests
- **Hilt Testing**: DI testing support
- **Robolectric**: Android unit tests

## Development Tools
- **KSP**: Kotlin Symbol Processing (annotation processing)
- **Proguard**: Code obfuscation and optimization
- **Jacoco**: Code coverage reporting

## Common Build Commands

### Development
```bash
# Debug build
./gradlew assembleDebug

# Run unit tests
./gradlew testDebugUnitTest

# Run instrumented tests
./gradlew connectedDebugAndroidTest

# Generate test coverage report
./gradlew testDebugUnitTest jacocoTestReport
```

### Release
```bash
# Release build
./gradlew assembleRelease

# Staging build
./gradlew assembleStaging

# Clean build
./gradlew clean assembleDebug
```

### Testing
```bash
# Run all tests with custom script
test-runner.bat

# Run specific test class
./gradlew testDebugUnitTest --tests="*MealRepositoryTest*"

# Run performance tests
./gradlew connectedDebugAndroidTest --tests="*PerformanceTest*"
```

## Environment Configuration
- **Environment Variables**: Managed via `.env` file
- **Build Variants**: Debug, Staging, Release
- **BuildConfig**: Compile-time configuration injection
- **Secure Storage**: Android Keystore for sensitive data

## Performance Considerations
- **16KB Page Size Support**: Android 15+ compatibility
- **Architecture Filters**: ARM64 and ARMv7 (excludes x86_64 due to ML Kit issues)
- **Memory Optimization**: Efficient image loading and caching
- **Database Optimization**: Proper indexing and query optimization