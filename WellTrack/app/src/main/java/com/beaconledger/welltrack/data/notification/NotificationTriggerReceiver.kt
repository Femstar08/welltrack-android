package com.beaconledger.welltrack.data.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.repository.NotificationRepository
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@AndroidEntryPoint
class NotificationTriggerReceiver : BroadcastReceiver() {
    
    @Inject
    lateinit var notificationManager: NotificationManager
    
    @Inject
    lateinit var notificationRepository: NotificationRepository
    
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val gson = Gson()
    
    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getStringExtra("notification_id") ?: return
        val notificationTypeString = intent.getStringExtra("notification_type") ?: return
        val title = intent.getStringExtra("title") ?: return
        val message = intent.getStringExtra("message") ?: return
        val metadata = intent.getStringExtra("metadata")
        
        val notificationType = try {
            NotificationType.valueOf(notificationTypeString)
        } catch (e: IllegalArgumentException) {
            return
        }
        
        coroutineScope.launch {
            // Check if notification should still be shown (user preferences, quiet hours, etc.)
            val shouldShow = notificationRepository.shouldShowNotification(notificationId)
            if (!shouldShow) return@launch
            
            // Update last triggered time
            notificationRepository.updateLastTriggered(notificationId, LocalDateTime.now())
            
            // Show the appropriate notification based on type
            when (notificationType) {
                NotificationType.MEAL_REMINDER -> {
                    val mealData = metadata?.let { 
                        gson.fromJson(it, MealReminderData::class.java) 
                    } ?: MealReminderData("Unknown", null, LocalDateTime.now())
                    
                    notificationManager.showMealReminderNotification(
                        notificationId, title, message, mealData
                    )
                }
                
                NotificationType.SUPPLEMENT_REMINDER -> {
                    val supplementData = metadata?.let { 
                        gson.fromJson(it, SupplementReminderData::class.java) 
                    } ?: SupplementReminderData("Unknown", "Unknown", "Unknown")
                    
                    notificationManager.showSupplementReminderNotification(
                        notificationId, title, message, supplementData
                    )
                }
                
                NotificationType.PANTRY_EXPIRY_ALERT -> {
                    val pantryData = metadata?.let { 
                        gson.fromJson(it, PantryExpiryData::class.java) 
                    } ?: PantryExpiryData("Unknown", LocalDateTime.now())
                    
                    notificationManager.showPantryExpiryNotification(
                        notificationId, title, message, pantryData
                    )
                }
                
                NotificationType.MOTIVATIONAL_HEALTH_GOAL -> {
                    val motivationalData = metadata?.let { 
                        gson.fromJson(it, MotivationalData::class.java) 
                    } ?: MotivationalData("Unknown", 0.0, 100.0, "Keep going!")
                    
                    notificationManager.showMotivationalNotification(
                        notificationId, title, message, motivationalData
                    )
                }
                
                else -> {
                    // Handle other notification types with general notification
                    notificationManager.showMotivationalNotification(
                        notificationId, title, message, 
                        MotivationalData("General", 0.0, 100.0, message)
                    )
                }
            }
            
            // Schedule next occurrence if it's a recurring notification
            notificationRepository.scheduleNextRecurrence(notificationId)
        }
    }
}