package com.zilehasnain.qazatracker.ui.achievements

import com.zilehasnain.qazatracker.domain.model.PrayerType
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementTextTest {

    private val today = LocalDate.of(2026, 9, 24)

    @Test
    fun `a milestone reads as all of that prayer completed`() {
        assertEquals("All Fajr Prayers Completed 🎉", milestoneTitle(PrayerType.FAJR))
        assertEquals("All Maghrib Prayers Completed 🎉", milestoneTitle(PrayerType.MAGHRIB))
    }

    @Test
    fun `the streak titles carry the length and the emoji`() {
        assertEquals("47-day streak 🔥", currentStreakTitle(47))
        assertEquals("Best streak: 52 days ⭐", bestStreakTitle(52))
        assertEquals("Best streak: 1 day ⭐", bestStreakTitle(1))
    }

    @Test
    fun `the best-streak detail is singular for one day`() {
        assertEquals("Best: 52 days", bestStreakDetail(52))
        assertEquals("Best: 1 day", bestStreakDetail(1))
    }

    @Test
    fun `a progress badge reads as a percentage complete with a tick`() {
        assertEquals("25% complete ✓", progressTitle(25))
        assertEquals("100% complete ✓", progressTitle(100))
    }

    @Test
    fun `unlocked on shows the date, with the year once it isn't this year`() {
        assertEquals("Unlocked on: September 15", unlockedText(LocalDate.of(2026, 9, 15), today))
        assertEquals("Unlocked on: December 3, 2025", unlockedText(LocalDate.of(2025, 12, 3), today))
    }

    @Test
    fun `the hero count is singular and plural, and honest about zero`() {
        assertEquals("Nothing unlocked yet", unlockedCountText(0))
        assertEquals("1 achievement unlocked", unlockedCountText(1))
        assertEquals("6 achievements unlocked", unlockedCountText(6))
    }

    @Test
    fun `progress emoji grow with the percentage`() {
        assertEquals(AchievementEmoji.SEEDLING, AchievementEmoji.forProgress(25))
        assertEquals(AchievementEmoji.HERB, AchievementEmoji.forProgress(50))
        assertEquals(AchievementEmoji.TREE, AchievementEmoji.forProgress(75))
        assertEquals(AchievementEmoji.TROPHY, AchievementEmoji.forProgress(100))
    }

    @Test
    fun `every prayer type has its own emoji`() {
        val emoji = PrayerType.entries.map { AchievementEmoji.forPrayer(it) }

        assertEquals(PrayerType.entries.size, emoji.toSet().size)
        assertTrue(emoji.all { it.isNotEmpty() })
    }
}
