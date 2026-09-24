package com.zilehasnain.qazatracker.domain.model

import java.time.Instant

/** Approximate device position, used only to feed the on-device calculation. */
data class LocationData(val latitude: Double, val longitude: Double) {
    companion object {
        /** Rawalpindi: what's shown until the device can supply a position. */
        val Default = LocationData(33.5651, 73.0169)
    }
}

/** One day's times. Each is an instant; the screen renders them in the device's time zone. */
data class PrayerTimeData(
    val fajr: Instant,
    val sunrise: Instant,
    val dhuhr: Instant,
    val asr: Instant,
    val maghrib: Instant,
    val isha: Instant
) {
    fun timeFor(prayerType: PrayerType): Instant = when (prayerType) {
        PrayerType.FAJR -> fajr
        PrayerType.DHUHR -> dhuhr
        PrayerType.ASR -> asr
        PrayerType.MAGHRIB -> maghrib
        PrayerType.ISHA -> isha
    }
}

/** What the user has chosen; the method is null until they override the country's recommendation. */
data class RegionSelection(
    val countryCode: String?,
    val methodOverride: PrayerCalculationMethod?,
    val customFajrAngle: Double,
    val customIshaAngle: Double
) {
    companion object {
        const val DefaultCustomAngle = 18.0
    }
}
