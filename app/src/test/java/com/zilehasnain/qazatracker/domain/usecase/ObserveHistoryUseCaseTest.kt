package com.zilehasnain.qazatracker.domain.usecase

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.zilehasnain.qazatracker.data.local.QazaDatabase
import com.zilehasnain.qazatracker.data.repository.QazaRepositoryImpl
import com.zilehasnain.qazatracker.domain.model.AdjustmentReason
import com.zilehasnain.qazatracker.domain.model.CalculationMethod
import com.zilehasnain.qazatracker.domain.model.HistoryEntry
import com.zilehasnain.qazatracker.domain.model.PrayerType
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
class ObserveHistoryUseCaseTest {

    private lateinit var db: QazaDatabase
    private lateinit var repository: QazaRepositoryImpl
    private lateinit var logCompletion: LogCompletionUseCase
    private lateinit var applyAdjustment: ApplyAdjustmentUseCase
    private lateinit var observeHistory: ObserveHistoryUseCase

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
        applyAdjustment = ApplyAdjustmentUseCase(repository, now = { fixedInstant })
        observeHistory = ObserveHistoryUseCase(repository)
    }

    @After
    fun tearDown() {
        db.close()
    }

    private suspend fun seedBaseline() {
        PrayerType.entries.forEach {
            repository.setBaseline(it, 100, fixedInstant, CalculationMethod.EXACT_DATES)
        }
    }

    @Test
    fun `N completions sharing a batchId collapse into one history entry, not N`() = runTest {
        seedBaseline()

        // 7 days x 3 prayer types = 21 individual CompletionLog rows, one batch call.
        logCompletion.batch(listOf(PrayerType.FAJR, PrayerType.DHUHR, PrayerType.ASR), days = 7)

        val entries = observeHistory().first()

        assertEquals(1, entries.size)
        val batch = entries.single() as HistoryEntry.BatchCompletion
        assertEquals(21, batch.totalCount)
        assertEquals(7, batch.days)
        assertEquals(setOf(PrayerType.FAJR, PrayerType.DHUHR, PrayerType.ASR), batch.prayerTypes)
    }

    @Test
    fun `two separate batch calls produce two separate grouped entries`() = runTest {
        seedBaseline()

        logCompletion.batch(listOf(PrayerType.FAJR), days = 3)
        logCompletion.batch(listOf(PrayerType.ISHA), days = 5)

        val entries = observeHistory().first()

        assertEquals(2, entries.size)
        assertTrue(entries.all { it is HistoryEntry.BatchCompletion })
    }

    @Test
    fun `single quick-log completions are not collapsed together`() = runTest {
        seedBaseline()

        logCompletion(PrayerType.FAJR)
        logCompletion(PrayerType.FAJR)
        logCompletion(PrayerType.DHUHR)

        val entries = observeHistory().first()

        // Three separate single-completion rows, not merged despite sharing a prayer type/timestamp source.
        assertEquals(3, entries.size)
        assertTrue(entries.all { it is HistoryEntry.SingleCompletion })
    }

    @Test
    fun `adjustment entries carry their reason through correctly`() = runTest {
        seedBaseline()

        applyAdjustment(PrayerType.MAGHRIB, delta = -5, reason = AdjustmentReason.MANUAL_CORRECTION, note = "Overcounted")
        applyAdjustment(PrayerType.ASR, delta = 10, reason = AdjustmentReason.RECALCULATION)
        applyAdjustment(PrayerType.FAJR, delta = -3, reason = AdjustmentReason.EXEMPTION)

        val entries = observeHistory().first().filterIsInstance<HistoryEntry.Adjustment>()
        val byPrayerType = entries.associateBy { it.prayerType }

        assertEquals(AdjustmentReason.MANUAL_CORRECTION, byPrayerType.getValue(PrayerType.MAGHRIB).reason)
        assertEquals(-5, byPrayerType.getValue(PrayerType.MAGHRIB).delta)
        assertEquals("Overcounted", byPrayerType.getValue(PrayerType.MAGHRIB).note)

        assertEquals(AdjustmentReason.RECALCULATION, byPrayerType.getValue(PrayerType.ASR).reason)
        assertEquals(AdjustmentReason.EXEMPTION, byPrayerType.getValue(PrayerType.FAJR).reason)
    }

    @Test
    fun `timeline merges completions and adjustments sorted most recent first`() = runTest {
        seedBaseline()

        applyAdjustmentAt(
            PrayerType.FAJR,
            delta = 5,
            reason = AdjustmentReason.RECALCULATION,
            timestamp = Instant.parse("2026-01-01T00:00:00Z")
        )
        logCompletionAt(PrayerType.DHUHR, Instant.parse("2026-02-01T00:00:00Z"))
        logCompletionAt(PrayerType.ASR, Instant.parse("2026-03-01T00:00:00Z"))

        val entries = observeHistory().first()

        assertEquals(3, entries.size)
        assertEquals(
            listOf(
                Instant.parse("2026-03-01T00:00:00Z"),
                Instant.parse("2026-02-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z")
            ),
            entries.map { it.timestamp }
        )
    }

    private suspend fun logCompletionAt(prayerType: PrayerType, timestamp: Instant) {
        LogCompletionUseCase(repository, now = { timestamp })(prayerType)
    }

    private suspend fun applyAdjustmentAt(
        prayerType: PrayerType,
        delta: Int,
        reason: AdjustmentReason,
        timestamp: Instant,
        note: String? = null
    ) {
        ApplyAdjustmentUseCase(repository, now = { timestamp })(prayerType, delta, reason, note)
    }
}
