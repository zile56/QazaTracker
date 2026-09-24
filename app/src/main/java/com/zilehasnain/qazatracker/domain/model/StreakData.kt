package com.zilehasnain.qazatracker.domain.model

import java.time.LocalDate

/**
 * A run of consecutive calendar days on which at least one prayer was logged. Always derived
 * from the completion log, never stored, like every other count in the app.
 *
 * [lastLoggedDate] is null only when nothing has ever been logged. [streakBrokenDate] is the
 * first missed day after the last logged one, and is null while the streak is still alive.
 */
data class StreakData(
    val currentStreak: Int,
    val longestStreak: Int,
    val lastLoggedDate: LocalDate?,
    val streakBrokenDate: LocalDate? = null,
    /** The last day of the best run (the most recent one, on a tie); null when nothing is logged. */
    val longestStreakEndedOn: LocalDate? = null
) {
    companion object {
        val None = StreakData(currentStreak = 0, longestStreak = 0, lastLoggedDate = null)
    }
}
