package com.beaconledger.welltrack.data.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.beaconledger.welltrack.data.database.dao.HealthMetricDao
import com.beaconledger.welltrack.data.model.DataSource
import com.beaconledger.welltrack.data.model.HealthMetric
import com.beaconledger.welltrack.data.model.HealthMetricType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(AndroidJUnit4::class)
class HealthMetricDaoIntegrationTest {

    private lateinit var database: WellTrackDatabase
    private lateinit var healthMetricDao: HealthMetricDao

    private val testHealthMetric = HealthMetric(
        id = "metric1",
        userId = "user1",
        type = HealthMetricType.HEART_RATE,
        value = 75.0,
        unit = "bpm",
        timestamp = LocalDateTime.now(),
        source = DataSource.HEALTH_CONNECT,
        confidence = 1.0f,
        isManualEntry = false
    )

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WellTrackDatabase::class.java
        ).allowMainThreadQueries().build()

        healthMetricDao = database.healthMetricDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndRetrieveHealthMetric() = runTest {
        // Insert health metric
        healthMetricDao.insertHealthMetric(testHealthMetric)

        // Retrieve health metric
        val retrievedMetric = healthMetricDao.getHealthMetricById("metric1")

        // Verify
        assertNotNull(retrievedMetric)
        assertEquals(testHealthMetric.id, retrievedMetric.id)
        assertEquals(testHealthMetric.type, retrievedMetric.type)
        assertEquals(testHealthMetric.value, retrievedMetric.value)
        assertEquals(testHealthMetric.source, retrievedMetric.source)
    }

    @Test
    fun getHealthMetricsForUser() = runTest {
        // Insert multiple health metrics
        val metric1 = testHealthMetric.copy(id = "metric1", type = HealthMetricType.HEART_RATE)
        val metric2 = testHealthMetric.copy(id = "metric2", type = HealthMetricType.WEIGHT)
        val metric3 = testHealthMetric.copy(id = "metric3", userId = "user2", type = HealthMetricType.HEART_RATE)

        healthMetricDao.insertHealthMetric(metric1)
        healthMetricDao.insertHealthMetric(metric2)
        healthMetricDao.insertHealthMetric(metric3)

        // Retrieve heart rate metrics for user1
        val heartRateMetrics = healthMetricDao.getHealthMetricsForUser("user1", HealthMetricType.HEART_RATE).first()

        // Verify
        assertEquals(1, heartRateMetrics.size)
        assertEquals("metric1", heartRateMetrics[0].id)
        assertEquals(HealthMetricType.HEART_RATE, heartRateMetrics[0].type)
    }

    @Test
    fun getHealthMetricsByDateRange() = runTest {
        // Insert metrics with different timestamps
        val now = LocalDateTime.now()
        val metric1 = testHealthMetric.copy(id = "metric1", timestamp = now.minusDays(2))
        val metric2 = testHealthMetric.copy(id = "metric2", timestamp = now.minusDays(1))
        val metric3 = testHealthMetric.copy(id = "metric3", timestamp = now)

        healthMetricDao.insertHealthMetric(metric1)
        healthMetricDao.insertHealthMetric(metric2)
        healthMetricDao.insertHealthMetric(metric3)

        // Retrieve metrics from yesterday to now
        val recentMetrics = healthMetricDao.getHealthMetricsByDateRange(
            "user1",
            HealthMetricType.HEART_RATE,
            now.minusDays(1),
            now
        ).first()

        // Verify
        assertEquals(2, recentMetrics.size)
        assertEquals("metric2", recentMetrics[0].id)
        assertEquals("metric3", recentMetrics[1].id)
    }

    @Test
    fun deleteHealthMetric() = runTest {
        // Insert health metric
        healthMetricDao.insertHealthMetric(testHealthMetric)

        // Verify insertion
        assertNotNull(healthMetricDao.getHealthMetricById("metric1"))

        // Delete health metric
        healthMetricDao.deleteHealthMetric("metric1")

        // Verify deletion
        val deletedMetric = healthMetricDao.getHealthMetricById("metric1")
        assertEquals(null, deletedMetric)
    }

    @Test
    fun getLatestHealthMetric() = runTest {
        // Insert metrics with different timestamps
        val now = LocalDateTime.now()
        val older = testHealthMetric.copy(id = "older", timestamp = now.minusHours(2))
        val newer = testHealthMetric.copy(id = "newer", timestamp = now)

        healthMetricDao.insertHealthMetric(older)
        healthMetricDao.insertHealthMetric(newer)

        // Get latest metric
        val latestMetric = healthMetricDao.getLatestHealthMetric("user1", HealthMetricType.HEART_RATE)

        // Verify
        assertNotNull(latestMetric)
        assertEquals("newer", latestMetric.id)
    }
}