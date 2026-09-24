package com.zilehasnain.qazatracker.ui.settings

import com.zilehasnain.qazatracker.domain.model.NotificationFrequency

data class SettingsUiState(
    val notificationFrequency: NotificationFrequency = NotificationFrequency.WEEKLY,
    val regionName: String? = null,
    val versionName: String = "",
    val exportState: ExportState = ExportState.Idle
)

sealed interface ExportState {
    data object Idle : ExportState
    data object Exporting : ExportState
    data class Success(val filePath: String, val recordCount: Int) : ExportState
    data class Failure(val message: String) : ExportState
}
