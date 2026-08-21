package com.example.qazatracker.domain.usecase

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.qazatracker.data.local.QazaDatabase
import com.example.qazatracker.data.repository.QazaRepositoryImpl
import com.example.qazatracker.domain.model.CalculationMethod
import com.example.qazatracker.domain.model.PrayerType
import java.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class LogCompletionUseCaseTest {

    private lateinit var db: QazaDatabase
    private lateinit var repository: QazaRepositoryImpl
    private lateinit var logCompletion: LogCompletionUseCase

    private val fixedInstant = Instant.parse("2026-03-01T00:00:00Z")

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            QazaDatabase::class.java
        ).allowMainThreadQueries().build()

        repository = QazaRepositoryImpl(
            baselineSnapshotDao = db.baselineSnapshotDao(),
            adjustmentLogDao = db.adjustmentLogDao(),
            completionLogDao = db.completionLogDao(),
            prayerLedgerDao = db.prayerLedgerDao()
        )

        logCompletion = LogCompletionUseCase(repository, now = { fixedInstant })
    }

    @After
    fun tearDown() {
        db.close()
    }

    private suspend fun seedBaseline(count: Int = 100) {
        PrayerType.entries.forEach { type ->
            repository.setBaseline(type, count, fixedInstant, CalculationMethod.EXACT_DATES)
        }
    }

    @Test
    fun `batch creates one completion per day times selected prayer type`() = runTest {
        seedBaseline()

        logCompletion.batch(listOf(PrayerType.FAJR, PrayerType.DHUHR), days = 7)

        val entries = db.completionLogDao().observeAll().first()
        assertEquals(14, entries.size) // 7 days x 2 prayer types

        val countByType = entries.groupingBy { it.prayerType }.eachCount()
        assertEquals(7, countByType[PrayerType.FAJR])
        assertEquals(7, countByType[PrayerType.DHUHR])
        assertTrue(PrayerType.ASR !in countByType)
    }

    @Test
    fun `batch entries share a single batch id`() = runTest {
        seedBaseline()

        logCompletion.batch(PrayerType.entries.toList(), days = 3)

        val entries = db.completionLogDao().observeAll().first()
        assertEquals(15, entries.size) // 3 days x 5 prayer types

        val batchIds = entries.map { it.batchId }.toSet()
        assertEquals(1, batchIds.size)
        assertTrue(batchIds.single() != null)
    }

    @Test
    fun `separate batch calls get distinct batch ids`() = runTest {
        seedBaseline()

        logCompletion.batch(listOf(PrayerType.FAJR), days = 2)
        logCompletion.batch(listOf(PrayerType.FAJR), days = 3)

        val entries = db.completionLogDao().observeAll().first()
        assertEquals(5, entries.size)

        val batchIds = entries.map { it.batchId }.toSet()
        assertEquals(2, batchIds.size)
    }

    @Test
    fun `dashboard aggregation reflects the batch immediately after it is written`() = runTest {
        seedBaseline(count = 50)

        val before = repository.observeRemainingCounts().first().sumOf { it.remaining }
        logCompletion.batch(PrayerType.entries.toList(), days = 4)
        val after = repository.observeRemainingCounts().first().sumOf { it.remaining }

        // Before: 5 x 50 = 250 remaining. After: 4 days x 5 types = 20 completed -> 230 remaining.
        assertEquals(250, before)
        assertEquals(230, after)
    }
}
