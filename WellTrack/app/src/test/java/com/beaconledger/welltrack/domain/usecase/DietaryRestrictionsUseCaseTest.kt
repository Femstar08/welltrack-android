package com.beaconledger.welltrack.domain.usecase

import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.repository.DietaryRestrictionsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DietaryRestrictionsUseCaseTest {
    
    private lateinit var dietaryRestrictionsRepository: DietaryRestrictionsRepository
    private lateinit var dietaryRestrictionsUseCase: DietaryRestrictionsUseCase
    
    @Before
    fun setup() {
        dietaryRestrictionsRepository = mockk()
        dietaryRestrictionsUseCase = DietaryRestrictionsUseCase(dietaryRestrictionsRepository)
    }
    
    @Test
    fun `getUserDietaryProfile should return dietary profile from repository`() = runTest {
        // Given
        val userId = "test-user"
        val expectedProfile = DietaryProfile(
            restrictions = listOf(
                UserDietaryRestriction(
                    id = "1",
                    userId = userId,
                    restrictionType = DietaryRestrictionType.VEGETARIAN,
                    severity = RestrictionSeverity.MODERATE,
                    notes = null,
                    isActive = true,
                    createdAt = "2024-01-01T00:00:00",
                    updatedAt = "2024-01-01T00:00:00"
                )
            ),
            allergies = listOf(
                UserAllergy(
                    id = "2",
                    userId = userId,
                    allergen = "Peanuts",
                    severity = AllergySeverity.SEVERE,
                    symptoms = "Hives, difficulty breathing",
                    notes = null,
                    isActive = true,
                    createdAt = "2024-01-01T00:00:00",
                    updatedAt = "2024-01-01T00:00:00"
                )
            ),
            preferences = listOf(
                UserFoodPreference(
                    id = "3",
                    userId = userId,
                    preferenceType = FoodPreferenceType.INGREDIENT,
                    item = "Tomatoes",
                    preference = PreferenceLevel.LOVE,
                    notes = null,
                    createdAt = "2024-01-01T00:00:00",
                    updatedAt = "2024-01-01T00:00:00"
                )
            )
        )
        
        coEvery { dietaryRestrictionsRepository.getUserDietaryProfile(userId) } returns flowOf(expectedProfile)
        
        // When
        val result = dietaryRestrictionsUseCase.getUserDietaryProfile(userId)
        
        // Then
        result.collect { profile ->
            assertEquals(expectedProfile, profile)
        }
        
        coVerify { dietaryRestrictionsRepository.getUserDietaryProfile(userId) }
    }
    
    @Test
    fun `addDietaryRestriction should call repository with correct parameters`() = runTest {
        // Given
        val userId = "test-user"
        val restrictionType = DietaryRestrictionType.GLUTEN_FREE
        val severity = RestrictionSeverity.STRICT
        val notes = "Celiac disease"
        val expectedId = "restriction-id"
        
        coEvery { 
            dietaryRestrictionsRepository.addDietaryRestriction(userId, any()) 
        } returns Result.success(expectedId)
        
        // When
        val result = dietaryRestrictionsUseCase.addDietaryRestriction(userId, restrictionType, severity, notes)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedId, result.getOrNull())
        
        coVerify { 
            dietaryRestrictionsRepository.addDietaryRestriction(
                userId, 
                match { request ->
                    request.restrictionType == restrictionType &&
                    request.severity == severity &&
                    request.notes == notes
                }
            ) 
        }
    }
    
    @Test
    fun `addAllergy should call repository with correct parameters`() = runTest {
        // Given
        val userId = "test-user"
        val allergen = "Shellfish"
        val severity = AllergySeverity.ANAPHYLAXIS
        val symptoms = "Severe reaction"
        val notes = "EpiPen required"
        val expectedId = "allergy-id"
        
        coEvery { 
            dietaryRestrictionsRepository.addAllergy(userId, any()) 
        } returns Result.success(expectedId)
        
        // When
        val result = dietaryRestrictionsUseCase.addAllergy(userId, allergen, severity, symptoms, notes)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedId, result.getOrNull())
        
        coVerify { 
            dietaryRestrictionsRepository.addAllergy(
                userId, 
                match { request ->
                    request.allergen == allergen &&
                    request.severity == severity &&
                    request.symptoms == symptoms &&
                    request.notes == notes
                }
            ) 
        }
    }
    
    @Test
    fun `addFoodPreference should call repository with correct parameters`() = runTest {
        // Given
        val userId = "test-user"
        val preferenceType = FoodPreferenceType.CUISINE
        val item = "Italian"
        val preference = PreferenceLevel.LOVE
        val notes = "Especially pasta dishes"
        val expectedId = "preference-id"
        
        coEvery { 
            dietaryRestrictionsRepository.addFoodPreference(userId, any()) 
        } returns Result.success(expectedId)
        
        // When
        val result = dietaryRestrictionsUseCase.addFoodPreference(userId, preferenceType, item, preference, notes)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedId, result.getOrNull())
        
        coVerify { 
            dietaryRestrictionsRepository.addFoodPreference(
                userId, 
                match { request ->
                    request.preferenceType == preferenceType &&
                    request.item == item &&
                    request.preference == preference &&
                    request.notes == notes
                }
            ) 
        }
    }
    
    @Test
    fun `setupBasicDietaryProfile should create comprehensive profile`() = runTest {
        // Given
        val userId = "test-user"
        val restrictions = listOf(DietaryRestrictionType.VEGETARIAN, DietaryRestrictionType.GLUTEN_FREE)
        val allergies = listOf("Peanuts", "Shellfish")
        val likedIngredients = listOf("Tomatoes", "Basil")
        val dislikedIngredients = listOf("Mushrooms")
        val preferredCuisines = listOf("Italian", "Mediterranean")
        
        coEvery { 
            dietaryRestrictionsRepository.updateDietaryProfile(userId, any()) 
        } returns Result.success(Unit)
        
        // When
        val result = dietaryRestrictionsUseCase.setupBasicDietaryProfile(
            userId = userId,
            restrictions = restrictions,
            allergies = allergies,
            likedIngredients = likedIngredients,
            dislikedIngredients = dislikedIngredients,
            preferredCuisines = preferredCuisines
        )
        
        // Then
        assertTrue(result.isSuccess)
        
        coVerify { 
            dietaryRestrictionsRepository.updateDietaryProfile(
                userId, 
                match { request ->
                    request.restrictions.size == 2 &&
                    request.allergies.size == 2 &&
                    request.preferences.size == 5 // 2 liked + 1 disliked + 2 cuisines
                }
            ) 
        }
    }
    
    @Test
    fun `checkRecipeCompatibility should return compatibility result`() = runTest {
        // Given
        val userId = "test-user"
        val recipeId = "recipe-123"
        val expectedCompatibility = DietaryCompatibility(
            isCompatible = true,
            violations = emptyList(),
            warnings = emptyList(),
            score = 1.0f
        )
        
        coEvery { 
            dietaryRestrictionsRepository.checkRecipeCompatibility(userId, recipeId) 
        } returns Result.success(expectedCompatibility)
        
        // When
        val result = dietaryRestrictionsUseCase.checkRecipeCompatibility(userId, recipeId)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedCompatibility, result.getOrNull())
        
        coVerify { dietaryRestrictionsRepository.checkRecipeCompatibility(userId, recipeId) }
    }
    
    @Test
    fun `getCompatibleRecipes should return filtered recipes`() = runTest {
        // Given
        val userId = "test-user"
        val minSeverity = RestrictionSeverity.MODERATE
        val expectedRecipes = listOf(
            Recipe(
                id = "recipe-1",
                name = "Vegetarian Pasta",
                prepTime = 15,
                cookTime = 20,
                servings = 4,
                instructions = "[]",
                nutritionInfo = "{}",
                sourceType = RecipeSource.MANUAL,
                sourceUrl = null,
                rating = 4.5f,
                tags = "vegetarian,pasta",
                createdAt = "2024-01-01T00:00:00",
                updatedAt = "2024-01-01T00:00:00"
            )
        )
        
        coEvery { 
            dietaryRestrictionsRepository.getCompatibleRecipes(userId, minSeverity) 
        } returns Result.success(expectedRecipes)
        
        // When
        val result = dietaryRestrictionsUseCase.getCompatibleRecipes(userId, minSeverity)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedRecipes, result.getOrNull())
        
        coVerify { dietaryRestrictionsRepository.getCompatibleRecipes(userId, minSeverity) }
    }
    
    @Test
    fun `tagRecipeWithDietaryInfo should call repository`() = runTest {
        // Given
        val recipeId = "recipe-123"
        val recipe = Recipe(
            id = recipeId,
            name = "Vegan Salad",
            prepTime = 10,
            cookTime = 0,
            servings = 2,
            instructions = "[]",
            nutritionInfo = "{}",
            sourceType = RecipeSource.MANUAL,
            sourceUrl = null,
            rating = null,
            tags = "vegan,healthy",
            createdAt = "2024-01-01T00:00:00",
            updatedAt = "2024-01-01T00:00:00"
        )
        
        coEvery { 
            dietaryRestrictionsRepository.tagRecipeWithDietaryInfo(recipeId, recipe) 
        } returns Result.success(Unit)
        
        // When
        val result = dietaryRestrictionsUseCase.tagRecipeWithDietaryInfo(recipeId, recipe)
        
        // Then
        assertTrue(result.isSuccess)
        
        coVerify { dietaryRestrictionsRepository.tagRecipeWithDietaryInfo(recipeId, recipe) }
    }
}