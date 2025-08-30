package com.beaconledger.welltrack.monitoring

import android.content.Context
import android.os.Build
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CrashReporter @Inject constructor(
    @ApplicationContext private val context: Context
) : Thread.UncaughtExceptionHandler {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
    private val json = Json { prettyPrint = true }
    
    companion object {
        private const val TAG = "CrashReporter"
        private const val CRASH_LOG_DIR = "crash_logs"
        private const val MAX_CRASH_FILES = 20
        private const val CRASH_FILE_PREFIX = "crash_"
    }

    init {
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(thread: Thread, exception: Throwable) {
        try {
            logCrash(thread, exception)
        } catch (e: Exception) {
            Log.e(TAG, "Error logging crash", e)
        } finally {
            // Call the default handler to ensure the app terminates properly
            defaultHandler?.uncaughtException(thread, exception)
        }
    }

    private fun logCrash(thread: Thread, exception: Throwable) {
        scope.launch {
            try {
                val crashReport = createCrashReport(thread, exception)
                saveCrashReport(crashReport)
                cleanupOldCrashFiles()
                
                Log.e(TAG, "Crash logged: ${crashReport.timestamp}")
            } catch (e: Exception) {
                Log.e(TAG, "Error creating crash report", e)
            }
        }
    }

    private fun createCrashReport(thread: Thread, exception: Throwable): CrashReport {
        val stackTrace = StringWriter().apply {
            exception.printStackTrace(PrintWriter(this))
        }.toString()

        return CrashReport(
            timestamp = System.currentTimeMillis(),
            threadName = thread.name,
            exceptionType = exception.javaClass.simpleName,
            exceptionMessage = exception.message ?: "No message",
            stackTrace = stackTrace,
            deviceInfo = getDeviceInfo(),
            appInfo = getAppInfo(),
            memoryInfo = getMemoryInfo()
        )
    }

    private fun getDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            androidVersion = Build.VERSION.RELEASE,
            apiLevel = Build.VERSION.SDK_INT,
            brand = Build.BRAND,
            device = Build.DEVICE,
            hardware = Build.HARDWARE,
            product = Build.PRODUCT
        )
    }

    private fun getAppInfo(): AppInfo {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        return AppInfo(
            packageName = context.packageName,
            versionName = packageInfo.versionName ?: "Unknown",
            versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }
        )
    }

    private fun getMemoryInfo(): MemoryInfo {
        val runtime = Runtime.getRuntime()
        return MemoryInfo(
            usedMemoryMB = ((runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)).toInt(),
            maxMemoryMB = (runtime.maxMemory() / (1024 * 1024)).toInt(),
            freeMemoryMB = (runtime.freeMemory() / (1024 * 1024)).toInt(),
            totalMemoryMB = (runtime.totalMemory() / (1024 * 1024)).toInt()
        )
    }

    private suspend fun saveCrashReport(crashReport: CrashReport) {
        withContext(Dispatchers.IO) {
            try {
                val crashDir = File(context.filesDir, CRASH_LOG_DIR)
                if (!crashDir.exists()) {
                    crashDir.mkdirs()
                }

                val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
                    .format(Date(crashReport.timestamp))
                val fileName = "${CRASH_FILE_PREFIX}${timestamp}.json"
                val crashFile = File(crashDir, fileName)

                val jsonString = json.encodeToString(crashReport)
                crashFile.writeText(jsonString)

                Log.i(TAG, "Crash report saved: ${crashFile.absolutePath}")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving crash report", e)
            }
        }
    }

    private suspend fun cleanupOldCrashFiles() {
        withContext(Dispatchers.IO) {
            try {
                val crashDir = File(context.filesDir, CRASH_LOG_DIR)
                if (!crashDir.exists()) return@withContext

                val crashFiles = crashDir.listFiles { file ->
                    file.name.startsWith(CRASH_FILE_PREFIX) && file.name.endsWith(".json")
                }?.sortedByDescending { it.lastModified() }

                if (crashFiles != null && crashFiles.size > MAX_CRASH_FILES) {
                    crashFiles.drop(MAX_CRASH_FILES).forEach { file ->
                        file.delete()
                        Log.d(TAG, "Deleted old crash file: ${file.name}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error cleaning up crash files", e)
            }
        }
    }

    fun getCrashReports(): List<CrashReport> {
        return try {
            val crashDir = File(context.filesDir, CRASH_LOG_DIR)
            if (!crashDir.exists()) return emptyList()

            crashDir.listFiles { file ->
                file.name.startsWith(CRASH_FILE_PREFIX) && file.name.endsWith(".json")
            }?.sortedByDescending { it.lastModified() }
                ?.take(10) // Return last 10 crash reports
                ?.mapNotNull { file ->
                    try {
                        val jsonString = file.readText()
                        json.decodeFromString<CrashReport>(jsonString)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing crash report: ${file.name}", e)
                        null
                    }
                } ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error reading crash reports", e)
            emptyList()
        }
    }

    fun clearCrashReports() {
        scope.launch {
            try {
                val crashDir = File(context.filesDir, CRASH_LOG_DIR)
                if (crashDir.exists()) {
                    crashDir.listFiles()?.forEach { it.delete() }
                    Log.i(TAG, "All crash reports cleared")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing crash reports", e)
            }
        }
    }
}

@Serializable
data class CrashReport(
    val timestamp: Long,
    val threadName: String,
    val exceptionType: String,
    val exceptionMessage: String,
    val stackTrace: String,
    val deviceInfo: DeviceInfo,
    val appInfo: AppInfo,
    val memoryInfo: MemoryInfo
)

@Serializable
data class DeviceInfo(
    val manufacturer: String,
    val model: String,
    val androidVersion: String,
    val apiLevel: Int,
    val brand: String,
    val device: String,
    val hardware: String,
    val product: String
)

@Serializable
data class AppInfo(
    val packageName: String,
    val versionName: String,
    val versionCode: Long
)

@Serializable
data class MemoryInfo(
    val usedMemoryMB: Int,
    val maxMemoryMB: Int,
    val freeMemoryMB: Int,
    val totalMemoryMB: Int
)