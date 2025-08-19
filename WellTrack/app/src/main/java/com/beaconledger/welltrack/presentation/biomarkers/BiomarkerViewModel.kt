package com.beaconledger.welltrack.presentation.biomarkers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.usecase.BiomarkerUseCase
import com.beaconledger.welltrack.domain.usecase.BiomarkerEntryInput
import com.beaconledger.welltrack.domain.usecase.ValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class BiomarkerViewModel @Inject constructor(
    private val biomarkerUseCase: BiomarkerUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(BiomarkerUiState())
    val uiState: StateFlow<BiomarkerUiState> = _uiState.asStateFlow()
    
    private val _currentUserId = MutableStateFlow("") // This should come from user session
    
    init {
        // Initialize with mock user ID - in real app this would come from authentication
        _currentUserId.value = "user123"
        loadData()
    }
    
    fun setUserId(userId: String) {
        _currentUserId.value = userId
        loadData()
    }
    
    private fun loadData() {
        val userId = _currentUserId.value
        if (userId.isNotEmpty()) {
            loadReminders(userId)
            loadBiomarkerEntries(userId)
            loadTestSessions(userId)
        }
    }
    
    private fun loadReminders(userId: String) {
        viewModelScope.launch {
            biomarkerUseCase.getActiveReminders(userId).collect { reminders ->
                _uiState.update { it.copy(reminders = reminders) }
            }
        }
        
        viewModelScope.launch {
            try {
                val overdueReminders = biomarkerUseCase.getOverdueReminders(userId)
                _uiState.update { it.copy(overdueReminders = overdueReminders) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
    
    private fun loadBiomarkerEntries(userId: String) {
        viewModelScope.launch {
            biomarkerUseCase.getAllBiomarkerEntries(userId).collect { entries ->
                _uiState.update { it.copy(biomarkerEntries = entries) }
            }
        }
    }
    
    private fun loadTestSessions(userId: String) {
        viewModelScope.launch {
            biomarkerUseCase.getTestSessions(userId).collect { sessions ->
                _uiState.update { it.copy(testSessions = sessions) }
            }
        }
    }
    
    // Reminder Management
    fun createReminder(
        testType: BloodTestType,
        reminderName: String,
        description: String? = null,
        frequency: ReminderFrequency,
        firstDueDate: LocalDate? = null
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val result = biomarkerUseCase.createBloodTestReminder(
                userId = _currentUserId.value,
                testType = testType,
                reminderName = reminderName,
                description = description,
                frequency = frequency,
                firstDueDate = firstDueDate
            )
            
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    error = if (result.isFailure) result.exceptionOrNull()?.message else null
                )
            }
        }
    }
    
    fun skipReminder(reminderId: String) {
        viewModelScope.launch {
            val result = biomarkerUseCase.skipReminder(reminderId)
            if (result.isFailure) {
                _uiState.update { it.copy(error = result.exceptionOrNull()?.message) }
            }
        }
    }
    
    fun markReminderCompleted(reminderId: String, completedDate: LocalDate? = null) {
        viewModelScope.launch {
            val result = biomarkerUseCase.markReminderCompleted(reminderId, completedDate)
            if (result.isFailure) {
                _uiState.update { it.copy(error = result.exceptionOrNull()?.message) }
            }
        }
    }
    
    fun deleteReminder(reminderId: String) {
        viewModelScope.launch {
            val result = biomarkerUseCase.deleteReminder(reminderId)
            if (result.isFailure) {
                _uiState.update { it.copy(error = result.exceptionOrNull()?.message) }
            }
        }
    }
    
    // Biomarker Entry Management
    fun saveBiomarkerEntry(
        testType: BloodTestType,
        biomarkerType: BiomarkerType,
        value: Double,
        unit: String,
        testDate: LocalDate,
        notes: String? = null,
        labName: String? = null,
        referenceRangeMin: Double? = null,
        referenceRangeMax: Double? = null
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val result = biomarkerUseCase.saveBiomarkerEntry(
                userId = _currentUserId.value,
                testType = testType,
                biomarkerType = biomarkerType,
                value = value,
                unit = unit,
                testDate = testDate,
                notes = notes,
                labName = labName,
                referenceRangeMin = referenceRangeMin,
                referenceRangeMax = referenceRangeMax
            )
            
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    error = if (result.isFailure) result.exceptionOrNull()?.message else null
                )
            }
        }
    }
    
    fun saveBulkBiomarkerEntries(
        testType: BloodTestType,
        testDate: LocalDate,
        entries: List<BiomarkerEntryInput>,
        labName: String? = null,
        sessionNotes: String? = null
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val result = biomarkerUseCase.saveBulkBiomarkerEntries(
                userId = _currentUserId.value,
                testType = testType,
                testDate = testDate,
                entries = entries,
                labName = labName,
                sessionNotes = sessionNotes
            )
            
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    error = if (result.isFailure) result.exceptionOrNull()?.message else null
                )
            }
        }
    }
    
    // UI State Management
    fun showCreateReminderDialog() {
        _uiState.update { it.copy(showCreateReminderDialog = true) }
    }
    
    fun hideCreateReminderDialog() {
        _uiState.update { it.copy(showCreateReminderDialog = false) }
    }
    
    fun showAddEntryDialog(testType: BloodTestType? = null) {
        _uiState.update { 
            it.copy(
                showAddEntryDialog = true,
                selectedTestType = testType
            )
        }
    }
    
    fun hideAddEntryDialog() {
        _uiState.update { 
            it.copy(
                showAddEntryDialog = false,
                selectedTestType = null
            )
        }
    }
    
    fun showBulkEntryDialog(testType: BloodTestType) {
        _uiState.update { 
            it.copy(
                showBulkEntryDialog = true,
                selectedTestType = testType
            )
        }
    }
    
    fun hideBulkEntryDialog() {
        _uiState.update { 
            it.copy(
                showBulkEntryDialog = false,
                selectedTestType = null
            )
        }
    }
    
    fun selectTab(tab: BiomarkerTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    // Helper functions
    fun getBiomarkerCategories(): List<BiomarkerCategory> {
        return biomarkerUseCase.getBiomarkerCategories()
    }
    
    fun getBiomarkersForTestType(testType: BloodTestType): List<BiomarkerType> {
        return biomarkerUseCase.getBiomarkersForTestType(testType)
    }
    
    fun getBiomarkerReference(biomarkerType: BiomarkerType): BiomarkerReference? {
        return biomarkerUseCase.getBiomarkerReference(biomarkerType)
    }
    
    fun validateBiomarkerValue(biomarkerType: BiomarkerType, value: Double, unit: String): ValidationResult {
        return biomarkerUseCase.validateBiomarkerValue(biomarkerType, value, unit)
    }
    
    fun getDefaultReminderFrequency(testType: BloodTestType): ReminderFrequency {
        return biomarkerUseCase.getDefaultReminderFrequency(testType)
    }
}

data class BiomarkerUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedTab: BiomarkerTab = BiomarkerTab.REMINDERS,
    
    // Reminders
    val reminders: List<BloodTestReminder> = emptyList(),
    val overdueReminders: List<BloodTestReminderWithStatus> = emptyList(),
    val showCreateReminderDialog: Boolean = false,
    
    // Biomarker Entries
    val biomarkerEntries: List<BiomarkerEntry> = emptyList(),
    val showAddEntryDialog: Boolean = false,
    val showBulkEntryDialog: Boolean = false,
    val selectedTestType: BloodTestType? = null,
    
    // Test Sessions
    val testSessions: List<BiomarkerTestSession> = emptyList()
)

enum class BiomarkerTab {
    REMINDERS,
    ENTRIES,
    TRENDS,
    INSIGHTS
}