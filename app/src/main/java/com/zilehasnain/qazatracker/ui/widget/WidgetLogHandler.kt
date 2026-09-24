package com.zilehasnain.qazatracker.ui.widget

import com.zilehasnain.qazatracker.domain.model.PrayerType
import com.zilehasnain.qazatracker.domain.usecase.LogCompletionUseCase
import javax.inject.Inject

/**
 * What a +1 tap does, separated from the BroadcastReceiver so it can be tested without one:
 * log through the same use case the app uses (so milestones are checked too), then refresh the
 * widget straight away rather than waiting for the observer to notice.
 */
class WidgetLogHandler @Inject constructor(
    private val logCompletion: LogCompletionUseCase,
    private val widgetUpdater: WidgetUpdater
) {
    /** @return the confirmation to toast. */
    suspend fun logPrayer(prayerType: PrayerType): String {
        logCompletion(prayerType)
        widgetUpdater.updateAll()
        return WidgetText.logged(prayerType)
    }
}
