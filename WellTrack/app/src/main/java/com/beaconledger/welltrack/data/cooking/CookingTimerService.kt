package com.beaconledger.welltrack.data.cooking

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.beaconledger.welltrack.data.model.CookingTimer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@AndroidEntryPoint
class CookingTimerService : Service() {
    
    @Inject
    lateinit var notificationManager: CookingNotificationManager
    
    private val binder = CookingTimerBinder()
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    private val _activeTimers = MutableStateFlow<List<CookingTimer>>(emptyList())
    val activeTimers: StateFlow<List<CookingTimer>> = _activeTimers.asStateFlow()
    
    private val timerJobs = mutableMapOf<String, Job>()
    
    inner class CookingTimerBinder : Binder() {
        fun getService(): CookingTimerService = this@CookingTimerService
    }
    
    override fun onBind(intent: Intent?): IBinder = binder
    
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        timerJobs.values.forEach { it.cancel() }
    }
    
    fun startTimer(timer: CookingTimer) {
        if (timerJobs.containsKey(timer.id)) {
            return // Timer already running
        }
        
        val job = serviceScope.launch {
            val durationMs = timer.durationMinutes * 60 * 1000L
            val startTime = System.currentTimeMillis()
            
            // Update active timers list
            _activeTimers.value = _activeTimers.value + timer
            
            try {
                delay(durationMs)
                
                // Timer completed
                val completedTimer = timer.copy(isActive = false, isCompleted = true)
                _activeTimers.value = _activeTimers.value.map { 
                    if (it.id == timer.id) completedTimer else it 
                }
                
                // Show notification
                notificationManager.showTimerCompletedNotification(completedTimer)
                
                // Remove from active timers after a delay
                delay(5000) // Keep in list for 5 seconds after completion
                _activeTimers.value = _activeTimers.value.filter { it.id != timer.id }
                
            } catch (e: CancellationException) {
                // Timer was cancelled
                _activeTimers.value = _activeTimers.value.filter { it.id != timer.id }
            } finally {
                timerJobs.remove(timer.id)
            }
        }
        
        timerJobs[timer.id] = job
    }
    
    fun stopTimer(timerId: String) {
        timerJobs[timerId]?.cancel()
        timerJobs.remove(timerId)
        
        _activeTimers.value = _activeTimers.value.filter { it.id != timerId }
        notificationManager.cancelTimerNotification(timerId)
    }
    
    fun pauseTimer(timerId: String) {
        // For simplicity, we'll just stop the timer
        // In a more sophisticated implementation, you'd save the remaining time
        stopTimer(timerId)
    }
    
    fun getRemainingTime(timerId: String): Long {
        val timer = _activeTimers.value.find { it.id == timerId } ?: return 0L
        if (!timer.isActive || timer.isCompleted) return 0L
        
        val elapsedTime = System.currentTimeMillis() - timer.startTime
        val totalTime = timer.durationMinutes * 60 * 1000L
        val remainingTime = totalTime - elapsedTime
        
        return maxOf(0L, remainingTime)
    }
    
    fun isTimerActive(timerId: String): Boolean {
        return _activeTimers.value.any { it.id == timerId && it.isActive && !it.isCompleted }
    }
}