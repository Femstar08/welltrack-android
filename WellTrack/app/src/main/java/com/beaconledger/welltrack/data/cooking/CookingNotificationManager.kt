package com.beaconledger.welltrack.data.cooking

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.beaconledger.welltrack.MainActivity
import com.beaconledger.welltrack.R
import com.beaconledger.welltrack.data.model.CookingTimer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CookingNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    companion object {
        private const val CHANNEL_ID = "cooking_timers"
        private const val CHANNEL_NAME = "Cooking Timers"
        private const val CHANNEL_DESCRIPTION = "Notifications for cooking timer alerts"
    }
    
    init {
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), null)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun showTimerCompletedNotification(timer: CookingTimer) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            timer.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_timer) // You'll need to add this icon
            .setContentTitle("Timer Completed!")
            .setContentText("${timer.name} - Step ${timer.stepNumber}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()
        
        try {
            notificationManager.notify(timer.id.hashCode(), notification)
        } catch (e: SecurityException) {
            // Handle case where POST_NOTIFICATIONS permission is not granted
            // In a production app, you would request the permission first
        }
    }
    
    fun cancelTimerNotification(timerId: String) {
        notificationManager.cancel(timerId.hashCode())
    }
    
    fun showCookingReminderNotification(recipeName: String, stepDescription: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            recipeName.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_cooking) // You'll need to add this icon
            .setContentTitle("Cooking Reminder")
            .setContentText("$recipeName: $stepDescription")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        try {
            notificationManager.notify(recipeName.hashCode(), notification)
        } catch (e: SecurityException) {
            // Handle case where POST_NOTIFICATIONS permission is not granted
            // In a production app, you would request the permission first
        }
    }
}