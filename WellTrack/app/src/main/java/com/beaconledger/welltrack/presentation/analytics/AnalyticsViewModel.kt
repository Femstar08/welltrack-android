package com.beaconledger.welltrack.presentation.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.usecase.AnalyticsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val analyticsUseCase: AnalyticsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    private val _currentUserId = MutableStateFlow("")
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _selectedTimeRange = MutableStateFlow(TimeRange.WEEK)

    init {
        observeAnalyticsData()
    }

    fun setUserId(userId: String) {
        _currentUserId.value = userId
        refreshData()
    }

    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date
        refreshData()
    }

    fun setTimeRange(timeRange: TimeRange) {
        _selectedTimeRange.value = timeRange
        refreshData()
    }

    private fun observeAnalyticsData() {
        viewModelScope.launch {
            combine(
                _currentUserId,
                _selectedDate,
                _selectedTimeRange
            ) { userId, date, timeRange ->
                if (userId.isNotEmpty()) {
                    loadAnalyticsData(userId, date, timeRange)
                }
            }.collect()
        }
    }

    private fun loadAnalyticsData(userId: String, date: LocalDate, timeRange: TimeRange) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // Load today's summary
                analyticsUseCase.getTodaysSummary(userId, date).collect { summary ->
                    _uiState.value = _uiState.value.copy(todaysSummary = summary)
                }

                // Load nutrition trends based on time range
                val nutritionTrends = when (timeRange) {
                    TimeRange.WEEK -> analyticsUseCase.getLastWeekNutritionTrends(userId)
                    TimeRange.MONTH -> analyticsUseCase.getNutritionTrendsForMonth(userId, date.monthValue, date.year)
                    TimeRange.THREE_MONTHS -> {
                        val startDate = date.minusMonths(3)
                        analyticsUseCase.getNutritionTrends(userId, startDate, date)
                    }
                }

                nutritionTrends.collect { trends ->
                    _uiState.value = _uiState.value.copy(nutritionTrends = trends)
                }

                // Load fitness stats
                val fitnessStats = when (timeRange) {
                    TimeRange.WEEK -> analyticsUseCase.getFitnessStatsForWeek(userId, date.minusDays(6))
                    TimeRange.MONTH -> analyticsUseCase.getFitnessStatsForMonth(userId, date.monthValue, date.year)
                    TimeRange.THREE_MONTHS -> {
                        val startDate = date.minusMonths(3)
                        analyticsUseCase.getFitnessStats(userId, startDate, date)
                    }
                }

                fitnessStats.collect { stats ->
                    _uiState.value = _uiState.value.copy(fitnessStats = stats)
                }

                // Load supplement adherence
                analyticsUseCase.getCurrentWeekSupplementAdherence(userId).collect { adherence ->
                    _uiState.value = _uiState.value.copy(supplementAdherenceRate = adherence)
                }

                // Load calendar activities
                analyticsUseCase.getCurrentMonthCalendarActivities(userId).collect { activities ->
                    _uiState.value = _uiState.value.copy(calendarActivities = activities)
                }

                _uiState.value = _uiState.value.copy(isLoading = false)

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun loadChartData(metric: String, days: Int = 30) {
        viewModelScope.launch {
            val userId = _currentUserId.value
            if (userId.isEmpty()) return@launch

            try {
                val chartData = when (metric) {
                    "calories" -> analyticsUseCase.getCaloriesChartData(userId, days)
                    "steps" -> analyticsUseCase.getStepsChartData(userId, days)
                    "water" -> analyticsUseCase.getWaterIntakeChartData(userId, days)
                    "weight" -> analyticsUseCase.getWeightChartData(userId, days)
                    else -> flowOf(emptyList())
                }

                chartData.collect { data ->
                    _uiState.value = _uiState.value.copy(
                        chartData = _uiState.value.chartData + (metric to data)
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun loadTrendAnalysis(metric: String, days: Int = 30) {
        viewModelScope.launch {
            val userId = _currentUserId.value
            if (userId.isEmpty()) return@launch

            try {
                val trendAnalysis = when (metric) {
                    "calories" -> analyticsUseCase.getCaloriesTrend(userId, days)
                    "steps" -> analyticsUseCase.getStepsTrend(userId, days)
                    "water" -> analyticsUseCase.getWaterIntakeTrend(userId, days)
                    "weight" -> analyticsUseCase.getWeightTrend(userId, days)
                    else -> flowOf(TrendAnalysis(metric, TrendDirection.STABLE, 0.0, "No data", null))
                }

                trendAnalysis.collect { analysis ->
                    _uiState.value = _uiState.value.copy(
                        trendAnalyses = _uiState.value.trendAnalyses + (metric to analysis)
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun loadCorrelationInsights(days: Int = 30) {
        viewModelScope.launch {
            val userId = _currentUserId.value
            if (userId.isEmpty()) return@launch

            try {
                analyticsUseCase.getCorrelationInsights(userId, days).collect { insights ->
                    _uiState.value = _uiState.value.copy(correlationInsights = insights)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun loadAchievements() {
        viewModelScope.launch {
            val userId = _currentUserId.value
            if (userId.isEmpty()) return@launch

            try {
                analyticsUseCase.getAchievements(userId).collect { achievements ->
                    _uiState.value = _uiState.value.copy(achievements = achievements)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun loadImprovementAreas() {
        viewModelScope.launch {
            val userId = _currentUserId.value
            if (userId.isEmpty()) return@launch

            try {
                analyticsUseCase.getImprovementAreas(userId).collect { areas ->
                    _uiState.value = _uiState.value.copy(improvementAreas = areas)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun refreshData() {
        val userId = _currentUserId.value
        val date = _selectedDate.value
        val timeRange = _selectedTimeRange.value
        
        if (userId.isNotEmpty()) {
            loadAnalyticsData(userId, date, timeRange)
            loadCorrelationInsights()
            loadAchievements()
            loadImprovementAreas()
        }
    }

    fun refreshAnalyticsData() {
        viewModelScope.launch {
            val userId = _currentUserId.value
            if (userId.isEmpty()) return@launch

            _uiState.value = _uiState.value.copy(isRefreshing = true)
            
            try {
                val result = analyticsUseCase.refreshAnalyticsData(userId)
                if (result.isSuccess) {
                    refreshData()
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = result.exceptionOrNull()?.message ?: "Failed to refresh data"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            } finally {
                _uiState.value = _uiState.value.copy(isRefreshing = false)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class AnalyticsUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val todaysSummary: TodaysSummary? = null,
    val nutritionTrends: List<NutritionTrend> = emptyList(),
    val fitnessStats: List<FitnessStats> = emptyList(),
    val supplementAdherenceRate: Float = 0f,
    val calendarActivities: List<CalendarActivity> = emptyList(),
    val chartData: Map<String, List<ChartDataPoint>> = emptyMap(),
    val trendAnalyses: Map<String, TrendAnalysis> = emptyMap(),
    val correlationInsights: List<CorrelationInsight> = emptyList(),
    val achievements: List<Achievement> = emptyList(),
    val improvementAreas: List<ImprovementArea> = emptyList()
)

enum class TimeRange {
    WEEK,
    MONTH,
    THREE_MONTHS
}