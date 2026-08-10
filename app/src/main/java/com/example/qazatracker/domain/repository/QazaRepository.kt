package com.example.qazatracker.domain.repository

import com.example.qazatracker.domain.model.AdjustmentReason
import com.example.qazatracker.domain.model.CalculationMethod
import com.example.qazatracker.domain.model.CompletionEntry
import com.example.qazatracker.domain.model.PrayerType
import com.example.qazatracker.domain.model.RemainingPrayerCount
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
}
