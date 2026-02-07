package com.beaconledger.welltrack

import android.app.Application
import com.beaconledger.welltrack.optimization.MemoryMonitor
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class WellTrackApplication : Application() {

    @Inject
    lateinit var memoryMonitor: MemoryMonitor

    override fun onCreate() {
        super.onCreate()
        memoryMonitor.logMemoryUsage() // Log initial memory usage
    }
}