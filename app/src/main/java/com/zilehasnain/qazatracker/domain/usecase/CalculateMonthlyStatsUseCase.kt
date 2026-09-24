package com.zilehasnain.qazatracker.domain.usecase

import com.zilehasnain.qazatracker.domain.model.CompletionEntry
import com.zilehasnain.qazatracker.domain.model.MonthlyStatsData
import com.zilehasnain.qazatracker.domain.model.PrayerType
import com.zilehasnain.qazatracker.domain.model.RemainingPrayerCount
import com.zilehasnain.qazatracker.domain.repository.QazaRepository
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Per-prayer-type completions for one calendar month, derived from the completion log like
 * everything else, so it updates live as prayers are logged. Months are cut in [zone] (the
 * device's, by default), so a prayer logged at 11:30 pm counts toward that local day's month.
 */
class CalculateMonthlyStatsUseCase(
    private val repository: QazaRepository,
    private val zone: () -> ZoneId = ZoneId::systemDefault
) {
    @Inject constructor(repository: QazaRepository) : this(repository, ZoneId::systemDefault)

    operator fun invoke(month: YearMonth): Flow<MonthlyStatsData> =
        combine(
            repository.observeCompletionLogs(),
            repository.observeRemainingCounts()
        ) { completions, counts -> calculate(month, completions, counts, zone()) }

    companion object {
        /** The whole calculation, as a pure function of the logs, so it needs no repository to test. */
        fun calculate(
            month: YearMonth,
            completions: List<CompletionEntry>,
            counts: List<RemainingPrayerCount>,
            zone: ZoneId
        ): MonthlyStatsData {
            val completedByType = completions
                .filter { YearMonth.from(it.timestamp.atZone(zone)) == month }
                .groupingBy { it.prayerType }
                .eachCount()

            val prayerStats = PrayerType.entries.associateWith { completedByType[it] ?: 0 }

            // Owed in all = completed ever + remaining (= baseline + adjustments). Remaining can
            // be negative when over-logged, so floor at 0 rather than shrinking the total.
            val owedByType = counts.associate { it.prayerType to (it.completed + it.remaining).coerceAtLeast(0) }
            val prayerTotals = PrayerType.entries.associateWith { owedByType[it] ?: 0 }

            val totalCompleted = prayerStats.values.sum()
            val totalOwed = prayerTotals.values.sum()

            return MonthlyStatsData(
                month = month,
                prayerStats = prayerStats,
                prayerTotals = prayerTotals,
                totalCompleted = totalCompleted,
                totalOwed = totalOwed,
                completionRate = MonthlyStatsData.percentOf(totalCompleted, totalOwed)
            )
        }
    }
}
