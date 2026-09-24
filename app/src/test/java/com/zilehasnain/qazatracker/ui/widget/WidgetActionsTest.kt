package com.zilehasnain.qazatracker.ui.widget

import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.zilehasnain.qazatracker.MainActivity
import com.zilehasnain.qazatracker.domain.model.PrayerType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

// A plain Application: the real one (Hilt) would start its own widget observer against the
// production database and race these tests. Nothing here needs Hilt.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26], application = Application::class)
class WidgetActionsTest {

    private val context = ApplicationProvider.getApplicationContext<Application>()

    @Test
    fun `the log intent targets the widget receiver with the prayer type`() {
        val intent = WidgetActions.logIntent(context, PrayerType.DHUHR)

        assertEquals(WidgetActions.ACTION_LOG_PRAYER, intent.action)
        assertEquals(QazaTrackerWidget::class.java.name, intent.component?.className)
        assertEquals("DHUHR", intent.getStringExtra(WidgetActions.EXTRA_PRAYER_TYPE))
    }

    @Test
    fun `the prayer type round-trips through the intent`() {
        PrayerType.entries.forEach {
            assertEquals(it, WidgetActions.prayerTypeFrom(WidgetActions.logIntent(context, it)))
        }
    }

    @Test
    fun `a missing or unknown prayer type is ignored rather than crashing`() {
        assertNull(WidgetActions.prayerTypeFrom(Intent(WidgetActions.ACTION_LOG_PRAYER)))
        assertNull(
            WidgetActions.prayerTypeFrom(
                Intent(WidgetActions.ACTION_LOG_PRAYER).putExtra(WidgetActions.EXTRA_PRAYER_TYPE, "WITR")
            )
        )
        assertNull(
            WidgetActions.prayerTypeFrom(
                Intent(WidgetActions.ACTION_LOG_PRAYER).putExtra(WidgetActions.EXTRA_PRAYER_TYPE, "fajr")
            )
        )
    }

    @Test
    fun `each prayer's button gets its own request code so five buttons stay five intents`() {
        val requestCodes = PrayerType.entries.map {
            shadowOf(WidgetActions.logPendingIntent(context, it)).requestCode
        }

        assertEquals(PrayerType.entries.size, requestCodes.toSet().size)
    }

    @Test
    fun `the log pending intent is an immutable broadcast to the widget carrying the right prayer`() {
        val pending = WidgetActions.logPendingIntent(context, PrayerType.ISHA)
        val shadow = shadowOf(pending)

        assertTrue(shadow.isBroadcastIntent)
        assertTrue(shadow.flags and PendingIntent.FLAG_IMMUTABLE != 0)
        assertEquals(WidgetActions.ACTION_LOG_PRAYER, shadow.savedIntent.action)
        assertEquals(PrayerType.ISHA, WidgetActions.prayerTypeFrom(shadow.savedIntent))
    }

    @Test
    fun `tapping elsewhere on the widget opens the main activity`() {
        val shadow = shadowOf(WidgetActions.openAppPendingIntent(context))

        assertTrue(shadow.isActivityIntent)
        assertEquals(MainActivity::class.java.name, shadow.savedIntent.component?.className)
        assertTrue(shadow.flags and PendingIntent.FLAG_IMMUTABLE != 0)
    }
}
