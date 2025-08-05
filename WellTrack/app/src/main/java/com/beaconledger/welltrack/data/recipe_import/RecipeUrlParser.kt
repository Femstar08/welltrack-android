package com.beaconledger.welltrack.data.recipe_import

import com.beaconledger.welltrack.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipeUrlParser @Inject constructor() {

    suspend fun parseRecipeFromUrl(url: String): Result<ParsedRecipe> = withContext(Dispatchers.IO) {
        try {
            val document = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(10000)
                .get()

            val parsedRecipe = parseStructuredData(document, url)
                ?: parseFallbackParsing(document, url)
                ?: return@withContext Result.failure(Exception("Unable to parse recipe from URL"))

            Result.success(parsedRecipe)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseStructuredData(document: Document, url: String): ParsedRecipe? {
        // Try to parse JSON-LD structured data first
        val jsonLdScripts = document.select("script[type=application/ld+json]")
        
        for (script in jsonLdScripts) {
            try {
                val jsonContent = script.html()
                if (jsonContent.contains("Recipe") || jsonContent.contains("recipe")) {
                    return parseJsonLdRecipe(jsonContent, url)
                }
            } catch (e: Exception) {
                // Continue to next script or fallback parsing
            }
        }

        // Try microdata parsing
        val microdataRecipe = document.select("[itemtype*=Recipe]").first()
        if (microdataRecipe != null) {
            return parseMicrodataRecipe(microdataRecipe, url)
        }

        return null
    }

    private fun parseJsonLdRecipe(jsonContent: String, url: String): ParsedRecipe? {
        // Simplified JSON-LD parsing - in production, use a proper JSON parser
        return try {
            val name = extractJsonValue(jsonContent, "name") ?: "Imported Recipe"
            val description = extractJsonValue(jsonContent, "description")
            val prepTime = parseTimeToMinutes(extractJsonValue(jsonContent, "prepTime"))
            val cookTime = parseTimeToMinutes(extractJsonValue(jsonContent, "cookTime"))
            val servings = extractJsonValue(jsonContent, "recipeYield")?.toIntOrNull() ?: 4

            val ingredients = parseJsonIngredients(jsonContent)
            val instructions = parseJsonInstructions(jsonContent)

            ParsedRecipe(
                name = name,
                description = description,
                prepTime = prepTime,
                cookTime = cookTime,
                servings = servings,
                ingredients = ingredients,
                instructions = instructions,
                sourceUrl = url,
                sourceType = RecipeSource.URL_IMPORT
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun parseMicrodataRecipe(element: org.jsoup.nodes.Element, url: String): ParsedRecipe? {
        return try {
            val name = element.select("[itemprop=name]").text().takeIf { it.isNotEmpty() } ?: "Imported Recipe"
            val description = element.select("[itemprop=description]").text()
            val prepTime = parseTimeToMinutes(element.select("[itemprop=prepTime]").attr("datetime"))
            val cookTime = parseTimeToMinutes(element.select("[itemprop=cookTime]").attr("datetime"))
            val servings = element.select("[itemprop=recipeYield]").text().toIntOrNull() ?: 4

            val ingredients = element.select("[itemprop=recipeIngredient]").map { ingredient ->
                parseIngredientText(ingredient.text())
            }

            val instructions = element.select("[itemprop=recipeInstruction]").mapIndexed { index, instruction ->
                RecipeStep(
                    stepNumber = index + 1,
                    instruction = instruction.text()
                )
            }

            ParsedRecipe(
                name = name,
                description = description,
                prepTime = prepTime,
                cookTime = cookTime,
                servings = servings,
                ingredients = ingredients,
                instructions = instructions,
                sourceUrl = url,
                sourceType = RecipeSource.URL_IMPORT
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun parseFallbackParsing(document: Document, url: String): ParsedRecipe? {
        return try {
            // Try common selectors for recipe sites
            val name = document.select("h1").first()?.text()
                ?: document.select(".recipe-title, .entry-title").first()?.text()
                ?: "Imported Recipe"

            val description = document.select(".recipe-description, .recipe-summary").first()?.text()

            // Look for ingredients in common containers
            val ingredientElements = document.select(
                ".recipe-ingredients li, .ingredients li, [class*=ingredient] li, .recipe-ingredient"
            )
            val ingredients = ingredientElements.map { parseIngredientText(it.text()) }

            // Look for instructions in common containers
            val instructionElements = document.select(
                ".recipe-instructions li, .instructions li, .recipe-method li, .directions li"
            )
            val instructions = instructionElements.mapIndexed { index, instruction ->
                RecipeStep(
                    stepNumber = index + 1,
                    instruction = instruction.text()
                )
            }

            if (ingredients.isEmpty() && instructions.isEmpty()) {
                return null
            }

            ParsedRecipe(
                name = name,
                description = description,
                prepTime = 15, // Default values when not found
                cookTime = 30,
                servings = 4,
                ingredients = ingredients,
                instructions = instructions,
                sourceUrl = url,
                sourceType = RecipeSource.URL_IMPORT
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun extractJsonValue(json: String, key: String): String? {
        val regex = "\"$key\"\\s*:\\s*\"([^\"]+)\"".toRegex()
        return regex.find(json)?.groupValues?.get(1)
    }

    private fun parseJsonIngredients(json: String): List<Ingredient> {
        val ingredientsRegex = "\"recipeIngredient\"\\s*:\\s*\\[(.*?)\\]".toRegex(RegexOption.DOT_MATCHES_ALL)
        val ingredientsMatch = ingredientsRegex.find(json) ?: return emptyList()
        
        val ingredientsText = ingredientsMatch.groupValues[1]
        val ingredientRegex = "\"([^\"]+)\"".toRegex()
        
        return ingredientRegex.findAll(ingredientsText).map { match ->
            parseIngredientText(match.groupValues[1])
        }.toList()
    }

    private fun parseJsonInstructions(json: String): List<RecipeStep> {
        val instructionsRegex = "\"recipeInstructions\"\\s*:\\s*\\[(.*?)\\]".toRegex(RegexOption.DOT_MATCHES_ALL)
        val instructionsMatch = instructionsRegex.find(json) ?: return emptyList()
        
        val instructionsText = instructionsMatch.groupValues[1]
        val instructionRegex = "\"text\"\\s*:\\s*\"([^\"]+)\"".toRegex()
        
        return instructionRegex.findAll(instructionsText).mapIndexed { index, match ->
            RecipeStep(
                stepNumber = index + 1,
                instruction = match.groupValues[1]
            )
        }.toList()
    }

    private fun parseIngredientText(text: String): Ingredient {
        // Simple ingredient parsing - extract quantity, unit, and name
        val cleanText = text.trim()
        val parts = cleanText.split(" ", limit = 3)
        
        val quantity = parts.getOrNull(0)?.toDoubleOrNull() ?: 1.0
        val unit = if (parts.size > 1 && parts[0].toDoubleOrNull() != null) {
            parts.getOrNull(1) ?: ""
        } else ""
        
        val name = if (parts.size > 2 && parts[0].toDoubleOrNull() != null) {
            parts.drop(2).joinToString(" ")
        } else cleanText
        
        return Ingredient(
            name = name,
            quantity = quantity,
            unit = unit,
            category = categorizeIngredient(name)
        )
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

    private fun parseTimeToMinutes(timeString: String?): Int {
        if (timeString.isNullOrEmpty()) return 0
        
        // Parse ISO 8601 duration (PT15M) or simple text (15 minutes)
        return when {
            timeString.startsWith("PT") -> {
                val minutes = "PT(\\d+)M".toRegex().find(timeString)?.groupValues?.get(1)?.toIntOrNull() ?: 0
                val hours = "PT(\\d+)H".toRegex().find(timeString)?.groupValues?.get(1)?.toIntOrNull() ?: 0
                hours * 60 + minutes
            }
            timeString.contains("hour") -> {
                val hours = "\\d+".toRegex().find(timeString)?.value?.toIntOrNull() ?: 0
                hours * 60
            }
            timeString.contains("min") -> {
                "\\d+".toRegex().find(timeString)?.value?.toIntOrNull() ?: 0
            }
            else -> timeString.toIntOrNull() ?: 0
        }
    }
}

data class ParsedRecipe(
    val name: String,
    val description: String? = null,
    val prepTime: Int,
    val cookTime: Int,
    val servings: Int,
    val ingredients: List<Ingredient>,
    val instructions: List<RecipeStep>,
    val sourceUrl: String,
    val sourceType: RecipeSource,
    val tags: List<String> = emptyList()
)