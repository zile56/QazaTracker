package com.zilehasnain.qazatracker.ui.dashboard

import com.zilehasnain.qazatracker.domain.model.MilestoneData
import com.zilehasnain.qazatracker.domain.model.PrayerMilestone
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MilestoneCelebrationTrackerTest {

    private val today = LocalDate.of(2026, 3, 10)

    private fun milestone(kind: PrayerMilestone, date: LocalDate = today) =
        MilestoneData(kind, date, isNewToday = date == today)

    @Test
    fun `the first emission never celebrates, even for a milestone from earlier today`() {
        val tracker = MilestoneCelebrationTracker()

        // e.g. the app was reopened after finishing Fajr this morning.
        val celebrations = tracker.newCelebrations(listOf(milestone(PrayerMilestone.COMPLETE_FAJR)))

        assertTrue(celebrations.isEmpty())
    }

    @Test
    fun `a milestone that appears after the first emission is celebrated`() {
        val tracker = MilestoneCelebrationTracker()
        tracker.newCelebrations(emptyList())

        val celebrations = tracker.newCelebrations(listOf(milestone(PrayerMilestone.COMPLETE_FAJR)))

        assertEquals(listOf(milestone(PrayerMilestone.COMPLETE_FAJR)), celebrations)
    }

    @Test
    fun `a repeat emission with nothing new doesn't celebrate again`() {
        val tracker = MilestoneCelebrationTracker()
        tracker.newCelebrations(emptyList())
        tracker.newCelebrations(listOf(milestone(PrayerMilestone.COMPLETE_FAJR)))

        val again = tracker.newCelebrations(listOf(milestone(PrayerMilestone.COMPLETE_FAJR)))

        assertTrue(again.isEmpty())
    }

    @Test
    fun `only the new entries are celebrated, not the older ones behind them`() {
        val tracker = MilestoneCelebrationTracker()
        val older = milestone(PrayerMilestone.COMPLETE_FAJR, LocalDate.of(2026, 3, 1))
        tracker.newCelebrations(listOf(older))

        // Newest first: the just-achieved Asr sits in front of the existing Fajr.
        val celebrations = tracker.newCelebrations(listOf(milestone(PrayerMilestone.COMPLETE_ASR), older))

        assertEquals(listOf(milestone(PrayerMilestone.COMPLETE_ASR)), celebrations)
    }

    @Test
    fun `several milestones achieved at once are all celebrated`() {
        val tracker = MilestoneCelebrationTracker()
        tracker.newCelebrations(emptyList())

        val celebrations = tracker.newCelebrations(
            listOf(milestone(PrayerMilestone.COMPLETE_ISHA), milestone(PrayerMilestone.COMPLETE_FAJR))
        )

        assertEquals(2, celebrations.size)
    }

    @Test
    fun `a new milestone that isn't dated today is not celebrated`() {
        val tracker = MilestoneCelebrationTracker()
        tracker.newCelebrations(emptyList())

        val stale = milestone(PrayerMilestone.COMPLETE_DHUHR, LocalDate.of(2026, 3, 9))

        assertTrue(tracker.newCelebrations(listOf(stale)).isEmpty())
    }
}
