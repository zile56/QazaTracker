package com.zilehasnain.qazatracker.domain.usecase

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.zilehasnain.qazatracker.data.local.QazaDatabase
import com.zilehasnain.qazatracker.data.repository.QazaRepositoryImpl
import com.zilehasnain.qazatracker.domain.model.MilestoneData
import com.zilehasnain.qazatracker.domain.model.MilestoneEntry
import com.zilehasnain.qazatracker.domain.model.PrayerMilestone
import com.zilehasnain.qazatracker.domain.model.PrayerType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
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
class ObserveMilestonesUseCaseTest {

    private lateinit var db: QazaDatabase
    private lateinit var repository: QazaRepositoryImpl

    private val today = LocalDate.of(2026, 3, 10)

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
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun observe(zone: ZoneId = ZoneOffset.UTC) =
        ObserveMilestonesUseCase(repository, today = { today }, zone = { zone })()

    @Test
    fun `no milestones yet gives an empty list`() = runTest {
        assertTrue(observe().first().isEmpty())
    }

    @Test
    fun `a milestone achieved today is new today`() = runTest {
        repository.recordMilestone(MilestoneEntry(PrayerType.FAJR, Instant.parse("2026-03-10T05:00:00Z")))

        assertEquals(
            listOf(MilestoneData(PrayerMilestone.COMPLETE_FAJR, today, isNewToday = true)),
            observe().first()
        )
    }

    @Test
    fun `an older milestone is not new today but keeps its achieved date`() = runTest {
        repository.recordMilestone(MilestoneEntry(PrayerType.ISHA, Instant.parse("2026-03-04T20:00:00Z")))

        val milestone = observe().first().single()

        assertEquals(PrayerMilestone.COMPLETE_ISHA, milestone.milestone)
        assertEquals(LocalDate.of(2026, 3, 4), milestone.milestoneAchievedDate)
        assertEquals(false, milestone.isNewToday)
    }

    @Test
    fun `milestones come back most recent first`() = runTest {
        repository.recordMilestone(MilestoneEntry(PrayerType.FAJR, Instant.parse("2026-03-01T10:00:00Z")))
        repository.recordMilestone(MilestoneEntry(PrayerType.ASR, Instant.parse("2026-03-09T10:00:00Z")))
        repository.recordMilestone(MilestoneEntry(PrayerType.DHUHR, Instant.parse("2026-03-05T10:00:00Z")))

        assertEquals(
            listOf(PrayerMilestone.COMPLETE_ASR, PrayerMilestone.COMPLETE_DHUHR, PrayerMilestone.COMPLETE_FAJR),
            observe().first().map { it.milestone }
        )
    }

    @Test
    fun `the achieved date follows the given timezone`() = runTest {
        // 22:00 UTC on Mar 9 is already Mar 10 (03:00) in Karachi.
        repository.recordMilestone(MilestoneEntry(PrayerType.MAGHRIB, Instant.parse("2026-03-09T22:00:00Z")))

        val milestone = observe(ZoneId.of("Asia/Karachi")).first().single()

        assertEquals(today, milestone.milestoneAchievedDate)
        assertTrue(milestone.isNewToday)
    }

    @Test
    fun `every prayer type maps to its own milestone`() {
        PrayerType.entries.forEach { type ->
            assertEquals(type, PrayerMilestone.forPrayer(type).prayerType)
        }
        assertEquals(PrayerType.entries.size, PrayerMilestone.entries.size)
    }
}
