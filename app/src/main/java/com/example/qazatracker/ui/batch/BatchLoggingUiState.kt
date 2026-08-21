package com.example.qazatracker.ui.batch

import com.example.qazatracker.domain.model.PrayerType

data class BatchLoggingUiState(
    val days: Int = 7,
    val selectedPrayerTypes: Set<PrayerType> = PrayerType.entries.toSet(),
    val isLogging: Boolean = false,
    val isComplete: Boolean = false
) {
    val canConfirm: Boolean get() = selectedPrayerTypes.isNotEmpty() && days in DAYS_RANGE && !isLogging
    val totalEntries: Int get() = days * selectedPrayerTypes.size

    companion object {
        val DAYS_RANGE = 1..90
    }
}
