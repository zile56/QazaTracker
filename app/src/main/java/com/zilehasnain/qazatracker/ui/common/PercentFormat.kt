package com.zilehasnain.qazatracker.ui.common

import java.util.Locale
import kotlin.math.roundToInt

/**
 * Whole percent normally ("51%"), but one decimal below 10% so a real share of a large
 * backlog reads "1.4%" rather than a flat "1%", and "<0.1%" rather than a misleading "0.0%" for
 * a real but tiny share. Always Latin digits, like the rest of the app.
 */
fun formatPercent(rate: Float): String = when {
    rate <= 0f -> "0%"
    rate < 0.1f -> "<0.1%"
    rate < 9.95f -> String.format(Locale.US, "%.1f%%", rate)
    else -> "${rate.roundToInt()}%"
}
