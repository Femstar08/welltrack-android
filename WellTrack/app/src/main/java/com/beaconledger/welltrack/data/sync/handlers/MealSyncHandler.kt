package com.beaconledger.welltrack.data.sync.handlers

import com.beaconledger.welltrack.data.sync.EntitySyncHandler
import com.beaconledger.welltrack.data.model.Meal
import com.beaconledger.welltrack.data.database.dao.MealDao
import com.beaconledger.welltrack.data.remote.SupabaseClient
import com.beaconledger.welltrack.data.security.EncryptionManager
import com.beaconledger.welltrack.data.security.SensitiveFieldsConfig
import java.time.ZoneOffset
import javax.inject.Inject

/**
 * Sync handler for Meal entities
 */
class MealSyncHandler @Inject constructor(
    private val mealDao: MealDao,
    private val supabaseClient: SupabaseClient,
    private val encryptionManager: EncryptionManager
) : EntitySyncHandler<Meal> {
    
    override suspend fun getLocalEntity(entityId: String): Meal? {
        return mealDao.getMealById(entityId)
    }
    
    override suspend fun getCloudEntity(entityId: String): Meal? {
        return try {
            supabaseClient.getMeal(entityId)
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun saveLocalEntity(entity: Meal) {
        mealDao.insertMeal(entity)
    }
    
    override suspend fun uploadToCloud(entity: Meal): Meal {
        return supabaseClient.upsertMeal(entity)
    }
    
    override suspend fun deleteLocalEntity(entityId: String) {
        mealDao.deleteMealById(entityId)
    }
    
    override suspend fun deleteCloudEntity(entityId: String) {
        supabaseClient.deleteMeal(entityId)
    }
    
    override fun getEntityVersion(entity: Meal): Long {
        return entity.timestamp.toEpochSecond(ZoneOffset.UTC)
    }
    
    override suspend fun encryptSensitiveData(entity: Meal): Meal {
        val entityMap = mapOf(
            "id" to entity.id,
            "userId" to entity.userId,
            "recipeId" to entity.recipeId,
            "timestamp" to entity.timestamp,
            "mealType" to entity.mealType,
            "portions" to entity.portions,
            "nutritionInfo" to entity.nutritionInfo,
            "score" to entity.score,
            "status" to entity.status,
            "notes" to entity.notes
        )
        
        val encryptedMap = encryptionManager.encryptSensitiveFields(
            entityMap,
            SensitiveFieldsConfig.MEAL_FIELDS
        )
        
        return entity.copy(
            notes = encryptedMap["notes"] as? String
        )
    }
    
    override suspend fun decryptSensitiveData(entity: Meal): Meal {
        val entityMap = mapOf(
            "id" to entity.id,
            "userId" to entity.userId,
            "recipeId" to entity.recipeId,
            "timestamp" to entity.timestamp,
            "mealType" to entity.mealType,
            "portions" to entity.portions,
            "nutritionInfo" to entity.nutritionInfo,
            "score" to entity.score,
            "status" to entity.status,
            "notes" to entity.notes
        )
        
        val decryptedMap = encryptionManager.decryptSensitiveFields(
            entityMap,
            SensitiveFieldsConfig.MEAL_FIELDS
        )
        
        return entity.copy(
            notes = decryptedMap["notes"] as? String
        )
    }
    
    override fun getEntityType(): String = "Meal"
}