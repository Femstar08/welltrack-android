package com.beaconledger.welltrack.data.security

import android.content.Context
import com.beaconledger.welltrack.data.database.WellTrackDatabase
import androidx.room.withTransaction
import com.beaconledger.welltrack.data.remote.SupabaseClient
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureDataDeletionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: WellTrackDatabase,
    private val supabaseClient: SupabaseClient,
    private val securePreferencesManager: SecurePreferencesManager,
    private val auditLogger: AuditLogger
) {
    
    sealed class DeletionResult {
        object Success : DeletionResult()
        data class PartialSuccess(val failedOperations: List<String>) : DeletionResult()
        data class Error(val message: String) : DeletionResult()
    }
    
    suspend fun deleteAllUserData(userId: String, includeCloudData: Boolean = true): DeletionResult {
        return withContext(Dispatchers.IO) {
            val failedOperations = mutableListOf<String>()
            
            try {
                auditLogger.logDataDeletion(userId, "FULL_ACCOUNT_DELETION_STARTED")
                
                // 1. Delete local database data
                try {
                    deleteLocalUserData(userId)
                    auditLogger.logDataDeletion(userId, "LOCAL_DATA_DELETED")
                } catch (e: Exception) {
                    failedOperations.add("Local database deletion: ${e.message}")
                }
                
                // 2. Delete cloud data if requested
                if (includeCloudData) {
                    try {
                        deleteCloudUserData(userId)
                        auditLogger.logDataDeletion(userId, "CLOUD_DATA_DELETED")
                    } catch (e: Exception) {
                        failedOperations.add("Cloud data deletion: ${e.message}")
                    }
                }
                
                // 3. Delete cached files
                try {
                    deleteCachedUserFiles(userId)
                    auditLogger.logDataDeletion(userId, "CACHED_FILES_DELETED")
                } catch (e: Exception) {
                    failedOperations.add("Cached files deletion: ${e.message}")
                }
                
                // 4. Clear secure preferences
                try {
                    clearUserPreferences(userId)
                    auditLogger.logDataDeletion(userId, "PREFERENCES_CLEARED")
                } catch (e: Exception) {
                    failedOperations.add("Preferences clearing: ${e.message}")
                }
                
                // 5. Overwrite sensitive memory (best effort)
                try {
                    overwriteSensitiveMemory()
                } catch (e: Exception) {
                    // Non-critical, don't add to failed operations
                }
                
                auditLogger.logDataDeletion(userId, "FULL_ACCOUNT_DELETION_COMPLETED")
                
                return@withContext when {
                    failedOperations.isEmpty() -> DeletionResult.Success
                    failedOperations.size < 4 -> DeletionResult.PartialSuccess(failedOperations)
                    else -> DeletionResult.Error("Multiple deletion operations failed: ${failedOperations.joinToString(", ")}")
                }
                
            } catch (e: Exception) {
                auditLogger.logDataDeletion(userId, "FULL_ACCOUNT_DELETION_FAILED", e.message)
                return@withContext DeletionResult.Error("Account deletion failed: ${e.message}")
            }
        }
    }
    
    private suspend fun deleteLocalUserData(userId: String) {
        // Delete all user-related data from local database
        database.withTransaction {
            // Delete in order to respect foreign key constraints
            database.mealDao().deleteAllMealsForUser(userId)
            database.recipeDao().deleteAllRecipesForUser(userId)
            database.healthMetricDao().deleteAllHealthMetricsForUser(userId)
            database.supplementDao().deleteAllSupplementsForUser(userId)
            database.biomarkerDao().deleteAllBiomarkersForUser(userId)
            database.pantryItemDao().deleteAllPantryItemsForUser(userId)
            database.shoppingListDao().deleteAllShoppingListsForUser(userId)
            database.mealPlanDao().deleteAllMealPlansForUser(userId)
            database.notificationDao().deleteAllNotificationsForUser(userId)
            database.dailyTrackingDao().deleteAllDailyTrackingForUser(userId)
            database.macronutrientDao().deleteAllMacronutrientsForUser(userId)
            database.syncStatusDao().deleteAllSyncStatusForUser(userId)
            database.achievementDao().deleteAllAchievementsForUser(userId)
            database.costBudgetDao().deleteAllCostBudgetsForUser(userId)
            
            // Finally delete the user profile
            database.userDao().deleteUser(userId)
        }
    }
    
    private suspend fun deleteCloudUserData(userId: String) {
        try {
            // Delete user data from Supabase
            supabaseClient.deleteUserAccount(userId)
        } catch (e: Exception) {
            throw Exception("Failed to delete cloud data: ${e.message}")
        }
    }
    
    private fun deleteCachedUserFiles(userId: String) {
        val userCacheDir = File(context.cacheDir, "user_$userId")
        if (userCacheDir.exists()) {
            secureDeleteDirectory(userCacheDir)
        }
        
        val userFilesDir = File(context.filesDir, "user_$userId")
        if (userFilesDir.exists()) {
            secureDeleteDirectory(userFilesDir)
        }
        
        // Delete profile images and other user-specific files
        val profileImagesDir = File(context.filesDir, "profile_images")
        if (profileImagesDir.exists()) {
            profileImagesDir.listFiles()?.forEach { file ->
                if (file.name.contains(userId)) {
                    secureDeleteFile(file)
                }
            }
        }
    }
    
    private fun clearUserPreferences(userId: String) {
        // Clear user-specific preferences
        securePreferencesManager.removeKeysWithPrefix("user_${userId}_")
        
        // If this is the last user, clear app-wide settings
        val remainingUsers = database.userDao().getAllUsersSync()
        if (remainingUsers.isEmpty()) {
            securePreferencesManager.clearAll()
        }
    }
    
    private fun secureDeleteFile(file: File) {
        if (!file.exists()) return
        
        try {
            // Overwrite file content multiple times before deletion
            val fileSize = file.length()
            file.outputStream().use { output ->
                // First pass: write zeros
                repeat(fileSize.toInt()) { output.write(0) }
                output.flush()
            }
            
            file.outputStream().use { output ->
                // Second pass: write random data
                val random = java.security.SecureRandom()
                val buffer = ByteArray(1024)
                var remaining = fileSize
                while (remaining > 0) {
                    val toWrite = minOf(buffer.size.toLong(), remaining).toInt()
                    random.nextBytes(buffer)
                    output.write(buffer, 0, toWrite)
                    remaining -= toWrite
                }
                output.flush()
            }
            
            // Finally delete the file
            file.delete()
        } catch (e: Exception) {
            // If secure deletion fails, at least try regular deletion
            file.delete()
        }
    }
    
    private fun secureDeleteDirectory(directory: File) {
        if (!directory.exists()) return
        
        directory.walkBottomUp().forEach { file ->
            if (file.isFile) {
                secureDeleteFile(file)
            } else if (file.isDirectory) {
                file.delete()
            }
        }
    }
    
    private fun overwriteSensitiveMemory() {
        // Force garbage collection to clear sensitive data from memory
        System.gc()
        System.runFinalization()
        System.gc()
    }
    
    suspend fun deleteSpecificDataType(userId: String, dataType: DataType): DeletionResult {
        return withContext(Dispatchers.IO) {
            try {
                auditLogger.logDataDeletion(userId, "SPECIFIC_DATA_DELETION_STARTED", dataType.name)
                
                when (dataType) {
                    DataType.MEALS -> {
                        database.mealDao().deleteAllMealsForUser(userId)
                        supabaseClient.deleteMealsForUser(userId)
                    }
                    DataType.RECIPES -> {
                        database.recipeDao().deleteAllRecipesForUser(userId)
                        supabaseClient.deleteRecipesForUser(userId)
                    }
                    DataType.HEALTH_METRICS -> {
                        database.healthMetricDao().deleteAllHealthMetricsForUser(userId)
                        supabaseClient.deleteHealthMetricsForUser(userId)
                    }
                    DataType.SUPPLEMENTS -> {
                        database.supplementDao().deleteAllSupplementsForUser(userId)
                        supabaseClient.deleteSupplementsForUser(userId)
                    }
                    DataType.BIOMARKERS -> {
                        database.biomarkerDao().deleteAllBiomarkersForUser(userId)
                        supabaseClient.deleteBiomarkersForUser(userId)
                    }
                    DataType.PANTRY -> {
                        database.pantryItemDao().deleteAllPantryItemsForUser(userId)
                        supabaseClient.deletePantryItemsForUser(userId)
                    }
                }
                
                auditLogger.logDataDeletion(userId, "SPECIFIC_DATA_DELETION_COMPLETED", dataType.name)
                DeletionResult.Success
            } catch (e: Exception) {
                auditLogger.logDataDeletion(userId, "SPECIFIC_DATA_DELETION_FAILED", "${dataType.name}: ${e.message}")
                DeletionResult.Error("Failed to delete ${dataType.name}: ${e.message}")
            }
        }
    }
    
    enum class DataType {
        MEALS, RECIPES, HEALTH_METRICS, SUPPLEMENTS, BIOMARKERS, PANTRY
    }
}