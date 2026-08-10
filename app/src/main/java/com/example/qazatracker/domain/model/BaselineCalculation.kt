package com.example.qazatracker.domain.model

import java.time.LocalDate

sealed interface BaselineCalculationInput {
    /** Missed days computed inclusively: both [from] and [to] count as missed days. */
    data class ExactDates(
        val from: LocalDate,
        val to: LocalDate
    ) : BaselineCalculationInput

    /**
     * [ageNowYears] and [ageObligatoryYears] estimate a birth date and an obligatory-start date
     * from [referenceDate] (defaults to today). The obligatory age is user-adjustable, not a
     * fixed puberty age.
     */
    data class AgeEstimate(
        val ageNowYears: Int,
        val ageObligatoryYears: Int,
        val referenceDate: LocalDate = LocalDate.now()
    ) : BaselineCalculationInput
}

data class BaselineCalculation(
    val missedDays: Long,
    val countsByPrayerType: Map<PrayerType, Int>,
    val method: CalculationMethod
)
