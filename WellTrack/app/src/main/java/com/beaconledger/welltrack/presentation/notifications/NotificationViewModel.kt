package com.beaconledger.welltrack.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beaconledger.welltrack.data.model.NotificationEntity
import com.beaconledger.welltrack.data.model.NotificationPreferences
import com.beaconledger.welltrack.domain.usecase.NotificationUseCase
import com.beaconledger.welltrack.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationUseCase: NotificationUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()
    
    private fun getCurrentUserId(): String {
        return authRepository.getCurrentUserId() ?: throw IllegalStateException("User not authenticated")
    }
    
    init {
        loadNotificationPreferences()
        loadUserNotifications()
    }
    
    private fun loadNotificationPreferences() {
        viewModelScope.launch {
            try {
                val currentUserId = getCurrentUserId()
                val preferences = notificationUseCase.getNotificationPreferences(currentUserId)
                _uiState.update { it.copy(preferences = preferences, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Failed to load notification preferences: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }
    
    private fun loadUserNotifications() {
        viewModelScope.launch {
            try {
                val currentUserId = getCurrentUserId()
                notificationUseCase.getUserNotifications(currentUserId)
                    .catch { e ->
                        _uiState.update { 
                            it.copy(error = "Failed to load notifications: ${e.message}")
                        }
                    }
                    .collect { notifications ->
                        _uiState.update { it.copy(notifications = notifications) }
                    }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Failed to load notifications: ${e.message}")
                }
            }
        }
    }
    
    fun updateMealRemindersEnabled(enabled: Boolean) {
        updatePreferences { it.copy(mealRemindersEnabled = enabled) }
        if (enabled) {
            setupMealReminders()
        }
    }
    
    fun updateSupplementRemindersEnabled(enabled: Boolean) {
        updatePreferences { it.copy(supplementRemindersEnabled = enabled) }
        if (enabled) {
            setupSupplementReminders()
        }
    }
    
    fun updatePantryAlertsEnabled(enabled: Boolean) {
        updatePreferences { it.copy(pantryAlertsEnabled = enabled) }
        if (enabled) {
            setupPantryAlerts()
        }
    }
    
    fun updateMotivationalNotificationsEnabled(enabled: Boolean) {
        updatePreferences { it.copy(motivationalNotificationsEnabled = enabled) }
        if (enabled) {
            setupMotivationalNotifications()
        }
    }
    
    fun updateWaterRemindersEnabled(enabled: Boolean) {
        updatePreferences { it.copy(waterRemindersEnabled = enabled) }
    }
    
    fun updateBloodTestRemindersEnabled(enabled: Boolean) {
        updatePreferences { it.copy(bloodTestRemindersEnabled = enabled) }
    }
    
    fun updateMealPrepRemindersEnabled(enabled: Boolean) {
        updatePreferences { it.copy(mealPrepRemindersEnabled = enabled) }
    }
    
    fun updateQuietHours(startTime: LocalTime?, endTime: LocalTime?) {
        updatePreferences { 
            it.copy(
                quietHoursStart = startTime?.toString(),
                quietHoursEnd = endTime?.toString()
            )
        }
    }
    
    fun updateSnoozeMinutes(minutes: Int) {
        updatePreferences { it.copy(snoozeMinutes = minutes) }
    }
    
    fun setupAllNotifications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val currentUserId = getCurrentUserId()
                notificationUseCase.setupAllNotifications(currentUserId)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        message = "All notifications have been set up successfully"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Failed to setup notifications: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }
    
    fun cancelAllNotifications() {
        viewModelScope.launch {
            try {
                val currentUserId = getCurrentUserId()
                notificationUseCase.cancelAllNotifications(currentUserId)
                _uiState.update { 
                    it.copy(message = "All notifications have been cancelled")
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Failed to cancel notifications: ${e.message}")
                }
            }
        }
    }
    
    fun snoozeNotification(notificationId: String, minutes: Int = 15) {
        viewModelScope.launch {
            try {
                notificationUseCase.snoozeNotification(notificationId, minutes)
                _uiState.update { 
                    it.copy(message = "Notification snoozed for $minutes minutes")
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Failed to snooze notification: ${e.message}")
                }
            }
        }
    }
    
    fun markNotificationComplete(notificationId: String, itemId: String) {
        viewModelScope.launch {
            try {
                notificationUseCase.markNotificationComplete(notificationId, itemId)
                _uiState.update { 
                    it.copy(message = "Notification marked as complete")
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Failed to mark notification complete: ${e.message}")
                }
            }
        }
    }
    
    fun dismissNotification(notificationId: String) {
        viewModelScope.launch {
            try {
                notificationUseCase.dismissNotification(notificationId)
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Failed to dismiss notification: ${e.message}")
                }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
    
    private fun updatePreferences(update: (NotificationPreferences) -> NotificationPreferences) {
        viewModelScope.launch {
            try {
                val currentPreferences = _uiState.value.preferences
                val updatedPreferences = update(currentPreferences)
                
                notificationUseCase.updateNotificationPreferences(updatedPreferences)
                _uiState.update { it.copy(preferences = updatedPreferences) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Failed to update preferences: ${e.message}")
                }
            }
        }
    }
    
    private fun setupMealReminders() {
        viewModelScope.launch {
            try {
                val currentUserId = getCurrentUserId()
                notificationUseCase.setupDailyMealReminders(currentUserId)
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Failed to setup meal reminders: ${e.message}")
                }
            }
        }
    }
    
    private fun setupSupplementReminders() {
        viewModelScope.launch {
            try {
                val currentUserId = getCurrentUserId()
                notificationUseCase.setupSupplementReminders(currentUserId)
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Failed to setup supplement reminders: ${e.message}")
                }
            }
        }
    }
    
    private fun setupPantryAlerts() {
        viewModelScope.launch {
            try {
                val currentUserId = getCurrentUserId()
                notificationUseCase.setupPantryExpiryAlerts(currentUserId)
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Failed to setup pantry alerts: ${e.message}")
                }
            }
        }
    }
    
    private fun setupMotivationalNotifications() {
        viewModelScope.launch {
            try {
                val currentUserId = getCurrentUserId()
                notificationUseCase.scheduleMotivationalNotifications(currentUserId)
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Failed to setup motivational notifications: ${e.message}")
                }
            }
        }
    }
}

data class NotificationUiState(
    val preferences: NotificationPreferences = NotificationPreferences(""),
    val notifications: List<NotificationEntity> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val message: String? = null
)