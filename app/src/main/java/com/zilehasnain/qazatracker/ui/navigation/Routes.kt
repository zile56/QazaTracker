package com.zilehasnain.qazatracker.ui.navigation

import com.zilehasnain.qazatracker.domain.model.CalculationMethod

object Routes {
    const val ONBOARDING = "onboarding"
    const val DASHBOARD = "dashboard"
    const val BATCH_LOGGING = "batchLogging"
    const val HISTORY = "history"
    const val SETTINGS = "settings"

    const val MISSED_DAYS_ARG = "missedDays"
    const val METHOD_ARG = "method"
    private const val BASELINE_SUMMARY_BASE = "baselineSummary"
    const val BASELINE_SUMMARY_ROUTE = "$BASELINE_SUMMARY_BASE/{$MISSED_DAYS_ARG}/{$METHOD_ARG}"

    fun baselineSummary(missedDays: Long, method: CalculationMethod): String =
        "$BASELINE_SUMMARY_BASE/$missedDays/${method.name}"
}
