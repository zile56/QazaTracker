package com.zilehasnain.qazatracker.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.zilehasnain.qazatracker.domain.model.PrayerCalculationMethod
import com.zilehasnain.qazatracker.domain.model.RegionSelection
import com.zilehasnain.qazatracker.domain.repository.PrayerTimesSettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val COUNTRY_KEY = stringPreferencesKey("prayer_times_country")
private val METHOD_KEY = stringPreferencesKey("prayer_times_method")
private val CUSTOM_FAJR_KEY = doublePreferencesKey("prayer_times_custom_fajr")
private val CUSTOM_ISHA_KEY = doublePreferencesKey("prayer_times_custom_isha")
private val SEEN_REGION_FINDER_KEY = booleanPreferencesKey("has_seen_region_finder")

class PrayerTimesSettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : PrayerTimesSettingsRepository {

    override fun observeSelection(): Flow<RegionSelection> = dataStore.data.map { prefs ->
        RegionSelection(
            countryCode = prefs[COUNTRY_KEY],
            methodOverride = prefs[METHOD_KEY]
                ?.let { runCatching { PrayerCalculationMethod.valueOf(it) }.getOrNull() },
            customFajrAngle = prefs[CUSTOM_FAJR_KEY] ?: RegionSelection.DefaultCustomAngle,
            customIshaAngle = prefs[CUSTOM_ISHA_KEY] ?: RegionSelection.DefaultCustomAngle
        )
    }

    override fun observeHasSeenRegionFinder(): Flow<Boolean> =
        dataStore.data.map { prefs -> prefs[SEEN_REGION_FINDER_KEY] ?: false }

    override suspend fun setCountry(countryCode: String) {
        dataStore.edit { prefs ->
            prefs[COUNTRY_KEY] = countryCode.uppercase()
            prefs.remove(METHOD_KEY)
            prefs[SEEN_REGION_FINDER_KEY] = true
        }
    }

    override suspend fun setMethod(method: PrayerCalculationMethod) {
        dataStore.edit { prefs -> prefs[METHOD_KEY] = method.name }
    }

    override suspend fun setCustomAngles(fajrAngle: Double, ishaAngle: Double) {
        dataStore.edit { prefs ->
            prefs[CUSTOM_FAJR_KEY] = fajrAngle
            prefs[CUSTOM_ISHA_KEY] = ishaAngle
            prefs[METHOD_KEY] = PrayerCalculationMethod.CUSTOM.name
            prefs[SEEN_REGION_FINDER_KEY] = true
        }
    }

    override suspend fun markRegionFinderSeen() {
        dataStore.edit { prefs -> prefs[SEEN_REGION_FINDER_KEY] = true }
    }
}
