package com.example.qazatracker.ui.baseline

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qazatracker.domain.model.CalculationMethod
import com.example.qazatracker.domain.model.PrayerType
import com.example.qazatracker.domain.usecase.ConfirmBaselineUseCase
import com.example.qazatracker.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class BaselineSummaryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val confirmBaselineUseCase: ConfirmBaselineUseCase
) : ViewModel() {

    private val _uiState: MutableStateFlow<BaselineSummaryUiState>

    init {
        val method = CalculationMethod.valueOf(
            checkNotNull(savedStateHandle[Routes.METHOD_ARG]) { "Missing method arg" }
        )
        val missedDays = checkNotNull(
            savedStateHandle.get<Long>(Routes.MISSED_DAYS_ARG)
        ) { "Missing missedDays arg" }

        _uiState = MutableStateFlow(
            BaselineSummaryUiState(
                method = method,
                missedDays = missedDays,
                counts = PrayerType.entries.associateWith { missedDays.toString() }
            )
        )
    }

    val uiState: StateFlow<BaselineSummaryUiState> = _uiState.asStateFlow()

    fun updateCount(prayerType: PrayerType, value: String) {
        if (value.length > 6 || (value.isNotEmpty() && !value.all(Char::isDigit))) return
        _uiState.update { it.copy(counts = it.counts + (prayerType to value)) }
    }

    fun confirmBaseline() {
        val state = _uiState.value
        val counts = state.counts.mapValues { it.value.toIntOrNull() ?: 0 }
        viewModelScope.launch {
            confirmBaselineUseCase(counts, state.method)
            _uiState.update { it.copy(isConfirmed = true) }
        }
    }
}
