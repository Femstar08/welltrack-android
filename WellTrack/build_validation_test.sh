#!/bin/bash

# WellTrack Android App Build Validation Test
# This script validates all critical build components and dependencies

echo "==================================================================="
echo "WellTrack Android App - Build Validation Test"
echo "==================================================================="

# Function to check if file exists
check_file() {
    if [ -f "$1" ]; then
        echo "✅ $1"
        return 0
    else
        echo "❌ Missing: $1"
        return 1
    fi
}

# Function to check if directory exists
check_dir() {
    if [ -d "$1" ]; then
        echo "✅ $1"
        return 0
    else
        echo "❌ Missing: $1"
        return 1
    fi
}

# Function to check import in file
check_import() {
    if grep -q "$2" "$1" 2>/dev/null; then
        echo "✅ Import '$2' found in $1"
        return 0
    else
        echo "❌ Import '$2' missing in $1"
        return 1
    fi
}

# Function to check Room entity annotation
check_room_entity() {
    if grep -q "@Entity" "$1" 2>/dev/null; then
        echo "✅ Room Entity annotation found in $1"
        return 0
    else
        echo "❌ Room Entity annotation missing in $1"
        return 1
    fi
}

echo ""
echo "1. BUILD CONFIGURATION VALIDATION"
echo "================================="

# Check build.gradle files
check_file "app/build.gradle.kts"
check_file "build.gradle.kts"
check_file "settings.gradle.kts"

# Check SDK versions
if grep -q "compileSdk = 34" app/build.gradle.kts; then
    echo "✅ Compile SDK version is 34"
else
    echo "❌ Compile SDK version issue"
fi

if grep -q "targetSdk = 34" app/build.gradle.kts; then
    echo "✅ Target SDK version is 34"
else
    echo "❌ Target SDK version issue"
fi

echo ""
echo "2. PROGUARD RULES VALIDATION"
echo "============================"

check_file "app/proguard-rules.pro"

# Check if ProGuard rules contain essential sections
if grep -q "ROOM DATABASE RULES" app/proguard-rules.pro; then
    echo "✅ Room database rules present"
else
    echo "❌ Room database rules missing"
fi

if grep -q "HILT/DAGGER RULES" app/proguard-rules.pro; then
    echo "✅ Hilt/Dagger rules present"
else
    echo "❌ Hilt/Dagger rules missing"
fi

echo ""
echo "3. APPLICATION STRUCTURE VALIDATION"
echo "=================================="

# Check main application files
check_file "app/src/main/java/com/beaconledger/welltrack/WellTrackApplication.kt"
check_file "app/src/main/java/com/beaconledger/welltrack/MainActivity.kt"
check_file "app/src/main/AndroidManifest.xml"

# Check AndroidManifest contains application class
if grep -q "android:name=\".WellTrackApplication\"" app/src/main/AndroidManifest.xml; then
    echo "✅ WellTrackApplication referenced in manifest"
else
    echo "❌ WellTrackApplication not referenced in manifest"
fi

echo ""
echo "4. DATABASE STRUCTURE VALIDATION"
echo "==============================="

# Check database files
check_file "app/src/main/java/com/beaconledger/welltrack/data/database/WellTrackDatabase.kt"
check_file "app/src/main/java/com/beaconledger/welltrack/data/database/Converters.kt"

# Check migrations
check_file "app/src/main/java/com/beaconledger/welltrack/data/database/migrations/Migration_1_to_2.kt"
check_file "app/src/main/java/com/beaconledger/welltrack/data/database/migrations/Migration_2_3.kt"

# Check DataDeletionRecord entity
check_room_entity "app/src/main/java/com/beaconledger/welltrack/data/compliance/DataPortabilityManager.kt"

# Check DeletionStatus converter
if grep -q "DeletionStatus" app/src/main/java/com/beaconledger/welltrack/data/database/Converters.kt; then
    echo "✅ DeletionStatus converter present"
else
    echo "❌ DeletionStatus converter missing"
fi

echo ""
echo "5. DAO VALIDATION"
echo "================"

# Check critical DAOs exist
CRITICAL_DAOS=(
    "UserDao.kt"
    "MealDao.kt"
    "HealthMetricDao.kt"
    "SupplementDao.kt"
    "BiomarkerDao.kt"
    "GoalDao.kt"
    "AuditLogDao.kt"
    "DataDeletionDao.kt"
)

for dao in "${CRITICAL_DAOS[@]}"; do
    check_file "app/src/main/java/com/beaconledger/welltrack/data/database/dao/$dao"
done

echo ""
echo "6. MODEL VALIDATION"
echo "=================="

# Check critical models exist
CRITICAL_MODELS=(
    "User.kt"
    "Meal.kt"
    "HealthMetric.kt"
    "Supplement.kt"
    "Biomarker.kt"
    "Goal.kt"
    "AuditLog.kt"
)

for model in "${CRITICAL_MODELS[@]}"; do
    check_file "app/src/main/java/com/beaconledger/welltrack/data/model/$model"
done

echo ""
echo "7. HILT DEPENDENCY INJECTION VALIDATION"
echo "======================================"

# Check Hilt modules exist
check_dir "app/src/main/java/com/beaconledger/welltrack/di"

# Check if WellTrackApplication has Hilt annotation
if grep -q "@HiltAndroidApp" app/src/main/java/com/beaconledger/welltrack/WellTrackApplication.kt; then
    echo "✅ WellTrackApplication has @HiltAndroidApp annotation"
else
    echo "❌ WellTrackApplication missing @HiltAndroidApp annotation"
fi

echo ""
echo "8. HEALTH APP COMPLIANCE VALIDATION"
echo "================================="

# Check Health Connect integration
if grep -q "health.connect.client" app/build.gradle.kts; then
    echo "✅ Health Connect dependency present"
else
    echo "❌ Health Connect dependency missing"
fi

# Check accessibility components
check_dir "app/src/main/java/com/beaconledger/welltrack/accessibility"

# Check security components
check_dir "app/src/main/java/com/beaconledger/welltrack/data/security"

echo ""
echo "9. REPOSITORY AND USE CASE VALIDATION"
echo "==================================="

# Check repositories exist
check_dir "app/src/main/java/com/beaconledger/welltrack/data/repository"
check_dir "app/src/main/java/com/beaconledger/welltrack/domain/repository"

# Check use cases exist
check_dir "app/src/main/java/com/beaconledger/welltrack/domain/usecase"

echo ""
echo "10. IMPORT VALIDATION"
echo "==================="

# Check critical imports in DataPortabilityManager
DATA_PORTABILITY_FILE="app/src/main/java/com/beaconledger/welltrack/data/compliance/DataPortabilityManager.kt"
check_import "$DATA_PORTABILITY_FILE" "androidx.room.Entity"
check_import "$DATA_PORTABILITY_FILE" "androidx.room.PrimaryKey"
check_import "$DATA_PORTABILITY_FILE" "com.beaconledger.welltrack.data.model.*"

echo ""
echo "==================================================================="
echo "BUILD VALIDATION TEST COMPLETED"
echo "==================================================================="

# Count files for verification
echo ""
echo "FILE COUNTS:"
echo "============"
echo "DAO files: $(find app/src/main/java/com/beaconledger/welltrack/data/database/dao -name "*.kt" | wc -l)"
echo "Model files: $(find app/src/main/java/com/beaconledger/welltrack/data/model -name "*.kt" | wc -l)"
echo "Repository files: $(find app/src/main/java/com/beaconledger/welltrack/data/repository -name "*.kt" | wc -l)"
echo "UseCase files: $(find app/src/main/java/com/beaconledger/welltrack/domain/usecase -name "*.kt" | wc -l)"
echo "DI Module files: $(find app/src/main/java/com/beaconledger/welltrack/di -name "*.kt" | wc -l)"
echo "UI files: $(find app/src/main/java/com/beaconledger/welltrack/presentation -name "*.kt" | wc -l)"

echo ""
echo "RECOMMENDATIONS:"
echo "==============="
echo "1. Run 'gradlew clean' before attempting build"
echo "2. Use 'gradlew assembleDebug' for debug build"
echo "3. Use 'gradlew assembleRelease' for release build"
echo "4. Check Android SDK path in local.properties"
echo "5. Ensure JAVA_HOME is set correctly"

echo ""
echo "Build validation script completed!"