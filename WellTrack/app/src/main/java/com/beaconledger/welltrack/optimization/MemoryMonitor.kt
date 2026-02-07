package com.beaconledger.welltrack.optimization

import android.app.ActivityManager
import android.content.ComponentCallbacks2
import android.content.Context
import android.content.res.Configuration
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoryMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) : ComponentCallbacks2 {

    companion object {
        private const val TAG = "MemoryMonitor"
        private const val MEMORY_THRESHOLD_PERCENTAGE = 0.80 // 80% of available memory
    }

    private val activityManager: ActivityManager by lazy {
        context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    }

    init {
        // Register this component to receive memory warnings
        context.applicationContext.registerComponentCallbacks(this)
        Log.d(TAG, "MemoryMonitor initialized and registered.")
    }

    /**
     * Logs the current memory usage of the application.
     */
    fun logMemoryUsage() {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val runtime = Runtime.getRuntime()
        val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024) // MB
        val maxMemory = runtime.maxMemory() / (1024 * 1024) // MB
        val totalMemory = runtime.totalMemory() / (1024 * 1024) // MB

        Log.d(TAG, "App Memory Usage: Used = ${usedMemory}MB, Max = ${maxMemory}MB, Total = ${totalMemory}MB")
        Log.d(TAG, "Device Memory: Available = ${memoryInfo.availMem / (1024 * 1024)}MB, Total = ${memoryInfo.totalMem / (1024 * 1024)}MB")

        val appMemoryPercentage = if (maxMemory > 0) (usedMemory.toFloat() / maxMemory) * 100 else 0f
        Log.d(TAG, "App Memory Usage: ${String.format("%.2f", appMemoryPercentage)}% of allocated max.")

        if (appMemoryPercentage > MEMORY_THRESHOLD_PERCENTAGE * 100) {
            Log.w(TAG, "WARNING: App memory usage is high! (${String.format("%.2f", appMemoryPercentage)}%)")
            clearCachesAndResources() // Implement specific actions for high memory usage
        }
    }

    /**
     * Called when the overall system memory is low.
     * @param level The memory trim level, indicating the severity of the memory warning.
     */
    override fun onTrimMemory(level: Int) {
        Log.d(TAG, "onTrimMemory received with level: $level")
        when (level) {
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE,
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW,
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> {
                // The app is running, but the device is low on memory.
                // Release any non-critical resources.
                Log.w(TAG, "System memory is running low. Releasing non-critical resources.")
                clearCachesAndResources()
            }
            ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> {
                // The app's UI is no longer visible.
                // Release resources that are only needed for the UI.
                Log.d(TAG, "UI is hidden. Releasing UI-related resources.")
                clearUiResources()
            }
            ComponentCallbacks2.TRIM_MEMORY_BACKGROUND,
            ComponentCallbacks2.TRIM_MEMORY_MODERATE,
            ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> {
                // The app is in the background, and the system needs memory.
                // Release as much memory as possible.
                Log.w(TAG, "App is in background and system needs memory. Releasing all possible resources.")
                clearAllResourcesAggressively()
            }
        }
        logMemoryUsage() // Log memory usage after trim
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        // Not directly related to memory, but part of ComponentCallbacks2
    }

    override fun onLowMemory() {
        // Called when the system is running critically low on memory.
        // This is a more severe warning than TRIM_MEMORY_COMPLETE.
        Log.e(TAG, "onLowMemory received: System is critically low on memory!")
        clearAllResourcesAggressively() // Implement critical memory handling
        logMemoryUsage()
    }

    /**
     * Clears non-critical caches and resources.
     * This method should be implemented to release memory that can be easily reloaded.
     */
    private fun clearCachesAndResources() {
        Log.d(TAG, "clearCachesAndResources: Releasing non-critical caches and resources.")
        // TODO: Implement actual cache clearing logic here (e.g., image caches, network caches)
    }

    /**
     * Clears resources specifically related to the UI that is no longer visible.
     */
    private fun clearUiResources() {
        Log.d(TAG, "clearUiResources: Releasing UI-related resources.")
        // TODO: Implement actual UI resource release logic here (e.g., large bitmaps, view hierarchies)
    }

    /**
     * Aggressively clears all possible resources to free up memory.
     * This might involve clearing all caches, releasing large data structures, etc.
     */
    private fun clearAllResourcesAggressively() {
        Log.d(TAG, "clearAllResourcesAggressively: Aggressively releasing all possible resources.")
        clearCachesAndResources() // Clear general caches
        clearUiResources() // Clear UI resources
        // TODO: Implement more aggressive resource release here if needed
    }
}
