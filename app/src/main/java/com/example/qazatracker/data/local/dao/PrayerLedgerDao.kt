package com.example.qazatracker.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.qazatracker.domain.model.PrayerType
import kotlinx.coroutines.flow.Flow

/**
 * Aggregation-only reads over the three ledger tables. Nothing here is stored —
 * remaining is always baseline + adjustments - completions, computed per query.
 */
@Dao
interface PrayerLedgerDao {
    @Query(
        """
        SELECT b.prayerType AS prayerType,
               b.initialCount + COALESCE(adj.totalDelta, 0) - COALESCE(comp.completedCount, 0) AS remaining,
               COALESCE(comp.completedCount, 0) AS completed
        FROM BaselineSnapshot b
        LEFT JOIN (
            SELECT prayerType, SUM(delta) AS totalDelta
            FROM AdjustmentLog
            GROUP BY prayerType
        ) adj ON adj.prayerType = b.prayerType
        LEFT JOIN (
            SELECT prayerType, COUNT(*) AS completedCount
            FROM CompletionLog
            GROUP BY prayerType
        ) comp ON comp.prayerType = b.prayerType
        """
    )
    fun observeRemainingCounts(): Flow<List<RemainingCount>>

    @Query(
        """
        SELECT b.prayerType AS prayerType,
               b.initialCount + COALESCE(adj.totalDelta, 0) - COALESCE(comp.completedCount, 0) AS remaining,
               COALESCE(comp.completedCount, 0) AS completed
        FROM BaselineSnapshot b
        LEFT JOIN (
            SELECT prayerType, SUM(delta) AS totalDelta
            FROM AdjustmentLog
            WHERE prayerType = :prayerType
            GROUP BY prayerType
        ) adj ON adj.prayerType = b.prayerType
        LEFT JOIN (
            SELECT prayerType, COUNT(*) AS completedCount
            FROM CompletionLog
            WHERE prayerType = :prayerType
            GROUP BY prayerType
        ) comp ON comp.prayerType = b.prayerType
        WHERE b.prayerType = :prayerType
        """
    )
    fun observeRemainingCount(prayerType: PrayerType): Flow<RemainingCount?>
}

data class RemainingCount(
    val prayerType: PrayerType,
    val remaining: Int,
    val completed: Int
)
