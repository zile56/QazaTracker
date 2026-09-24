package com.zilehasnain.qazatracker.domain.model

import java.time.YearMonth

/**
 * One calendar month of completions.
 *
 * [prayerStats] counts what was completed in [month]. [prayerTotals] is what each type owes in
 * all: baseline plus adjustments, i.e. completed-ever plus remaining — the same figure the
 * dashboard's progress bars use, so the two screens never disagree. [completionRate] is
 * [totalCompleted] as a percentage (0-100) of [totalOwed].
 */
data class MonthlyStatsData(
    val month: YearMonth,
    val prayerStats: Map<PrayerType, Int>,
    val prayerTotals: Map<PrayerType, Int>,
    val totalCompleted: Int,
    val totalOwed: Int,
    val completionRate: Float
) {
    /** Percentage (0-100) of what this prayer type owes that was completed in the month. */
    fun rateFor(prayerType: PrayerType): Float =
        percentOf(prayerStats[prayerType] ?: 0, prayerTotals[prayerType] ?: 0)

    companion object {
        fun empty(month: YearMonth) = MonthlyStatsData(
            month = month,
            prayerStats = PrayerType.entries.associateWith { 0 },
            prayerTotals = PrayerType.entries.associateWith { 0 },
            totalCompleted = 0,
            totalOwed = 0,
            completionRate = 0f
        )

        /** 0 when nothing is owed; clamped to 100 if over-logged. */
        fun percentOf(completed: Int, owed: Int): Float =
            if (owed <= 0) 0f else (completed * 100f / owed).coerceIn(0f, 100f)
    }
}
