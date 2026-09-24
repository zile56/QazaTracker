package com.zilehasnain.qazatracker.domain.usecase

import com.zilehasnain.qazatracker.domain.model.CompletionProjection
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.math.ceil

/**
 * Projects a completion date from pace (completions per day elapsed since tracking began),
 * not a fixed daily target — this is a backlog, and pace naturally varies.
 */
class ProjectCompletionDateUseCase @Inject constructor() {

    operator fun invoke(
        totalRemaining: Int,
        totalCompleted: Int,
        trackingStartedAt: LocalDate,
        today: LocalDate = LocalDate.now()
    ): CompletionProjection {
        if (totalRemaining <= 0) return CompletionProjection.AlreadyCaughtUp

        val daysElapsed = ChronoUnit.DAYS.between(trackingStartedAt, today)
        if (daysElapsed <= 0 || totalCompleted <= 0) return CompletionProjection.InsufficientData

        val pacePerDay = totalCompleted.toDouble() / daysElapsed
        val daysNeeded = ceil(totalRemaining / pacePerDay).toLong()
        return CompletionProjection.Estimated(
            projectedDate = today.plusDays(daysNeeded),
            daysRemaining = daysNeeded
        )
    }
}
