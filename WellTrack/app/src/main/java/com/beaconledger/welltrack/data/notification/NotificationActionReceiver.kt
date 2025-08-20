package com.beaconledger.welltrack.data.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.beaconledger.welltrack.data.model.NotificationType
import com.beaconledger.welltrack.domain.repository.NotificationRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationActionReceiver : BroadcastReceiver() {
    
    companion object {
        const val ACTION_SNOOZE = "com.beaconledger.welltrack.SNOOZE_NOTIFICATION"
        const val ACTION_MARK_COMPLETE = "com.beaconledger.welltrack.MARK_COMPLETE"
        const val ACTION_VIEW_RECIPES = "com.beaconledger.welltrack.VIEW_RECIPES"
        const val ACTION_DISMISS = "com.beaconledger.welltrack.DISMISS_NOTIFICATION"
    }
    
    @Inject
    lateinit var notificationRepository: NotificationRepository
    
    @Inject
    lateinit var notificationManager: NotificationManager
    
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    
    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getStringExtra("notification_id") ?: return
        
        when (intent.action) {
            ACTION_SNOOZE -> {
                val notificationType = intent.getStringExtra("notification_type")?.let {
                    NotificationType.valueOf(it)
                } ?: return
                
                coroutineScope.launch {
                    notificationRepository.snoozeNotification(notificationId, 15)
                    notificationManager.cancelNotification(notificationId)
                }
            }
            
            ACTION_MARK_COMPLETE -> {
                val itemId = intent.getStringExtra("item_id") ?: return
                
                coroutineScope.launch {
                    notificationRepository.markNotificationComplete(notificationId, itemId)
                    notificationManager.cancelNotification(notificationId)
                }
            }
            
            ACTION_VIEW_RECIPES -> {
                val ingredientName = intent.getStringExtra("ingredient_name") ?: return
                
                // Launch main activity with recipe search
                val mainIntent = Intent(context, com.beaconledger.welltrack.MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra("navigate_to", "recipes")
                    putExtra("search_ingredient", ingredientName)
                }
                context.startActivity(mainIntent)
                
                coroutineScope.launch {
                    notificationManager.cancelNotification(notificationId)
                }
            }
            
            ACTION_DISMISS -> {
                coroutineScope.launch {
                    notificationManager.cancelNotification(notificationId)
                }
            }
        }
    }
}