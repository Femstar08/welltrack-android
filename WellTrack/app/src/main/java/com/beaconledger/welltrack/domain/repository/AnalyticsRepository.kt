package com.beaconledger.welltrack.domain.repository

import com.beaconledger.welltrack.data.model.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface AnalyticsRepository {
    
    // Today's summary
    fun getTodaysSummary(userId: String, date: LocalDate): Flow<TodaysSummary>
    
    // Nutrition trends
    fun getNutritionTrends(userId: String, startDate: LocalDate, endDate: LocalDate): Flow<List<NutritionTrend>>
    fun getNutritionTrendsForWeek(userId: String, weekStartDate: LocalDate): Flow<List<NutritionTrend>>
    fun getNutritionTrendsForMonth(userId: String, month: Int, year: Int): Flow<List<NutritionTrend>>
    
    // Fitness statistics
    fun getFitnessStats(userId: String, startDate: LocalDate, endDate: LocalDate): Flow<List<FitnessStats>>
    fun getFitnessStatsForWeek(userId: String, weekStartDate: LocalDate): Flow<List<FitnessStats>>
    fun getFitnessStatsForMonth(userId: String, month: Int, year: Int): Flow<List<FitnessStats>>
    
    // Supplement adherence
    fun getSupplementAdherence(userId: String, startDate: LocalDate, endDate: LocalDate): Flow<List<SupplementAdherence>>
    fun getSupplementAdherenceRate(userId: String, startDate: LocalDate, endDate: LocalDate): Flow<Float>
    
    // Weekly and monthly analytics
    fun getWeeklyAnalytics(userId: String, weekStartDate: LocalDate): Flow<WeeklyAnalytics>
    fun getMonthlyAnalytics(userId: String, month: Int, year: Int): Flow<MonthlyAnalytics>
    
    // Calendar view data
    fun getCalendarActivities(userId: String, startDate: LocalDate, endDate: LocalDate): Flow<List<CalendarActivity>>
    
    // Chart data
    fun getChartData(userId: String, metric: String, startDate: LocalDate, endDate: LocalDate): Flow<List<ChartDataPoint>>
    
    // Trend analysis
    fun getTrendAnalysis(userId: String, metric: String, days: Int): Flow<TrendAnalysis>
    fun getCorrelationInsights(userId: String, days: Int): Flow<List<CorrelationInsight>>
    
    // Achievements
    fun getAchievements(userId: String): Flow<List<Achievement>>
    fun checkForNewAchievements(userId: String, date: LocalDate): Flow<List<Achievement>>
    
    // Improvement areas
    fun getImprovementAreas(userId: String): Flow<List<ImprovementArea>>
    
    // Data aggregation
    suspend fun refreshAnalyticsData(userId: String): Result<Unit>
    suspend fun calculateDailyMetrics(userId: String, date: LocalDate): Result<Unit>
}
