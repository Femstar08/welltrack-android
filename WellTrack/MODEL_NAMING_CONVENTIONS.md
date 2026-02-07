# Model Naming Conventions

## Purpose
This document defines naming conventions to prevent model redeclaration conflicts and maintain code clarity.

## Core Principles

### 1. Domain Models (Primary Models)
**Location:** `/app/src/main/java/com/beaconledger/welltrack/data/model/`

**Naming:** Use simple, descriptive names
- These are the primary data models used by Room entities
- No prefix or suffix needed
- Examples: `Goal`, `Meal`, `HealthMetric`, `Biomarker`

### 2. Export Models (DTOs for Data Export)
**Location:** `/app/src/main/java/com/beaconledger/welltrack/data/model/DataExport.kt`

**Naming:** Prefix with `Export`
- Used for data export operations (JSON, CSV, PDF)
- Include only fields needed for export reports
- Examples: `ExportGoalProgress`, `ExportDateRange`, `ExportBiomarkerTrend`

### 3. Analysis Models (For Analytics & Insights)
**Location:** `/app/src/main/java/com/beaconledger/welltrack/data/model/Analytics.kt` or `/data/model/AIInsights.kt`

**Naming:** Use descriptive suffixes
- Suffix with `Analysis`, `Trend`, `Insight`, etc.
- Examples: `GoalProgressAnalysis`, `BiomarkerTrend`, `MetricTrendAnalysis`

### 4. Sync Models (For Data Synchronization)
**Location:** `/app/src/main/java/com/beaconledger/welltrack/data/model/HealthSyncModels.kt`

**Naming:** Prefix with `Sync` or use `Status`/`Result` suffix
- Examples: `SyncStats`, `HealthSyncResult`, `PlatformSyncStatus`

## Current Model Mappings

### GoalProgress Family
- `GoalProgress` - Domain model (Room Entity) in `/data/model/Goal.kt`
- `ExportGoalProgress` - Export DTO in `/data/model/DataExport.kt`
- `GoalProgressAnalysis` - AI analysis model in `/data/model/AIInsights.kt`

### DateRange Family
- `DateRange` - Query model with String dates in `/domain/repository/HealthDataRepository.kt`
- `ExportDateRange` - Export model with LocalDateTime in `/data/model/DataExport.kt`

### BiomarkerTrend Family
- `BiomarkerTrend` - Analytics model in `/data/model/Biomarker.kt`
- `ExportBiomarkerTrend` - Export DTO in `/data/model/DataExport.kt`

## Guidelines for Adding New Models

### Before Creating a New Model Class:

1. **Search for Existing Models**
   ```bash
   grep -r "data class YourModelName" app/src/main/java/
   ```

2. **Choose the Right Location**
   - Room Entity? → `/data/model/[Domain].kt`
   - Export DTO? → `/data/model/DataExport.kt`
   - Analytics? → `/data/model/Analytics.kt` or `/data/model/AIInsights.kt`
   - Sync? → `/data/model/HealthSyncModels.kt`

3. **Apply Naming Convention**
   - Domain models: Simple name (e.g., `UserProfile`)
   - Export models: `Export` prefix (e.g., `ExportUserProfile`)
   - Analysis models: Descriptive suffix (e.g., `UserProfileAnalysis`)
   - Sync models: `Sync` prefix or status suffix (e.g., `UserSyncStatus`)

4. **Document the Purpose**
   Add KDoc comments explaining:
   - What the model represents
   - Where it's used
   - How it differs from similar models

## Common Patterns

### Data Transformation Flow
```
Domain Model (Room Entity)
    ↓
Repository Layer
    ↓
Use Case Layer
    ↓
Export Model (DTO) → Export Service → File Output
```

### Example: Goal Progress
```kotlin
// Domain model - stored in database
@Entity(tableName = "goal_progress")
data class GoalProgress(...)

// Export model - used in health reports
data class ExportGoalProgress(...)

// Transform in repository
fun GoalProgress.toExportModel() = ExportGoalProgress(...)
```

## Refactoring Checklist

When renaming a model to avoid conflicts:

- [ ] Rename the class definition
- [ ] Update all import statements
- [ ] Update all property type references
- [ ] Update all function parameter types
- [ ] Update all function return types
- [ ] Update all TypeConverter methods
- [ ] Update all DAO methods
- [ ] Update all test files
- [ ] Run `./gradlew compileDebugKotlin` to verify
- [ ] Update documentation

## Tools for Verification

### Check for Redeclarations
```bash
# Find all declarations of a class
grep -r "^data class ClassName\|^class ClassName" app/src/main/java/

# Count declarations (should be 1)
grep -r "^data class ClassName" app/src/main/java/ | wc -l
```

### Find All Usages
```bash
# Find all files using a class
grep -r "\bClassName\b" app/src/main/java/ --include="*.kt"
```

## Best Practices

1. **Be Specific:** Use descriptive names that clearly indicate purpose
2. **Be Consistent:** Follow established patterns in the codebase
3. **Be Searchable:** Avoid overly generic names
4. **Document Decisions:** Add comments explaining why a model exists
5. **Review Before Committing:** Check for potential conflicts

## Questions?

If unsure about naming:
1. Check this document for similar models
2. Search the codebase for existing patterns
3. Ask the team in code review
4. When in doubt, add a descriptive prefix/suffix
