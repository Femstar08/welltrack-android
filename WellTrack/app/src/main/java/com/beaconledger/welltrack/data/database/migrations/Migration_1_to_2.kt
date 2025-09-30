package com.beaconledger.welltrack.data.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Migration for Goals tracking system and enhanced data export

        // Create indexes for better performance on goals queries
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_goals_userId_targetDate
            ON goals(userId, targetDate)
        """)

        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_goals_userId_type_isActive
            ON goals(userId, type, isActive)
        """)

        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_goal_progress_goalId_recordedAt
            ON goal_progress(goalId, recordedAt)
        """)

        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_goal_milestones_goalId_order
            ON goal_milestones(goalId, `order`)
        """)

        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_goal_predictions_goalId_calculatedAt
            ON goal_predictions(goalId, calculatedAt)
        """)

        // Create indexes for better performance on export queries
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_health_metrics_userId_timestamp
            ON health_metrics(userId, timestamp)
        """)

        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_health_metrics_type_timestamp
            ON health_metrics(type, timestamp)
        """)

        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_meals_userId_timestamp
            ON meals(userId, timestamp)
        """)

        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_supplements_userId_createdAt
            ON supplements(userId, createdAt)
        """)

        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_biomarkers_userId_testDate
            ON biomarkers(userId, testDate)
        """)

        // Create indexes for data sync performance
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_sync_status_entityType_lastSyncTime
            ON sync_status(entityType, lastSyncTime)
        """)

        // Create composite indexes for complex queries
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_health_metrics_userId_type_source
            ON health_metrics(userId, type, source)
        """)

        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_meals_userId_mealType_timestamp
            ON meals(userId, mealType, timestamp)
        """)
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Future migration for additional features
        // This can be used for any future database schema changes
    }
}