package com.beaconledger.welltrack.data.security

import android.content.Context
import android.os.Build
import com.beaconledger.welltrack.data.database.WellTrackDatabase
import com.beaconledger.welltrack.data.model.AuditLog
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuditLogger @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: WellTrackDatabase
) {
    
    private val auditScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    companion object {
        // Event types for health data access
        const val EVENT_HEALTH_DATA_READ = "HEALTH_DATA_READ"
        const val EVENT_HEALTH_DATA_WRITE = "HEALTH_DATA_WRITE"
        const val EVENT_HEALTH_DATA_DELETE = "HEALTH_DATA_DELETE"
        const val EVENT_HEALTH_DATA_EXPORT = "HEALTH_DATA_EXPORT"
        const val EVENT_HEALTH_DATA_SYNC = "HEALTH_DATA_SYNC"
        
        // Event types for authentication
        const val EVENT_LOGIN_SUCCESS = "LOGIN_SUCCESS"
        const val EVENT_LOGIN_FAILURE = "LOGIN_FAILURE"
        const val EVENT_LOGOUT = "LOGOUT"
        const val EVENT_BIOMETRIC_AUTH_SUCCESS = "BIOMETRIC_AUTH_SUCCESS"
        const val EVENT_BIOMETRIC_AUTH_FAILURE = "BIOMETRIC_AUTH_FAILURE"
        const val EVENT_APP_LOCK = "APP_LOCK"
        const val EVENT_APP_UNLOCK = "APP_UNLOCK"
        
        // Event types for data operations
        const val EVENT_DATA_DELETION = "DATA_DELETION"
        const val EVENT_ACCOUNT_TERMINATION = "ACCOUNT_TERMINATION"
        const val EVENT_PRIVACY_SETTINGS_CHANGE = "PRIVACY_SETTINGS_CHANGE"
        const val EVENT_SECURITY_SETTINGS_CHANGE = "SECURITY_SETTINGS_CHANGE"
        
        // Event types for external integrations
        const val EVENT_EXTERNAL_SYNC = "EXTERNAL_SYNC"
        const val EVENT_THIRD_PARTY_ACCESS = "THIRD_PARTY_ACCESS"
        
        // Event types for sensitive operations
        const val EVENT_SENSITIVE_DATA_ACCESS = "SENSITIVE_DATA_ACCESS"
        const val EVENT_ENCRYPTION_KEY_ROTATION = "ENCRYPTION_KEY_ROTATION"
        const val EVENT_BACKUP_CREATED = "BACKUP_CREATED"
        const val EVENT_BACKUP_RESTORED = "BACKUP_RESTORED"
    }
    
    fun logHealthDataAccess(
        userId: String,
        action: String,
        dataType: String,
        recordCount: Int = 1,
        additionalInfo: String? = null
    ) {
        logEvent(
            userId = userId,
            eventType = EVENT_HEALTH_DATA_READ,
            action = action,
            resourceType = "HEALTH_DATA",
            resourceId = dataType,
            additionalInfo = buildString {
                append("dataType=$dataType, recordCount=$recordCount")
                additionalInfo?.let { append(", $it") }
            }
        )
    }
    
    fun logHealthDataModification(
        userId: String,
        action: String,
        dataType: String,
        recordId: String? = null,
        oldValue: String? = null,
        newValue: String? = null
    ) {
        logEvent(
            userId = userId,
            eventType = EVENT_HEALTH_DATA_WRITE,
            action = action,
            resourceType = "HEALTH_DATA",
            resourceId = recordId ?: dataType,
            additionalInfo = buildString {
                append("dataType=$dataType")
                recordId?.let { append(", recordId=$it") }
                oldValue?.let { append(", oldValue=$it") }
                newValue?.let { append(", newValue=$it") }
            }
        )
    }
    
    fun logAuthentication(
        userId: String?,
        eventType: String,
        success: Boolean,
        method: String,
        failureReason: String? = null
    ) {
        logEvent(
            userId = userId,
            eventType = eventType,
            action = if (success) "SUCCESS" else "FAILURE",
            resourceType = "AUTHENTICATION",
            resourceId = method,
            additionalInfo = buildString {
                append("method=$method, success=$success")
                failureReason?.let { append(", reason=$it") }
            }
        )
    }
    
    fun logDataDeletion(
        userId: String,
        action: String,
        dataType: String? = null
    ) {
        logEvent(
            userId = userId,
            eventType = EVENT_DATA_DELETION,
            action = action,
            resourceType = "DATA_DELETION",
            resourceId = dataType,
            additionalInfo = dataType?.let { "dataType=$it" }
        )
    }
    
    fun logPrivacySettingsChange(
        userId: String,
        action: String,
        settingsDetails: String? = null
    ) {
        logEvent(
            userId = userId,
            eventType = EVENT_PRIVACY_SETTINGS_CHANGE,
            action = action,
            resourceType = "PRIVACY_SETTINGS",
            additionalInfo = settingsDetails
        )
    }
    
    fun logSecuritySettingsChange(
        userId: String,
        action: String,
        settingType: String,
        oldValue: String? = null,
        newValue: String? = null
    ) {
        logEvent(
            userId = userId,
            eventType = EVENT_SECURITY_SETTINGS_CHANGE,
            action = action,
            resourceType = "SECURITY_SETTINGS",
            resourceId = settingType,
            additionalInfo = buildString {
                append("settingType=$settingType")
                oldValue?.let { append(", oldValue=$it") }
                newValue?.let { append(", newValue=$it") }
            }
        )
    }
    
    fun logExternalSync(
        userId: String,
        platform: String,
        action: String,
        recordCount: Int = 0,
        success: Boolean = true,
        errorMessage: String? = null
    ) {
        logEvent(
            userId = userId,
            eventType = EVENT_EXTERNAL_SYNC,
            action = action,
            resourceType = "EXTERNAL_PLATFORM",
            resourceId = platform,
            additionalInfo = buildString {
                append("platform=$platform, recordCount=$recordCount, success=$success")
                errorMessage?.let { append(", error=$it") }
            }
        )
    }
    
    fun logSensitiveDataAccess(
        userId: String,
        dataType: String,
        action: String,
        justification: String? = null
    ) {
        logEvent(
            userId = userId,
            eventType = EVENT_SENSITIVE_DATA_ACCESS,
            action = action,
            resourceType = "SENSITIVE_DATA",
            resourceId = dataType,
            additionalInfo = buildString {
                append("dataType=$dataType")
                justification?.let { append(", justification=$it") }
            }
        )
    }
    
    private fun logEvent(
        userId: String?,
        eventType: String,
        action: String,
        resourceType: String,
        resourceId: String? = null,
        additionalInfo: String? = null
    ) {
        auditScope.launch {
            try {
                val auditLog = AuditLog(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    eventType = eventType,
                    action = action,
                    resourceType = resourceType,
                    resourceId = resourceId,
                    timestamp = LocalDateTime.now(),
                    ipAddress = getDeviceInfo(),
                    userAgent = getUserAgent(),
                    sessionId = getCurrentSessionId(),
                    additionalInfo = additionalInfo
                )
                
                database.auditLogDao().insertAuditLog(auditLog)
                
                // Also log to system log for critical events
                if (isCriticalEvent(eventType)) {
                    android.util.Log.w("WellTrack_Audit", "Critical event: $eventType - $action for user $userId")
                }
                
            } catch (e: Exception) {
                // Audit logging should not crash the app
                android.util.Log.e("AuditLogger", "Failed to log audit event", e)
            }
        }
    }
    
    private fun isCriticalEvent(eventType: String): Boolean {
        return when (eventType) {
            EVENT_DATA_DELETION,
            EVENT_ACCOUNT_TERMINATION,
            EVENT_ENCRYPTION_KEY_ROTATION,
            EVENT_LOGIN_FAILURE,
            EVENT_BIOMETRIC_AUTH_FAILURE -> true
            else -> false
        }
    }
    
    private fun getDeviceInfo(): String {
        return "${Build.MANUFACTURER} ${Build.MODEL} (${Build.VERSION.RELEASE})"
    }
    
    private fun getUserAgent(): String {
        return "WellTrack Android/${getAppVersion()} (${Build.VERSION.RELEASE}; ${Build.MODEL})"
    }
    
    private fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "unknown"
        } catch (e: Exception) {
            "unknown"
        }
    }
    
    private fun getCurrentSessionId(): String? {
        // This would typically come from a session manager
        // For now, return null or implement session tracking
        return null
    }
    
    suspend fun getAuditLogsForUser(
        userId: String,
        eventType: String? = null,
        startDate: LocalDateTime? = null,
        endDate: LocalDateTime? = null,
        limit: Int = 100
    ): List<AuditLog> {
        return database.auditLogDao().getAuditLogsForUser(
            userId = userId,
            eventType = eventType,
            startDate = startDate,
            endDate = endDate,
            limit = limit
        )
    }
    
    suspend fun deleteOldAuditLogs(retentionDays: Int = 1095) { // 3 years default
        val cutoffDate = LocalDateTime.now().minusDays(retentionDays.toLong())
        database.auditLogDao().deleteAuditLogsBefore(cutoffDate)
    }
    
    suspend fun exportAuditLogs(userId: String): List<Map<String, Any?>> {
        val logs = database.auditLogDao().getAllAuditLogsForUser(userId)
        return logs.map { log ->
            mapOf(
                "id" to log.id,
                "eventType" to log.eventType,
                "action" to log.action,
                "resourceType" to log.resourceType,
                "resourceId" to log.resourceId,
                "timestamp" to log.timestamp.toString(),
                "ipAddress" to log.ipAddress,
                "userAgent" to log.userAgent,
                "sessionId" to log.sessionId,
                "additionalInfo" to log.additionalInfo
            )
        }
    }
}