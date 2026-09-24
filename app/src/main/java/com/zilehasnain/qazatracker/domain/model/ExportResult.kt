package com.zilehasnain.qazatracker.domain.model

/** Outcome of a full-data export — enough for the UI to report where the file landed. */
data class ExportResult(
    val filePath: String,
    val recordCount: Int
)
