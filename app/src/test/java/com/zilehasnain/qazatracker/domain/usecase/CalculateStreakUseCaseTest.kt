package com.zilehasnain.qazatracker.domain.usecase

import com.zilehasnain.qazatracker.domain.model.CompletionEntry
import com.zilehasnain.qazatracker.domain.model.PrayerType
import com.zilehasnain.qazatracker.domain.model.StreakData
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CalculateStreakUseCaseTest {

    private val calculateStreak = CalculateStreakUseCase()
    private val zone: ZoneId = ZoneOffset.UTC
    private val today = LocalDate.of(2026, 3, 10)

    /** [count] prayers logged at midday on the day that is [daysAgo] days before today. */
    private fun logged(daysAgo: Long, count: Int = 3): List<CompletionEntry> {
        val instant = today.minusDays(daysAgo).atTime(12, 0).atZone(zone).toInstant()
        return List(count) { CompletionEntry(PrayerType.entries[it % PrayerType.entries.size], instant) }
    }

    private fun streakFor(vararg entries: List<CompletionEntry>): StreakData =
        calculateStreak(entries.toList().flatten(), today, zone)

    @Test
    fun `nothing logged yet means no streak at all`() {
        assertEquals(StreakData.None, streakFor())
    }

    @Test
    fun `logging 3 prayers today gives a streak of 1`() {
        val streak = streakFor(logged(daysAgo = 0))

        assertEquals(1, streak.currentStreak)
        assertEquals(1, streak.longestStreak)
        assertEquals(today, streak.lastLoggedDate)
        assertNull(streak.streakBrokenDate)
    }

    @Test
    fun `logging again the next day makes it 2`() {
        val streak = streakFor(logged(daysAgo = 1), logged(daysAgo = 0))

        assertEquals(2, streak.currentStreak)
        assertEquals(2, streak.longestStreak)
    }

    @Test
    fun `several prayers on the same day still count as one day`() {
        val streak = streakFor(logged(daysAgo = 0, count = 12))

        assertEquals(1, streak.currentStreak)
    }

    @Test
    fun `skipping a day resets the streak to 1 on the next log, and the longest is kept`() {
        // A 3-day run, then a missed day, then today.
        val streak = streakFor(logged(4), logged(3), logged(2), logged(0))

        assertEquals(1, streak.currentStreak)
        assertEquals(3, streak.longestStreak)
    }

    @Test
    fun `the longest streak comes from an earlier run when the current one is shorter`() {
        // 5-day run long ago, gap, then a 2-day run ending today.
        val streak = streakFor(
            logged(12), logged(11), logged(10), logged(9), logged(8),
            logged(1), logged(0)
        )

        assertEquals(2, streak.currentStreak)
        assertEquals(5, streak.longestStreak)
    }

    @Test
    fun `the longest streak grows when the current run overtakes it`() {
        val streak = streakFor(logged(6), logged(5), logged(3), logged(2), logged(1), logged(0))

        assertEquals(4, streak.currentStreak)
        assertEquals(4, streak.longestStreak)
    }

    @Test
    fun `a streak is still alive if the last log was yesterday and today is not logged yet`() {
        val streak = streakFor(logged(2), logged(1))

        assertEquals(2, streak.currentStreak)
        assertNull(streak.streakBrokenDate)
    }

    @Test
    fun `after a missed day with no log today the current streak reads 0 and the break date is recorded`() {
        val streak = streakFor(logged(4), logged(3), logged(2))

        assertEquals(0, streak.currentStreak)
        assertEquals(3, streak.longestStreak)
        assertEquals(today.minusDays(2), streak.lastLoggedDate)
        assertEquals(today.minusDays(1), streak.streakBrokenDate)
    }

    @Test
    fun `days are taken in the given timezone, not UTC`() {
        // 22:00 UTC on Mar 9 is already 03:00 on Mar 10 in Karachi (UTC+5).
        val lateEvening = java.time.Instant.parse("2026-03-09T22:00:00Z")
        val karachi = ZoneId.of("Asia/Karachi")
        val completions = listOf(CompletionEntry(PrayerType.ISHA, lateEvening))

        val streak = calculateStreak(completions, today, karachi)

        assertEquals(today, streak.lastLoggedDate)
        assertEquals(1, streak.currentStreak)
    }

    @Test
    fun `entry order doesn't matter`() {
        val shuffled = (logged(0) + logged(2) + logged(1)).shuffled(java.util.Random(7))

        val streak = calculateStreak(shuffled, today, zone)

        assertEquals(3, streak.currentStreak)
    }
}
