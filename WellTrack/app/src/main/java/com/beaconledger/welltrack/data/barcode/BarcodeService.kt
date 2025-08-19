package com.beaconledger.welltrack.data.barcode

import com.beaconledger.welltrack.data.model.IngredientCategory
import com.beaconledger.welltrack.data.model.SupplementCategory
import com.beaconledger.welltrack.data.model.SupplementNutrition
import com.beaconledger.welltrack.domain.repository.ProductInfo
import javax.inject.Inject
import javax.inject.Singleton

interface BarcodeService {
    suspend fun getProductInfo(barcode: String): ProductInfo?
    suspend fun getSupplementInfo(barcode: String): SupplementInfo?
    suspend fun scanBarcode(imageData: ByteArray): String?
}

@Singleton
class BarcodeServiceImpl @Inject constructor(
    private val openFoodFactsApi: OpenFoodFactsApi,
    private val barcodeScanner: BarcodeScanner
) : BarcodeService {
    
    override suspend fun getProductInfo(barcode: String): ProductInfo? {
        return try {
            // Try OpenFoodFacts API first
            val product = openFoodFactsApi.getProduct(barcode)
            if (product != null) {
                return ProductInfo(
                    name = product.productName ?: "Unknown Product",
                    barcode = barcode,
                    category = mapToIngredientCategory(product.categories),
                    defaultUnit = product.servingUnit ?: "g",
                    nutritionInfo = product.nutriments,
                    brand = product.brands,
                    imageUrl = product.imageUrl
                )
            }
            
            // Fallback to local database or manual entry
            getLocalProductInfo(barcode)
        } catch (e: Exception) {
            // Return basic product info for manual completion
            ProductInfo(
                name = "Product $barcode",
                barcode = barcode,
                category = IngredientCategory.OTHER,
                defaultUnit = "g"
            )
        }
    }
    
    override suspend fun getSupplementInfo(barcode: String): SupplementInfo? {
        return try {
            // Try OpenFoodFacts API first for supplement data
            val product = openFoodFactsApi.getProduct(barcode)
            if (product != null && isSupplementProduct(product.categories)) {
                return SupplementInfo(
                    name = product.productName ?: "Unknown Supplement",
                    brand = product.brands,
                    description = null,
                    servingSize = "1",
                    servingUnit = product.servingUnit ?: "capsule",
                    category = mapToSupplementCategory(product.categories),
                    nutrition = mapToSupplementNutrition(product.nutriments),
                    imageUrl = product.imageUrl
                )
            }
            
            // Fallback to basic supplement info
            SupplementInfo(
                name = "Supplement $barcode",
                brand = null,
                description = null,
                servingSize = "1",
                servingUnit = "capsule",
                category = SupplementCategory.OTHER,
                nutrition = SupplementNutrition(),
                imageUrl = null
            )
        } catch (e: Exception) {
            // Return basic supplement info for manual completion
            SupplementInfo(
                name = "Supplement $barcode",
                brand = null,
                description = null,
                servingSize = "1",
                servingUnit = "capsule",
                category = SupplementCategory.OTHER,
                nutrition = SupplementNutrition(),
                imageUrl = null
            )
        }
    }

    override suspend fun scanBarcode(imageData: ByteArray): String? {
        return barcodeScanner.scanFromImage(imageData)
    }
    
    private suspend fun getLocalProductInfo(barcode: String): ProductInfo? {
        // This could be expanded to include a local database of common products
        return null
    }
    
    private fun isSupplementProduct(categories: String?): Boolean {
        if (categories == null) return false
        val categoryLower = categories.lowercase()
        return categoryLower.contains("supplement") || 
               categoryLower.contains("vitamin") || 
               categoryLower.contains("mineral") ||
               categoryLower.contains("protein powder") ||
               categoryLower.contains("dietary supplement")
    }
    
    private fun mapToSupplementCategory(categories: String?): SupplementCategory {
        if (categories == null) return SupplementCategory.OTHER
        
        val categoryLower = categories.lowercase()
        return when {
            categoryLower.contains("vitamin") -> SupplementCategory.VITAMIN
            categoryLower.contains("mineral") -> SupplementCategory.MINERAL
            categoryLower.contains("protein") -> SupplementCategory.PROTEIN
            categoryLower.contains("amino") -> SupplementCategory.AMINO_ACID
            categoryLower.contains("herbal") || categoryLower.contains("herb") -> SupplementCategory.HERBAL
            categoryLower.contains("probiotic") -> SupplementCategory.PROBIOTIC
            categoryLower.contains("omega") || categoryLower.contains("fish oil") -> SupplementCategory.OMEGA_3
            categoryLower.contains("pre-workout") || categoryLower.contains("pre workout") -> SupplementCategory.PRE_WORKOUT
            categoryLower.contains("post-workout") || categoryLower.contains("post workout") -> SupplementCategory.POST_WORKOUT
            categoryLower.contains("digestive") -> SupplementCategory.DIGESTIVE
            categoryLower.contains("immune") -> SupplementCategory.IMMUNE
            categoryLower.contains("energy") -> SupplementCategory.ENERGY
            categoryLower.contains("sleep") || categoryLower.contains("melatonin") -> SupplementCategory.SLEEP
            categoryLower.contains("joint") -> SupplementCategory.JOINT
            else -> SupplementCategory.OTHER
        }
    }
    
    private fun mapToSupplementNutrition(nutriments: Map<String, Double>?): SupplementNutrition {
        if (nutriments == null) return SupplementNutrition()
        
        return SupplementNutrition(
            calories = nutriments["energy-kcal"],
            protein = nutriments["proteins"],
            carbs = nutriments["carbohydrates"],
            fat = nutriments["fat"],
            fiber = nutriments["fiber"],
            sugar = nutriments["sugars"],
            sodium = nutriments["sodium"],
            potassium = nutriments["potassium"],
            calcium = nutriments["calcium"],
            iron = nutriments["iron"],
            vitaminD = nutriments["vitamin-d"],
            vitaminB12 = nutriments["vitamin-b12"],
            vitaminC = nutriments["vitamin-c"],
            magnesium = nutriments["magnesium"],
            zinc = nutriments["zinc"],
            omega3 = nutriments["omega-3-fat"]
        )
    }

    private fun mapToIngredientCategory(categories: String?): IngredientCategory {
        if (categories == null) return IngredientCategory.OTHER
        
        val categoryLower = categories.lowercase()
        return when {
            categoryLower.contains("meat") || categoryLower.contains("poultry") -> IngredientCategory.PROTEIN
            categoryLower.contains("fish") || categoryLower.contains("seafood") -> IngredientCategory.PROTEIN
            categoryLower.contains("dairy") || categoryLower.contains("milk") || categoryLower.contains("cheese") -> IngredientCategory.DAIRY
            categoryLower.contains("vegetable") || categoryLower.contains("produce") -> IngredientCategory.VEGETABLES
            categoryLower.contains("fruit") -> IngredientCategory.FRUITS
            categoryLower.contains("grain") || categoryLower.contains("bread") || categoryLower.contains("cereal") -> IngredientCategory.GRAINS
            categoryLower.contains("spice") || categoryLower.contains("herb") -> IngredientCategory.SPICES
            categoryLower.contains("oil") || categoryLower.contains("fat") -> IngredientCategory.OILS
            categoryLower.contains("beverage") || categoryLower.contains("drink") -> IngredientCategory.BEVERAGES
            else -> IngredientCategory.OTHER
        }
    }
}

// OpenFoodFacts API integration
interface OpenFoodFactsApi {
    suspend fun getProduct(barcode: String): OpenFoodFactsProduct?
}

data class OpenFoodFactsProduct(
    val productName: String?,
    val brands: String?,
    val categories: String?,
    val servingUnit: String?,
    val nutriments: Map<String, Double>?,
    val imageUrl: String?
)

data class SupplementInfo(
    val name: String,
    val brand: String?,
    val description: String?,
    val servingSize: String,
    val servingUnit: String,
    val category: SupplementCategory,
    val nutrition: SupplementNutrition,
    val imageUrl: String?
)

// Barcode scanner using ML Kit
interface BarcodeScanner {
    suspend fun scanFromImage(imageData: ByteArray): String?
    suspend fun scanFromCamera(): String?
}

@Singleton
class MLKitBarcodeScanner @Inject constructor() : BarcodeScanner {
    
    override suspend fun scanFromImage(imageData: ByteArray): String? {
        return try {
            // For now, return a mock barcode for testing
            // In a real implementation, this would use ML Kit:
            // val image = InputImage.fromByteArray(imageData, width, height, rotation, InputImage.IMAGE_FORMAT_NV21)
            // val scanner = BarcodeScanning.getClient()
            // val result = scanner.process(image).await()
            // return result.firstOrNull()?.rawValue
            
            // Mock implementation for testing
            if (imageData.isNotEmpty()) {
                "1234567890123" // Mock EAN-13 barcode
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun scanFromCamera(): String? {
        // This would integrate with CameraX and ML Kit
        // For now, return a mock barcode for testing
        return "1234567890123"
    }
}