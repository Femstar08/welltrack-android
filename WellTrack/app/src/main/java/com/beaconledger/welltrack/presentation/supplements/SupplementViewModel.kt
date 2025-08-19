package com.beaconledger.welltrack.presentation.supplements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.data.database.dao.*
import com.beaconledger.welltrack.domain.usecase.SupplementUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class SupplementViewModel @Inject constructor(
    private val supplementUseCase: SupplementUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SupplementUiState())
    val uiState: StateFlow<SupplementUiState> = _uiState.asStateFlow()
    
    private val _selectedUserId = MutableStateFlow<String?>(null)
    
    init {
        // Initialize with default user - in real app this would come from auth
        _selectedUserId.value = "default-user-id"
        loadSupplementData()
    }
    
    fun setUserId(userId: String) {
        _selectedUserId.value = userId
        loadSupplementData()
    }
    
    private fun loadSupplementData() {
        val userId = _selectedUserId.value ?: return
        
        viewModelScope.launch {
            // Load dashboard data
            supplementUseCase.getSupplementDashboardData(userId)
                .catch { error ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = error.message
                    )}
                }
                .collect { dashboardData ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            userSupplements = dashboardData.userSupplements,
                            todayIntakes = dashboardData.todayIntakes,
                            summary = dashboardData.summary,
                            upcomingReminders = dashboardData.upcomingReminders,
                            missedSupplements = dashboardData.missedSupplements,
                            error = null
                        )
                    }
                }
        }
        
        // Load supplement library
        viewModelScope.launch {
            supplementUseCase.getAllSupplements()
                .catch { error ->
                    _uiState.update { it.copy(error = error.message) }
                }
                .collect { supplements ->
                    _uiState.update { it.copy(
                        availableSupplements = supplements
                    )}
                }
        }
    }
    
    // Supplement Library Actions
    fun searchSupplements(query: String) {
        if (query.isBlank()) {
            loadAllSupplements()
            return
        }
        
        viewModelScope.launch {
            supplementUseCase.searchSupplements(query)
                .catch { error ->
                    _uiState.update { it.copy(error = error.message) }
                }
                .collect { supplements ->
                    _uiState.update { it.copy(
                        availableSupplements = supplements,
                        searchQuery = query
                    )}
                }
        }
    }
    
    fun filterByCategory(category: SupplementCategory?) {
        viewModelScope.launch {
            val flow = if (category != null) {
                supplementUseCase.getSupplementsByCategory(category)
            } else {
                supplementUseCase.getAllSupplements()
            }
            
            flow.catch { error ->
                _uiState.update { it.copy(error = error.message) }
            }.collect { supplements ->
                _uiState.update { it.copy(
                    availableSupplements = supplements,
                    selectedCategory = category
                )}
            }
        }
    }
    
    private fun loadAllSupplements() {
        viewModelScope.launch {
            supplementUseCase.getAllSupplements()
                .catch { error ->
                    _uiState.update { it.copy(error = error.message) }
                }
                .collect { supplements ->
                    _uiState.update { it.copy(
                        availableSupplements = supplements,
                        searchQuery = ""
                    )}
                }
        }
    }
    
    fun createSupplement(
        name: String,
        brand: String?,
        description: String?,
        servingSize: String,
        servingUnit: String,
        category: SupplementCategory,
        nutrition: SupplementNutrition
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            supplementUseCase.createSupplement(
                name, brand, description, servingSize, servingUnit, category, nutrition
            ).fold(
                onSuccess = {
                    _uiState.update { it.copy(
                        isLoading = false,
                        showCreateDialog = false
                    )}
                    loadAllSupplements()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = error.message
                    )}
                }
            )
        }
    }
    
    fun scanSupplementBarcode(barcode: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            supplementUseCase.importSupplementFromBarcode(barcode).fold(
                onSuccess = { supplement ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        scannedSupplement = supplement
                    )}
                },
                onFailure = { error ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = error.message
                    )}
                }
            )
        }
    }
    
    // User Supplement Management
    fun addSupplementToUser(
        supplementId: String,
        customName: String?,
        dosage: Double,
        dosageUnit: String,
        frequency: SupplementFrequency,
        scheduledTimes: List<SupplementSchedule>,
        notes: String?
    ) {
        val userId = _selectedUserId.value ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            supplementUseCase.addSupplementToUser(
                userId, supplementId, customName, dosage, dosageUnit, frequency, scheduledTimes, notes
            ).fold(
                onSuccess = {
                    _uiState.update { it.copy(
                        isLoading = false,
                        showAddDialog = false
                    )}
                    loadSupplementData()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = error.message
                    )}
                }
            )
        }
    }
    
    fun updateUserSupplement(
        userSupplementId: String,
        customName: String?,
        dosage: Double,
        dosageUnit: String,
        frequency: SupplementFrequency,
        scheduledTimes: List<SupplementSchedule>,
        notes: String?
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            supplementUseCase.updateUserSupplement(
                userSupplementId, customName, dosage, dosageUnit, frequency, scheduledTimes, notes
            ).fold(
                onSuccess = {
                    _uiState.update { it.copy(
                        isLoading = false,
                        showEditDialog = false,
                        selectedUserSupplement = null
                    )}
                    loadSupplementData()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = error.message
                    )}
                }
            )
        }
    }
    
    fun removeSupplementFromUser(userSupplementId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            supplementUseCase.removeSupplementFromUser(userSupplementId).fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false) }
                    loadSupplementData()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = error.message
                    )}
                }
            )
        }
    }
    
    // Intake Tracking
    fun logSupplementIntake(
        userSupplementId: String,
        actualDosage: Double,
        dosageUnit: String,
        notes: String?
    ) {
        val userId = _selectedUserId.value ?: return
        
        viewModelScope.launch {
            supplementUseCase.logSupplementIntake(
                userId, userSupplementId, actualDosage, dosageUnit, notes
            ).fold(
                onSuccess = {
                    loadSupplementData()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(error = error.message) }
                }
            )
        }
    }
    
    fun markIntakeAsCompleted(intakeId: String, actualDosage: Double, notes: String?) {
        viewModelScope.launch {
            supplementUseCase.markScheduledIntakeAsCompleted(intakeId, actualDosage, notes).fold(
                onSuccess = {
                    loadSupplementData()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(error = error.message) }
                }
            )
        }
    }
    
    fun markIntakeAsSkipped(intakeId: String, notes: String?) {
        viewModelScope.launch {
            supplementUseCase.markScheduledIntakeAsSkipped(intakeId, notes).fold(
                onSuccess = {
                    loadSupplementData()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(error = error.message) }
                }
            )
        }
    }
    
    fun generateTodayScheduledIntakes() {
        val userId = _selectedUserId.value ?: return
        
        viewModelScope.launch {
            supplementUseCase.generateTodayScheduledIntakes(userId).fold(
                onSuccess = {
                    loadSupplementData()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(error = error.message) }
                }
            )
        }
    }
    
    // UI State Management
    fun showCreateSupplementDialog() {
        _uiState.update { it.copy(showCreateDialog = true) }
    }
    
    fun hideCreateSupplementDialog() {
        _uiState.update { it.copy(showCreateDialog = false) }
    }
    
    fun showAddSupplementDialog(supplement: Supplement? = null) {
        _uiState.update { it.copy(
            showAddDialog = true,
            selectedSupplement = supplement
        )}
    }
    
    fun hideAddSupplementDialog() {
        _uiState.update { it.copy(
            showAddDialog = false,
            selectedSupplement = null
        )}
    }
    
    fun showEditSupplementDialog(userSupplement: UserSupplementWithDetails) {
        _uiState.update { it.copy(
            showEditDialog = true,
            selectedUserSupplement = userSupplement
        )}
    }
    
    fun hideEditSupplementDialog() {
        _uiState.update { it.copy(
            showEditDialog = false,
            selectedUserSupplement = null
        )}
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun setSelectedTab(tab: SupplementTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }
}

data class SupplementUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedTab: SupplementTab = SupplementTab.MY_SUPPLEMENTS,
    
    // Supplement Library
    val availableSupplements: List<Supplement> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: SupplementCategory? = null,
    val scannedSupplement: Supplement? = null,
    
    // User Supplements
    val userSupplements: List<UserSupplementWithDetails> = emptyList(),
    val todayIntakes: List<SupplementIntakeWithDetails> = emptyList(),
    val summary: SupplementDailySummary = SupplementDailySummary("", 0, 0, 0, 0f),
    val upcomingReminders: List<SupplementReminder> = emptyList(),
    val missedSupplements: List<UserSupplementWithDetails> = emptyList(),
    
    // Dialog States
    val showCreateDialog: Boolean = false,
    val showAddDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val selectedSupplement: Supplement? = null,
    val selectedUserSupplement: UserSupplementWithDetails? = null
)

enum class SupplementTab {
    MY_SUPPLEMENTS,
    LIBRARY,
    ANALYTICS
}