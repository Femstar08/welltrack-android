package com.beaconledger.welltrack.data.analysis

import com.beaconledger.welltrack.data.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DietaryAnalyzer @Inject constructor() {
    
    // Common allergens and their variations
    private val allergenKeywords = mapOf(
        "peanuts" to listOf("peanut", "groundnut", "arachis"),
        "tree nuts" to listOf("almond", "walnut", "cashew", "pecan", "pistachio", "hazelnut", "brazil nut", "macadamia"),
        "dairy" to listOf("milk", "cheese", "butter", "cream", "yogurt", "lactose", "casein", "whey"),
        "eggs" to listOf("egg", "albumin", "mayonnaise"),
        "soy" to listOf("soy", "soya", "tofu", "tempeh", "miso", "edamame", "soybean"),
        "wheat" to listOf("wheat", "flour", "gluten", "bread", "pasta", "couscous"),
        "shellfish" to listOf("shrimp", "crab", "lobster", "scallop", "clam", "mussel", "oyster"),
        "fish" to listOf("salmon", "tuna", "cod", "halibut", "sardine", "anchovy"),
        "sesame" to listOf("sesame", "tahini", "hummus"),
        "sulfites" to listOf("sulfite", "wine", "dried fruit")
    )
    
    // Dietary restriction indicators
    private val restrictionKeywords = mapOf(
        DietaryRestrictionType.VEGETARIAN to listOf("vegetarian", "veggie", "no meat", "plant-based"),
        DietaryRestrictionType.VEGAN to listOf("vegan", "plant-based", "dairy-free", "no animal products"),
        DietaryRestrictionType.GLUTEN_FREE to listOf("gluten-free", "gluten free", "gf", "celiac"),
        DietaryRestrictionType.KETO to listOf("keto", "ketogenic", "low-carb", "lchf"),
        DietaryRestrictionType.PALEO to listOf("paleo", "paleolithic", "primal"),
        DietaryRestrictionType.DAIRY_FREE to listOf("dairy-free", "dairy free", "lactose-free", "no dairy"),
        DietaryRestrictionType.LOW_SODIUM to listOf("low-sodium", "low sodium", "no salt", "salt-free"),
        DietaryRestrictionType.HIGH_PROTEIN to listOf("high-protein", "high protein", "protein-rich"),
        DietaryRestrictionType.LOW_CARB to listOf("low-carb", "low carb", "reduced carb"),
        DietaryRestrictionType.HALAL to listOf("halal", "islamic", "no pork", "no alcohol"),
        DietaryRestrictionType.KOSHER to listOf("kosher", "jewish", "pareve", "parve")
    )
    
    // Cuisine indicators
    private val cuisineKeywords = mapOf(
        "italian" to listOf("italian", "pasta", "pizza", "risotto", "marinara", "pesto"),
        "mexican" to listOf("mexican", "taco", "burrito", "salsa", "guacamole", "enchilada"),
        "asian" to listOf("asian", "stir-fry", "soy sauce", "ginger", "sesame"),
        "chinese" to listOf("chinese", "wok", "dim sum", "szechuan", "cantonese"),
        "indian" to listOf("indian", "curry", "turmeric", "cumin", "garam masala", "naan"),
        "mediterranean" to listOf("mediterranean", "olive oil", "feta", "olives", "hummus"),
        "french" to listOf("french", "baguette", "brie", "wine", "herbs de provence"),
        "thai" to listOf("thai", "coconut milk", "lemongrass", "fish sauce", "pad thai"),
        "japanese" to listOf("japanese", "sushi", "miso", "sake", "wasabi", "nori"),
        "greek" to listOf("greek", "feta", "tzatziki", "oregano", "phyllo")
    )
    
    // Cooking method indicators
    private val cookingMethodKeywords = mapOf(
        "grilled" to listOf("grilled", "grill", "barbecue", "bbq", "charred"),
        "baked" to listOf("baked", "bake", "oven", "roasted", "roast"),
        "fried" to listOf("fried", "fry", "deep-fried", "pan-fried", "sautéed"),
        "steamed" to listOf("steamed", "steam", "steamer"),
        "boiled" to listOf("boiled", "boil", "poached", "simmered"),
        "raw" to listOf("raw", "fresh", "uncooked", "sashimi", "carpaccio"),
        "smoked" to listOf("smoked", "smoke", "cured"),
        "braised" to listOf("braised", "braise", "slow-cooked", "stewed")
    )
    
    fun analyzeRecipeForDietaryTags(recipe: Recipe): List<DietaryTag> {
        val tags = mutableListOf<DietaryTag>()
        val searchText = "${recipe.name} ${recipe.tags}".lowercase()
        
        // Analyze for dietary restrictions
        restrictionKeywords.forEach { (restriction, keywords) ->
            val confidence = calculateKeywordConfidence(searchText, keywords)
            if (confidence > 0.3f) {
                tags.add(
                    DietaryTag(
                        type = DietaryTagType.RESTRICTION,
                        value = restriction.name,
                        confidence = confidence
                    )
                )
            }
        }
        
        // Analyze for potential allergens
        allergenKeywords.forEach { (allergen, keywords) ->
            val confidence = calculateKeywordConfidence(searchText, keywords)
            if (confidence > 0.4f) {
                tags.add(
                    DietaryTag(
                        type = DietaryTagType.ALLERGEN,
                        value = allergen,
                        confidence = confidence
                    )
                )
            }
        }
        
        // Analyze for cuisine type
        cuisineKeywords.forEach { (cuisine, keywords) ->
            val confidence = calculateKeywordConfidence(searchText, keywords)
            if (confidence > 0.5f) {
                tags.add(
                    DietaryTag(
                        type = DietaryTagType.CUISINE,
                        value = cuisine,
                        confidence = confidence
                    )
                )
            }
        }
        
        // Analyze for cooking methods
        cookingMethodKeywords.forEach { (method, keywords) ->
            val confidence = calculateKeywordConfidence(searchText, keywords)
            if (confidence > 0.4f) {
                tags.add(
                    DietaryTag(
                        type = DietaryTagType.COOKING_METHOD,
                        value = method,
                        confidence = confidence
                    )
                )
            }
        }
        
        // Analyze nutritional focus based on prep/cook time and name
        tags.addAll(analyzeNutritionalFocus(recipe))
        
        return tags
    }
    
    fun analyzeMealForDietaryTags(meal: Meal): List<DietaryTag> {
        val tags = mutableListOf<DietaryTag>()
        
        // For meals, we would analyze based on the meal's nutritional info
        // and any associated recipe information
        
        // Add meal type tag
        tags.add(
            DietaryTag(
                type = DietaryTagType.MEAL_TYPE,
                value = meal.mealType.name,
                confidence = 1.0f
            )
        )
        
        return tags
    }
    
    fun checkDietaryCompatibility(
        userProfile: DietaryProfile,
        recipeTags: List<DietaryTag>
    ): DietaryCompatibility {
        val violations = mutableListOf<DietaryViolation>()
        val warnings = mutableListOf<DietaryWarning>()
        
        // Check for dietary restriction violations
        userProfile.restrictions.forEach { restriction ->
            val conflictingTags = recipeTags.filter { tag ->
                tag.type == DietaryTagType.RESTRICTION && 
                !isRestrictionCompatible(restriction.restrictionType, tag.value)
            }
            
            if (conflictingTags.isNotEmpty()) {
                violations.add(
                    DietaryViolation(
                        type = ViolationType.DIETARY_RESTRICTION,
                        restriction = restriction.restrictionType,
                        allergen = null,
                        severity = restriction.severity,
                        description = "Recipe conflicts with ${restriction.restrictionType.displayName} restriction",
                        affectedIngredients = conflictingTags.map { it.value }
                    )
                )
            }
        }
        
        // Check for allergen violations
        userProfile.allergies.forEach { allergy ->
            val allergenTags = recipeTags.filter { tag ->
                tag.type == DietaryTagType.ALLERGEN && 
                tag.value.contains(allergy.allergen, ignoreCase = true)
            }
            
            if (allergenTags.isNotEmpty()) {
                violations.add(
                    DietaryViolation(
                        type = ViolationType.ALLERGEN,
                        restriction = null,
                        allergen = allergy.allergen,
                        severity = RestrictionSeverity.MEDICAL, // Allergies are always medical severity
                        description = "Recipe contains ${allergy.allergen} allergen",
                        affectedIngredients = allergenTags.map { it.value }
                    )
                )
            }
        }
        
        // Check for preference mismatches
        userProfile.preferences.forEach { preference ->
            if (preference.preference == PreferenceLevel.HATE || preference.preference == PreferenceLevel.DISLIKE) {
                val conflictingTags = recipeTags.filter { tag ->
                    tag.value.contains(preference.item, ignoreCase = true)
                }
                
                if (conflictingTags.isNotEmpty() && preference.preference == PreferenceLevel.HATE) {
                    violations.add(
                        DietaryViolation(
                            type = ViolationType.STRONG_DISLIKE,
                            restriction = null,
                            allergen = null,
                            severity = RestrictionSeverity.MODERATE,
                            description = "Recipe contains strongly disliked ${preference.preferenceType.displayName.lowercase()}: ${preference.item}",
                            affectedIngredients = listOf(preference.item)
                        )
                    )
                } else if (conflictingTags.isNotEmpty()) {
                    warnings.add(
                        DietaryWarning(
                            type = WarningType.PREFERENCE_MISMATCH,
                            description = "Recipe contains disliked ${preference.preferenceType.displayName.lowercase()}: ${preference.item}",
                            suggestion = "Consider substituting or omitting this ingredient"
                        )
                    )
                }
            }
        }
        
        val isCompatible = violations.isEmpty()
        val score = calculateCompatibilityScore(violations, warnings, userProfile.preferences, recipeTags)
        
        return DietaryCompatibility(
            isCompatible = isCompatible,
            violations = violations,
            warnings = warnings,
            score = score
        )
    }
    
    private fun calculateKeywordConfidence(text: String, keywords: List<String>): Float {
        val matches = keywords.count { keyword ->
            text.contains(keyword, ignoreCase = true)
        }
        return if (keywords.isEmpty()) 0f else matches.toFloat() / keywords.size
    }
    
    private fun analyzeNutritionalFocus(recipe: Recipe): List<DietaryTag> {
        val tags = mutableListOf<DietaryTag>()
        val totalTime = recipe.prepTime + recipe.cookTime
        val recipeName = recipe.name.lowercase()
        
        // Quick meals
        if (totalTime <= 30) {
            tags.add(
                DietaryTag(
                    type = DietaryTagType.NUTRITION_FOCUS,
                    value = "quick_meal",
                    confidence = 0.8f
                )
            )
        }
        
        // Healthy indicators
        val healthyKeywords = listOf("healthy", "light", "fresh", "clean", "nutritious")
        if (healthyKeywords.any { recipeName.contains(it) }) {
            tags.add(
                DietaryTag(
                    type = DietaryTagType.NUTRITION_FOCUS,
                    value = "healthy",
                    confidence = 0.7f
                )
            )
        }
        
        // Comfort food indicators
        val comfortKeywords = listOf("comfort", "hearty", "rich", "creamy", "indulgent")
        if (comfortKeywords.any { recipeName.contains(it) }) {
            tags.add(
                DietaryTag(
                    type = DietaryTagType.NUTRITION_FOCUS,
                    value = "comfort_food",
                    confidence = 0.7f
                )
            )
        }
        
        return tags
    }
    
    private fun isRestrictionCompatible(userRestriction: DietaryRestrictionType, recipeTag: String): Boolean {
        // This would implement logic to check if a recipe tag is compatible with a user's restriction
        // For example, a vegetarian user should be compatible with vegetarian recipes
        return when (userRestriction) {
            DietaryRestrictionType.VEGETARIAN -> recipeTag.contains("vegetarian", ignoreCase = true) || 
                                                recipeTag.contains("vegan", ignoreCase = true)
            DietaryRestrictionType.VEGAN -> recipeTag.contains("vegan", ignoreCase = true)
            DietaryRestrictionType.GLUTEN_FREE -> recipeTag.contains("gluten", ignoreCase = true)
            else -> true // Default to compatible for other restrictions
        }
    }
    
    /**
     * Analyze recipe compatibility with user's dietary profile
     */
    fun analyzeRecipeCompatibility(recipe: Recipe, dietaryProfile: DietaryProfile): DietaryCompatibility {
        val recipeTags = analyzeRecipeForDietaryTags(recipe)
        return checkDietaryCompatibility(dietaryProfile, recipeTags)
    }
    
    /**
     * Analyze ingredients for dietary violations
     */
    fun analyzeIngredients(ingredients: List<Ingredient>, dietaryProfile: DietaryProfile): List<DietaryViolation> {
        val violations = mutableListOf<DietaryViolation>()
        
        for (ingredient in ingredients) {
            // Check allergies
            for (allergy in dietaryProfile.allergies) {
                if (ingredient.name.contains(allergy.allergen, ignoreCase = true)) {
                    violations.add(
                        DietaryViolation(
                            type = ViolationType.ALLERGEN,
                            restriction = null,
                            allergen = allergy.allergen,
                            severity = when (allergy.severity) {
                                AllergySeverity.MILD -> RestrictionSeverity.MILD
                                AllergySeverity.MODERATE -> RestrictionSeverity.MODERATE
                                AllergySeverity.SEVERE -> RestrictionSeverity.STRICT
                                AllergySeverity.ANAPHYLAXIS -> RestrictionSeverity.MEDICAL
                            },
                            description = "Contains allergen: ${allergy.allergen}",
                            affectedIngredients = listOf(ingredient.name)
                        )
                    )
                }
            }
            
            // Check dietary restrictions
            for (restriction in dietaryProfile.restrictions) {
                if (violatesRestriction(ingredient.name, restriction.restrictionType)) {
                    violations.add(
                        DietaryViolation(
                            type = ViolationType.DIETARY_RESTRICTION,
                            restriction = restriction.restrictionType,
                            allergen = null,
                            severity = restriction.severity,
                            description = "Violates ${restriction.restrictionType.displayName} restriction",
                            affectedIngredients = listOf(ingredient.name)
                        )
                    )
                }
            }
        }
        
        return violations
    }
    
    private fun violatesRestriction(ingredientName: String, restriction: DietaryRestrictionType): Boolean {
        val ingredient = ingredientName.lowercase()
        return when (restriction) {
            DietaryRestrictionType.VEGETARIAN -> 
                ingredient.contains("meat") || ingredient.contains("chicken") ||
                ingredient.contains("beef") || ingredient.contains("pork") ||
                ingredient.contains("fish") || ingredient.contains("seafood")
            DietaryRestrictionType.VEGAN -> 
                violatesRestriction(ingredientName, DietaryRestrictionType.VEGETARIAN) ||
                ingredient.contains("milk") || ingredient.contains("cheese") ||
                ingredient.contains("egg") || ingredient.contains("honey") ||
                ingredient.contains("butter") || ingredient.contains("cream")
            DietaryRestrictionType.GLUTEN_FREE -> 
                ingredient.contains("wheat") || ingredient.contains("barley") ||
                ingredient.contains("rye") || ingredient.contains("flour")
            DietaryRestrictionType.DAIRY_FREE -> 
                ingredient.contains("milk") || ingredient.contains("cheese") ||
                ingredient.contains("butter") || ingredient.contains("cream") ||
                ingredient.contains("yogurt")
            DietaryRestrictionType.NUT_FREE -> 
                ingredient.contains("nut") || ingredient.contains("almond") ||
                ingredient.contains("walnut") || ingredient.contains("peanut")
            DietaryRestrictionType.SOY_FREE -> 
                ingredient.contains("soy") || ingredient.contains("tofu") ||
                ingredient.contains("tempeh")
            else -> false
        }
    }
    
    private fun calculateCompatibilityScore(
        violations: List<DietaryViolation>,
        warnings: List<DietaryWarning>,
        preferences: List<UserFoodPreference>,
        recipeTags: List<DietaryTag>
    ): Float {
        var score = 1.0f
        
        // Deduct for violations
        violations.forEach { violation ->
            score -= when (violation.severity) {
                RestrictionSeverity.MEDICAL -> 1.0f // Complete incompatibility
                RestrictionSeverity.STRICT -> 0.8f
                RestrictionSeverity.MODERATE -> 0.4f
                RestrictionSeverity.MILD -> 0.2f
            }
        }
        
        // Deduct for warnings
        score -= warnings.size * 0.1f
        
        // Add bonus for positive preferences
        preferences.forEach { preference ->
            if (preference.preference == PreferenceLevel.LOVE || preference.preference == PreferenceLevel.LIKE) {
                val matchingTags = recipeTags.filter { tag ->
                    tag.value.contains(preference.item, ignoreCase = true)
                }
                if (matchingTags.isNotEmpty()) {
                    score += when (preference.preference) {
                        PreferenceLevel.LOVE -> 0.2f
                        PreferenceLevel.LIKE -> 0.1f
                        else -> 0f
                    }
                }
            }
        }
        
        return maxOf(0f, minOf(1f, score))
    }
}

data class DietaryTag(
    val type: DietaryTagType,
    val value: String,
    val confidence: Float
)