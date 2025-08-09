package com.beaconledger.welltrack.ingredients

import com.beaconledger.welltrack.data.database.dao.*
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.data.repository.IngredientPreferenceRepositoryImpl
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IngredientPreferenceRepositoryImplTest {
    
    @Mock
    private lateinit var ingredientPreferenceDao: IngredientPreferenceDao
    
    @Mock
    private lateinit var pantryDao: PantryDao
    
    @Mock
    private lateinit var ingredientUsageDao: IngredientUsageDao
    
    private lateinit var repository: IngredientPreferenceRepositoryImpl
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = IngredientPreferenceRepositoryImpl(
            ingredientPreferenceDao,
            pantryDao,
            ingredientUsageDao
        )
    }
    
    @Test
    fun `savePreference should create and insert preference successfully`() = runTest {
        // Given
        val userId = "user123"
        val request = IngredientPreferenceRequest(
            ingredientName = "Chicken",
            preferenceType = PreferenceType.PREFERRED,
            priority = 8,
            notes = "Love grilled chicken"
        )
        
        // When
        val result = repository.savePreference(userId, request)
        
        // Then
        assertTrue(result.isSuccess)
        verify(ingredientPreferenceDao).insertPreference(any())
    }
    
    @Test
    fun `savePantryItem should create and insert pantry item successfully`() = runTest {
        // Given
        val userId = "user123"
        val request = PantryItemRequest(
            ingredientName = "Tomatoes",
            quantity = 5.0,
            unit = "pieces",
            category = IngredientCategory.VEGETABLES,
            expiryDate = "2024-02-15",
            location = "Fridge"
        )
        
        // When
        val result = repository.savePantryItem(userId, request)
        
        // Then
        assertTrue(result.isSuccess)
        verify(pantryDao).insertPantryItem(any())
    }
    
    @Test
    fun `recordIngredientUsage should create usage history and reduce pantry quantity`() = runTest {
        // Given
        val userId = "user123"
        val ingredientName = "Onions"
        val quantityUsed = 2.0
        val unit = "pieces"
        val usageType = UsageType.RECIPE
        val recipeId = "recipe123"
        
        val existingPantryItem = PantryItem(
            id = "pantry123",
            userId = userId,
            ingredientName = ingredientName,
            quantity = 10.0,
            unit = unit,
            category = IngredientCategory.VEGETABLES
        )
        
        whenever(pantryDao.getPantryItem(userId, ingredientName)).thenReturn(existingPantryItem)
        
        // When
        val result = repository.recordIngredientUsage(
            userId, ingredientName, quantityUsed, unit, usageType, recipeId
        )
        
        // Then
        assertTrue(result.isSuccess)
        verify(ingredientUsageDao).insertUsageHistory(any())
        verify(pantryDao).reduceQuantity(userId, ingredientName, quantityUsed)
        verify(pantryDao).updateLowStockStatus(userId)
    }
    
    @Test
    fun `getIngredientSuggestions should return combined preferred and frequently used ingredients`() = runTest {
        // Given
        val userId = "user123"
        val preferredIngredients = listOf(
            IngredientPreference(
                id = "pref1",
                userId = userId,
                ingredientName = "Chicken",
                preferenceType = PreferenceType.PREFERRED,
                priority = 8
            )
        )
        
        val usageStats = listOf(
            IngredientUsageStats(
                ingredientName = "Beef",
                usageCount = 5,
                lastUsed = "2024-01-15"
            )
        )
        
        whenever(ingredientPreferenceDao.getPreferredIngredients(userId)).thenReturn(preferredIngredients)
        whenever(ingredientUsageDao.getMostUsedIngredients(userId, any())).thenReturn(usageStats)
        whenever(pantryDao.getPantryItem(userId, "Chicken")).thenReturn(null)
        whenever(pantryDao.getPantryItem(userId, "Beef")).thenReturn(null)
        whenever(ingredientUsageDao.getIngredientUsageStats(userId, "Chicken")).thenReturn(null)
        whenever(ingredientPreferenceDao.getPreferenceForIngredient(userId, "Beef")).thenReturn(null)
        
        // When
        val suggestions = repository.getIngredientSuggestions(userId, 10)
        
        // Then
        assertEquals(2, suggestions.size)
        assertTrue(suggestions.any { it.ingredientName == "Chicken" && it.isPreferred })
        assertTrue(suggestions.any { it.ingredientName == "Beef" && !it.isPreferred })
    }
    
    @Test
    fun `deletePreference should remove preference for ingredient`() = runTest {
        // Given
        val userId = "user123"
        val ingredientName = "Mushrooms"
        
        // When
        val result = repository.deletePreference(userId, ingredientName)
        
        // Then
        assertTrue(result.isSuccess)
        verify(ingredientPreferenceDao).deletePreferenceForIngredient(userId, ingredientName)
    }
    
    @Test
    fun `updatePantryItem should update item with new timestamp`() = runTest {
        // Given
        val pantryItem = PantryItem(
            id = "pantry123",
            userId = "user123",
            ingredientName = "Rice",
            quantity = 2.0,
            unit = "kg",
            category = IngredientCategory.GRAINS
        )
        
        // When
        val result = repository.updatePantryItem(pantryItem)
        
        // Then
        assertTrue(result.isSuccess)
        verify(pantryDao).updatePantryItem(any())
    }
}