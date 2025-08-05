package com.beaconledger.welltrack.data.recipe_import

import android.graphics.Bitmap
import android.net.Uri
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.data.nutrition.NutritionCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipeImportService @Inject constructor(
    private val urlParser: RecipeUrlParser,
    private val ocrParser: RecipeOcrParser,
    private val validator: RecipeImportValidator,
    private val nutritionCalculator: NutritionCalculator
) {

    suspend fun importFromUrl(url: String): Flow<ImportProgress> = flow {
        emit(ImportProgress.Started("Connecting to website..."))
        
        try {
            emit(ImportProgress.InProgress(25, "Downloading recipe content..."))
            
            val parseResult = urlParser.parseRecipeFromUrl(url)
            if (parseResult.isFailure) {
                emit(ImportProgress.Failed("Failed to parse recipe from URL: ${parseResult.exceptionOrNull()?.message}"))
                return@flow
            }
            
            val parsedRecipe = parseResult.getOrThrow()
            emit(ImportProgress.InProgress(50, "Validating recipe data..."))
            
            val validationResult = validator.validateParsedRecipe(parsedRecipe)
            emit(ImportProgress.InProgress(75, "Calculating nutrition information..."))
            
            val recipe = convertToRecipe(parsedRecipe)
            emit(ImportProgress.InProgress(90, "Finalizing import..."))
            
            emit(ImportProgress.Success(
                recipe = recipe,
                validationResult = validationResult,
                source = ImportSource.URL
            ))
            
        } catch (e: Exception) {
            emit(ImportProgress.Failed("Import failed: ${e.message}"))
        }
    }

    suspend fun importFromImage(imageUri: Uri): Flow<ImportProgress> = flow {
        emit(ImportProgress.Started("Processing image..."))
        
        try {
            emit(ImportProgress.InProgress(20, "Analyzing image content..."))
            
            val parseResult = ocrParser.parseRecipeFromImage(imageUri)
            if (parseResult.isFailure) {
                emit(ImportProgress.Failed("Failed to scan recipe from image: ${parseResult.exceptionOrNull()?.message}"))
                return@flow
            }
            
            val parsedRecipe = parseResult.getOrThrow()
            emit(ImportProgress.InProgress(60, "Validating scanned data..."))
            
            val validationResult = validator.validateParsedRecipe(parsedRecipe)
            emit(ImportProgress.InProgress(80, "Calculating nutrition information..."))
            
            val recipe = convertToRecipe(parsedRecipe)
            emit(ImportProgress.InProgress(95, "Finalizing import..."))
            
            emit(ImportProgress.Success(
                recipe = recipe,
                validationResult = validationResult,
                source = ImportSource.OCR
            ))
            
        } catch (e: Exception) {
            emit(ImportProgress.Failed("Import failed: ${e.message}"))
        }
    }

    suspend fun importFromBitmap(bitmap: Bitmap): Flow<ImportProgress> = flow {
        emit(ImportProgress.Started("Processing image..."))
        
        try {
            emit(ImportProgress.InProgress(20, "Scanning text from image..."))
            
            val parseResult = ocrParser.parseRecipeFromBitmap(bitmap)
            if (parseResult.isFailure) {
                emit(ImportProgress.Failed("Failed to scan recipe from image: ${parseResult.exceptionOrNull()?.message}"))
                return@flow
            }
            
            val parsedRecipe = parseResult.getOrThrow()
            emit(ImportProgress.InProgress(60, "Validating scanned data..."))
            
            val validationResult = validator.validateParsedRecipe(parsedRecipe)
            emit(ImportProgress.InProgress(80, "Calculating nutrition information..."))
            
            val recipe = convertToRecipe(parsedRecipe)
            emit(ImportProgress.InProgress(95, "Finalizing import..."))
            
            emit(ImportProgress.Success(
                recipe = recipe,
                validationResult = validationResult,
                source = ImportSource.OCR
            ))
            
        } catch (e: Exception) {
            emit(ImportProgress.Failed("Import failed: ${e.message}"))
        }
    }

    private fun convertToRecipe(parsedRecipe: ParsedRecipe): Recipe {
        val nutritionInfo = nutritionCalculator.calculateNutritionInfo(parsedRecipe.ingredients)
        val currentTime = LocalDateTime.now().toString()
        
        return Recipe(
            id = UUID.randomUUID().toString(),
            name = parsedRecipe.name,
            prepTime = parsedRecipe.prepTime,
            cookTime = parsedRecipe.cookTime,
            servings = parsedRecipe.servings,
            instructions = serializeSteps(parsedRecipe.instructions),
            nutritionInfo = serializeNutritionInfo(nutritionInfo),
            sourceType = parsedRecipe.sourceType,
            sourceUrl = parsedRecipe.sourceUrl.takeIf { it.isNotEmpty() },
            tags = serializeStringList(parsedRecipe.tags),
            createdAt = currentTime,
            updatedAt = currentTime
        )
    }

    private fun serializeSteps(steps: List<RecipeStep>): String {
        return steps.joinToString("|||") { step ->
            "${step.stepNumber}::${step.instruction}::${step.duration ?: ""}::${step.temperature ?: ""}::${step.equipment.joinToString(",")}"
        }
    }

    private fun serializeStringList(list: List<String>): String {
        return list.joinToString(",")
    }

    private fun serializeNutritionInfo(nutrition: NutritionInfo): String {
        return "${nutrition.calories},${nutrition.carbohydrates},${nutrition.proteins},${nutrition.fats},${nutrition.fiber},${nutrition.sodium},${nutrition.potassium}"
    }
}

sealed class ImportProgress {
    data class Started(val message: String) : ImportProgress()
    data class InProgress(val percentage: Int, val message: String) : ImportProgress()
    data class Success(
        val recipe: Recipe,
        val validationResult: ValidationResult,
        val source: ImportSource
    ) : ImportProgress()
    data class Failed(val error: String) : ImportProgress()
}

enum class ImportSource {
    URL,
    OCR
}

data class ImportResult(
    val recipe: Recipe,
    val ingredients: List<Ingredient>,
    val validationResult: ValidationResult,
    val source: ImportSource
)