package com.beaconledger.welltrack.data.recipe_import

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import com.beaconledger.welltrack.data.model.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Singleton
class RecipeOcrParser @Inject constructor(
    private val context: Context
) {

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun parseRecipeFromImage(imageUri: Uri): Result<ParsedRecipe> {
        return try {
            val bitmap = loadBitmapFromUri(imageUri)
            val recognizedText = recognizeText(bitmap)
            val parsedRecipe = parseRecipeFromText(recognizedText)
            Result.success(parsedRecipe)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun parseRecipeFromBitmap(bitmap: Bitmap): Result<ParsedRecipe> {
        return try {
            val recognizedText = recognizeText(bitmap)
            val parsedRecipe = parseRecipeFromText(recognizedText)
            Result.success(parsedRecipe)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun loadBitmapFromUri(uri: Uri): Bitmap {
        // Simplified implementation - in production, use proper image loading
        return suspendCoroutine { continuation ->
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                continuation.resume(bitmap)
            } catch (e: Exception) {
                throw Exception("Failed to load image from URI: ${e.message}")
            }
        }
    }

    private suspend fun recognizeText(bitmap: Bitmap): String {
        return suspendCoroutine { continuation ->
            val image = InputImage.fromBitmap(bitmap, 0)
            textRecognizer.process(image)
                .addOnSuccessListener { result ->
                    continuation.resume(result.text)
                }
                .addOnFailureListener { exception ->
                    throw Exception("OCR failed: ${exception.message}")
                }
        }
    }

    private fun parseRecipeFromText(text: String): ParsedRecipe {
        val lines = text.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
        
        val recipeName = extractRecipeName(lines)
        val ingredients = extractIngredients(lines)
        val instructions = extractInstructions(lines)
        val (prepTime, cookTime, servings) = extractMetadata(lines)

        return ParsedRecipe(
            name = recipeName,
            description = null,
            prepTime = prepTime,
            cookTime = cookTime,
            servings = servings,
            ingredients = ingredients,
            instructions = instructions,
            sourceUrl = "",
            sourceType = RecipeSource.OCR_SCAN
        )
    }

    private fun extractRecipeName(lines: List<String>): String {
        // Look for the recipe name - usually the first significant line or a title-like line
        for (line in lines.take(10)) { // Check first 10 lines
            if (line.length > 3 && 
                !line.contains("ingredients", ignoreCase = true) &&
                !line.contains("instructions", ignoreCase = true) &&
                !line.contains("directions", ignoreCase = true) &&
                !isIngredientLine(line) &&
                !isInstructionLine(line)) {
                return line
            }
        }
        return "OCR Scanned Recipe"
    }

    private fun extractIngredients(lines: List<String>): List<Ingredient> {
        val ingredients = mutableListOf<Ingredient>()
        var inIngredientsSection = false
        
        for (line in lines) {
            when {
                line.contains("ingredients", ignoreCase = true) -> {
                    inIngredientsSection = true
                    continue
                }
                line.contains("instructions", ignoreCase = true) ||
                line.contains("directions", ignoreCase = true) ||
                line.contains("method", ignoreCase = true) -> {
                    inIngredientsSection = false
                    break
                }
                inIngredientsSection && isIngredientLine(line) -> {
                    ingredients.add(parseIngredientFromLine(line))
                }
                !inIngredientsSection && isIngredientLine(line) -> {
                    // Sometimes ingredients don't have a clear header
                    ingredients.add(parseIngredientFromLine(line))
                }
            }
        }
        
        return ingredients
    }

    private fun extractInstructions(lines: List<String>): List<RecipeStep> {
        val instructions = mutableListOf<RecipeStep>()
        var inInstructionsSection = false
        var stepNumber = 1
        
        for (line in lines) {
            when {
                line.contains("instructions", ignoreCase = true) ||
                line.contains("directions", ignoreCase = true) ||
                line.contains("method", ignoreCase = true) -> {
                    inInstructionsSection = true
                    continue
                }
                inInstructionsSection && isInstructionLine(line) -> {
                    instructions.add(RecipeStep(
                        stepNumber = stepNumber++,
                        instruction = cleanInstructionText(line)
                    ))
                }
                !inInstructionsSection && isInstructionLine(line) && line.length > 20 -> {
                    // Sometimes instructions don't have a clear header
                    instructions.add(RecipeStep(
                        stepNumber = stepNumber++,
                        instruction = cleanInstructionText(line)
                    ))
                }
            }
        }
        
        return instructions
    }

    private fun extractMetadata(lines: List<String>): Triple<Int, Int, Int> {
        var prepTime = 15 // Default values
        var cookTime = 30
        var servings = 4
        
        for (line in lines) {
            val lowerLine = line.lowercase()
            
            // Extract prep time
            if (lowerLine.contains("prep") && lowerLine.contains("time")) {
                prepTime = extractTimeFromLine(line)
            }
            
            // Extract cook time
            if (lowerLine.contains("cook") && lowerLine.contains("time")) {
                cookTime = extractTimeFromLine(line)
            }
            
            // Extract servings
            if (lowerLine.contains("serves") || lowerLine.contains("serving")) {
                servings = extractServingsFromLine(line)
            }
        }
        
        return Triple(prepTime, cookTime, servings)
    }

    private fun isIngredientLine(line: String): Boolean {
        val lowerLine = line.lowercase()
        
        // Check for common ingredient patterns
        return (
            // Has quantity patterns
            line.matches(".*\\d+.*".toRegex()) &&
            // Has measurement units
            (lowerLine.contains("cup") || lowerLine.contains("tbsp") || lowerLine.contains("tsp") ||
             lowerLine.contains("oz") || lowerLine.contains("lb") || lowerLine.contains("gram") ||
             lowerLine.contains("ml") || lowerLine.contains("liter") || lowerLine.contains("piece"))
        ) || (
            // Or starts with bullet points/numbers
            line.matches("^[•\\-\\*\\d+\\.].*".toRegex()) &&
            line.length < 100 && // Ingredients are usually shorter
            !isInstructionLine(line)
        )
    }

    private fun isInstructionLine(line: String): Boolean {
        val lowerLine = line.lowercase()
        
        return line.length > 15 && (
            // Contains cooking verbs
            lowerLine.contains("heat") || lowerLine.contains("cook") || lowerLine.contains("bake") ||
            lowerLine.contains("mix") || lowerLine.contains("stir") || lowerLine.contains("add") ||
            lowerLine.contains("combine") || lowerLine.contains("place") || lowerLine.contains("pour") ||
            lowerLine.contains("season") || lowerLine.contains("serve") ||
            // Or starts with step indicators
            line.matches("^\\d+[\\.)\\s].*".toRegex())
        )
    }

    private fun parseIngredientFromLine(line: String): Ingredient {
        // Clean the line
        val cleanLine = line.replace("^[•\\-\\*\\d+\\.]\\s*".toRegex(), "").trim()
        
        // Try to extract quantity and unit
        val quantityRegex = "^(\\d+(?:\\.\\d+)?(?:/\\d+)?(?:\\s*-\\s*\\d+(?:\\.\\d+)?)?)\\s*".toRegex()
        val quantityMatch = quantityRegex.find(cleanLine)
        
        val quantity = quantityMatch?.groupValues?.get(1)?.let { parseQuantity(it) } ?: 1.0
        val remainingText = quantityMatch?.let { cleanLine.substring(it.range.last + 1) } ?: cleanLine
        
        // Extract unit
        val unitRegex = "^(cups?|tbsps?|tsps?|tablespoons?|teaspoons?|oz|ounces?|lbs?|pounds?|grams?|g|ml|liters?|pieces?|cloves?|slices?)\\s+".toRegex(RegexOption.IGNORE_CASE)
        val unitMatch = unitRegex.find(remainingText)
        
        val unit = unitMatch?.groupValues?.get(1) ?: ""
        val ingredientName = unitMatch?.let { remainingText.substring(it.range.last + 1) } ?: remainingText
        
        return Ingredient(
            name = ingredientName.trim(),
            quantity = quantity,
            unit = unit,
            category = categorizeIngredient(ingredientName)
        )
    }

    private fun parseQuantity(quantityText: String): Double {
        return when {
            quantityText.contains("/") -> {
                val parts = quantityText.split("/")
                if (parts.size == 2) {
                    (parts[0].toDoubleOrNull() ?: 1.0) / (parts[1].toDoubleOrNull() ?: 1.0)
                } else 1.0
            }
            quantityText.contains("-") -> {
                // Take the average of range
                val parts = quantityText.split("-")
                if (parts.size == 2) {
                    ((parts[0].trim().toDoubleOrNull() ?: 1.0) + (parts[1].trim().toDoubleOrNull() ?: 1.0)) / 2
                } else 1.0
            }
            else -> quantityText.toDoubleOrNull() ?: 1.0
        }
    }

    private fun cleanInstructionText(line: String): String {
        return line.replace("^\\d+[\\.)\\s]*".toRegex(), "").trim()
    }

    private fun extractTimeFromLine(line: String): Int {
        val timeRegex = "(\\d+)\\s*(min|minute|hour|hr)".toRegex(RegexOption.IGNORE_CASE)
        val match = timeRegex.find(line) ?: return 15
        
        val value = match.groupValues[1].toIntOrNull() ?: 15
        val unit = match.groupValues[2].lowercase()
        
        return if (unit.startsWith("h")) value * 60 else value
    }

    private fun extractServingsFromLine(line: String): Int {
        val servingsRegex = "(\\d+)".toRegex()
        return servingsRegex.find(line)?.groupValues?.get(1)?.toIntOrNull() ?: 4
    }

    private fun categorizeIngredient(name: String): IngredientCategory {
        val lowerName = name.lowercase()
        return when {
            lowerName.contains("chicken") || lowerName.contains("beef") || 
            lowerName.contains("pork") || lowerName.contains("fish") ||
            lowerName.contains("egg") || lowerName.contains("tofu") -> IngredientCategory.PROTEIN
            
            lowerName.contains("onion") || lowerName.contains("carrot") ||
            lowerName.contains("celery") || lowerName.contains("pepper") ||
            lowerName.contains("tomato") || lowerName.contains("garlic") -> IngredientCategory.VEGETABLES
            
            lowerName.contains("apple") || lowerName.contains("banana") ||
            lowerName.contains("berry") || lowerName.contains("orange") -> IngredientCategory.FRUITS
            
            lowerName.contains("rice") || lowerName.contains("pasta") ||
            lowerName.contains("bread") || lowerName.contains("flour") -> IngredientCategory.GRAINS
            
            lowerName.contains("milk") || lowerName.contains("cheese") ||
            lowerName.contains("butter") || lowerName.contains("cream") -> IngredientCategory.DAIRY
            
            lowerName.contains("salt") || lowerName.contains("pepper") ||
            lowerName.contains("herb") || lowerName.contains("spice") -> IngredientCategory.SPICES
            
            lowerName.contains("oil") || lowerName.contains("fat") -> IngredientCategory.OILS
            
            else -> IngredientCategory.OTHER
        }
    }
}