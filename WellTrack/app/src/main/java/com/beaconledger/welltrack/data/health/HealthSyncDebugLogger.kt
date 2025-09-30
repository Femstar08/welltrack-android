package com.beaconledger.welltrack.data.health

import android.content.Context
import android.util.Log
import com.beaconledger.welltrack.data.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.io.File
import java.io.FileWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Comprehensive health sync debugging and logging utility
 * Provides detailed logging for troubleshooting cross-platform health sync issues
 */
@Singleton
class HealthSyncDebugLogger @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val TAG = "HealthSyncDebug"
        private const val LOG_FILE_NAME = "health_sync_debug.log"
        private const val MAX_LOG_FILE_SIZE = 5 * 1024 * 1024 // 5MB
        private const val MAX_LOG_ENTRIES = 1000
    }
    
    private val _debugEvents = MutableSharedFlow<HealthSyncDebugEvent>()
    val debugEvents: SharedFlow<HealthSyncDebugEvent> = _debugEvents.asSharedFlow()
    
    private val logEntries = mutableListOf<HealthSyncLogEntry>()
    private val logFile = File(context.filesDir, LOG_FILE_NAME)
    
    /**
     * Log sync start event
     */
    fun logSyncStart(
        userId: String,
        platforms: List<String>,
        timeRange: Pair<java.time.Instant, java.time.Instant>
    ) {
        val entry = HealthSyncLogEntry(
            timestamp = LocalDateTime.now(),
            level = LogLevel.INFO,
            category = LogCategory.SYNC_LIFECYCLE,
            message = "Health sync started for user $userId",
            details = mapOf(
                "userId" to userId,
                "platforms" to platforms.joinToString(","),
                "startTime" to timeRange.first.toString(),
                "endTime" to timeRange.second.toString(),
                "timeRangeDuration" to "${java.time.Duration.between(timeRange.first, timeRange.second).toHours()}h"
            )
        )
        
        logEntry(entry)
        emitDebugEvent(HealthSyncDebugEvent.SyncStarted(userId, platforms, timeRange))
    }
    
    /**
     * Log platform sync attempt
     */
    fun logPlatformSyncAttempt(
        platform: String,
        isAvailable: Boolean,
        hasPermissions: Boolean,
        isAuthenticated: Boolean? = null
    ) {
        val entry = HealthSyncLogEntry(
            timestamp = LocalDateTime.now(),
            level = LogLevel.INFO,
            category = LogCategory.PLATFORM_SYNC,
            message = "Attempting sync for platform: $platform",
            details = mapOf(
                "platform" to platform,
                "isAvailable" to isAvailable.toString(),
                "hasPermissions" to hasPermissions.toString(),
                "isAuthenticated" to (isAuthenticated?.toString() ?: "N/A"),
                "canSync" to (isAvailable && hasPermissions && (isAuthenticated != false)).toString()
            )
        )
        
        logEntry(entry)
        emitDebugEvent(HealthSyncDebugEvent.PlatformSyncAttempt(platform, isAvailable, hasPermissions, isAuthenticated))
    }
    
    /**
     * Log platform sync result
     */
    fun logPlatformSyncResult(
        platform: String,
        syncStatus: SyncState,
        metricsCount: Int,
        errorMessage: String? = null,
        syncDuration: Long? = null
    ) {
        val level = when (syncStatus) {
            SyncState.SYNCED -> LogLevel.INFO
            SyncState.FAILED -> LogLevel.ERROR
            SyncState.CONFLICT -> LogLevel.WARN
            else -> LogLevel.DEBUG
        }
        
        val entry = HealthSyncLogEntry(
            timestamp = LocalDateTime.now(),
            level = level,
            category = LogCategory.PLATFORM_SYNC,
            message = "Platform sync completed: $platform - $syncStatus",
            details = mapOf(
                "platform" to platform,
                "syncStatus" to syncStatus.toString(),
                "metricsCount" to metricsCount.toString(),
                "errorMessage" to (errorMessage ?: "None"),
                "syncDurationMs" to (syncDuration?.toString() ?: "Unknown")
            )
        )
        
        logEntry(entry)
        emitDebugEvent(HealthSyncDebugEvent.PlatformSyncResult(platform, syncStatus, metricsCount, errorMessage))
    }
    
    /**
     * Log data validation results
     */
    fun logDataValidation(
        totalMetrics: Int,
        validMetrics: Int,
        invalidMetrics: Int,
        validationErrors: List<String>
    ) {
        val level = if (invalidMetrics > 0) LogLevel.WARN else LogLevel.INFO
        
        val entry = HealthSyncLogEntry(
            timestamp = LocalDateTime.now(),
            level = level,
            category = LogCategory.DATA_VALIDATION,
            message = "Data validation completed: $validMetrics/$totalMetrics valid",
            details = mapOf(
                "totalMetrics" to totalMetrics.toString(),
                "validMetrics" to validMetrics.toString(),
                "invalidMetrics" to invalidMetrics.toString(),
                "validationSuccessRate" to "${(validMetrics.toFloat() / totalMetrics * 100).toInt()}%",
                "validationErrors" to validationErrors.joinToString("; ")
            )
        )
        
        logEntry(entry)
        emitDebugEvent(HealthSyncDebugEvent.DataValidation(totalMetrics, validMetrics, invalidMetrics, validationErrors))
    }
    
    /**
     * Log conflict resolution
     */
    fun logConflictResolution(
        conflictType: ConflictType,
        conflictedMetrics: Int,
        resolvedMetrics: Int,
        resolutionStrategy: String,
        details: Map<String, Any> = emptyMap()
    ) {
        val entry = HealthSyncLogEntry(
            timestamp = LocalDateTime.now(),
            level = LogLevel.WARN,
            category = LogCategory.CONFLICT_RESOLUTION,
            message = "Conflict resolution: $conflictType - $conflictedMetrics conflicts resolved to $resolvedMetrics metrics",
            details = mapOf(
                "conflictType" to conflictType.toString(),
                "conflictedMetrics" to conflictedMetrics.toString(),
                "resolvedMetrics" to resolvedMetrics.toString(),
                "resolutionStrategy" to resolutionStrategy,
                "resolutionEfficiency" to "${((resolvedMetrics.toFloat() / conflictedMetrics) * 100).toInt()}%"
            ) + details
        )
        
        logEntry(entry)
        emitDebugEvent(HealthSyncDebugEvent.ConflictResolution(conflictType, conflictedMetrics, resolvedMetrics, resolutionStrategy))
    }
    
    /**
     * Log sync completion
     */
    fun logSyncCompletion(
        userId: String,
        result: HealthSyncResult,
        totalDuration: Long,
        platformStatuses: List<PlatformSyncStatus>
    ) {
        val level = when (result) {
            is HealthSyncResult.Success -> LogLevel.INFO
            is HealthSyncResult.Error -> LogLevel.ERROR
            is HealthSyncResult.PartialSuccess -> LogLevel.WARN
        }
        
        val successfulPlatforms = platformStatuses.count { it.syncStatus == SyncState.SYNCED }
        val totalPlatforms = platformStatuses.size
        
        val entry = HealthSyncLogEntry(
            timestamp = LocalDateTime.now(),
            level = level,
            category = LogCategory.SYNC_LIFECYCLE,
            message = "Health sync completed for user $userId: ${result.javaClass.simpleName}",
            details = mapOf(
                "userId" to userId,
                "resultType" to result.javaClass.simpleName,
                "totalDurationMs" to totalDuration.toString(),
                "successfulPlatforms" to successfulPlatforms.toString(),
                "totalPlatforms" to totalPlatforms.toString(),
                "platformSuccessRate" to "${(successfulPlatforms.toFloat() / totalPlatforms * 100).toInt()}%",
                "syncedMetricsCount" to when (result) {
                    is HealthSyncResult.Success -> result.syncedMetricsCount.toString()
                    is HealthSyncResult.PartialSuccess -> result.syncedMetricsCount.toString()
                    else -> "0"
                }
            )
        )
        
        logEntry(entry)
        emitDebugEvent(HealthSyncDebugEvent.SyncCompleted(userId, result, totalDuration))
    }
    
    /**
     * Log performance metrics
     */
    fun logPerformanceMetrics(
        operation: String,
        duration: Long,
        itemsProcessed: Int,
        memoryUsage: Long? = null
    ) {
        val throughput = if (duration > 0) (itemsProcessed.toFloat() / (duration / 1000f)) else 0f
        
        val entry = HealthSyncLogEntry(
            timestamp = LocalDateTime.now(),
            level = LogLevel.DEBUG,
            category = LogCategory.PERFORMANCE,
            message = "Performance: $operation completed in ${duration}ms",
            details = mapOf(
                "operation" to operation,
                "durationMs" to duration.toString(),
                "itemsProcessed" to itemsProcessed.toString(),
                "throughputPerSecond" to String.format("%.2f", throughput),
                "memoryUsageMB" to (memoryUsage?.let { it / (1024 * 1024) }?.toString() ?: "Unknown")
            )
        )
        
        logEntry(entry)
        emitDebugEvent(HealthSyncDebugEvent.PerformanceMetric(operation, duration, itemsProcessed))
    }
    
    /**
     * Log error with detailed context
     */
    fun logError(
        operation: String,
        error: Throwable,
        context: Map<String, Any> = emptyMap()
    ) {
        val entry = HealthSyncLogEntry(
            timestamp = LocalDateTime.now(),
            level = LogLevel.ERROR,
            category = LogCategory.ERROR,
            message = "Error in $operation: ${error.message}",
            details = mapOf(
                "operation" to operation,
                "errorType" to error.javaClass.simpleName,
                "errorMessage" to (error.message ?: "Unknown error"),
                "stackTrace" to error.stackTraceToString().take(1000) // Limit stack trace length
            ) + context
        )
        
        logEntry(entry)
        emitDebugEvent(HealthSyncDebugEvent.Error(operation, error, context))
    }
    
    /**
     * Log device and environment information
     */
    fun logDeviceInfo() {
        val entry = HealthSyncLogEntry(
            timestamp = LocalDateTime.now(),
            level = LogLevel.INFO,
            category = LogCategory.DEVICE_INFO,
            message = "Device and environment information",
            details = mapOf(
                "androidVersion" to android.os.Build.VERSION.RELEASE,
                "sdkVersion" to android.os.Build.VERSION.SDK_INT.toString(),
                "manufacturer" to android.os.Build.MANUFACTURER,
                "model" to android.os.Build.MODEL,
                "device" to android.os.Build.DEVICE,
                "brand" to android.os.Build.BRAND,
                "appVersion" to getAppVersion(),
                "availableMemoryMB" to getAvailableMemory().toString(),
                "isSamsungDevice" to android.os.Build.MANUFACTURER.lowercase().contains("samsung").toString()
            )
        )
        
        logEntry(entry)
    }
    
    /**
     * Get sync statistics
     */
    fun getSyncStatistics(timeRange: Pair<LocalDateTime, LocalDateTime>? = null): HealthSyncStatistics {
        val relevantEntries = if (timeRange != null) {
            logEntries.filter { it.timestamp.isAfter(timeRange.first) && it.timestamp.isBefore(timeRange.second) }
        } else {
            logEntries.takeLast(100) // Last 100 entries
        }
        
        val syncAttempts = relevantEntries.count { it.category == LogCategory.SYNC_LIFECYCLE && it.message.contains("started") }
        val syncSuccesses = relevantEntries.count { it.category == LogCategory.SYNC_LIFECYCLE && it.message.contains("completed") && it.level == LogLevel.INFO }
        val syncFailures = relevantEntries.count { it.category == LogCategory.SYNC_LIFECYCLE && it.message.contains("completed") && it.level == LogLevel.ERROR }
        
        val platformSyncAttempts = relevantEntries.count { it.category == LogCategory.PLATFORM_SYNC && it.message.contains("Attempting") }
        val platformSyncSuccesses = relevantEntries.count { it.category == LogCategory.PLATFORM_SYNC && it.message.contains("completed") && it.details["syncStatus"] == "SYNCED" }
        
        val validationAttempts = relevantEntries.count { it.category == LogCategory.DATA_VALIDATION }
        val conflictResolutions = relevantEntries.count { it.category == LogCategory.CONFLICT_RESOLUTION }
        val errors = relevantEntries.count { it.level == LogLevel.ERROR }
        
        val avgSyncDuration = relevantEntries
            .filter { it.category == LogCategory.SYNC_LIFECYCLE && it.details.containsKey("totalDurationMs") }
            .mapNotNull { it.details["totalDurationMs"]?.toString()?.toLongOrNull() }
            .average()
            .takeIf { !it.isNaN() } ?: 0.0
        
        return HealthSyncStatistics(
            syncAttempts = syncAttempts,
            syncSuccesses = syncSuccesses,
            syncFailures = syncFailures,
            successRate = if (syncAttempts > 0) (syncSuccesses.toFloat() / syncAttempts) else 0f,
            platformSyncAttempts = platformSyncAttempts,
            platformSyncSuccesses = platformSyncSuccesses,
            platformSuccessRate = if (platformSyncAttempts > 0) (platformSyncSuccesses.toFloat() / platformSyncAttempts) else 0f,
            validationAttempts = validationAttempts,
            conflictResolutions = conflictResolutions,
            totalErrors = errors,
            averageSyncDurationMs = avgSyncDuration.toLong(),
            timeRange = timeRange
        )
    }
    
    /**
     * Export logs to file
     */
    fun exportLogs(): File {
        try {
            FileWriter(logFile, false).use { writer ->
                writer.write("Health Sync Debug Log Export\n")
                writer.write("Generated: ${LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}\n")
                writer.write("Total Entries: ${logEntries.size}\n\n")
                
                logEntries.forEach { entry ->
                    writer.write("${entry.timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)} ")
                    writer.write("[${entry.level}] ")
                    writer.write("[${entry.category}] ")
                    writer.write("${entry.message}\n")
                    
                    if (entry.details.isNotEmpty()) {
                        entry.details.forEach { (key, value) ->
                            writer.write("  $key: $value\n")
                        }
                    }
                    writer.write("\n")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to export logs", e)
        }
        
        return logFile
    }
    
    /**
     * Clear all logs
     */
    fun clearLogs() {
        logEntries.clear()
        if (logFile.exists()) {
            logFile.delete()
        }
        Log.i(TAG, "Health sync debug logs cleared")
    }
    
    /**
     * Get recent log entries
     */
    fun getRecentLogs(count: Int = 50): List<HealthSyncLogEntry> {
        return logEntries.takeLast(count)
    }
    
    /**
     * Filter logs by criteria
     */
    fun filterLogs(
        level: LogLevel? = null,
        category: LogCategory? = null,
        timeRange: Pair<LocalDateTime, LocalDateTime>? = null,
        searchText: String? = null
    ): List<HealthSyncLogEntry> {
        return logEntries.filter { entry ->
            (level == null || entry.level == level) &&
            (category == null || entry.category == category) &&
            (timeRange == null || (entry.timestamp.isAfter(timeRange.first) && entry.timestamp.isBefore(timeRange.second))) &&
            (searchText == null || entry.message.contains(searchText, ignoreCase = true) || 
             entry.details.values.any { it.toString().contains(searchText, ignoreCase = true) })
        }
    }
    
    private fun logEntry(entry: HealthSyncLogEntry) {
        // Add to in-memory list
        logEntries.add(entry)
        
        // Maintain size limit
        if (logEntries.size > MAX_LOG_ENTRIES) {
            logEntries.removeAt(0)
        }
        
        // Log to Android Log
        when (entry.level) {
            LogLevel.DEBUG -> Log.d(TAG, "${entry.category}: ${entry.message}")
            LogLevel.INFO -> Log.i(TAG, "${entry.category}: ${entry.message}")
            LogLevel.WARN -> Log.w(TAG, "${entry.category}: ${entry.message}")
            LogLevel.ERROR -> Log.e(TAG, "${entry.category}: ${entry.message}")
        }
        
        // Write to file (async to avoid blocking)
        try {
            appendToLogFile(entry)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write to log file", e)
        }
    }
    
    private fun appendToLogFile(entry: HealthSyncLogEntry) {
        // Check file size and rotate if necessary
        if (logFile.exists() && logFile.length() > MAX_LOG_FILE_SIZE) {
            rotateLogFile()
        }
        
        FileWriter(logFile, true).use { writer ->
            writer.write("${entry.timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)} ")
            writer.write("[${entry.level}] ")
            writer.write("[${entry.category}] ")
            writer.write("${entry.message}")
            
            if (entry.details.isNotEmpty()) {
                writer.write(" | Details: ${entry.details}")
            }
            writer.write("\n")
        }
    }
    
    private fun rotateLogFile() {
        val backupFile = File(context.filesDir, "${LOG_FILE_NAME}.backup")
        if (backupFile.exists()) {
            backupFile.delete()
        }
        logFile.renameTo(backupFile)
    }
    
    private fun emitDebugEvent(event: HealthSyncDebugEvent) {
        try {
            _debugEvents.tryEmit(event)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to emit debug event", e)
        }
    }
    
    private fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            "${packageInfo.versionName} (${packageInfo.longVersionCode})"
        } catch (e: Exception) {
            "Unknown"
        }
    }
    
    private fun getAvailableMemory(): Long {
        return try {
            val runtime = Runtime.getRuntime()
            (runtime.maxMemory() - runtime.totalMemory() + runtime.freeMemory()) / (1024 * 1024)
        } catch (e: Exception) {
            0L
        }
    }
}

// Data classes for logging
data class HealthSyncLogEntry(
    val timestamp: LocalDateTime,
    val level: LogLevel,
    val category: LogCategory,
    val message: String,
    val details: Map<String, Any> = emptyMap()
)

enum class LogLevel {
    DEBUG, INFO, WARN, ERROR
}

enum class LogCategory {
    SYNC_LIFECYCLE,
    PLATFORM_SYNC,
    DATA_VALIDATION,
    CONFLICT_RESOLUTION,
    PERFORMANCE,
    ERROR,
    DEVICE_INFO
}

enum class ConflictType {
    TEMPORAL_OVERLAP,
    VALUE_MISMATCH,
    SOURCE_PRIORITY,
    MANUAL_OVERRIDE,
    DUPLICATE_DATA
}

data class HealthSyncStatistics(
    val syncAttempts: Int,
    val syncSuccesses: Int,
    val syncFailures: Int,
    val successRate: Float,
    val platformSyncAttempts: Int,
    val platformSyncSuccesses: Int,
    val platformSuccessRate: Float,
    val validationAttempts: Int,
    val conflictResolutions: Int,
    val totalErrors: Int,
    val averageSyncDurationMs: Long,
    val timeRange: Pair<LocalDateTime, LocalDateTime>?
)

sealed class HealthSyncDebugEvent {
    data class SyncStarted(
        val userId: String,
        val platforms: List<String>,
        val timeRange: Pair<java.time.Instant, java.time.Instant>
    ) : HealthSyncDebugEvent()
    
    data class PlatformSyncAttempt(
        val platform: String,
        val isAvailable: Boolean,
        val hasPermissions: Boolean,
        val isAuthenticated: Boolean?
    ) : HealthSyncDebugEvent()
    
    data class PlatformSyncResult(
        val platform: String,
        val syncStatus: SyncState,
        val metricsCount: Int,
        val errorMessage: String?
    ) : HealthSyncDebugEvent()
    
    data class DataValidation(
        val totalMetrics: Int,
        val validMetrics: Int,
        val invalidMetrics: Int,
        val validationErrors: List<String>
    ) : HealthSyncDebugEvent()
    
    data class ConflictResolution(
        val conflictType: ConflictType,
        val conflictedMetrics: Int,
        val resolvedMetrics: Int,
        val resolutionStrategy: String
    ) : HealthSyncDebugEvent()
    
    data class SyncCompleted(
        val userId: String,
        val result: HealthSyncResult,
        val totalDuration: Long
    ) : HealthSyncDebugEvent()
    
    data class PerformanceMetric(
        val operation: String,
        val duration: Long,
        val itemsProcessed: Int
    ) : HealthSyncDebugEvent()
    
    data class Error(
        val operation: String,
        val error: Throwable,
        val context: Map<String, Any>
    ) : HealthSyncDebugEvent()
}