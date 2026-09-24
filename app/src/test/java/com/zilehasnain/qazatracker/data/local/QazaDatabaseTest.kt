package com.zilehasnain.qazatracker.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.zilehasnain.qazatracker.data.local.entity.AdjustmentLog
import com.zilehasnain.qazatracker.data.local.entity.BaselineSnapshot
import com.zilehasnain.qazatracker.data.local.entity.CompletionLog
import com.zilehasnain.qazatracker.domain.model.AdjustmentReason
import com.zilehasnain.qazatracker.domain.model.CalculationMethod
import com.zilehasnain.qazatracker.domain.model.PrayerType
import java.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class QazaDatabaseTest {

    private lateinit var db: QazaDatabase

    @Before
    fun createDb() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            QazaDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    private suspend fun seedBaseline(prayerType: PrayerType, initialCount: Int) {
        db.baselineSnapshotDao().upsert(
            BaselineSnapshot(
                prayerType = prayerType,
                initialCount = initialCount,
                calculatedAt = Instant.parse("2026-01-01T00:00:00Z"),
                method = CalculationMethod.EXACT_DATES
            )
        )
    }

    @Test
    fun `insert and read a baseline snapshot`() = runTest {
        seedBaseline(PrayerType.FAJR, 120)

        val stored = db.baselineSnapshotDao().observeByType(PrayerType.FAJR).first()

        assertNotNull(stored)
        assertEquals(120, stored!!.initialCount)
        assertEquals(CalculationMethod.EXACT_DATES, stored.method)
    }

    @Test
    fun `insert and read completion logs`() = runTest {
        val entry = CompletionLog(
            prayerType = PrayerType.DHUHR,
            timestamp = Instant.parse("2026-01-05T12:00:00Z"),
            batchId = "batch-1"
        )
        db.completionLogDao().insert(entry)

        val stored = db.completionLogDao().observeByType(PrayerType.DHUHR).first()

        assertEquals(1, stored.size)
        assertEquals("batch-1", stored.first().batchId)
    }

    @Test
    fun `remaining equals baseline minus completions when no adjustments`() = runTest {
        seedBaseline(PrayerType.ASR, 100)
        repeat(30) {
            db.completionLogDao().insert(
                CompletionLog(prayerType = PrayerType.ASR, timestamp = Instant.now())
            )
        }

        val remaining = db.prayerLedgerDao().observeRemainingCount(PrayerType.ASR).first()

        assertEquals(70, remaining?.remaining)
    }

    @Test
    fun `aggregation is computed independently per prayer type`() = runTest {
        seedBaseline(PrayerType.FAJR, 200)
        seedBaseline(PrayerType.MAGHRIB, 50)

        repeat(10) {
            db.completionLogDao().insert(
                CompletionLog(prayerType = PrayerType.FAJR, timestamp = Instant.now())
            )
        }
        db.completionLogDao().insert(
            CompletionLog(prayerType = PrayerType.MAGHRIB, timestamp = Instant.now())
        )
        db.adjustmentLogDao().insert(
            AdjustmentLog(
                prayerType = PrayerType.MAGHRIB,
                delta = 5,
                reason = AdjustmentReason.RECALCULATION,
                timestamp = Instant.now()
            )
        )

        val counts = db.prayerLedgerDao().observeRemainingCounts().first()
            .associateBy { it.prayerType }

        // 200 initial - 10 completed = 190, untouched by Maghrib's adjustment.
        assertEquals(190, counts.getValue(PrayerType.FAJR).remaining)
        // 50 initial + 5 adjustment - 1 completed = 54.
        assertEquals(54, counts.getValue(PrayerType.MAGHRIB).remaining)
    }

    @Test
    fun `positive adjustments increase remaining`() = runTest {
        seedBaseline(PrayerType.ISHA, 80)
        db.adjustmentLogDao().insert(
            AdjustmentLog(
                prayerType = PrayerType.ISHA,
                delta = 15,
                reason = AdjustmentReason.RECALCULATION,
                note = "Recomputed after correcting age estimate",
                timestamp = Instant.now()
            )
        )

        val remaining = db.prayerLedgerDao().observeRemainingCount(PrayerType.ISHA).first()

        assertEquals(95, remaining?.remaining)
    }

    @Test
    fun `negative adjustment delta reduces remaining`() = runTest {
        seedBaseline(PrayerType.DHUHR, 60)
        db.adjustmentLogDao().insert(
            AdjustmentLog(
                prayerType = PrayerType.DHUHR,
                delta = -12,
                reason = AdjustmentReason.MANUAL_CORRECTION,
                note = "User over-counted missed days",
                timestamp = Instant.now()
            )
        )

        val remaining = db.prayerLedgerDao().observeRemainingCount(PrayerType.DHUHR).first()

        assertEquals(48, remaining?.remaining)
    }

    @Test
    fun `multiple negative adjustments and completions combine correctly`() = runTest {
        seedBaseline(PrayerType.FAJR, 300)
        db.adjustmentLogDao().insert(
            AdjustmentLog(
                prayerType = PrayerType.FAJR,
                delta = -20,
                reason = AdjustmentReason.MANUAL_CORRECTION,
                timestamp = Instant.parse("2026-02-01T00:00:00Z")
            )
        )
        db.adjustmentLogDao().insert(
            AdjustmentLog(
                prayerType = PrayerType.FAJR,
                delta = -5,
                reason = AdjustmentReason.EXEMPTION,
                timestamp = Instant.parse("2026-02-02T00:00:00Z")
            )
        )
        repeat(50) {
            db.completionLogDao().insert(
                CompletionLog(prayerType = PrayerType.FAJR, timestamp = Instant.now())
            )
        }

        val remaining = db.prayerLedgerDao().observeRemainingCount(PrayerType.FAJR).first()

        // 300 - 20 - 5 - 50 = 225
        assertEquals(225, remaining?.remaining)
    }

    @Test
    fun `remaining can go negative when over-logged, formula stays raw`() = runTest {
        seedBaseline(PrayerType.MAGHRIB, 5)
        repeat(8) {
            db.completionLogDao().insert(
                CompletionLog(prayerType = PrayerType.MAGHRIB, timestamp = Instant.now())
            )
        }

        val remaining = db.prayerLedgerDao().observeRemainingCount(PrayerType.MAGHRIB).first()

        assertEquals(-3, remaining?.remaining)
    }
}
