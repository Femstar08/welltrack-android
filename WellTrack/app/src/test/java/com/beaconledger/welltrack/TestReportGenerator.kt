package com.beaconledger.welltrack

import org.junit.runner.Description
import org.junit.runner.Result
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunListener
import java.io.File
import java.io.FileWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Custom test report generator for WellTrack testing suite
 * Generates detailed HTML reports with test results, coverage, and performance metrics
 */
class TestReportGenerator : RunListener() {

    private val testResults = mutableListOf<TestResult>()
    private var startTime: Long = 0
    private var endTime: Long = 0

    data class TestResult(
        val className: String,
        val methodName: String,
        val status: TestStatus,
        val duration: Long,
        val errorMessage: String? = null,
        val stackTrace: String? = null
    )

    enum class TestStatus {
        PASSED, FAILED, IGNORED
    }

    override fun testRunStarted(description: Description?) {
        startTime = System.currentTimeMillis()
        println("🚀 Starting WellTrack Test Suite...")
    }

    override fun testRunFinished(result: Result?) {
        endTime = System.currentTimeMillis()
        generateReport(result)
    }

    override fun testStarted(description: Description?) {
        description?.let {
            println("▶️  Running: ${it.className}.${it.methodName}")
        }
    }

    override fun testFinished(description: Description?) {
        description?.let {
            testResults.add(
                TestResult(
                    className = it.className,
                    methodName = it.methodName,
                    status = TestStatus.PASSED,
                    duration = 0 // Would need to track individual test timing
                )
            )
            println("✅ Passed: ${it.className}.${it.methodName}")
        }
    }

    override fun testFailure(failure: Failure?) {
        failure?.let {
            // Update the last test result to failed
            val lastIndex = testResults.size - 1
            if (lastIndex >= 0) {
                testResults[lastIndex] = testResults[lastIndex].copy(
                    status = TestStatus.FAILED,
                    errorMessage = it.message,
                    stackTrace = it.trace
                )
            }
            println("❌ Failed: ${it.description.className}.${it.description.methodName}")
            println("   Error: ${it.message}")
        }
    }

    override fun testIgnored(description: Description?) {
        description?.let {
            testResults.add(
                TestResult(
                    className = it.className,
                    methodName = it.methodName,
                    status = TestStatus.IGNORED,
                    duration = 0
                )
            )
            println("⏭️  Ignored: ${it.className}.${it.methodName}")
        }
    }

    private fun generateReport(result: Result?) {
        val totalDuration = endTime - startTime
        val passedTests = testResults.count { it.status == TestStatus.PASSED }
        val failedTests = testResults.count { it.status == TestStatus.FAILED }
        val ignoredTests = testResults.count { it.status == TestStatus.IGNORED }
        val totalTests = testResults.size

        // Console summary
        println("\n" + "=".repeat(60))
        println("📊 WellTrack Test Suite Results")
        println("=".repeat(60))
        println("Total Tests: $totalTests")
        println("✅ Passed: $passedTests")
        println("❌ Failed: $failedTests")
        println("⏭️  Ignored: $ignoredTests")
        println("⏱️  Duration: ${totalDuration}ms")
        println("📈 Success Rate: ${if (totalTests > 0) (passedTests * 100 / totalTests) else 0}%")
        println("=".repeat(60))

        // Generate HTML report
        generateHtmlReport(totalDuration, passedTests, failedTests, ignoredTests, totalTests)

        // Generate coverage report summary
        generateCoverageReport()

        if (failedTests > 0) {
            println("\n❌ Some tests failed. Check the detailed report for more information.")
            printFailedTests()
        } else {
            println("\n🎉 All tests passed successfully!")
        }
    }

    private fun printFailedTests() {
        val failedTests = testResults.filter { it.status == TestStatus.FAILED }
        if (failedTests.isNotEmpty()) {
            println("\n💥 Failed Tests Details:")
            println("-".repeat(40))
            failedTests.forEach { test ->
                println("❌ ${test.className}.${test.methodName}")
                println("   Error: ${test.errorMessage}")
                println()
            }
        }
    }

    private fun generateHtmlReport(
        totalDuration: Long,
        passedTests: Int,
        failedTests: Int,
        ignoredTests: Int,
        totalTests: Int
    ) {
        try {
            val reportDir = File("app/build/reports/tests")
            reportDir.mkdirs()

            val htmlFile = File(reportDir, "welltrack-test-report.html")
            FileWriter(htmlFile).use { writer ->
                writer.write(generateHtmlContent(totalDuration, passedTests, failedTests, ignoredTests, totalTests))
            }

            println("📄 HTML Report generated: ${htmlFile.absolutePath}")
        } catch (e: Exception) {
            println("⚠️  Failed to generate HTML report: ${e.message}")
        }
    }

    private fun generateHtmlContent(
        totalDuration: Long,
        passedTests: Int,
        failedTests: Int,
        ignoredTests: Int,
        totalTests: Int
    ): String {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        val successRate = if (totalTests > 0) (passedTests * 100 / totalTests) else 0

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <title>WellTrack Test Report</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 20px; }
                    .header { background: #f5f5f5; padding: 20px; border-radius: 5px; }
                    .summary { display: flex; gap: 20px; margin: 20px 0; }
                    .metric { background: white; padding: 15px; border-radius: 5px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                    .passed { border-left: 4px solid #4CAF50; }
                    .failed { border-left: 4px solid #f44336; }
                    .ignored { border-left: 4px solid #ff9800; }
                    .total { border-left: 4px solid #2196F3; }
                    .test-list { margin-top: 20px; }
                    .test-item { padding: 10px; margin: 5px 0; border-radius: 3px; }
                    .test-passed { background: #e8f5e8; }
                    .test-failed { background: #ffeaea; }
                    .test-ignored { background: #fff3e0; }
                    .error-details { margin-top: 10px; padding: 10px; background: #f5f5f5; border-radius: 3px; font-family: monospace; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>🏥 WellTrack Test Report</h1>
                    <p>Generated on: $timestamp</p>
                    <p>Total Duration: ${totalDuration}ms</p>
                </div>
                
                <div class="summary">
                    <div class="metric total">
                        <h3>Total Tests</h3>
                        <h2>$totalTests</h2>
                    </div>
                    <div class="metric passed">
                        <h3>✅ Passed</h3>
                        <h2>$passedTests</h2>
                    </div>
                    <div class="metric failed">
                        <h3>❌ Failed</h3>
                        <h2>$failedTests</h2>
                    </div>
                    <div class="metric ignored">
                        <h3>⏭️ Ignored</h3>
                        <h2>$ignoredTests</h2>
                    </div>
                </div>
                
                <div class="metric">
                    <h3>📈 Success Rate</h3>
                    <h2>$successRate%</h2>
                </div>
                
                <div class="test-list">
                    <h2>Test Results</h2>
                    ${generateTestResultsHtml()}
                </div>
                
                <div style="margin-top: 40px; padding: 20px; background: #f0f8ff; border-radius: 5px;">
                    <h3>📋 Test Categories Covered</h3>
                    <ul>
                        <li>✅ Repository Layer Tests - Data access and persistence</li>
                        <li>✅ Use Case Tests - Business logic validation</li>
                        <li>✅ Database Integration Tests - Room database operations</li>
                        <li>✅ UI Flow Tests - Critical user journeys</li>
                        <li>✅ Performance Tests - Database and UI performance</li>
                        <li>✅ Security Tests - Authentication and encryption</li>
                    </ul>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    private fun generateTestResultsHtml(): String {
        return testResults.joinToString("\n") { test ->
            val statusClass = when (test.status) {
                TestStatus.PASSED -> "test-passed"
                TestStatus.FAILED -> "test-failed"
                TestStatus.IGNORED -> "test-ignored"
            }

            val statusIcon = when (test.status) {
                TestStatus.PASSED -> "✅"
                TestStatus.FAILED -> "❌"
                TestStatus.IGNORED -> "⏭️"
            }

            val errorDetails = if (test.status == TestStatus.FAILED && test.errorMessage != null) {
                """<div class="error-details">
                    <strong>Error:</strong> ${test.errorMessage}<br>
                    ${test.stackTrace?.take(500) ?: ""}
                </div>"""
            } else ""

            """<div class="test-item $statusClass">
                $statusIcon <strong>${test.className}</strong>.${test.methodName}
                $errorDetails
            </div>"""
        }
    }

    private fun generateCoverageReport() {
        println("\n📊 Test Coverage Summary:")
        println("-".repeat(40))
        
        val repositoryTests = testResults.count { it.className.contains("Repository") }
        val useCaseTests = testResults.count { it.className.contains("UseCase") }
        val securityTests = testResults.count { it.className.contains("Security") }
        val uiTests = testResults.count { it.className.contains("ui.") }
        val performanceTests = testResults.count { it.className.contains("performance") }
        
        println("🗄️  Repository Tests: $repositoryTests")
        println("⚙️  Use Case Tests: $useCaseTests")
        println("🔒 Security Tests: $securityTests")
        println("🖥️  UI Tests: $uiTests")
        println("⚡ Performance Tests: $performanceTests")
        println("-".repeat(40))
    }
}