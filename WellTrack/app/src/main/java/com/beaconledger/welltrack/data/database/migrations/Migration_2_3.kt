package com.beaconledger.welltrack.data.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create user_preferences table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `user_preferences` (
                `userId` TEXT NOT NULL,
                `notificationsEnabled` INTEGER NOT NULL DEFAULT 1,
                `darkModeEnabled` INTEGER NOT NULL DEFAULT 0,
                `language` TEXT NOT NULL DEFAULT 'en',
                `accessibilitySettings` TEXT NOT NULL DEFAULT 'false,false,false,true,false,false,false,false,false',
                PRIMARY KEY(`userId`)
            )
        """)

        // Create data_deletion_records table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `data_deletion_records` (
                `id` TEXT NOT NULL,
                `userId` TEXT NOT NULL,
                `scheduledDate` TEXT NOT NULL,
                `status` TEXT NOT NULL,
                `createdAt` TEXT NOT NULL,
                `completedAt` TEXT,
                `dataCategories` TEXT NOT NULL,
                PRIMARY KEY(`id`)
            )
        """)
    }
}