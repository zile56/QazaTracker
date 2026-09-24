package com.zilehasnain.qazatracker.ui.dashboard

import com.zilehasnain.qazatracker.domain.model.PrayerType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * AdjustmentDialogState.canConfirm mirrors ApplyAdjustmentUseCase's own
 * require(delta != 0) — these two must never drift apart.
 */
class AdjustmentDialogStateTest {

    @Test
    fun `zero magnitude cannot confirm, regardless of sign`() {
        val positive = AdjustmentDialogState(PrayerType.FAJR, isNegative = false, magnitudeInput = "0")
        val negative = AdjustmentDialogState(PrayerType.FAJR, isNegative = true, magnitudeInput = "0")

        assertFalse(positive.canConfirm)
        assertFalse(negative.canConfirm)
        assertEquals(0, positive.delta)
        assertEquals(0, negative.delta)
    }

    @Test
    fun `empty magnitude cannot confirm`() {
        val state = AdjustmentDialogState(PrayerType.FAJR, magnitudeInput = "")

        assertFalse(state.canConfirm)
        assertEquals(null, state.delta)
    }

    @Test
    fun `non-numeric magnitude cannot confirm`() {
        val state = AdjustmentDialogState(PrayerType.FAJR, magnitudeInput = "abc")

        assertFalse(state.canConfirm)
        assertEquals(null, state.delta)
    }

    @Test
    fun `positive non-zero magnitude can confirm with a positive delta`() {
        val state = AdjustmentDialogState(PrayerType.FAJR, isNegative = false, magnitudeInput = "5")

        assertTrue(state.canConfirm)
        assertEquals(5, state.delta)
    }

    @Test
    fun `negative sign flips a non-zero magnitude to a negative delta`() {
        val state = AdjustmentDialogState(PrayerType.FAJR, isNegative = true, magnitudeInput = "5")

        assertTrue(state.canConfirm)
        assertEquals(-5, state.delta)
    }
}
