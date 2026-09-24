package com.zilehasnain.qazatracker.domain.usecase

import com.batoulapps.adhan.CalculationParameters
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import com.zilehasnain.qazatracker.domain.model.LocationData
import com.zilehasnain.qazatracker.domain.model.PrayerCalculationMethod
import com.zilehasnain.qazatracker.domain.model.PrayerTimeData
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.Instant
import javax.inject.Inject

/**
 * Works out a day's prayer times with pure astronomy (the Adhan library): no network, no
 * location lookups, nothing stored. Given the same inputs it always returns the same times,
 * so there is nothing worth caching.
 */
class GetPrayerTimesUseCase @Inject constructor() {

    /**
     * Null only when the sun never reaches the needed angle that day (far-polar latitudes),
     * where the library can't produce every time.
     */
    operator fun invoke(
        date: LocalDate,
        location: LocationData,
        method: PrayerCalculationMethod,
        hanafiAsr: Boolean,
        customFajrAngle: Double = method.fajrAngle,
        customIshaAngle: Double = method.ishaAngle
    ): PrayerTimeData? = calculate(date, location, method, hanafiAsr, customFajrAngle, customIshaAngle)

    companion object {
        fun calculate(
            date: LocalDate,
            location: LocationData,
            method: PrayerCalculationMethod,
            hanafiAsr: Boolean,
            customFajrAngle: Double,
            customIshaAngle: Double
        ): PrayerTimeData? {
            val parameters = when {
                method == PrayerCalculationMethod.CUSTOM -> CalculationParameters(customFajrAngle, customIshaAngle)
                method.ishaIntervalMinutes > 0 -> CalculationParameters(method.fajrAngle, method.ishaIntervalMinutes)
                else -> CalculationParameters(method.fajrAngle, method.ishaAngle)
            }
            parameters.madhab = if (hanafiAsr) Madhab.HANAFI else Madhab.SHAFI

            val times = runCatching {
                PrayerTimes(
                    Coordinates(location.latitude, location.longitude),
                    DateComponents(date.year, date.monthValue, date.dayOfMonth),
                    parameters
                )
            }.getOrNull() ?: return null

            return PrayerTimeData(
                fajr = (times.fajr ?: return null).toMinute(),
                sunrise = (times.sunrise ?: return null).toMinute(),
                dhuhr = (times.dhuhr ?: return null).toMinute(),
                asr = (times.asr ?: return null).toMinute(),
                maghrib = (times.maghrib ?: return null).toMinute(),
                isha = (times.isha ?: return null).toMinute()
            )
        }
    }
}

/** The library rounds to the minute but leaks the clock's milliseconds; drop them so results are stable. */
private fun java.util.Date.toMinute(): Instant = toInstant().plusSeconds(30).truncatedTo(ChronoUnit.MINUTES)
