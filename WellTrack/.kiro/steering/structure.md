# WellTrack Project Structure

## Root Directory

- **`.env`**: Environment variables (API keys, configuration)
- **`.env.example`**: Template for environment setup
- **`build.gradle.kts`**: Root build configuration
- **`settings.gradle.kts`**: Project settings and module inclusion
- **`gradle/`**: Gradle wrapper and version catalogs
- **`local.properties`**: Local SDK paths (not in version control)

## Application Module (`app/`)

```
app/
├── build.gradle.kts          # App-level build configuration
├── proguard-rules.pro        # ProGuard/R8 optimization rules
└── src/                      # Source code directory
    ├── main/                 # Main source set
    ├── test/                 # Unit tests
    └── androidTest/          # Instrumented tests
```

## Database (`database/`)

SQL schema files for Supabase database setup:

- **`01_user_profiles.sql`**: User profile tables
- **`02_recipes.sql`**: Recipe management schema
- **`03_meals.sql`**: Meal planning tables
- **`04_health_data.sql`**: Health metrics storage
- **`05_shopping_pantry.sql`**: Shopping and pantry management
- **`06_sample_data.sql`**: Test data for development

## Documentation

- **Implementation summaries**: `*_IMPLEMENTATION_SUMMARY.md` files
- **Setup guides**: `*_SETUP_GUIDE.md` and `*_SETUP.md` files
- **Compliance docs**: `GARMIN_BRAND_COMPLIANCE.md`, `ACCESSIBILITY_*`

## Utility Scripts

- **`fix_*.py`**: Python scripts for icon and asset management
- **`test-runner.bat`**: Windows batch script for test execution
- **`run-tests.gradle`**: Gradle test configuration

## Kiro Configuration (`.kiro/`)

- **`steering/`**: AI assistant guidance documents
- **`specs/`**: Feature specifications and implementation plans

## Architecture Patterns

- **MVVM**: Model-View-ViewModel with Compose
- **Repository Pattern**: Data layer abstraction
- **Dependency Injection**: Hilt for component management
- **Clean Architecture**: Separation of concerns across layers

## Package Structure Convention

```
com.beaconledger.welltrack/
├── ui/                       # Compose UI components and screens
├── data/                     # Data layer (repositories, network, local)
├── domain/                   # Business logic and use cases
├── di/                       # Dependency injection modules
├── utils/                    # Utility classes and extensions
└── accessibility/            # Accessibility-specific components
```

## Key Directories to Know

- **`app/src/main/java/`**: Main Kotlin source code
- **`app/src/main/res/`**: Android resources (layouts, strings, etc.)
- **`database/`**: SQL schema files for backend setup
- **`.kiro/steering/`**: AI assistant configuration and guidelines
