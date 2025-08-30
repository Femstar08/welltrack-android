package com.beaconledger.welltrack.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.beaconledger.welltrack.data.model.AuditLog
import java.time.LocalDateTime

@Dao
interface AuditLogDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(auditLog: AuditLog)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLogs(auditLogs: List<AuditLog>)
    
    @Query("""
        SELECT * FROM audit_logs 
        WHERE userId = :userId 
        AND (:eventType IS NULL OR eventType = :eventType)
        AND (:startDate IS NULL OR timestamp >= :startDate)
        AND (:endDate IS NULL OR timestamp <= :endDate)
        ORDER BY timestamp DESC 
        LIMIT :limit
    """)
    suspend fun getAuditLogsForUser(
        userId: String,
        eventType: String? = null,
        startDate: LocalDateTime? = null,
        endDate: LocalDateTime? = null,
        limit: Int = 100
    ): List<AuditLog>
    
    @Query("SELECT * FROM audit_logs WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getAllAuditLogsForUser(userId: String): List<AuditLog>
    
    @Query("""
        SELECT * FROM audit_logs 
        WHERE eventType = :eventType 
        AND timestamp >= :startDate 
        ORDER BY timestamp DESC 
        LIMIT :limit
    """)
    suspend fun getAuditLogsByType(
        eventType: String,
        startDate: LocalDateTime,
        limit: Int = 100
    ): List<AuditLog>
    
    @Query("""
        SELECT * FROM audit_logs 
        WHERE resourceType = :resourceType 
        AND resourceId = :resourceId 
        ORDER BY timestamp DESC 
        LIMIT :limit
    """)
    suspend fun getAuditLogsByResource(
        resourceType: String,
        resourceId: String,
        limit: Int = 50
    ): List<AuditLog>
    
    @Query("SELECT COUNT(*) FROM audit_logs WHERE userId = :userId")
    suspend fun getAuditLogCountForUser(userId: String): Int
    
    @Query("SELECT COUNT(*) FROM audit_logs WHERE eventType = :eventType AND timestamp >= :startDate")
    suspend fun getEventCountSince(eventType: String, startDate: LocalDateTime): Int
    
    @Query("DELETE FROM audit_logs WHERE timestamp < :cutoffDate")
    suspend fun deleteAuditLogsBefore(cutoffDate: LocalDateTime): Int
    
    @Query("DELETE FROM audit_logs WHERE userId = :userId")
    suspend fun deleteAllAuditLogsForUser(userId: String): Int
    
    @Query("""
        SELECT * FROM audit_logs 
        WHERE userId = :userId 
        AND eventType IN ('HEALTH_DATA_READ', 'HEALTH_DATA_WRITE', 'HEALTH_DATA_DELETE', 'SENSITIVE_DATA_ACCESS')
        ORDER BY timestamp DESC 
        LIMIT :limit
    """)
    suspend fun getSensitiveDataAccessLogs(userId: String, limit: Int = 100): List<AuditLog>
    
    @Query("""
        SELECT * FROM audit_logs 
        WHERE userId = :userId 
        AND eventType IN ('LOGIN_SUCCESS', 'LOGIN_FAILURE', 'BIOMETRIC_AUTH_SUCCESS', 'BIOMETRIC_AUTH_FAILURE', 'APP_LOCK', 'APP_UNLOCK')
        ORDER BY timestamp DESC 
        LIMIT :limit
    """)
    suspend fun getAuthenticationLogs(userId: String, limit: Int = 100): List<AuditLog>
    
    @Query("""
        SELECT * FROM audit_logs 
        WHERE userId = :userId 
        AND eventType IN ('DATA_DELETION', 'ACCOUNT_TERMINATION', 'PRIVACY_SETTINGS_CHANGE', 'SECURITY_SETTINGS_CHANGE')
        ORDER BY timestamp DESC 
        LIMIT :limit
    """)
    suspend fun getSecurityEventLogs(userId: String, limit: Int = 100): List<AuditLog>
    
    @Query("""
        SELECT eventType, COUNT(*) as count 
        FROM audit_logs 
        WHERE userId = :userId 
        AND timestamp >= :startDate 
        GROUP BY eventType 
        ORDER BY count DESC
    """)
    suspend fun getEventTypeSummary(userId: String, startDate: LocalDateTime): List<EventTypeSummary>
    
    data class EventTypeSummary(
        val eventType: String,
        val count: Int
    )
}