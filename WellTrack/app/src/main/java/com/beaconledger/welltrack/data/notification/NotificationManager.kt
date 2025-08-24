package com.beaconledger.welltrack.data.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.beaconledger.welltrack.MainActivity
import com.beaconledger.welltrack.R
import com.beaconledger.welltrack.data.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val MEAL_REMINDER_CHANNEL_ID = "meal_reminders"
        private const val SUPPLEMENT_CHANNEL_ID = "supplement_reminders"
        private const val PANTRY_CHANNEL_ID = "pantry_alerts"
        private const val MOTIVATIONAL_CHANNEL_ID = "motivational_notifications"
        private const val GENERAL_CHANNEL_ID = "general_notifications"
        
        private const val MEAL_REMINDER_REQUEST_CODE = 1000
        private const val SUPPLEMENT_REQUEST_CODE = 2000
        private const val PANTRY_REQUEST_CODE = 3000
        private const val MOTIVATIONAL_REQUEST_CODE = 4000
    }
    
    private val notificationManager = NotificationManagerCompat.from(context)
    
    init {
        createNotificationChannels()
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    MEAL_REMINDER_CHANNEL_ID,
                    "Meal Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Reminders for planned meals"
                    enableVibration(true)
                },
                NotificationChannel(
                    SUPPLEMENT_CHANNEL_ID,
                    "Supplement Reminders",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Reminders for supplement dosages"
                    enableVibration(true)
                },
                NotificationChannel(
                    PANTRY_CHANNEL_ID,
                    "Pantry Alerts",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Alerts for expiring pantry items"
                },
                NotificationChannel(
                    MOTIVATIONAL_CHANNEL_ID,
                    "Health Goals",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Motivational notifications for health goals"
                },
                NotificationChannel(
                    GENERAL_CHANNEL_ID,
                    "General",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "General WellTrack notifications"
                }
            )
            
            val systemNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            channels.forEach { channel ->
                systemNotificationManager.createNotificationChannel(channel)
            }
        }
    }
    
    fun showMealReminderNotification(
        notificationId: String,
        title: String,
        message: String,
        mealData: MealReminderData
    ) {
        if (!checkPermission()) return
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "meal_planner")
            putExtra("meal_type", mealData.mealType)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            MEAL_REMINDER_REQUEST_CODE + notificationId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val snoozeIntent = createSnoozeIntent(notificationId, NotificationType.MEAL_REMINDER)
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            MEAL_REMINDER_REQUEST_CODE + notificationId.hashCode() + 1,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, MEAL_REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_restaurant)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_snooze, "Snooze 15min", snoozePendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        
        try {
            notificationManager.notify(notificationId.hashCode(), notification)
        } catch (e: SecurityException) {
            // Handle permission denied
        }
    }
    
    fun showSupplementReminderNotification(
        notificationId: String,
        title: String,
        message: String,
        supplementData: SupplementReminderData
    ) {
        if (!checkPermission()) return
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "supplements")
            putExtra("supplement_name", supplementData.supplementName)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            SUPPLEMENT_REQUEST_CODE + notificationId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val snoozeIntent = createSnoozeIntent(notificationId, NotificationType.SUPPLEMENT_REMINDER)
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            SUPPLEMENT_REQUEST_CODE + notificationId.hashCode() + 1,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val takenIntent = createMarkCompleteIntent(notificationId, supplementData.supplementName)
        val takenPendingIntent = PendingIntent.getBroadcast(
            context,
            SUPPLEMENT_REQUEST_CODE + notificationId.hashCode() + 2,
            takenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, SUPPLEMENT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_medication)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_snooze, "Snooze 15min", snoozePendingIntent)
            .addAction(R.drawable.ic_check, "Taken", takenPendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        
        try {
            notificationManager.notify(notificationId.hashCode(), notification)
        } catch (e: SecurityException) {
            // Handle permission denied
        }
    }
    
    fun showPantryExpiryNotification(
        notificationId: String,
        title: String,
        message: String,
        pantryData: PantryExpiryData
    ) {
        if (!checkPermission()) return
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "pantry")
            putExtra("ingredient_name", pantryData.ingredientName)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            PANTRY_REQUEST_CODE + notificationId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val recipesIntent = createViewRecipesIntent(notificationId, pantryData.ingredientName)
        val recipesPendingIntent = PendingIntent.getBroadcast(
            context,
            PANTRY_REQUEST_CODE + notificationId.hashCode() + 1,
            recipesIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notificationBuilder = NotificationCompat.Builder(context, PANTRY_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_warning)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        
        if (pantryData.suggestedRecipes.isNotEmpty()) {
            notificationBuilder.addAction(R.drawable.ic_recipe, "View Recipes", recipesPendingIntent)
        }
        
        try {
            notificationManager.notify(notificationId.hashCode(), notificationBuilder.build())
        } catch (e: SecurityException) {
            // Handle permission denied
        }
    }
    
    fun showMotivationalNotification(
        notificationId: String,
        title: String,
        message: String,
        motivationalData: MotivationalData
    ) {
        if (!checkPermission()) return
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "analytics")
            putExtra("goal_type", motivationalData.goalType)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            MOTIVATIONAL_REQUEST_CODE + notificationId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, MOTIVATIONAL_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_trending_up)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        
        try {
            notificationManager.notify(notificationId.hashCode(), notification)
        } catch (e: SecurityException) {
            // Handle permission denied
        }
    }
    
    fun cancelNotification(notificationId: String) {
        notificationManager.cancel(notificationId.hashCode())
    }
    
    private fun checkPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= 33) { // API 33 = TIRAMISU
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
    
    private fun createSnoozeIntent(notificationId: String, type: NotificationType): Intent {
        return Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_SNOOZE
            putExtra("notification_id", notificationId)
            putExtra("notification_type", type.name)
        }
    }
    
    private fun createMarkCompleteIntent(notificationId: String, itemId: String): Intent {
        return Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_MARK_COMPLETE
            putExtra("notification_id", notificationId)
            putExtra("item_id", itemId)
        }
    }
    
    private fun createViewRecipesIntent(notificationId: String, ingredientName: String): Intent {
        return Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_VIEW_RECIPES
            putExtra("notification_id", notificationId)
            putExtra("ingredient_name", ingredientName)
        }
    }
}