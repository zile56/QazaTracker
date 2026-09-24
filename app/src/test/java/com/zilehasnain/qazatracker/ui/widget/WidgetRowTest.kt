package com.zilehasnain.qazatracker.ui.widget

import com.zilehasnain.qazatracker.domain.model.PrayerType
import com.zilehasnain.qazatracker.domain.model.RemainingPrayerCount
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WidgetRowTest {

    private fun counts(remaining: Map<PrayerType, Int>) =
        remaining.map { (type, left) -> RemainingPrayerCount(type, remaining = left, completed = 0) }

    @Test
    fun `no baseline means no rows, so the widget can show its setup message`() {
        assertNull(widgetRowsFor(emptyList()))
    }

    @Test
    fun `all five prayer types come back in prayer order`() {
        val rows = widgetRowsFor(counts(PrayerType.entries.associateWith { 100 }))!!

        assertEquals(PrayerType.entries, rows.map { it.prayerType })
    }

    @Test
    fun `each row carries its own remaining count`() {
        val rows = widgetRowsFor(
            counts(
                mapOf(
                    PrayerType.FAJR to 2191, PrayerType.DHUHR to 2000, PrayerType.ASR to 1500,
                    PrayerType.MAGHRIB to 10, PrayerType.ISHA to 1
                )
            )
        )!!.associateBy { it.prayerType }

        assertEquals(2191, rows.getValue(PrayerType.FAJR).remaining)
        assertEquals(1500, rows.getValue(PrayerType.ASR).remaining)
        assertEquals(1, rows.getValue(PrayerType.ISHA).remaining)
    }

    @Test
    fun `over-logged reads as done rather than a negative number`() {
        val rows = widgetRowsFor(counts(PrayerType.entries.associateWith { -3 }))!!

        assertEquals(0, rows.first().remaining)
        assertEquals("Done ✓", rows.first().countText)
    }

    @Test
    fun `a type missing from the counts reads as done`() {
        val rows = widgetRowsFor(counts(mapOf(PrayerType.FAJR to 5)))!!.associateBy { it.prayerType }

        assertEquals("5 left", rows.getValue(PrayerType.FAJR).countText)
        assertEquals("Done ✓", rows.getValue(PrayerType.ISHA).countText)
    }

    @Test
    fun `the count text reads like the dashboard`() {
        assertEquals("2191 left", WidgetRow(PrayerType.FAJR, 2191).countText)
        assertEquals("1 left", WidgetRow(PrayerType.FAJR, 1).countText)
        assertEquals("Done ✓", WidgetRow(PrayerType.FAJR, 0).countText)
    }

    @Test
    fun `the toasts name the prayer`() {
        assertEquals("Fajr logged ✓", WidgetText.logged(PrayerType.FAJR))
        assertEquals("Maghrib logged ✓", WidgetText.logged(PrayerType.MAGHRIB))
        assertEquals("Couldn't log Asr", WidgetText.failed(PrayerType.ASR))
    }
}
