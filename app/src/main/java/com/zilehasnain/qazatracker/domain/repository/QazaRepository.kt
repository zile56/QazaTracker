package com.zilehasnain.qazatracker.domain.repository

import com.zilehasnain.qazatracker.domain.model.AdjustmentEntry
import com.zilehasnain.qazatracker.domain.model.AdjustmentReason
import com.zilehasnain.qazatracker.domain.model.CalculationMethod
import com.zilehasnain.qazatracker.domain.model.CompletionEntry
import com.zilehasnain.qazatracker.domain.model.MilestoneEntry
import com.zilehasnain.qazatracker.domain.model.PrayerType
import com.zilehasnain.qazatracker.domain.model.RemainingPrayerCount
import java.time.Instant
import kotlinx.coroutines.flow.Flow

interface QazaRepository {
    suspend fun setBaseline(
        prayerType: PrayerType,
        initialCount: Int,
        calculatedAt: Instant,
        method: CalculationMethod
    )

    suspend fun recordCompletion(entry: CompletionEntry)

    suspend fun recordCompletions(entries: List<CompletionEntry>)

    suspend fun applyAdjustment(
        prayerType: PrayerType,
        delta: Int,
        reason: AdjustmentReason,
        note: String?,
        timestamp: Instant
    )

    fun observeRemainingCounts(): Flow<List<RemainingPrayerCount>>

    fun observeRemainingCount(prayerType: PrayerType): Flow<RemainingPrayerCount?>

    /** True once at least one prayer type has a confirmed baseline. */
    fun observeHasBaseline(): Flow<Boolean>

    /** The moment the baseline was confirmed (all five rows share one timestamp), or null if unset. */
    fun observeBaselineStartedAt(): Flow<Instant?>

    /** Raw completion log rows, most recent first — grouping/collapsing is a domain concern. */
    fun observeCompletionLogs(): Flow<List<CompletionEntry>>

    /** Raw adjustment log rows, most recent first. */
    fun observeAdjustments(): Flow<List<AdjustmentEntry>>

    suspend fun recordMilestone(entry: MilestoneEntry)

    /** Completed-all-prayers milestones, most recent first. */
    fun observeMilestones(): Flow<List<MilestoneEntry>>
}
