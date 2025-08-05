package com.beaconledger.welltrack.data.recipe_import

import com.beaconledger.welltrack.data.model.Ingredient
import com.beaconledger.welltrack.data.model.RecipeStep
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipeImportValidator @Inject constructor() {

    fun validateParsedRecipe(parsedRecipe: ParsedRecipe): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        val warnings = mutableListOf<ValidationWarning>()

        // Validate recipe name
        if (parsedRecipe.name.isBlank()) {
            errors.add(ValidationError.MISSING_NAME)
        } else if (parsedRecipe.name.length < 3) {
            warnings.add(ValidationWarning.SHORT_NAME)
        }

        // Validate ingredients
        if (parsedRecipe.ingredients.isEmpty()) {
            errors.add(ValidationError.NO_INGREDIENTS)
        } else {
            parsedRecipe.ingredients.forEachIndexed { index, ingredient ->
                validateIngredient(ingredient, index)?.let { error ->
                    errors.add(error)
                }
            }
        }

        // Validate instructions
        if (parsedRecipe.instructions.isEmpty()) {
            errors.add(ValidationError.NO_INSTRUCTIONS)
        } else {
            parsedRecipe.instructions.forEachIndexed { index, instruction ->
                validateInstruction(instruction, index)?.let { error ->
                    errors.add(error)
                }
            }
        }

        // Validate timing
        if (parsedRecipe.prepTime < 0) {
            errors.add(ValidationError.INVALID_PREP_TIME)
        }
        if (parsedRecipe.cookTime < 0) {
            errors.add(ValidationError.INVALID_COOK_TIME)
        }
        if (parsedRecipe.prepTime == 0 && parsedRecipe.cookTime == 0) {
            warnings.add(ValidationWarning.NO_TIMING_INFO)
        }

        // Validate servings
        if (parsedRecipe.servings <= 0) {
            errors.add(ValidationError.INVALID_SERVINGS)
        }

        // Check for potential OCR errors
        if (parsedRecipe.sourceType == com.beaconledger.welltrack.data.model.RecipeSource.OCR_SCAN) {
            checkForOcrErrors(parsedRecipe, warnings)
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            warnings = warnings,
            suggestions = generateSuggestions(parsedRecipe, errors, warnings)
        )
    }

    private fun validateIngredient(ingredient: Ingredient, index: Int): ValidationError? {
        return when {
            ingredient.name.isBlank() -> ValidationError.MISSING_INGREDIENT_NAME(index)
            ingredient.quantity <= 0 -> ValidationError.INVALID_INGREDIENT_QUANTITY(index)
            ingredient.name.length < 2 -> ValidationError.INVALID_INGREDIENT_NAME(index)
            else -> null
        }
    }

    private fun validateInstruction(instruction: RecipeStep, index: Int): ValidationError? {
        return when {
            instruction.instruction.isBlank() -> ValidationError.MISSING_INSTRUCTION(index)
            instruction.instruction.length < 10 -> ValidationError.SHORT_INSTRUCTION(index)
            else -> null
        }
    }

    private fun checkForOcrErrors(parsedRecipe: ParsedRecipe, warnings: MutableList<ValidationWarning>) {
        // Check for common OCR misreads
        val commonOcrErrors = listOf(
            "rn" to "m", "cl" to "d", "0" to "o", "1" to "l", "5" to "s"
        )

        val allText = buildString {
            append(parsedRecipe.name)
            parsedRecipe.ingredients.forEach { append(" ${it.name}") }
            parsedRecipe.instructions.forEach { append(" ${it.instruction}") }
        }.lowercase()

        commonOcrErrors.forEach { (error, _) ->
            if (allText.contains(error)) {
                warnings.add(ValidationWarning.POTENTIAL_OCR_ERROR)
            }
        }

        // Check for unusual characters that might indicate OCR errors
        val unusualChars = "[^a-zA-Z0-9\\s.,!?()-/]".toRegex()
        if (unusualChars.containsMatchIn(allText)) {
            warnings.add(ValidationWarning.UNUSUAL_CHARACTERS)
        }

        // Check for very short ingredients (might be OCR fragments)
        val shortIngredients = parsedRecipe.ingredients.count { it.name.length < 3 }
        if (shortIngredients > parsedRecipe.ingredients.size * 0.3) {
            warnings.add(ValidationWarning.MANY_SHORT_INGREDIENTS)
        }
    }

    private fun generateSuggestions(
        parsedRecipe: ParsedRecipe,
        errors: List<ValidationError>,
        warnings: List<ValidationWarning>
    ): List<String> {
        val suggestions = mutableListOf<String>()

        if (errors.contains(ValidationError.MISSING_NAME)) {
            suggestions.add("Add a descriptive name for your recipe")
        }

        if (errors.any { it is ValidationError.MISSING_INGREDIENT_NAME }) {
            suggestions.add("Review ingredient names and fill in any missing information")
        }

        if (errors.any { it is ValidationError.INVALID_INGREDIENT_QUANTITY }) {
            suggestions.add("Check ingredient quantities - they should be positive numbers")
        }

        if (warnings.contains(ValidationWarning.NO_TIMING_INFO)) {
            suggestions.add("Consider adding prep and cook times for better meal planning")
        }

        if (warnings.contains(ValidationWarning.POTENTIAL_OCR_ERROR)) {
            suggestions.add("Review the text for any OCR scanning errors and correct them")
        }

        if (parsedRecipe.ingredients.size < 3) {
            suggestions.add("This recipe has very few ingredients - make sure all ingredients were captured")
        }

        if (parsedRecipe.instructions.size < 2) {
            suggestions.add("Consider breaking down the cooking process into more detailed steps")
        }

        return suggestions
    }
}

data class ValidationResult(
    val isValid: Boolean,
    val errors: List<ValidationError>,
    val warnings: List<ValidationWarning>,
    val suggestions: List<String>
)

sealed class ValidationError(val message: String) {
    object MISSING_NAME : ValidationError("Recipe name is required")
    object NO_INGREDIENTS : ValidationError("Recipe must have at least one ingredient")
    object NO_INSTRUCTIONS : ValidationError("Recipe must have at least one instruction")
    object INVALID_PREP_TIME : ValidationError("Prep time must be a positive number")
    object INVALID_COOK_TIME : ValidationError("Cook time must be a positive number")
    object INVALID_SERVINGS : ValidationError("Servings must be a positive number")
    
    data class MISSING_INGREDIENT_NAME(val index: Int) : ValidationError("Ingredient ${index + 1} is missing a name")
    data class INVALID_INGREDIENT_QUANTITY(val index: Int) : ValidationError("Ingredient ${index + 1} has invalid quantity")
    data class INVALID_INGREDIENT_NAME(val index: Int) : ValidationError("Ingredient ${index + 1} name is too short")
    data class MISSING_INSTRUCTION(val index: Int) : ValidationError("Instruction ${index + 1} is empty")
    data class SHORT_INSTRUCTION(val index: Int) : ValidationError("Instruction ${index + 1} is too short")
}

sealed class ValidationWarning(val message: String) {
    object SHORT_NAME : ValidationWarning("Recipe name is quite short")
    object NO_TIMING_INFO : ValidationWarning("No timing information provided")
    object POTENTIAL_OCR_ERROR : ValidationWarning("Potential OCR scanning errors detected")
    object UNUSUAL_CHARACTERS : ValidationWarning("Unusual characters detected - check for scanning errors")
    object MANY_SHORT_INGREDIENTS : ValidationWarning("Many ingredients have very short names - check for missing text")
}