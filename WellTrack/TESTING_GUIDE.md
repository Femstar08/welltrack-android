# WellTrack Testing Guide

## Overview

This document provides a comprehensive guide to the testing infrastructure implemented for the WellTrack Android application. The testing suite covers all major components and follows Android testing best practices.

## Testing Architecture

### Test Pyramid Structure

The testing suite follows the standard Android testing pyramid:

- **Unit Tests (70%)**: Fast, isolated tests for business logic
- **Integration Tests (20%)**: Database and API integration testing
- **UI Tests (10%)**: End-to-end user journey testing

### Test Categories

#### 1. Unit Tests (`app/src/test/`)

**Repository Tests**

- `AuthRepositoryImplTest` - Authentication operations
- `MealRepositoryImplTest` - Meal data management
- `HealthConnectRepositoryImplTest` - Health data integration
- `RecipeRepositoryImplTest` - Recipe management

**Use Case Tests**

- All domain use cases are tested for business logic validation
- Mock dependencies to isolate business logic
- Test edge cases and error scenarios

**Security Tests**

- `EncryptionTest` - Data encryption/decryption
- `AuthenticationSecurityTest` - Authentication security validation

#### 2. Integration Tests (`app/src/androidTest/`)

**Database Integration**

- `MealDaoIntegrationTest` - Room database operations
- `HealthMetricDaoIntegrationTest` - Health data persistence

**Performance Tests**

- `DatabasePerformanceTest` - Database operation performance
- `UIPerformanceTest` - UI rendering and interaction performance

#### 3. UI Tests (`app/src/androidTest/`)

**Critical User Journeys**

- `AuthenticationFlowTest` - Login/signup flows
- `MealLoggingFlowTest` - Meal logging workflows
- `NavigationFlowTest` - App navigation testing

## Test Configuration

### Dependencies

The following testing dependencies are configured in `build.gradle.kts`:

```kotlin
// Unit Testing
testImplementation("junit:junit:4.13.2")
testImplementation("org.mockito:mockito-core:5.7.0")
testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

// Android Testing
androidTestImplementation("androidx.test.ext:junit:1.1.5")
androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
androidTestImplementation("androidx.compose.ui:ui-test-junit4")

// Hilt Testing
testImplementation("com.google.dagger:hilt-android-testing:2.48")
androidTestImplementation("com.google.dagger:hilt-android-testing:2.48")
```

### Test Configuration Files

- `test-config.properties` - Test environment configuration
- `TestUtils.kt` - Utility functions for creating test data
- `TestSuite.kt` - Unit test suite runner
- `IntegrationTestSuite.kt` - Integration test suite runner

## Running Tests

### Command Line

#### Run All Tests

```bash
./gradlew test connectedAndroidTest
```

#### Run Unit Tests Only

```bash
./gradlew testDebugUnitTest
```

#### Run Integration Tests Only

```bash
./gradlew connectedDebugAndroidTest
```

#### Run with Coverage

```bash
./gradlew testDebugUnitTest jacocoTestReport
```

### Using Test Runner Script

Execute the comprehensive test runner:

```bash
test-runner.bat
```

This script runs:

1. Unit tests
2. Integration tests
3. Coverage report generation
4. Code quality checks

### Android Studio

1. Right-click on test files or packages
2. Select "Run Tests"
3. View results in the Test Results panel

## Test Data Management

### Test Utilities

The `TestUtils` class provides helper methods for creating test data:

```kotlin
// Create test user
val testUser = TestUtils.createTestUser(
    id = "test_user_1",
    email = "test@example.com"
)

// Create test meal
val testMeal = TestUtils.createTestMeal(
    userId = "test_user_1",
    mealType = MealType.BREAKFAST
)

// Create test health metric
val testMetric = TestUtils.createTestHealthMetric(
    userId = "test_user_1",
    type = HealthMetricType.HEART_RATE
)
```

### Mock Data

Tests use Mockito for mocking dependencies:

```kotlin
@Mock
private lateinit var mealDao: MealDao

@Mock
private lateinit var supabaseClient: SupabaseClient

@Before
fun setup() {
    repository = MealRepositoryImpl(mealDao, supabaseClient)
}
```

## Performance Testing

### Database Performance

Tests verify that database operations complete within acceptable time limits:

- Bulk inserts: < 5 seconds for 1000 records
- Queries: < 1 second for complex queries
- Updates: < 2 seconds for 100 updates

### UI Performance

Tests verify UI responsiveness:

- Screen composition: < 2 seconds
- User interactions: < 1 second
- Memory usage: < 50MB increase during operations

## Security Testing

### Encryption Testing

Validates data encryption/decryption:

- Symmetric encryption works correctly
- Different inputs produce different encrypted outputs
- Decryption recovers original data

### Authentication Security

Tests authentication security measures:

- Weak passwords are rejected
- Invalid email formats are rejected
- Session timeouts are enforced
- Multiple failed attempts are blocked

## Coverage Requirements

### Minimum Coverage Targets

- **Overall**: 80% line coverage
- **Use Cases**: 90% line coverage
- **Repositories**: 85% line coverage
- **Critical Paths**: 95% line coverage

### Coverage Reports

Coverage reports are generated in HTML format:

- Location: `app/build/reports/jacoco/test/html/index.html`
- Includes line, branch, and method coverage
- Highlights uncovered code sections

## Continuous Integration

### Test Automation

Tests are designed to run in CI/CD pipelines:

- All tests are deterministic and repeatable
- No external dependencies required
- Parallel execution supported
- Comprehensive reporting

### Test Reporting

The `TestReportGenerator` creates detailed HTML reports:

- Test execution summary
- Pass/fail statistics
- Performance metrics
- Coverage information

## Best Practices

### Writing Tests

1. **Follow AAA Pattern**: Arrange, Act, Assert
2. **Use Descriptive Names**: Test names should describe the scenario
3. **Test One Thing**: Each test should verify a single behavior
4. **Mock External Dependencies**: Isolate units under test
5. **Use Test Data Builders**: Create consistent test data

### Test Organization

1. **Group Related Tests**: Use nested test classes
2. **Shared Setup**: Use `@Before` for common setup
3. **Clean Teardown**: Use `@After` for cleanup
4. **Test Categories**: Separate unit, integration, and UI tests

### Performance Considerations

1. **Fast Unit Tests**: Keep unit tests under 100ms
2. **Parallel Execution**: Enable parallel test execution
3. **Resource Cleanup**: Clean up resources after tests
4. **Memory Management**: Monitor memory usage in tests

## Troubleshooting

### Common Issues

#### Test Compilation Errors

- Ensure all dependencies are properly configured
- Check for missing imports
- Verify test source sets are configured

#### Test Failures

- Check mock configurations
- Verify test data setup
- Review assertion logic

#### Performance Issues

- Profile slow tests
- Optimize database operations
- Reduce test data size

### Debug Tips

1. **Use Logging**: Add debug logs to understand test flow
2. **Breakpoints**: Set breakpoints in test code
3. **Test Isolation**: Run individual tests to isolate issues
4. **Mock Verification**: Verify mock interactions

## Future Enhancements

### Planned Improvements

1. **Visual Regression Testing**: Screenshot comparison tests
2. **Load Testing**: High-volume data testing
3. **Accessibility Testing**: Screen reader and navigation testing
4. **Cross-Device Testing**: Testing on different screen sizes
5. **API Contract Testing**: Verify API compatibility

### Test Infrastructure

1. **Test Data Management**: Centralized test data creation
2. **Custom Matchers**: Domain-specific assertion helpers
3. **Test Fixtures**: Reusable test configurations
4. **Reporting Enhancements**: More detailed test analytics

## Resources

### Documentation

- [Android Testing Guide](https://developer.android.com/training/testing)
- [Compose Testing](https://developer.android.com/jetpack/compose/testing)
- [Hilt Testing](https://dagger.dev/hilt/testing)

### Tools

- [Mockito](https://mockito.org/)
- [JUnit](https://junit.org/junit4/)
- [Espresso](https://developer.android.com/training/testing/espresso)

---

This testing guide ensures comprehensive coverage of the WellTrack application with maintainable, reliable tests that support continuous development and deployment.

Test user = test@example123.com
Test password = testtest
