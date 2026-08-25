package com.example.qazatracker.domain.usecase

import com.example.qazatracker.domain.model.AdjustmentEntry
import com.example.qazatracker.domain.model.CompletionEntry
import com.example.qazatracker.domain.model.HistoryEntry
import com.example.qazatracker.domain.repository.QazaRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Merges completions and adjustments into one chronological timeline. Completions that
 * share a batchId (from a single batch-logging submission) collapse into one
 * [HistoryEntry.BatchCompletion] instead of one row per underlying log entry.
 */
class ObserveHistoryUseCase @Inject constructor(
    private val repository: QazaRepository
) {
    operator fun invoke(): Flow<List<HistoryEntry>> =
        combine(
            repository.observeCompletionLogs(),
            repository.observeAdjustments()
        ) { completions, adjustments -> buildTimeline(completions, adjustments) }

    private fun buildTimeline(
        completions: List<CompletionEntry>,
        adjustments: List<AdjustmentEntry>
    ): List<HistoryEntry> {
        val (batched, single) = completions.partition { it.batchId != null }

        val batchEntries = batched
            .groupBy { it.batchId }
            .map { (batchId, entries) ->
                val prayerTypes = entries.map { it.prayerType }.toSet()
                HistoryEntry.BatchCompletion(
                    batchId = requireNotNull(batchId),
                    prayerTypes = prayerTypes,
                    days = entries.size / prayerTypes.size,
                    totalCount = entries.size,
                    timestamp = entries.first().timestamp
                )
            }

        val singleEntries = single.map { HistoryEntry.SingleCompletion(it.prayerType, it.timestamp) }

        val adjustmentEntries = adjustments.map {
            HistoryEntry.Adjustment(it.prayerType, it.delta, it.reason, it.note, it.timestamp)
        }

        return (batchEntries + singleEntries + adjustmentEntries).sortedByDescending { it.timestamp }
    }
}
