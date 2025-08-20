package com.beaconledger.welltrack.presentation.dailytracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.usecase.DailyTrackingUseCase
import com.beaconledger.welltrack.domain.usecase.MacronutrientProgress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class DailyTrackingViewModel @Inject constructor(
    private val dailyTrackingUseCase: DailyTrackingUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DailyTrackingUiState())
    val uiState: StateFlow<DailyTrackingUiState> = _uiState.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _currentUserId = MutableStateFlow("") // Should be injected from auth
    
    init {
        observeDailyTrackingSummary()
    }

    fun setUserId(userId: String) {
        _currentUserId.value = userId
        observeDailyTrackingSummary()
    }

    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date
        observeDailyTrackingSummary()
    }

    private fun observeDailyTrackingSummary() {
        viewModelScope.launch {
            combine(
                _currentUserId,
                _selectedDate
            ) { userId, date ->
                if (userId.isNotEmpty()) {
                    dailyTrackingUseCase.getDailyTrackingSummary(userId, date)
                } else {
                    flowOf(DailyTrackingSummary(userId, date))
                }
            }.flatMapLatest { it }.collect { summary ->
                _uiState.value = _uiState.value.copy(
                    dailySummary = summary,
                    isLoading = false
                )
            }
        }
    }

    fun saveMorningTracking(data: MorningTrackingData) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = dailyTrackingUseCase.saveMorningTracking(
                _currentUserId.value,
                _selectedDate.value,
                data
            )
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = if (result.isFailure) result.exceptionOrNull()?.message else null
            )
        }
    }

    fun savePreWorkoutTracking(data: PreWorkoutTrackingData) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = dailyTrackingUseCase.savePreWorkoutTracking(
                _currentUserId.value,
                _selectedDate.value,
                data
            )
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = if (result.isFailure) result.exceptionOrNull()?.message else null
            )
        }
    }

    fun savePostWorkoutTracking(data: PostWorkoutTrackingData) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = dailyTrackingUseCase.savePostWorkoutTracking(
                _currentUserId.value,
                _selectedDate.value,
                data
            )
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = if (result.isFailure) result.exceptionOrNull()?.message else null
            )
        }
    }

    fun saveBedtimeTracking(data: BedtimeTrackingData) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = dailyTrackingUseCase.saveBedtimeTracking(
                _currentUserId.value,
                _selectedDate.value,
                data
            )
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = if (result.isFailure) result.exceptionOrNull()?.message else null
            )
        }
    }

    fun addWaterIntake(amountMl: Int, source: String = "Water") {
        viewModelScope.launch {
            val result = dailyTrackingUseCase.addWaterIntake(
                _currentUserId.value,
                _selectedDate.value,
                amountMl,
                source
            )
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    error = result.exceptionOrNull()?.message
                )
            }
        }
    }

    fun loadMorningTracking() {
        viewModelScope.launch {
            val data = dailyTrackingUseCase.getMorningTracking(_currentUserId.value, _selectedDate.value)
            _uiState.value = _uiState.value.copy(morningTrackingData = data)
        }
    }

    fun loadPreWorkoutTracking() {
        viewModelScope.launch {
            val data = dailyTrackingUseCase.getPreWorkoutTracking(_currentUserId.value, _selectedDate.value)
            _uiState.value = _uiState.value.copy(preWorkoutTrackingData = data)
        }
    }

    fun loadPostWorkoutTracking() {
        viewModelScope.launch {
            val data = dailyTrackingUseCase.getPostWorkoutTracking(_currentUserId.value, _selectedDate.value)
            _uiState.value = _uiState.value.copy(postWorkoutTrackingData = data)
        }
    }

    fun loadBedtimeTracking() {
        viewModelScope.launch {
            val data = dailyTrackingUseCase.getBedtimeTracking(_currentUserId.value, _selectedDate.value)
            _uiState.value = _uiState.value.copy(bedtimeTrackingData = data)
        }
    }

    fun loadWaterIntake() {
        viewModelScope.launch {
            val data = dailyTrackingUseCase.getWaterIntake(_currentUserId.value, _selectedDate.value)
            _uiState.value = _uiState.value.copy(waterIntakeData = data)
        }
    }

    fun loadMacronutrientProgress() {
        viewModelScope.launch {
            val progress = dailyTrackingUseCase.calculateMacronutrientProgress(_currentUserId.value, _selectedDate.value)
            _uiState.value = _uiState.value.copy(macronutrientProgress = progress)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun refreshData() {
        loadMorningTracking()
        loadPreWorkoutTracking()
        loadPostWorkoutTracking()
        loadBedtimeTracking()
        loadWaterIntake()
        loadMacronutrientProgress()
    }
}

data class DailyTrackingUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val dailySummary: DailyTrackingSummary? = null,
    val morningTrackingData: MorningTrackingData? = null,
    val preWorkoutTrackingData: PreWorkoutTrackingData? = null,
    val postWorkoutTrackingData: PostWorkoutTrackingData? = null,
    val bedtimeTrackingData: BedtimeTrackingData? = null,
    val waterIntakeData: WaterIntakeData? = null,
    val macronutrientProgress: MacronutrientProgress? = null
)