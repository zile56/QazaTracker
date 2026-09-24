package com.zilehasnain.qazatracker.domain.repository

import com.zilehasnain.qazatracker.domain.model.ExportResult

interface DataExportRepository {
    /** Serializes every Room table (baselines, adjustments, completions) to a JSON file. */
    suspend fun exportAllData(): ExportResult
}
