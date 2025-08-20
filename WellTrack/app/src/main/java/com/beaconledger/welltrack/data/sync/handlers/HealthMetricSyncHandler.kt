package com.beaconledger.welltrack.data.sync.handlers

import com.beaconledger.welltrack.data.sync.EntitySyncHandler
import com.beaconledger.welltrack.data.model.HealthMetric
import com.beaconledger.welltrack.data.database.dao.HealthMetricDao
import com.beaconledger.welltrack.data.remote.SupabaseClient
import com.beaconledger.welltrack.data.security.EncryptionManager
import com.beaconledger.welltrack.data.security.SensitiveFieldsConfig
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.time.ZoneOffset
import javax.inject.Inject

/**
 * Sync handler for HealthMetric entities
 */
class HealthMetricSyncHandler @Inject constructor(
    private val healthMetricDao: HealthMetricDao,
    private val supabaseClient: SupabaseClient,
    private val encryptionManager: EncryptionManager
) : EntitySyncHandler<HealthMetric> {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    override suspend fun getLocalEntity(entityId: String): HealthMetric? {
        return healthMetricDao.getHealthMetricById(entityId)
    }
    
    override suspend fun getCloudEntity(entityId: String): HealthMetric? {
        return try {
            supabaseClient.getHealthMetric(entityId)
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun saveLocalEntity(entity: HealthMetric) {
        healthMetricDao.insertHealthMetric(entity)
    }
    
    override suspend fun uploadToCloud(entity: HealthMetric): HealthMetric {
        return supabaseClient.upsertHealthMetric(entity)
    }
    
    override suspend fun deleteLocalEntity(entityId: String) {
        healthMetricDao.deleteHealthMetricById(entityId)
    }
    
    override suspend fun deleteCloudEntity(entityId: String) {
        supabaseClient.deleteHealthMetric(entityId)
    }
    
    override fun getEntityVersion(entity: HealthMetric): Long {
        return entity.timestamp.toEpochSecond(ZoneOffset.UTC)
    }
    
    override suspend fun encryptSensitiveData(entity: HealthMetric): HealthMetric {
        val entityMap = mapOf(
            "id" to entity.id,
            "userId" to entity.userId,
            "type" to entity.type,
            "value" to entity.value.toString(),
            "unit" to entity.unit,
            "timestamp" to entity.timestamp,
            "source" to entity.source,
            "metadata" to entity.metadata,
            "confidence" to entity.confidence,
            "isManualEntry" to entity.isManualEntry
        )
        
        val encryptedMap = encryptionManager.encryptSensitiveFields(
            entityMap,
            SensitiveFieldsConfig.HEALTH_METRIC_FIELDS
        )
        
        return entity.copy(
            value = (encryptedMap["value"] as? String)?.toDoubleOrNull() ?: entity.value,
            metadata = encryptedMap["metadata"] as? String
        )
    }
    
    override suspend fun decryptSensitiveData(entity: HealthMetric): HealthMetric {
        val entityMap = mapOf(
            "id" to entity.id,
            "userId" to entity.userId,
            "type" to entity.type,
            "value" to entity.value.toString(),
            "unit" to entity.unit,
            "timestamp" to entity.timestamp,
            "source" to entity.source,
            "metadata" to entity.metadata,
            "confidence" to entity.confidence,
            "isManualEntry" to entity.isManualEntry
        )
        
        val decryptedMap = encryptionManager.decryptSensitiveFields(
            entityMap,
            SensitiveFieldsConfig.HEALTH_METRIC_FIELDS
        )
        
        return entity.copy(
            value = (decryptedMap["value"] as? String)?.toDoubleOrNull() ?: entity.value,
            metadata = decryptedMap["metadata"] as? String
        )
    }
    
    override fun getEntityType(): String = "HealthMetric"
}