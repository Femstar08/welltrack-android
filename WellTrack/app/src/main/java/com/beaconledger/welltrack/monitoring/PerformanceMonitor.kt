package com.beaconledger.welltrack.monitoring

import android.app.ActivityManager
import android.content.Context
import android.os.Debug
import android.os.Process
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PerformanceMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) : DefaultLifecycleObserver {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    
    private val _performanceMetrics = MutableStateFlow(PerformanceMetrics())
    val performanceMetrics: StateFlow<PerformanceMetrics> = _performanceMetrics.asStateFlow()
    
    private var monitoringJob: Job? = null
    private var isMonitoring = false
    
    companion object {
        private const val TAG = "PerformanceMonitor"
        private const val MONITORING_INTERVAL = 5000L // 5 seconds
        private const val MAX_LOG_FILES = 10
        private const val LOG_FILE_PREFIX = "performance_"
    }

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        startMonitoring()
    }

    override fun onStop(owner: LifecycleOwner) {
        stopMonitoring()
    }

    fun startMonitoring() {
        if (isMonitoring) return
        
        isMonitoring = true
        monitoringJob = scope.launch {
            while (isActive && isMonitoring) {
                try {
                    val metrics = collectPerformanceMetrics()
                    _performanceMetrics.value = metrics
                    
                    // Log critical performance issues
                    if (metrics.memoryUsagePercent > 80) {
                        logPerformanceIssue("High memory usage: ${metrics.memoryUsagePercent}%")
                    }
                    
                    if (metrics.cpuUsagePercent > 70) {
                        logPerformanceIssue("High CPU usage: ${metrics.cpuUsagePercent}%")
                    }
                    
                    delay(MONITORING_INTERVAL)
                } catch (e: Exception) {
                    Log.e(TAG, "Error collecting performance metrics", e)
                }
            }
        }
    }

    fun stopMonitoring() {
        isMonitoring = false
        monitoringJob?.cancel()
    }

    private fun collectPerformanceMetrics(): PerformanceMetrics {
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        val memoryUsagePercent = ((usedMemory.toDouble() / maxMemory) * 100).toInt()
        
        // Get native heap info
        val nativeHeapSize = Debug.getNativeHeapSize()
        val nativeHeapAllocated = Debug.getNativeHeapAllocatedSize()
        val nativeHeapFree = Debug.getNativeHeapFreeSize()
        
        // CPU usage estimation (simplified)
        val cpuUsage = estimateCpuUsage()
        
        return PerformanceMetrics(
            timestamp = System.currentTimeMillis(),
            memoryUsagePercent = memoryUsagePercent,
            usedMemoryMB = (usedMemory / (1024 * 1024)).toInt(),
            maxMemoryMB = (maxMemory / (1024 * 1024)).toInt(),
            availableMemoryMB = (memInfo.availMem / (1024 * 1024)).toInt(),
            totalMemoryMB = (memInfo.totalMem / (1024 * 1024)).toInt(),
            nativeHeapSizeMB = (nativeHeapSize / (1024 * 1024)).toInt(),
            nativeHeapAllocatedMB = (nativeHeapAllocated / (1024 * 1024)).toInt(),
            nativeHeapFreeMB = (nativeHeapFree / (1024 * 1024)).toInt(),
            cpuUsagePercent = cpuUsage,
            isLowMemory = memInfo.lowMemory,
            memoryThreshold = (memInfo.threshold / (1024 * 1024)).toInt()
        )
    }

    private fun estimateCpuUsage(): Int {
        // Simplified CPU usage estimation
        // In a real implementation, you might use more sophisticated methods
        return try {
            val pid = Process.myPid()
            val statFile = File("/proc/$pid/stat")
            if (statFile.exists()) {
                // This is a simplified estimation
                // Real CPU usage calculation would require multiple samples
                Random().nextInt(20) // Placeholder for actual CPU calculation
            } else {
                0
            }
        } catch (e: Exception) {
            0
        }
    }

    private fun logPerformanceIssue(message: String) {
        Log.w(TAG, message)
        
        scope.launch {
            try {
                val logFile = getPerformanceLogFile()
                val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(Date())
                val logEntry = "[$timestamp] $message\n"
                
                logFile.appendText(logEntry)
                
                // Clean up old log files
                cleanupOldLogFiles()
            } catch (e: Exception) {
                Log.e(TAG, "Error writing performance log", e)
            }
        }
    }

    private fun getPerformanceLogFile(): File {
        val logsDir = File(context.filesDir, "performance_logs")
        if (!logsDir.exists()) {
            logsDir.mkdirs()
        }
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = dateFormat.format(Date())
        
        return File(logsDir, "${LOG_FILE_PREFIX}${today}.log")
    }

    private fun cleanupOldLogFiles() {
        try {
            val logsDir = File(context.filesDir, "performance_logs")
            if (!logsDir.exists()) return
            
            val logFiles = logsDir.listFiles { file ->
                file.name.startsWith(LOG_FILE_PREFIX) && file.name.endsWith(".log")
            }?.sortedByDescending { it.lastModified() }
            
            if (logFiles != null && logFiles.size > MAX_LOG_FILES) {
                logFiles.drop(MAX_LOG_FILES).forEach { file ->
                    file.delete()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up log files", e)
        }
    }

    fun getPerformanceLogs(): List<String> {
        return try {
            val logsDir = File(context.filesDir, "performance_logs")
            if (!logsDir.exists()) return emptyList()
            
            logsDir.listFiles { file ->
                file.name.startsWith(LOG_FILE_PREFIX) && file.name.endsWith(".log")
            }?.sortedByDescending { it.lastModified() }
                ?.take(5) // Return last 5 log files
                ?.map { it.readText() }
                ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error reading performance logs", e)
            emptyList()
        }
    }

    fun triggerMemoryCleanup() {
        scope.launch {
            try {
                System.gc()
                Runtime.getRuntime().gc()
                Log.i(TAG, "Memory cleanup triggered")
            } catch (e: Exception) {
                Log.e(TAG, "Error during memory cleanup", e)
            }
        }
    }
}

data class PerformanceMetrics(
    val timestamp: Long = 0L,
    val memoryUsagePercent: Int = 0,
    val usedMemoryMB: Int = 0,
    val maxMemoryMB: Int = 0,
    val availableMemoryMB: Int = 0,
    val totalMemoryMB: Int = 0,
    val nativeHeapSizeMB: Int = 0,
    val nativeHeapAllocatedMB: Int = 0,
    val nativeHeapFreeMB: Int = 0,
    val cpuUsagePercent: Int = 0,
    val isLowMemory: Boolean = false,
    val memoryThreshold: Int = 0
)