package com.zilehasnain.qazatracker.domain.model

/**
 * How the sun's angle below the horizon is turned into Fajr and Isha. Only those two differ
 * between methods; Dhuhr, Asr and Maghrib are the same astronomy everywhere.
 *
 * Isha is either an angle or, for the Gulf, a fixed number of minutes after Maghrib.
 */
enum class PrayerCalculationMethod(
    val displayName: String,
    val fajrAngle: Double,
    val ishaAngle: Double = 0.0,
    val ishaIntervalMinutes: Int = 0
) {
    KARACHI("Karachi (University of Islamic Sciences)", 18.0, 18.0),
    EGYPTIAN("Egyptian General Authority", 19.5, 17.5),
    GULF("Gulf / Umm al-Qura", 18.5, ishaIntervalMinutes = 90),
    ISNA("North America (ISNA)", 15.0, 15.0),
    MIQAT("Miqat (Southeast Asia)", 20.0, 18.0),
    MWL("Muslim World League", 18.0, 17.0),
    TEHRAN("Tehran (Institute of Geophysics)", 17.7, 14.0),
    JAFARI("Jafari (Shia Ithna-Ashari)", 16.0, 14.0),

    /** The angles come from the user, not from this enum. */
    CUSTOM("Custom angles", 18.0, 18.0);

    /** "18° / 18°", or "18.5° / 90 min" for methods that fix Isha by interval. */
    fun anglesSummary(fajr: Double = fajrAngle, isha: Double = ishaAngle): String {
        val ishaText = if (ishaIntervalMinutes > 0) "$ishaIntervalMinutes min" else "${trim(isha)}°"
        return "${trim(fajr)}° / $ishaText"
    }

    private fun trim(value: Double): String =
        if (value == value.toInt().toDouble()) value.toInt().toString() else value.toString()
}
