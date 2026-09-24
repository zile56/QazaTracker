package com.zilehasnain.qazatracker.domain.model

import java.time.Instant
import java.time.LocalDate

/** "Every missed prayer of this type has been made up." */
enum class PrayerMilestone(val prayerType: PrayerType) {
    COMPLETE_FAJR(PrayerType.FAJR),
    COMPLETE_DHUHR(PrayerType.DHUHR),
    COMPLETE_ASR(PrayerType.ASR),
    COMPLETE_MAGHRIB(PrayerType.MAGHRIB),
    COMPLETE_ISHA(PrayerType.ISHA);

    companion object {
        fun forPrayer(prayerType: PrayerType): PrayerMilestone = entries.first { it.prayerType == prayerType }
    }
}

/** A milestone as stored: which prayer type, and the moment its last missed prayer was completed. */
data class MilestoneEntry(
    val prayerType: PrayerType,
    val achievedAt: Instant
)

/** A milestone as shown. [isNewToday] is true when it was achieved on today's date. */
data class MilestoneData(
    val milestone: PrayerMilestone,
    val milestoneAchievedDate: LocalDate,
    val isNewToday: Boolean
)
