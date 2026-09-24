package com.zilehasnain.qazatracker.ui.history

import com.zilehasnain.qazatracker.domain.model.HistoryEntry

data class HistoryUiState(
    val entries: List<HistoryEntry> = emptyList()
)
