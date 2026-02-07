package com.beaconledger.welltrack.data.compliance

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.content.res.Resources
import com.beaconledger.welltrack.config.EnvironmentConfig
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.io.ByteArrayInputStream

/**
 * Test suite for brand compliance validation
 * 
 * Tests comprehensive brand compliance validation for all third-party integrations:
 * - Garmin Connect branding and attribution
 * - Samsung Health partnership acknowledgments
 * - Google Health Connect attribution
 * - App store compliance requirements
 * - Privacy policy compliance
 */
class BrandComplianceValidatorTest {
    
    private lateinit var validator: BrandComplianceValidator
    private lateinit var mockContext: Context
    private lateinit var mockEnvironmentConfig: EnvironmentConfig
    private lateinit var mockGarminLegalComplianceManager: GarminLegalComplianceManager
    private lateinit var mockGarminBrandComplianceManager: GarminBrandComplianceManager
    private lateinit var mockAssetManager: AssetManager
    private lateinit var mockResources: Resources
    private lateinit var mockPackageManager: PackageManager
    
    @Before
    fun setup() {
        mockContext = mockk()
        mockEnvironmentConfig = mockk()
        mockGarminLegalComplianceManager = mockk()
        mockGarminBrandComplianceManager = mockk()
        mockAssetManager = mockk()
        mockResources = mockk()
        mockPackageManager = mockk()
        
        every { mockContext.assets } returns mockAssetManager
        every { mockContext.resources } returns mockResources
        every { mockContext.packageManager } returns mockPackageManager
        every { mockContext.packageName } returns "com.beaconledger.welltrack"
        
        validator = BrandComplianceValidator(
            context = mockContext,
            environmentConfig = mockEnvironmentConfig,
            garminLegalComplianceManager = mockGarminLegalComplianceManager,
            garminBrandComplianceManager = mockGarminBrandComplianceManager
        )
    }
    
    @After
    fun tearDown() {
        unmockkAll()
    }
    
    @Test
    fun `validateBrandCompliance returns compliant result when all checks pass`() = runTest {
        // Arrange
        setupValidBrandCompliance()
        
        // Act
        val result = validator.validateBrandCompliance()
        
        // Assert
        assertTrue("Should be compliant when all checks pass", result.isCompliant)
        assertFalse("Should not have critical issues", result.hasCriticalIssues)
        assertEquals("Should have 6 checks", 6, result.checks.size)
        assertTrue("Summary should indicate compliance", result.summary.contains("Ready for app store submission"))
    }
    
    @Test
    fun `validateBrandCompliance returns non-compliant result when critical checks fail`() = runTest {
        // Arrange
        setupInvalidBrandCompliance()
        
        // Act
        val result = validator.validateBrandCompliance()
        
        // Assert
        assertFalse("Should not be compliant when critical checks fail", result.isCompliant)
        assertTrue("Should have critical issues", result.hasCriticalIssues)
        assertTrue("Summary should indicate issues", result.summary.contains("Brand compliance issues found"))
        assertTrue("Should have recommendations", result.recommendations.isNotEmpty())
    }
    
    @Test
    fun `validateGarminBrandingCompliance passes with proper implementation`() = runTest {
        // Arrange
        setupValidGarminBranding()
        
        // Act
        val result = validator.validateBrandCompliance()
        val garminCheck = result.checks.find { it.requirement == "Garmin Branding Compliance" }
        
        // Assert
        assertNotNull("Garmin branding check should exist", garminCheck)
        assertTrue("Garmin branding check should pass", garminCheck!!.isCompliant)
        assertEquals("Should be critical severity", BrandComplianceSeverity.CRITICAL, garminCheck.severity)
        assertTrue("Details should show implemented components", garminCheck.details.contains("✓ Implemented"))
    }
    
    @Test
    fun `validateSamsungHealthCompliance checks partnership acknowledgments`() = runTest {
        // Arrange
        setupValidSamsungHealthCompliance()
        
        // Act
        val result = validator.validateBrandCompliance()
        val samsungCheck = result.checks.find { it.requirement == "Samsung Health Partnership Compliance" }
        
        // Assert
        assertNotNull("Samsung Health check should exist", samsungCheck)
        assertTrue("Samsung Health check should pass", samsungCheck!!.isCompliant)
        assertTrue("Details should mention Samsung Health manager", samsungCheck.details.contains("Samsung Health Manager"))
        assertTrue("Details should mention attribution", samsungCheck.details.contains("Attribution"))
        assertTrue("Details should mention trademarks", samsungCheck.details.contains("Trademarks"))
    }
    
    @Test
    fun `validateHealthConnectCompliance checks Google attribution`() = runTest {
        // Arrange
        setupValidHealthConnectCompliance()
        
        // Act
        val result = validator.validateBrandCompliance()
        val healthConnectCheck = result.checks.find { it.requirement == "Google Health Connect Compliance" }
        
        // Assert
        assertNotNull("Health Connect check should exist", healthConnectCheck)
        assertTrue("Health Connect check should pass", healthConnectCheck!!.isCompliant)
        assertTrue("Details should mention Health Connect manager", healthConnectCheck.details.contains("Health Connect Manager"))
        assertTrue("Details should mention permissions", healthConnectCheck.details.contains("Permissions"))
        assertTrue("Details should mention Google trademarks", healthConnectCheck.details.contains("Google Trademarks"))
    }
    
    @Test
    fun `validateThirdPartyLicensingCompliance checks all licensing requirements`() = runTest {
        // Arrange
        setupValidThirdPartyLicensing()
        
        // Act
        val result = validator.validateBrandCompliance()
        val licensingCheck = result.checks.find { it.requirement == "Third-Party Licensing Compliance" }
        
        // Assert
        assertNotNull("Licensing check should exist", licensingCheck)
        assertTrue("Licensing check should pass", licensingCheck!!.isCompliant)
        assertTrue("Details should mention open source licenses", licensingCheck.details.contains("Open Source Licenses"))
        assertTrue("Details should mention library attributions", licensingCheck.details.contains("Library Attributions"))
        assertTrue("Details should mention API acknowledgments", licensingCheck.details.contains("API Service Acknowledgments"))
    }
    
    @Test
    fun `validateAppStoreCompliance checks health category requirements`() = runTest {
        // Arrange
        setupValidAppStoreCompliance()
        
        // Act
        val result = validator.validateBrandCompliance()
        val appStoreCheck = result.checks.find { it.requirement == "App Store Compliance" }
        
        // Assert
        assertNotNull("App store check should exist", appStoreCheck)
        assertTrue("App store check should pass", appStoreCheck!!.isCompliant)
        assertTrue("Details should mention app store template", appStoreCheck.details.contains("App Store Listing Template"))
        assertTrue("Details should mention health category", appStoreCheck.details.contains("Health Category Compliance"))
        assertTrue("Details should mention medical disclaimers", appStoreCheck.details.contains("Medical Disclaimers"))
    }
    
    @Test
    fun `validatePrivacyPolicyCompliance checks comprehensive coverage`() = runTest {
        // Arrange
        setupValidPrivacyPolicyCompliance()
        
        // Act
        val result = validator.validateBrandCompliance()
        val privacyCheck = result.checks.find { it.requirement == "Privacy Policy Compliance" }
        
        // Assert
        assertNotNull("Privacy policy check should exist", privacyCheck)
        assertTrue("Privacy policy check should pass", privacyCheck!!.isCompliant)
        assertEquals("Should be critical severity", BrandComplianceSeverity.CRITICAL, privacyCheck.severity)
        assertTrue("Details should mention accessibility", privacyCheck.details.contains("Accessibility"))
        assertTrue("Details should mention health data coverage", privacyCheck.details.contains("Health Data Coverage"))
        assertTrue("Details should mention GDPR/CCPA", privacyCheck.details.contains("GDPR/CCPA"))
    }
    
    @Test
    fun `validateBrandCompliance handles exceptions gracefully`() = runTest {
        // Arrange
        every { mockGarminBrandComplianceManager.generateGarminAttribution(any()) } throws RuntimeException("Test error")
        
        // Act
        val result = validator.validateBrandCompliance()
        
        // Assert
        assertFalse("Should not be compliant on exception", result.isCompliant)
        assertTrue("Should have critical issues", result.hasCriticalIssues)
        assertEquals("Should have one error check", 1, result.checks.size)
        assertTrue("Summary should indicate failure", result.summary.contains("technical error"))
    }
    
    @Test
    fun `compliance result includes proper recommendations`() = runTest {
        // Arrange
        setupPartiallyValidBrandCompliance()
        
        // Act
        val result = validator.validateBrandCompliance()
        
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
        setupValidBrandCompliance()
        
        // Act
        val result = validator.validateBrandCompliance()
        
        // Assert
        assertTrue("Summary should show total checks", result.summary.contains("Total Checks: 6"))
        assertTrue("Summary should show passed count", result.summary.contains("Passed: 6"))
        assertTrue("Summary should show failed count", result.summary.contains("Failed: 0"))
    }
    
    // Helper methods for test setup
    
    private fun setupValidBrandCompliance() {
        setupValidGarminBranding()
        setupValidSamsungHealthCompliance()
        setupValidHealthConnectCompliance()
        setupValidThirdPartyLicensing()
        setupValidAppStoreCompliance()
        setupValidPrivacyPolicyCompliance()
    }
    
    private fun setupInvalidBrandCompliance() {
        setupInvalidGarminBranding()
        setupInvalidSamsungHealthCompliance()
        setupInvalidHealthConnectCompliance()
        setupInvalidThirdPartyLicensing()
        setupInvalidAppStoreCompliance()
        setupInvalidPrivacyPolicyCompliance()
    }
    
    private fun setupPartiallyValidBrandCompliance() {
        setupValidGarminBranding()
        setupInvalidSamsungHealthCompliance() // One invalid check
        setupValidHealthConnectCompliance()
        setupValidThirdPartyLicensing()
        setupValidAppStoreCompliance()
        setupValidPrivacyPolicyCompliance()
    }
    
    private fun setupValidGarminBranding() {
        // Mock Garmin brand compliance manager
        every { mockGarminBrandComplianceManager.generateGarminAttribution(any()) } returns "Garmin Fenix 7"
        
        // Mock Garmin legal compliance
        val mockGarminResult = GarminComplianceResult(
            isCompliant = true,
            checks = listOf(
                ComplianceCheck(
                    requirement = "Garmin Brand Attribution",
                    description = "Test attribution",
                    isCompliant = true,
                    details = "Attribution implemented"
                )
            ),
            summary = "All requirements met"
        )
        every { mockGarminLegalComplianceManager.validateDeveloperProgramCompliance() } returns mockGarminResult
        
        // Mock string resources
        every { mockResources.getIdentifier("garmin_trademark_acknowledgment", "string", any()) } returns 1
    }
    
    private fun setupInvalidGarminBranding() {
        every { mockGarminBrandComplianceManager.generateGarminAttribution(any()) } returns null
        
        val mockGarminResult = GarminComplianceResult(
            isCompliant = false,
            checks = listOf(
                ComplianceCheck(
                    requirement = "Garmin Brand Attribution",
                    description = "Test attribution",
                    isCompliant = false,
                    details = "Attribution missing"
                )
            ),
            summary = "Requirements not met"
        )
        every { mockGarminLegalComplianceManager.validateDeveloperProgramCompliance() } returns mockGarminResult
        
        every { mockResources.getIdentifier("garmin_trademark_acknowledgment", "string", any()) } returns 0
    }
    
    private fun setupValidSamsungHealthCompliance() {
        val privacyPolicyContent = """
            <html>
            <body>
                <h1>Privacy Policy</h1>
                <p>Samsung Health integration provides ECG and body composition data.</p>
                <p>Samsung Health is a trademark of Samsung Electronics Co., Ltd.</p>
            </body>
            </html>
        """.trimIndent()
        
        every { mockAssetManager.open("privacy_policy.html") } returns 
            ByteArrayInputStream(privacyPolicyContent.toByteArray())
    }
    
    private fun setupInvalidSamsungHealthCompliance() {
        val privacyPolicyContent = """
            <html>
            <body>
                <h1>Privacy Policy</h1>
                <p>Basic privacy policy without Samsung Health information.</p>
            </body>
            </html>
        """.trimIndent()
        
        every { mockAssetManager.open("privacy_policy.html") } returns 
            ByteArrayInputStream(privacyPolicyContent.toByteArray())
    }
    
    private fun setupValidHealthConnectCompliance() {
        val privacyPolicyContent = """
            <html>
            <body>
                <h1>Privacy Policy</h1>
                <p>Google Health Connect integration provides unified health data access.</p>
                <p>Google Health Connect is a trademark of Google LLC.</p>
            </body>
            </html>
        """.trimIndent()
        
        every { mockAssetManager.open("privacy_policy.html") } returns 
            ByteArrayInputStream(privacyPolicyContent.toByteArray())
        
        // Mock Health Connect permissions
        val mockPackageInfo = PackageInfo().apply {
            requestedPermissions = arrayOf("android.permission.health.READ_HEART_RATE")
        }
        every { mockPackageManager.getPackageInfo(any<String>(), any<Int>()) } returns mockPackageInfo
    }
    
    private fun setupInvalidHealthConnectCompliance() {
        val privacyPolicyContent = """
            <html>
            <body>
                <h1>Privacy Policy</h1>
                <p>Basic privacy policy without Health Connect information.</p>
            </body>
            </html>
        """.trimIndent()
        
        every { mockAssetManager.open("privacy_policy.html") } returns 
            ByteArrayInputStream(privacyPolicyContent.toByteArray())
        
        val mockPackageInfo = PackageInfo().apply {
            requestedPermissions = arrayOf("android.permission.INTERNET")
        }
        every { mockPackageManager.getPackageInfo(any<String>(), any<Int>()) } returns mockPackageInfo
    }
    
    private fun setupValidThirdPartyLicensing() {
        val privacyPolicyContent = """
            <html>
            <body>
                <h1>Privacy Policy</h1>
                <p>This app uses third-party libraries and Supabase API services.</p>
                <p>All trademarks are property of their respective owners. © 2025 WellTrack.</p>
            </body>
            </html>
        """.trimIndent()
        
        every { mockAssetManager.open("privacy_policy.html") } returns 
            ByteArrayInputStream(privacyPolicyContent.toByteArray())
        
        every { mockAssetManager.open("licenses.txt") } returns 
            ByteArrayInputStream("License information".toByteArray())
    }
    
    private fun setupInvalidThirdPartyLicensing() {
        val privacyPolicyContent = """
            <html>
            <body>
                <h1>Privacy Policy</h1>
                <p>Basic privacy policy without licensing information.</p>
            </body>
            </html>
        """.trimIndent()
        
        every { mockAssetManager.open("privacy_policy.html") } returns 
            ByteArrayInputStream(privacyPolicyContent.toByteArray())
        
        every { mockAssetManager.open("licenses.txt") } throws java.io.IOException("File not found")
    }
    
    private fun setupValidAppStoreCompliance() {
        val privacyPolicyContent = """
            <html>
            <body>
                <h1>Privacy Policy</h1>
                <p>This health and fitness app provides medical advice for informational purposes only.</p>
                <p>Children under 13 years are not permitted to use this service.</p>
            </body>
            </html>
        """.trimIndent()
        
        every { mockAssetManager.open("privacy_policy.html") } returns 
            ByteArrayInputStream(privacyPolicyContent.toByteArray())
        
        every { mockAssetManager.open("../../../APP_STORE_LISTING_TEMPLATE.md") } returns 
            ByteArrayInputStream("App store template".toByteArray())
    }
    
    private fun setupInvalidAppStoreCompliance() {
        val privacyPolicyContent = """
            <html>
            <body>
                <h1>Privacy Policy</h1>
                <p>Basic privacy policy.</p>
            </body>
            </html>
        """.trimIndent()
        
        every { mockAssetManager.open("privacy_policy.html") } returns 
            ByteArrayInputStream(privacyPolicyContent.toByteArray())
        
        every { mockAssetManager.open("../../../APP_STORE_LISTING_TEMPLATE.md") } throws java.io.IOException("File not found")
    }
    
    private fun setupValidPrivacyPolicyCompliance() {
        val privacyPolicyContent = """
            <html>
            <body>
                <h1>Privacy Policy</h1>
                <p>Garmin Connect integration provides HRV and recovery data.</p>
                <p>Samsung Health integration provides ECG data.</p>
                <p>Health Connect provides unified health data access.</p>
                <p>You have the right to delete your data under GDPR regulations.</p>
                <p>Data retention policies ensure secure storage and timely deletion.</p>
            </body>
            </html>
        """.trimIndent()
        
        every { mockAssetManager.open("privacy_policy.html") } returns 
            ByteArrayInputStream(privacyPolicyContent.toByteArray())
    }
    
    private fun setupInvalidPrivacyPolicyCompliance() {
        every { mockAssetManager.open("privacy_policy.html") } throws java.io.IOException("File not found")
    }
}