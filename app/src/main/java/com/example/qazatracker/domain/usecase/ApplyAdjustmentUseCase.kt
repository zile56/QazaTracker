package com.example.qazatracker.domain.usecase

import com.example.qazatracker.domain.model.AdjustmentReason
import com.example.qazatracker.domain.model.PrayerType
import com.example.qazatracker.domain.repository.QazaRepository
import java.time.Instant
import javax.inject.Inject

class ApplyAdjustmentUseCase(
    private val repository: QazaRepository,
    private val now: () -> Instant = Instant::now
) {
    // Dagger can't resolve a `() -> Instant` binding and ignores Kotlin default
    // parameter values, so Hilt is pointed at this delegating constructor instead.
    @Inject constructor(repository: QazaRepository) : this(repository, Instant::now)

    suspend operator fun invoke(
        prayerType: PrayerType,
        delta: Int,
        reason: AdjustmentReason,
        note: String? = null
    ) {
        require(delta != 0) { "Adjustment delta must be non-zero" }
        repository.applyAdjustment(prayerType, delta, reason, note, now())
    }
}
