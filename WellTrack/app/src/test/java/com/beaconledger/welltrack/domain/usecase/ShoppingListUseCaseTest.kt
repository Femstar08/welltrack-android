package com.beaconledger.welltrack.domain.usecase

import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.repository.ShoppingListRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class ShoppingListUseCaseTest {
    
    private lateinit var shoppingListRepository: ShoppingListRepository
    private lateinit var shoppingListUseCase: ShoppingListUseCase
    
    @Before
    fun setup() {
        shoppingListRepository = mockk()
        shoppingListUseCase = ShoppingListUseCase(shoppingListRepository)
    }
    
    @Test
    fun `createShoppingList should validate input and call repository`() = runTest {
        // Given
        val userId = "user123"
        val request = ShoppingListCreateRequest(
            name = "Weekly Groceries",
            description = "Groceries for this week"
        )
        val expectedId = "shopping_list_123"
        
        coEvery { 
            shoppingListRepository.createShoppingList(request, userId) 
        } returns Result.success(expectedId)
        
        // When
        val result = shoppingListUseCase.createShoppingList(request, userId)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedId, result.getOrNull())
        coVerify { shoppingListRepository.createShoppingList(request, userId) }
    }
    
    @Test
    fun `addShoppingListItem should validate input before calling repository`() = runTest {
        // Given
        val shoppingListId = "list123"
        val validRequest = ShoppingListItemCreateRequest(
            name = "Apples",
            quantity = 2.0,
            unit = "lbs",
            category = IngredientCategory.FRUITS
        )
        val expectedId = "item123"
        
        coEvery { 
            shoppingListRepository.addShoppingListItem(shoppingListId, validRequest) 
        } returns Result.success(expectedId)
        
        // When
        val result = shoppingListUseCase.addShoppingListItem(shoppingListId, validRequest)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedId, result.getOrNull())
        coVerify { shoppingListRepository.addShoppingListItem(shoppingListId, validRequest) }
    }
    
    @Test
    fun `addShoppingListItem should fail validation for empty name`() = runTest {
        // Given
        val shoppingListId = "list123"
        val invalidRequest = ShoppingListItemCreateRequest(
            name = "",
            quantity = 2.0,
            unit = "lbs"
        )
        
        // When
        val result = shoppingListUseCase.addShoppingListItem(shoppingListId, invalidRequest)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals("Item name cannot be empty", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { shoppingListRepository.addShoppingListItem(any(), any()) }
    }
    
    @Test
    fun `addShoppingListItem should fail validation for zero quantity`() = runTest {
        // Given
        val shoppingListId = "list123"
        val invalidRequest = ShoppingListItemCreateRequest(
            name = "Apples",
            quantity = 0.0,
            unit = "lbs"
        )
        
        // When
        val result = shoppingListUseCase.addShoppingListItem(shoppingListId, invalidRequest)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals("Quantity must be greater than 0", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { shoppingListRepository.addShoppingListItem(any(), any()) }
    }
    
    @Test
    fun `generateShoppingListFromMealPlan should create proper request`() = runTest {
        // Given
        val userId = "user123"
        val mealPlanId = "plan123"
        val name = "Generated List"
        val expectedResult = ShoppingListGenerationResult(
            success = true,
            shoppingList = createMockShoppingListWithItems()
        )
        
        coEvery { 
            shoppingListRepository.generateShoppingListFromMealPlan(any()) 
        } returns Result.success(expectedResult)
        
        // When
        val result = shoppingListUseCase.generateShoppingListFromMealPlan(
            userId = userId,
            mealPlanId = mealPlanId,
            name = name,
            includeExistingPantryItems = true,
            consolidateSimilarItems = false,
            excludeCategories = listOf(IngredientCategory.SPICES)
        )
        
        // Then
        assertTrue(result.isSuccess)
        val generationResult = result.getOrNull()
        assertNotNull(generationResult)
        assertTrue(generationResult!!.success)
        
        coVerify { 
            shoppingListRepository.generateShoppingListFromMealPlan(
                match { request ->
                    request.userId == userId &&
                    request.mealPlanId == mealPlanId &&
                    request.name == name &&
                    request.includeExistingPantryItems == true &&
                    request.consolidateSimilarItems == false &&
                    request.excludeCategories.contains(IngredientCategory.SPICES)
                }
            ) 
        }
    }
    
    @Test
    fun `getShoppingListsByUser should return flow from repository`() = runTest {
        // Given
        val userId = "user123"
        val expectedLists = listOf(createMockShoppingList())
        
        coEvery { 
            shoppingListRepository.getShoppingListsByUser(userId) 
        } returns flowOf(expectedLists)
        
        // When
        val result = shoppingListUseCase.getShoppingListsByUser(userId)
        
        // Then
        result.collect { lists ->
            assertEquals(expectedLists, lists)
        }
        coVerify { shoppingListRepository.getShoppingListsByUser(userId) }
    }
    
    private fun createMockShoppingList(): ShoppingList {
        return ShoppingList(
            id = "list123",
            userId = "user123",
            name = "Test List",
            description = "Test Description"
        )
    }
    
    private fun createMockShoppingListWithItems(): ShoppingListWithItems {
        val shoppingList = createMockShoppingList()
        val items = listOf(
            ShoppingListItem(
                id = "item1",
                shoppingListId = "list123",
                name = "Apples",
                quantity = 2.0,
                unit = "lbs",
                category = IngredientCategory.FRUITS
            )
        )
        
        return ShoppingListWithItems(
            shoppingList = shoppingList,
            items = items
        )
    }
}