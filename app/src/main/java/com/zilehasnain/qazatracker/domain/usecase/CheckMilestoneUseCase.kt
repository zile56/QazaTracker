package com.zilehasnain.qazatracker.domain.usecase

import com.zilehasnain.qazatracker.domain.model.MilestoneEntry
import com.zilehasnain.qazatracker.domain.model.PrayerType
import com.zilehasnain.qazatracker.domain.repository.QazaRepository
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * Records a milestone when logging completions takes a prayer type from "still has missed
 * prayers" to "none left". Usage is snapshot, write, check:
 *
 *     val before = checkMilestone.remainingByType()
 *     repository.recordCompletion(...)
 *     checkMilestone(before)
 *
 * It compares before with after, rather than just testing "remaining == 0", so that:
 *  - a batch that overshoots past zero (remaining goes negative) still counts once,
 *  - logging more prayers of an already-finished type doesn't repeat the milestone,
 *  - a type with no baseline at all can never trigger one.
 */
class CheckMilestoneUseCase(
    private val repository: QazaRepository,
    private val now: () -> Instant = Instant::now
) {
    // Dagger can't resolve a `() -> Instant` binding, same as LogCompletionUseCase.
    @Inject constructor(repository: QazaRepository) : this(repository, Instant::now)

    suspend fun remainingByType(): Map<PrayerType, Int> =
        repository.observeRemainingCounts().first().associate { it.prayerType to it.remaining }

    /** @return the prayer types whose milestone was just recorded. */
    suspend operator fun invoke(before: Map<PrayerType, Int>): List<PrayerType> {
        val after = remainingByType()
        val achieved = PrayerType.entries.filter { type ->
            (before[type] ?: 0) > 0 && (after[type] ?: 0) <= 0
        }
        val achievedAt = now()
        achieved.forEach { repository.recordMilestone(MilestoneEntry(it, achievedAt)) }
        return achieved
    }
}
