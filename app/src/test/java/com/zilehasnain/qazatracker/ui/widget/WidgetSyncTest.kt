package com.zilehasnain.qazatracker.ui.widget

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.view.View
import android.widget.TextView
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.zilehasnain.qazatracker.R
import com.zilehasnain.qazatracker.data.local.QazaDatabase
import com.zilehasnain.qazatracker.data.repository.QazaRepositoryImpl
import com.zilehasnain.qazatracker.domain.model.CalculationMethod
import com.zilehasnain.qazatracker.domain.model.PrayerType
import com.zilehasnain.qazatracker.domain.usecase.LogCompletionUseCase
import java.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAppWidgetManager

/**
 * A real widget is placed with Robolectric's AppWidgetManager, wired to a real in-memory database,
 * so what is checked is what a user would see: tap +1 and the number drops; log in the app and the
 * widget follows. Plain Application for the same reason as the other widget tests.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26], application = Application::class)
class WidgetSyncTest {

    private lateinit var db: QazaDatabase
    private lateinit var repository: QazaRepositoryImpl
    private lateinit var updater: WidgetUpdater
    private lateinit var widgets: ShadowAppWidgetManager
    private var widgetId = 0

    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val fixedInstant = Instant.parse("2026-03-01T09:00:00Z")
    private val observerScope = CoroutineScope(Dispatchers.Default)

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(context, QazaDatabase::class.java).allowMainThreadQueries().build()
        repository = QazaRepositoryImpl(
            baselineSnapshotDao = db.baselineSnapshotDao(),
            adjustmentLogDao = db.adjustmentLogDao(),
            completionLogDao = db.completionLogDao(),
            prayerLedgerDao = db.prayerLedgerDao(),
            milestoneDao = db.milestoneDao()
        )
        updater = WidgetUpdater(context, repository)

        widgets = shadowOf(AppWidgetManager.getInstance(context))
    }

    /**
     * Places a widget for the provider by ComponentName. Robolectric's createWidget(providerClass)
     * would instead construct the real receiver and deliver broadcasts to it, which is a Hilt
     * receiver and so needs the Hilt application; the tests here don't need to run it.
     */
    private fun placeWidget() {
        widgetId = 1
        widgets.bindAppWidgetId(widgetId, ComponentName(context, QazaTrackerWidget::class.java))
    }

    private fun runWithWidget(block: suspend TestScope.() -> Unit) = runTest {
        placeWidget()
        block()
    }

    @After
    fun tearDown() {
        observerScope.cancel()
        db.close()
    }

    private suspend fun seedBaseline(count: Int) {
        PrayerType.entries.forEach {
            repository.setBaseline(it, count, fixedInstant, CalculationMethod.EXACT_DATES)
        }
    }

    private fun text(id: Int) = widgets.getViewFor(widgetId).findViewById<TextView>(id).text.toString()

    private fun visibility(id: Int) = widgets.getViewFor(widgetId).findViewById<View>(id).visibility

    private fun handler() = WidgetLogHandler(LogCompletionUseCase(repository, now = { fixedInstant }), updater)

    /** Null until the widget has been rendered for the first time. */
    private fun textOrNull(id: Int): String? =
        widgets.getViewFor(widgetId)?.findViewById<TextView>(id)?.text?.toString()

    /** Real-time wait (Room emits on its own threads), unlike runTest's virtual delay. */
    private suspend fun awaitText(id: Int, expected: String) = withContext(Dispatchers.Default) {
        withTimeout(10_000) { while (textOrNull(id) != expected) delay(25) }
    }

    // ---- The widget's +1 ----

    @Test
    fun `tapping plus one logs the prayer and the widget shows one fewer`() = runWithWidget {
        seedBaseline(count = 50)
        updater.updateAll()
        assertEquals("50 left", text(R.id.widget_count_fajr))

        val toast = handler().logPrayer(PrayerType.FAJR)

        assertEquals("Fajr logged ✓", toast)
        assertEquals("49 left", text(R.id.widget_count_fajr))
        assertEquals(1, repository.observeCompletionLogs().first().size)
        assertEquals(PrayerType.FAJR, repository.observeCompletionLogs().first().single().prayerType)
    }

    @Test
    fun `only the tapped prayer's count changes`() = runWithWidget {
        seedBaseline(count = 50)

        handler().logPrayer(PrayerType.ASR)

        assertEquals("49 left", text(R.id.widget_count_asr))
        assertEquals("50 left", text(R.id.widget_count_fajr))
        assertEquals("50 left", text(R.id.widget_count_dhuhr))
        assertEquals("50 left", text(R.id.widget_count_maghrib))
        assertEquals("50 left", text(R.id.widget_count_isha))
    }

    @Test
    fun `repeated taps keep counting down`() = runWithWidget {
        seedBaseline(count = 50)

        repeat(3) { handler().logPrayer(PrayerType.ISHA) }

        assertEquals("47 left", text(R.id.widget_count_isha))
        assertEquals(3, repository.observeCompletionLogs().first().size)
    }

    @Test
    fun `logging the last missed prayer reads done and records the milestone like the app does`() = runWithWidget {
        seedBaseline(count = 1)

        handler().logPrayer(PrayerType.MAGHRIB)

        assertEquals("Done ✓", text(R.id.widget_count_maghrib))
        assertEquals(listOf(PrayerType.MAGHRIB), repository.observeMilestones().first().map { it.prayerType })
    }

    // ---- Reading the database ----

    @Test
    fun `the widget shows the true counts from the database`() = runWithWidget {
        seedBaseline(count = 2191)

        updater.updateAll()

        listOf(
            R.id.widget_count_fajr, R.id.widget_count_dhuhr, R.id.widget_count_asr,
            R.id.widget_count_maghrib, R.id.widget_count_isha
        ).forEach { assertEquals("2191 left", text(it)) }
    }

    @Test
    fun `before any baseline the widget asks the user to set up`() = runWithWidget {
        updater.updateAll()

        assertEquals(View.VISIBLE, visibility(R.id.widget_empty))
        assertEquals(View.GONE, visibility(R.id.widget_rows))
    }

    @Test
    fun `once a baseline exists the rows replace the setup message`() = runWithWidget {
        updater.updateAll()
        assertEquals(View.VISIBLE, visibility(R.id.widget_empty))

        seedBaseline(count = 10)
        updater.updateAll()

        assertEquals(View.VISIBLE, visibility(R.id.widget_rows))
        assertEquals(View.GONE, visibility(R.id.widget_empty))
        assertEquals("10 left", text(R.id.widget_count_fajr))
    }

    @Test
    fun `updating with no widget on the home screen is a harmless no-op`() = runTest {
        seedBaseline(count = 10)

        updater.updateAll()
        handler().logPrayer(PrayerType.FAJR)

        assertEquals(1, repository.observeCompletionLogs().first().size)
    }

    // ---- Keeping the widget in step with the app ----

    @Test
    fun `logging in the app updates the widget without any widget involvement`() = runWithWidget {
        seedBaseline(count = 50)
        updater.observeChanges(observerScope)
        awaitText(R.id.widget_count_asr, "50 left")

        // Exactly what the Dashboard's +1 does.
        LogCompletionUseCase(repository, now = { fixedInstant })(PrayerType.ASR)

        awaitText(R.id.widget_count_asr, "49 left")
        assertEquals("50 left", text(R.id.widget_count_fajr))
    }

    @Test
    fun `a batch logged in the app updates every affected row on the widget`() = runWithWidget {
        seedBaseline(count = 50)
        updater.observeChanges(observerScope)
        awaitText(R.id.widget_count_fajr, "50 left")

        LogCompletionUseCase(repository, now = { fixedInstant }).batch(listOf(PrayerType.FAJR, PrayerType.ISHA), days = 7)

        awaitText(R.id.widget_count_fajr, "43 left")
        awaitText(R.id.widget_count_isha, "43 left")
        assertEquals("50 left", text(R.id.widget_count_dhuhr))
    }

    @Test
    fun `the widget also renders once on start, so it is never stale after an update`() = runWithWidget {
        seedBaseline(count = 33)

        updater.observeChanges(observerScope)

        awaitText(R.id.widget_count_dhuhr, "33 left")
    }
}
