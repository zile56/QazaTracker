package com.example.qazatracker.ui.navigation

import com.example.qazatracker.domain.model.CalculationMethod

object Routes {
    const val ONBOARDING = "onboarding"

    const val MISSED_DAYS_ARG = "missedDays"
    const val METHOD_ARG = "method"
    private const val BASELINE_SUMMARY_BASE = "baselineSummary"
    const val BASELINE_SUMMARY_ROUTE = "$BASELINE_SUMMARY_BASE/{$MISSED_DAYS_ARG}/{$METHOD_ARG}"

    fun baselineSummary(missedDays: Long, method: CalculationMethod): String =
        "$BASELINE_SUMMARY_BASE/$missedDays/${method.name}"
}
