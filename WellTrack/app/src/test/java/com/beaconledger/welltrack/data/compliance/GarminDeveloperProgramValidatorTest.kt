package com.beaconledger.welltrack.data.compliance

import android.content.Context
import android.content.res.AssetManager
import com.beaconledger.welltrack.config.EnvironmentConfig
import com.beaconledger.welltrack.data.health.GarminConnectManager
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.io.ByteArrayInputStream

/**
 * Test suite for Garmin Developer Program compliance validation
 * 
 * Tests all aspects of developer program compliance including:
 * - Developer program membership validation
 * - API credentials and registration
 * - Security and privacy standards
 * - Rate limiting compliance
 * - Device compatibility
 */
class GarminDeveloperProgramValidatorTest {
    
    private lateinit var validator: GarminDeveloperProgramValidator
    private lateinit var mockContext: Context
    private lateinit var mockEnvironmentConfig: EnvironmentConfig
    private lateinit var mockGarminConnectManager: GarminConnectManager
    private lateinit var mockGarminLegalComplianceManager: GarminLegalComplianceManager
    private lateinit var mockAssetManager: AssetManager
    
    @Before
    fun setup() {
        mockContext = mockk()
        mockEnvironmentConfig = mockk()
        mockGarminConnectManager = mockk()
        mockGarminLegalComplianceManager = mockk()
        mockAssetManager = mockk()
        
        every { mockContext.assets } returns mockAssetManager
        
        validator = GarminDeveloperProgramValidator(
            context = mockContext,
            environmentConfig = mockEnvironmentConfig,
            garminConnectManager = mockGarminConnectManager,
            garminLegalComplianceManager = mockGarminLegalComplianceManager
        )
    }
    
    @After
    fun tearDown() {
        unmockkAll()
    }
    
    @Test
    fun `validateCompliance returns compliant result when all checks pass`() = runTest {
        // Arrange
        setupValidConfiguration()
        setupValidPrivacyPolicy()
        setupValidLegalCompliance()
        
        // Act
        val result = validator.validateCompliance()
        
        // Assert
        assertTrue("Should be compliant when all checks pass", result.isCompliant)
        assertFalse("Should not have critical issues", result.hasCriticalIssues)
        assertEquals("Should have 6 checks", 6, result.checks.size)
        assertTrue("Summary should indicate compliance", result.summary.contains("Ready for production"))
    }
    
    @Test
    fun `validateCompliance returns non-compliant result when critical checks fail`() = runTest {
        // Arrange
        setupInvalidConfiguration()
        setupValidPrivacyPolicy()
        setupValidLegalCompliance()
        
        // Act
        val result = validator.validateCompliance()
        
        // Assert
        assertFalse("Should not be compliant when critical checks fail", result.isCompliant)
        assertTrue("Should have critical issues", result.hasCriticalIssues)
        assertTrue("Summary should indicate issues", result.summary.contains("Compliance issues found"))
        assertTrue("Should have recommendations", result.recommendations.isNotEmpty())
    }
    
    @Test
    fun `validateDeveloperProgramMembership passes with valid credentials`() = runTest {
        // Arrange
        setupValidConfiguration()
        
        // Act
        val result = validator.validateCompliance()
        val membershipCheck = result.checks.find { it.requirement == "Developer Program Membership" }
        
        // Assert
        assertNotNull("Membership check should exist", membershipCheck)
        assertTrue("Membership check should pass", membershipCheck!!.isCompliant)
        assertEquals("Should be critical severity", ComplianceSeverity.CRITICAL, membershipCheck.severity)
        assertTrue("Details should show valid credentials", membershipCheck.details.contains("✓ Valid"))
    }
    
    @Test
    fun `validateDeveloperProgramMembership fails with invalid credentials`() = runTest {
        // Arrange
        setupInvalidConfiguration()
        
        // Act
        val result = validator.validateCompliance()
        val membershipCheck = result.checks.find { it.requirement == "Developer Program Membership" }
        
        // Assert
        assertNotNull("Membership check should exist", membershipCheck)
        assertFalse("Membership check should fail", membershipCheck!!.isCompliant)
        assertTrue("Details should show invalid credentials", membershipCheck.details.contains("✗ Invalid"))
        assertNotNull("Should have remediation", membershipCheck.remediation)
    }
    
    @Test
    fun `validateDeveloperAgreementCompliance checks OAuth implementation`() = runTest {
        // Arrange
        setupValidConfiguration()
        setupValidPrivacyPolicy()
        setupValidLegalCompliance()
        
        // Act
        val result = validator.validateCompliance()
        val agreementCheck = result.checks.find { it.requirement == "Developer Agreement Compliance" }
        
        // Assert
        assertNotNull("Agreement check should exist", agreementCheck)
        assertTrue("Agreement check should pass", agreementCheck!!.isCompliant)
        assertTrue("Details should mention OAuth", agreementCheck.details.contains("OAuth 2.0 PKCE"))
        assertTrue("Details should mention brand guidelines", agreementCheck.details.contains("Brand Guidelines"))
    }
    
    @Test
    fun `validateDataUsagePolicies checks privacy policy content`() = runTest {
        // Arrange
        setupValidConfiguration()
        setupValidPrivacyPolicy()
        setupValidLegalCompliance()
        
        // Act
        val result = validator.validateCompliance()
        val dataUsageCheck = result.checks.find { it.requirement == "Data Usage Policies" }
        
        // Assert
        assertNotNull("Data usage check should exist", dataUsageCheck)
        assertTrue("Data usage check should pass", dataUsageCheck!!.isCompliant)
        assertTrue("Details should mention transparency", dataUsageCheck.details.contains("Transparency"))
        assertTrue("Details should mention retention", dataUsageCheck.details.contains("Retention"))
    }
    
    @Test
    fun `validateSecurityAndPrivacyStandards checks encryption and HTTPS`() = runTest {
        // Arrange
        setupValidConfiguration()
        setupValidPrivacyPolicy()
        setupValidLegalCompliance()
        
        // Act
        val result = validator.validateCompliance()
        val securityCheck = result.checks.find { it.requirement == "Security and Privacy Standards" }
        
        // Assert
        assertNotNull("Security check should exist", securityCheck)
        assertTrue("Security check should pass", securityCheck!!.isCompliant)
        assertTrue("Details should mention HTTPS", securityCheck.details.contains("HTTPS"))
        assertTrue("Details should mention encryption", securityCheck.details.contains("Encryption"))
        assertEquals("Should be critical severity", ComplianceSeverity.CRITICAL, securityCheck.severity)
    }
    
    @Test
    fun `validateApiRateLimitingCompliance checks rate limiting implementation`() = runTest {
        // Arrange
        setupValidConfiguration()
        setupValidPrivacyPolicy()
        setupValidLegalCompliance()
        
        // Act
        val result = validator.validateCompliance()
        val rateLimitCheck = result.checks.find { it.requirement == "API Rate Limiting Compliance" }
        
        // Assert
        assertNotNull("Rate limit check should exist", rateLimitCheck)
        assertTrue("Rate limit check should pass", rateLimitCheck!!.isCompliant)
        assertTrue("Details should mention rate limiting", rateLimitCheck.details.contains("Rate Limiting"))
        assertTrue("Details should mention backoff", rateLimitCheck.details.contains("Backoff"))
        assertTrue("Details should show API limits", rateLimitCheck.details.contains("200/min"))
    }
    
    @Test
    fun `validateDeviceCompatibility lists supported devices and data types`() = runTest {
        // Arrange
        setupValidConfiguration()
        setupValidPrivacyPolicy()
        setupValidLegalCompliance()
        
        // Act
        val result = validator.validateCompliance()
        val compatibilityCheck = result.checks.find { it.requirement == "Device Compatibility" }
        
        // Assert
        assertNotNull("Compatibility check should exist", compatibilityCheck)
        assertTrue("Compatibility check should pass", compatibilityCheck!!.isCompliant)
        assertTrue("Details should mention device categories", compatibilityCheck.details.contains("Device Categories"))
        assertTrue("Details should mention data types", compatibilityCheck.details.contains("Data Types"))
        assertTrue("Details should mention Forerunner", compatibilityCheck.details.contains("Forerunner"))
        assertTrue("Details should mention HRV", compatibilityCheck.details.contains("HRV"))
    }
    
    @Test
    fun `validateCompliance handles exceptions gracefully`() = runTest {
        // Arrange
        every { mockEnvironmentConfig.garminClientId } throws RuntimeException("Config error")
        
        // Act
        val result = validator.validateCompliance()
        
        // Assert
        assertFalse("Should not be compliant on exception", result.isCompliant)
        assertTrue("Should have critical issues", result.hasCriticalIssues)
        assertEquals("Should have one error check", 1, result.checks.size)
        assertTrue("Summary should indicate failure", result.summary.contains("technical error"))
    }
    
    @Test
    fun `compliance result includes proper recommendations`() = runTest {
        // Arrange
        setupPartiallyValidConfiguration()
        setupValidPrivacyPolicy()
        setupValidLegalCompliance()
        
        // Act
        val result = validator.validateCompliance()
        
        // Assert
        assertTrue("Should have recommendations", result.recommendations.isNotEmpty())
        result.recommendations.forEach { recommendation ->
            assertTrue("Recommendation should have requirement name", recommendation.contains(":"))
            assertFalse("Recommendation should not be empty", recommendation.isBlank())
        }
    }
    
    @Test
    fun `compliance summary shows correct statistics`() = runTest {
        // Arrange
        setupValidConfiguration()
        setupValidPrivacyPolicy()
        setupValidLegalCompliance()
        
        // Act
        val result = validator.validateCompliance()
        
        // Assert
        assertTrue("Summary should show total checks", result.summary.contains("Total Checks: 6"))
        assertTrue("Summary should show passed count", result.summary.contains("Passed: 6"))
        assertTrue("Summary should show failed count", result.summary.contains("Failed: 0"))
    }
    
    // Helper methods for test setup
    
    private fun setupValidConfiguration() {
        every { mockEnvironmentConfig.garminClientId } returns "valid_client_id_12345"
        every { mockEnvironmentConfig.garminClientSecret } returns "valid_client_secret_67890abcdef"
        every { mockEnvironmentConfig.garminRedirectUri } returns "https://welltrack.app/auth/garmin/callback"
    }
    
    private fun setupInvalidConfiguration() {
        every { mockEnvironmentConfig.garminClientId } returns ""
        every { mockEnvironmentConfig.garminClientSecret } returns "short"
        every { mockEnvironmentConfig.garminRedirectUri } returns "http://localhost:3000/callback"
    }
    
    private fun setupPartiallyValidConfiguration() {
        every { mockEnvironmentConfig.garminClientId } returns "valid_client_id_12345"
        every { mockEnvironmentConfig.garminClientSecret } returns "short" // Invalid
        every { mockEnvironmentConfig.garminRedirectUri } returns "https://welltrack.app/auth/garmin/callback"
    }
    
    private fun setupValidPrivacyPolicy() {
        val privacyPolicyContent = """
            <html>
            <body>
                <h1>Privacy Policy</h1>
                <h2>Garmin Data Collection</h2>
                <p>We collect health data from Garmin devices including HRV, recovery, and stress metrics.</p>
                <h2>Data Retention</h2>
                <p>We retain your data for as long as necessary and provide deletion options.</p>
                <h2>Third-Party Sharing</h2>
                <p>We do not share your Garmin data with third parties without consent.</p>
            </body>
            </html>
        """.trimIndent()
        
        every { mockAssetManager.open("privacy_policy.html") } returns 
            ByteArrayInputStream(privacyPolicyContent.toByteArray())
    }
    
    private fun setupValidLegalCompliance() {
        val mockComplianceResult = GarminComplianceResult(
            isCompliant = true,
            checks = listOf(
                ComplianceCheck(
                    requirement = "Garmin Brand Attribution",
                    description = "Test attribution",
                    isCompliant = true,
                    details = "Attribution implemented"
                ),
                ComplianceCheck(
                    requirement = "Privacy Policy Compliance",
                    description = "Test privacy",
                    isCompliant = true,
                    details = "Privacy policy compliant"
                )
            ),
            summary = "All requirements met"
        )
        
        val mockDisclaimers = GarminLegalDisclaimers(
            healthDataDisclaimer = "Health data is for informational purposes only",
            dataAccuracyDisclaimer = "Data accuracy may vary",
            thirdPartyDisclaimer = "Not affiliated with Garmin",
            liabilityDisclaimer = "Use at your own risk"
        )
        
        every { mockGarminLegalComplianceManager.validateDeveloperProgramCompliance() } returns mockComplianceResult
        every { mockGarminLegalComplianceManager.getGarminLegalDisclaimers() } returns mockDisclaimers
    }
}