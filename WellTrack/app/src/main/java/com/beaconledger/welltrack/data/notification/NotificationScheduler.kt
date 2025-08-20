package com.beaconledger.welltrack.data.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.beaconledger.welltrack.data.model.NotificationEntity
import com.beaconledger.welltrack.data.model.NotificationType
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    
    fun scheduleNotification(notification: NotificationEntity) {
        val intent = Intent(context, NotificationTriggerReceiver::class.java).apply {
            putExtra("notification_id", notification.id)
            putExtra("notification_type", notification.type.name)
            putExtra("title", notification.title)
            putExtra("message", notification.message)
            putExtra("metadata", notification.metadata)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notification.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val triggerTime = notification.scheduledTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
            else -> {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        }
    }
    
    fun cancelNotification(notificationId: String) {
        val intent = Intent(context, NotificationTriggerReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }
    
    fun rescheduleNotification(notification: NotificationEntity, newTime: LocalDateTime) {
        cancelNotification(notification.id)
        scheduleNotification(notification.copy(scheduledTime = newTime))
    }
    
    fun scheduleRecurringNotifications(notifications: List<NotificationEntity>) {
        notifications.forEach { notification ->
            if (notification.isRecurring && notification.isEnabled) {
                scheduleNotification(notification)
            }
        }
    }
}