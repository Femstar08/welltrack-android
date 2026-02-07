# WellTrack Project Structure

## Root Directory Layout

```
WellTrack/                          # Main Android project
├── app/                           # Application module
├── build.gradle.kts              # Root build configuration
├── settings.gradle.kts           # Project settings
├── .env                          # Environment variables (not in VCS)
├── .env.example                  # Environment template
└── *.md                          # Documentation files

WellTrack-dashboard/               # Web dashboard (React/TypeScript)
WellTrack_dashboard_latest/        # Latest dashboard version
Garmin API Guide/                  # API documentation and guides
```

## Android App Structure (`WellTrack/app/src/main/java/com/beaconledger/welltrack/`)

### Core Architecture Layers

```
├── data/                          # Data Layer
│   ├── database/                  # Room database
│   │   ├── dao/                   # Data Access Objects
│   │   ├── WellTrackDatabase.kt   # Database configuration
│   │   └── Converters.kt          # Type converters
│   ├── repository/                # Repository implementations
│   ├── remote/                    # API clients (Supabase, etc.)
│   ├── model/                     # Data models
│   └── sync/                      # Data synchronization
│
├── domain/                        # Domain Layer
│   ├── repository/                # Repository interfaces
│   └── usecase/                   # Business logic use cases
│
├── presentation/                  # Presentation Layer
│   ├── [feature]/                 # Feature-specific UI
│   │   ├── [Feature]Screen.kt     # Compose screens
│   │   ├── [Feature]ViewModel.kt  # ViewModels
│   │   ├── [Feature]Components.kt # Reusable components
│   │   └── [Feature]Dialogs.kt    # Dialog components
│   ├── components/                # Shared UI components
│   ├── navigation/                # Navigation configuration
│   └── theme/                     # UI theming
│
├── di/                           # Dependency Injection modules
├── config/                       # Configuration classes
├── accessibility/                # Accessibility utilities
├── security/                     # Security implementations
└── WellTrackApplication.kt       # Application class
```

### Feature Organization

Each major feature follows a consistent structure:

```
presentation/[feature]/
├── [Feature]Screen.kt            # Main screen composable
├── [Feature]ViewModel.kt         # State management
├── [Feature]Components.kt        # Feature-specific components
├── [Feature]Dialogs.kt          # Modal dialogs
└── [Feature]Navigation.kt       # Feature navigation (if complex)

data/[feature]/
├── [Feature]Repository.kt       # Repository implementation
├── [Feature]Api.kt             # API service
├── [Feature]Dao.kt             # Database access
└── [Feature]Models.kt          # Data models

domain/[feature]/
├── [Feature]Repository.kt      # Repository interface
└── [Feature]UseCase.kt         # Business logic
```

## Key Features and Their Locations

### Core Features
- **Authentication**: `presentation/auth/`, `data/auth/`
- **Dashboard**: `presentation/dashboard/`
- **Meals**: `presentation/meals/`, `data/meals/`
- **Recipes**: `presentation/recipes/`, `data/recipes/`
- **Health Data**: `presentation/health/`, `data/health/`
- **Shopping**: `presentation/shoppinglist/`, `data/shopping/`
- **Pantry**: `presentation/pantry/`, `data/pantry/`

### Specialized Components
- **Security**: `data/security/`, `presentation/security/`
- **Accessibility**: `accessibility/`, `presentation/accessibility/`
- **Data Export**: `data/export/`, `presentation/dataexport/`
- **Compliance**: `data/compliance/` (Garmin brand compliance)
- **Sync**: `data/sync/` (cross-device synchronization)

## Testing Structure (`WellTrack/app/src/test/` and `WellTrack/app/src/androidTest/`)

```
test/java/com/beaconledger/welltrack/
├── data/repository/              # Repository unit tests
├── domain/usecase/              # Use case unit tests
├── presentation/                # ViewModel tests
├── security/                    # Security tests
├── utils/                       # Test utilities
└── TestUtils.kt                # Common test helpers

androidTest/java/com/beaconledger/welltrack/
├── data/database/              # Database integration tests
├── ui/                         # UI flow tests
├── performance/                # Performance tests
└── accessibility/              # Accessibility tests
```

## Configuration Files

### Build Configuration
- `build.gradle.kts` - App-level build configuration
- `proguard-rules.pro` - Code obfuscation rules
- `gradle.properties` - Gradle properties

### Environment & Security
- `.env` - Environment variables (local, not committed)
- `.env.example` - Environment template
- `local.properties` - Local SDK paths

### Resources (`WellTrack/app/src/main/res/`)
```
├── drawable/                    # Vector drawables and icons
├── values/                      # Strings, colors, dimensions
│   ├── strings.xml             # App strings
│   ├── colors.xml              # Color definitions
│   └── garmin_strings.xml      # Garmin-specific strings
├── xml/                        # XML configurations
└── assets/                     # Static assets (privacy policy, etc.)
```

## Naming Conventions

### Files
- **Screens**: `[Feature]Screen.kt` (e.g., `MealScreen.kt`)
- **ViewModels**: `[Feature]ViewModel.kt` (e.g., `MealViewModel.kt`)
- **Repositories**: `[Feature]RepositoryImpl.kt` (e.g., `MealRepositoryImpl.kt`)
- **Use Cases**: `[Feature]UseCase.kt` (e.g., `MealUseCase.kt`)
- **DAOs**: `[Feature]Dao.kt` (e.g., `MealDao.kt`)
- **Models**: `[Feature].kt` (e.g., `Meal.kt`)

### Packages
- Use lowercase with dots: `com.beaconledger.welltrack.presentation.meals`
- Group by feature, then by layer: `presentation/meals/`, `data/meals/`
- Shared utilities: `utils/`, `common/`, `shared/`

### Classes
- **Screens**: PascalCase ending in "Screen" (e.g., `MealPlanningScreen`)
- **ViewModels**: PascalCase ending in "ViewModel" (e.g., `MealPlanningViewModel`)
- **Repositories**: Interface without suffix, Impl with "Impl" suffix
- **Use Cases**: PascalCase ending in "UseCase" (e.g., `CreateMealUseCase`)

## Documentation Structure

### Project Documentation
- `README.md` - Project overview and setup
- `ENVIRONMENT_SETUP_GUIDE.md` - Environment configuration
- `DATABASE_SETUP_GUIDE.md` - Database setup instructions
- `TESTING_GUIDE.md` - Testing procedures and standards

### Implementation Summaries
- `*_IMPLEMENTATION_SUMMARY.md` - Feature implementation details
- `GARMIN_BRAND_COMPLIANCE.md` - Garmin integration compliance
- `ACCESSIBILITY_IMPLEMENTATION_SUMMARY.md` - Accessibility features

## Development Workflow

### Adding New Features
1. Create feature package in `presentation/[feature]/`
2. Implement data layer in `data/[feature]/`
3. Define domain contracts in `domain/[feature]/`
4. Add DI module in `di/[Feature]Module.kt`
5. Write tests in appropriate test directories
6. Update navigation in `presentation/navigation/`

### Code Organization Principles
- **Single Responsibility**: Each class has one clear purpose
- **Feature-First**: Organize by feature, then by layer
- **Dependency Direction**: Presentation → Domain ← Data
- **Testability**: All classes are easily testable with clear dependencies