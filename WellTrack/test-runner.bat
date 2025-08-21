@echo off
echo.
echo ========================================
echo    WellTrack Comprehensive Test Suite
echo ========================================
echo.

REM Check if gradlew exists
if not exist gradlew.bat (
    echo Error: gradlew.bat not found. Please run from project root.
    pause
    exit /b 1
)

echo Starting comprehensive test execution...
echo.

REM Run unit tests
echo [1/4] Running Unit Tests...
echo ----------------------------------------
call gradlew testDebugUnitTest --continue
if %ERRORLEVEL% neq 0 (
    echo Warning: Some unit tests failed
)
echo.

REM Run integration tests
echo [2/4] Running Integration Tests...
echo ----------------------------------------
call gradlew connectedDebugAndroidTest --continue
if %ERRORLEVEL% neq 0 (
    echo Warning: Some integration tests failed
)
echo.

REM Generate test coverage report
echo [3/4] Generating Coverage Report...
echo ----------------------------------------
call gradlew jacocoTestReport
if %ERRORLEVEL% neq 0 (
    echo Warning: Coverage report generation failed
)
echo.

REM Run lint checks
echo [4/4] Running Code Quality Checks...
echo ----------------------------------------
call gradlew lint
if %ERRORLEVEL% neq 0 (
    echo Warning: Lint checks found issues
)
echo.

echo ========================================
echo           Test Execution Complete
echo ========================================
echo.
echo Test Reports Available:
echo - Unit Tests: app\build\reports\tests\testDebugUnitTest\index.html
echo - Integration Tests: app\build\reports\androidTests\connected\index.html
echo - Coverage Report: app\build\reports\jacoco\testDebugUnitTest\html\index.html
echo - Lint Report: app\build\reports\lint-results-debug.html
echo.

pause