package com.zilehasnain.qazatracker.ui.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import com.zilehasnain.qazatracker.domain.model.RemainingPrayerCount
import com.zilehasnain.qazatracker.domain.repository.QazaRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Keeps every placed widget showing the current remaining counts.
 *
 * Sync in both directions needs no custom broadcasts, because Room is the single source of truth
 * and both sides read from it. The widget's +1 writes through LogCompletionUseCase, and the app's
 * Dashboard observes that same database, so it updates on its own. In the other direction,
 * [observeChanges] watches the counts for the life of the app process and re-renders the widget on
 * any change: quick-logging on the Dashboard, batch logging, an adjustment, a new baseline.
 */
@Singleton
class WidgetUpdater @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: QazaRepository
) {
    /** Re-renders now, from what's in the database. Used after the widget's own +1. */
    suspend fun updateAll() {
        push(repository.observeRemainingCounts().first())
    }

    /** Re-renders on every change to the counts (including once immediately) until [scope] ends. */
    fun observeChanges(scope: CoroutineScope): Job = scope.launch {
        repository.observeRemainingCounts().distinctUntilChanged().collect { push(it) }
    }

    private fun push(counts: List<RemainingPrayerCount>) {
        val manager = AppWidgetManager.getInstance(context)
        val widgetIds = manager.getAppWidgetIds(ComponentName(context, QazaTrackerWidget::class.java))
        if (widgetIds.isEmpty()) return

        manager.updateAppWidget(widgetIds, WidgetRemoteViews.build(context, widgetRowsFor(counts)))
    }
}
