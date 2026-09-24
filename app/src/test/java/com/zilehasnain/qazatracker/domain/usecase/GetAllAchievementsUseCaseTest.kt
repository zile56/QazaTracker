package com.zilehasnain.qazatracker.domain.usecase

import com.zilehasnain.qazatracker.domain.model.AchievementsData
import com.zilehasnain.qazatracker.domain.model.AdjustmentEntry
import com.zilehasnain.qazatracker.domain.model.AdjustmentReason
import com.zilehasnain.qazatracker.domain.model.CompletionEntry
import com.zilehasnain.qazatracker.domain.model.MilestoneAchievement
import com.zilehasnain.qazatracker.domain.model.MilestoneEntry
import com.zilehasnain.qazatracker.domain.model.PrayerType
import com.zilehasnain.qazatracker.domain.model.ProgressAchievement
import com.zilehasnain.qazatracker.domain.model.RemainingPrayerCount
import com.zilehasnain.qazatracker.domain.model.StreakAchievement
import com.zilehasnain.qazatracker.domain.model.StreakData
import com.zilehasnain.qazatracker.domain.usecase.GetAllAchievementsUseCase.Companion.calculate
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** The pure derivation: what each section contains, and on which day each thing was unlocked. */
class GetAllAchievementsUseCaseTest {

    private val utc: ZoneId = ZoneOffset.UTC
    private val today = LocalDate.of(2026, 3, 10)

    private fun date(day: Int) = LocalDate.of(2026, 3, day)

    private fun completions(count: Int, iso: String): List<CompletionEntry> =
        List(count) { CompletionEntry(PrayerType.entries[it % PrayerType.entries.size], Instant.parse(iso)) }

    private fun adjustment(delta: Int, iso: String) =
        AdjustmentEntry(PrayerType.FAJR, delta, AdjustmentReason.MANUAL_CORRECTION, null, Instant.parse(iso))

    /** Counts exactly as the ledger query would report them for these logs. */
    private fun countsFor(
        initialPerType: Int,
        completions: List<CompletionEntry>,
        adjustments: List<AdjustmentEntry>
    ) = PrayerType.entries.map { type ->
        val completed = completions.count { it.prayerType == type }
        val delta = adjustments.filter { it.prayerType == type }.sumOf { it.delta }
        RemainingPrayerCount(type, remaining = initialPerType + delta - completed, completed = completed)
    }

    /** 20 per type = 100 owed in all, unless [initialPerType] says otherwise. */
    private fun achievements(
        completions: List<CompletionEntry> = emptyList(),
        adjustments: List<AdjustmentEntry> = emptyList(),
        milestones: List<MilestoneEntry> = emptyList(),
        streak: StreakData = StreakData.None,
        initialPerType: Int = 20,
        counts: List<RemainingPrayerCount> = countsFor(initialPerType, completions, adjustments)
    ): AchievementsData = calculate(milestones, completions, adjustments, counts, streak, utc)

    // ---- Empty ----

    @Test
    fun `with nothing logged there is nothing unlocked`() {
        val data = achievements()

        assertTrue(data.isEmpty)
        assertEquals(0, data.totalCount)
        assertEquals(AchievementsData.Empty, data)
    }

    // ---- Milestones ----

    @Test
    fun `every milestone appears with its prayer and day, in the order given`() {
        val data = achievements(
            milestones = listOf(
                MilestoneEntry(PrayerType.ISHA, Instant.parse("2026-03-09T20:00:00Z")),
                MilestoneEntry(PrayerType.FAJR, Instant.parse("2026-03-02T05:00:00Z"))
            )
        )

        assertEquals(
            listOf(MilestoneAchievement(PrayerType.ISHA, date(9)), MilestoneAchievement(PrayerType.FAJR, date(2))),
            data.milestones
        )
    }

    @Test
    fun `a milestone's day follows the timezone`() {
        val late = MilestoneEntry(PrayerType.MAGHRIB, Instant.parse("2026-03-01T22:00:00Z"))

        val inKarachi = calculate(listOf(late), emptyList(), emptyList(), emptyList(), StreakData.None, ZoneId.of("Asia/Karachi"))

        assertEquals(date(2), inKarachi.milestones.single().achievedDate)
    }

    @Test
    fun `finishing the same prayer twice is two milestones`() {
        val data = achievements(
            milestones = listOf(
                MilestoneEntry(PrayerType.FAJR, Instant.parse("2026-03-08T05:00:00Z")),
                MilestoneEntry(PrayerType.FAJR, Instant.parse("2026-03-01T05:00:00Z"))
            )
        )

        assertEquals(2, data.milestones.size)
    }

    // ---- Streaks ----

    @Test
    fun `a streak that is also the best shows as one entry`() {
        val data = achievements(
            streak = StreakData(currentStreak = 4, longestStreak = 4, lastLoggedDate = today, longestStreakEndedOn = today)
        )

        assertEquals(listOf(StreakAchievement(4, today, isCurrent = true)), data.streaks)
    }

    @Test
    fun `a current streak shorter than the best shows both`() {
        val data = achievements(
            streak = StreakData(
                currentStreak = 2, longestStreak = 5, lastLoggedDate = today, longestStreakEndedOn = date(1)
            )
        )

        assertEquals(
            listOf(StreakAchievement(2, today, isCurrent = true), StreakAchievement(5, date(1), isCurrent = false)),
            data.streaks
        )
    }

    @Test
    fun `a broken streak leaves only the best`() {
        val data = achievements(
            streak = StreakData(
                currentStreak = 0, longestStreak = 4, lastLoggedDate = date(5),
                streakBrokenDate = date(6), longestStreakEndedOn = date(4)
            )
        )

        assertEquals(listOf(StreakAchievement(4, date(4), isCurrent = false)), data.streaks)
    }

    @Test
    fun `no streak data means no streak entries`() {
        assertTrue(achievements(streak = StreakData.None).streaks.isEmpty())
    }

    // ---- Progress badges ----

    @Test
    fun `each badge is dated to the day its threshold was first reached`() {
        // 100 owed. 25 done on the 1st, 50 on the 3rd, 75 on the 5th, all 100 on the 7th.
        val done = completions(25, "2026-03-01T10:00:00Z") + completions(25, "2026-03-03T10:00:00Z") +
            completions(25, "2026-03-05T10:00:00Z") + completions(25, "2026-03-07T10:00:00Z")

        val badges = achievements(completions = done).progressBadges

        assertEquals(
            listOf(
                ProgressAchievement(25, date(1)), ProgressAchievement(50, date(3)),
                ProgressAchievement(75, date(5)), ProgressAchievement(100, date(7))
            ),
            badges
        )
    }

    @Test
    fun `just under a threshold does not unlock it, exactly on it does`() {
        assertTrue(achievements(completions = completions(24, "2026-03-01T10:00:00Z")).progressBadges.isEmpty())

        val at25 = achievements(completions = completions(25, "2026-03-01T10:00:00Z")).progressBadges

        assertEquals(listOf(ProgressAchievement(25, date(1))), at25)
    }

    @Test
    fun `one big batch can unlock several badges on the same day`() {
        val badges = achievements(completions = completions(60, "2026-03-02T10:00:00Z")).progressBadges

        assertEquals(listOf(ProgressAchievement(25, date(2)), ProgressAchievement(50, date(2))), badges)
    }

    @Test
    fun `clearing everything at once unlocks all four badges, in order`() {
        val done = completions(100, "2026-03-04T10:00:00Z")

        assertEquals(listOf(25, 50, 75, 100), achievements(completions = done).progressBadges.map { it.percentage })
    }

    @Test
    fun `an adjustment that lowers what is owed brings a badge forward`() {
        // 100 owed, then a -20 correction: 20 owed less means 20 completions is already 25%.
        val done = completions(20, "2026-03-04T10:00:00Z")
        val correction = listOf(adjustment(-20, "2026-03-03T10:00:00Z"))

        val badges = achievements(completions = done, adjustments = correction).progressBadges

        assertEquals(listOf(ProgressAchievement(25, date(4))), badges)
    }

    @Test
    fun `a correction that later lifts a threshold unlocks the badge on the correction's day`() {
        // 30 of 100 done on the 1st (30%). On the 5th a -40 correction leaves 60 owed: 30/60 = 50%.
        val done = completions(30, "2026-03-01T10:00:00Z")
        val correction = listOf(adjustment(-40, "2026-03-05T10:00:00Z"))

        val badges = achievements(completions = done, adjustments = correction).progressBadges

        assertEquals(listOf(ProgressAchievement(25, date(1)), ProgressAchievement(50, date(5))), badges)
    }

    @Test
    fun `a badge stays unlocked when added missed prayers push the percentage back down`() {
        val done = completions(50, "2026-03-01T10:00:00Z")
        val addedBack = listOf(adjustment(+100, "2026-03-05T10:00:00Z"))

        val badges = achievements(completions = done, adjustments = addedBack).progressBadges

        assertEquals(listOf(25, 50), badges.map { it.percentage })
    }

    @Test
    fun `input order does not matter, events are replayed by time`() {
        val early = completions(25, "2026-03-01T10:00:00Z")
        val late = completions(25, "2026-03-06T10:00:00Z")

        val shuffled = achievements(completions = late + early).progressBadges

        assertEquals(listOf(ProgressAchievement(25, date(1)), ProgressAchievement(50, date(6))), shuffled)
    }

    @Test
    fun `no baseline means no progress badges, even with completions on record`() {
        val data = achievements(completions = completions(30, "2026-03-01T10:00:00Z"), counts = emptyList())

        assertTrue(data.progressBadges.isEmpty())
    }

    @Test
    fun `a badge's day follows the timezone`() {
        val done = completions(25, "2026-03-01T22:00:00Z") // already the 2nd in Karachi

        val inKarachi = calculate(
            emptyList(), done, emptyList(), countsFor(20, done, emptyList()), StreakData.None, ZoneId.of("Asia/Karachi")
        )

        assertEquals(date(2), inKarachi.progressBadges.single().achievedDate)
    }

    // ---- Together ----

    @Test
    fun `the total counts every unlocked item across the three sections`() {
        val done = completions(50, "2026-03-01T10:00:00Z")
        val data = achievements(
            completions = done,
            milestones = listOf(MilestoneEntry(PrayerType.FAJR, Instant.parse("2026-03-01T10:00:00Z"))),
            streak = StreakData(3, 3, today, longestStreakEndedOn = today)
        )

        // 1 milestone + 1 streak + badges 25% and 50%.
        assertEquals(4, data.totalCount)
    }
}
