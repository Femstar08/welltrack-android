package com.beaconledger.welltrack.domain.usecase

import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.repository.PantryRepository
import com.beaconledger.welltrack.domain.repository.ProductInfo
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class PantryUseCaseTest {
    
    private lateinit var pantryRepository: PantryRepository
    private lateinit var pantryUseCase: PantryUseCase
    
    private val testUserId = "test-user-id"
    
    @Before
    fun setup() {
        pantryRepository = mockk()
        pantryUseCase = PantryUseCase(pantryRepository)
    }
    
    @Test
    fun `getPantryItemsForUser returns items from repository`() = runTest {
        // Given
        val testItems = listOf(
            createTestPantryItem("Milk", 1.0, "L"),
            createTestPantryItem("Bread", 2.0, "loaves")
        )
        every { pantryRepository.getPantryItemsForUser(testUserId) } returns flowOf(testItems)
        
        // When
        val result = pantryUseCase.getPantryItemsForUser(testUserId)
        
        // Then
        result.collect { items ->
            assertEquals(2, items.size)
            assertEquals("Milk", items[0].ingredientName)
            assertEquals("Bread", items[1].ingredientName)
        }
        
        verify { pantryRepository.getPantryItemsForUser(testUserId) }
    }
    
    @Test
    fun `addPantryItem calls repository with correct request`() = runTest {
        // Given
        val request = PantryItemRequest(
            ingredientName = "Tomatoes",
            quantity = 5.0,
            unit = "pieces",
            category = IngredientCategory.VEGETABLES
        )
        val expectedItem = createTestPantryItem("Tomatoes", 5.0, "pieces")
        coEvery { pantryRepository.addPantryItem(testUserId, request) } returns Result.success(expectedItem)
        
        // When
        val result = pantryUseCase.addPantryItem(testUserId, request)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals("Tomatoes", result.getOrNull()?.ingredientName)
        
        coVerify { pantryRepository.addPantryItem(testUserId, request) }
    }
    
    @Test
    fun `addPantryItemByBarcode calls repository and returns success`() = runTest {
        // Given
        val barcode = "1234567890123"
        val expectedItem = createTestPantryItem("Test Product", 1.0, "g")
        coEvery { pantryRepository.addPantryItemByBarcode(testUserId, barcode) } returns Result.success(expectedItem)
        
        // When
        val result = pantryUseCase.addPantryItemByBarcode(testUserId, barcode)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals("Test Product", result.getOrNull()?.ingredientName)
        
        coVerify { pantryRepository.addPantryItemByBarcode(testUserId, barcode) }
    }
    
    @Test
    fun `getProductInfoByBarcode returns product info`() = runTest {
        // Given
        val barcode = "1234567890123"
        val productInfo = ProductInfo(
            name = "Test Product",
            barcode = barcode,
            category = IngredientCategory.OTHER,
            defaultUnit = "g"
        )
        coEvery { pantryRepository.getProductInfoByBarcode(barcode) } returns Result.success(productInfo)
        
        // When
        val result = pantryUseCase.getProductInfoByBarcode(barcode)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals("Test Product", result.getOrNull()?.name)
        assertEquals(barcode, result.getOrNull()?.barcode)
        
        coVerify { pantryRepository.getProductInfoByBarcode(barcode) }
    }
    
    @Test
    fun `updateQuantity calls repository with correct parameters`() = runTest {
        // Given
        val ingredientName = "Milk"
        val newQuantity = 2.5
        coEvery { pantryRepository.updateQuantity(testUserId, ingredientName, newQuantity) } returns Result.success(Unit)
        
        // When
        val result = pantryUseCase.updateQuantity(testUserId, ingredientName, newQuantity)
        
        // Then
        assertTrue(result.isSuccess)
        
        coVerify { pantryRepository.updateQuantity(testUserId, ingredientName, newQuantity) }
    }
    
    @Test
    fun `recordIngredientUsage creates usage history and calls repository`() = runTest {
        // Given
        val ingredientName = "Flour"
        val quantityUsed = 200.0
        val unit = "g"
        val recipeId = "recipe-123"
        val usageType = UsageType.RECIPE
        
        coEvery { 
            pantryRepository.recordIngredientUsage(testUserId, any<IngredientUsageHistory>()) 
        } returns Result.success(Unit)
        
        // When
        val result = pantryUseCase.recordIngredientUsage(
            userId = testUserId,
            ingredientName = ingredientName,
            quantityUsed = quantityUsed,
            unit = unit,
            recipeId = recipeId,
            usageType = usageType
        )
        
        // Then
        assertTrue(result.isSuccess)
        
        coVerify { 
            pantryRepository.recordIngredientUsage(testUserId, match<IngredientUsageHistory> { usage ->
                usage.ingredientName == ingredientName &&
                usage.quantityUsed == quantityUsed &&
                usage.unit == unit &&
                usage.recipeId == recipeId &&
                usage.usageType == usageType
            })
        }
    }
    
    @Test
    fun `getPantryAlerts combines all alert types`() = runTest {
        // Given
        val lowStockItems = listOf(createTestPantryItem("Sugar", 0.1, "kg", isLowStock = true))
        val expiringItems = listOf(createTestPantryItem("Milk", 1.0, "L", expiryDate = "2024-01-15"))
        val expiredItems = listOf(createTestPantryItem("Bread", 1.0, "loaf", expiryDate = "2024-01-01"))
        
        every { pantryRepository.getLowStockItems(testUserId) } returns flowOf(lowStockItems)
        every { pantryRepository.getExpiringItems(testUserId) } returns flowOf(expiringItems)
        every { pantryRepository.getExpiredItems(testUserId) } returns flowOf(expiredItems)
        
        // When
        val result = pantryUseCase.getPantryAlerts(testUserId)
        
        // Then
        result.collect { alertsData ->
            assertEquals(1, alertsData.lowStockItems.size)
            assertEquals(1, alertsData.expiringItems.size)
            assertEquals(1, alertsData.expiredItems.size)
            assertEquals(3, alertsData.totalAlerts)
        }
        
        verify { pantryRepository.getLowStockItems(testUserId) }
        verify { pantryRepository.getExpiringItems(testUserId) }
        verify { pantryRepository.getExpiredItems(testUserId) }
    }
    
    @Test
    fun `searchPantryItems returns filtered results`() = runTest {
        // Given
        val query = "mil"
        val searchResults = listOf(
            createTestPantryItem("Milk", 1.0, "L"),
            createTestPantryItem("Almond Milk", 0.5, "L")
        )
        coEvery { pantryRepository.searchPantryItems(testUserId, query, 10) } returns searchResults
        
        // When
        val result = pantryUseCase.searchPantryItems(testUserId, query)
        
        // Then
        assertEquals(2, result.size)
        assertTrue(result.all { it.ingredientName.contains("Milk", ignoreCase = true) })
        
        coVerify { pantryRepository.searchPantryItems(testUserId, query, 10) }
    }
    
    @Test
    fun `cleanupExpiredItems returns count of removed items`() = runTest {
        // Given
        val removedCount = 3
        coEvery { pantryRepository.cleanupExpiredItems(testUserId) } returns Result.success(removedCount)
        
        // When
        val result = pantryUseCase.cleanupExpiredItems(testUserId)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(removedCount, result.getOrNull())
        
        coVerify { pantryRepository.cleanupExpiredItems(testUserId) }
    }
    
    private fun createTestPantryItem(
        name: String,
        quantity: Double,
        unit: String,
        isLowStock: Boolean = false,
        expiryDate: String? = null
    ): PantryItem {
        return PantryItem(
            id = "test-id-${name.lowercase()}",
            userId = testUserId,
            ingredientName = name,
            quantity = quantity,
            unit = unit,
            category = IngredientCategory.OTHER,
            isLowStock = isLowStock,
            expiryDate = expiryDate
        )
    }
}