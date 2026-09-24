package com.zilehasnain.qazatracker.ui.batch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zilehasnain.qazatracker.domain.model.PrayerType
import com.zilehasnain.qazatracker.domain.usecase.LogCompletionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class BatchLoggingViewModel @Inject constructor(
    private val logCompletion: LogCompletionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BatchLoggingUiState())
    val uiState: StateFlow<BatchLoggingUiState> = _uiState.asStateFlow()

    fun setDays(days: Int) {
        _uiState.update { it.copy(days = days.coerceIn(BatchLoggingUiState.DAYS_RANGE)) }
    }

    fun incrementDays() = setDays(_uiState.value.days + 1)

    fun decrementDays() = setDays(_uiState.value.days - 1)

    fun togglePrayerType(prayerType: PrayerType) {
        _uiState.update { state ->
            val selected = state.selectedPrayerTypes
            state.copy(
                selectedPrayerTypes = if (prayerType in selected) selected - prayerType else selected + prayerType
            )
        }
    }

    fun confirm() {
        val state = _uiState.value
        if (!state.canConfirm) return

        _uiState.update { it.copy(isLogging = true) }
        viewModelScope.launch {
            logCompletion.batch(state.selectedPrayerTypes.toList(), state.days)
            _uiState.update { it.copy(isLogging = false, isComplete = true) }
        }
    }
}
