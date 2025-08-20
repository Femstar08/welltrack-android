package com.beaconledger.welltrack.data.sync

/**
 * Interface for handling synchronization of specific entity types
 */
interface EntitySyncHandler<T : Any> {
    
    /**
     * Gets the local entity by ID
     */
    suspend fun getLocalEntity(entityId: String): T?
    
    /**
     * Gets the cloud entity by ID
     */
    suspend fun getCloudEntity(entityId: String): T?
    
    /**
     * Saves entity to local storage
     */
    suspend fun saveLocalEntity(entity: T)
    
    /**
     * Uploads entity to cloud storage
     */
    suspend fun uploadToCloud(entity: T): T
    
    /**
     * Deletes entity from local storage
     */
    suspend fun deleteLocalEntity(entityId: String)
    
    /**
     * Deletes entity from cloud storage
     */
    suspend fun deleteCloudEntity(entityId: String)
    
    /**
     * Gets the version/timestamp of an entity for conflict detection
     */
    fun getEntityVersion(entity: T): Long
    
    /**
     * Encrypts sensitive data in the entity before cloud upload
     */
    suspend fun encryptSensitiveData(entity: T): T
    
    /**
     * Decrypts sensitive data in the entity after cloud download
     */
    suspend fun decryptSensitiveData(entity: T): T
    
    /**
     * Gets the entity type name for this handler
     */
    fun getEntityType(): String
}