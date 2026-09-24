package com.zilehasnain.qazatracker.ui.widget

import com.zilehasnain.qazatracker.domain.model.PrayerType
import com.zilehasnain.qazatracker.domain.model.RemainingPrayerCount
import com.zilehasnain.qazatracker.ui.common.displayName

/** What one widget row shows: a prayer type and how many missed prayers of it are left. */
data class WidgetRow(val prayerType: PrayerType, val remaining: Int) {
    val countText: String get() = if (remaining > 0) "$remaining left" else "Done ✓"
}

/**
 * The five rows in prayer order, or null when nothing has been set up yet (no baseline exists),
 * which the widget shows as a "open the app to set up" message rather than five zeros.
 * Negative remaining (over-logged) reads as done, the same as on the dashboard.
 */
fun widgetRowsFor(counts: List<RemainingPrayerCount>): List<WidgetRow>? {
    if (counts.isEmpty()) return null
    val byType = counts.associateBy { it.prayerType }
    return PrayerType.entries.map { WidgetRow(it, (byType[it]?.remaining ?: 0).coerceAtLeast(0)) }
}

/** The toasts shown after tapping +1 on the widget. */
object WidgetText {
    fun logged(prayerType: PrayerType): String = "${prayerType.displayName()} logged ✓"

    fun failed(prayerType: PrayerType): String = "Couldn't log ${prayerType.displayName()}"
}
