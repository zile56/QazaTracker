package com.example.qazatracker.domain.usecase

import com.example.qazatracker.domain.model.BaselineCalculationInput
import com.example.qazatracker.domain.model.CalculationMethod
import com.example.qazatracker.domain.model.PrayerType
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class CalculateBaselineUseCaseTest {

    private val useCase = CalculateBaselineUseCase()

    // ---- Exact dates: basic behavior ----

    @Test
    fun `exact dates counts both endpoints inclusively`() {
        val result = useCase(
            BaselineCalculationInput.ExactDates(
                from = LocalDate.of(2026, 1, 1),
                to = LocalDate.of(2026, 1, 10)
            )
        )

        assertEquals(10L, result.missedDays)
        assertEquals(CalculationMethod.EXACT_DATES, result.method)
    }

    @Test
    fun `exact dates applies the same count to all five prayer types`() {
        val result = useCase(
            BaselineCalculationInput.ExactDates(
                from = LocalDate.of(2026, 1, 1),
                to = LocalDate.of(2026, 1, 10)
            )
        )

        assertEquals(PrayerType.entries.toSet(), result.countsByPrayerType.keys)
        assertEquals(setOf(10), result.countsByPrayerType.values.toSet())
    }

    @Test
    fun `exact dates same day counts as a single missed day`() {
        val result = useCase(
            BaselineCalculationInput.ExactDates(
                from = LocalDate.of(2026, 5, 4),
                to = LocalDate.of(2026, 5, 4)
            )
        )

        assertEquals(1L, result.missedDays)
    }

    @Test
    fun `exact dates rejects an end date before the start date`() {
        assertThrows(IllegalArgumentException::class.java) {
            useCase(
                BaselineCalculationInput.ExactDates(
                    from = LocalDate.of(2026, 5, 10),
                    to = LocalDate.of(2026, 5, 1)
                )
            )
        }
    }

    // ---- Exact dates: leap year handling ----

    @Test
    fun `exact dates spanning Feb 29 in a leap year includes the leap day`() {
        // 2024 is a leap year: Feb 27, 28, 29, Mar 1 -> 4 inclusive days.
        val result = useCase(
            BaselineCalculationInput.ExactDates(
                from = LocalDate.of(2024, 2, 27),
                to = LocalDate.of(2024, 3, 1)
            )
        )

        assertEquals(4L, result.missedDays)
    }

    @Test
    fun `exact dates over the identical month-day span in a non-leap year is one day shorter`() {
        // 2023 is not a leap year: Feb 27, 28, Mar 1 -> 3 inclusive days (no Feb 29).
        val result = useCase(
            BaselineCalculationInput.ExactDates(
                from = LocalDate.of(2023, 2, 27),
                to = LocalDate.of(2023, 3, 1)
            )
        )

        assertEquals(3L, result.missedDays)
    }

    @Test
    fun `exact dates spanning a century non-leap year does not include Feb 29`() {
        // 2100 is divisible by 100 but not 400, so it is NOT a leap year despite being divisible by 4.
        val result = useCase(
            BaselineCalculationInput.ExactDates(
                from = LocalDate.of(2100, 2, 27),
                to = LocalDate.of(2100, 3, 1)
            )
        )

        assertEquals(3L, result.missedDays)
    }

    // ---- Age estimate: basic behavior ----

    @Test
    fun `age estimate derives days from an obligatory start date to the reference date`() {
        // Reference 2026-08-11, age 27 -> estimated birth 1999-08-11.
        // Obligatory age 12 -> obligatory start 2011-08-11.
        val result = useCase(
            BaselineCalculationInput.AgeEstimate(
                ageNowYears = 27,
                ageObligatoryYears = 12,
                referenceDate = LocalDate.of(2026, 8, 11)
            )
        )

        val expectedDays = java.time.temporal.ChronoUnit.DAYS.between(
            LocalDate.of(2011, 8, 11),
            LocalDate.of(2026, 8, 11)
        )
        assertEquals(expectedDays, result.missedDays)
        assertEquals(CalculationMethod.AGE_ESTIMATE, result.method)
    }

    @Test
    fun `age estimate applies the same count to all five prayer types`() {
        val result = useCase(
            BaselineCalculationInput.AgeEstimate(
                ageNowYears = 20,
                ageObligatoryYears = 12,
                referenceDate = LocalDate.of(2026, 8, 11)
            )
        )

        assertEquals(PrayerType.entries.size, result.countsByPrayerType.size)
        assertEquals(1, result.countsByPrayerType.values.toSet().size)
    }

    @Test
    fun `age estimate rejects an obligatory age of zero or less`() {
        assertThrows(IllegalArgumentException::class.java) {
            useCase(
                BaselineCalculationInput.AgeEstimate(
                    ageNowYears = 20,
                    ageObligatoryYears = 0,
                    referenceDate = LocalDate.of(2026, 8, 11)
                )
            )
        }
    }

    @Test
    fun `age estimate rejects a current age that has not reached the obligatory age`() {
        assertThrows(IllegalArgumentException::class.java) {
            useCase(
                BaselineCalculationInput.AgeEstimate(
                    ageNowYears = 10,
                    ageObligatoryYears = 12,
                    referenceDate = LocalDate.of(2026, 8, 11)
                )
            )
        }
    }

    // ---- Age estimate: leap year handling ----

    @Test
    fun `age estimate span including Feb 29 counts 366 days`() {
        // Reference 2024-03-01, age 2 -> estimated birth 2022-03-01.
        // Obligatory age 1 -> obligatory start 2023-03-01.
        // 2023-03-01 to 2024-03-01 crosses Feb 29 2024 -> 366 days.
        val result = useCase(
            BaselineCalculationInput.AgeEstimate(
                ageNowYears = 2,
                ageObligatoryYears = 1,
                referenceDate = LocalDate.of(2024, 3, 1)
            )
        )

        assertEquals(366L, result.missedDays)
    }

    @Test
    fun `age estimate span over the same month-day window without a leap day counts 365 days`() {
        // Reference 2023-03-01, age 2 -> estimated birth 2021-03-01.
        // Obligatory age 1 -> obligatory start 2022-03-01.
        // 2022-03-01 to 2023-03-01 crosses Feb 2023, which is not a leap year -> 365 days.
        val result = useCase(
            BaselineCalculationInput.AgeEstimate(
                ageNowYears = 2,
                ageObligatoryYears = 1,
                referenceDate = LocalDate.of(2023, 3, 1)
            )
        )

        assertEquals(365L, result.missedDays)
    }

    @Test
    fun `age estimate rolls a Feb 29 birth estimate down to Feb 28 in a non-leap target year`() {
        // Reference is itself Feb 29 2028 (a leap year).
        // Age 4 -> estimated birth 2028-02-29 minus 4 years = 2024-02-29 (2024 is a leap year, valid).
        // Obligatory age 2 -> 2024-02-29 plus 2 years = 2026-02-28 (2026 is not a leap year;
        // java.time rolls the day down to the last valid day of February instead of overflowing
        // into March, which is exactly the leap-year pitfall a hand-rolled `years * 365` would miss).
        val result = useCase(
            BaselineCalculationInput.AgeEstimate(
                ageNowYears = 4,
                ageObligatoryYears = 2,
                referenceDate = LocalDate.of(2028, 2, 29)
            )
        )

        val expectedDays = java.time.temporal.ChronoUnit.DAYS.between(
            LocalDate.of(2026, 2, 28),
            LocalDate.of(2028, 2, 29)
        )
        assertEquals(731L, expectedDays)
        assertEquals(731L, result.missedDays)
    }
}
