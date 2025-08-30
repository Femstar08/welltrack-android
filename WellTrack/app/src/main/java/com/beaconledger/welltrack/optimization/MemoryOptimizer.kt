package com.beaconledger.welltrack.optimization

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.util.LruCache
import androidx.paging.PagingConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoryOptimizer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    // Image cache with memory-aware sizing
    private val imageCache: LruCache<String, Bitmap> by lazy {
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSize = maxMemory / 8 // Use 1/8th of available memory for image cache
        
        object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                return bitmap.byteCount / 1024
            }
            
            override fun entryRemoved(
                evicted: Boolean,
                key: String,
                oldValue: Bitmap,
                newValue: Bitmap?
            ) {
                if (evicted && !oldValue.isRecycled) {
                    oldValue.recycle()
                }
            }
        }
    }
    
    // Weak reference cache for frequently accessed objects
    private val objectCache = mutableMapOf<String, WeakReference<Any>>()
    
    companion object {
        private const val TAG = "MemoryOptimizer"
        
        // Paging configuration for large datasets
        val PAGING_CONFIG = PagingConfig(
            pageSize = 20,
            prefetchDistance = 5,
            enablePlaceholders = false,
            initialLoadSize = 40,
            maxSize = 200
        )
        
        // Batch processing sizes
        const val BATCH_SIZE_SMALL = 50
        const val BATCH_SIZE_MEDIUM = 100
        const val BATCH_SIZE_LARGE = 200
    }

    /**
     * Process large datasets in batches to prevent memory overflow
     */
    fun <T, R> processBatched(
        items: List<T>,
        batchSize: Int = BATCH_SIZE_MEDIUM,
        processor: suspend (List<T>) -> List<R>
    ): Flow<List<R>> = flow {
        items.chunked(batchSize).forEach { batch ->
            try {
                val result = processor(batch)
                emit(result)
                
                // Allow other coroutines to run and GC to occur
                yield()
            } catch (e: Exception) {
                Log.e(TAG, "Error processing batch", e)
                emit(emptyList())
            }
        }
    }.flowOn(Dispatchers.Default)

    /**
     * Load and cache images with memory optimization
     */
    suspend fun loadOptimizedImage(
        imagePath: String,
        maxWidth: Int = 1024,
        maxHeight: Int = 1024
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            // Check cache first
            imageCache.get(imagePath)?.let { return@withContext it }
            
            // Load with size constraints
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            
            BitmapFactory.decodeFile(imagePath, options)
            
            // Calculate sample size
            options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight)
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.RGB_565 // Use less memory
            
            val bitmap = BitmapFactory.decodeFile(imagePath, options)
            
            // Cache the optimized bitmap
            bitmap?.let { imageCache.put(imagePath, it) }
            
            bitmap
        } catch (e: Exception) {
            Log.e(TAG, "Error loading optimized image: $imagePath", e)
            null
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    /**
     * Cache objects with weak references to prevent memory leaks
     */
    fun <T : Any> cacheObject(key: String, obj: T): T {
        objectCache[key] = WeakReference(obj)
        return obj
    }

    /**
     * Retrieve cached object
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getCachedObject(key: String): T? {
        return objectCache[key]?.get() as? T
    }

    /**
     * Clear expired weak references
     */
    fun cleanupObjectCache() {
        scope.launch {
            val iterator = objectCache.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                if (entry.value.get() == null) {
                    iterator.remove()
                }
            }
            Log.d(TAG, "Object cache cleaned up. Size: ${objectCache.size}")
        }
    }

    /**
     * Monitor memory usage and trigger cleanup when needed
     */
    fun monitorMemoryUsage() {
        scope.launch {
            while (isActive) {
                try {
                    val runtime = Runtime.getRuntime()
                    val usedMemory = runtime.totalMemory() - runtime.freeMemory()
                    val maxMemory = runtime.maxMemory()
                    val memoryPercent = (usedMemory.toDouble() / maxMemory * 100).toInt()

                    if (memoryPercent > 75) {
                        Log.w(TAG, "High memory usage detected: $memoryPercent%")
                        performMemoryCleanup()
                    }

                    delay(10000) // Check every 10 seconds
                } catch (e: Exception) {
                    Log.e(TAG, "Error monitoring memory usage", e)
                }
            }
        }
    }

    /**
     * Perform aggressive memory cleanup
     */
    fun performMemoryCleanup() {
        scope.launch {
            try {
                // Clear image cache partially
                imageCache.evictAll()
                
                // Clean up object cache
                cleanupObjectCache()
                
                // Clear temporary files
                clearTempFiles()
                
                // Suggest garbage collection
                System.gc()
                
                Log.i(TAG, "Memory cleanup completed")
            } catch (e: Exception) {
                Log.e(TAG, "Error during memory cleanup", e)
            }
        }
    }

    private suspend fun clearTempFiles() = withContext(Dispatchers.IO) {
        try {
            val tempDir = File(context.cacheDir, "temp")
            if (tempDir.exists()) {
                tempDir.listFiles()?.forEach { file ->
                    if (file.lastModified() < System.currentTimeMillis() - 24 * 60 * 60 * 1000) {
                        file.delete()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing temp files", e)
        }
    }

    /**
     * Get current memory usage statistics
     */
    fun getMemoryStats(): MemoryStats {
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        
        return MemoryStats(
            usedMemoryMB = (usedMemory / (1024 * 1024)).toInt(),
            maxMemoryMB = (runtime.maxMemory() / (1024 * 1024)).toInt(),
            freeMemoryMB = (runtime.freeMemory() / (1024 * 1024)).toInt(),
            imageCacheSize = imageCache.size(),
            imageCacheMaxSize = imageCache.maxSize(),
            objectCacheSize = objectCache.size
        )
    }

    /**
     * Create memory-efficient list processing
     */
    fun <T> createMemoryEfficientProcessor(): MemoryEfficientProcessor<T> {
        return MemoryEfficientProcessor()
    }
}

data class MemoryStats(
    val usedMemoryMB: Int,
    val maxMemoryMB: Int,
    val freeMemoryMB: Int,
    val imageCacheSize: Int,
    val imageCacheMaxSize: Int,
    val objectCacheSize: Int
) {
    val memoryUsagePercent: Int
        get() = ((usedMemoryMB.toDouble() / maxMemoryMB) * 100).toInt()
}

/**
 * Memory-efficient processor for large datasets
 */
class MemoryEfficientProcessor<T> {
    private val processedItems = mutableListOf<T>()
    private var batchCount = 0
    
    suspend fun processBatch(
        items: List<T>,
        processor: suspend (T) -> T
    ): List<T> = withContext(Dispatchers.Default) {
        val results = mutableListOf<T>()
        
        items.forEach { item ->
            try {
                val processed = processor(item)
                results.add(processed)
                
                // Yield periodically to prevent blocking
                if (results.size % 10 == 0) {
                    yield()
                }
            } catch (e: Exception) {
                Log.e("MemoryEfficientProcessor", "Error processing item", e)
            }
        }
        
        batchCount++
        Log.d("MemoryEfficientProcessor", "Processed batch $batchCount with ${results.size} items")
        
        results
    }
    
    fun getProcessedCount(): Int = processedItems.size
    fun getBatchCount(): Int = batchCount
}