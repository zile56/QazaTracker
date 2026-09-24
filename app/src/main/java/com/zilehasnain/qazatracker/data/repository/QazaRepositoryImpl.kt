package com.zilehasnain.qazatracker.data.repository

import com.zilehasnain.qazatracker.data.local.dao.AdjustmentLogDao
import com.zilehasnain.qazatracker.data.local.dao.BaselineSnapshotDao
import com.zilehasnain.qazatracker.data.local.dao.CompletionLogDao
import com.zilehasnain.qazatracker.data.local.dao.MilestoneDao
import com.zilehasnain.qazatracker.data.local.dao.PrayerLedgerDao
import com.zilehasnain.qazatracker.data.local.entity.AdjustmentLog
import com.zilehasnain.qazatracker.data.local.entity.BaselineSnapshot
import com.zilehasnain.qazatracker.data.local.entity.CompletionLog
import com.zilehasnain.qazatracker.data.local.entity.MilestoneEntity
import com.zilehasnain.qazatracker.domain.model.AdjustmentEntry
import com.zilehasnain.qazatracker.domain.model.AdjustmentReason
import com.zilehasnain.qazatracker.domain.model.CalculationMethod
import com.zilehasnain.qazatracker.domain.model.CompletionEntry
import com.zilehasnain.qazatracker.domain.model.MilestoneEntry
import com.zilehasnain.qazatracker.domain.model.PrayerType
import com.zilehasnain.qazatracker.domain.model.RemainingPrayerCount
import com.zilehasnain.qazatracker.domain.repository.QazaRepository
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class QazaRepositoryImpl @Inject constructor(
    private val baselineSnapshotDao: BaselineSnapshotDao,
    private val adjustmentLogDao: AdjustmentLogDao,
    private val completionLogDao: CompletionLogDao,
    private val prayerLedgerDao: PrayerLedgerDao,
    private val milestoneDao: MilestoneDao
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

    override suspend fun recordMilestone(entry: MilestoneEntry) {
        milestoneDao.insertMilestone(MilestoneEntity(prayerType = entry.prayerType, achievedAt = entry.achievedAt))
    }

    override fun observeMilestones(): Flow<List<MilestoneEntry>> =
        milestoneDao.getMilestones().map { rows ->
            rows.map { MilestoneEntry(it.prayerType, it.achievedAt) }
        }

    private fun CompletionEntry.toEntity() =
        CompletionLog(prayerType = prayerType, timestamp = timestamp, batchId = batchId)
}
