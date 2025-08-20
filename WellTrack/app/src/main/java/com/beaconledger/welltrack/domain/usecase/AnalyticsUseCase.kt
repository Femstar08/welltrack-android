package com.beaconledger.welltrack.domain.usecase

import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.repository.AnalyticsRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsUseCase @Inject constructor(
    private val analyticsRepository: AnalyticsRepository
) {

    fun getTodaysSummary(userId: String, date: LocalDate = LocalDate.now()): Flow<TodaysSummary> {
        return analyticsRepository.getTodaysSummary(userId, date)
    }

    fun getNutritionTrends(userId: String, startDate: LocalDate, endDate: LocalDate): Flow<List<NutritionTrend>> {
        return analyticsRepository.getNutritionTrends(userId, startDate, endDate)
    }

    fun getNutritionTrendsForWeek(userId: String, weekStartDate: LocalDate): Flow<List<NutritionTrend>> {
        return analyticsRepository.getNutritionTrendsForWeek(userId, weekStartDate)
    }

    fun getNutritionTrendsForMonth(userId: String, month: Int, year: Int): Flow<List<NutritionTrend>> {
        return analyticsRepository.getNutritionTrendsForMonth(userId, month, year)
    }

    fun getFitnessStats(userId: String, startDate: LocalDate, endDate: LocalDate): Flow<List<FitnessStats>> {
        return analyticsRepository.getFitnessStats(userId, startDate, endDate)
    }

    fun getFitnessStatsForWeek(userId: String, weekStartDate: LocalDate): Flow<List<FitnessStats>> {
        return analyticsRepository.getFitnessStatsForWeek(userId, weekStartDate)
    }

    fun getFitnessStatsForMonth(userId: String, month: Int, year: Int): Flow<List<FitnessStats>> {
        return analyticsRepository.getFitnessStatsForMonth(userId, month, year)
    }

    fun getSupplementAdherence(userId: String, startDate: LocalDate, endDate: LocalDate): Flow<List<SupplementAdherence>> {
        return analyticsRepository.getSupplementAdherence(userId, startDate, endDate)
    }

    fun getSupplementAdherenceRate(userId: String, startDate: LocalDate, endDate: LocalDate): Flow<Float> {
        return analyticsRepository.getSupplementAdherenceRate(userId, startDate, endDate)
    }

    fun getWeeklyAnalytics(userId: String, weekStartDate: LocalDate): Flow<WeeklyAnalytics> {
        return analyticsRepository.getWeeklyAnalytics(userId, weekStartDate)
    }

    fun getMonthlyAnalytics(userId: String, month: Int, year: Int): Flow<MonthlyAnalytics> {
        return analyticsRepository.getMonthlyAnalytics(userId, month, year)
    }

    fun getCalendarActivities(userId: String, startDate: LocalDate, endDate: LocalDate): Flow<List<CalendarActivity>> {
        return analyticsRepository.getCalendarActivities(userId, startDate, endDate)
    }

    fun getChartData(userId: String, metric: String, startDate: LocalDate, endDate: LocalDate): Flow<List<ChartDataPoint>> {
        return analyticsRepository.getChartData(userId, metric, startDate, endDate)
    }

    fun getTrendAnalysis(userId: String, metric: String, days: Int = 30): Flow<TrendAnalysis> {
        return analyticsRepository.getTrendAnalysis(userId, metric, days)
    }

    fun getCorrelationInsights(userId: String, days: Int = 30): Flow<List<CorrelationInsight>> {
        return analyticsRepository.getCorrelationInsights(userId, days)
    }

    fun getAchievements(userId: String): Flow<List<Achievement>> {
        return analyticsRepository.getAchievements(userId)
    }

    fun checkForNewAchievements(userId: String, date: LocalDate = LocalDate.now()): Flow<List<Achievement>> {
        return analyticsRepository.checkForNewAchievements(userId, date)
    }

    fun getImprovementAreas(userId: String): Flow<List<ImprovementArea>> {
        return analyticsRepository.getImprovementAreas(userId)
    }

    suspend fun refreshAnalyticsData(userId: String): Result<Unit> {
        return analyticsRepository.refreshAnalyticsData(userId)
    }

    suspend fun calculateDailyMetrics(userId: String, date: LocalDate = LocalDate.now()): Result<Unit> {
        return analyticsRepository.calculateDailyMetrics(userId, date)
    }

    // Convenience methods for common date ranges
    fun getLastWeekNutritionTrends(userId: String): Flow<List<NutritionTrend>> {
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(6)
        return getNutritionTrends(userId, startDate, endDate)
    }

    fun getLastMonthFitnessStats(userId: String): Flow<List<FitnessStats>> {
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(29)
        return getFitnessStats(userId, startDate, endDate)
    }

    fun getCurrentWeekSupplementAdherence(userId: String): Flow<Float> {
        val today = LocalDate.now()
        val startOfWeek = today.minusDays(today.dayOfWeek.value.toLong() - 1)
        return getSupplementAdherenceRate(userId, startOfWeek, today)
    }

    fun getCurrentMonthCalendarActivities(userId: String): Flow<List<CalendarActivity>> {
        val today = LocalDate.now()
        val startOfMonth = today.withDayOfMonth(1)
        return getCalendarActivities(userId, startOfMonth, today)
    }

    // Chart data helpers
    fun getCaloriesChartData(userId: String, days: Int = 30): Flow<List<ChartDataPoint>> {
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(days.toLong())
        return getChartData(userId, "calories", startDate, endDate)
    }

    fun getStepsChartData(userId: String, days: Int = 30): Flow<List<ChartDataPoint>> {
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(days.toLong())
        return getChartData(userId, "steps", startDate, endDate)
    }

    fun getWaterIntakeChartData(userId: String, days: Int = 30): Flow<List<ChartDataPoint>> {
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(days.toLong())
        return getChartData(userId, "water", startDate, endDate)
    }

    fun getWeightChartData(userId: String, days: Int = 30): Flow<List<ChartDataPoint>> {
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(days.toLong())
        return getChartData(userId, "weight", startDate, endDate)
    }

    // Analysis helpers
    fun getCaloriesTrend(userId: String, days: Int = 30): Flow<TrendAnalysis> {
        return getTrendAnalysis(userId, "calories", days)
    }

    fun getStepsTrend(userId: String, days: Int = 30): Flow<TrendAnalysis> {
        return getTrendAnalysis(userId, "steps", days)
    }

    fun getWaterIntakeTrend(userId: String, days: Int = 30): Flow<TrendAnalysis> {
        return getTrendAnalysis(userId, "water", days)
    }

    fun getWeightTrend(userId: String, days: Int = 30): Flow<TrendAnalysis> {
        return getTrendAnalysis(userId, "weight", days)
    }
}