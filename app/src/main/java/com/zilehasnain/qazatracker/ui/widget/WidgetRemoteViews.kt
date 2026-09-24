package com.zilehasnain.qazatracker.ui.widget

import android.content.Context
import android.view.View
import android.widget.RemoteViews
import com.zilehasnain.qazatracker.R
import com.zilehasnain.qazatracker.domain.model.PrayerType

/**
 * Turns the rows into RemoteViews for widget_qaza_tracker.xml. Only text and click handlers are
 * set here, never colours: the layout's colour resources are resolved by the launcher, so the
 * widget follows light/dark changes on its own instead of freezing at whatever theme it was
 * last rendered in.
 */
internal object WidgetRemoteViews {

    private class RowIds(val count: Int, val add: Int)

    private fun idsFor(prayerType: PrayerType): RowIds = when (prayerType) {
        PrayerType.FAJR -> RowIds(R.id.widget_count_fajr, R.id.widget_add_fajr)
        PrayerType.DHUHR -> RowIds(R.id.widget_count_dhuhr, R.id.widget_add_dhuhr)
        PrayerType.ASR -> RowIds(R.id.widget_count_asr, R.id.widget_add_asr)
        PrayerType.MAGHRIB -> RowIds(R.id.widget_count_maghrib, R.id.widget_add_maghrib)
        PrayerType.ISHA -> RowIds(R.id.widget_count_isha, R.id.widget_add_isha)
    }

    /** [rows] is null when nothing is set up yet; the widget then shows its setup message. */
    fun build(context: Context, rows: List<WidgetRow>?): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_qaza_tracker)

        // Anywhere on the widget that isn't a +1 button opens the app.
        views.setOnClickPendingIntent(R.id.widget_root, WidgetActions.openAppPendingIntent(context))

        if (rows == null) {
            views.setViewVisibility(R.id.widget_rows, View.GONE)
            views.setViewVisibility(R.id.widget_empty, View.VISIBLE)
            return views
        }

        views.setViewVisibility(R.id.widget_rows, View.VISIBLE)
        views.setViewVisibility(R.id.widget_empty, View.GONE)
        rows.forEach { row ->
            val ids = idsFor(row.prayerType)
            views.setTextViewText(ids.count, row.countText)
            views.setOnClickPendingIntent(ids.add, WidgetActions.logPendingIntent(context, row.prayerType))
        }
        return views
    }
}
