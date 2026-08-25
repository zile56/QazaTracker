package com.example.qazatracker.ui.history

import com.example.qazatracker.domain.model.HistoryEntry

data class HistoryUiState(
    val entries: List<HistoryEntry> = emptyList()
)
