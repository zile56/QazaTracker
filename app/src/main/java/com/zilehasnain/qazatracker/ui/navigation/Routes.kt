package com.zilehasnain.qazatracker.ui.navigation

import com.zilehasnain.qazatracker.domain.model.CalculationMethod

object Routes {
    const val TUTORIAL = "tutorial"
    const val ONBOARDING = "onboarding"
    const val DASHBOARD = "dashboard"
    const val BATCH_LOGGING = "batchLogging"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
    const val STATISTICS = "statistics"
    const val ACHIEVEMENTS = "achievements"
    const val PRAYER_TIMES = "prayerTimes"
    const val INSPIRATION = "inspiration"

    const val MISSED_DAYS_ARG = "missedDays"
    const val METHOD_ARG = "method"
    private const val BASELINE_SUMMARY_BASE = "baselineSummary"
    const val BASELINE_SUMMARY_ROUTE = "$BASELINE_SUMMARY_BASE/{$MISSED_DAYS_ARG}/{$METHOD_ARG}"

    fun baselineSummary(missedDays: Long, method: CalculationMethod): String =
        "$BASELINE_SUMMARY_BASE/$missedDays/${method.name}"

    /**
     * Someone who already has a baseline goes straight to the dashboard even if the tutorial
     * flag is unset (e.g. they installed before the tutorial existed) — its "get started"
     * button leads to onboarding, which would be wrong for them.
     */
    fun startDestinationFor(hasBaseline: Boolean, hasSeenTutorial: Boolean): String = when {
        hasBaseline -> DASHBOARD
        !hasSeenTutorial -> TUTORIAL
        else -> ONBOARDING
    }
}
