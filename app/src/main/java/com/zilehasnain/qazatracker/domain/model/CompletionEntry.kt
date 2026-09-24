package com.zilehasnain.qazatracker.domain.model

import java.time.Instant

data class CompletionEntry(
    val prayerType: PrayerType,
    val timestamp: Instant,
    val batchId: String? = null
)
