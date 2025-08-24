package com.beaconledger.welltrack.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.data.database.dao.*

@Database(
    entities = [
        User::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class WellTrackDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao

    companion object {
        const val DATABASE_NAME = "welltrack_database"
    }
}