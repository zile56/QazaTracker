package com.zilehasnain.qazatracker.ui.statistics

import androidx.lifecycle.viewModelScope
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.zilehasnain.qazatracker.data.local.QazaDatabase
import com.zilehasnain.qazatracker.data.repository.QazaRepositoryImpl
import com.zilehasnain.qazatracker.domain.model.CalculationMethod
import com.zilehasnain.qazatracker.domain.model.PrayerType
import com.zilehasnain.qazatracker.domain.usecase.CalculateMonthlyStatsUseCase
import com.zilehasnain.qazatracker.domain.usecase.LogCompletionUseCase
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneOffset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Real in-memory database with logs spread across months, the clock fixed to September 2026 and
 * tracking having begun in July 2026 — so month changes, bounds and counts are all end to end.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class StatisticsViewModelTest {

    private lateinit var db: QazaDatabase
    private lateinit var repository: QazaRepositoryImpl
    private lateinit var viewModel: StatisticsViewModel

    private val july = YearMonth.of(2026, 7)
    private val august = YearMonth.of(2026, 8)
    private val september = YearMonth.of(2026, 9)

    @Before
    fun setUp() {
        // viewModelScope runs on Dispatchers.Main, which a plain JVM/Robolectric test doesn't drive.
        Dispatchers.setMain(UnconfinedTestDispatcher())

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
        if (::viewModel.isInitialized) viewModel.viewModelScope.cancel()
        db.close()
        Dispatchers.resetMain()
    }

    private suspend fun seedBaseline(startedAt: String = "2026-07-15T08:00:00Z", count: Int = 50) {
        PrayerType.entries.forEach {
            repository.setBaseline(it, count, Instant.parse(startedAt), CalculationMethod.EXACT_DATES)
        }
    }

    private suspend fun logAt(type: PrayerType, isoInstant: String, times: Int = 1) {
        val logger = LogCompletionUseCase(repository, now = { Instant.parse(isoInstant) })
        repeat(times) { logger(type) }
    }

    private fun createViewModel() {
        viewModel = StatisticsViewModel(
            calculateMonthlyStats = CalculateMonthlyStatsUseCase(repository, zone = { ZoneOffset.UTC }),
            repository = repository,
            currentMonth = september,
            zone = ZoneOffset.UTC
        )
    }

    @Test
    fun `starts on the current month and shows that month's counts`() = runTest {
        seedBaseline()
        logAt(PrayerType.FAJR, "2026-09-10T09:00:00Z", times = 3)
        logAt(PrayerType.ASR, "2026-08-10T09:00:00Z", times = 2)
        createViewModel()

        assertEquals(september, viewModel.selectedMonth.value)
        val stats = viewModel.currentMonthStats.first { it.month == september && it.totalCompleted == 3 }

        assertEquals(3, stats.prayerStats[PrayerType.FAJR])
        assertEquals(0, stats.prayerStats[PrayerType.ASR])
        assertEquals(250, stats.totalOwed)
    }

    @Test
    fun `moving to the previous month shows that month's counts`() = runTest {
        seedBaseline()
        logAt(PrayerType.FAJR, "2026-09-10T09:00:00Z", times = 3)
        logAt(PrayerType.ASR, "2026-08-10T09:00:00Z", times = 2)
        createViewModel()
        viewModel.earliestMonth.first { it == july }

        viewModel.onMonthChanged(august)

        assertEquals(august, viewModel.selectedMonth.value)
        val stats = viewModel.currentMonthStats.first { it.month == august && it.totalCompleted == 2 }
        assertEquals(2, stats.prayerStats[PrayerType.ASR])
        assertEquals(0, stats.prayerStats[PrayerType.FAJR])
    }

    @Test
    fun `going forward again returns to the current month's counts`() = runTest {
        seedBaseline()
        logAt(PrayerType.FAJR, "2026-09-10T09:00:00Z", times = 3)
        createViewModel()
        viewModel.earliestMonth.first { it == july }

        viewModel.onMonthChanged(august)
        viewModel.onMonthChanged(september)

        val stats = viewModel.currentMonthStats.first { it.month == september && it.totalCompleted == 3 }
        assertEquals(3, stats.prayerStats[PrayerType.FAJR])
    }

    @Test
    fun `the earliest month is the month tracking began`() = runTest {
        seedBaseline(startedAt = "2026-07-15T08:00:00Z")
        createViewModel()

        assertEquals(july, viewModel.earliestMonth.first { it == july })
    }

    @Test
    fun `cannot move before the month tracking began`() = runTest {
        seedBaseline()
        createViewModel()
        viewModel.earliestMonth.first { it == july }

        viewModel.onMonthChanged(YearMonth.of(2026, 1))

        assertEquals(july, viewModel.selectedMonth.value)
    }

    @Test
    fun `cannot move past the current month`() = runTest {
        seedBaseline()
        createViewModel()
        viewModel.earliestMonth.first { it == july }

        viewModel.onMonthChanged(YearMonth.of(2026, 10))

        assertEquals(september, viewModel.selectedMonth.value)
    }

    @Test
    fun `a month with nothing logged reports zero, not stale numbers from the month before`() = runTest {
        seedBaseline()
        logAt(PrayerType.FAJR, "2026-09-10T09:00:00Z", times = 3)
        createViewModel()
        viewModel.earliestMonth.first { it == july }
        viewModel.currentMonthStats.first { it.month == september && it.totalCompleted == 3 }

        viewModel.onMonthChanged(july)

        val stats = viewModel.currentMonthStats.first { it.month == july }
        assertEquals(0, stats.totalCompleted)
        assertEquals(0f, stats.completionRate, 0f)
    }
}
