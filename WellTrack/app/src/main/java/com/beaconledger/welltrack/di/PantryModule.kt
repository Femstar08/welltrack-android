package com.beaconledger.welltrack.di

import com.beaconledger.welltrack.data.barcode.BarcodeService
import com.beaconledger.welltrack.data.barcode.BarcodeServiceImpl
import com.beaconledger.welltrack.data.barcode.BarcodeScanner
import com.beaconledger.welltrack.data.barcode.MLKitBarcodeScanner
import com.beaconledger.welltrack.data.barcode.OpenFoodFactsApi
import com.beaconledger.welltrack.data.database.dao.IngredientUsageDao
import com.beaconledger.welltrack.data.database.dao.PantryDao
import com.beaconledger.welltrack.data.database.dao.RecipeDao
import com.beaconledger.welltrack.data.database.WellTrackDatabase
import com.beaconledger.welltrack.data.repository.PantryRepositoryImpl
import com.beaconledger.welltrack.domain.repository.PantryRepository
import com.beaconledger.welltrack.domain.usecase.PantryUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PantryModule {
    
    @Provides
    @Singleton
    fun providePantryDao(database: WellTrackDatabase): PantryDao {
        return database.pantryDao()
    }
    
    @Provides
    @Singleton
    fun provideIngredientUsageDao(database: WellTrackDatabase): IngredientUsageDao {
        return database.ingredientUsageDao()
    }
    
    @Provides
    @Singleton
    fun provideBarcodeScanner(): BarcodeScanner {
        return MLKitBarcodeScanner()
    }
    
    @Provides
    @Singleton
    fun provideOpenFoodFactsApi(retrofit: Retrofit): OpenFoodFactsApi {
        return OpenFoodFactsApiImpl()
    }
    
    @Provides
    @Singleton
    fun provideBarcodeService(
        openFoodFactsApi: OpenFoodFactsApi,
        barcodeScanner: BarcodeScanner
    ): BarcodeService {
        return BarcodeServiceImpl(openFoodFactsApi, barcodeScanner)
    }
    
    @Provides
    @Singleton
    fun providePantryRepository(
        pantryDao: PantryDao,
        ingredientUsageDao: IngredientUsageDao,
        recipeDao: RecipeDao,
        barcodeService: BarcodeService
    ): PantryRepository {
        return PantryRepositoryImpl(pantryDao, ingredientUsageDao, recipeDao, barcodeService)
    }
    
    @Provides
    @Singleton
    fun providePantryUseCase(
        pantryRepository: PantryRepository
    ): PantryUseCase {
        return PantryUseCase(pantryRepository)
    }
}

// Implementation for OpenFoodFacts API
class OpenFoodFactsApiImpl : OpenFoodFactsApi {
    override suspend fun getProduct(barcode: String): com.beaconledger.welltrack.data.barcode.OpenFoodFactsProduct? {
        return try {
            // Mock implementation for testing - in real app this would make HTTP requests
            // to https://world.openfoodfacts.org/api/v0/product/{barcode}.json
            
            // Return mock data for common test barcodes
            when (barcode) {
                "1234567890123" -> com.beaconledger.welltrack.data.barcode.OpenFoodFactsProduct(
                    productName = "Test Product",
                    brands = "Test Brand",
                    categories = "Dairy products",
                    servingUnit = "g",
                    nutriments = mapOf(
                        "energy-kcal_100g" to 150.0,
                        "proteins_100g" to 8.0,
                        "carbohydrates_100g" to 12.0,
                        "fat_100g" to 3.5
                    ),
                    imageUrl = null
                )
                "9876543210987" -> com.beaconledger.welltrack.data.barcode.OpenFoodFactsProduct(
                    productName = "Organic Milk",
                    brands = "Organic Brand",
                    categories = "Dairy products, Milk",
                    servingUnit = "ml",
                    nutriments = mapOf(
                        "energy-kcal_100g" to 64.0,
                        "proteins_100g" to 3.2,
                        "carbohydrates_100g" to 4.8,
                        "fat_100g" to 3.6
                    ),
                    imageUrl = null
                )
                else -> null // Unknown barcode
            }
        } catch (e: Exception) {
            null
        }
    }
}