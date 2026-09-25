package com.zilehasnain.qazatracker.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zilehasnain.qazatracker.BuildConfig
import com.zilehasnain.qazatracker.domain.model.NotificationFrequency
import com.zilehasnain.qazatracker.domain.repository.DataExportRepository
import com.zilehasnain.qazatracker.domain.repository.SettingsRepository
import com.zilehasnain.qazatracker.domain.usecase.ScheduleNotificationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val dataExportRepository: DataExportRepository,
    private val scheduleNotifications: ScheduleNotificationsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState(versionName = BuildConfig.VERSION_NAME))
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.observeNotificationFrequency().collect { frequency ->
                _uiState.update { it.copy(notificationFrequency = frequency) }
            }
        }
    }

    fun onFrequencySelected(frequency: NotificationFrequency) {
        viewModelScope.launch { scheduleNotifications(frequency) }
    }

    fun onExportClicked() {
        viewModelScope.launch {
            _uiState.update { it.copy(exportState = ExportState.Exporting) }
            runCatching { dataExportRepository.exportAllData() }
                .onSuccess { result ->
                    _uiState.update {
                        it.copy(exportState = ExportState.Success(result.filePath, result.recordCount))
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(exportState = ExportState.Failure(error.message ?: "Export failed"))
                    }
                }
        }
    }

    fun onExportStatusDismissed() {
        _uiState.update { it.copy(exportState = ExportState.Idle) }
    }
}
