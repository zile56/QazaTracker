package com.zilehasnain.qazatracker.domain.model

import java.time.LocalDate

/** How much history backs an estimate, by distinct days on which anything was logged. */
enum class EstimateConfidence(val label: String) {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High");

    companion object {
        const val HIGH_FROM_DAYS = 7
        const val LOW_BELOW_DAYS = 3

        fun forDaysLogged(daysLogged: Int): EstimateConfidence = when {
            daysLogged >= HIGH_FROM_DAYS -> HIGH
            daysLogged < LOW_BELOW_DAYS -> LOW
            else -> MEDIUM
        }
    }
}

/** How far away the estimated finish is, which the dashboard colour-codes. */
enum class EstimateHorizon { WITHIN_THREE_MONTHS, THREE_TO_SIX_MONTHS, OVER_SIX_MONTHS }

/**
 * Where the current pace leads. [averagePrayersPerDay] is prayers completed divided by calendar
 * days since tracking began (days off count, since this is pace over time, not on logging days).
 * [daysLogged] is different: how many distinct days anything was logged, which drives
 * [confidence] — one big batch on a single day is one day of evidence, however many prayers.
 */
data class CompletionEstimate(
    val estimatedDate: LocalDate,
    val daysRemaining: Int,
    val averagePrayersPerDay: Float,
    val confidence: EstimateConfidence,
    val trackingStartedOn: LocalDate,
    val daysTracked: Int,
    val daysLogged: Int,
    val prayersCompleted: Int,
    val prayersRemaining: Int
) {
    /**
     * Share of the whole projected span (tracking start to [estimatedDate]) already elapsed.
     * At a constant pace that equals completed / (completed + remaining), so it always agrees
     * with the dashboard's progress bars.
     */
    val fractionElapsed: Float
        get() {
            val span = daysTracked.toLong() + daysRemaining
            return if (span <= 0) 0f else (daysTracked.toFloat() / span).coerceIn(0f, 1f)
        }

    /** Within 3 calendar months counts as near, up to 6 as medium, beyond that far. */
    fun horizon(today: LocalDate): EstimateHorizon = when {
        !estimatedDate.isAfter(today.plusMonths(3)) -> EstimateHorizon.WITHIN_THREE_MONTHS
        !estimatedDate.isAfter(today.plusMonths(6)) -> EstimateHorizon.THREE_TO_SIX_MONTHS
        else -> EstimateHorizon.OVER_SIX_MONTHS
    }
}
