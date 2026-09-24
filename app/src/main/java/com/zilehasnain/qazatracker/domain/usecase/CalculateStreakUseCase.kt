package com.zilehasnain.qazatracker.domain.usecase

import com.zilehasnain.qazatracker.domain.model.CompletionEntry
import com.zilehasnain.qazatracker.domain.model.StreakData
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/**
 * Pure function of the completion log, so there is no counter to keep in sync: logging a prayer
 * simply adds a row, and the next calculation sees it. A day counts if at least one prayer was
 * logged on it (in [zone]), however many.
 *
 * The current streak is the run of consecutive days ending on the last logged day, and it is
 * still alive if that day is today or yesterday (today's log may just not have happened yet).
 * After a longer gap it reads 0, and the next log starts a fresh run of 1.
 */
class CalculateStreakUseCase @Inject constructor() {

    operator fun invoke(
        completions: List<CompletionEntry>,
        today: LocalDate,
        zone: ZoneId
    ): StreakData {
        if (completions.isEmpty()) return StreakData.None

        val loggedDays = completions.map { it.timestamp.atZone(zone).toLocalDate() }.toSortedSet()

        var longest = 0
        var longestEndedOn: LocalDate? = null
        var run = 0
        var previous: LocalDate? = null
        for (day in loggedDays) {
            run = if (previous != null && day == previous.plusDays(1)) run + 1 else 1
            // >= so that on a tie the most recent run is the one reported as the best.
            if (run >= longest) {
                longest = run
                longestEndedOn = day
            }
            previous = day
        }

        // `run` is now the length of the run that ends on the most recent logged day.
        val lastLogged = loggedDays.last()
        val alive = !lastLogged.isBefore(today.minusDays(1))

        return StreakData(
            currentStreak = if (alive) run else 0,
            longestStreak = longest,
            lastLoggedDate = lastLogged,
            streakBrokenDate = if (alive) null else lastLogged.plusDays(1),
            longestStreakEndedOn = longestEndedOn
        )
    }
}
