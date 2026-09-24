package com.zilehasnain.qazatracker.domain.usecase

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.zilehasnain.qazatracker.data.local.QazaDatabase
import com.zilehasnain.qazatracker.data.repository.QazaRepositoryImpl
import com.zilehasnain.qazatracker.domain.model.CalculationMethod
import com.zilehasnain.qazatracker.domain.model.PrayerType
import com.zilehasnain.qazatracker.domain.model.ProgressAchievement
import com.zilehasnain.qazatracker.domain.model.StreakAchievement
import java.time.Instant
import java.time.LocalDate
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

/**
 * The real thing end to end: a real in-memory database, prayers logged through the same use case
 * the app uses (so milestones are recorded for real), and the achievements read back from the flow.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class GetAllAchievementsFlowTest {

    private lateinit var db: QazaDatabase
    private lateinit var repository: QazaRepositoryImpl
    private lateinit var getAchievements: GetAllAchievementsUseCase

    private val march10 = LocalDate.of(2026, 3, 10)

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
        getAchievements = GetAllAchievementsUseCase(
            repository,
            CalculateStreakUseCase(),
            today = { march10 },
            zone = { ZoneOffset.UTC }
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    private suspend fun seedBaseline(perType: Int) {
        PrayerType.entries.forEach {
            repository.setBaseline(it, perType, Instant.parse("2026-03-01T00:00:00Z"), CalculationMethod.EXACT_DATES)
        }
    }

    private suspend fun logAt(type: PrayerType, iso: String, times: Int) {
        val logger = LogCompletionUseCase(repository, now = { Instant.parse(iso) })
        repeat(times) { logger(type) }
    }

    @Test
    fun `a fresh install has nothing unlocked`() = runTest {
        seedBaseline(perType = 2)

        assertTrue(getAchievements().first().isEmpty)
    }

    @Test
    fun `working through all five prayers over three days unlocks everything`() = runTest {
        seedBaseline(perType = 2) // 10 owed in all
        logAt(PrayerType.FAJR, "2026-03-08T05:00:00Z", 2) // Fajr done: 2/10 = 20%
        logAt(PrayerType.DHUHR, "2026-03-09T12:00:00Z", 2) // 4/10 = 40%   -> 25%
        logAt(PrayerType.ASR, "2026-03-10T10:00:00Z", 2) // 6/10 = 60%     -> 50%
        logAt(PrayerType.MAGHRIB, "2026-03-10T11:00:00Z", 2) // 8/10 = 80% -> 75%
        logAt(PrayerType.ISHA, "2026-03-10T12:00:00Z", 2) // 10/10 = 100%  -> 100%

        val data = getAchievements().first()

        // Milestones: every prayer was finished, most recent first.
        assertEquals(
            listOf(PrayerType.ISHA, PrayerType.MAGHRIB, PrayerType.ASR, PrayerType.DHUHR, PrayerType.FAJR),
            data.milestones.map { it.prayerType }
        )
        assertEquals(LocalDate.of(2026, 3, 8), data.milestones.last().achievedDate)
        assertEquals(march10, data.milestones.first().achievedDate)

        // Progress: each threshold dated to the day it was crossed.
        assertEquals(
            listOf(
                ProgressAchievement(25, LocalDate.of(2026, 3, 9)),
                ProgressAchievement(50, march10),
                ProgressAchievement(75, march10),
                ProgressAchievement(100, march10)
            ),
            data.progressBadges
        )

        // Streak: three consecutive days ending today, and it is the best, so a single entry.
        assertEquals(listOf(StreakAchievement(3, march10, isCurrent = true)), data.streaks)
    }

    @Test
    fun `a batch that finishes a prayer type unlocks its milestone and matching progress`() = runTest {
        seedBaseline(perType = 5) // 25 owed in all
        LogCompletionUseCase(repository, now = { Instant.parse("2026-03-10T09:00:00Z") })
            .batch(listOf(PrayerType.FAJR, PrayerType.ISHA), days = 5) // 10 done: 40%

        val data = getAchievements().first()

        assertEquals(setOf(PrayerType.FAJR, PrayerType.ISHA), data.milestones.map { it.prayerType }.toSet())
        assertEquals(listOf(25), data.progressBadges.map { it.percentage })
    }

    @Test
    fun `the flow updates as more is logged`() = runTest {
        seedBaseline(perType = 2)
        logAt(PrayerType.FAJR, "2026-03-10T05:00:00Z", 2)
        assertEquals(1, getAchievements().first().milestones.size)

        logAt(PrayerType.DHUHR, "2026-03-10T06:00:00Z", 2)

        assertEquals(2, getAchievements().first().milestones.size)
    }
}
