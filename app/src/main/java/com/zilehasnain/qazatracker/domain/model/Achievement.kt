package com.zilehasnain.qazatracker.domain.model

import java.time.LocalDate

/** Something the user has unlocked, and the day it happened. */
sealed interface Achievement {
    val achievedDate: LocalDate
}

/** Every missed prayer of [prayerType] was made up. One per time it happened. */
data class MilestoneAchievement(
    val prayerType: PrayerType,
    override val achievedDate: LocalDate
) : Achievement

/**
 * A run of consecutive days with something logged. [isCurrent] separates the streak still
 * going ([achievedDate] is the last day logged) from the best one ever, when that is a
 * different, earlier run ([achievedDate] is the day it reached its length).
 */
data class StreakAchievement(
    val streakLength: Int,
    override val achievedDate: LocalDate,
    val isCurrent: Boolean
) : Achievement

/** Overall progress reached [percentage] of everything owed: 25, 50, 75 or 100. */
data class ProgressAchievement(
    val percentage: Int,
    override val achievedDate: LocalDate
) : Achievement

data class AchievementsData(
    val milestones: List<MilestoneAchievement>,
    val streaks: List<StreakAchievement>,
    val progressBadges: List<ProgressAchievement>
) {
    val totalCount: Int get() = milestones.size + streaks.size + progressBadges.size
    val isEmpty: Boolean get() = totalCount == 0

    companion object {
        val Empty = AchievementsData(emptyList(), emptyList(), emptyList())
    }
}
