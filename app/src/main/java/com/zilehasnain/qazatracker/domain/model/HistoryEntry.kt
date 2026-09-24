package com.zilehasnain.qazatracker.domain.model

import java.time.Instant

/**
 * One row in the history/ledger timeline. Batch-logged completions (same batchId) are
 * collapsed into a single [BatchCompletion] rather than one row per underlying
 * CompletionLog — otherwise one batch submission could flood the list with dozens of
 * near-identical rows.
 */
sealed interface HistoryEntry {
    val timestamp: Instant

    data class SingleCompletion(
        val prayerType: PrayerType,
        override val timestamp: Instant
    ) : HistoryEntry

    data class BatchCompletion(
        val batchId: String,
        val prayerTypes: Set<PrayerType>,
        val days: Int,
        val totalCount: Int,
        override val timestamp: Instant
    ) : HistoryEntry

    data class Adjustment(
        val prayerType: PrayerType,
        val delta: Int,
        val reason: AdjustmentReason,
        val note: String?,
        override val timestamp: Instant
    ) : HistoryEntry

    /** The moment every missed prayer of [prayerType] had been made up. */
    data class Milestone(
        val prayerType: PrayerType,
        override val timestamp: Instant
    ) : HistoryEntry
}
