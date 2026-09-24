package com.zilehasnain.qazatracker.ui.tutorial

import com.zilehasnain.qazatracker.fakes.FakeSettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TutorialViewModelTest {

    private lateinit var settingsRepository: FakeSettingsRepository
    private lateinit var viewModel: TutorialViewModel

    @Before
    fun setUp() {
        // viewModelScope runs on Dispatchers.Main, which doesn't exist in a plain JVM test.
        Dispatchers.setMain(UnconfinedTestDispatcher())
        settingsRepository = FakeSettingsRepository()
        viewModel = TutorialViewModel(settingsRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `the tutorial counts as unseen until it is dismissed`() {
        assertFalse(settingsRepository.hasSeenTutorial.value)
    }

    @Test
    fun `dismissing marks the tutorial as seen and then continues`() {
        var doneCalls = 0

        viewModel.onDismiss { doneCalls++ }

        assertTrue(settingsRepository.hasSeenTutorial.value)
        assertEquals(1, doneCalls)
    }

    @Test
    fun `the flag is saved before the continue callback runs`() {
        var seenWhenDone: Boolean? = null

        viewModel.onDismiss { seenWhenDone = settingsRepository.hasSeenTutorial.value }

        assertEquals(true, seenWhenDone)
    }

    @Test
    fun `a double tap only continues once`() {
        var doneCalls = 0

        viewModel.onDismiss { doneCalls++ }
        viewModel.onDismiss { doneCalls++ }

        assertEquals(1, doneCalls)
    }
}
