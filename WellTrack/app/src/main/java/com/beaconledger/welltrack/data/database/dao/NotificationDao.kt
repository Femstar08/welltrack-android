package com.beaconledger.welltrack.data.database.dao

import androidx.room.*
import com.beaconledger.welltrack.data.model.NotificationEntity
import com.beaconledger.welltrack.data.model.NotificationType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface NotificationDao {
    
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY scheduledTime ASC")
    fun getNotificationsForUser(userId: String): Flow<List<NotificationEntity>>
    
    @Query("SELECT * FROM notifications WHERE userId = :userId AND type = :type ORDER BY scheduledTime ASC")
    fun getNotificationsByType(userId: String, type: NotificationType): Flow<List<NotificationEntity>>
    
    @Query("SELECT * FROM notifications WHERE scheduledTime <= :currentTime AND isEnabled = 1")
    suspend fun getPendingNotifications(currentTime: LocalDateTime): List<NotificationEntity>
    
    @Query("SELECT * FROM notifications WHERE id = :id")
    suspend fun getNotificationById(id: String): NotificationEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)
    
    @Update
    suspend fun updateNotification(notification: NotificationEntity)
    
    @Delete
    suspend fun deleteNotification(notification: NotificationEntity)
    
    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotificationById(id: String)
    
    @Query("DELETE FROM notifications WHERE userId = :userId AND type = :type")
    suspend fun deleteNotificationsByType(userId: String, type: NotificationType)
    
    @Query("UPDATE notifications SET isEnabled = :enabled WHERE id = :id")
    suspend fun updateNotificationEnabled(id: String, enabled: Boolean)
    
    @Query("UPDATE notifications SET lastTriggered = :triggeredTime WHERE id = :id")
    suspend fun updateLastTriggered(id: String, triggeredTime: LocalDateTime)
    
    @Query("SELECT * FROM notifications WHERE isRecurring = 1 AND isEnabled = 1")
    suspend fun getRecurringNotifications(): List<NotificationEntity>
    
    @Query("DELETE FROM notifications WHERE userId = :userId")
    suspend fun deleteAllNotificationsForUser(userId: String)
}