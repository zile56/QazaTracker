package com.zilehasnain.qazatracker.domain.usecase

import com.zilehasnain.qazatracker.domain.model.CalculationMethod
import com.zilehasnain.qazatracker.domain.model.PrayerType
import com.zilehasnain.qazatracker.domain.repository.QazaRepository
import java.time.Instant
import javax.inject.Inject

/** Persists the user-reviewed baseline counts from the baseline summary screen. */
class ConfirmBaselineUseCase(
    private val repository: QazaRepository,
    private val now: () -> Instant = Instant::now
) {
    // Dagger can't resolve a `() -> Instant` binding and ignores Kotlin default
    // parameter values, so Hilt is pointed at this delegating constructor instead.
    @Inject constructor(repository: QazaRepository) : this(repository, Instant::now)

    suspend operator fun invoke(counts: Map<PrayerType, Int>, method: CalculationMethod) {
        val calculatedAt = now()
        counts.forEach { (prayerType, count) ->
            repository.setBaseline(prayerType, count, calculatedAt, method)
        }
    }
}
