package com.zilehasnain.qazatracker.domain.usecase

import com.zilehasnain.qazatracker.domain.model.AchievementsData
import com.zilehasnain.qazatracker.domain.model.AdjustmentEntry
import com.zilehasnain.qazatracker.domain.model.CompletionEntry
import com.zilehasnain.qazatracker.domain.model.MilestoneAchievement
import com.zilehasnain.qazatracker.domain.model.MilestoneEntry
import com.zilehasnain.qazatracker.domain.model.ProgressAchievement
import com.zilehasnain.qazatracker.domain.model.RemainingPrayerCount
import com.zilehasnain.qazatracker.domain.model.StreakAchievement
import com.zilehasnain.qazatracker.domain.model.StreakData
import com.zilehasnain.qazatracker.domain.repository.QazaRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Everything the user has unlocked, derived from the same logs as the rest of the app, so
 * there is nothing extra to store or keep in sync:
 *  - milestones come from the milestone log (one row per time a prayer type was finished);
 *  - the streak comes from [CalculateStreakUseCase] over the completion log;
 *  - progress badges come from replaying completions and adjustments in order, noting the day
 *    overall progress first reached each of 25, 50, 75 and 100 percent.
 */
class GetAllAchievementsUseCase(
    private val repository: QazaRepository,
    private val calculateStreak: CalculateStreakUseCase,
    private val today: () -> LocalDate = LocalDate::now,
    private val zone: () -> ZoneId = ZoneId::systemDefault
) {
    @Inject constructor(
        repository: QazaRepository,
        calculateStreak: CalculateStreakUseCase
    ) : this(repository, calculateStreak, LocalDate::now, ZoneId::systemDefault)

    operator fun invoke(): Flow<AchievementsData> =
        combine(
            repository.observeMilestones(),
            repository.observeCompletionLogs(),
            repository.observeAdjustments(),
            repository.observeRemainingCounts()
        ) { milestones, completions, adjustments, counts ->
            val currentZone = zone()
            calculate(
                milestones = milestones,
                completions = completions,
                adjustments = adjustments,
                counts = counts,
                streak = calculateStreak(completions, today(), currentZone),
                zone = currentZone
            )
        }

    companion object {
        val PROGRESS_THRESHOLDS = listOf(25, 50, 75, 100)

        /** The whole derivation as a pure function of the logs. */
        fun calculate(
            milestones: List<MilestoneEntry>,
            completions: List<CompletionEntry>,
            adjustments: List<AdjustmentEntry>,
            counts: List<RemainingPrayerCount>,
            streak: StreakData,
            zone: ZoneId
        ): AchievementsData = AchievementsData(
            milestones = milestones.map { MilestoneAchievement(it.prayerType, it.achievedAt.toLocalDate(zone)) },
            streaks = streakAchievements(streak),
            progressBadges = progressBadges(completions, adjustments, counts, zone)
        )

        /**
         * The streak still going, then the best one if it is a different run. When the current
         * streak *is* the best (or is broken), only one entry appears.
         */
        private fun streakAchievements(streak: StreakData): List<StreakAchievement> = buildList {
            val lastLogged = streak.lastLoggedDate
            if (streak.currentStreak > 0 && lastLogged != null) {
                add(StreakAchievement(streak.currentStreak, lastLogged, isCurrent = true))
            }
            val bestEnded = streak.longestStreakEndedOn
            if (streak.longestStreak > streak.currentStreak && bestEnded != null) {
                add(StreakAchievement(streak.longestStreak, bestEnded, isCurrent = false))
            }
        }

        /**
         * Overall progress is completed / owed, where owed is baseline plus adjustments so far.
         * Nothing stores the baseline total or a history of the percentage, but both follow from
         * the formula remaining = baseline + adjustments - completed: today's owed total
         * (completed + remaining) minus every adjustment leaves the baseline. Replaying the events
         * in time order then finds the first moment each threshold was met. A badge stays unlocked
         * if later adjustments push the percentage back down.
         */
        private fun progressBadges(
            completions: List<CompletionEntry>,
            adjustments: List<AdjustmentEntry>,
            counts: List<RemainingPrayerCount>,
            zone: ZoneId
        ): List<ProgressAchievement> {
            if (counts.isEmpty()) return emptyList() // no baseline yet, so nothing to measure against

            val owedNow = counts.sumOf { it.completed + it.remaining }.toLong()
            var owed = owedNow - adjustments.sumOf { it.delta }
            var completed = 0L

            // Same-instant events: adjustments first, so a batch and its correction read consistently.
            val events = adjustments.map { Event(it.timestamp, completedDelta = 0, owedDelta = it.delta) } +
                completions.map { Event(it.timestamp, completedDelta = 1, owedDelta = 0) }

            val reachedOn = mutableMapOf<Int, LocalDate>()
            events.sortedWith(compareBy<Event> { it.at }.thenBy { it.completedDelta }).forEach { event ->
                completed += event.completedDelta
                owed += event.owedDelta
                if (owed > 0) {
                    PROGRESS_THRESHOLDS.filter { it !in reachedOn && completed * 100 >= it * owed }
                        .forEach { reachedOn[it] = event.at.toLocalDate(zone) }
                }
            }

            return PROGRESS_THRESHOLDS.mapNotNull { percent ->
                reachedOn[percent]?.let { ProgressAchievement(percent, it) }
            }
        }

        private class Event(val at: Instant, val completedDelta: Int, val owedDelta: Int)

        private fun Instant.toLocalDate(zone: ZoneId): LocalDate = atZone(zone).toLocalDate()
    }
}
