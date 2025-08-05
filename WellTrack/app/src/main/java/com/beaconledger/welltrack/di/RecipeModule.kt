package com.beaconledger.welltrack.di

import android.content.Context
import com.beaconledger.welltrack.data.recipe_import.RecipeImportService
import com.beaconledger.welltrack.data.recipe_import.RecipeOcrParser
import com.beaconledger.welltrack.data.recipe_import.RecipeUrlParser
import com.beaconledger.welltrack.data.recipe_import.RecipeImportValidator
import com.beaconledger.welltrack.data.repository.RecipeRepositoryImpl
import com.beaconledger.welltrack.domain.repository.RecipeRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RecipeModule {

    @Binds
    @Singleton
    abstract fun bindRecipeRepository(
        recipeRepositoryImpl: RecipeRepositoryImpl
    ): RecipeRepository

    companion object {
        @Provides
        @Singleton
        fun provideRecipeUrlParser(): RecipeUrlParser {
            return RecipeUrlParser()
        }

        @Provides
        @Singleton
        fun provideRecipeOcrParser(
            @ApplicationContext context: Context
        ): RecipeOcrParser {
            return RecipeOcrParser(context)
        }

        @Provides
        @Singleton
        fun provideRecipeImportValidator(): RecipeImportValidator {
            return RecipeImportValidator()
        }

        @Provides
        @Singleton
        fun provideRecipeImportService(
            urlParser: RecipeUrlParser,
            ocrParser: RecipeOcrParser,
            validator: RecipeImportValidator,
            nutritionCalculator: com.beaconledger.welltrack.data.nutrition.NutritionCalculator
        ): RecipeImportService {
            return RecipeImportService(urlParser, ocrParser, validator, nutritionCalculator)
        }
    }
}