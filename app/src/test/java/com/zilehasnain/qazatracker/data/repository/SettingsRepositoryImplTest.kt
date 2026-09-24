package com.zilehasnain.qazatracker.data.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.core.app.ApplicationProvider
import com.zilehasnain.qazatracker.domain.model.NotificationFrequency
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Exercises the real DataStore Preferences implementation (a temp file per test, not a
 * fake) — this is what proves frequency selections actually persist, not just that the
 * ViewModel calls through to a repository.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class SettingsRepositoryImplTest {

    private lateinit var repository: SettingsRepositoryImpl
    private lateinit var backingFile: File

    @Before
    fun createRepository() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        backingFile = File(context.cacheDir, "test_settings_${System.nanoTime()}.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(produceFile = { backingFile })
        repository = SettingsRepositoryImpl(dataStore)
    }

    @Test
    fun `defaults to weekly before any selection is made`() = runTest {
        assertEquals(NotificationFrequency.WEEKLY, repository.observeNotificationFrequency().first())
    }

    @Test
    fun `setting a frequency persists it for subsequent reads`() = runTest {
        repository.setNotificationFrequency(NotificationFrequency.DAILY)

        assertEquals(NotificationFrequency.DAILY, repository.observeNotificationFrequency().first())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `a new repository instance reading the same store sees the persisted value`() = runTest {
        // A dedicated scope for the first instance, so it can be torn down (DataStore
        // holds an exclusive lock on its backing file for as long as its scope is alive) —
        // otherwise opening a second DataStore on the same file throws IllegalStateException.
        val firstScope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher())
        val firstDataStore = PreferenceDataStoreFactory.create(
            scope = firstScope,
            produceFile = { backingFile }
        )
        SettingsRepositoryImpl(firstDataStore).setNotificationFrequency(NotificationFrequency.NEVER)
        firstScope.cancel()

        val reopened = SettingsRepositoryImpl(
            PreferenceDataStoreFactory.create(produceFile = { backingFile })
        )

        assertEquals(NotificationFrequency.NEVER, reopened.observeNotificationFrequency().first())
    }

    @Test
    fun `the tutorial counts as unseen on a fresh install`() = runTest {
        assertFalse(repository.observeHasSeenTutorial().first())
    }

    @Test
    fun `marking the tutorial seen persists it`() = runTest {
        repository.markTutorialSeen()

        assertTrue(repository.observeHasSeenTutorial().first())
    }

    @Test
    fun `marking the tutorial seen twice is harmless and stays seen`() = runTest {
        repository.markTutorialSeen()
        repository.markTutorialSeen()

        assertTrue(repository.observeHasSeenTutorial().first())
    }

    @Test
    fun `the tutorial flag doesn't disturb the notification frequency`() = runTest {
        repository.setNotificationFrequency(NotificationFrequency.DAILY)
        repository.markTutorialSeen()

        assertEquals(NotificationFrequency.DAILY, repository.observeNotificationFrequency().first())
    }

    @Test
    fun `switching frequencies overwrites the previous selection`() = runTest {
        repository.setNotificationFrequency(NotificationFrequency.BIWEEKLY)
        repository.setNotificationFrequency(NotificationFrequency.NEVER)

        assertEquals(NotificationFrequency.NEVER, repository.observeNotificationFrequency().first())
    }
}
