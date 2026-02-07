# WellTrack Architecture Guide

## Overview

WellTrack follows Clean Architecture principles with MVVM pattern to ensure maintainability, testability, and scalability. The architecture separates concerns into distinct layers with clear responsibilities and dependencies flowing inward toward the domain layer.

## Architecture Layers

### 1. Presentation Layer (UI)
**Location**: `app/src/main/java/com/beaconledger/welltrack/presentation/`

The presentation layer handles user interface and user interactions. It consists of:

#### Components
- **Screens**: Jetpack Compose screens for each feature
- **ViewModels**: Handle UI state and business logic coordination
- **UI Components**: Reusable Compose components
- **Navigation**: Navigation graph and routing logic

#### Key Design Patterns
- **MVVM**: ViewModels manage UI state and coordinate with use cases
- **Unidirectional Data Flow**: State flows down, events flow up
- **State Management**: StateFlow and LiveData for reactive UI updates

#### Example Structure
```
presentation/
├── components/          # Reusable UI components
│   ├── CommonComponents.kt
│   ├── ChartComponents.kt
│   └── EnhancedComponents.kt
├── navigation/          # Navigation configuration
│   └── WellTrackNavigation.kt
├── [feature]/          # Feature-specific screens
│   ├── [Feature]Screen.kt
│   ├── [Feature]ViewModel.kt
│   └── [Feature]Components.kt
└── theme/              # Design system and theming
    ├── Theme.kt
    ├── Color.kt
    └── Typography.kt
```

### 2. Domain Layer (Business Logic)
**Location**: `app/src/main/java/com/beaconledger/welltrack/domain/`

The domain layer contains business logic and is independent of external frameworks.

#### Components
- **Use Cases**: Encapsulate specific business operations
- **Repository Interfaces**: Define data access contracts
- **Domain Models**: Business entities and value objects

#### Key Principles
- **Framework Independence**: No Android or UI dependencies
- **Single Responsibility**: Each use case handles one business operation
- **Dependency Inversion**: Depends on abstractions, not implementations

#### Example Structure
```
domain/
├── repository/         # Repository interfaces
│   ├── MealRepository.kt
│   ├── HealthDataRepository.kt
│   └── UserRepository.kt
└── usecase/           # Business logic use cases
    ├── MealPlanningUseCase.kt
    ├── HealthDataUseCase.kt
    └── AnalyticsUseCase.kt
```

### 3. Data Layer (Data Management)
**Location**: `app/src/main/java/com/beaconledger/welltrack/data/`

The data layer handles data persistence, network operations, and external integrations.

#### Components
- **Repository Implementations**: Concrete implementations of domain contracts
- **Data Sources**: Local database, remote APIs, health platforms
- **DAOs**: Database access objects for Room
- **Models**: Data transfer objects and entity mappings

#### Key Features
- **Data Source Abstraction**: Repository pattern hides data source complexity
- **Caching Strategy**: Local-first with cloud sync
- **Error Handling**: Consistent error handling across data sources

#### Example Structure
```
data/
├── database/          # Local database (Room)
│   ├── WellTrackDatabase.kt
│   ├── dao/           # Data access objects
│   └── entities/      # Database entities
├── remote/            # Remote data sources
│   ├── SupabaseClient.kt
│   └── api/           # API interfaces
├── health/            # Health platform integrations
│   ├── HealthConnectManager.kt
│   ├── GarminConnectManager.kt
│   └── SamsungHealthManager.kt
├── repository/        # Repository implementations
│   ├── MealRepositoryImpl.kt
│   └── HealthDataRepositoryImpl.kt
└── model/            # Data models and DTOs
    ├── Meal.kt
    └── HealthMetric.kt
```

## Dependency Injection

### Hilt Configuration
WellTrack uses Hilt for dependency injection to manage dependencies and improve testability.

#### Module Structure
```
di/
├── AppModule.kt          # Application-level dependencies
├── DatabaseModule.kt     # Database and DAO bindings
├── NetworkModule.kt      # Network and API clients
├── RepositoryModule.kt   # Repository implementations
└── [Feature]Module.kt    # Feature-specific dependencies
```

#### Key Benefits
- **Testability**: Easy to mock dependencies for testing
- **Maintainability**: Centralized dependency configuration
- **Performance**: Compile-time dependency resolution

### Example Dependency Graph
```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindMealRepository(
        mealRepositoryImpl: MealRepositoryImpl
    ): MealRepository

    @Binds
    abstract fun bindHealthDataRepository(
        healthDataRepositoryImpl: HealthDataRepositoryImpl
    ): HealthDataRepository
}
```

## Data Flow Architecture

### Request Flow
1. **UI Event**: User interaction triggers event in Compose screen
2. **ViewModel**: Receives event and calls appropriate use case
3. **Use Case**: Executes business logic and calls repository
4. **Repository**: Coordinates between local and remote data sources
5. **Data Source**: Fetches/stores data (database, API, health platform)
6. **Response**: Data flows back through layers to update UI state

### State Management
```kotlin
// ViewModel example
class MealPlanViewModel @Inject constructor(
    private val mealPlanningUseCase: MealPlanningUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MealPlanUiState())
    val uiState: StateFlow<MealPlanUiState> = _uiState.asStateFlow()

    fun generateMealPlan(parameters: MealPlanParameters) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val mealPlan = mealPlanningUseCase.generateMealPlan(parameters)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    mealPlan = mealPlan
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
}
```

## Health Data Integration Architecture

### Multi-Platform Strategy
WellTrack integrates with multiple health platforms using a unified interface:

```kotlin
interface HealthDataSource {
    suspend fun getHealthMetrics(type: HealthMetricType): Result<List<HealthMetric>>
    suspend fun syncHealthData(): Result<SyncStatus>
    fun isAvailable(): Boolean
}

// Implementations
class HealthConnectDataSource : HealthDataSource
class GarminConnectDataSource : HealthDataSource
class SamsungHealthDataSource : HealthDataSource
```

### Data Prioritization
When multiple sources provide the same data type:
1. **User Preference**: User-selected preferred source
2. **Data Quality**: More comprehensive data takes priority
3. **Recency**: More recent data overrides older data
4. **Source Reliability**: Platform-specific reliability scoring

### Conflict Resolution
```kotlin
class HealthDataConflictResolver {
    fun resolveConflicts(
        data: List<HealthMetricDataPoint>
    ): List<HealthMetricDataPoint> {
        return data
            .groupBy { it.timestamp }
            .mapValues { (_, conflicts) ->
                when {
                    conflicts.size == 1 -> conflicts.first()
                    else -> selectBestDataPoint(conflicts)
                }
            }
            .values
            .toList()
    }
}
```

## Security Architecture

### Authentication Flow
1. **App Launch**: Check for existing session
2. **Biometric Check**: Prompt for biometric authentication if enabled
3. **Supabase Auth**: Validate session with backend
4. **Health Platforms**: Re-authenticate with health platforms if needed

### Data Protection Layers
1. **Transport Security**: HTTPS/TLS for all network communication
2. **Storage Encryption**: AES-256 encryption for local data
3. **Key Management**: Android Keystore for secure key storage
4. **Access Control**: Biometric authentication and app lock

### Privacy by Design
```kotlin
class PrivacyControlsManager {
    fun getDefaultPrivacySettings(): PrivacySettings {
        return PrivacySettings(
            shareHealthData = false,      // Privacy-first defaults
            shareAnalytics = false,
            allowPersonalization = false,
            dataRetentionDays = 365      // Minimal retention
        )
    }
}
```

## Performance Architecture

### Lazy Loading Strategy
- **Screen Components**: Load components only when visible
- **Data Loading**: Paginated loading for large datasets
- **Image Loading**: Coil with memory and disk caching
- **Database Queries**: Indexed queries with pagination

### Caching Architecture
```kotlin
class CacheStrategy {
    companion object {
        const val MEAL_CACHE_TTL = 24.hours
        const val HEALTH_DATA_CACHE_TTL = 1.hours
        const val RECIPE_CACHE_TTL = 7.days
    }
}
```

### Background Processing
- **WorkManager**: Scheduled health data sync
- **Foreground Services**: Real-time cooking guidance
- **Broadcast Receivers**: System event handling

## Testing Architecture

### Testing Pyramid
1. **Unit Tests**: 70% - Repository, use case, and utility testing
2. **Integration Tests**: 20% - Database and API integration
3. **UI Tests**: 10% - Critical user journey validation

### Test Structure
```
test/
├── unit/
│   ├── repository/     # Repository layer tests
│   ├── usecase/       # Business logic tests
│   └── viewmodel/     # ViewModel tests
├── integration/
│   ├── database/      # Database integration tests
│   └── api/          # API integration tests
└── ui/
    ├── screens/       # Screen-level UI tests
    └── components/    # Component UI tests
```

### Mocking Strategy
- **Repositories**: Mocked for use case and ViewModel tests
- **APIs**: MockWebServer for network testing
- **Database**: In-memory Room database for testing

## Error Handling Architecture

### Error Types
```kotlin
sealed class WellTrackError : Exception() {
    object NetworkError : WellTrackError()
    object AuthenticationError : WellTrackError()
    object DataValidationError : WellTrackError()
    data class HealthPlatformError(val platform: String) : WellTrackError()
}
```

### Error Flow
1. **Data Layer**: Catch and wrap platform-specific errors
2. **Domain Layer**: Business logic validation errors
3. **Presentation Layer**: Convert errors to user-friendly messages
4. **UI Layer**: Display appropriate error states and recovery options

## Scalability Considerations

### Horizontal Scaling
- **Feature Modules**: Prepared for dynamic feature modules
- **Microservices Ready**: Repository pattern supports service decomposition
- **Multi-User**: Architecture supports family/group features

### Vertical Scaling
- **Database Optimization**: Efficient queries and indexing
- **Memory Management**: Careful lifecycle management
- **Battery Optimization**: Intelligent background processing

## Future Architecture Enhancements

### Planned Improvements
1. **Modularization**: Break into feature modules for larger teams
2. **Offline-First**: Enhanced offline capabilities with conflict resolution
3. **Real-Time Sync**: WebSocket-based real-time data synchronization
4. **AI/ML Integration**: On-device machine learning for insights

### Migration Strategies
- **Gradual Refactoring**: Incremental architecture improvements
- **Feature Toggles**: Safe deployment of architectural changes
- **Data Migration**: Versioned database migrations
- **Backward Compatibility**: Maintain API compatibility during transitions

---

## Architecture Decision Records (ADRs)

### ADR-001: Clean Architecture Adoption
**Decision**: Adopt Clean Architecture with MVVM pattern
**Rationale**: Improved testability, maintainability, and separation of concerns
**Status**: Accepted

### ADR-002: Jetpack Compose for UI
**Decision**: Use Jetpack Compose instead of traditional Views
**Rationale**: Modern declarative UI, better performance, type safety
**Status**: Accepted

### ADR-003: Repository Pattern for Data Layer
**Decision**: Implement Repository pattern for data access
**Rationale**: Abstract data sources, enable offline-first, improve testability
**Status**: Accepted

### ADR-004: Hilt for Dependency Injection
**Decision**: Use Hilt instead of manual DI or other frameworks
**Rationale**: Compile-time validation, Android integration, Google support
**Status**: Accepted

---

**Last Updated**: January 2025
**Reviewed By**: Code Reviewer, Backend Developer
**Next Review**: April 2025