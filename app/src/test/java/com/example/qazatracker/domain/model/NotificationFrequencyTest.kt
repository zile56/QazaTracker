package com.example.qazatracker.domain.model

import java.time.Duration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Pure mapping, no Android/WorkManager involved — the interval each frequency schedules at. */
class NotificationFrequencyTest {

    @Test
    fun `never maps to null, the signal to cancel any pending work`() {
        assertNull(NotificationFrequency.NEVER.repeatInterval())
    }

    @Test
    fun `daily maps to a 24 hour interval`() {
        assertEquals(Duration.ofHours(24), NotificationFrequency.DAILY.repeatInterval())
    }

    @Test
    fun `weekly maps to a 7 day interval`() {
        assertEquals(Duration.ofDays(7), NotificationFrequency.WEEKLY.repeatInterval())
    }

    @Test
    fun `biweekly maps to a 14 day interval`() {
        assertEquals(Duration.ofDays(14), NotificationFrequency.BIWEEKLY.repeatInterval())
    }
}
