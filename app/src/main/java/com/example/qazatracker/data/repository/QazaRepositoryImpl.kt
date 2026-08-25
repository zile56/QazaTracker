package com.example.qazatracker.data.repository

import com.example.qazatracker.data.local.dao.AdjustmentLogDao
import com.example.qazatracker.data.local.dao.BaselineSnapshotDao
import com.example.qazatracker.data.local.dao.CompletionLogDao
import com.example.qazatracker.data.local.dao.PrayerLedgerDao
import com.example.qazatracker.data.local.entity.AdjustmentLog
import com.example.qazatracker.data.local.entity.BaselineSnapshot
import com.example.qazatracker.data.local.entity.CompletionLog
import com.example.qazatracker.domain.model.AdjustmentEntry
import com.example.qazatracker.domain.model.AdjustmentReason
import com.example.qazatracker.domain.model.CalculationMethod
import com.example.qazatracker.domain.model.CompletionEntry
import com.example.qazatracker.domain.model.PrayerType
import com.example.qazatracker.domain.model.RemainingPrayerCount
import com.example.qazatracker.domain.repository.QazaRepository
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class QazaRepositoryImpl @Inject constructor(
    private val baselineSnapshotDao: BaselineSnapshotDao,
    private val adjustmentLogDao: AdjustmentLogDao,
    private val completionLogDao: CompletionLogDao,
    private val prayerLedgerDao: PrayerLedgerDao
) : QazaRepository {

    override suspend fun setBaseline(
        prayerType: PrayerType,
        initialCount: Int,
        calculatedAt: Instant,
        method: CalculationMethod
    ) {
        baselineSnapshotDao.upsert(
            BaselineSnapshot(
                prayerType = prayerType,
                initialCount = initialCount,
                calculatedAt = calculatedAt,
                method = method
            )
        )
    }

    override suspend fun recordCompletion(entry: CompletionEntry) {
        completionLogDao.insert(entry.toEntity())
    }

    override suspend fun recordCompletions(entries: List<CompletionEntry>) {
        completionLogDao.insertAll(entries.map { it.toEntity() })
    }

    override suspend fun applyAdjustment(
        prayerType: PrayerType,
        delta: Int,
        reason: AdjustmentReason,
        note: String?,
        timestamp: Instant
    ) {
        adjustmentLogDao.insert(
            AdjustmentLog(
                prayerType = prayerType,
                delta = delta,
                reason = reason,
                note = note,
                timestamp = timestamp
            )
        )
    }

    override fun observeRemainingCounts(): Flow<List<RemainingPrayerCount>> =
        prayerLedgerDao.observeRemainingCounts().map { rows ->
            rows.map { RemainingPrayerCount(it.prayerType, it.remaining, it.completed) }
        }

    override fun observeRemainingCount(prayerType: PrayerType): Flow<RemainingPrayerCount?> =
        prayerLedgerDao.observeRemainingCount(prayerType).map {
            it?.let { row -> RemainingPrayerCount(row.prayerType, row.remaining, row.completed) }
        }

    override fun observeHasBaseline(): Flow<Boolean> =
        baselineSnapshotDao.observeAll().map { it.isNotEmpty() }

    override fun observeBaselineStartedAt(): Flow<Instant?> =
        baselineSnapshotDao.observeAll().map { snapshots -> snapshots.minOfOrNull { it.calculatedAt } }

    override fun observeCompletionLogs(): Flow<List<CompletionEntry>> =
        completionLogDao.observeAll().map { rows ->
            rows.map { CompletionEntry(it.prayerType, it.timestamp, it.batchId) }
        }

    override fun observeAdjustments(): Flow<List<AdjustmentEntry>> =
        adjustmentLogDao.observeAll().map { rows ->
            rows.map { AdjustmentEntry(it.prayerType, it.delta, it.reason, it.note, it.timestamp) }
        }

    private fun CompletionEntry.toEntity() =
        CompletionLog(prayerType = prayerType, timestamp = timestamp, batchId = batchId)
}
