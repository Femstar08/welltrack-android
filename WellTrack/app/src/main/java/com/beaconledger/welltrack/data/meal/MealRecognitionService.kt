package com.beaconledger.welltrack.data.meal

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.beaconledger.welltrack.data.model.Ingredient
import com.beaconledger.welltrack.data.model.IngredientCategory
import com.beaconledger.welltrack.data.model.NutritionInfo
import com.beaconledger.welltrack.data.nutrition.NutritionCalculator
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MealRecognitionService @Inject constructor(
    private val nutritionCalculator: NutritionCalculator
) {
    
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    
    /**
     * Recognize meal from image using ML Kit OCR and basic food recognition
     * This is a simplified implementation - in production, you'd use a more sophisticated ML model
     */
    suspend fun recognizeMealFromImage(context: Context, imageUri: Uri): Result<MealRecognitionResult> {
        return try {
            val image = InputImage.fromFilePath(context, imageUri)
            val visionText = suspendCancellableCoroutine { continuation ->
                textRecognizer.process(image)
                    .addOnSuccessListener { result ->
                        continuation.resume(result)
                    }
                    .addOnFailureListener { exception ->
                        continuation.resumeWithException(exception)
                    }
            }
            
            val recognizedText = visionText.text.lowercase()
            val recognizedIngredients = extractIngredientsFromText(recognizedText)
            val nutritionInfo = nutritionCalculator.calculateNutritionInfo(recognizedIngredients)
            
            val result = MealRecognitionResult(
                recognizedIngredients = recognizedIngredients,
                nutritionInfo = nutritionInfo,
                confidence = calculateConfidence(recognizedIngredients),
                suggestedMealName = generateMealName(recognizedIngredients)
            )
            
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Recognize meal from bitmap
     */
    suspend fun recognizeMealFromBitmap(bitmap: Bitmap): Result<MealRecognitionResult> {
        return try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val visionText = suspendCancellableCoroutine { continuation ->
                textRecognizer.process(image)
                    .addOnSuccessListener { result ->
                        continuation.resume(result)
                    }
                    .addOnFailureListener { exception ->
                        continuation.resumeWithException(exception)
                    }
            }
            
            val recognizedText = visionText.text.lowercase()
            val recognizedIngredients = extractIngredientsFromText(recognizedText)
            val nutritionInfo = nutritionCalculator.calculateNutritionInfo(recognizedIngredients)
            
            val result = MealRecognitionResult(
                recognizedIngredients = recognizedIngredients,
                nutritionInfo = nutritionInfo,
                confidence = calculateConfidence(recognizedIngredients),
                suggestedMealName = generateMealName(recognizedIngredients)
            )
            
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Extract ingredients from recognized text using keyword matching
     * This is a simplified approach - production would use NLP and food databases
     */
    private fun extractIngredientsFromText(text: String): List<Ingredient> {
        val ingredients = mutableListOf<Ingredient>()
        
        // Common food keywords and their categories
        val foodKeywords = mapOf(
            // Proteins
            "chicken" to IngredientCategory.PROTEIN,
            "beef" to IngredientCategory.PROTEIN,
            "pork" to IngredientCategory.PROTEIN,
            "fish" to IngredientCategory.PROTEIN,
            "salmon" to IngredientCategory.PROTEIN,
            "tuna" to IngredientCategory.PROTEIN,
            "egg" to IngredientCategory.PROTEIN,
            "tofu" to IngredientCategory.PROTEIN,
            
            // Vegetables
            "broccoli" to IngredientCategory.VEGETABLES,
            "carrot" to IngredientCategory.VEGETABLES,
            "spinach" to IngredientCategory.VEGETABLES,
            "tomato" to IngredientCategory.VEGETABLES,
            "onion" to IngredientCategory.VEGETABLES,
            "pepper" to IngredientCategory.VEGETABLES,
            "lettuce" to IngredientCategory.VEGETABLES,
            "cucumber" to IngredientCategory.VEGETABLES,
            
            // Grains
            "rice" to IngredientCategory.GRAINS,
            "pasta" to IngredientCategory.GRAINS,
            "bread" to IngredientCategory.GRAINS,
            "quinoa" to IngredientCategory.GRAINS,
            "oats" to IngredientCategory.GRAINS,
            
            // Fruits
            "apple" to IngredientCategory.FRUITS,
            "banana" to IngredientCategory.FRUITS,
            "orange" to IngredientCategory.FRUITS,
            "berry" to IngredientCategory.FRUITS,
            "strawberry" to IngredientCategory.FRUITS,
            
            // Dairy
            "cheese" to IngredientCategory.DAIRY,
            "milk" to IngredientCategory.DAIRY,
            "yogurt" to IngredientCategory.DAIRY,
            "butter" to IngredientCategory.DAIRY
        )
        
        foodKeywords.forEach { (keyword, category) ->
            if (text.contains(keyword)) {
                ingredients.add(
                    Ingredient(
                        name = keyword.replaceFirstChar { it.uppercase() },
                        quantity = 100.0, // Default quantity
                        unit = "g",
                        category = category
                    )
                )
            }
        }
        
        // If no ingredients found, add a generic "Mixed meal" ingredient
        if (ingredients.isEmpty()) {
            ingredients.add(
                Ingredient(
                    name = "Mixed meal",
                    quantity = 200.0,
                    unit = "g",
                    category = IngredientCategory.OTHER
                )
            )
        }
        
        return ingredients
    }
    
    /**
     * Calculate confidence score based on number of recognized ingredients
     */
    private fun calculateConfidence(ingredients: List<Ingredient>): Float {
        return when {
            ingredients.size >= 3 -> 0.8f
            ingredients.size == 2 -> 0.6f
            ingredients.size == 1 -> 0.4f
            else -> 0.2f
        }
    }
    
    /**
     * Generate a meal name based on recognized ingredients
     */
    private fun generateMealName(ingredients: List<Ingredient>): String {
        if (ingredients.isEmpty()) return "Unknown Meal"
        
        val mainIngredients = ingredients.take(2)
        return when (mainIngredients.size) {
            1 -> "${mainIngredients[0].name} Meal"
            2 -> "${mainIngredients[0].name} with ${mainIngredients[1].name}"
            else -> "Mixed Meal"
        }
    }
}

data class MealRecognitionResult(
    val recognizedIngredients: List<Ingredient>,
    val nutritionInfo: NutritionInfo,
    val confidence: Float,
    val suggestedMealName: String
)