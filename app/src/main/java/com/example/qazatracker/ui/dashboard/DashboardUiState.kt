package com.example.qazatracker.ui.dashboard

import com.example.qazatracker.domain.model.CompletionProjection
import com.example.qazatracker.domain.model.PrayerType

data class DashboardUiState(
    val rows: List<PrayerRowUiState> = PrayerType.entries.map { PrayerRowUiState(it, completed = 0, remaining = 0) },
    val projection: CompletionProjection = CompletionProjection.InsufficientData
) {
    val totalRemaining: Int get() = rows.sumOf { it.remaining }
}

data class PrayerRowUiState(
    val prayerType: PrayerType,
    val completed: Int,
    val remaining: Int
) {
    /** Fraction complete, for the progress bar. No missed prayers at all reads as fully done. */
    val progressFraction: Float
        get() {
            val total = completed + remaining
            return if (total <= 0) 1f else (completed.toFloat() / total).coerceIn(0f, 1f)
        }
}
