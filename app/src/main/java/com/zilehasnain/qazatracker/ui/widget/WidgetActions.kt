package com.zilehasnain.qazatracker.ui.widget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.zilehasnain.qazatracker.MainActivity
import com.zilehasnain.qazatracker.domain.model.PrayerType

/**
 * The widget's +1 buttons send a broadcast to [QazaTrackerWidget] itself. The receiver is not
 * exported, so only these app-owned PendingIntents can reach it — another app can't send the
 * action and log prayers on the user's behalf.
 */
object WidgetActions {
    const val ACTION_LOG_PRAYER = "com.zilehasnain.qazatracker.widget.LOG_PRAYER"
    const val EXTRA_PRAYER_TYPE = "prayer_type"

    // PendingIntent identity ignores extras, so each prayer type needs its own request code or
    // all five buttons would collapse into one intent and every tap would log the same prayer.
    private const val LOG_REQUEST_CODE_BASE = 100
    private const val OPEN_APP_REQUEST_CODE = 1

    fun logIntent(context: Context, prayerType: PrayerType): Intent =
        Intent(context, QazaTrackerWidget::class.java)
            .setAction(ACTION_LOG_PRAYER)
            .putExtra(EXTRA_PRAYER_TYPE, prayerType.name)

    fun prayerTypeFrom(intent: Intent): PrayerType? {
        val name = intent.getStringExtra(EXTRA_PRAYER_TYPE) ?: return null
        return PrayerType.entries.firstOrNull { it.name == name }
    }

    fun logPendingIntent(context: Context, prayerType: PrayerType): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            LOG_REQUEST_CODE_BASE + prayerType.ordinal,
            logIntent(context, prayerType),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    fun openAppPendingIntent(context: Context): PendingIntent =
        PendingIntent.getActivity(
            context,
            OPEN_APP_REQUEST_CODE,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
}
