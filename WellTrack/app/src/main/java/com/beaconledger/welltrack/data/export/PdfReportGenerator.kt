package com.beaconledger.welltrack.data.export

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.beaconledger.welltrack.data.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfReportGenerator @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val pageWidth = 595 // A4 width in points
    private val pageHeight = 842 // A4 height in points
    private val margin = 50
    private val contentWidth = pageWidth - (2 * margin)
    
    suspend fun generateHealthReport(
        healthReport: HealthReport,
        outputFile: File
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val document = PdfDocument()
            var currentPage = 1
            
            // Page 1: Summary and Overview
            val page1 = document.startPage(createPageInfo(currentPage++))
            drawReportHeader(page1.canvas, healthReport)
            drawHealthSummary(page1.canvas, healthReport.summary)
            document.finishPage(page1)
            
            // Page 2: Nutrition Analysis
            val page2 = document.startPage(createPageInfo(currentPage++))
            drawPageHeader(page2.canvas, "Nutrition Analysis", currentPage - 1)
            drawNutritionAnalysis(page2.canvas, healthReport.nutritionAnalysis)
            document.finishPage(page2)
            
            // Page 3: Fitness Metrics
            val page3 = document.startPage(createPageInfo(currentPage++))
            drawPageHeader(page3.canvas, "Fitness & Health Metrics", currentPage - 1)
            drawFitnessMetrics(page3.canvas, healthReport.fitnessMetrics)
            document.finishPage(page3)
            
            // Page 4: Supplement Adherence
            val page4 = document.startPage(createPageInfo(currentPage++))
            drawPageHeader(page4.canvas, "Supplement Adherence", currentPage - 1)
            drawSupplementAdherence(page4.canvas, healthReport.supplementAdherence)
            document.finishPage(page4)
            
            // Page 5: Biomarker Trends
            if (healthReport.biomarkerTrends.isNotEmpty()) {
                val page5 = document.startPage(createPageInfo(currentPage++))
                drawPageHeader(page5.canvas, "Biomarker Trends", currentPage - 1)
                drawBiomarkerTrends(page5.canvas, healthReport.biomarkerTrends)
                document.finishPage(page5)
            }
            
            // Page 6: Goal Progress
            if (healthReport.goalProgress.isNotEmpty()) {
                val page6 = document.startPage(createPageInfo(currentPage++))
                drawPageHeader(page6.canvas, "Goal Progress", currentPage - 1)
                drawGoalProgress(page6.canvas, healthReport.goalProgress)
                document.finishPage(page6)
            }
            
            // Page 7: Recommendations
            if (healthReport.recommendations.isNotEmpty()) {
                val page7 = document.startPage(createPageInfo(currentPage))
                drawPageHeader(page7.canvas, "Recommendations", currentPage)
                drawRecommendations(page7.canvas, healthReport.recommendations)
                document.finishPage(page7)
            }
            
            // Write to file
            FileOutputStream(outputFile).use { outputStream ->
                document.writeTo(outputStream)
            }
            document.close()
            
            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun createPageInfo(pageNumber: Int): PdfDocument.PageInfo {
        return PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
    }
    
    private fun drawReportHeader(canvas: Canvas, healthReport: HealthReport) {
        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 24f
            isFakeBoldText = true
        }

        val subtitlePaint = Paint().apply {
            color = Color.GRAY
            textSize = 16f
        }

        val bodyPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
        }

        val disclaimerPaint = Paint().apply {
            color = Color.GRAY
            textSize = 10f
        }

        var yPosition = margin + 40f

        // Title
        canvas.drawText("WellTrack Health Report", margin.toFloat(), yPosition, titlePaint)
        yPosition += 40

        // Report period
        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
        val periodText = "${healthReport.reportPeriod.startDate.format(formatter)} - ${healthReport.reportPeriod.endDate.format(formatter)}"
        canvas.drawText("Report Period: $periodText", margin.toFloat(), yPosition, subtitlePaint)
        yPosition += 30

        // Generated date
        val generatedText = "Generated: ${healthReport.generatedAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm"))}"
        canvas.drawText(generatedText, margin.toFloat(), yPosition, bodyPaint)
        yPosition += 30

        // Medical disclaimer
        canvas.drawText("DISCLAIMER: This report is for informational purposes only and should not replace", margin.toFloat(), yPosition, disclaimerPaint)
        yPosition += 15
        canvas.drawText("professional medical advice. Consult with healthcare providers for medical decisions.", margin.toFloat(), yPosition, disclaimerPaint)
        yPosition += 25

        // Garmin attribution (compliance requirement)
        canvas.drawText("Health data includes information from Garmin Connect™", margin.toFloat(), yPosition, disclaimerPaint)
        yPosition += 15
        canvas.drawText("Garmin® and Connect IQ™ are trademarks of Garmin Ltd. or its subsidiaries.", margin.toFloat(), yPosition, disclaimerPaint)
        yPosition += 25

        // Draw separator line
        canvas.drawLine(margin.toFloat(), yPosition, (pageWidth - margin).toFloat(), yPosition, bodyPaint)
    }
    
    private fun drawPageHeader(canvas: Canvas, title: String, pageNumber: Int) {
        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 20f
            isFakeBoldText = true
        }
        
        val pagePaint = Paint().apply {
            color = Color.GRAY
            textSize = 10f
        }
        
        // Title
        canvas.drawText(title, margin.toFloat(), margin + 30f, titlePaint)
        
        // Page number
        canvas.drawText("Page $pageNumber", (pageWidth - margin - 50).toFloat(), margin + 20f, pagePaint)
        
        // Separator line
        canvas.drawLine(margin.toFloat(), margin + 50f, (pageWidth - margin).toFloat(), margin + 50f, titlePaint)
    }
    
    private fun drawHealthSummary(canvas: Canvas, summary: HealthSummary) {
        val headerPaint = Paint().apply {
            color = Color.BLACK
            textSize = 16f
            isFakeBoldText = true
        }
        
        val bodyPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
        }
        
        var yPosition = 200f
        
        canvas.drawText("Health Summary", margin.toFloat(), yPosition, headerPaint)
        yPosition += 30
        
        val summaryItems = listOf(
            "Total Meals Logged: ${summary.totalMealsLogged}",
            "Average Meal Score: ${"%.1f".format(summary.averageMealScore)}",
            "Supplement Compliance: ${"%.1f".format(summary.supplementComplianceRate * 100)}%",
            "Active Goals: ${summary.activeGoals}",
            "Completed Goals: ${summary.completedGoals}",
            "Health Data Points: ${summary.healthConnectDataPoints}"
        )
        
        summaryItems.forEach { item ->
            canvas.drawText("• $item", margin + 20f, yPosition, bodyPaint)
            yPosition += 25
        }
    }
    
    private fun drawNutritionAnalysis(canvas: Canvas, nutrition: NutritionAnalysis) {
        val headerPaint = Paint().apply {
            color = Color.BLACK
            textSize = 16f
            isFakeBoldText = true
        }
        
        val bodyPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
        }
        
        var yPosition = 120f
        
        // Average Daily Calories
        canvas.drawText("Daily Nutrition Overview", margin.toFloat(), yPosition, headerPaint)
        yPosition += 30
        
        canvas.drawText("Average Daily Calories: ${"%.0f".format(nutrition.averageDailyCalories)} kcal", margin + 20f, yPosition, bodyPaint)
        yPosition += 25
        
        canvas.drawText("Average Daily Hydration: ${"%.1f".format(nutrition.hydrationAverage)} L", margin + 20f, yPosition, bodyPaint)
        yPosition += 40
        
        // Macronutrient Breakdown
        canvas.drawText("Macronutrient Breakdown", margin.toFloat(), yPosition, headerPaint)
        yPosition += 30
        
        nutrition.macronutrientBreakdown.forEach { (macro, value) ->
            canvas.drawText("• $macro: ${"%.1f".format(value)}g", margin + 20f, yPosition, bodyPaint)
            yPosition += 25
        }
        
        yPosition += 20
        
        // Micronutrient Status
        if (nutrition.micronutrientStatus.isNotEmpty()) {
            canvas.drawText("Micronutrient Status", margin.toFloat(), yPosition, headerPaint)
            yPosition += 30
            
            nutrition.micronutrientStatus.forEach { (nutrient, status) ->
                canvas.drawText("• $nutrient: $status", margin + 20f, yPosition, bodyPaint)
                yPosition += 25
            }
        }
    }
    
    private fun drawFitnessMetrics(canvas: Canvas, fitness: FitnessMetrics) {
        val headerPaint = Paint().apply {
            color = Color.BLACK
            textSize = 16f
            isFakeBoldText = true
        }
        
        val bodyPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
        }
        
        var yPosition = 120f
        
        canvas.drawText("Fitness & Activity Metrics", margin.toFloat(), yPosition, headerPaint)
        yPosition += 30
        
        val fitnessItems = mutableListOf<String>()
        fitnessItems.add("Average Daily Steps: ${fitness.averageSteps}")
        fitness.averageHeartRate?.let { fitnessItems.add("Average Heart Rate: $it bpm") }
        fitnessItems.add("Workout Frequency: ${fitness.workoutFrequency} per week")
        fitness.sleepQuality?.let { fitnessItems.add("Sleep Quality Score: ${"%.1f".format(it)}/10") }
        fitness.stressLevels?.let { fitnessItems.add("Average Stress Level: ${"%.1f".format(it)}/10") }
        
        fitnessItems.forEach { item ->
            canvas.drawText("• $item", margin + 20f, yPosition, bodyPaint)
            yPosition += 25
        }
    }
    
    private fun drawSupplementAdherence(canvas: Canvas, supplements: ExportSupplementAdherence) {
        val headerPaint = Paint().apply {
            color = Color.BLACK
            textSize = 16f
            isFakeBoldText = true
        }
        
        val bodyPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
        }
        
        var yPosition = 120f
        
        canvas.drawText("Supplement Adherence", margin.toFloat(), yPosition, headerPaint)
        yPosition += 30
        
        canvas.drawText("Total Supplements: ${supplements.totalSupplements}", margin + 20f, yPosition, bodyPaint)
        yPosition += 25

        canvas.drawText("Adherence Rate: ${"%.1f".format(supplements.adherenceRate * 100)}%", margin + 20f, yPosition, bodyPaint)
        yPosition += 25

        canvas.drawText("Missed Doses: ${supplements.missedDoses}", margin + 20f, yPosition, bodyPaint)
        yPosition += 40

        if (supplements.supplementEffectiveness.isNotEmpty()) {
            canvas.drawText("Supplement Effectiveness", margin.toFloat(), yPosition, headerPaint)
            yPosition += 30

            supplements.supplementEffectiveness.forEach { (supplement, effectiveness) ->
                canvas.drawText("• $supplement: $effectiveness", margin + 20f, yPosition, bodyPaint)
                yPosition += 25
            }
        }
    }
    
    private fun drawBiomarkerTrends(canvas: Canvas, biomarkers: List<ExportBiomarkerTrend>) {
        val headerPaint = Paint().apply {
            color = Color.BLACK
            textSize = 16f
            isFakeBoldText = true
        }
        
        val bodyPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
        }
        
        var yPosition = 120f
        
        canvas.drawText("Biomarker Trends", margin.toFloat(), yPosition, headerPaint)
        yPosition += 30
        
        biomarkers.forEach { biomarker ->
            canvas.drawText("${biomarker.biomarkerType}:", margin + 20f, yPosition, headerPaint)
            yPosition += 20

            canvas.drawText("  Current: ${biomarker.latestValue} (${biomarker.trend})", margin + 40f, yPosition, bodyPaint)
            yPosition += 20

            canvas.drawText("  Target Range: ${biomarker.targetRange}", margin + 40f, yPosition, bodyPaint)
            yPosition += 30
        }
    }
    
    private fun drawGoalProgress(canvas: Canvas, goals: List<ExportGoalProgress>) {
        val headerPaint = Paint().apply {
            color = Color.BLACK
            textSize = 16f
            isFakeBoldText = true
        }
        
        val bodyPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
        }
        
        var yPosition = 120f
        
        canvas.drawText("Goal Progress", margin.toFloat(), yPosition, headerPaint)
        yPosition += 30
        
        goals.forEach { goal ->
            canvas.drawText("${goal.goalType}:", margin + 20f, yPosition, headerPaint)
            yPosition += 20
            
            canvas.drawText("  Progress: ${"%.1f".format(goal.progressPercentage)}%", margin + 40f, yPosition, bodyPaint)
            yPosition += 20
            
            canvas.drawText("  Current: ${goal.currentValue} / Target: ${goal.targetValue}", margin + 40f, yPosition, bodyPaint)
            yPosition += 30
        }
    }
    
    private fun drawRecommendations(canvas: Canvas, recommendations: List<String>) {
        val headerPaint = Paint().apply {
            color = Color.BLACK
            textSize = 16f
            isFakeBoldText = true
        }
        
        val bodyPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
        }
        
        var yPosition = 120f
        
        canvas.drawText("Personalized Recommendations", margin.toFloat(), yPosition, headerPaint)
        yPosition += 30
        
        recommendations.forEachIndexed { index, recommendation ->
            canvas.drawText("${index + 1}. $recommendation", margin + 20f, yPosition, bodyPaint)
            yPosition += 25
        }
    }
}