package com.zilehasnain.qazatracker.ui.achievements

import androidx.lifecycle.viewModelScope
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.zilehasnain.qazatracker.data.local.QazaDatabase
import com.zilehasnain.qazatracker.data.repository.QazaRepositoryImpl
import com.zilehasnain.qazatracker.domain.model.CalculationMethod
import com.zilehasnain.qazatracker.domain.model.PrayerType
import com.zilehasnain.qazatracker.domain.usecase.CalculateStreakUseCase
import com.zilehasnain.qazatracker.domain.usecase.GetAllAchievementsUseCase
import com.zilehasnain.qazatracker.domain.usecase.LogCompletionUseCase
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class AchievementsViewModelTest {

    private lateinit var db: QazaDatabase
    private lateinit var repository: QazaRepositoryImpl
    private lateinit var viewModel: AchievementsViewModel

    @Before
    fun setUp() {
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

    private fun createViewModel() {
        viewModel = AchievementsViewModel(
            GetAllAchievementsUseCase(
                repository,
                CalculateStreakUseCase(),
                today = { LocalDate.of(2026, 3, 10) },
                zone = { ZoneOffset.UTC }
            )
        )
    }

    private suspend fun seedBaseline(perType: Int) {
        PrayerType.entries.forEach {
            repository.setBaseline(it, perType, Instant.parse("2026-03-01T00:00:00Z"), CalculationMethod.EXACT_DATES)
        }
    }

    @Test
    fun `it reports not-loaded until the first result arrives, so empty isn't flashed`() = runTest {
        createViewModel()

        assertFalse(viewModel.hasLoaded.value)
        assertTrue(viewModel.achievements.value.isEmpty) // the placeholder, not a verdict
    }

    @Test
    fun `an empty database loads to a genuinely empty result`() = runTest {
        seedBaseline(perType = 3)
        createViewModel()

        backgroundScope.launch { viewModel.achievements.collect { } }
        viewModel.hasLoaded.first { it }

        assertTrue(viewModel.achievements.value.isEmpty)
    }

    @Test
    fun `logged prayers show up as achievements once loaded`() = runTest {
        seedBaseline(perType = 2)
        val logger = LogCompletionUseCase(repository, now = { Instant.parse("2026-03-10T05:00:00Z") })
        repeat(2) { logger(PrayerType.FAJR) }
        createViewModel()

        val data = viewModel.achievements.first { !it.isEmpty }

        assertEquals(listOf(PrayerType.FAJR), data.milestones.map { it.prayerType })
        assertEquals(1, data.streaks.size)
        assertTrue(viewModel.hasLoaded.value)
    }
}
