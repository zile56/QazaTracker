package com.zilehasnain.qazatracker.domain.usecase

import com.zilehasnain.qazatracker.domain.model.CompletionEntry
import com.zilehasnain.qazatracker.domain.model.CompletionEstimate
import com.zilehasnain.qazatracker.domain.model.CompletionProjection
import com.zilehasnain.qazatracker.domain.model.EstimateConfidence
import com.zilehasnain.qazatracker.domain.model.EstimateHorizon
import com.zilehasnain.qazatracker.domain.model.PrayerType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProjectCompletionDateUseCaseTest {

    private val useCase = ProjectCompletionDateUseCase()
    private val today = LocalDate.of(2026, 3, 10)
    private val utc: ZoneId = ZoneOffset.UTC

    /** [count] prayers logged at midday, [daysAgo] days before today. */
    private fun logged(daysAgo: Long, count: Int = 1): List<CompletionEntry> {
        val instant = today.minusDays(daysAgo).atTime(12, 0).atZone(utc).toInstant()
        return List(count) { CompletionEntry(PrayerType.entries[it % PrayerType.entries.size], instant) }
    }

    /** [total] prayers spread over [distinctDays] different days (the most recent days first). */
    private fun spread(total: Int, distinctDays: Int): List<CompletionEntry> =
        (0 until distinctDays).flatMap { day ->
            val share = total / distinctDays + if (day < total % distinctDays) 1 else 0
            logged(daysAgo = day.toLong(), count = share)
        }

    private fun project(
        remaining: Int,
        completions: List<CompletionEntry>,
        startedDaysAgo: Long,
        zone: ZoneId = utc
    ) = useCase(remaining, completions, today.minusDays(startedDaysAgo), today, zone)

    private fun estimateOf(projection: CompletionProjection): CompletionEstimate =
        (projection as CompletionProjection.Estimated).estimate

    // ---- The three states ----

    @Test
    fun `nothing remaining means already caught up`() {
        assertEquals(CompletionProjection.AlreadyCaughtUp, project(0, spread(50, 10), startedDaysAgo = 10))
    }

    @Test
    fun `caught up wins even with no history at all`() {
        assertEquals(CompletionProjection.AlreadyCaughtUp, project(0, emptyList(), startedDaysAgo = 0))
    }

    @Test
    fun `no completions yet is not enough data`() {
        assertEquals(CompletionProjection.InsufficientData, project(100, emptyList(), startedDaysAgo = 10))
    }

    @Test
    fun `tracking that began today is not enough data, even with completions`() {
        assertEquals(CompletionProjection.InsufficientData, project(100, logged(0, 20), startedDaysAgo = 0))
    }

    // ---- The estimate itself ----

    @Test
    fun `the date is today plus the days the current pace needs`() {
        // 50 done over 10 days = 5 a day; 100 remaining = 20 days.
        val estimate = estimateOf(project(100, spread(50, 10), startedDaysAgo = 10))

        assertEquals(20, estimate.daysRemaining)
        assertEquals(today.plusDays(20), estimate.estimatedDate)
        assertEquals(5f, estimate.averagePrayersPerDay, 0.0001f)
    }

    @Test
    fun `a part-day rounds up, never down`() {
        // 100 / 5 = 20 exactly; 101 / 5 = 20.2 -> 21.
        assertEquals(20, estimateOf(project(100, spread(50, 10), startedDaysAgo = 10)).daysRemaining)
        assertEquals(21, estimateOf(project(101, spread(50, 10), startedDaysAgo = 10)).daysRemaining)
    }

    @Test
    fun `pace counts every day since tracking began, including days off`() {
        // 50 prayers logged on only 2 days, but 25 days have passed: 2 a day, not 25 a day.
        val estimate = estimateOf(project(100, spread(50, 2), startedDaysAgo = 25))

        assertEquals(2f, estimate.averagePrayersPerDay, 0.0001f)
        assertEquals(50, estimate.daysRemaining)
    }

    @Test
    fun `logging more prayers pulls the completion date earlier`() {
        val before = estimateOf(project(remaining = 100, completions = spread(50, 10), startedDaysAgo = 10))
        // Ten more prayers logged: 10 fewer remaining, 10 more completed.
        val after = estimateOf(project(remaining = 90, completions = spread(60, 10), startedDaysAgo = 10))

        assertEquals(20, before.daysRemaining)
        assertEquals(15, after.daysRemaining)
        assertTrue(after.estimatedDate.isBefore(before.estimatedDate))
    }

    @Test
    fun `the breakdown carries the numbers behind the estimate`() {
        val estimate = estimateOf(project(100, spread(50, 10), startedDaysAgo = 10))

        assertEquals(today.minusDays(10), estimate.trackingStartedOn)
        assertEquals(10, estimate.daysTracked)
        assertEquals(10, estimate.daysLogged)
        assertEquals(50, estimate.prayersCompleted)
        assertEquals(100, estimate.prayersRemaining)
    }

    @Test
    fun `an absurdly slow pace clamps instead of overflowing`() {
        val estimate = estimateOf(project(Int.MAX_VALUE, logged(0, 1), startedDaysAgo = 3650))

        assertEquals(Int.MAX_VALUE, estimate.daysRemaining)
    }

    // ---- Confidence is about distinct days logged ----

    @Test
    fun `fewer than 3 days logged is low confidence`() {
        assertEquals(EstimateConfidence.LOW, estimateOf(project(100, spread(50, 1), startedDaysAgo = 10)).confidence)
        assertEquals(EstimateConfidence.LOW, estimateOf(project(100, spread(50, 2), startedDaysAgo = 10)).confidence)
    }

    @Test
    fun `3 to 6 days logged is medium confidence`() {
        assertEquals(EstimateConfidence.MEDIUM, estimateOf(project(100, spread(50, 3), startedDaysAgo = 10)).confidence)
        assertEquals(EstimateConfidence.MEDIUM, estimateOf(project(100, spread(50, 6), startedDaysAgo = 10)).confidence)
    }

    @Test
    fun `7 or more days logged is high confidence`() {
        assertEquals(EstimateConfidence.HIGH, estimateOf(project(100, spread(50, 7), startedDaysAgo = 10)).confidence)
        assertEquals(EstimateConfidence.HIGH, estimateOf(project(100, spread(50, 10), startedDaysAgo = 10)).confidence)
    }

    @Test
    fun `one huge batch on a single day is still only one day of evidence`() {
        // 300 prayers logged at once, a month after starting: a pace exists, but confidence is low.
        val estimate = estimateOf(project(1000, logged(daysAgo = 2, count = 300), startedDaysAgo = 30))

        assertEquals(1, estimate.daysLogged)
        assertEquals(EstimateConfidence.LOW, estimate.confidence)
    }

    @Test
    fun `days are counted in the given timezone`() {
        // 20:00 and 22:00 UTC are the same UTC day, but 01:00 and 03:00 the next day in Karachi (UTC+5),
        // so both are one day there; 18:00 UTC is still the earlier local day.
        val sameUtcDay = listOf(
            CompletionEntry(PrayerType.FAJR, Instant.parse("2026-03-05T18:00:00Z")),
            CompletionEntry(PrayerType.ASR, Instant.parse("2026-03-05T20:00:00Z")),
            CompletionEntry(PrayerType.ISHA, Instant.parse("2026-03-05T22:00:00Z"))
        )

        val inUtc = estimateOf(project(100, sameUtcDay, startedDaysAgo = 10, zone = utc))
        val inKarachi = estimateOf(project(100, sameUtcDay, startedDaysAgo = 10, zone = ZoneId.of("Asia/Karachi")))

        assertEquals(1, inUtc.daysLogged)
        assertEquals(2, inKarachi.daysLogged)
    }

    @Test
    fun `the confidence thresholds are exactly 3 and 7`() {
        assertEquals(EstimateConfidence.LOW, EstimateConfidence.forDaysLogged(0))
        assertEquals(EstimateConfidence.LOW, EstimateConfidence.forDaysLogged(2))
        assertEquals(EstimateConfidence.MEDIUM, EstimateConfidence.forDaysLogged(3))
        assertEquals(EstimateConfidence.MEDIUM, EstimateConfidence.forDaysLogged(6))
        assertEquals(EstimateConfidence.HIGH, EstimateConfidence.forDaysLogged(7))
        assertEquals("High", EstimateConfidence.HIGH.label)
        assertEquals("Low", EstimateConfidence.LOW.label)
    }

    // ---- Progress and colour horizon ----

    @Test
    fun `the timeline fraction is the share of the whole span already elapsed`() {
        // 10 days tracked, 20 to go: a third of the way.
        val estimate = estimateOf(project(100, spread(50, 10), startedDaysAgo = 10))

        assertEquals(1f / 3f, estimate.fractionElapsed, 0.0001f)
    }

    @Test
    fun `the timeline fraction always matches completed over completed plus remaining`() {
        val estimate = estimateOf(project(remaining = 100, completions = spread(50, 10), startedDaysAgo = 10))

        assertEquals(50f / (50 + 100), estimate.fractionElapsed, 0.01f)
    }

    private fun estimateFinishing(on: LocalDate) = CompletionEstimate(
        estimatedDate = on,
        daysRemaining = 1,
        averagePrayersPerDay = 1f,
        confidence = EstimateConfidence.HIGH,
        trackingStartedOn = today.minusDays(10),
        daysTracked = 10,
        daysLogged = 10,
        prayersCompleted = 10,
        prayersRemaining = 1
    )

    @Test
    fun `within three months is the near horizon, boundary included`() {
        assertEquals(EstimateHorizon.WITHIN_THREE_MONTHS, estimateFinishing(today.plusDays(1)).horizon(today))
        assertEquals(EstimateHorizon.WITHIN_THREE_MONTHS, estimateFinishing(today.plusMonths(3)).horizon(today))
    }

    @Test
    fun `three to six months is the middle horizon, boundaries included`() {
        assertEquals(EstimateHorizon.THREE_TO_SIX_MONTHS, estimateFinishing(today.plusMonths(3).plusDays(1)).horizon(today))
        assertEquals(EstimateHorizon.THREE_TO_SIX_MONTHS, estimateFinishing(today.plusMonths(6)).horizon(today))
    }

    @Test
    fun `beyond six months is the far horizon`() {
        assertEquals(EstimateHorizon.OVER_SIX_MONTHS, estimateFinishing(today.plusMonths(6).plusDays(1)).horizon(today))
        assertEquals(EstimateHorizon.OVER_SIX_MONTHS, estimateFinishing(today.plusYears(5)).horizon(today))
    }
}
