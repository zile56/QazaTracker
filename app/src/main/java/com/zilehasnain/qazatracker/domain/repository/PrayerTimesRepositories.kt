package com.zilehasnain.qazatracker.domain.repository

import com.zilehasnain.qazatracker.domain.model.LocationData
import com.zilehasnain.qazatracker.domain.model.PrayerCalculationMethod
import com.zilehasnain.qazatracker.domain.model.RegionSelection
import kotlinx.coroutines.flow.Flow

/** The user's region and method choices, kept on the device. */
interface PrayerTimesSettingsRepository {
    fun observeSelection(): Flow<RegionSelection>

    /** False until the user has finished the Find Your Region dialog once. */
    fun observeHasSeenRegionFinder(): Flow<Boolean>

    /** Picking a country resets any earlier method override, so the new country's suggestion applies. */
    suspend fun setCountry(countryCode: String)

    suspend fun setMethod(method: PrayerCalculationMethod)

    suspend fun setCustomAngles(fajrAngle: Double, ishaAngle: Double)

    suspend fun markRegionFinderSeen()
}

/** Where the device is, roughly. Nothing leaves the device. */
interface LocationProvider {
    fun hasPermission(): Boolean

    /** Null when permission is missing or the device has no recent position to offer. */
    suspend fun currentLocation(): LocationData?
}
