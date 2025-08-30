package com.beaconledger.welltrack.security

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.beaconledger.welltrack.data.database.WellTrackDatabase
import com.beaconledger.welltrack.data.model.AuditLog
import com.beaconledger.welltrack.data.remote.SupabaseClient
import com.beaconledger.welltrack.data.security.*
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class SecurityIntegrationTest {
    
    private lateinit var context: Context
    private lateinit var mockDatabase: WellTrackDatabase
    private lateinit var mockSupabaseClient: SupabaseClient
    private lateinit var securePreferencesManager: SecurePreferencesManager
    private lateinit var auditLogger: AuditLogger
    private lateinit var biometricAuthManager: BiometricAuthManager
    private lateinit var appLockManager: AppLockManager
    private lateinit var privacyControlsManager: PrivacyControlsManager
    private lateinit var secureDataDeletionManager: SecureDataDeletionManager
    private lateinit var securityIntegrationManager: SecurityIntegrationManager
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        mockDatabase = mockk(relaxed = true)
        mockSupabaseClient = mockk(relaxed = true)
        
        // Create real instances for testing
        securePreferencesManager = SecurePreferencesManager(context)
        auditLogger = AuditLogger(context, mockDatabase)
        biometricAuthManager = BiometricAuthManager(context)
        appLockManager = AppLockManager(context, securePreferencesManager)
        privacyControlsManager = PrivacyControlsManager(context, securePreferencesManager, auditLogger)
        secureDataDeletionManager = SecureDataDeletionManager(
            context, mockDatabase, mockSupabaseClient, securePreferencesManager, auditLogger
        )
        securityIntegrationManager = SecurityIntegrationManager(
            context, biometricAuthManager, appLockManager, privacyControlsManager,
            secureDataDeletionManager, auditLogger, securePreferencesManager
        )
        
        // Mock database interactions
        every { mockDatabase.auditLogDao() } returns mockk(relaxed = true)
        coEvery { mockDatabase.auditLogDao().insertAuditLog(any()) } returns Unit
    }
    
    @After
    fun tearDown() {
        // Clear preferences after each test
        securePreferencesManager.clearAll()
        clearAllMocks()
    }
    
    @Test
    fun `test app lock functionality`() = runTest {
        // Initially app should not be locked
        assertFalse(appLockManager.isAppLockEnabled())
        assertFalse(appLockManager.isAppLocked.value)
        
        // Enable app lock
        appLockManager.setAppLockEnabled(true)
        assertTrue(appLockManager.isAppLockEnabled())
        
        // Set timeout
        appLockManager.setLockTimeoutMinutes(5)
        assertEquals(5, appLockManager.getLockTimeoutMinutes())
        
        // Unlock app
        appLockManager.unlockApp()
        assertFalse(appLockManager.isAppLocked.value)
        
        // Lock app manually
        appLockManager.lockApp()
        assertTrue(appLockManager.isAppLocked.value)
    }
    
    @Test
    fun `test biometric authentication availability`() {
        val result = biometricAuthManager.isBiometricAvailable()
        
        // In test environment, biometric is typically not available
        assertTrue(
            result is BiometricAuthManager.BiometricResult.BiometricNotAvailable ||
            result is BiometricAuthManager.BiometricResult.Error
        )
        
        assertFalse(biometricAuthManager.canUseBiometricAuthentication())
    }
    
    @Test
    fun `test privacy controls default settings`() = runTest {
        val defaultSettings = privacyControlsManager.privacySettings.value
        
        // Verify privacy-first defaults
        assertFalse(defaultSettings.dataSharingEnabled)
        assertFalse(defaultSettings.analyticsEnabled)
        assertTrue(defaultSettings.crashReportingEnabled) // Should be true for app stability
        assertFalse(defaultSettings.healthDataSharingEnabled)
        assertFalse(defaultSettings.mealDataSharingEnabled)
        assertFalse(defaultSettings.socialFeaturesEnabled)
        assertFalse(defaultSettings.locationSharingEnabled)
        assertFalse(defaultSettings.thirdPartyIntegrationsEnabled)
        assertTrue(defaultSettings.dataExportAllowed)
        assertFalse(defaultSettings.marketingCommunicationsEnabled)
        assertFalse(defaultSettings.personalizedAdsEnabled)
    }
    
    @Test
    fun `test privacy controls update`() = runTest {
        val newSettings = PrivacyControlsManager.PrivacySettings(
            dataSharingEnabled = true,
            analyticsEnabled = true,
            healthDataSharingEnabled = true
        )
        
        privacyControlsManager.updatePrivacySettings(newSettings, "test-user-id")
        
        val updatedSettings = privacyControlsManager.privacySettings.value
        assertTrue(updatedSettings.dataSharingEnabled)
        assertTrue(updatedSettings.analyticsEnabled)
        assertTrue(updatedSettings.healthDataSharingEnabled)
    }
    
    @Test
    fun `test data sharing permissions`() {
        // Test default permissions (should be restrictive)
        assertFalse(privacyControlsManager.isDataSharingAllowed(PrivacyControlsManager.DataSharingType.HEALTH_DATA))
        assertFalse(privacyControlsManager.isDataSharingAllowed(PrivacyControlsManager.DataSharingType.MEAL_DATA))
        assertFalse(privacyControlsManager.isDataSharingAllowed(PrivacyControlsManager.DataSharingType.ANALYTICS))
        assertTrue(privacyControlsManager.isDataSharingAllowed(PrivacyControlsManager.DataSharingType.CRASH_REPORTS))
        
        // Enable data sharing
        val permissiveSettings = PrivacyControlsManager.PrivacySettings(
            dataSharingEnabled = true,
            analyticsEnabled = true,
            healthDataSharingEnabled = true,
            mealDataSharingEnabled = true
        )
        
        privacyControlsManager.updatePrivacySettings(permissiveSettings)
        
        assertTrue(privacyControlsManager.isDataSharingAllowed(PrivacyControlsManager.DataSharingType.HEALTH_DATA))
        assertTrue(privacyControlsManager.isDataSharingAllowed(PrivacyControlsManager.DataSharingType.MEAL_DATA))
        assertTrue(privacyControlsManager.isDataSharingAllowed(PrivacyControlsManager.DataSharingType.ANALYTICS))
    }
    
    @Test
    fun `test secure preferences encryption`() {
        val testKey = "test_key"
        val testValue = "sensitive_data_12345"
        
        // Store encrypted data
        securePreferencesManager.putString(testKey, testValue)
        
        // Retrieve and verify
        val retrievedValue = securePreferencesManager.getString(testKey)
        assertEquals(testValue, retrievedValue)
        
        // Verify key exists
        assertTrue(securePreferencesManager.contains(testKey))
        
        // Remove and verify
        securePreferencesManager.remove(testKey)
        assertFalse(securePreferencesManager.contains(testKey))
    }
    
    @Test
    fun `test audit logging functionality`() = runTest {
        val testUserId = "test-user-123"
        val testDataType = "HEALTH_DATA"
        val testAction = "READ"
        
        // Mock the DAO to capture the audit log
        val capturedAuditLog = slot<AuditLog>()
        coEvery { mockDatabase.auditLogDao().insertAuditLog(capture(capturedAuditLog)) } returns Unit
        
        // Log a health data access event
        auditLogger.logHealthDataAccess(
            userId = testUserId,
            action = testAction,
            dataType = testDataType,
            recordCount = 5
        )
        
        // Verify the audit log was created correctly
        coVerify { mockDatabase.auditLogDao().insertAuditLog(any()) }
        
        val auditLog = capturedAuditLog.captured
        assertEquals(testUserId, auditLog.userId)
        assertEquals(AuditLogger.EVENT_HEALTH_DATA_READ, auditLog.eventType)
        assertEquals(testAction, auditLog.action)
        assertEquals("HEALTH_DATA", auditLog.resourceType)
        assertTrue(auditLog.additionalInfo?.contains("dataType=$testDataType") == true)
        assertTrue(auditLog.additionalInfo?.contains("recordCount=5") == true)
    }
    
    @Test
    fun `test authentication event logging`() = runTest {
        val testUserId = "test-user-123"
        
        val capturedAuditLog = slot<AuditLog>()
        coEvery { mockDatabase.auditLogDao().insertAuditLog(capture(capturedAuditLog)) } returns Unit
        
        // Log successful authentication
        auditLogger.logAuthentication(
            userId = testUserId,
            eventType = AuditLogger.EVENT_LOGIN_SUCCESS,
            success = true,
            method = "BIOMETRIC"
        )
        
        coVerify { mockDatabase.auditLogDao().insertAuditLog(any()) }
        
        val auditLog = capturedAuditLog.captured
        assertEquals(testUserId, auditLog.userId)
        assertEquals(AuditLogger.EVENT_LOGIN_SUCCESS, auditLog.eventType)
        assertEquals("SUCCESS", auditLog.action)
        assertEquals("AUTHENTICATION", auditLog.resourceType)
        assertTrue(auditLog.additionalInfo?.contains("method=BIOMETRIC") == true)
        assertTrue(auditLog.additionalInfo?.contains("success=true") == true)
    }
    
    @Test
    fun `test security integration manager initialization`() = runTest {
        val securityStatus = securityIntegrationManager.securityStatus.value
        
        // Verify initial security status
        assertFalse(securityStatus.isAppLocked)
        assertFalse(securityStatus.biometricAvailable) // In test environment
        assertFalse(securityStatus.biometricEnabled)
        assertTrue(securityStatus.privacyControlsActive) // Should be true with default privacy-first settings
        assertTrue(securityStatus.auditingEnabled)
        assertEquals(SecurityIntegrationManager.SecurityLevel.BASIC, securityStatus.securityLevel)
    }
    
    @Test
    fun `test security recommendations`() {
        val recommendations = securityIntegrationManager.getSecurityRecommendations()
        
        // Should recommend enabling app lock by default
        assertTrue(recommendations.any { 
            it.action is SecurityIntegrationManager.SecurityAction.EnableAppLock 
        })
        
        // Enable app lock and check recommendations change
        appLockManager.setAppLockEnabled(true)
        
        val updatedRecommendations = securityIntegrationManager.getSecurityRecommendations()
        
        // Should no longer recommend enabling app lock
        assertFalse(updatedRecommendations.any { 
            it.action is SecurityIntegrationManager.SecurityAction.EnableAppLock 
        })
    }
    
    @Test
    fun `test secure data operation logging`() = runTest {
        val testUserId = "test-user-123"
        val testOperation = "DATA_EXPORT"
        val testDataType = "USER_PROFILE"
        
        val capturedAuditLogs = mutableListOf<AuditLog>()
        coEvery { mockDatabase.auditLogDao().insertAuditLog(capture(capturedAuditLogs)) } returns Unit
        
        // Perform secure operation
        securityIntegrationManager.performSecureDataOperation(
            userId = testUserId,
            operation = testOperation,
            dataType = testDataType
        ) {
            // Simulate some data operation
            kotlinx.coroutines.delay(10)
        }
        
        // Verify both start and completion events were logged
        coVerify(exactly = 2) { mockDatabase.auditLogDao().insertAuditLog(any()) }
        
        val startLog = capturedAuditLogs.first()
        val completionLog = capturedAuditLogs.last()
        
        assertEquals("${testOperation}_STARTED", startLog.action)
        assertEquals("${testOperation}_COMPLETED", completionLog.action)
        assertEquals(testDataType, startLog.resourceId)
        assertEquals(testDataType, completionLog.resourceId)
    }
    
    @Test
    fun `test data retention periods`() {
        val healthDataRetention = privacyControlsManager.getDataRetentionPeriod(
            PrivacyControlsManager.DataRetentionType.HEALTH_METRICS
        )
        val mealDataRetention = privacyControlsManager.getDataRetentionPeriod(
            PrivacyControlsManager.DataRetentionType.MEAL_LOGS
        )
        val auditLogRetention = privacyControlsManager.getDataRetentionPeriod(
            PrivacyControlsManager.DataRetentionType.AUDIT_LOGS
        )
        
        // Verify appropriate retention periods
        assertEquals(365 * 7, healthDataRetention) // 7 years for health data
        assertEquals(365 * 2, mealDataRetention) // 2 years for meal data
        assertEquals(365 * 3, auditLogRetention) // 3 years for audit logs
    }
    
    @Test
    fun `test privacy settings export`() {
        val exportData = privacyControlsManager.exportPrivacySettings()
        
        // Verify export contains expected fields
        assertTrue(exportData.containsKey("dataSharingEnabled"))
        assertTrue(exportData.containsKey("analyticsEnabled"))
        assertTrue(exportData.containsKey("healthDataSharingEnabled"))
        assertTrue(exportData.containsKey("exportedAt"))
        
        // Verify default values
        assertEquals(false, exportData["dataSharingEnabled"])
        assertEquals(false, exportData["analyticsEnabled"])
        assertEquals(true, exportData["crashReportingEnabled"])
    }
}