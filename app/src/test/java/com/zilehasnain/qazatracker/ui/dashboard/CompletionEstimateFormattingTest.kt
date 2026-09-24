package com.zilehasnain.qazatracker.ui.dashboard

import com.zilehasnain.qazatracker.domain.model.EstimateHorizon
import com.zilehasnain.qazatracker.ui.theme.PaceGreenContainer
import com.zilehasnain.qazatracker.ui.theme.PaceGreenContainerDark
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class CompletionEstimateFormattingTest {

    private val today = LocalDate.of(2026, 9, 24)

    // ---- Dates ----

    @Test
    fun `a date this year is month and day`() {
        assertEquals("December 15", formatEstimateDate(LocalDate.of(2026, 12, 15), today))
    }

    @Test
    fun `a date in another year includes the year`() {
        assertEquals("December 15, 2027", formatEstimateDate(LocalDate.of(2027, 12, 15), today))
    }

    @Test
    fun `the short form abbreviates the month`() {
        assertEquals("Sep 24", formatShortDate(LocalDate.of(2026, 9, 24), today))
        assertEquals("Jan 3, 2025", formatShortDate(LocalDate.of(2025, 1, 3), today))
    }

    // ---- Time remaining ----

    @Test
    fun `short spans read in days`() {
        assertEquals("~1 day", formatRemaining(1))
        assertEquals("~45 days", formatRemaining(45))
        assertEquals("~60 days", formatRemaining(60))
    }

    @Test
    fun `medium spans read in months`() {
        assertEquals("~2 months", formatRemaining(61))
        assertEquals("~3 months", formatRemaining(90))
        assertEquals("~5 months", formatRemaining(150))
        assertEquals("~24 months", formatRemaining(730))
    }

    @Test
    fun `long spans read in years`() {
        assertEquals("~2.0 years", formatRemaining(731))
        assertEquals("~5.5 years", formatRemaining(2008))
    }

    @Test
    fun `just under ten years rounds to a whole 10 rather than 10 point 0`() {
        assertEquals("~10 years", formatRemaining(3650))
        assertEquals("~15 years", formatRemaining(5479))
    }

    // ---- Pace ----

    @Test
    fun `pace shows one decimal`() {
        assertEquals("Logging ~8.5 prayers/day", formatPace(8.5f))
        assertEquals("Logging ~0.4 prayers/day", formatPace(0.42f))
        assertEquals("Logging ~5.0 prayers/day", formatPace(5f))
    }

    // ---- Colour coding ----

    @Test
    fun `each horizon gets its own colours`() {
        val near = paceColorsFor(EstimateHorizon.WITHIN_THREE_MONTHS, darkTheme = false)
        val middle = paceColorsFor(EstimateHorizon.THREE_TO_SIX_MONTHS, darkTheme = false)
        val far = paceColorsFor(EstimateHorizon.OVER_SIX_MONTHS, darkTheme = false)

        assertNotEquals(near.container, middle.container)
        assertNotEquals(middle.container, far.container)
        assertNotEquals(near.container, far.container)
    }

    @Test
    fun `near is the green set, in both themes`() {
        assertEquals(PaceGreenContainer, paceColorsFor(EstimateHorizon.WITHIN_THREE_MONTHS, darkTheme = false).container)
        assertEquals(PaceGreenContainerDark, paceColorsFor(EstimateHorizon.WITHIN_THREE_MONTHS, darkTheme = true).container)
    }

    @Test
    fun `dark theme swaps every horizon to its own dark set`() {
        EstimateHorizon.entries.forEach {
            assertNotEquals(paceColorsFor(it, darkTheme = false).container, paceColorsFor(it, darkTheme = true).container)
            assertNotEquals(paceColorsFor(it, darkTheme = false).content, paceColorsFor(it, darkTheme = true).content)
        }
    }
}
