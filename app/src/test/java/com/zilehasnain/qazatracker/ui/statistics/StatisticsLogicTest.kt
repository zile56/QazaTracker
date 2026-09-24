package com.zilehasnain.qazatracker.ui.statistics

import com.zilehasnain.qazatracker.ui.common.formatPercent
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Test

class StatisticsLogicTest {

    // ---- formatPercent ----

    @Test
    fun `zero shows as a plain 0 percent`() {
        assertEquals("0%", formatPercent(0f))
    }

    @Test
    fun `a whole-number style percent for ordinary rates`() {
        assertEquals("51%", formatPercent(51.2f))
        assertEquals("100%", formatPercent(100f))
        assertEquals("10%", formatPercent(10f))
    }

    @Test
    fun `small rates against a big backlog keep one decimal`() {
        assertEquals("1.4%", formatPercent(1.36f))
        assertEquals("5.0%", formatPercent(5f))
    }

    @Test
    fun `a real but tiny rate never reads as zero`() {
        assertEquals("<0.1%", formatPercent(0.04f))
    }

    @Test
    fun `just below ten rounds up to a whole 10 rather than 10 point 0`() {
        assertEquals("10%", formatPercent(9.96f))
    }

    // ---- month bounds ----

    private val july = YearMonth.of(2026, 7)
    private val september = YearMonth.of(2026, 9)

    @Test
    fun `a month inside the range is left alone`() {
        assertEquals(YearMonth.of(2026, 8), YearMonth.of(2026, 8).clampedTo(july, september))
    }

    @Test
    fun `going earlier than tracking began stops at the first month`() {
        assertEquals(july, YearMonth.of(2026, 1).clampedTo(july, september))
    }

    @Test
    fun `going past the current month stops at the current month`() {
        assertEquals(september, YearMonth.of(2027, 2).clampedTo(july, september))
    }

    @Test
    fun `the range endpoints themselves are allowed`() {
        assertEquals(july, july.clampedTo(july, september))
        assertEquals(september, september.clampedTo(july, september))
    }

    @Test
    fun `an earliest month after the current one (clock skew) collapses to the current month`() {
        val skewedEarliest = YearMonth.of(2026, 12)

        assertEquals(september, YearMonth.of(2026, 11).clampedTo(skewedEarliest, september))
        assertEquals(september, september.clampedTo(skewedEarliest, september))
    }
}
