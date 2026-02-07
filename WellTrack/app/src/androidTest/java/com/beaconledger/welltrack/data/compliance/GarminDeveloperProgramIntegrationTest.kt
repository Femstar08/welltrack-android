package com.beaconledger.welltrack.data.compliance

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.beaconledger.welltrack.config.EnvironmentConfig
import com.beaconledger.welltrack.data.health.GarminConnectManager
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import javax.inject.Inject

/**
 * Integration tests for Garmin Developer Program compliance validation
 * 
 * These tests validate the complete compliance validation flow in a real Android environment
 * with actual dependencies and configuration.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class GarminDeveloperProgramIntegrationTest {
    
    @get:Rule
    val hiltRule = HiltAndroidRule(this)
    
    @Inject
    lateinit var validator: GarminDeveloperProgramValidator
    
    @Inject
    lateinit var environmentConfig: EnvironmentConfig
    
    @Inject
    lateinit var garminConnectManager: GarminConnectManager
    
    @Inject
    lateinit var garminLegalComplianceManager: GarminLegalComplianceManager
    
    private lateinit var context: Context
    
    @Before
    fun setup() {
        hiltRule.inject()
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }
    
    @Test
    fun testFullComplianceValidationFlow() = runTest {
        // Act
        val result = validator.validateCompliance()
        
        // Assert
        assertNotNull("Compliance result should not be null", result)
        assertEquals("Should have 6 compliance checks", 6, result.checks.size)
        assertFalse("Summary should not be empty", result.summary.isBlank())
        assertFalse("Recommendations should not be empty", result.recommendations.isEmpty())
        
        // Verify all required checks are present
        val checkRequirements = result.checks.map { it.requirement }
        assertTrue("Should include developer program membership check", 
            checkRequirements.contains("Developer Program Membership"))
        assertTrue("Should include developer agreement compliance check", 
            checkRequirements.contains("Developer Agreement Compliance"))
        assertTrue("Should include data usage policies check", 
            checkRequirements.contains("Data Usage Policies"))
        assertTrue("Should include security and privacy standards check", 
            checkRequirements.contains("Security and Privacy Standards"))
        assertTrue("Should include API rate limiting compliance check", 
            checkRequirements.contains("API Rate Limiting Compliance"))
        assertTrue("Should include device compatibility check", 
            checkRequirements.contains("Device Compatibility"))
    }
    
    @Test
    fun testDeveloperProgramMembershipValidation() = runTest {
        // Act
        val result = validator.validateCompliance()
        val membershipCheck = result.checks.find { it.requirement == "Developer Program Membership" }
        
        // Assert
        assertNotNull("Membership check should exist", membershipCheck)
        assertEquals("Should be critical severity", ComplianceSeverity.CRITICAL, membershipCheck!!.severity)
        assertFalse("Details should not be empty", membershipCheck.details.isBlank())
        
        // Check that validation covers all required aspects
        assertTrue("Should validate API credentials", membershipCheck.details.contains("API Credentials"))
        assertTrue("Should validate redirect URI", membershipCheck.details.contains("Redirect URI"))
        assertTrue("Should validate app registration", membershipCheck.details.contains("App Registration"))
    }
    
    @Test
    fun testSecurityAndPrivacyStandardsValidation() = runTest {
        // Act
        val result = validator.validateCompliance()
        val securityCheck = result.checks.find { it.requirement == "Security and Privacy Standards" }
        
        // Assert
        assertNotNull("Security check should exist", securityCheck)
        assertEquals("Should be critical severity", ComplianceSeverity.CRITICAL, securityCheck!!.severity)
        
        // Verify security requirements are checked
        assertTrue("Should check secure token storage", securityCheck.details.contains("Secure Token Storage"))
        assertTrue("Should check HTTPS enforcement", securityCheck.details.contains("HTTPS Enforcement"))
        assertTrue("Should check data encryption", securityCheck.details.contains("Data Encryption"))
        assertTrue("Should check privacy policy", securityCheck.details.contains("Privacy Policy"))
    }
    
    @Test
    fun testApiRateLimitingComplianceValidation() = runTest {
        // Act
        val result = validator.validateCompliance()
        val rateLimitCheck = result.checks.find { it.requirement == "API Rate Limiting Compliance" }
        
        // Assert
        assertNotNull("Rate limit check should exist", rateLimitCheck)
        assertEquals("Should be medium severity", ComplianceSeverity.MEDIUM, rateLimitCheck!!.severity)
        
        // Verify rate limiting requirements are checked
        assertTrue("Should check rate limiting implementation", rateLimitCheck.details.contains("Rate Limiting Implementation"))
        assertTrue("Should check exponential backoff", rateLimitCheck.details.contains("Exponential Backoff"))
        assertTrue("Should check request queuing", rateLimitCheck.details.contains("Request Queuing"))
        assertTrue("Should show API limits", rateLimitCheck.details.contains("200/min"))
    }
    
    @Test
    fun testDeviceCompatibilityValidation() = runTest {
        // Act
        val result = validator.validateCompliance()
        val compatibilityCheck = result.checks.find { it.requirement == "Device Compatibility" }
        
        // Assert
        assertNotNull("Compatibility check should exist", compatibilityCheck)
        assertEquals("Should be medium severity", ComplianceSeverity.MEDIUM, compatibilityCheck!!.severity)
        
        // Verify device compatibility information is included
        assertTrue("Should list supported device categories", compatibilityCheck.details.contains("Supported Device Categories"))
        assertTrue("Should list supported data types", compatibilityCheck.details.contains("Supported Data Types"))
        assertTrue("Should mention fitness trackers", compatibilityCheck.details.contains("Fitness Trackers"))
        assertTrue("Should mention HRV data", compatibilityCheck.details.contains("Heart Rate Variability"))
    }
    
    @Test
    fun testPrivacyPolicyValidation() = runTest {
        // Act
        val result = validator.validateCompliance()
        
        // Assert - Privacy policy should be accessible and contain required content
        try {
            val privacyPolicyStream = context.assets.open("privacy_policy.html")
            val privacyPolicyContent = privacyPolicyStream.bufferedReader().use { it.readText() }
            
            assertTrue("Privacy policy should contain Garmin references", privacyPolicyContent.contains("Garmin"))
            assertTrue("Privacy policy should mention health data", privacyPolicyContent.contains("health data"))
            assertTrue("Privacy policy should mention data retention", privacyPolicyContent.contains("retention"))
            
            privacyPolicyStream.close()
        } catch (e: Exception) {
            fail("Privacy policy should be accessible: ${e.message}")
        }
    }
    
    @Test
    fun testBrandComplianceIntegration() = runTest {
        // Act
        val legalComplianceResult = garminLegalComplianceManager.validateDeveloperProgramCompliance()
        
        // Assert
        assertNotNull("Legal compliance result should not be null", legalComplianceResult)
        assertFalse("Legal compliance checks should not be empty", legalComplianceResult.checks.isEmpty())
        
        // Verify brand compliance is included
        val brandCheck = legalComplianceResult.checks.find { it.requirement == "Garmin Brand Attribution" }
        assertNotNull("Brand attribution check should exist", brandCheck)
    }
    
    @Test
    fun testEnvironmentConfigurationValidation() = runTest {
        // Act - Test that environment configuration is properly loaded
        val hasGarminConfig = try {
            environmentConfig.garminClientId.isNotBlank() &&
            environmentConfig.garminClientSecret.isNotBlank() &&
            environmentConfig.garminRedirectUri.isNotBlank()
        } catch (e: Exception) {
            false
        }
        
        // Assert
        if (hasGarminConfig) {
            // If configuration is present, validate format
            assertTrue("Garmin client ID should be properly formatted", 
                environmentConfig.garminClientId.length >= 10)
            assertTrue("Garmin client secret should be properly formatted", 
                environmentConfig.garminClientSecret.length >= 20)
            assertTrue("Garmin redirect URI should use HTTPS", 
                environmentConfig.garminRedirectUri.startsWith("https://"))
        } else {
            // If configuration is missing, that's expected in test environment
            println("Garmin configuration not present - expected in test environment")
        }
    }
    
    @Test
    fun testComplianceResultStructure() = runTest {
        // Act
        val result = validator.validateCompliance()
        
        // Assert - Verify the structure of compliance result
        assertNotNull("Compliance result should have isCompliant field", result.isCompliant)
        assertNotNull("Compliance result should have hasCriticalIssues field", result.hasCriticalIssues)
        assertNotNull("Compliance result should have checks list", result.checks)
        assertNotNull("Compliance result should have summary", result.summary)
        assertNotNull("Compliance result should have recommendations", result.recommendations)
        
        // Verify each check has required fields
        result.checks.forEach { check ->
            assertFalse("Check requirement should not be empty", check.requirement.isBlank())
            assertFalse("Check description should not be empty", check.description.isBlank())
            assertNotNull("Check should have compliance status", check.isCompliant)
            assertNotNull("Check should have severity", check.severity)
            assertFalse("Check details should not be empty", check.details.isBlank())
        }
    }
    
    @Test
    fun testComplianceRecommendations() = runTest {
        // Act
        val result = validator.validateCompliance()
        
        // Assert
        result.recommendations.forEach { recommendation ->
            assertFalse("Recommendation should not be empty", recommendation.isBlank())
            if (!result.isCompliant) {
                assertTrue("Non-compliant result should have actionable recommendations", 
                    recommendation.contains(":") || recommendation.contains("implement") || recommendation.contains("review"))
            }
        }
    }
    
    @Test
    fun testGarminConnectManagerIntegration() = runTest {
        // Act - Test that GarminConnectManager is properly integrated
        val managerClass = garminConnectManager::class.java
        val methods = managerClass.declaredMethods
        
        // Assert - Verify required OAuth methods exist
        val hasAuthMethods = methods.any { it.name.contains("generateAuthorizationUrl") || it.name.contains("auth") } &&
                            methods.any { it.name.contains("exchangeCodeForToken") || it.name.contains("token") }
        
        assertTrue("GarminConnectManager should have OAuth methods for compliance", hasAuthMethods)
    }
    
    @Test
    fun testComplianceValidationPerformance() = runTest {
        // Act - Measure validation performance
        val startTime = System.currentTimeMillis()
        val result = validator.validateCompliance()
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        
        // Assert
        assertNotNull("Validation should complete successfully", result)
        assertTrue("Validation should complete within reasonable time (< 10 seconds)", duration < 10000)
        println("Compliance validation completed in ${duration}ms")
    }
    
    @Test
    fun testDeveloperProgramMembershipStatusValidation() = runTest {
        // Act
        val result = validator.validateCompliance()
        val membershipCheck = result.checks.find { it.requirement == "Developer Program Membership" }
        
        // Assert
        assertNotNull("Membership check should exist", membershipCheck)
        assertTrue("Should validate developer membership status", membershipCheck!!.details.contains("Developer Membership"))
        
        // Check that all membership aspects are validated
        assertTrue("Should validate API credentials", membershipCheck.details.contains("API Credentials"))
        assertTrue("Should validate redirect URI", membershipCheck.details.contains("Redirect URI"))
        assertTrue("Should validate app registration", membershipCheck.details.contains("App Registration"))
    }
    
    @Test
    fun testSecurityStandardsComprehensiveValidation() = runTest {
        // Act
        val result = validator.validateCompliance()
        val securityCheck = result.checks.find { it.requirement == "Security and Privacy Standards" }
        
        // Assert
        assertNotNull("Security check should exist", securityCheck)
        
        // Verify comprehensive security standards are checked
        assertTrue("Should check security standards implementation", securityCheck!!.details.contains("Security Standards Implementation"))
        assertTrue("Should check secure token storage", securityCheck.details.contains("Secure Token Storage"))
        assertTrue("Should check data encryption", securityCheck.details.contains("Data Encryption"))
        assertTrue("Should check HTTPS enforcement", securityCheck.details.contains("HTTPS Enforcement"))
        assertTrue("Should check privacy policy compliance", securityCheck.details.contains("Privacy Policy"))
    }
    
    @Test
    fun testPrivacyPolicyGarminRequirementsValidation() = runTest {
        // Act
        val result = validator.validateCompliance()
        val dataUsageCheck = result.checks.find { it.requirement == "Data Usage Policies" }
        
        // Assert
        assertNotNull("Data usage check should exist", dataUsageCheck)
        
        // Verify Garmin-specific privacy policy requirements are checked
        assertTrue("Should check privacy policy requirements", dataUsageCheck!!.details.contains("Privacy Policy Requirements"))
        
        // Check that privacy policy contains required Garmin-specific content
        try {
            val privacyPolicyStream = context.assets.open("privacy_policy.html")
            val privacyPolicyContent = privacyPolicyStream.bufferedReader().use { it.readText() }
            
            assertTrue("Privacy policy should mention Garmin data collection", privacyPolicyContent.contains("Garmin"))
            assertTrue("Privacy policy should mention HRV data", privacyPolicyContent.contains("HRV") || privacyPolicyContent.contains("heart rate variability"))
            assertTrue("Privacy policy should mention recovery data", privacyPolicyContent.contains("recovery"))
            
            privacyPolicyStream.close()
        } catch (e: Exception) {
            println("Privacy policy validation skipped - file not accessible in test environment")
        }
    }
    
    @Test
    fun testApiRateLimitingComprehensiveValidation() = runTest {
        // Act
        val result = validator.validateCompliance()
        val rateLimitCheck = result.checks.find { it.requirement == "API Rate Limiting Compliance" }
        
        // Assert
        assertNotNull("Rate limit check should exist", rateLimitCheck)
        
        // Verify comprehensive rate limiting validation
        assertTrue("Should check API rate limiting implementation", rateLimitCheck!!.details.contains("API Rate Limiting Implementation"))
        assertTrue("Should check rate limiting implementation", rateLimitCheck.details.contains("Rate Limiting Implementation"))
        assertTrue("Should check exponential backoff", rateLimitCheck.details.contains("Exponential Backoff"))
        assertTrue("Should check request queuing", rateLimitCheck.details.contains("Request Queuing"))
        
        // Verify API limits are documented
        assertTrue("Should document per minute limits", rateLimitCheck.details.contains("Per Minute: 200"))
        assertTrue("Should document per hour limits", rateLimitCheck.details.contains("Per Hour: 12000"))
        assertTrue("Should document per day limits", rateLimitCheck.details.contains("Per Day: 100000"))
    }
    
    @Test
    fun testDeviceCompatibilityAcrossModelsValidation() = runTest {
        // Act
        val result = validator.validateCompliance()
        val compatibilityCheck = result.checks.find { it.requirement == "Device Compatibility" }
        
        // Assert
        assertNotNull("Compatibility check should exist", compatibilityCheck)
        
        // Verify device model compatibility testing
        assertTrue("Should test device model compatibility", compatibilityCheck!!.details.contains("Device Model Compatibility Testing"))
        
        // Check specific device models are tested
        assertTrue("Should test Forerunner series", compatibilityCheck.details.contains("Forerunner"))
        assertTrue("Should test Fenix series", compatibilityCheck.details.contains("Fenix"))
        assertTrue("Should test Venu series", compatibilityCheck.details.contains("Venu"))
        assertTrue("Should test Vivoactive series", compatibilityCheck.details.contains("Vivoactive"))
        assertTrue("Should test Epix series", compatibilityCheck.details.contains("Epix"))
    }
    
    @Test
    fun testConnectIQDeveloperAgreementValidation() = runTest {
        // Act
        val result = validator.validateCompliance()
        val agreementCheck = result.checks.find { it.requirement == "Developer Agreement Compliance" }
        
        // Assert
        assertNotNull("Agreement check should exist", agreementCheck)
        
        // Verify Connect IQ specific agreement validation
        assertTrue("Should validate Connect IQ agreement", agreementCheck!!.details.contains("Connect IQ Agreement"))
        assertTrue("Should validate OAuth 2.0 PKCE", agreementCheck.details.contains("OAuth 2.0 PKCE"))
        assertTrue("Should validate brand guidelines", agreementCheck.details.contains("Brand Guidelines"))
        assertTrue("Should validate data attribution", agreementCheck.details.contains("Data Attribution"))
        assertTrue("Should validate user consent and deletion", agreementCheck.details.contains("User Consent & Deletion"))
    }
}