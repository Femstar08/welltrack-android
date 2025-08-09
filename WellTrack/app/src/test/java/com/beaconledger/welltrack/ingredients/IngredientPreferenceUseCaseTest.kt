package com.beaconledger.welltrack.ingredients

import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.repository.IngredientPreferenceRepository
import com.beaconledger.welltrack.domain.usecase.IngredientPreferenceUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IngredientPreferenceUseCaseTest {
    
    @Mock
    private lateinit var repository: IngredientPreferenceRepository
    
    private lateinit var useCase: IngredientPreferenceUseCase
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = IngredientPreferenceUseCase(repository)
    }
    
    @Test
    fun `addPreferredIngredient should save preference with correct type and priority`() = runTest {
        // Given
        val userId = "user123"
        val ingredientName = "Salmon"
        val priority = 7
        
        whenever(repository.savePreference(any(), any())).thenReturn(Result.success("pref123"))
        
        // When
        val result = useCase.addPreferredIngredient(userId, ingredientName, priority)
        
        // Then
        assertTrue(result.isSuccess)
        verify(repository).savePreference(
            eq(userId),
            argThat { request ->
                request.ingredientName == ingredientName &&
                request.preferenceType == PreferenceType.PREFERRED &&
                request.priority == priority
            }
        )
    }
    
    @Test
    fun `addDislikedIngredient should save preference with disliked type`() = runTest {
        // Given
        val userId = "user123"
        val ingredientName = "Broccoli"
        val notes = "Too bitter for me"
        
        whenever(repository.savePreference(any(), any())).thenReturn(Result.success("pref123"))
        
        // When
        val result = useCase.addDislikedIngredient(userId, ingredientName, notes)
        
        // Then
        assertTrue(result.isSuccess)
        verify(repository).savePreference(
            eq(userId),
            argThat { request ->
                request.ingredientName == ingredientName &&
                request.preferenceType == PreferenceType.DISLIKED &&
                request.notes == notes
            }
        )
    }
    
    @Test
    fun `addAllergicIngredient should save preference with allergic type`() = runTest {
        // Given
        val userId = "user123"
        val ingredientName = "Peanuts"
        val notes = "Severe allergy"
        
        whenever(repository.savePreference(any(), any())).thenReturn(Result.success("pref123"))
        
        // When
        val result = useCase.addAllergicIngredient(userId, ingredientName, notes)
        
        // Then
        assertTrue(result.isSuccess)
        verify(repository).savePreference(
            eq(userId),
            argThat { request ->
                request.ingredientName == ingredientName &&
                request.preferenceType == PreferenceType.ALLERGIC &&
                request.notes == notes
            }
        )
    }
    
    @Test
    fun `addPantryItem should save pantry item with correct parameters`() = runTest {
        // Given
        val userId = "user123"
        val ingredientName = "Carrots"
        val quantity = 1.5
        val unit = "kg"
        val category = IngredientCategory.VEGETABLES
        val expiryDate = "2024-02-20"
        val location = "Fridge"
        
        whenever(repository.savePantryItem(any(), any())).thenReturn(Result.success("pantry123"))
        
        // When
        val result = useCase.addPantryItem(userId, ingredientName, quantity, unit, category, expiryDate, location)
        
        // Then
        assertTrue(result.isSuccess)
        verify(repository).savePantryItem(
            eq(userId),
            argThat { request ->
                request.ingredientName == ingredientName &&
                request.quantity == quantity &&
                request.unit == unit &&
                request.category == category &&
                request.expiryDate == expiryDate &&
                request.location == location
            }
        )
    }
    
    @Test
    fun `consumeIngredient should record usage history`() = runTest {
        // Given
        val userId = "user123"
        val ingredientName = "Garlic"
        val quantityUsed = 3.0
        val unit = "cloves"
        val usageType = UsageType.COOKING
        val recipeId = "recipe123"
        
        whenever(repository.recordIngredientUsage(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(Result.success("usage123"))
        
        // When
        val result = useCase.consumeIngredient(userId, ingredientName, quantityUsed, unit, usageType, recipeId)
        
        // Then
        assertTrue(result.isSuccess)
        verify(repository).recordIngredientUsage(
            userId, ingredientName, quantityUsed, unit, usageType, recipeId, null
        )
    }
    
    @Test
    fun `shouldAvoidIngredient should return true for disliked and allergic ingredients`() = runTest {
        // Given
        val userId = "user123"
        val dislikedIngredient = "Mushrooms"
        val allergicIngredient = "Shellfish"
        val neutralIngredient = "Chicken"
        
        whenever(repository.getPreferenceForIngredient(userId, dislikedIngredient))
            .thenReturn(IngredientPreference("1", userId, dislikedIngredient, PreferenceType.DISLIKED, 0))
        whenever(repository.getPreferenceForIngredient(userId, allergicIngredient))
            .thenReturn(IngredientPreference("2", userId, allergicIngredient, PreferenceType.ALLERGIC, 0))
        whenever(repository.getPreferenceForIngredient(userId, neutralIngredient))
            .thenReturn(null)
        
        // When & Then
        assertTrue(useCase.shouldAvoidIngredient(userId, dislikedIngredient))
        assertTrue(useCase.shouldAvoidIngredient(userId, allergicIngredient))
        assertFalse(useCase.shouldAvoidIngredient(userId, neutralIngredient))
    }
    
    @Test
    fun `getIngredientPreferenceScore should return correct scores based on preference type`() = runTest {
        // Given
        val userId = "user123"
        val preferredIngredient = "Chicken"
        val dislikedIngredient = "Mushrooms"
        val allergicIngredient = "Peanuts"
        val neutralIngredient = "Rice"
        
        whenever(repository.getPreferenceForIngredient(userId, preferredIngredient))
            .thenReturn(IngredientPreference("1", userId, preferredIngredient, PreferenceType.PREFERRED, 8))
        whenever(repository.getPreferenceForIngredient(userId, dislikedIngredient))
            .thenReturn(IngredientPreference("2", userId, dislikedIngredient, PreferenceType.DISLIKED, 0))
        whenever(repository.getPreferenceForIngredient(userId, allergicIngredient))
            .thenReturn(IngredientPreference("3", userId, allergicIngredient, PreferenceType.ALLERGIC, 0))
        whenever(repository.getPreferenceForIngredient(userId, neutralIngredient))
            .thenReturn(null)
        
        whenever(repository.getIngredientUsageStats(userId, any())).thenReturn(null)
        
        // When & Then
        assertEquals(80, useCase.getIngredientPreferenceScore(userId, preferredIngredient)) // 8 * 10
        assertEquals(-50, useCase.getIngredientPreferenceScore(userId, dislikedIngredient))
        assertEquals(-100, useCase.getIngredientPreferenceScore(userId, allergicIngredient))
        assertEquals(0, useCase.getIngredientPreferenceScore(userId, neutralIngredient))
    }
    
    @Test
    fun `updatePreferencePriority should update preference with new priority`() = runTest {
        // Given
        val preference = IngredientPreference(
            id = "pref123",
            userId = "user123",
            ingredientName = "Beef",
            preferenceType = PreferenceType.PREFERRED,
            priority = 5
        )
        val newPriority = 9
        
        whenever(repository.updatePreference(any())).thenReturn(Result.success(Unit))
        
        // When
        val result = useCase.updatePreferencePriority(preference, newPriority)
        
        // Then
        assertTrue(result.isSuccess)
        verify(repository).updatePreference(
            argThat { updatedPreference ->
                updatedPreference.priority == newPriority &&
                updatedPreference.id == preference.id
            }
        )
    }
}