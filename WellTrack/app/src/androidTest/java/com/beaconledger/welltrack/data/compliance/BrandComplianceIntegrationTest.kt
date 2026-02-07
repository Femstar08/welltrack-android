package com.beaconledger.welltrack.data.compliance

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
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
 * Integration tests for brand compliance validation
 * 
 * These tests validate the complete brand compliance validation flow in a real Android environment
 * with actual dependencies and configuration.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class BrandComplianceIntegrationTest {
    
    @get:Rule
    val hiltRule = HiltAndroidRule(this)
    
    @Inject
    lateinit var validator: BrandComplianceValidator
    
    private lateinit var context: Context
    
    @Before
    fun setup() {
        hiltRule.inject()
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }
    
    @Test
    fun testFullBrandComplianceValidationFlow() = runTest {
        // Act
        val result = validator.validateBrandCompliance()
        
        // Assert
        assertNotNull("Compliance result should not be null", result)
        assertEquals("Should have 6 compliance checks", 6, result.checks.size)
        assertFalse("Summary should not be empty", result.summary.isBlank())
        assertFalse("Recommendations should not be empty", result.recommendations.isEmpty())
        
        // Verify all required checks are present
        val checkRequirements = result.checks.map { it.requirement }
        assertTrue("Should include Garmin branding compliance check", 
            checkRequirements.contains("Garmin Branding Compliance"))
        assertTrue("Should include Samsung Health partnership compliance check", 
            checkRequirements.contains("Samsung Health Partnership Compliance"))
        assertTrue("Should include Google Health Connect compliance check", 
            checkRequirements.contains("Google Health Connect Compliance"))
        assertTrue("Should include third-party licensing compliance check", 
            checkRequirements.contains("Third-Party Licensing Compliance"))
        assertTrue("Should include app store compliance check", 
            checkRequirements.contains("App Store Compliance"))
        assertTrue("Should include privacy policy compliance check", 
            checkRequirements.contains("Privacy Policy Compliance"))
    }
    
    @Test
    fun testGarminBrandingComplianceValidation() = runTest {
        // Act
        val result = validator.validateBrandCompliance()
        val garminCheck = result.checks.find { it.requirement == "Garmin Branding Compliance" }
        
        // Assert
        assertNotNull("Garmin branding check should exist", garminCheck)
        assertEquals("Should be critical severity", BrandComplianceSeverity.CRITICAL, garminCheck!!.severity)
        assertFalse("Details should not be empty", garminCheck.details.isBlank())
        
        // Check that validation covers all required aspects
        assertTrue("Should validate Garmin brand manager", garminCheck.details.contains("Garmin Brand Manager"))
        assertTrue("Should validate Works with Garmin badge", garminCheck.details.contains("Works with Garmin Badge"))
        assertTrue("Should validate Garmin attribution", garminCheck.details.contains("Garmin Attribution"))
        assertTrue("Should validate trademark compliance", garminCheck.details.contains("Trademark Compliance"))
    }
    
    @Test
    fun testSamsungHealthPartnershipComplianceValidation() = runTest {
        // Act
        val result = validator.validateBrandCompliance()
        val samsungCheck = result.checks.find { it.requirement == "Samsung Health Partnership Compliance" }
        
        // Assert
        assertNotNull("Samsung Health check should exist", samsungCheck)
        assertEquals("Should be high severity", BrandComplianceSeverity.HIGH, samsungCheck!!.severity)
        
        // Verify Samsung Health requirements are checked
        assertTrue("Should check Samsung Health manager", samsungCheck.details.contains("Samsung Health Manager"))
        assertTrue("Should check Samsung Health attribution", samsungCheck.details.contains("Samsung Health Attribution"))
        assertTrue("Should check Samsung Health trademarks", samsungCheck.details.contains("Samsung Health Trademarks"))
        assertTrue("Should check Samsung Health privacy section", samsungCheck.details.contains("Samsung Health Privacy Section"))
    }
    
    @Test
    fun testHealthConnectComplianceValidation() = runTest {
        // Act
        val result = validator.validateBrandCompliance()
        val healthConnectCheck = result.checks.find { it.requirement == "Google Health Connect Compliance" }
        
        // Assert
        assertNotNull("Health Connect check should exist", healthConnectCheck)
        assertEquals("Should be high severity", BrandComplianceSeverity.HIGH, healthConnectCheck!!.severity)
        
        // Verify Health Connect requirements are checked
        assertTrue("Should check Health Connect manager", healthConnectCheck.details.contains("Health Connect Manager"))
        assertTrue("Should check Health Connect attribution", healthConnectCheck.details.contains("Health Connect Attribution"))
        assertTrue("Should check Health Connect permissions", healthConnectCheck.details.contains("Health Connect Permissions"))
        assertTrue("Should check Google trademarks", healthConnectCheck.details.contains("Google Trademarks"))
    }
    
    @Test
    fun testThirdPartyLicensingComplianceValidation() = runTest {
        // Act
        val result = validator.validateBrandCompliance()
        val licensingCheck = result.checks.find { it.requirement == "Third-Party Licensing Compliance" }
        
        // Assert
        assertNotNull("Licensing check should exist", licensingCheck)
        assertEquals("Should be medium severity", BrandComplianceSeverity.MEDIUM, licensingCheck!!.severity)
        
        // Verify licensing requirements are checked
        assertTrue("Should check open source licenses", licensingCheck.details.contains("Open Source Licenses"))
        assertTrue("Should check library attributions", licensingCheck.details.contains("Library Attributions"))
        assertTrue("Should check API service acknowledgments", licensingCheck.details.contains("API Service Acknowledgments"))
        assertTrue("Should check trademark notices", licensingCheck.details.contains("Trademark & Copyright Notices"))
    }
    
    @Test
    fun testAppStoreComplianceValidation() = runTest {
        // Act
        val result = validator.validateBrandCompliance()
        val appStoreCheck = result.checks.find { it.requirement == "App Store Compliance" }
        
        // Assert
        assertNotNull("App store check should exist", appStoreCheck)
        assertEquals("Should be high severity", BrandComplianceSeverity.HIGH, appStoreCheck!!.severity)
        
        // Verify app store requirements are checked
        assertTrue("Should check app store listing template", appStoreCheck.details.contains("App Store Listing Template"))
        assertTrue("Should check health category compliance", appStoreCheck.details.contains("Health Category Compliance"))
        assertTrue("Should check medical disclaimers", appStoreCheck.details.contains("Medical Disclaimers"))
        assertTrue("Should check age rating compliance", appStoreCheck.details.contains("Age Rating Compliance"))
    }
    
    @Test
    fun testPrivacyPolicyComplianceValidation() = runTest {
        // Act
        val result = validator.validateBrandCompliance()
        val privacyCheck = result.checks.find { it.requirement == "Privacy Policy Compliance" }
        
        // Assert
        assertNotNull("Privacy policy check should exist", privacyCheck)
        assertEquals("Should be critical severity", BrandComplianceSeverity.CRITICAL, privacyCheck!!.severity)
        
        // Verify privacy policy requirements are checked
        assertTrue("Should check privacy policy accessibility", privacyCheck.details.contains("Privacy Policy Accessibility"))
        assertTrue("Should check health data coverage", privacyCheck.details.contains("Health Data Coverage"))
        assertTrue("Should check GDPR/CCPA compliance", privacyCheck.details.contains("GDPR/CCPA Compliance"))
        assertTrue("Should check data retention policies", privacyCheck.details.contains("Data Retention Policies"))
    }
    
    @Test
    fun testPrivacyPolicyContentValidation() = runTest {
        // Act - Test that privacy policy contains required content
        try {
            val privacyPolicyStream = context.assets.open("privacy_policy.html")
            val privacyPolicyContent = privacyPolicyStream.bufferedReader().use { it.readText() }
            
            // Assert - Verify privacy policy contains all required health platform information
            assertTrue("Privacy policy should contain Garmin references", privacyPolicyContent.contains("Garmin"))
            assertTrue("Privacy policy should contain Samsung Health references", privacyPolicyContent.contains("Samsung Health"))
            assertTrue("Privacy policy should contain Health Connect references", privacyPolicyContent.contains("Health Connect"))
            assertTrue("Privacy policy should mention HRV data", privacyPolicyContent.contains("HRV") || privacyPolicyContent.contains("Heart Rate Variability"))
            assertTrue("Privacy policy should mention recovery data", privacyPolicyContent.contains("recovery"))
            assertTrue("Privacy policy should mention data retention", privacyPolicyContent.contains("retention"))
            assertTrue("Privacy policy should mention data deletion", privacyPolicyContent.contains("deletion"))
            
            privacyPolicyStream.close()
        } catch (e: Exception) {
            fail("Privacy policy should be accessible: ${e.message}")
        }
    }
    
    @Test
    fun testHealthPlatformManagersExist() = runTest {
        // Act - Test that all required health platform managers are implemented
        val hasGarminManager = try {
            Class.forName("com.beaconledger.welltrack.data.health.GarminConnectManager")
            true
        } catch (e: ClassNotFoundException) {
            false
        }
        
        val hasSamsungManager = try {
            Class.forName("com.beaconledger.welltrack.data.health.SamsungHealthManager")
            true
        } catch (e: ClassNotFoundException) {
            false
        }
        
        val hasHealthConnectManager = try {
            Class.forName("com.beaconledger.welltrack.data.health.HealthConnectManager")
            true
        } catch (e: ClassNotFoundException) {
            false
        }
        
        // Assert
        assertTrue("Garmin Connect Manager should exist", hasGarminManager)
        assertTrue("Samsung Health Manager should exist", hasSamsungManager)
        assertTrue("Health Connect Manager should exist", hasHealthConnectManager)
    }
    
    @Test
    fun testBrandComplianceComponentsExist() = runTest {
        // Act - Test that all required brand compliance components are implemented
        val hasGarminBrandManager = try {
            Class.forName("com.beaconledger.welltrack.data.compliance.GarminBrandComplianceManager")
            true
        } catch (e: ClassNotFoundException) {
            false
        }
        
        val hasGarminLegalManager = try {
            Class.forName("com.beaconledger.welltrack.data.compliance.GarminLegalComplianceManager")
            true
        } catch (e: ClassNotFoundException) {
            false
        }
        
        val hasAttributionComponents = try {
            Class.forName("com.beaconledger.welltrack.presentation.components.GarminAttributionComponents")
            true
        } catch (e: ClassNotFoundException) {
            false
        }
        
        val hasHealthPlatformComponents = try {
            Class.forName("com.beaconledger.welltrack.presentation.components.HealthPlatformAttributionComponents")
            true
        } catch (e: ClassNotFoundException) {
            false
        }
        
        // Assert
        assertTrue("Garmin Brand Compliance Manager should exist", hasGarminBrandManager)
        assertTrue("Garmin Legal Compliance Manager should exist", hasGarminLegalManager)
        assertTrue("Garmin Attribution Components should exist", hasAttributionComponents)
        assertTrue("Health Platform Attribution Components should exist", hasHealthPlatformComponents)
    }
    
    @Test
    fun testComplianceResultStructure() = runTest {
        // Act
        val result = validator.validateBrandCompliance()
        
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
        val result = validator.validateBrandCompliance()
        
        // Assert
        result.recommendations.forEach { recommendation ->
            assertFalse("Recommendation should not be empty", recommendation.isBlank())
            if (!result.isCompliant) {
                assertTrue("Non-compliant result should have actionable recommendations", 
                    recommendation.contains(":") || recommendation.contains("implement") || recommendation.contains("ensure"))
            }
        }
    }
    
    @Test
    fun testBrandComplianceValidationPerformance() = runTest {
        // Act - Measure validation performance
        val startTime = System.currentTimeMillis()
        val result = validator.validateBrandCompliance()
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        
        // Assert
        assertNotNull("Validation should complete successfully", result)
        assertTrue("Validation should complete within reasonable time (< 5 seconds)", duration < 5000)
        println("Brand compliance validation completed in ${duration}ms")
    }
    
    @Test
    fun testHealthPlatformStringResources() = runTest {
        // Act - Test that all required string resources exist
        val resources = context.resources
        val packageName = context.packageName
        
        // Assert - Check key string resources exist
        val samsungHealthAttribution = resources.getIdentifier("samsung_health_attribution", "string", packageName)
        val healthConnectAttribution = resources.getIdentifier("health_connect_attribution", "string", packageName)
        val worksWithSamsungHealth = resources.getIdentifier("works_with_samsung_health", "string", packageName)
        val worksWithHealthConnect = resources.getIdentifier("works_with_health_connect", "string", packageName)
        
        assertTrue("Samsung Health attribution string should exist", samsungHealthAttribution != 0)
        assertTrue("Health Connect attribution string should exist", healthConnectAttribution != 0)
        assertTrue("Works with Samsung Health string should exist", worksWithSamsungHealth != 0)
        assertTrue("Works with Health Connect string should exist", worksWithHealthConnect != 0)
    }
}