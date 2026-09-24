package com.zilehasnain.qazatracker.domain.usecase

import com.zilehasnain.qazatracker.domain.model.CompletionEntry
import com.zilehasnain.qazatracker.domain.model.CompletionEstimate
import com.zilehasnain.qazatracker.domain.model.CompletionProjection
import com.zilehasnain.qazatracker.domain.model.EstimateConfidence
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.math.ceil

/**
 * Projects a completion date from pace (completions per day elapsed since tracking began),
 * not a fixed daily target — this is a backlog, and pace naturally varies.
 *
 * Takes the completion log rather than a bare count so it can also report how many distinct
 * days were logged, which is what confidence is based on.
 */
class ProjectCompletionDateUseCase @Inject constructor() {

    operator fun invoke(
        totalRemaining: Int,
        completions: List<CompletionEntry>,
        trackingStartedAt: LocalDate,
        today: LocalDate = LocalDate.now(),
        zone: ZoneId = ZoneId.systemDefault()
    ): CompletionProjection {
        if (totalRemaining <= 0) return CompletionProjection.AlreadyCaughtUp

        val daysTracked = ChronoUnit.DAYS.between(trackingStartedAt, today)
        val totalCompleted = completions.size
        if (daysTracked <= 0 || totalCompleted <= 0) return CompletionProjection.InsufficientData

        val pacePerDay = totalCompleted.toDouble() / daysTracked
        // A vanishingly small pace can't be allowed to overflow an Int.
        val daysNeeded = ceil(totalRemaining / pacePerDay).toLong().coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        val daysLogged = completions.map { it.timestamp.atZone(zone).toLocalDate() }.distinct().size

        return CompletionProjection.Estimated(
            CompletionEstimate(
                estimatedDate = today.plusDays(daysNeeded.toLong()),
                daysRemaining = daysNeeded,
                averagePrayersPerDay = pacePerDay.toFloat(),
                confidence = EstimateConfidence.forDaysLogged(daysLogged),
                trackingStartedOn = trackingStartedAt,
                daysTracked = daysTracked.toInt(),
                daysLogged = daysLogged,
                prayersCompleted = totalCompleted,
                prayersRemaining = totalRemaining
            )
        )
    }
}
