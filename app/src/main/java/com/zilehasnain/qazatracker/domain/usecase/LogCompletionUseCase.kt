package com.zilehasnain.qazatracker.domain.usecase

import com.zilehasnain.qazatracker.domain.model.CompletionEntry
import com.zilehasnain.qazatracker.domain.model.PrayerType
import com.zilehasnain.qazatracker.domain.repository.QazaRepository
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class LogCompletionUseCase(
    private val repository: QazaRepository,
    private val now: () -> Instant = Instant::now
) {
    // Dagger can't resolve a `() -> Instant` binding and ignores Kotlin default
    // parameter values, so Hilt is pointed at this delegating constructor instead.
    @Inject constructor(repository: QazaRepository) : this(repository, Instant::now)

    /** Logs a single completion for one prayer type, right now. */
    suspend operator fun invoke(prayerType: PrayerType) {
        repository.recordCompletion(CompletionEntry(prayerType = prayerType, timestamp = now()))
    }

    /** Logs [days] worth of completions across [prayerTypes], grouped under one batch id. */
    suspend fun batch(prayerTypes: List<PrayerType>, days: Int) {
        require(prayerTypes.isNotEmpty()) { "Select at least one prayer type" }
        require(days > 0) { "Days must be positive" }

        val batchId = UUID.randomUUID().toString()
        val timestamp = now()
        val entries = buildList {
            repeat(days) {
                prayerTypes.forEach { prayerType ->
                    add(CompletionEntry(prayerType = prayerType, timestamp = timestamp, batchId = batchId))
                }
            }
        }
        repository.recordCompletions(entries)
    }
}
