# Design Document

## Overview

WellTrack is a comprehensive native Android application built using modern Android development practices with Kotlin, Jetpack Compose for UI, and a clean architecture pattern. The app integrates multiple health and fitness data sources through Health Connect, provides intelligent meal planning with AI-powered insights, and supports multi-user profiles with secure cloud synchronization via Supabase.

The application follows Material Design 3 principles and implements a modular architecture that supports offline-first functionality, real-time synchronization, and seamless integration with external health platforms and services.

## Architecture

### High-Level Architecture

The application follows a layered clean architecture pattern with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                       │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐ │
│  │   Activities    │  │   Fragments     │  │   Compose    │ │
│  │                 │  │                 │  │     UI       │ │
│  └─────────────────┘  └─────────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                     Domain Layer                            │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐ │
│  │   Use Cases     │  │   Repositories  │  │   Entities   │ │
│  │                 │  │  (Interfaces)   │  │              │ │
│  └─────────────────┘  └─────────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                      Data Layer                             │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐ │
│  │ Local Database  │  │  Remote APIs    │  │  Health      │ │
│  │    (Room)       │  │   (Supabase)    │  │  Connect     │ │
│  └─────────────────┘  └─────────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

### Technology Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material Design 3
- **Architecture**: MVVM with Clean Architecture
- **Dependency Injection**: Hilt
- **Local Database**: Room with SQLite
- **Backend**: Supabase (Authentication, Database, Storage)
- **Health Integration**: Health Connect API
- **Image Processing**: CameraX, ML Kit OCR
- **Networking**: Retrofit with OkHttp
- **Asynchronous Programming**: Kotlin Coroutines and Flow
- **Navigation**: Jetpack Navigation Compose
- **Testing**: JUnit, Espresso, Compose Testing

## Components and Interfaces

### Core Modules

#### 1. Authentication Module

- **SupabaseAuthManager**: Handles user authentication via Supabase
- **ProfileManager**: Manages user profiles and multi-user switching
- **SessionManager**: Maintains authentication state and tokens

#### 2. Meal Management Module

- **RecipeRepository**: CRUD operations for recipes with OCR and URL parsing
- **MealLogger**: Handles meal logging with multiple input methods
- **NutritionCalculator**: Computes nutritional breakdowns and scoring
- **MealPlanGenerator**: AI-powered meal planning with preference filtering

#### 3. Health Integration Module

- **HealthConnectManager**: Interfaces with Android Health Connect
- **GarminConnectManager**: OAuth 2.0 PKCE integration with Garmin Connect API for HRV, recovery, stress, and biological age data
- **SamsungHealthManager**: Samsung Health SDK integration for ECG and detailed body composition
- **ExternalPlatformManager**: Orchestrates data collection from multiple platforms with prioritization
- **HealthDataPrioritizer**: Implements data source prioritization and deduplication logic
- **ManualHealthEntryManager**: Provides manual entry fallbacks for all health metrics
- **BiomarkerTracker**: Manages blood test results and health metrics
- **CustomHabitTracker**: Handles user-defined habit tracking

#### 4. Shopping and Pantry Module

- **ShoppingListGenerator**: Auto-generates lists from meal plans
- **PantryManager**: Tracks inventory with expiry date monitoring
- **BarcodeScanner**: Processes product barcodes for automatic entry
- **CostCalculator**: Estimates meal costs and budget tracking

#### 5. Analytics and Insights Module

- **DataAggregator**: Combines data from multiple sources
- **InsightEngine**: Generates AI-powered recommendations
- **TrendAnalyzer**: Identifies patterns in health and nutrition data
- **ReportGenerator**: Creates comprehensive health reports

### Data Models

#### Core Entities

```kotlin
data class User(
    val id: String,
    val email: String,
    val name: String,
    val profilePhoto: String?,
    val age: Int?,
    val fitnessGoals: List<FitnessGoal>,
    val dietaryRestrictions: List<DietaryRestriction>,
    val preferences: UserPreferences
)

data class Recipe(
    val id: String,
    val name: String,
    val ingredients: List<Ingredient>,
    val instructions: List<String>,
    val nutritionInfo: NutritionInfo,
    val prepTime: Int,
    val cookTime: Int,
    val servings: Int,
    val tags: List<String>,
    val rating: Float?,
    val source: RecipeSource
)

data class Meal(
    val id: String,
    val userId: String,
    val recipeId: String?,
    val timestamp: LocalDateTime,
    val mealType: MealType,
    val portions: Float,
    val nutritionInfo: NutritionInfo,
    val score: MealScore,
    val status: MealStatus,
    val notes: String?
)

data class HealthMetric(
    val id: String,
    val userId: String,
    val type: HealthMetricType,
    val value: Double,
    val unit: String,
    val timestamp: LocalDateTime,
    val source: DataSource,
    val metadata: String? = null, // JSON for platform-specific data
    val confidence: Float = 1.0f, // Data quality score for prioritization
    val isManualEntry: Boolean = false
)

data class PlatformStatus(
    val platformName: String,
    val isAvailable: Boolean,
    val isConnected: Boolean,
    val lastSyncTime: LocalDateTime?,
    val supportedMetrics: List<HealthMetricType>,
    val authenticationStatus: AuthStatus
)

data class AuthenticationResult(
    val isSuccess: Boolean,
    val accessToken: String?,
    val refreshToken: String?,
    val expiresAt: LocalDateTime?,
    val error: String?
)

enum class AuthStatus {
    NOT_CONFIGURED,
    AUTHENTICATED,
    TOKEN_EXPIRED,
    AUTHENTICATION_FAILED,
    PERMISSION_DENIED
}
```

### API Interfaces

#### Repository Interfaces

```kotlin
interface RecipeRepository {
    suspend fun getAllRecipes(userId: String): Flow<List<Recipe>>
    suspend fun getRecipeById(id: String): Recipe?
    suspend fun saveRecipe(recipe: Recipe): Result<String>
    suspend fun importFromUrl(url: String): Result<Recipe>
    suspend fun parseFromOCR(imageUri: Uri): Result<Recipe>
}

interface MealRepository {
    suspend fun logMeal(meal: Meal): Result<String>
    suspend fun getMealsForDate(userId: String, date: LocalDate): Flow<List<Meal>>
    suspend fun updateMealStatus(mealId: String, status: MealStatus): Result<Unit>
}

interface HealthDataRepository {
    suspend fun syncHealthConnectData(userId: String): Result<Unit>
    suspend fun syncGarminConnectData(userId: String): Result<Unit>
    suspend fun syncSamsungHealthData(userId: String): Result<Unit>
    suspend fun syncAllPlatforms(userId: String): Result<Unit>
    suspend fun getHealthMetrics(userId: String, type: HealthMetricType, dateRange: DateRange): Flow<List<HealthMetric>>
    suspend fun saveCustomHabit(habit: CustomHabit): Result<String>
    suspend fun manuallyEnterHealthMetric(metric: HealthMetric): Result<String>
    suspend fun getPlatformStatus(): Flow<Map<String, PlatformStatus>>
}

interface ExternalPlatformRepository {
    suspend fun authenticateGarmin(): Result<AuthenticationResult>
    suspend fun authenticateSamsungHealth(): Result<AuthenticationResult>
    suspend fun disconnectPlatform(platform: String): Result<Unit>
    suspend fun refreshPlatformTokens(platform: String): Result<Unit>
}
```

## External Platform Integration Architecture

### OAuth 2.0 PKCE Flow for Garmin Connect

The Garmin Connect integration uses OAuth 2.0 with PKCE (Proof Key for Code Exchange) for secure authentication:

```kotlin
class GarminOAuthManager {
    suspend fun initiateAuthentication(): Result<String> {
        // Generate PKCE code verifier and challenge
        val codeVerifier = generateCodeVerifier()
        val codeChallenge = generateCodeChallenge(codeVerifier)

        // Build authorization URL
        val authUrl = buildAuthorizationUrl(codeChallenge)
        return Result.success(authUrl)
    }

    suspend fun exchangeCodeForTokens(authCode: String): Result<TokenResponse> {
        // Exchange authorization code for access/refresh tokens
        return performTokenExchange(authCode, codeVerifier)
    }
}
```

### Data Source Prioritization Algorithm

The system implements a sophisticated prioritization algorithm to handle data from multiple sources:

```kotlin
class HealthDataPrioritizer {
    private val platformPriority = mapOf(
        HealthMetricType.HRV to listOf("Garmin Connect", "Samsung Health", "Health Connect"),
        HealthMetricType.ECG to listOf("Samsung Health", "Health Connect"),
        HealthMetricType.BODY_FAT_PERCENTAGE to listOf("Samsung Health", "Health Connect"),
        HealthMetricType.TRAINING_RECOVERY to listOf("Garmin Connect"),
        HealthMetricType.STRESS_SCORE to listOf("Garmin Connect", "Samsung Health"),
        HealthMetricType.BIOLOGICAL_AGE to listOf("Garmin Connect")
    )

    fun prioritizeMetrics(metrics: List<HealthMetric>): List<HealthMetric> {
        return metrics
            .groupBy { "${it.type}_${it.timestamp.toLocalDate()}" }
            .mapValues { (_, duplicates) -> selectBestMetric(duplicates) }
            .values
            .toList()
    }

    private fun selectBestMetric(duplicates: List<HealthMetric>): HealthMetric {
        // Priority factors: source preference, data confidence, recency
        return duplicates.maxByOrNull { metric ->
            val sourcePriority = getPlatformPriority(metric.type, metric.source)
            val confidenceScore = metric.confidence
            val recencyScore = calculateRecencyScore(metric.timestamp)

            sourcePriority * 0.5 + confidenceScore * 0.3 + recencyScore * 0.2
        } ?: duplicates.first()
    }
}
```

### Manual Entry Fallback System

When external platforms are unavailable, users can manually enter any health metric:

```kotlin
class ManualHealthEntryManager {
    suspend fun createManualEntry(
        userId: String,
        metricType: HealthMetricType,
        value: Double,
        timestamp: LocalDateTime,
        notes: String? = null
    ): Result<String> {
        val metric = HealthMetric(
            id = UUID.randomUUID().toString(),
            userId = userId,
            type = metricType,
            value = value,
            unit = getUnitForMetricType(metricType),
            timestamp = timestamp,
            source = DataSource.MANUAL_ENTRY,
            isManualEntry = true,
            confidence = 0.8f, // Manual entries have lower confidence
            metadata = notes?.let { "{\"notes\":\"$it\"}" }
        )

        return healthMetricDao.insertHealthMetric(metric)
    }

    fun getSupportedManualMetrics(): List<HealthMetricType> {
        return HealthMetricType.values().toList()
    }
}
```

### Platform-Specific Data Handling

Each platform has unique data structures and capabilities:

#### Garmin Connect Specific Metrics

- **HRV (Heart Rate Variability)**: RMSSD values in milliseconds
- **Training Recovery**: Recovery score (0-100) with sleep and HRV factors
- **Stress Score**: Stress level (0-100) based on HRV analysis
- **Biological Age**: Calculated age based on fitness metrics
- **Advanced Sleep Metrics**: Sleep score, deep sleep, REM sleep phases

#### Samsung Health Specific Metrics

- **ECG Readings**: Raw ECG data with rhythm analysis
- **Detailed Body Composition**: Body fat %, muscle mass, bone mass, water %
- **Blood Pressure**: Systolic/diastolic with measurement context
- **SpO2**: Blood oxygen saturation levels

### Error Handling and Resilience

The system implements comprehensive error handling for platform integrations:

```kotlin
sealed class PlatformError : Exception() {
    object NetworkUnavailable : PlatformError()
    object AuthenticationExpired : PlatformError()
    object PermissionDenied : PlatformError()
    object RateLimitExceeded : PlatformError()
    object PlatformUnavailable : PlatformError()
    data class ApiError(val code: Int, val message: String) : PlatformError()
}

class PlatformErrorHandler {
    suspend fun handleError(error: PlatformError, platform: String): ErrorAction {
        return when (error) {
            is PlatformError.AuthenticationExpired -> {
                // Attempt token refresh
                refreshTokens(platform)
                ErrorAction.Retry
            }
            is PlatformError.RateLimitExceeded -> {
                // Implement exponential backoff
                ErrorAction.DelayAndRetry(calculateBackoffDelay())
            }
            is PlatformError.NetworkUnavailable -> {
                // Queue for later sync
                ErrorAction.QueueForLater
            }
            else -> ErrorAction.ShowError(error.message ?: "Unknown error")
        }
    }
}
```

## Data Models

### Database Schema

The local Room database consists of the following main tables:

#### Users Table

- Primary key: user_id
- Fields: email, name, profile_photo, age, created_at, updated_at
- Relationships: One-to-many with meals, recipes, health_metrics

#### Recipes Table

- Primary key: recipe_id
- Fields: name, prep_time, cook_time, servings, instructions_json, nutrition_json, source_type, source_url
- Relationships: Many-to-many with ingredients, one-to-many with meals

#### Meals Table

- Primary key: meal_id
- Foreign keys: user_id, recipe_id
- Fields: timestamp, meal_type, portions, nutrition_json, score, status, notes

#### Health Metrics Table

- Primary key: metric_id
- Foreign key: user_id
- Fields: metric_type, value, unit, timestamp, source, metadata_json

#### Pantry Items Table

- Primary key: item_id
- Foreign key: user_id
- Fields: ingredient_name, quantity, unit, expiry_date, purchase_date, barcode

### Cloud Database (Supabase)

The cloud database mirrors the local schema with additional fields for synchronization:

- sync_status (pending, synced, conflict)
- last_modified timestamp
- device_id for conflict resolution

## Error Handling

### Error Categories

1. **Network Errors**: Connection timeouts, API failures, sync conflicts
2. **Authentication Errors**: Invalid credentials, expired tokens, permission denied
3. **Data Validation Errors**: Invalid input, constraint violations, format errors
4. **Health Integration Errors**: Permission denied, service unavailable, data format issues
5. **Storage Errors**: Disk space, file corruption, backup failures

### Error Handling Strategy

```kotlin
sealed class WellTrackError : Exception() {
    data class NetworkError(val code: Int, override val message: String) : WellTrackError()
    data class AuthenticationError(override val message: String) : WellTrackError()
    data class ValidationError(val field: String, override val message: String) : WellTrackError()
    data class HealthIntegrationError(val platform: String, override val message: String) : WellTrackError()
    data class StorageError(override val message: String) : WellTrackError()
}

class ErrorHandler {
    fun handleError(error: WellTrackError): ErrorAction {
        return when (error) {
            is NetworkError -> if (error.code >= 500) ErrorAction.Retry else ErrorAction.ShowMessage
            is AuthenticationError -> ErrorAction.RedirectToLogin
            is ValidationError -> ErrorAction.ShowFieldError(error.field)
            is HealthIntegrationError -> ErrorAction.ShowIntegrationDialog
            is StorageError -> ErrorAction.ShowStorageDialog
        }
    }
}
```

### Offline Handling

- All critical operations work offline with local storage
- Background sync when connectivity is restored
- Conflict resolution using last-write-wins with user override option
- Optimistic UI updates with rollback on failure

## Testing Strategy

### Testing Pyramid

#### Unit Tests (70%)

- Repository implementations with mocked dependencies
- Use case business logic validation
- Data model transformations and validations
- Utility functions and calculations

#### Integration Tests (20%)

- Database operations with Room
- API integration with Supabase
- Health Connect integration
- End-to-end data flow testing

#### UI Tests (10%)

- Critical user journeys with Compose Testing
- Navigation flow validation
- Accessibility compliance testing
- Multi-user profile switching

### Test Implementation

```kotlin
@RunWith(AndroidJUnit4::class)
class MealRepositoryTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: WellTrackDatabase
    private lateinit var mealDao: MealDao
    private lateinit var repository: MealRepositoryImpl

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WellTrackDatabase::class.java
        ).allowMainThreadQueries().build()

        mealDao = database.mealDao()
        repository = MealRepositoryImpl(mealDao, mockNetworkService)
    }

    @Test
    fun logMeal_savesToLocalDatabase() = runTest {
        val meal = createTestMeal()
        val result = repository.logMeal(meal)

        assertTrue(result.isSuccess)
        val savedMeal = mealDao.getMealById(meal.id)
        assertEquals(meal.name, savedMeal?.name)
    }
}
```

### Performance Testing

- Memory usage monitoring during data sync
- Battery consumption analysis for background operations
- Network efficiency testing for API calls
- UI responsiveness testing with large datasets

### Security Testing

- Authentication flow security validation
- Data encryption verification
- API security testing with invalid tokens
- Local storage security assessment
