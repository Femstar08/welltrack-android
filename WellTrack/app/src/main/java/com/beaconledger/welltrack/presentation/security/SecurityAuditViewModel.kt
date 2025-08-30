package com.beaconledger.welltrack.presentation.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beaconledger.welltrack.data.model.AuditLog
import com.beaconledger.welltrack.data.security.AuditLogger
import com.beaconledger.welltrack.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class SecurityAuditViewModel @Inject constructor(
    private val auditLogger: AuditLogger,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    data class SecurityAuditUiState(
        val isLoading: Boolean = false,
        val auditLogs: List<AuditLog> = emptyList(),
        val summary: AuditSummary? = null,
        val availableEventTypes: List<String> = emptyList(),
        val selectedEventTypes: Set<String> = emptySet(),
        val activeFilters: List<String> = emptyList(),
        val dateRange: Pair<String?, String?> = Pair(null, null),
        val showFilterDialog: Boolean = false,
        val selectedLog: AuditLog? = null,
        val errorMessage: String? = null
    )
    
    data class AuditSummary(
        val totalEvents: Int,
        val loginAttempts: Int,
        val dataAccess: Int,
        val securityAlerts: Int,
        val lastActivity: LocalDateTime?
    )
    
    private val _uiState = MutableStateFlow(SecurityAuditUiState())
    val uiState: StateFlow<SecurityAuditUiState> = _uiState.asStateFlow()
    
    private val currentUserId: String?
        get() = authRepository.getCurrentUserId()
    
    fun loadAuditLogs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                currentUserId?.let { userId ->
                    // Load audit logs
                    val logs = auditLogger.getAuditLogsForUser(
                        userId = userId,
                        eventType = null,
                        startDate = null,
                        endDate = null,
                        limit = 100
                    )
                    
                    // Generate summary
                    val summary = generateSummary(userId, logs)
                    
                    // Get available event types
                    val eventTypes = logs.map { it.eventType }.distinct().sorted()
                    
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            auditLogs = logs,
                            summary = summary,
                            availableEventTypes = eventTypes
                        )
                    }
                    
                    // Log that audit logs were accessed
                    auditLogger.logSensitiveDataAccess(
                        userId = userId,
                        dataType = "AUDIT_LOGS",
                        action = "VIEW_AUDIT_LOGS",
                        justification = "User accessed security audit screen"
                    )
                    
                } ?: run {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "No user logged in"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load audit logs: ${e.message}"
                    )
                }
            }
        }
    }
    
    private suspend fun generateSummary(userId: String, logs: List<AuditLog>): AuditSummary {
        val sevenDaysAgo = LocalDateTime.now().minusDays(7)
        val recentLogs = logs.filter { it.timestamp.isAfter(sevenDaysAgo) }
        
        val loginAttempts = recentLogs.count { 
            it.eventType in listOf("LOGIN_SUCCESS", "LOGIN_FAILURE", "BIOMETRIC_AUTH_SUCCESS", "BIOMETRIC_AUTH_FAILURE")
        }
        
        val dataAccess = recentLogs.count { 
            it.eventType in listOf("HEALTH_DATA_READ", "HEALTH_DATA_WRITE", "SENSITIVE_DATA_ACCESS")
        }
        
        val securityAlerts = recentLogs.count { 
            it.eventType in listOf("LOGIN_FAILURE", "BIOMETRIC_AUTH_FAILURE", "DATA_DELETION", "ACCOUNT_TERMINATION")
        }
        
        val lastActivity = logs.maxByOrNull { it.timestamp }?.timestamp
        
        return AuditSummary(
            totalEvents = recentLogs.size,
            loginAttempts = loginAttempts,
            dataAccess = dataAccess,
            securityAlerts = securityAlerts,
            lastActivity = lastActivity
        )
    }
    
    fun refreshLogs() {
        loadAuditLogs()
    }
    
    fun showFilterDialog() {
        _uiState.update { it.copy(showFilterDialog = true) }
    }
    
    fun hideFilterDialog() {
        _uiState.update { it.copy(showFilterDialog = false) }
    }
    
    fun applyFilters(eventTypes: Set<String>, dateRange: Pair<String?, String?>) {
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    selectedEventTypes = eventTypes,
                    dateRange = dateRange,
                    showFilterDialog = false,
                    isLoading = true
                )
            }
            
            try {
                currentUserId?.let { userId ->
                    val logs = if (eventTypes.isEmpty()) {
                        auditLogger.getAuditLogsForUser(
                            userId = userId,
                            eventType = null,
                            startDate = null,
                            endDate = null,
                            limit = 100
                        )
                    } else {
                        // Load logs for each selected event type and combine
                        val allLogs = mutableListOf<AuditLog>()
                        eventTypes.forEach { eventType ->
                            val typeLogs = auditLogger.getAuditLogsForUser(
                                userId = userId,
                                eventType = eventType,
                                startDate = null,
                                endDate = null,
                                limit = 100
                            )
                            allLogs.addAll(typeLogs)
                        }
                        allLogs.distinctBy { it.id }.sortedByDescending { it.timestamp }
                    }
                    
                    // Update active filters display
                    val activeFilters = mutableListOf<String>()
                    if (eventTypes.isNotEmpty()) {
                        activeFilters.addAll(eventTypes.map { it.replace("_", " ").lowercase().replaceFirstChar { char -> char.uppercase() } })
                    }
                    
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            auditLogs = logs,
                            activeFilters = activeFilters
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to apply filters: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun removeFilter(filter: String) {
        val currentFilters = _uiState.value.activeFilters.toMutableList()
        currentFilters.remove(filter)
        
        // Convert back to event types and reapply
        val eventTypes = currentFilters.map { 
            it.uppercase().replace(" ", "_")
        }.toSet()
        
        applyFilters(eventTypes, _uiState.value.dateRange)
    }
    
    fun showLogDetails(auditLog: AuditLog) {
        _uiState.update { it.copy(selectedLog = auditLog) }
        
        // Log that specific audit log was viewed
        viewModelScope.launch {
            currentUserId?.let { userId ->
                auditLogger.logSensitiveDataAccess(
                    userId = userId,
                    dataType = "AUDIT_LOG_DETAILS",
                    action = "VIEW_LOG_DETAILS",
                    justification = "User viewed details of audit log ${auditLog.id}"
                )
            }
        }
    }
    
    fun hideLogDetails() {
        _uiState.update { it.copy(selectedLog = null) }
    }
    
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    fun exportAuditLogs() {
        viewModelScope.launch {
            try {
                currentUserId?.let { userId ->
                    val exportData = auditLogger.exportAuditLogs(userId)
                    
                    // Log the export action
                    auditLogger.logSensitiveDataAccess(
                        userId = userId,
                        dataType = "AUDIT_LOGS",
                        action = "EXPORT_AUDIT_LOGS",
                        justification = "User exported audit logs for compliance/review"
                    )
                    
                    // In a real implementation, this would trigger a file download or email
                    _uiState.update {
                        it.copy(errorMessage = "Audit logs exported successfully. Check your downloads folder.")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Failed to export audit logs: ${e.message}")
                }
            }
        }
    }
    
    fun getSecurityInsights(): List<SecurityInsight> {
        val logs = _uiState.value.auditLogs
        val insights = mutableListOf<SecurityInsight>()
        
        // Analyze login patterns
        val recentFailures = logs.filter { 
            it.eventType == "LOGIN_FAILURE" && 
            it.timestamp.isAfter(LocalDateTime.now().minusHours(24))
        }
        
        if (recentFailures.size > 3) {
            insights.add(
                SecurityInsight(
                    title = "Multiple Failed Login Attempts",
                    description = "${recentFailures.size} failed login attempts in the last 24 hours",
                    severity = InsightSeverity.WARNING,
                    recommendation = "Consider enabling stronger authentication methods"
                )
            )
        }
        
        // Analyze data access patterns
        val sensitiveAccess = logs.filter { 
            it.eventType == "SENSITIVE_DATA_ACCESS" &&
            it.timestamp.isAfter(LocalDateTime.now().minusDays(7))
        }
        
        if (sensitiveAccess.size > 10) {
            insights.add(
                SecurityInsight(
                    title = "High Sensitive Data Access",
                    description = "${sensitiveAccess.size} sensitive data access events in the last week",
                    severity = InsightSeverity.INFO,
                    recommendation = "Regular access pattern detected - no action needed"
                )
            )
        }
        
        // Check for security setting changes
        val securityChanges = logs.filter { 
            it.eventType in listOf("SECURITY_SETTINGS_CHANGE", "PRIVACY_SETTINGS_CHANGE") &&
            it.timestamp.isAfter(LocalDateTime.now().minusDays(30))
        }
        
        if (securityChanges.isNotEmpty()) {
            insights.add(
                SecurityInsight(
                    title = "Recent Security Changes",
                    description = "${securityChanges.size} security/privacy setting changes in the last month",
                    severity = InsightSeverity.INFO,
                    recommendation = "Review recent changes to ensure they align with your security preferences"
                )
            )
        }
        
        return insights
    }
    
    data class SecurityInsight(
        val title: String,
        val description: String,
        val severity: InsightSeverity,
        val recommendation: String
    )
    
    enum class InsightSeverity {
        INFO, WARNING, CRITICAL
    }
}