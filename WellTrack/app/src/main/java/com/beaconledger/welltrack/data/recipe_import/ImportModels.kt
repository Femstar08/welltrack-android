package com.beaconledger.welltrack.data.recipe_import

import java.time.LocalDateTime

/**
 * Progress state for recipe import operations
 */
data class ImportProgress(
    val currentStep: ImportStep = ImportStep.INITIALIZING,
    val progress: Float = 0f,
    val message: String = "",
    val isComplete: Boolean = false,
    val hasError: Boolean = false,
    val errorMessage: String? = null,
    val source: ImportSource? = null,
    val estimatedTimeRemaining: Long? = null, // in seconds
    val itemsProcessed: Int = 0,
    val totalItems: Int = 0
)

/**
 * Steps in the import process
 */
enum class ImportStep(val displayName: String) {
    INITIALIZING("Initializing"),
    DOWNLOADING("Downloading content"),
    PARSING("Parsing recipe"),
    EXTRACTING("Extracting ingredients"),
    VALIDATING("Validating data"),
    SAVING("Saving recipe"),
    COMPLETE("Complete"),
    ERROR("Error occurred")
}

/**
 * Source of recipe import
 */
enum class ImportSource(val displayName: String) {
    URL("Web URL"),
    PHOTO_OCR("Photo/OCR"),
    MANUAL_ENTRY("Manual Entry"),
    FILE_UPLOAD("File Upload"),
    CLIPBOARD("Clipboard"),
    BARCODE("Barcode Scan")
}

/**
 * Result of recipe import operation
 */
sealed class ImportResult {
    data class Success(
        val recipeId: String,
        val recipeName: String,
        val warnings: List<String> = emptyList()
    ) : ImportResult()

    data class Error(
        val errorMessage: String,
        val cause: Throwable? = null,
        val canRetry: Boolean = true
    ) : ImportResult()

    data class Cancelled(
        val reason: String = "User cancelled"
    ) : ImportResult()
}

/**
 * Configuration for import operations
 */
data class ImportConfig(
    val source: ImportSource,
    val autoSave: Boolean = true,
    val validateNutrition: Boolean = true,
    val timeout: Long = 30000, // milliseconds
    val retryAttempts: Int = 3
)