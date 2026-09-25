package com.zilehasnain.qazatracker.ui.inspiration

import androidx.lifecycle.viewModelScope
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.zilehasnain.qazatracker.data.local.QazaDatabase
import com.zilehasnain.qazatracker.data.repository.InspirationRepositoryImpl
import com.zilehasnain.qazatracker.domain.model.InspirationItem
import com.zilehasnain.qazatracker.domain.usecase.GetDailyInspirationUseCase
import java.time.LocalDate
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class DailyInspirationViewModelTest {

    private lateinit var db: QazaDatabase
    private lateinit var repository: InspirationRepositoryImpl
    private lateinit var viewModel: DailyInspirationViewModel
    private var today = LocalDate.of(2026, 9, 25)
    private val getDaily = GetDailyInspirationUseCase()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), QazaDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = InspirationRepositoryImpl(db.inspirationDao())
        newViewModel()
    }

    private fun newViewModel() {
        viewModel = DailyInspirationViewModel(repository, getDaily, { today }, Random(7))
    }

    @After
    fun tearDown() {
        viewModel.viewModelScope.cancel()
        db.close()
        Dispatchers.resetMain()
    }

    private fun TestScope.collect() {
        backgroundScope.launch { viewModel.uiState.collect { } }
    }

    private suspend fun await(condition: (InspirationUiState) -> Boolean): InspirationUiState =
        viewModel.uiState.first(condition)

    @Test
    fun `it opens on the days own pair`() = runTest {
        collect()

        assertEquals(getDaily(today), viewModel.uiState.value.inspiration)
        assertEquals(viewModel.uiState.value.inspiration.verse, viewModel.dailyVerse.value)
        assertEquals(viewModel.uiState.value.inspiration.hadith, viewModel.dailyHadith.value)
    }


    @Test
    fun `next and previous walk to the neighbouring days and back`() = runTest {
        collect()
        val start = await { it.offset == 0 }.inspiration

        viewModel.onNext()
        val next = await { it.offset == 1 }
        assertEquals(getDaily(today, 1).verse, next.inspiration.verse)
        assertTrue(next.movedForward)

        viewModel.onPrevious()
        val back = await { it.offset == 0 }
        assertEquals(start, back.inspiration)
        assertFalse(back.movedForward)

        viewModel.onPrevious()
        assertEquals(getDaily(today, -1).verse, await { it.offset == -1 }.inspiration.verse)
    }

    @Test
    fun `refresh jumps to a different pair`() = runTest {
        collect()
        val start = await { it.offset == 0 }.inspiration

        viewModel.onRefresh()

        val jumped = await { it.offset != 0 }
        assertNotEquals(start.verse, jumped.inspiration.verse)
    }

    @Test
    fun `a bookmark saves, shows in the saved list with a count, and can be removed`() = runTest {
        collect()
        val verse = InspirationItem.Verse(await { true }.inspiration.verse)
        val saying = InspirationItem.Saying(viewModel.uiState.value.inspiration.hadith)

        viewModel.onBookmark(verse)
        viewModel.onBookmark(saying)

        val saved = await { it.savedCount == 2 }
        assertTrue(saved.isSaved(verse.id))
        assertEquals(setOf(saying.id, verse.id), saved.saved.map { it.id }.toSet())

        viewModel.onBookmark(verse)

        val after = await { it.savedCount == 1 }
        assertFalse(after.isSaved(verse.id))
    }

    @Test
    fun `bookmarks survive a fresh view model, as after a restart`() = runTest {
        collect()
        val verse = InspirationItem.Verse(await { true }.inspiration.verse)
        viewModel.onBookmark(verse)
        await { it.savedCount == 1 }
        viewModel.viewModelScope.cancel()

        newViewModel()
        collect()

        assertEquals(listOf(verse.id), await { it.savedCount == 1 }.saved.map { it.id })
    }

    @Test
    fun `saving twice does not duplicate`() = runTest {
        assertTrue(repository.observeBookmarkedIds().first().isEmpty())
        repository.setBookmarked("q2:153", true)
        repository.setBookmarked("q2:153", true)

        assertEquals(listOf("q2:153"), repository.observeBookmarkedIds().first())
    }

    @Test
    fun `the selected tab is kept in state`() = runTest {
        collect()

        viewModel.onTabSelected(InspirationTab.SAVED)

        assertEquals(InspirationTab.SAVED, await { it.tab == InspirationTab.SAVED }.tab)
    }

    @Test
    fun `a new day gives new picks once the view model is recreated`() = runTest {
        collect()
        val before = await { true }.inspiration
        viewModel.viewModelScope.cancel()

        today = today.plusDays(1)
        newViewModel()
        collect()

        assertNotEquals(before.verse, await { true }.inspiration.verse)
    }
}
