package com.zilehasnain.qazatracker.domain.usecase

import com.zilehasnain.qazatracker.domain.model.CompletionEntry
import com.zilehasnain.qazatracker.domain.model.MonthlyStatsData
import com.zilehasnain.qazatracker.domain.model.PrayerType
import com.zilehasnain.qazatracker.domain.model.RemainingPrayerCount
import com.zilehasnain.qazatracker.domain.usecase.CalculateMonthlyStatsUseCase.Companion.calculate
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Test

/** Pure calculation, no database — the counts and the rate are what this screen must get right. */
class CalculateMonthlyStatsUseCaseTest {

    private val september = YearMonth.of(2026, 9)
    private val utc: ZoneId = ZoneOffset.UTC

    private fun done(type: PrayerType, isoInstant: String) = CompletionEntry(type, Instant.parse(isoInstant))

    private fun done(type: PrayerType, times: Int, isoInstant: String) =
        List(times) { done(type, isoInstant) }

    /** [owed] is baseline + adjustments; remaining is derived from what's been completed overall. */
    private fun counts(owed: Int, completedByType: Map<PrayerType, Int>) =
        PrayerType.entries.map {
            val completed = completedByType[it] ?: 0
            RemainingPrayerCount(it, remaining = owed - completed, completed = completed)
        }

    @Test
    fun `counts completions per prayer type within the month`() {
        val completions =
            done(PrayerType.FAJR, 3, "2026-09-05T10:00:00Z") +
                done(PrayerType.ASR, 2, "2026-09-20T15:00:00Z") +
                done(PrayerType.ISHA, 1, "2026-09-30T22:00:00Z")

        val stats = calculate(september, completions, counts(50, emptyMap()), utc)

        assertEquals(3, stats.prayerStats[PrayerType.FAJR])
        assertEquals(0, stats.prayerStats[PrayerType.DHUHR])
        assertEquals(2, stats.prayerStats[PrayerType.ASR])
        assertEquals(0, stats.prayerStats[PrayerType.MAGHRIB])
        assertEquals(1, stats.prayerStats[PrayerType.ISHA])
        assertEquals(6, stats.totalCompleted)
    }

    @Test
    fun `completions from other months are not counted`() {
        val completions =
            done(PrayerType.FAJR, 4, "2026-08-31T12:00:00Z") +
                done(PrayerType.FAJR, 2, "2026-09-10T12:00:00Z") +
                done(PrayerType.FAJR, 5, "2026-10-01T12:00:00Z") +
                done(PrayerType.FAJR, 7, "2025-09-10T12:00:00Z") // same month, previous year

        val stats = calculate(september, completions, counts(50, emptyMap()), utc)

        assertEquals(2, stats.prayerStats[PrayerType.FAJR])
    }

    @Test
    fun `the first and last instants of the month both belong to it`() {
        val completions = listOf(
            done(PrayerType.DHUHR, "2026-09-01T00:00:00Z"),
            done(PrayerType.DHUHR, "2026-09-30T23:59:59Z"),
            done(PrayerType.DHUHR, "2026-10-01T00:00:00Z") // next month
        )

        val stats = calculate(september, completions, counts(50, emptyMap()), utc)

        assertEquals(2, stats.prayerStats[PrayerType.DHUHR])
    }

    @Test
    fun `months are cut in the given timezone, not UTC`() {
        // 20:00 UTC on Sep 30 is already 01:00 on Oct 1 in Karachi (UTC+5).
        val completions = done(PrayerType.MAGHRIB, 3, "2026-09-30T20:00:00Z")
        val karachi = ZoneId.of("Asia/Karachi")

        val inSeptember = calculate(september, completions, counts(50, emptyMap()), karachi)
        val inOctober = calculate(YearMonth.of(2026, 10), completions, counts(50, emptyMap()), karachi)

        assertEquals(0, inSeptember.prayerStats[PrayerType.MAGHRIB])
        assertEquals(3, inOctober.prayerStats[PrayerType.MAGHRIB])
    }

    @Test
    fun `completion rate is the month's completions as a percentage of everything owed`() {
        // 5 types x 50 owed = 250. Completed this month: 25 + 26 + 25 + 26 + 26 = 128.
        val perType = mapOf(
            PrayerType.FAJR to 25, PrayerType.DHUHR to 26, PrayerType.ASR to 25,
            PrayerType.MAGHRIB to 26, PrayerType.ISHA to 26
        )
        val completions = perType.flatMap { (type, n) -> done(type, n, "2026-09-12T09:00:00Z") }

        val stats = calculate(september, completions, counts(50, perType), utc)

        assertEquals(128, stats.totalCompleted)
        assertEquals(250, stats.totalOwed)
        assertEquals(51.2f, stats.completionRate, 0.001f)
    }

    @Test
    fun `each prayer type gets its own owed total and rate`() {
        val completions = done(PrayerType.FAJR, 45, "2026-09-12T09:00:00Z") +
            done(PrayerType.DHUHR, 48, "2026-09-12T09:00:00Z")

        val stats = calculate(
            september,
            completions,
            counts(50, mapOf(PrayerType.FAJR to 45, PrayerType.DHUHR to 48)),
            utc
        )

        assertEquals(50, stats.prayerTotals[PrayerType.FAJR])
        assertEquals(90f, stats.rateFor(PrayerType.FAJR), 0.001f)
        assertEquals(96f, stats.rateFor(PrayerType.DHUHR), 0.001f)
        assertEquals(0f, stats.rateFor(PrayerType.ASR), 0.001f)
    }

    @Test
    fun `owed includes adjustments because it is completed plus remaining`() {
        // A baseline of 100 with a -30 correction leaves 70 owed in all; 10 done so far.
        val counts = listOf(RemainingPrayerCount(PrayerType.ASR, remaining = 60, completed = 10))
        val completions = done(PrayerType.ASR, 10, "2026-09-03T12:00:00Z")

        val stats = calculate(september, completions, counts, utc)

        assertEquals(70, stats.prayerTotals[PrayerType.ASR])
    }

    @Test
    fun `a month against a large backlog gives a small honest rate`() {
        val counts = listOf(RemainingPrayerCount(PrayerType.FAJR, remaining = 3287, completed = 45))
        val completions = done(PrayerType.FAJR, 45, "2026-09-12T09:00:00Z")

        val stats = calculate(september, completions, counts, utc)

        assertEquals(3332, stats.totalOwed)
        assertEquals(1.35f, stats.completionRate, 0.01f)
    }

    @Test
    fun `nothing owed means a rate of zero rather than a divide-by-zero`() {
        val stats = calculate(september, emptyList(), emptyList(), utc)

        assertEquals(0, stats.totalOwed)
        assertEquals(0f, stats.completionRate, 0f)
        assertEquals(0f, stats.rateFor(PrayerType.FAJR), 0f)
    }

    @Test
    fun `over-logging is floored so it never shrinks the total, and the rate caps at 100`() {
        // Baseline 5, but 8 completed: remaining is -3.
        val counts = listOf(RemainingPrayerCount(PrayerType.FAJR, remaining = -3, completed = 8))
        val completions = done(PrayerType.FAJR, 8, "2026-09-03T12:00:00Z")

        val stats = calculate(september, completions, counts, utc)

        assertEquals(8, stats.prayerStats[PrayerType.FAJR])
        assertEquals(100f, stats.rateFor(PrayerType.FAJR), 0f)
        assertEquals(100f, stats.completionRate, 0f)
    }

    @Test
    fun `an empty month still lists every prayer type at zero`() {
        val stats = calculate(september, emptyList(), counts(50, emptyMap()), utc)

        assertEquals(PrayerType.entries.toSet(), stats.prayerStats.keys)
        assertEquals(0, stats.totalCompleted)
        assertEquals(0f, stats.completionRate, 0f)
        assertEquals(250, stats.totalOwed)
    }

    @Test
    fun `the empty placeholder has the given month and all zeros`() {
        val empty = MonthlyStatsData.empty(september)

        assertEquals(september, empty.month)
        assertEquals(0, empty.totalCompleted)
        assertEquals(PrayerType.entries.size, empty.prayerStats.size)
    }
}
