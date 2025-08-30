package com.beaconledger.welltrack.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey
    val id: String,
    val userId: String?,
    val eventType: String,
    val action: String,
    val resourceType: String,
    val resourceId: String?,
    val timestamp: LocalDateTime,
    val ipAddress: String?,
    val userAgent: String?,
    val sessionId: String?,
    val additionalInfo: String?
)