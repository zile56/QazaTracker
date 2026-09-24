package com.zilehasnain.qazatracker.domain.usecase

import com.zilehasnain.qazatracker.domain.model.BaselineCalculation
import com.zilehasnain.qazatracker.domain.model.BaselineCalculationInput
import com.zilehasnain.qazatracker.domain.model.CalculationMethod
import com.zilehasnain.qazatracker.domain.model.PrayerType
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * Turns either an exact date range or an age estimate into a missed-day count, applied
 * uniformly across all five prayer types. Always uses java.time date arithmetic so leap
 * years are handled by the platform rather than an approximation like `years * 365`.
 */
class CalculateBaselineUseCase @Inject constructor() {

    operator fun invoke(input: BaselineCalculationInput): BaselineCalculation {
        val (missedDays, method) = when (input) {
            is BaselineCalculationInput.ExactDates -> calculateFromDates(input)
            is BaselineCalculationInput.AgeEstimate -> calculateFromAge(input)
        }

        val countsByPrayerType = PrayerType.entries.associateWith { missedDays.toInt() }
        return BaselineCalculation(missedDays, countsByPrayerType, method)
    }

    private fun calculateFromDates(
        input: BaselineCalculationInput.ExactDates
    ): Pair<Long, CalculationMethod> {
        require(!input.to.isBefore(input.from)) {
            "End date (${input.to}) must not be before start date (${input.from})"
        }
        // Inclusive of both endpoints: the first and last day both had missed prayers.
        val missedDays = ChronoUnit.DAYS.between(input.from, input.to) + 1
        return missedDays to CalculationMethod.EXACT_DATES
    }

    private fun calculateFromAge(
        input: BaselineCalculationInput.AgeEstimate
    ): Pair<Long, CalculationMethod> {
        require(input.ageObligatoryYears > 0) {
            "Age prayer became obligatory must be positive"
        }
        require(input.ageNowYears > input.ageObligatoryYears) {
            "Current age must be greater than the age prayer became obligatory"
        }

        // minusYears/plusYears correctly roll a Feb 29 estimate down to Feb 28 in
        // non-leap target years, rather than overflowing into March.
        val estimatedBirthDate = input.referenceDate.minusYears(input.ageNowYears.toLong())
        val obligatoryStartDate = estimatedBirthDate.plusYears(input.ageObligatoryYears.toLong())
        val missedDays = ChronoUnit.DAYS.between(obligatoryStartDate, input.referenceDate)
        return missedDays to CalculationMethod.AGE_ESTIMATE
    }
}
