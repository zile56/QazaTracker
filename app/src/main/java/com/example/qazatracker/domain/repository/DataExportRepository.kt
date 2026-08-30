package com.example.qazatracker.domain.repository

import com.example.qazatracker.domain.model.ExportResult

interface DataExportRepository {
    /** Serializes every Room table (baselines, adjustments, completions) to a JSON file. */
    suspend fun exportAllData(): ExportResult
}
