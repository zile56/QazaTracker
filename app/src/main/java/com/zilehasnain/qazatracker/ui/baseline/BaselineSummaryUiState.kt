package com.zilehasnain.qazatracker.ui.baseline

import com.zilehasnain.qazatracker.domain.model.CalculationMethod
import com.zilehasnain.qazatracker.domain.model.PrayerType

data class BaselineSummaryUiState(
    val method: CalculationMethod,
    val missedDays: Long,
    val counts: Map<PrayerType, String> = emptyMap(),
    val isConfirmed: Boolean = false
) {
    val total: Int get() = counts.values.sumOf { it.toIntOrNull() ?: 0 }

    val explainerText: String
        get() = when (method) {
            CalculationMethod.EXACT_DATES ->
                "Based on your date range ($missedDays days), here's our estimate — five prayers a day. Adjust any row before you begin."

            CalculationMethod.AGE_ESTIMATE ->
                "Based on $missedDays days since prayer became obligatory for you, here's our estimate. Adjust any row before you begin."
        }
}
