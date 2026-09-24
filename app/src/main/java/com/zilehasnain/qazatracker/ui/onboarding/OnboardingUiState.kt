package com.zilehasnain.qazatracker.ui.onboarding

import com.zilehasnain.qazatracker.domain.model.BaselineCalculation
import com.zilehasnain.qazatracker.domain.model.CalculationMethod
import java.time.LocalDate

data class OnboardingUiState(
    val method: CalculationMethod? = null,
    val dateFrom: LocalDate? = null,
    val dateTo: LocalDate? = null,
    val ageNowInput: String = "",
    val ageObligatoryInput: String = "",
    val result: BaselineCalculation? = null,
    val errorMessage: String? = null
) {
    val canContinue: Boolean
        get() = when (method) {
            CalculationMethod.EXACT_DATES ->
                dateFrom != null && dateTo != null && !dateTo.isBefore(dateFrom)

            CalculationMethod.AGE_ESTIMATE -> {
                val ageNow = ageNowInput.toIntOrNull()
                val ageObligatory = ageObligatoryInput.toIntOrNull()
                ageNow != null && ageObligatory != null && ageObligatory > 0 && ageNow > ageObligatory
            }

            null -> false
        }
}
