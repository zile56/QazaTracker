package com.zilehasnain.qazatracker.ui.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * The home-screen widget: five prayer types, what's left of each, and a +1 button per row.
 *
 * Registered not exported (see the manifest and [WidgetActions]). Database work happens off the
 * main thread inside goAsync(), which keeps the process alive until finish() so a slow first
 * database open can't have the receiver killed mid-write.
 */
@AndroidEntryPoint
class QazaTrackerWidget : AppWidgetProvider() {

    @Inject lateinit var widgetUpdater: WidgetUpdater
    @Inject lateinit var logHandler: WidgetLogHandler

    /** Widget added, resized, or the device restarted: render from the database. */
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        runAsync { widgetUpdater.updateAll() }
    }

    override fun onReceive(context: Context, intent: Intent) {
        // Lets AppWidgetProvider route APPWIDGET_UPDATE and friends to onUpdate() and the like.
        super.onReceive(context, intent)

        if (intent.action != WidgetActions.ACTION_LOG_PRAYER) return
        val prayerType = WidgetActions.prayerTypeFrom(intent) ?: return

        runAsync {
            val message = try {
                logHandler.logPrayer(prayerType)
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                WidgetText.failed(prayerType)
            }
            showToast(context, message)
        }
    }

    private fun runAsync(block: suspend () -> Unit) {
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            try {
                block()
            } finally {
                pending.finish()
            }
        }
    }

    private fun showToast(context: Context, message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context.applicationContext, message, Toast.LENGTH_SHORT).show()
        }
    }
}
