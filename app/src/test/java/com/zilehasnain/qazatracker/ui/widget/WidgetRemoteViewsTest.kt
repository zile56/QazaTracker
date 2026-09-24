package com.zilehasnain.qazatracker.ui.widget

import android.app.Application
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import com.zilehasnain.qazatracker.R
import com.zilehasnain.qazatracker.domain.model.PrayerType
import com.zilehasnain.qazatracker.ui.common.displayName
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Inflates the real widget layout with the real RemoteViews, so a broken id or layout fails here. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26], application = Application::class)
class WidgetRemoteViewsTest {

    private val context = ApplicationProvider.getApplicationContext<Application>()

    private fun render(rows: List<WidgetRow>?): View =
        WidgetRemoteViews.build(context, rows).apply(context, FrameLayout(context))

    private fun View.text(id: Int) = findViewById<TextView>(id).text.toString()

    private fun allRows(remaining: Int = 2191) = PrayerType.entries.map { WidgetRow(it, remaining) }

    private data class Ids(val name: Int, val count: Int, val add: Int)

    private val idsByType = mapOf(
        PrayerType.FAJR to Ids(R.id.widget_name_fajr, R.id.widget_count_fajr, R.id.widget_add_fajr),
        PrayerType.DHUHR to Ids(R.id.widget_name_dhuhr, R.id.widget_count_dhuhr, R.id.widget_add_dhuhr),
        PrayerType.ASR to Ids(R.id.widget_name_asr, R.id.widget_count_asr, R.id.widget_add_asr),
        PrayerType.MAGHRIB to Ids(R.id.widget_name_maghrib, R.id.widget_count_maghrib, R.id.widget_add_maghrib),
        PrayerType.ISHA to Ids(R.id.widget_name_isha, R.id.widget_count_isha, R.id.widget_add_isha)
    )

    @Test
    fun `every prayer type shows its remaining count`() {
        val view = render(
            listOf(
                WidgetRow(PrayerType.FAJR, 2191), WidgetRow(PrayerType.DHUHR, 2000),
                WidgetRow(PrayerType.ASR, 1500), WidgetRow(PrayerType.MAGHRIB, 10),
                WidgetRow(PrayerType.ISHA, 0)
            )
        )

        assertEquals("2191 left", view.text(R.id.widget_count_fajr))
        assertEquals("2000 left", view.text(R.id.widget_count_dhuhr))
        assertEquals("1500 left", view.text(R.id.widget_count_asr))
        assertEquals("10 left", view.text(R.id.widget_count_maghrib))
        assertEquals("Done ✓", view.text(R.id.widget_count_isha))
    }

    @Test
    fun `the prayer names in the layout match the app's own names`() {
        val view = render(allRows())

        idsByType.forEach { (type, ids) ->
            assertEquals(type.displayName(), view.text(ids.name))
        }
    }

    @Test
    fun `each row has a plus button labelled for its prayer`() {
        val view = render(allRows())

        idsByType.forEach { (type, ids) ->
            val button = view.findViewById<TextView>(ids.add)
            assertEquals("+", button.text.toString())
            assertEquals("Log one ${type.displayName()}", button.contentDescription.toString())
        }
    }

    @Test
    fun `with rows the list is shown and the setup message hidden`() {
        val view = render(allRows())

        assertEquals(View.VISIBLE, view.findViewById<View>(R.id.widget_rows).visibility)
        assertEquals(View.GONE, view.findViewById<View>(R.id.widget_empty).visibility)
    }

    @Test
    fun `with nothing set up the setup message replaces the list`() {
        val view = render(null)

        assertEquals(View.GONE, view.findViewById<View>(R.id.widget_rows).visibility)
        assertEquals(View.VISIBLE, view.findViewById<View>(R.id.widget_empty).visibility)
        assertEquals("Open Qaza Tracker to set up your missed prayers.", view.text(R.id.widget_empty))
    }

    @Test
    fun `the layout contains a row for every prayer type`() {
        val view = render(allRows())

        listOf(
            R.id.widget_row_fajr, R.id.widget_row_dhuhr, R.id.widget_row_asr,
            R.id.widget_row_maghrib, R.id.widget_row_isha
        ).forEach { assertEquals(View.VISIBLE, view.findViewById<View>(it).visibility) }
    }
}
