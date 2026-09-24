package com.zilehasnain.qazatracker.domain.usecase

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.zilehasnain.qazatracker.data.local.QazaDatabase
import com.zilehasnain.qazatracker.data.repository.QazaRepositoryImpl
import com.zilehasnain.qazatracker.domain.model.AdjustmentReason
import com.zilehasnain.qazatracker.domain.model.CalculationMethod
import com.zilehasnain.qazatracker.domain.model.MilestoneEntry
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

/** Runs against a real in-memory database, so the before/after remaining counts are the real ones. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class CheckMilestoneUseCaseTest {

    private lateinit var db: QazaDatabase
    private lateinit var repository: QazaRepositoryImpl
    private lateinit var logCompletion: LogCompletionUseCase
    private lateinit var applyAdjustment: ApplyAdjustmentUseCase

    private val fixedInstant = Instant.parse("2026-03-01T09:30:00Z")

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
            prayerLedgerDao = db.prayerLedgerDao(),
            milestoneDao = db.milestoneDao()
        )
        logCompletion = LogCompletionUseCase(repository, now = { fixedInstant })
        applyAdjustment = ApplyAdjustmentUseCase(repository, now = { fixedInstant })
    }

    @After
    fun tearDown() {
        db.close()
    }

    private suspend fun seedBaseline(count: Int) {
        PrayerType.entries.forEach {
            repository.setBaseline(it, count, fixedInstant, CalculationMethod.EXACT_DATES)
        }
    }

    private suspend fun milestones(): List<MilestoneEntry> = repository.observeMilestones().first()

    @Test
    fun `logging the final missed prayer of a type records a milestone for it`() = runTest {
        seedBaseline(count = 3)

        repeat(2) { logCompletion(PrayerType.FAJR) }
        assertTrue("2 of 3 done, nothing yet", milestones().isEmpty())

        logCompletion(PrayerType.FAJR)

        assertEquals(listOf(MilestoneEntry(PrayerType.FAJR, fixedInstant)), milestones())
    }

    @Test
    fun `finishing one type doesn't record milestones for the others`() = runTest {
        seedBaseline(count = 1)

        logCompletion(PrayerType.ASR)

        assertEquals(listOf(PrayerType.ASR), milestones().map { it.prayerType })
    }

    @Test
    fun `logging another prayer afterwards doesn't repeat the milestone`() = runTest {
        seedBaseline(count = 1)
        logCompletion(PrayerType.FAJR)

        // Over-logging past zero: remaining goes negative, but it was already finished.
        logCompletion(PrayerType.FAJR)
        logCompletion(PrayerType.FAJR)

        assertEquals(1, milestones().count { it.prayerType == PrayerType.FAJR })
    }

    @Test
    fun `a batch that overshoots past zero records exactly one milestone`() = runTest {
        seedBaseline(count = 5)

        logCompletion.batch(listOf(PrayerType.MAGHRIB), days = 10)

        assertEquals(1, milestones().size)
        assertEquals(PrayerType.MAGHRIB, milestones().single().prayerType)
    }

    @Test
    fun `a batch that finishes several types records one milestone per type`() = runTest {
        seedBaseline(count = 4)

        logCompletion.batch(listOf(PrayerType.FAJR, PrayerType.DHUHR, PrayerType.ISHA), days = 4)

        assertEquals(
            setOf(PrayerType.FAJR, PrayerType.DHUHR, PrayerType.ISHA),
            milestones().map { it.prayerType }.toSet()
        )
        assertEquals(3, milestones().size)
    }

    @Test
    fun `a batch that stops short of finishing records nothing`() = runTest {
        seedBaseline(count = 10)

        logCompletion.batch(PrayerType.entries.toList(), days = 9)

        assertTrue(milestones().isEmpty())
    }

    @Test
    fun `a manual adjustment down to zero is not a completion and records nothing`() = runTest {
        seedBaseline(count = 5)

        applyAdjustment(PrayerType.FAJR, -5, AdjustmentReason.MANUAL_CORRECTION, null)

        assertTrue(milestones().isEmpty())
    }

    @Test
    fun `finishing a type again after adding missed prayers back is a second milestone`() = runTest {
        seedBaseline(count = 1)
        logCompletion(PrayerType.FAJR)

        applyAdjustment(PrayerType.FAJR, 2, AdjustmentReason.MANUAL_CORRECTION, null)
        repeat(2) { logCompletion(PrayerType.FAJR) }

        assertEquals(2, milestones().count { it.prayerType == PrayerType.FAJR })
    }

    @Test
    fun `a prayer type without a baseline can never trigger a milestone`() = runTest {
        // No baseline seeded at all.
        logCompletion(PrayerType.FAJR)

        assertTrue(milestones().isEmpty())
    }
}
