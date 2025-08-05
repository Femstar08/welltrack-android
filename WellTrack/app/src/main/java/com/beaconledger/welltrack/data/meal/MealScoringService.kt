package com.beaconledger.welltrack.data.meal

import com.beaconledger.welltrack.data.model.MealScore
import com.beaconledger.welltrack.data.model.NutritionInfo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MealScoringService @Inject constructor() {
    
    /**
     * Calculate meal score based on nutritional content
     * Uses A-E grading system with color coding
     */
    fun calculateMealScore(nutritionInfo: NutritionInfo): MealScore {
        val score = calculateNutritionalScore(nutritionInfo)
        
        return when {
            score >= 85 -> MealScore.A
            score >= 70 -> MealScore.B
            score >= 55 -> MealScore.C
            score >= 40 -> MealScore.D
            else -> MealScore.E
        }
    }
    
    /**
     * Calculate nutritional score (0-100) based on various factors
     */
    private fun calculateNutritionalScore(nutrition: NutritionInfo): Int {
        var score = 50 // Base score
        
        // Protein content (good protein content increases score)
        val proteinRatio = nutrition.proteins / (nutrition.calories / 4) // protein calories / total calories
        score += when {
            proteinRatio >= 0.25 -> 15 // 25%+ protein calories
            proteinRatio >= 0.20 -> 10 // 20%+ protein calories
            proteinRatio >= 0.15 -> 5  // 15%+ protein calories
            else -> -5 // Low protein
        }
        
        // Fiber content (high fiber is good)
        score += when {
            nutrition.fiber >= 10 -> 15 // High fiber
            nutrition.fiber >= 5 -> 10  // Moderate fiber
            nutrition.fiber >= 2 -> 5   // Some fiber
            else -> -5 // Low fiber
        }
        
        // Fat content (moderate fat is good, too much is bad)
        val fatRatio = (nutrition.fats * 9) / nutrition.calories // fat calories / total calories
        score += when {
            fatRatio <= 0.35 && fatRatio >= 0.20 -> 10 // Healthy fat range
            fatRatio <= 0.45 -> 5 // Acceptable fat
            fatRatio > 0.60 -> -15 // Too much fat
            else -> 0
        }
        
        // Carbohydrate quality (moderate carbs are good)
        val carbRatio = (nutrition.carbohydrates * 4) / nutrition.calories
        score += when {
            carbRatio <= 0.60 && carbRatio >= 0.30 -> 10 // Healthy carb range
            carbRatio <= 0.70 -> 5 // Acceptable carbs
            carbRatio > 0.80 -> -10 // Too many carbs
            else -> 0
        }
        
        // Sodium content (lower is better)
        score += when {
            nutrition.sodium <= 600 -> 10 // Low sodium
            nutrition.sodium <= 1000 -> 5 // Moderate sodium
            nutrition.sodium <= 1500 -> 0 // Acceptable sodium
            else -> -10 // High sodium
        }
        
        // Potassium content (higher is better)
        score += when {
            nutrition.potassium >= 400 -> 10 // High potassium
            nutrition.potassium >= 200 -> 5  // Moderate potassium
            else -> 0
        }
        
        // Calorie density (moderate density is good)
        val caloriesPerGram = nutrition.calories / 100 // Assuming 100g serving
        score += when {
            caloriesPerGram <= 1.5 -> 5 // Low calorie density
            caloriesPerGram <= 2.5 -> 10 // Moderate calorie density
            caloriesPerGram <= 4.0 -> 0 // High calorie density
            else -> -10 // Very high calorie density
        }
        
        return score.coerceIn(0, 100)
    }
    
    /**
     * Get detailed scoring breakdown for display
     */
    fun getMealScoreBreakdown(nutritionInfo: NutritionInfo): MealScoreBreakdown {
        val totalScore = calculateNutritionalScore(nutritionInfo)
        val grade = calculateMealScore(nutritionInfo)
        
        return MealScoreBreakdown(
            totalScore = totalScore,
            grade = grade,
            proteinScore = calculateProteinScore(nutritionInfo),
            fiberScore = calculateFiberScore(nutritionInfo),
            fatScore = calculateFatScore(nutritionInfo),
            sodiumScore = calculateSodiumScore(nutritionInfo),
            overallFeedback = generateFeedback(grade, nutritionInfo)
        )
    }
    
    private fun calculateProteinScore(nutrition: NutritionInfo): Int {
        val proteinRatio = nutrition.proteins / (nutrition.calories / 4)
        return when {
            proteinRatio >= 0.25 -> 15
            proteinRatio >= 0.20 -> 10
            proteinRatio >= 0.15 -> 5
            else -> -5
        }.coerceIn(0, 15)
    }
    
    private fun calculateFiberScore(nutrition: NutritionInfo): Int {
        return when {
            nutrition.fiber >= 10 -> 15
            nutrition.fiber >= 5 -> 10
            nutrition.fiber >= 2 -> 5
            else -> -5
        }.coerceIn(0, 15)
    }
    
    private fun calculateFatScore(nutrition: NutritionInfo): Int {
        val fatRatio = (nutrition.fats * 9) / nutrition.calories
        return when {
            fatRatio <= 0.35 && fatRatio >= 0.20 -> 10
            fatRatio <= 0.45 -> 5
            fatRatio > 0.60 -> -15
            else -> 0
        }.coerceIn(0, 10)
    }
    
    private fun calculateSodiumScore(nutrition: NutritionInfo): Int {
        return when {
            nutrition.sodium <= 600 -> 10
            nutrition.sodium <= 1000 -> 5
            nutrition.sodium <= 1500 -> 0
            else -> -10
        }.coerceIn(0, 10)
    }
    
    private fun generateFeedback(grade: MealScore, nutrition: NutritionInfo): String {
        return when (grade) {
            MealScore.A -> "Excellent nutritional balance! This meal provides great macro and micronutrient distribution."
            MealScore.B -> "Good meal choice with solid nutritional value. Minor improvements could be made."
            MealScore.C -> "Average meal with room for improvement. Consider adding more vegetables or reducing processed ingredients."
            MealScore.D -> "Below average nutritional quality. Try to include more whole foods and balance macronutrients."
            MealScore.E -> "Poor nutritional quality. This meal is high in calories, sodium, or lacks essential nutrients."
        }
    }
}

data class MealScoreBreakdown(
    val totalScore: Int,
    val grade: MealScore,
    val proteinScore: Int,
    val fiberScore: Int,
    val fatScore: Int,
    val sodiumScore: Int,
    val overallFeedback: String
)