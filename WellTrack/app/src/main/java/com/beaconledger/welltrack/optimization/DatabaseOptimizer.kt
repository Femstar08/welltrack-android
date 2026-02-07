package com.beaconledger.welltrack.optimization

import android.content.Context
import android.util.Log
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.beaconledger.welltrack.data.database.WellTrackDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseOptimizer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: WellTrackDatabase
) {

    companion object {
        private const val TAG = "DatabaseOptimizer"
    }

    /**
     * Analyzes and optimizes database indexes.
     * This function should be called during a maintenance window.
     */
    suspend fun optimizeIndexes() = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting database index optimization...")
            database.openHelper.writableDatabase.execSQL("ANALYZE;")
            database.openHelper.writableDatabase.execSQL("REINDEX;")
            Log.d(TAG, "Database index optimization completed successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Error during database index optimization", e)
        }
    }

    /**
     * Monitors the performance of a given query.
     * @param query The query to be executed.
     * @param action The action to be performed with the query.
     */
    suspend fun <T> monitorQueryPerformance(query: String, action: suspend () -> T): T {
        val startTime = System.currentTimeMillis()
        val result = action()
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        Log.d(TAG, "Query performance: '$query' took $duration ms")
        return result
    }

    /**
     * Optimizes memory usage for large datasets by clearing the cache.
     */
    suspend fun optimizeMemoryUsage() = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Optimizing memory usage...")
            database.clearAllTables()
            Log.d(TAG, "Memory usage optimization completed successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Error during memory usage optimization", e)
        }
    }

    /**
     * Performs a vacuum on the database to rebuild it and free up space.
     * This function should be called during a maintenance window.
     */
    suspend fun vacuumDatabase() = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting database vacuum...")
            database.openHelper.writableDatabase.execSQL("VACUUM;")
            Log.d(TAG, "Database vacuum completed successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Error during database vacuum", e)
        }
    }
}
