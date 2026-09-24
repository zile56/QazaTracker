package com.zilehasnain.qazatracker.data.repository

import android.Manifest
import android.content.pm.PackageManager
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.core.app.ApplicationProvider
import com.zilehasnain.qazatracker.domain.model.PrayerCalculationMethod
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class PrayerTimesSettingsRepositoryImplTest {

    private lateinit var repository: PrayerTimesSettingsRepositoryImpl

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val file = File(context.cacheDir, "test_prayer_times_${System.nanoTime()}.preferences_pb")
        repository = PrayerTimesSettingsRepositoryImpl(PreferenceDataStoreFactory.create(produceFile = { file }))
    }

    @Test
    fun `nothing is chosen at first`() = runTest {
        val selection = repository.observeSelection().first()

        assertNull(selection.countryCode)
        assertNull(selection.methodOverride)
        assertFalse(repository.observeHasSeenRegionFinder().first())
    }

    @Test
    fun `a chosen country persists and marks the finder as seen`() = runTest {
        repository.setCountry("pk")

        assertEquals("PK", repository.observeSelection().first().countryCode)
        assertTrue(repository.observeHasSeenRegionFinder().first())
    }

    @Test
    fun `an alternative method persists, and picking a new country clears it`() = runTest {
        repository.setCountry("PK")
        repository.setMethod(PrayerCalculationMethod.MWL)
        assertEquals(PrayerCalculationMethod.MWL, repository.observeSelection().first().methodOverride)

        repository.setCountry("EG")

        assertNull(repository.observeSelection().first().methodOverride)
    }

    @Test
    fun `custom angles persist and switch the method to custom`() = runTest {
        repository.setCustomAngles(16.5, 14.5)

        val selection = repository.observeSelection().first()
        assertEquals(16.5, selection.customFajrAngle, 0.0)
        assertEquals(14.5, selection.customIshaAngle, 0.0)
        assertEquals(PrayerCalculationMethod.CUSTOM, selection.methodOverride)
    }

    @Test
    fun `the app asks for approximate location only and never for the internet`() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val requested = context.packageManager
            .getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
            .requestedPermissions.orEmpty().toList()

        assertFalse(requested.contains(Manifest.permission.INTERNET))
        assertFalse(requested.contains(Manifest.permission.ACCESS_FINE_LOCATION))
        assertTrue(requested.contains(Manifest.permission.ACCESS_COARSE_LOCATION))
    }
}
