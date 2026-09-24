package com.zilehasnain.qazatracker.ui.onboarding

import androidx.lifecycle.ViewModel
import com.zilehasnain.qazatracker.domain.model.BaselineCalculationInput
import com.zilehasnain.qazatracker.domain.model.CalculationMethod
import com.zilehasnain.qazatracker.domain.usecase.CalculateBaselineUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val calculateBaselineUseCase: CalculateBaselineUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun selectMethod(method: CalculationMethod) {
        _uiState.update { it.copy(method = method, result = null, errorMessage = null) }
    }

    fun onDateFromSelected(date: LocalDate) {
        _uiState.update { it.copy(dateFrom = date, result = null, errorMessage = null) }
    }

    fun onDateToSelected(date: LocalDate) {
        _uiState.update { it.copy(dateTo = date, result = null, errorMessage = null) }
    }

    fun onAgeNowChanged(value: String) {
        if (value.length <= 3 && value.all(Char::isDigit)) {
            _uiState.update { it.copy(ageNowInput = value, result = null, errorMessage = null) }
        }
    }

    fun onAgeObligatoryChanged(value: String) {
        if (value.length <= 3 && value.all(Char::isDigit)) {
            _uiState.update { it.copy(ageObligatoryInput = value, result = null, errorMessage = null) }
        }
    }

    fun onContinueClicked() {
        val state = _uiState.value
        val input = when (state.method) {
            CalculationMethod.EXACT_DATES -> {
                val from = state.dateFrom ?: return
                val to = state.dateTo ?: return
                BaselineCalculationInput.ExactDates(from, to)
            }

            CalculationMethod.AGE_ESTIMATE -> {
                val ageNow = state.ageNowInput.toIntOrNull() ?: return
                val ageObligatory = state.ageObligatoryInput.toIntOrNull() ?: return
                BaselineCalculationInput.AgeEstimate(ageNow, ageObligatory)
            }

            null -> return
        }

        runCatching { calculateBaselineUseCase(input) }
            .onSuccess { result -> _uiState.update { it.copy(result = result, errorMessage = null) } }
            .onFailure { error -> _uiState.update { it.copy(result = null, errorMessage = error.message) } }
    }
}
