package com.example.qazatracker.data.repository

import android.content.Context
import com.example.qazatracker.data.local.dao.AdjustmentLogDao
import com.example.qazatracker.data.local.dao.BaselineSnapshotDao
import com.example.qazatracker.data.local.dao.CompletionLogDao
import com.example.qazatracker.data.local.entity.AdjustmentLog
import com.example.qazatracker.data.local.entity.BaselineSnapshot
import com.example.qazatracker.data.local.entity.CompletionLog
import com.example.qazatracker.domain.model.ExportResult
import com.example.qazatracker.domain.repository.DataExportRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.time.Instant
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

/**
 * Dumps all three Room tables (the app's entire event-sourced audit trail) to a single
 * JSON file in the app's cache directory, for the user to back up or move elsewhere.
 * Read-only: this never touches the database, only exports a snapshot of it.
 */
class DataExportRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val baselineSnapshotDao: BaselineSnapshotDao,
    private val adjustmentLogDao: AdjustmentLogDao,
    private val completionLogDao: CompletionLogDao
) : DataExportRepository {

    override suspend fun exportAllData(): ExportResult {
        val baselines = baselineSnapshotDao.observeAll().first()
        val adjustments = adjustmentLogDao.observeAll().first()
        val completions = completionLogDao.observeAll().first()

        val json = JSONObject().apply {
            put("exportedAt", Instant.now().toString())
            put("baselineSnapshots", JSONArray(baselines.map { it.toJson() }))
            put("adjustmentLogs", JSONArray(adjustments.map { it.toJson() }))
            put("completionLogs", JSONArray(completions.map { it.toJson() }))
        }

        val exportsDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val fileName = "qaza_export_${exportFileTimestampFormatter.format(Instant.now())}.json"
        val file = File(exportsDir, fileName)
        file.writeText(json.toString(2))

        return ExportResult(
            filePath = file.absolutePath,
            recordCount = baselines.size + adjustments.size + completions.size
        )
    }
}

private val exportFileTimestampFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").withZone(java.time.ZoneOffset.UTC)

private fun BaselineSnapshot.toJson(): JSONObject = JSONObject().apply {
    put("prayerType", prayerType.name)
    put("initialCount", initialCount)
    put("calculatedAt", calculatedAt.toString())
    put("method", method.name)
}

private fun AdjustmentLog.toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("prayerType", prayerType.name)
    put("delta", delta)
    put("reason", reason.name)
    put("note", note)
    put("timestamp", timestamp.toString())
}

private fun CompletionLog.toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("prayerType", prayerType.name)
    put("timestamp", timestamp.toString())
    put("batchId", batchId)
}
