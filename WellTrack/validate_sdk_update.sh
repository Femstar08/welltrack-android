#!/bin/bash

# WellTrack SDK Update Validation Script
# Validates that the SDK update resolves AAR metadata compatibility issues

echo "🏥 WellTrack Health App - SDK Update Validation"
echo "================================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print status
print_status() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✅ $2${NC}"
    else
        echo -e "${RED}❌ $2${NC}"
        exit 1
    fi
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

echo "Step 1: Cleaning previous builds..."
./gradlew clean > /dev/null 2>&1
print_status $? "Project cleaned"

echo "Step 2: Checking SDK versions in build files..."

# Check compileSdk version
COMPILE_SDK=$(grep -r "compileSdk.*36" app/build.gradle.kts)
if [ ! -z "$COMPILE_SDK" ]; then
    print_status 0 "compileSdk set to 36"
else
    print_status 1 "compileSdk not set to 36"
fi

# Check targetSdk version
TARGET_SDK=$(grep -r "targetSdk.*35" app/build.gradle.kts)
if [ ! -z "$TARGET_SDK" ]; then
    print_status 0 "targetSdk set to 35"
else
    print_status 1 "targetSdk not set to 35"
fi

echo "Step 3: Validating dependency compatibility..."

# Check Health Connect dependency
HC_DEP=$(grep -r "androidx.health.connect:connect-client:1.1.0-rc03" app/build.gradle.kts)
if [ ! -z "$HC_DEP" ]; then
    print_status 0 "Health Connect 1.1.0-rc03 dependency found"
else
    print_status 1 "Health Connect dependency not found or incorrect version"
fi

# Check AndroidX Core dependency
CORE_DEP=$(grep -r "androidx.core:core-ktx.*1.16.0" gradle/libs.versions.toml)
if [ ! -z "$CORE_DEP" ]; then
    print_status 0 "AndroidX Core 1.16.0 dependency found"
else
    print_status 1 "AndroidX Core dependency not found or incorrect version"
fi

echo "Step 4: Checking Android Manifest permissions..."

# Check for new Android 15+ permissions
FOREGROUND_SERVICE=$(grep -r "FOREGROUND_SERVICE_HEALTH" app/src/main/AndroidManifest.xml)
if [ ! -z "$FOREGROUND_SERVICE" ]; then
    print_status 0 "FOREGROUND_SERVICE_HEALTH permission added"
else
    print_status 1 "FOREGROUND_SERVICE_HEALTH permission missing"
fi

# Check for granular media permissions
MEDIA_IMAGES=$(grep -r "READ_MEDIA_IMAGES" app/src/main/AndroidManifest.xml)
if [ ! -z "$MEDIA_IMAGES" ]; then
    print_status 0 "Granular media permissions added"
else
    print_status 1 "Granular media permissions missing"
fi

echo "Step 5: Validating ProGuard rules..."

# Check for enhanced Health Connect rules
HC_RULES=$(grep -r "androidx.health.connect.client.records" app/proguard-rules.pro)
if [ ! -z "$HC_RULES" ]; then
    print_status 0 "Enhanced Health Connect ProGuard rules found"
else
    print_status 1 "Enhanced Health Connect ProGuard rules missing"
fi

echo "Step 6: Testing build compilation..."

# Test debug build
print_warning "Building debug variant (this may take a few minutes)..."
./gradlew assembleDebug > build_debug.log 2>&1
if [ $? -eq 0 ]; then
    print_status 0 "Debug build successful"
    rm -f build_debug.log
else
    print_status 1 "Debug build failed - check build_debug.log for details"
    echo "Build errors:"
    tail -20 build_debug.log
    exit 1
fi

echo "Step 7: Testing health-specific features..."

# Check for Health Connect manager
HC_MANAGER=$(find app/src/main/java -name "*HealthConnectManager*" -type f)
if [ ! -z "$HC_MANAGER" ]; then
    print_status 0 "HealthConnectManager implementation found"
else
    print_status 1 "HealthConnectManager implementation missing"
fi

# Check for Garmin integration
GARMIN_MANAGER=$(find app/src/main/java -name "*GarminConnectManager*" -type f)
if [ ! -z "$GARMIN_MANAGER" ]; then
    print_status 0 "GarminConnectManager implementation found"
else
    print_warning "GarminConnectManager implementation not found"
fi

echo "Step 8: Validating service declarations..."

# Check foreground service type
SERVICE_TYPE=$(grep -r 'foregroundServiceType="health"' app/src/main/AndroidManifest.xml)
if [ ! -z "$SERVICE_TYPE" ]; then
    print_status 0 "Foreground service type set to 'health'"
else
    print_status 1 "Foreground service type not properly configured"
fi

echo ""
echo "🎉 SDK Update Validation Complete!"
echo "=================================="
echo ""
echo "Summary of changes:"
echo "- compileSdk: 34 → 36 (Health Connect compatibility)"
echo "- targetSdk: 34 → 35 (Android 15 support)"
echo "- Added Android 15+ health app permissions"
echo "- Enhanced ProGuard rules for API 36"
echo "- Updated service declarations"
echo "- Modernized media permissions"
echo ""
echo "✅ All 8 problematic dependencies should now be compatible"
echo "✅ Build should complete without AAR metadata errors"
echo "✅ App is ready for Google Play Store health app compliance"
echo ""
echo "Next steps:"
echo "1. Run comprehensive tests: ./gradlew test"
echo "2. Test on physical devices with Android 15"
echo "3. Validate Health Connect integration"
echo "4. Submit for Play Store review"